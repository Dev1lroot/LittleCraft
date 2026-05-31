/*
 * Formabitur — block-model JSON → EntityModel geometry bridge.
 * Part of mclib, a shared utility library for dev1lroot's Minecraft mods.
 *
 * Converts Minecraft block model JSON elements into ModelPart.Cube objects
 * with exact per-face UV mapping, UV inversion, texture rotation, and
 * element geometry rotation.
 *
 * ── UV conventions ────────────────────────────────────────────────────────
 * Block model JSON UV is in 0–16 space (16 = full texture edge).
 * Normalised 0–1 UV = json_uv / 16.
 *
 * ── Geometry rotation ─────────────────────────────────────────────────────
 * Element "rotation" (origin, axis, angle, rescale) is applied to all eight
 * cube corners before building face polygons.  Face normals are derived from
 * the actual (post-rotation) edge cross-product so lighting is correct.
 *
 * ── Texture rotation ──────────────────────────────────────────────────────
 * Face "rotation" (0/90/180/270°) shifts the base UV index per vertex by
 * (i + shift) % 4, matching Quadrant.rotateVertexIndex() in vanilla.
 *
 * ── Up / Down axis ────────────────────────────────────────────────────────
 * Entity model Y increases toward the feet; block model Y increases upward.
 * JSON "up"   → Direction.UP   polygon (entity maxY)
 * JSON "down" → Direction.DOWN polygon (entity minY)
 */

package com.dev1lroot.mclib.formabitur;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class Formabitur
{
    private Formabitur() {}

    // ── Corner indices per face direction (from FaceInfo static initialiser) ─
    // Each int[4] gives the four p[] indices in FaceInfo vertex order.
    private static final Map<Direction, int[]> FACE_CORNERS = Map.of(
        Direction.NORTH, new int[]{2, 1, 0, 3},
        Direction.SOUTH, new int[]{7, 4, 5, 6},
        Direction.WEST,  new int[]{3, 0, 4, 7},
        Direction.EAST,  new int[]{6, 5, 1, 2},
        Direction.DOWN,  new int[]{4, 0, 1, 5},  // entity minY = JSON "up"
        Direction.UP,    new int[]{3, 7, 6, 2}   // entity maxY = JSON "down"
    );

    // ── Public API ────────────────────────────────────────────────────────

    /**
     * Builds a single root {@link ModelPart} from a block model JSON resource.
     *
     * @param rm        active resource manager
     * @param modelPath full asset identifier with path and {@code .json} extension
     * @param texWidth  texture width in pixels
     * @param texHeight texture height in pixels
     * @param inflate   uniform outward grow per cube
     * @return root ModelPart; empty if the resource is absent
     */
    public static ModelPart buildFromBlockModel(
        ResourceManager rm, Identifier modelPath,
        float texWidth, float texHeight, float inflate)
    {
        JsonObject json = loadJson(rm, modelPath);
        if (json == null)
        {
            System.err.println("[Formabitur] Missing block model: " + modelPath
                + " — ModelPart will be empty (invisible).");
            return new ModelPart(List.of(), Map.of());
        }

        List<ModelPart.Cube> cubes = new ArrayList<>();
        if (json.has("elements"))
        {
            for (JsonElement el : json.getAsJsonArray("elements"))
                cubes.add(buildCube(el.getAsJsonObject(), inflate));
        }
        return new ModelPart(cubes, Map.of());
    }

    // ── JSON loading ──────────────────────────────────────────────────────

    private static JsonObject loadJson(ResourceManager rm, Identifier path)
    {
        var opt = rm.getResource(path);
        if (opt.isEmpty()) return null;
        try (Reader r = new InputStreamReader(opt.get().open(), StandardCharsets.UTF_8))
        {
            return JsonParser.parseReader(r).getAsJsonObject();
        }
        catch (IOException e)
        {
            System.err.println("[Formabitur] Failed to read " + path + ": " + e.getMessage());
            return null;
        }
    }

    // ── Cube construction ─────────────────────────────────────────────────

    private static ModelPart.Cube buildCube(JsonObject el, float inflate)
    {
        JsonArray from = el.getAsJsonArray("from");
        JsonArray to   = el.getAsJsonArray("to");

        float x0 = from.get(0).getAsFloat(), y0 = from.get(1).getAsFloat(), z0 = from.get(2).getAsFloat();
        float x1 = to.get(0).getAsFloat(),   y1 = to.get(1).getAsFloat(),   z1 = to.get(2).getAsFloat();

        // Inflated bounds.
        float ax0 = x0 - inflate, ay0 = y0 - inflate, az0 = z0 - inflate;
        float ax1 = x1 + inflate, ay1 = y1 + inflate, az1 = z1 + inflate;

        // Build the 8 corners of the (inflated) cube, then optionally rotate them.
        float[][] corners = corners(ax0, ay0, az0, ax1, ay1, az1);
        if (el.has("rotation"))
            rotateCorners(el.getAsJsonObject("rotation"), corners);

        // Collect per-face UV + rotation data.
        Map<Direction, float[]> faceUvs = new EnumMap<>(Direction.class);
        if (el.has("faces"))
        {
            JsonObject faces = el.getAsJsonObject("faces");
            readFace(faces, "north", Direction.NORTH, faceUvs);
            readFace(faces, "south", Direction.SOUTH, faceUvs);
            readFace(faces, "east",  Direction.EAST,  faceUvs);
            readFace(faces, "west",  Direction.WEST,  faceUvs);
            readFace(faces, "up",    Direction.UP,    faceUvs);
            readFace(faces, "down",  Direction.DOWN,  faceUvs);
        }

        Set<Direction> visibleDirs = faceUvs.isEmpty()
            ? EnumSet.allOf(Direction.class)
            : EnumSet.copyOf(faceUvs.keySet());

        // Create the Cube for its bounding box; all polygons are replaced below.
        ModelPart.Cube cube = new ModelPart.Cube(
            0, 0,
            ax0, ay0, az0,
            ax1 - ax0, ay1 - ay0, az1 - az0,
            0f, 0f, 0f,
            false,
            1f, 1f,
            visibleDirs
        );

        for (int i = 0; i < cube.polygons.length; i++)
        {
            Direction dir = directionOf(cube.polygons[i].normal());
            if (dir == null) continue;
            float[] uv = faceUvs.get(dir);
            if (uv == null) continue;
            cube.polygons[i] = facePolygon(corners, uv, dir);
        }

        return cube;
    }

    // ── Eight-corner helpers ──────────────────────────────────────────────

    /** Returns the 8 corners of the axis-aligned box in the FaceInfo p[] order. */
    private static float[][] corners(float x0, float y0, float z0,
                                      float x1, float y1, float z1)
    {
        return new float[][]{
            {x0, y0, z0}, // p0  MIN_X MIN_Y MIN_Z
            {x1, y0, z0}, // p1  MAX_X MIN_Y MIN_Z
            {x1, y1, z0}, // p2  MAX_X MAX_Y MIN_Z
            {x0, y1, z0}, // p3  MIN_X MAX_Y MIN_Z
            {x0, y0, z1}, // p4  MIN_X MIN_Y MAX_Z
            {x1, y0, z1}, // p5  MAX_X MIN_Y MAX_Z
            {x1, y1, z1}, // p6  MAX_X MAX_Y MAX_Z
            {x0, y1, z1}, // p7  MIN_X MAX_Y MAX_Z
        };
    }

    /**
     * Applies the element-level geometry rotation (origin, axis, angle, rescale)
     * to every corner in-place.  Mirrors CuboidRotation.computeTransform().
     */
    /**
     * Two formats supported:
     * • Single-axis (standard): {"axis":"y","angle":45,"origin":[…],"rescale":false}
     * • Euler XYZ (Blockbench ≥ 1.21.11): {"x":0,"y":-75,"z":0,"origin":[…]}
     */
    private static void rotateCorners(JsonObject rot, float[][] corners)
    {
        JsonArray orig = rot.getAsJsonArray("origin");
        float ox = orig.get(0).getAsFloat() / 16f;
        float oy = orig.get(1).getAsFloat() / 16f;
        float oz = orig.get(2).getAsFloat() / 16f;

        if (rot.has("axis"))
        {
            // Standard single-axis block model rotation
            String axis     = rot.get("axis").getAsString();
            float  angleDeg = rot.has("angle") ? rot.get("angle").getAsFloat() : 0f;
            if (angleDeg != 0f)
            {
                boolean rescale = rot.has("rescale") && rot.get("rescale").getAsBoolean();
                rotateCornersAxis(corners, ox, oy, oz, axis, angleDeg, rescale);
            }
        }
        else
        {
            // Euler XYZ rotation (Blockbench format_version ≥ 1.21.11) — apply X → Y → Z
            float ax = rot.has("x") ? rot.get("x").getAsFloat() : 0f;
            float ay = rot.has("y") ? rot.get("y").getAsFloat() : 0f;
            float az = rot.has("z") ? rot.get("z").getAsFloat() : 0f;
            if (ax != 0f) rotateCornersAxis(corners, ox, oy, oz, "x", ax, false);
            if (ay != 0f) rotateCornersAxis(corners, ox, oy, oz, "y", ay, false);
            if (az != 0f) rotateCornersAxis(corners, ox, oy, oz, "z", az, false);
        }
    }

    private static void rotateCornersAxis(
        float[][] corners, float ox, float oy, float oz,
        String axis, float angleDeg, boolean rescale)
    {
        double angle = Math.toRadians(angleDeg);
        float  cos   = (float) Math.cos(angle);
        float  sin   = (float) Math.sin(angle);
        float  scale = (rescale && Math.abs(sin) > 1e-6f) ? 1f / cos : 1f;

        for (float[] c : corners)
        {
            float dx = c[0] / 16f - ox;
            float dy = c[1] / 16f - oy;
            float dz = c[2] / 16f - oz;

            float rx, ry, rz;
            switch (axis)
            {
                case "y"  -> { rx =  dx*cos + dz*sin; ry = dy;               rz = -dx*sin + dz*cos; }
                case "x"  -> { rx = dx;               ry =  dy*cos - dz*sin; rz =  dy*sin + dz*cos; }
                default   -> { rx =  dx*cos - dy*sin; ry =  dx*sin + dy*cos; rz = dz;               }
            }

            if (rescale)
            {
                switch (axis)
                {
                    case "y"  -> { rx *= scale; rz *= scale; }
                    case "x"  -> { ry *= scale; rz *= scale; }
                    default   -> { rx *= scale; ry *= scale; }
                }
            }

            c[0] = (ox + rx) * 16f;
            c[1] = (oy + ry) * 16f;
            c[2] = (oz + rz) * 16f;
        }
    }

    // ── Face UV reading ───────────────────────────────────────────────────

    private static void readFace(
        JsonObject faces, String key, Direction dir, Map<Direction, float[]> out)
    {
        if (!faces.has(key)) return;
        JsonObject face = faces.getAsJsonObject(key);
        JsonArray uv = face.getAsJsonArray("uv");
        int shift = face.has("rotation") ? face.get("rotation").getAsInt() / 90 : 0;
        out.put(dir, new float[]{
            uv.get(0).getAsFloat(), uv.get(1).getAsFloat(),
            uv.get(2).getAsFloat(), uv.get(3).getAsFloat(),
            shift
        });
    }

    // ── Per-face polygon construction ─────────────────────────────────────

    private static ModelPart.Polygon facePolygon(
        float[][] corners, float[] uv, Direction dir)
    {
        float u0 = uv[0] / 16f, v0 = uv[1] / 16f;
        float u1 = uv[2] / 16f, v1 = uv[3] / 16f;

        // Base UV lookup: idx 0→(u0,v0) 1→(u0,v1) 2→(u1,v1) 3→(u1,v0)
        // Texture rotation shifts which base entry each vertex gets.
        int shift = (int)(uv[4] + 0.5f);
        float[][] base = {{u0,v0},{u0,v1},{u1,v1},{u1,v0}};
        float[] b0 = base[shift       % 4];
        float[] b1 = base[(shift + 1) % 4];
        float[] b2 = base[(shift + 2) % 4];
        float[] b3 = base[(shift + 3) % 4];

        int[] ci = FACE_CORNERS.get(dir);
        float[] c0 = corners[ci[0]], c1 = corners[ci[1]],
                c2 = corners[ci[2]], c3 = corners[ci[3]];

        ModelPart.Vertex[] v = {
            new ModelPart.Vertex(c0[0], c0[1], c0[2], b0[0], b0[1]),
            new ModelPart.Vertex(c1[0], c1[1], c1[2], b1[0], b1[1]),
            new ModelPart.Vertex(c2[0], c2[1], c2[2], b2[0], b2[1]),
            new ModelPart.Vertex(c3[0], c3[1], c3[2], b3[0], b3[1])
        };

        // Compute the actual face normal from edge cross-product so rotated
        // geometry gets correct lighting (falls back to dir.getUnitVec3f() for
        // degenerate / zero-area faces).
        float ex = c1[0]-c0[0], ey = c1[1]-c0[1], ez = c1[2]-c0[2];
        float fx = c3[0]-c0[0], fy = c3[1]-c0[1], fz = c3[2]-c0[2];
        float nx = ey*fz - ez*fy, ny = ez*fx - ex*fz, nz = ex*fy - ey*fx;
        float len = (float) Math.sqrt(nx*nx + ny*ny + nz*nz);
        Vector3fc normal = (len > 1e-6f)
            ? new Vector3f(nx/len, ny/len, nz/len)
            : dir.getUnitVec3f();

        return new ModelPart.Polygon(v, normal);
    }

    // ── Direction detection ───────────────────────────────────────────────

    private static Direction directionOf(Vector3fc n)
    {
        float ax = Math.abs(n.x()), ay = Math.abs(n.y()), az = Math.abs(n.z());
        if (az >= ax && az >= ay) return n.z() > 0 ? Direction.SOUTH : Direction.NORTH;
        if (ax >= ay)             return n.x() > 0 ? Direction.EAST  : Direction.WEST;
        return                           n.y() > 0 ? Direction.UP    : Direction.DOWN;
    }
}
