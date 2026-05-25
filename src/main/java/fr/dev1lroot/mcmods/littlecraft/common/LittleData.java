package fr.dev1lroot.mcmods.littlecraft.common;

import com.mojang.serialization.Codec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import static fr.dev1lroot.mcmods.littlecraft.LittleMod.MODID;

@Mod(MODID)
public class LittleData
{
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
        DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, MODID);

    @SuppressWarnings("unchecked")
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> LITTLE =
        (DeferredHolder<AttachmentType<?>, AttachmentType<Boolean>>) ATTACHMENT_TYPES.register("little", () ->
            AttachmentType.builder(() -> false)
                .serialize(Codec.BOOL.optionalFieldOf("value", false))
                .copyOnDeath()
                .sync(ByteBufCodecs.BOOL)
                .build());

    @SuppressWarnings("unchecked")
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> LITTLEAGE =
        (DeferredHolder<AttachmentType<?>, AttachmentType<Integer>>) ATTACHMENT_TYPES.register("little_age", () ->
            AttachmentType.builder(() -> 0)
                .serialize(Codec.INT.optionalFieldOf("value", 0))
                .copyOnDeath()
                .sync(ByteBufCodecs.VAR_INT)
                .build());

    public LittleData(IEventBus modBus)
    {
        ATTACHMENT_TYPES.register(modBus);
    }

    public static void set(Player player, boolean value)
    {
        player.setData(LITTLE.get(), value);
    }

    public static boolean get(Player player)
    {
        return player.getData(LITTLE.get());
    }

    public static void setAge(Player player, int value)
    {
        player.setData(LITTLEAGE.get(), value);
    }

    public static int getAge(Player player)
    {
        return player.getData(LITTLEAGE.get());
    }
}
