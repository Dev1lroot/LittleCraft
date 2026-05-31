/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.client;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static fr.dev1lroot.mcmods.littlecraft.LittleMod.MODID;

/**
 * General-purpose CPU-side texture compositor.
 *
 * <p>Usage:
 * <pre>{@code
 *   Identifier result = new TextureCompositor()
 *       .addLayer(baseTexture)
 *       .addLayer(overlayTexture, wetnessAlpha)
 *       .compile();
 * }</pre>
 *
 * <p>Layers are composited left-to-right using Porter-Duff "over".
 * Each layer's per-pixel alpha is additionally multiplied by the alpha
 * passed to {@link #addLayer(Identifier, int)}.
 *
 * <p>All source {@link NativeImage}s and compiled {@link DynamicTexture}s are
 * cached statically and shared across instances. Call {@link #clearAll()} on
 * resource reload to release everything.
 *
 * <p>Alpha bucketing is the caller's responsibility — round the alpha value
 * before calling {@code addLayer} to control cache granularity.
 */
public final class TextureCompositor {

    // ── Static caches ────────────────────────────────────────────────────────

    /** Source images loaded from the resource manager; closed in {@link #clearAll()}. */
    private static final Map<String, NativeImage> SOURCE_CACHE   = new HashMap<>();
    /** Identifiers of compiled {@link DynamicTexture}s keyed by layer-stack fingerprint. */
    private static final Map<String, Identifier>  COMPILED_CACHE = new HashMap<>();
    /** Identifiers that failed to load; avoids repeated I/O on missing resources. */
    private static final Set<String>              MISSING        = new HashSet<>();

    // ── Instance state ───────────────────────────────────────────────────────

    private final List<Layer> layers = new ArrayList<>();

    private record Layer(Identifier path, int alpha) {}

    // ── Public API ───────────────────────────────────────────────────────────

    /**
     * Adds a fully-opaque layer (alpha = 255).
     *
     * @return {@code this} for fluent chaining
     */
    public TextureCompositor addLayer(Identifier path) {
        return addLayer(path, 255);
    }

    /**
     * Adds a layer composited at {@code alpha} (0–255).
     * The layer's own per-pixel alpha is multiplied by this value.
     *
     * @return {@code this} for fluent chaining
     */
    public TextureCompositor addLayer(Identifier path, int alpha) {
        layers.add(new Layer(path, Math.clamp(alpha, 0, 255)));
        return this;
    }

    /**
     * Composites all layers and returns an {@link Identifier} pointing to the result.
     *
     * <p>Fast-path: a single fully-opaque layer returns its {@code path} directly
     * without uploading anything to the GPU.
     *
     * <p>Falls back to the first layer's path if no source image can be loaded.
     *
     * @throws IllegalStateException if no layers were added
     */
    public Identifier compile() {
        if (layers.isEmpty())
            throw new IllegalStateException("TextureCompositor.compile() called with no layers");

        // Fast-path: nothing to composite.
        if (layers.size() == 1 && layers.getFirst().alpha() == 255)
            return layers.getFirst().path();

        String key = buildKey();
        Identifier cached = COMPILED_CACHE.get(key);
        if (cached != null) return cached;

        // Composite layers left-to-right into a mutable NativeImage.
        NativeImage canvas = null;
        for (Layer layer : layers) {
            NativeImage src = loadOnce(layer.path());
            if (src == null) continue;
            if (canvas == null) {
                canvas = copyWithAlpha(src, layer.alpha());
            } else {
                blendOver(canvas, src, layer.alpha());
            }
        }

        if (canvas == null)
            return layers.getFirst().path();

        String texPath = "texture_compositor/" + String.format("%08x", key.hashCode() & 0x7FFFFFFF);
        Identifier id  = Identifier.fromNamespaceAndPath(MODID, texPath);

        // DynamicTexture(Supplier<String> label, NativeImage) uploads immediately.
        Minecraft.getInstance().getTextureManager()
                .register(id, new DynamicTexture(() -> texPath, canvas));

        COMPILED_CACHE.put(key, id);
        return id;
    }

    /**
     * Releases all compiled GPU textures and closes all cached source images.
     * Must be called on resource reload (e.g. from an {@code AddClientReloadListenersEvent}).
     */
    public static void clearAll() {
        var tm = Minecraft.getInstance().getTextureManager();
        COMPILED_CACHE.values().forEach(tm::release);
        COMPILED_CACHE.clear();

        SOURCE_CACHE.values().forEach(NativeImage::close);
        SOURCE_CACHE.clear();

        MISSING.clear();
    }

    // ── Internal helpers ─────────────────────────────────────────────────────

    /** Builds a stable cache key from the ordered layer stack. */
    private String buildKey() {
        var sb = new StringBuilder();
        for (var l : layers) {
            if (!sb.isEmpty()) sb.append('|');
            sb.append(l.path()).append(':').append(l.alpha());
        }
        return sb.toString();
    }

    /** Loads and caches a {@link NativeImage}; returns {@code null} on missing / I/O error. */
    private static NativeImage loadOnce(Identifier id) {
        String key = id.toString();
        NativeImage hit = SOURCE_CACHE.get(key);
        if (hit != null) return hit;
        if (MISSING.contains(key)) return null;

        try {
            var res = Minecraft.getInstance().getResourceManager().getResource(id);
            if (res.isEmpty()) { MISSING.add(key); return null; }
            try (var stream = res.get().open()) {
                NativeImage img = NativeImage.read(stream);
                SOURCE_CACHE.put(key, img);
                return img;
            }
        } catch (IOException e) {
            MISSING.add(key);
            return null;
        }
    }

    /**
     * Returns a new mutable copy of {@code src} with every pixel's alpha
     * scaled by {@code alphaScale / 255}.
     */
    private static NativeImage copyWithAlpha(NativeImage src, int alphaScale) {
        int w = src.getWidth(), h = src.getHeight();
        NativeImage out = new NativeImage(w, h, false);
        if (alphaScale == 255) {
            for (int y = 0; y < h; y++)
                for (int x = 0; x < w; x++)
                    out.setPixel(x, y, src.getPixel(x, y));
        } else {
            for (int y = 0; y < h; y++)
                for (int x = 0; x < w; x++)
                    out.setPixel(x, y, scaleAlpha(src.getPixel(x, y), alphaScale));
        }
        return out;
    }

    /**
     * Composites {@code top} over {@code canvas} in-place.
     * Top pixels' alpha is first multiplied by {@code topAlphaScale / 255}.
     * Only the overlapping region (min dimensions) is modified.
     */
    private static void blendOver(NativeImage canvas, NativeImage top, int topAlphaScale) {
        int w = Math.min(canvas.getWidth(),  top.getWidth());
        int h = Math.min(canvas.getHeight(), top.getHeight());
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++)
                canvas.setPixel(x, y, over(canvas.getPixel(x, y), top.getPixel(x, y), topAlphaScale));
    }

    /** Scales a pixel's alpha channel by {@code scale / 255}; RGB is unchanged. */
    private static int scaleAlpha(int px, int scale) {
        int a = ((px >> 24) & 0xFF) * scale / 255;
        return (px & 0x00FFFFFF) | (a << 24);
    }

    /**
     * Porter-Duff "over": {@code top} composited over {@code base}.
     * Top's per-pixel alpha is additionally scaled by {@code topAlphaScale / 255}.
     *
     * <p>ARGB layout (as used by {@link NativeImage#getPixel}):
     * A = bits 24–31, R = 16–23, G = 8–15, B = 0–7.
     */
    private static int over(int base, int top, int topAlphaScale) {
        int bA = (base >> 24) & 0xFF;
        int bR = (base >> 16) & 0xFF, bG = (base >> 8) & 0xFF, bB = base & 0xFF;

        int tA = ((top >> 24) & 0xFF) * topAlphaScale / 255;
        int tR = (top >> 16) & 0xFF,  tG = (top >> 8) & 0xFF,  tB = top  & 0xFF;

        int rA = bA + tA * (255 - bA) / 255;
        if (rA == 0) return 0;

        int rR = (bR * bA * (255 - tA) / 255 + tR * tA) / rA;
        int rG = (bG * bA * (255 - tA) / 255 + tG * tA) / rA;
        int rB = (bB * bA * (255 - tA) / 255 + tB * tA) / rA;

        return (rA << 24) | (rR << 16) | (rG << 8) | rB;
    }
}
