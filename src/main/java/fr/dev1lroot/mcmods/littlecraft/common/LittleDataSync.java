/*
 * Copyright (c) 2026 David Eichendorf <admin@dev1lroot.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package fr.dev1lroot.mcmods.littlecraft.common;

import net.neoforged.fml.common.EventBusSubscriber;

// Clone and load sync are now handled automatically by the serializable+copyOnDeath+synced attachment in LittleData.
@EventBusSubscriber(modid = "littlecraft")
public class LittleDataSync {}
