package fr.dev1lroot.mcmods.littlecraft.content;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import static fr.dev1lroot.mcmods.littlecraft.LittleMod.MODID;

public class LittleContentRegistry
{
    // Все реестры
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, MODID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, MODID);

    public static void register(IEventBus bus)
    {
        ITEMS.register(bus);
        BLOCKS.register(bus);

        // Регистрируем отдельные классы контента
        fr.dev1lroot.mcmods.littlecraft.content.item.Diaper.register();
        // fr.dev1lroot.mcmods.littlecraft.content.block.MyBlock.register();
    }
}
