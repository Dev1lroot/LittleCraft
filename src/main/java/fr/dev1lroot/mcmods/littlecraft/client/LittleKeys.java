/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

public class LittleKeys
{
    public static final KeyMapping KEY_PISS = new KeyMapping(
            "key.littlecraft.piss",
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            KeyMapping.Category.GAMEPLAY
    );

    public static final KeyMapping KEY_POOP = new KeyMapping(
            "key.littlecraft.poop",
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            KeyMapping.Category.GAMEPLAY
    );
}
