package net.minecraft.server.level;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.hash.HashCode;
import com.google.common.net.InetAddresses;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.chat.ResolutionContext;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundShowDialogPacket;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundHurtAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket;
import net.minecraft.network.protocol.game.ClientboundMountScreenOpenPacket;
import net.minecraft.network.protocol.game.ClientboundOpenBookPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ClientboundOpenSignEditorPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatEndPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatEnterPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerLookAtPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerRotationPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundServerDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket;
import net.minecraft.network.protocol.game.ClientboundSetCursorItemPacket;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.network.protocol.game.CommonPlayerSpawnInfo;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.TextFilter;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.waypoints.ServerWaypointManager;
import net.minecraft.stats.ServerRecipeBook;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.HashOps;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.util.debug.DebugSubscription;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.Container;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.attribute.BedRule;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.entity.animal.nautilus.AbstractNautilus;
import net.minecraft.world.entity.animal.parrot.Parrot;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.monster.warden.WardenSpawnTracker;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.ContainerSynchronizer;
import net.minecraft.world.inventory.HorseInventoryMenu;
import net.minecraft.world.inventory.NautilusInventoryMenu;
import net.minecraft.world.inventory.RemoteSlot;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.ServerItemCooldowns;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.ScoreAccess;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class ServerPlayer extends Player {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int NEUTRAL_MOB_DEATH_NOTIFICATION_RADII_XZ = 32;
    private static final int NEUTRAL_MOB_DEATH_NOTIFICATION_RADII_Y = 10;
    private static final int FLY_STAT_RECORDING_SPEED = 25;
    public static final double BLOCK_INTERACTION_DISTANCE_VERIFICATION_BUFFER = 1.0;
    public static final double ENTITY_INTERACTION_DISTANCE_VERIFICATION_BUFFER = 3.0;
    public static final int ENDER_PEARL_TICKET_RADIUS = 2;
    public static final String ENDER_PEARLS_TAG = "ender_pearls";
    public static final String ENDER_PEARL_DIMENSION_TAG = "ender_pearl_dimension";
    public static final String TAG_DIMENSION = "Dimension";
    private static final AttributeModifier CREATIVE_BLOCK_INTERACTION_RANGE_MODIFIER = new AttributeModifier(
        Identifier.withDefaultNamespace("creative_mode_block_range"), 0.5, AttributeModifier.Operation.ADD_VALUE
    );
    private static final AttributeModifier CREATIVE_ENTITY_INTERACTION_RANGE_MODIFIER = new AttributeModifier(
        Identifier.withDefaultNamespace("creative_mode_entity_range"), 2.0, AttributeModifier.Operation.ADD_VALUE
    );
    private static final Component SPAWN_SET_MESSAGE = Component.translatable("block.minecraft.set_spawn");
    private static final AttributeModifier WAYPOINT_TRANSMIT_RANGE_CROUCH_MODIFIER = new AttributeModifier(
        Identifier.withDefaultNamespace("waypoint_transmit_range_crouch"), -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
    );
    private static final boolean DEFAULT_SEEN_CREDITS = false;
    private static final boolean DEFAULT_SPAWN_EXTRA_PARTICLES_ON_FALL = false;
    public ServerGamePacketListenerImpl connection;
    private final MinecraftServer server;
    public final ServerPlayerGameMode gameMode;
    private final PlayerAdvancements advancements;
    private final ServerStatsCounter stats;
    private float lastRecordedHealthAndAbsorption = Float.MIN_VALUE;
    private int lastRecordedFoodLevel = Integer.MIN_VALUE;
    private int lastRecordedAirLevel = Integer.MIN_VALUE;
    private int lastRecordedArmor = Integer.MIN_VALUE;
    private int lastRecordedLevel = Integer.MIN_VALUE;
    private int lastRecordedExperience = Integer.MIN_VALUE;
    private float lastSentHealth = -1.0E8F;
    private int lastSentFood = -99999999;
    private boolean lastFoodSaturationZero = true;
    private int lastSentExp = -99999999;
    private ChatVisiblity chatVisibility = ChatVisiblity.FULL;
    private ParticleStatus particleStatus = ParticleStatus.ALL;
    private boolean canChatColor = true;
    private long lastActionTime = Util.getMillis();
    private @Nullable Entity camera;
    private boolean isChangingDimension;
    public boolean seenCredits = false;
    private final ServerRecipeBook recipeBook;
    private @Nullable Vec3 levitationStartPos;
    private int levitationStartTime;
    private boolean disconnected;
    private int requestedViewDistance = 2;
    private String language = "en_us";
    private @Nullable Vec3 startingToFallPosition;
    private @Nullable Vec3 enteredNetherPosition;
    private @Nullable Vec3 enteredLavaOnVehiclePosition;
    private SectionPos lastSectionPos = SectionPos.of(0, 0, 0);
    private ChunkTrackingView chunkTrackingView = ChunkTrackingView.EMPTY;
    private ServerPlayer.@Nullable RespawnConfig respawnConfig;
    private final TextFilter textFilter;
    private boolean textFilteringEnabled;
    private boolean allowsListing;
    private boolean spawnExtraParticlesOnFall = false;
    private WardenSpawnTracker wardenSpawnTracker = new WardenSpawnTracker();
    private @Nullable BlockPos raidOmenPosition;
    private Vec3 lastKnownClientMovement = Vec3.ZERO;
    private Input lastClientInput = Input.EMPTY;
    private final Set<ThrownEnderpearl> enderPearls = new HashSet<>();
    private long timeEntitySatOnShoulder;
    private CompoundTag shoulderEntityLeft = new CompoundTag();
    private CompoundTag shoulderEntityRight = new CompoundTag();
    private final ContainerSynchronizer containerSynchronizer = new ContainerSynchronizer() {
        private final LoadingCache<TypedDataComponent<?>, Integer> cache;

        {
            Objects.requireNonNull(ServerPlayer.this);
            this.cache = CacheBuilder.newBuilder()
                .maximumSize(256L)
                .build(
                    new CacheLoader<TypedDataComponent<?>, Integer>() {
                        private final DynamicOps<HashCode> registryHashOps;

                        {
                            Objects.requireNonNull(<VAR_NAMELESS_ENCLOSURE>);
                            this.registryHashOps = ServerPlayer.this.registryAccess().createSerializationContext(HashOps.CRC32C_INSTANCE);
                        }

                        public Integer load(TypedDataComponent<?> component) {
                            return component.encodeValue(this.registryHashOps)
                                .getOrThrow(msg -> new IllegalArgumentException("Failed to hash " + component + ": " + msg))
                                .asInt();
                        }
                    }
                );
        }

        @Override
        public void sendInitialData(AbstractContainerMenu container, List<ItemStack> slotItems, ItemStack carriedItem, int[] dataSlots) {
            ServerPlayer.this.connection
                .send(new ClientboundContainerSetContentPacket(container.containerId, container.incrementStateId(), slotItems, carriedItem));

            for (int slot = 0; slot < dataSlots.length; slot++) {
                this.broadcastDataValue(container, slot, dataSlots[slot]);
            }
        }

        @Override
        public void sendSlotChange(AbstractContainerMenu container, int slotIndex, ItemStack itemStack) {
            ServerPlayer.this.connection.send(new ClientboundContainerSetSlotPacket(container.containerId, container.incrementStateId(), slotIndex, itemStack));
        }

        @Override
        public void sendCarriedChange(AbstractContainerMenu container, ItemStack itemStack) {
            ServerPlayer.this.connection.send(new ClientboundSetCursorItemPacket(itemStack));
        }

        @Override
        public void sendDataChange(AbstractContainerMenu container, int id, int value) {
            this.broadcastDataValue(container, id, value);
        }

        private void broadcastDataValue(AbstractContainerMenu container, int id, int value) {
            ServerPlayer.this.connection.send(new ClientboundContainerSetDataPacket(container.containerId, id, value));
        }

        @Override
        public RemoteSlot createSlot() {
            return new RemoteSlot.Synchronized(this.cache::getUnchecked);
        }
    };
    private final ContainerListener containerListener = new ContainerListener() {
        {
            Objects.requireNonNull(ServerPlayer.this);
        }

        @Override
        public void slotChanged(AbstractContainerMenu container, int slotIndex, ItemStack changedItem) {
            Slot slot = container.getSlot(slotIndex);
            if (!(slot instanceof ResultSlot)) {
                if (slot.container == ServerPlayer.this.getInventory()) {
                    CriteriaTriggers.INVENTORY_CHANGED.trigger(ServerPlayer.this, ServerPlayer.this.getInventory(), changedItem);
                }
            }
        }

        @Override
        public void dataChanged(AbstractContainerMenu container, int id, int value) {
        }
    };
    private @Nullable RemoteChatSession chatSession;
    public final @Nullable Object object;
    private final CommandSource commandSource = new CommandSource() {
        {
            Objects.requireNonNull(ServerPlayer.this);
        }

        @Override
        public boolean acceptsSuccess() {
            return ServerPlayer.this.level().getGameRules().get(GameRules.SEND_COMMAND_FEEDBACK);
        }

        @Override
        public boolean acceptsFailure() {
            return true;
        }

        @Override
        public boolean shouldInformAdmins() {
            return true;
        }

        @Override
        public void sendSystemMessage(Component message) {
            ServerPlayer.this.sendSystemMessage(message);
        }
    };
    private Set<DebugSubscription<?>> requestedDebugSubscriptions = Set.of();
    private int containerCounter;
    public boolean wonGame;

    public ServerPlayer(MinecraftServer server, ServerLevel level, GameProfile gameProfile, ClientInformation clientInformation) {
        super(level, gameProfile);
        this.server = server;
        this.textFilter = server.createTextFilterForPlayer(this);
        this.gameMode = server.createGameModeForPlayer(this);
        this.gameMode.setGameModeForPlayer(this.calculateGameModeForNewPlayer(null), null);
        this.recipeBook = new ServerRecipeBook((id, output) -> server.getRecipeManager().listDisplaysForRecipe(id, output));
        this.stats = server.getPlayerList().getPlayerStats(this);
        this.advancements = server.getPlayerList().getPlayerAdvancements(this);
        this.updateOptions(clientInformation);
        this.object = null;
    }

    @Override
    public BlockPos adjustSpawnLocation(ServerLevel level, BlockPos spawnSuggestion) {
        CompletableFuture<Vec3> future = PlayerSpawnFinder.findSpawn(level, spawnSuggestion);
        this.server.managedBlock(future::isDone);
        return BlockPos.containing(future.join());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.wardenSpawnTracker = input.read("warden_spawn_tracker", WardenSpawnTracker.CODEC).orElseGet(WardenSpawnTracker::new);
        this.enteredNetherPosition = input.read("entered_nether_pos", Vec3.CODEC).orElse(null);
        this.seenCredits = input.getBooleanOr("seenCredits", false);
        input.read("recipeBook", ServerRecipeBook.Packed.CODEC)
            .ifPresent(p -> this.recipeBook.loadUntrusted(p, id -> this.server.getRecipeManager().byKey(id).isPresent()));
        if (this.isSleeping()) {
            this.stopSleeping();
        }

        this.respawnConfig = input.read("respawn", ServerPlayer.RespawnConfig.CODEC).orElse(null);
        this.spawnExtraParticlesOnFall = input.getBooleanOr("spawn_extra_particles_on_fall", false);
        this.raidOmenPosition = input.read("raid_omen_position", BlockPos.CODEC).orElse(null);
        this.gameMode
            .setGameModeForPlayer(this.calculateGameModeForNewPlayer(readPlayerMode(input, "playerGameType")), readPlayerMode(input, "previousPlayerGameType"));
        this.setShoulderEntityLeft(input.read("ShoulderEntityLeft", CompoundTag.CODEC).orElseGet(CompoundTag::new));
        this.setShoulderEntityRight(input.read("ShoulderEntityRight", CompoundTag.CODEC).orElseGet(CompoundTag::new));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("warden_spawn_tracker", WardenSpawnTracker.CODEC, this.wardenSpawnTracker);
        this.storeGameTypes(output);
        output.putBoolean("seenCredits", this.seenCredits);
        output.storeNullable("entered_nether_pos", Vec3.CODEC, this.enteredNetherPosition);
        this.saveParentVehicle(output);
        output.store("recipeBook", ServerRecipeBook.Packed.CODEC, this.recipeBook.pack());
        output.putString("Dimension", this.level().dimension().identifier().toString());
        output.storeNullable("respawn", ServerPlayer.RespawnConfig.CODEC, this.respawnConfig);
        output.putBoolean("spawn_extra_particles_on_fall", this.spawnExtraParticlesOnFall);
        output.storeNullable("raid_omen_position", BlockPos.CODEC, this.raidOmenPosition);
        this.saveEnderPearls(output);
        if (!this.getShoulderEntityLeft().isEmpty()) {
            output.store("ShoulderEntityLeft", CompoundTag.CODEC, this.getShoulderEntityLeft());
        }

        if (!this.getShoulderEntityRight().isEmpty()) {
            output.store("ShoulderEntityRight", CompoundTag.CODEC, this.getShoulderEntityRight());
        }
    }

    private void saveParentVehicle(ValueOutput playerOutput) {
        Entity rootVehicle = this.getRootVehicle();
        Entity vehicle = this.getVehicle();
        if (vehicle != null && rootVehicle != this && rootVehicle.hasExactlyOnePlayerPassenger()) {
            ValueOutput vehicleWrapper = playerOutput.child("RootVehicle");
            vehicleWrapper.store("Attach", UUIDUtil.CODEC, vehicle.getUUID());
            rootVehicle.save(vehicleWrapper.child("Entity"));
        }
    }

    public void loadAndSpawnParentVehicle(ValueInput playerInput) {
        Optional<ValueInput> rootTag = playerInput.child("RootVehicle");
        if (!rootTag.isEmpty()) {
            ServerLevel serverLevel = this.level();
            Entity vehicle = EntityType.loadEntityRecursive(
                rootTag.get().childOrEmpty("Entity"), serverLevel, EntitySpawnReason.LOAD, e -> !serverLevel.addWithUUID(e) ? null : e
            );
            if (vehicle != null) {
                UUID attachTo = rootTag.get().read("Attach", UUIDUtil.CODEC).orElse(null);
                if (vehicle.getUUID().equals(attachTo)) {
                    this.startRiding(vehicle, true, false);
                } else {
                    for (Entity entity : vehicle.getIndirectPassengers()) {
                        if (entity.getUUID().equals(attachTo)) {
                            this.startRiding(entity, true, false);
                            break;
                        }
                    }
                }

                if (!this.isPassenger()) {
                    LOGGER.warn("Couldn't reattach entity to player");
                    vehicle.discard();

                    for (Entity entityx : vehicle.getIndirectPassengers()) {
                        entityx.discard();
                    }
                }
            }
        }
    }

    private void saveEnderPearls(ValueOutput playerOutput) {
        if (!this.enderPearls.isEmpty()) {
            ValueOutput.ValueOutputList pearlsOutput = playerOutput.childrenList("ender_pearls");

            for (ThrownEnderpearl enderPearl : this.enderPearls) {
                if (enderPearl.isRemoved()) {
                    LOGGER.warn("Trying to save removed ender pearl, skipping");
                } else {
                    ValueOutput pearlTag = pearlsOutput.addChild();
                    enderPearl.save(pearlTag);
                    pearlTag.store("ender_pearl_dimension", Level.RESOURCE_KEY_CODEC, enderPearl.level().dimension());
                }
            }
        }
    }

    public void loadAndSpawnEnderPearls(ValueInput playerInput) {
        playerInput.childrenListOrEmpty("ender_pearls").forEach(this::loadAndSpawnEnderPearl);
    }

    private void loadAndSpawnEnderPearl(ValueInput pearlInput) {
        Optional<ResourceKey<Level>> pearlLevelKey = pearlInput.read("ender_pearl_dimension", Level.RESOURCE_KEY_CODEC);
        if (!pearlLevelKey.isEmpty()) {
            ServerLevel pearlLevel = this.level().getServer().getLevel(pearlLevelKey.get());
            if (pearlLevel != null) {
                Entity pearl = EntityType.loadEntityRecursive(
                    pearlInput, pearlLevel, EntitySpawnReason.LOAD, entity -> !pearlLevel.addWithUUID(entity) ? null : entity
                );
                if (pearl != null) {
                    placeEnderPearlTicket(pearlLevel, pearl.chunkPosition());
                } else {
                    LOGGER.warn("Failed to spawn player ender pearl in level ({}), skipping", pearlLevelKey.get());
                }
            } else {
                LOGGER.warn("Trying to load ender pearl without level ({}) being loaded, skipping", pearlLevelKey.get());
            }
        }
    }

    public void setExperiencePoints(int amount) {
        float limit = this.getXpNeededForNextLevel();
        float max = (limit - 1.0F) / limit;
        float experiencePointsToSet = Mth.clamp(amount / limit, 0.0F, max);
        if (experiencePointsToSet != this.experienceProgress) {
            this.experienceProgress = experiencePointsToSet;
            this.lastSentExp = -1;
        }
    }

    public void setExperienceLevels(int amount) {
        if (amount != this.experienceLevel) {
            this.experienceLevel = amount;
            this.lastSentExp = -1;
        }
    }

    @Override
    public void giveExperienceLevels(int amount) {
        if (amount != 0) {
            super.giveExperienceLevels(amount);
            this.lastSentExp = -1;
        }
    }

    @Override
    public void onEnchantmentPerformed(ItemStack itemStack, int enchantmentCost) {
        super.onEnchantmentPerformed(itemStack, enchantmentCost);
        this.lastSentExp = -1;
    }

    private void initMenu(AbstractContainerMenu container) {
        container.addSlotListener(this.containerListener);
        container.setSynchronizer(this.containerSynchronizer);
    }

    public void initInventoryMenu() {
        this.initMenu(this.inventoryMenu);
    }

    @Override
    public void onEnterCombat() {
        super.onEnterCombat();
        this.connection.send(ClientboundPlayerCombatEnterPacket.INSTANCE);
    }

    @Override
    public void onLeaveCombat() {
        super.onLeaveCombat();
        this.connection.send(new ClientboundPlayerCombatEndPacket(this.getCombatTracker()));
    }

    @Override
    public void onInsideBlock(BlockState state) {
        CriteriaTriggers.ENTER_BLOCK.trigger(this, state);
    }

    @Override
    protected ItemCooldowns createItemCooldowns() {
        return new ServerItemCooldowns(this);
    }

    @Override
    public void tick() {
        this.connection.tickClientLoadTimeout();
        this.gameMode.tick();
        this.wardenSpawnTracker.tick();
        if (this.invulnerableTime > 0) {
            this.invulnerableTime--;
        }

        this.containerMenu.broadcastChanges();
        if (!this.containerMenu.stillValid(this)) {
            this.closeContainer();
            this.containerMenu = this.inventoryMenu;
        }

        Entity camera = this.getCamera();
        if (camera != this) {
            if (camera.isAlive()) {
                this.absSnapTo(camera.getX(), camera.getY(), camera.getZ(), camera.getYRot(), camera.getXRot());
                this.level().getChunkSource().move(this);
                if (this.wantsToStopRiding()) {
                    this.setCamera(this);
                }
            } else {
                this.setCamera(this);
            }
        }

        CriteriaTriggers.TICK.trigger(this);
        if (this.levitationStartPos != null) {
            CriteriaTriggers.LEVITATION.trigger(this, this.levitationStartPos, this.tickCount - this.levitationStartTime);
        }

        this.trackStartFallingPosition();
        this.trackEnteredOrExitedLavaOnVehicle();
        this.updatePlayerAttributes();
        this.advancements.flushDirty(this, true);
    }

    private void updatePlayerAttributes() {
        AttributeInstance blockInteractionRange = this.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);
        if (blockInteractionRange != null) {
            if (this.isCreative()) {
                blockInteractionRange.addOrUpdateTransientModifier(CREATIVE_BLOCK_INTERACTION_RANGE_MODIFIER);
            } else {
                blockInteractionRange.removeModifier(CREATIVE_BLOCK_INTERACTION_RANGE_MODIFIER);
            }
        }

        AttributeInstance entityInteractionRange = this.getAttribute(Attributes.ENTITY_INTERACTION_RANGE);
        if (entityInteractionRange != null) {
            if (this.isCreative()) {
                entityInteractionRange.addOrUpdateTransientModifier(CREATIVE_ENTITY_INTERACTION_RANGE_MODIFIER);
            } else {
                entityInteractionRange.removeModifier(CREATIVE_ENTITY_INTERACTION_RANGE_MODIFIER);
            }
        }

        AttributeInstance waypointTransmitRange = this.getAttribute(Attributes.WAYPOINT_TRANSMIT_RANGE);
        if (waypointTransmitRange != null) {
            if (this.isCrouching()) {
                waypointTransmitRange.addOrUpdateTransientModifier(WAYPOINT_TRANSMIT_RANGE_CROUCH_MODIFIER);
            } else {
                waypointTransmitRange.removeModifier(WAYPOINT_TRANSMIT_RANGE_CROUCH_MODIFIER);
            }
        }
    }

    public void doTick() {
        try {
            if (!this.isSpectator() || !this.touchingUnloadedChunk()) {
                super.tick();
                if (!this.containerMenu.stillValid(this)) {
                    this.closeContainer();
                    this.containerMenu = this.inventoryMenu;
                }

                this.foodData.tick(this);
                this.awardStat(Stats.PLAY_TIME);
                this.awardStat(Stats.TOTAL_WORLD_TIME);
                if (this.isAlive()) {
                    this.awardStat(Stats.TIME_SINCE_DEATH);
                }

                if (this.isDiscrete()) {
                    this.awardStat(Stats.CROUCH_TIME);
                }

                if (!this.isSleeping()) {
                    this.awardStat(Stats.TIME_SINCE_REST);
                }
            }

            for (int i = 0; i < this.getInventory().getContainerSize(); i++) {
                ItemStack itemStack = this.getInventory().getItem(i);
                if (!itemStack.isEmpty()) {
                    this.synchronizeSpecialItemUpdates(itemStack);
                }
            }

            if (this.getHealth() != this.lastSentHealth
                || this.lastSentFood != this.foodData.getFoodLevel()
                || this.foodData.getSaturationLevel() == 0.0F != this.lastFoodSaturationZero) {
                this.connection.send(new ClientboundSetHealthPacket(this.getHealth(), this.foodData.getFoodLevel(), this.foodData.getSaturationLevel()));
                this.lastSentHealth = this.getHealth();
                this.lastSentFood = this.foodData.getFoodLevel();
                this.lastFoodSaturationZero = this.foodData.getSaturationLevel() == 0.0F;
            }

            if (this.getHealth() + this.getAbsorptionAmount() != this.lastRecordedHealthAndAbsorption) {
                this.lastRecordedHealthAndAbsorption = this.getHealth() + this.getAbsorptionAmount();
                this.updateScoreForCriteria(ObjectiveCriteria.HEALTH, Mth.ceil(this.lastRecordedHealthAndAbsorption));
            }

            if (this.foodData.getFoodLevel() != this.lastRecordedFoodLevel) {
                this.lastRecordedFoodLevel = this.foodData.getFoodLevel();
                this.updateScoreForCriteria(ObjectiveCriteria.FOOD, Mth.ceil((float)this.lastRecordedFoodLevel));
            }

            if (this.getAirSupply() != this.lastRecordedAirLevel) {
                this.lastRecordedAirLevel = this.getAirSupply();
                this.updateScoreForCriteria(ObjectiveCriteria.AIR, Mth.ceil((float)this.lastRecordedAirLevel));
            }

            if (this.getArmorValue() != this.lastRecordedArmor) {
                this.lastRecordedArmor = this.getArmorValue();
                this.updateScoreForCriteria(ObjectiveCriteria.ARMOR, Mth.ceil((float)this.lastRecordedArmor));
            }

            if (this.totalExperience != this.lastRecordedExperience) {
                this.lastRecordedExperience = this.totalExperience;
                this.updateScoreForCriteria(ObjectiveCriteria.EXPERIENCE, Mth.ceil((float)this.lastRecordedExperience));
            }

            if (this.experienceLevel != this.lastRecordedLevel) {
                this.lastRecordedLevel = this.experienceLevel;
                this.updateScoreForCriteria(ObjectiveCriteria.LEVEL, Mth.ceil((float)this.lastRecordedLevel));
            }

            if (this.totalExperience != this.lastSentExp) {
                this.lastSentExp = this.totalExperience;
                this.connection.send(new ClientboundSetExperiencePacket(this.experienceProgress, this.totalExperience, this.experienceLevel));
            }

            if (this.tickCount % 20 == 0) {
                CriteriaTriggers.LOCATION.trigger(this);
            }
        } catch (Throwable var4) {
            CrashReport report = CrashReport.forThrowable(var4, "Ticking player");
            CrashReportCategory category = report.addCategory("Player being ticked");
            this.fillCrashReportCategory(category);
            throw new ReportedException(report);
        }
    }

    private void synchronizeSpecialItemUpdates(ItemStack itemStack) {
        MapId mapId = itemStack.get(DataComponents.MAP_ID);
        MapItemSavedData data = MapItem.getSavedData(mapId, this.level());
        if (data != null) {
            Packet<?> packet = data.getUpdatePacket(mapId, this);
            if (packet != null) {
                this.connection.send(packet);
            }
        }
    }

    @Override
    protected void tickRegeneration() {
        if (this.level().getDifficulty() == Difficulty.PEACEFUL && this.level().getGameRules().get(GameRules.NATURAL_HEALTH_REGENERATION)) {
            if (this.tickCount % 20 == 0) {
                if (this.getHealth() < this.getMaxHealth()) {
                    this.heal(1.0F);
                }

                float saturation = this.foodData.getSaturationLevel();
                if (saturation < 20.0F) {
                    this.foodData.setSaturation(saturation + 1.0F);
                }
            }

            if (this.tickCount % 10 == 0 && this.foodData.needsFood()) {
                this.foodData.setFoodLevel(this.foodData.getFoodLevel() + 1);
            }
        }
    }

    @Override
    public void handleShoulderEntities() {
        this.playShoulderEntityAmbientSound(this.getShoulderEntityLeft());
        this.playShoulderEntityAmbientSound(this.getShoulderEntityRight());
        if (this.fallDistance > 0.5 || this.isInWater() || this.getAbilities().flying || this.isSleeping() || this.isInPowderSnow) {
            this.removeEntitiesOnShoulder();
        }
    }

    private void playShoulderEntityAmbientSound(CompoundTag shoulderEntityTag) {
        if (!shoulderEntityTag.isEmpty() && !shoulderEntityTag.getBooleanOr("Silent", false)) {
            if (this.random.nextInt(200) == 0) {
                EntityType<?> entityType = shoulderEntityTag.read("id", EntityType.CODEC).orElse(null);
                if (entityType == EntityType.PARROT && !Parrot.imitateNearbyMobs(this.level(), this)) {
                    this.level()
                        .playSound(
                            null,
                            this.getX(),
                            this.getY(),
                            this.getZ(),
                            Parrot.getAmbient(this.level(), this.random),
                            this.getSoundSource(),
                            1.0F,
                            Parrot.getPitch(this.random)
                        );
                }
            }
        }
    }

    public boolean setEntityOnShoulder(CompoundTag entityTag) {
        if (this.isPassenger() || !this.onGround() || this.isInWater() || this.isInPowderSnow) {
            return false;
        } else if (this.getShoulderEntityLeft().isEmpty()) {
            this.setShoulderEntityLeft(entityTag);
            this.timeEntitySatOnShoulder = this.level().getGameTime();
            return true;
        } else if (this.getShoulderEntityRight().isEmpty()) {
            this.setShoulderEntityRight(entityTag);
            this.timeEntitySatOnShoulder = this.level().getGameTime();
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void removeEntitiesOnShoulder() {
        if (this.timeEntitySatOnShoulder + 20L < this.level().getGameTime()) {
            this.respawnEntityOnShoulder(this.getShoulderEntityLeft());
            this.setShoulderEntityLeft(new CompoundTag());
            this.respawnEntityOnShoulder(this.getShoulderEntityRight());
            this.setShoulderEntityRight(new CompoundTag());
        }
    }

    private void respawnEntityOnShoulder(CompoundTag tag) {
        ServerLevel reporter = this.level();
        if (reporter instanceof ServerLevel) {
            ServerLevel serverLevel = reporter;
            if (!tag.isEmpty()) {
                try (ProblemReporter.ScopedCollector reporterx = new ProblemReporter.ScopedCollector(this.problemPath(), LOGGER)) {
                    EntityType.create(
                            TagValueInput.create(reporterx.forChild(() -> ".shoulder"), serverLevel.registryAccess(), tag), serverLevel, EntitySpawnReason.LOAD
                        )
                        .ifPresent(entity -> {
                            if (entity instanceof TamableAnimal tamed) {
                                tamed.setOwner(this);
                            }

                            entity.setPos(this.getX(), this.getY() + 0.7F, this.getZ());
                            serverLevel.addWithUUID(entity);
                        });
                }
            }
        }
    }

    @Override
    public void resetFallDistance() {
        if (this.getHealth() > 0.0F && this.startingToFallPosition != null) {
            CriteriaTriggers.FALL_FROM_HEIGHT.trigger(this, this.startingToFallPosition);
        }

        this.startingToFallPosition = null;
        super.resetFallDistance();
    }

    public void trackStartFallingPosition() {
        if (this.fallDistance > 0.0 && this.startingToFallPosition == null) {
            this.startingToFallPosition = this.position();
            if (this.currentImpulseImpactPos != null && this.currentImpulseImpactPos.y <= this.startingToFallPosition.y) {
                CriteriaTriggers.FALL_AFTER_EXPLOSION.trigger(this, this.currentImpulseImpactPos, this.currentExplosionCause);
            }
        }
    }

    public void trackEnteredOrExitedLavaOnVehicle() {
        if (this.getVehicle() != null && this.getVehicle().isInLava()) {
            if (this.enteredLavaOnVehiclePosition == null) {
                this.enteredLavaOnVehiclePosition = this.position();
            } else {
                CriteriaTriggers.RIDE_ENTITY_IN_LAVA_TRIGGER.trigger(this, this.enteredLavaOnVehiclePosition);
            }
        }

        if (this.enteredLavaOnVehiclePosition != null && (this.getVehicle() == null || !this.getVehicle().isInLava())) {
            this.enteredLavaOnVehiclePosition = null;
        }
    }

    private void updateScoreForCriteria(ObjectiveCriteria criteria, int value) {
        this.level().getScoreboard().forAllObjectives(criteria, this, score -> score.set(value));
    }

    @Override
    public void die(DamageSource source) {
        this.gameEvent(GameEvent.ENTITY_DIE);
        boolean showDeathMessage = this.level().getGameRules().get(GameRules.SHOW_DEATH_MESSAGES);
        if (showDeathMessage) {
            Component deathMessage = this.getCombatTracker().getDeathMessage();
            this.connection
                .send(
                    new ClientboundPlayerCombatKillPacket(this.getId(), deathMessage),
                    PacketSendListener.exceptionallySend(
                        () -> {
                            int truncatedMessageSize = 256;
                            String truncatedDeathMessage = deathMessage.getString(256);
                            Component explanation = Component.translatable(
                                "death.attack.message_too_long", Component.literal(truncatedDeathMessage).withStyle(ChatFormatting.YELLOW)
                            );
                            Component fakeDeathMessage = Component.translatable("death.attack.even_more_magic", this.getDisplayName())
                                .withStyle(style -> style.withHoverEvent(new HoverEvent.ShowText(explanation)));
                            return new ClientboundPlayerCombatKillPacket(this.getId(), fakeDeathMessage);
                        }
                    )
                );
            Team team = this.getTeam();
            if (team == null || team.getDeathMessageVisibility() == Team.Visibility.ALWAYS) {
                this.server.getPlayerList().broadcastSystemMessage(deathMessage, false);
            } else if (team.getDeathMessageVisibility() == Team.Visibility.HIDE_FOR_OTHER_TEAMS) {
                this.server.getPlayerList().broadcastSystemToTeam(this, deathMessage);
            } else if (team.getDeathMessageVisibility() == Team.Visibility.HIDE_FOR_OWN_TEAM) {
                this.server.getPlayerList().broadcastSystemToAllExceptTeam(this, deathMessage);
            }
        } else {
            this.connection.send(new ClientboundPlayerCombatKillPacket(this.getId(), CommonComponents.EMPTY));
        }

        this.removeEntitiesOnShoulder();
        if (this.level().getGameRules().get(GameRules.FORGIVE_DEAD_PLAYERS)) {
            this.tellNeutralMobsThatIDied();
        }

        if (!this.isSpectator()) {
            this.dropAllDeathLoot(this.level(), source);
        }

        this.level().getScoreboard().forAllObjectives(ObjectiveCriteria.DEATH_COUNT, this, ScoreAccess::increment);
        LivingEntity killer = this.getKillCredit();
        if (killer != null) {
            this.awardStat(Stats.ENTITY_KILLED_BY.get(killer.getType()));
            killer.awardKillScore(this, source);
            this.createWitherRose(killer);
        }

        this.level().broadcastEntityEvent(this, (byte)3);
        this.awardStat(Stats.DEATHS);
        this.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_DEATH));
        this.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));
        this.clearFire();
        this.setTicksFrozen(0);
        this.setSharedFlagOnFire(false);
        this.getCombatTracker().recheckStatus();
        this.setLastDeathLocation(Optional.of(GlobalPos.of(this.level().dimension(), this.blockPosition())));
        this.connection.markClientUnloadedAfterDeath();
    }

    private void tellNeutralMobsThatIDied() {
        AABB aabb = new AABB(this.blockPosition()).inflate(32.0, 10.0, 32.0);
        this.level()
            .getEntitiesOfClass(Mob.class, aabb, EntitySelector.NO_SPECTATORS)
            .stream()
            .filter(mob -> mob instanceof NeutralMob)
            .forEach(mob -> ((NeutralMob)mob).playerDied(this.level(), this));
    }

    @Override
    public void awardKillScore(Entity victim, DamageSource killingBlow) {
        if (victim != this) {
            super.awardKillScore(victim, killingBlow);
            Scoreboard scoreboard = this.level().getScoreboard();
            scoreboard.forAllObjectives(ObjectiveCriteria.KILL_COUNT_ALL, this, ScoreAccess::increment);
            if (victim instanceof Player) {
                this.awardStat(Stats.PLAYER_KILLS);
                scoreboard.forAllObjectives(ObjectiveCriteria.KILL_COUNT_PLAYERS, this, ScoreAccess::increment);
            } else {
                this.awardStat(Stats.MOB_KILLS);
            }

            this.handleTeamKill(this, victim, ObjectiveCriteria.TEAM_KILL);
            this.handleTeamKill(victim, this, ObjectiveCriteria.KILLED_BY_TEAM);
            CriteriaTriggers.PLAYER_KILLED_ENTITY.trigger(this, victim, killingBlow);
        }
    }

    private void handleTeamKill(ScoreHolder source, ScoreHolder target, ObjectiveCriteria[] criteriaByTeam) {
        Scoreboard scoreboard = this.level().getScoreboard();
        PlayerTeam ownTeam = scoreboard.getPlayersTeam(target.getScoreboardName());
        if (ownTeam != null) {
            int color = ownTeam.getColor().getId();
            if (color >= 0 && color < criteriaByTeam.length) {
                scoreboard.forAllObjectives(criteriaByTeam[color], source, ScoreAccess::increment);
            }
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (this.isInvulnerableTo(level, source)) {
            return false;
        } else {
            Entity entity = source.getEntity();
            if (entity instanceof Player player && !this.canHarmPlayer(player)) {
                return false;
            } else {
                return entity instanceof AbstractArrow arrow && arrow.getOwner() instanceof Player player && !this.canHarmPlayer(player)
                    ? false
                    : super.hurtServer(level, source, damage);
            }
        }
    }

    @Override
    public boolean canHarmPlayer(Player target) {
        return !this.isPvpAllowed() ? false : super.canHarmPlayer(target);
    }

    private boolean isPvpAllowed() {
        return this.level().isPvpAllowed();
    }

    public TeleportTransition findRespawnPositionAndUseSpawnBlock(boolean consumeSpawnBlock, TeleportTransition.PostTeleportTransition postTeleportTransition) {
        ServerPlayer.RespawnConfig respawnConfig = this.getRespawnConfig();
        ServerLevel respawnLevel = this.server.getLevel(ServerPlayer.RespawnConfig.getDimensionOrDefault(respawnConfig));
        if (respawnLevel != null && respawnConfig != null) {
            Optional<ServerPlayer.RespawnPosAngle> respawn = findRespawnAndUseSpawnBlock(respawnLevel, respawnConfig, consumeSpawnBlock);
            if (respawn.isPresent()) {
                ServerPlayer.RespawnPosAngle respawnPosAngle = respawn.get();
                return new TeleportTransition(
                    respawnLevel, respawnPosAngle.position(), Vec3.ZERO, respawnPosAngle.yaw(), respawnPosAngle.pitch(), postTeleportTransition
                );
            } else {
                return TeleportTransition.missingRespawnBlock(this, postTeleportTransition);
            }
        } else {
            return TeleportTransition.createDefault(this, postTeleportTransition);
        }
    }

    public boolean isReceivingWaypoints() {
        return this.getAttributeValue(Attributes.WAYPOINT_RECEIVE_RANGE) > 0.0;
    }

    @Override
    protected void onAttributeUpdated(Holder<Attribute> attribute) {
        if (attribute.is(Attributes.WAYPOINT_RECEIVE_RANGE)) {
            ServerWaypointManager waypointManager = this.level().getWaypointManager();
            if (this.getAttributes().getValue(attribute) > 0.0) {
                waypointManager.addPlayer(this);
            } else {
                waypointManager.removePlayer(this);
            }
        }

        super.onAttributeUpdated(attribute);
    }

    private static Optional<ServerPlayer.RespawnPosAngle> findRespawnAndUseSpawnBlock(
        ServerLevel level, ServerPlayer.RespawnConfig respawnConfig, boolean consumeSpawnBlock
    ) {
        LevelData.RespawnData respawnData = respawnConfig.respawnData;
        BlockPos pos = respawnData.pos();
        float yaw = respawnData.yaw();
        float pitch = respawnData.pitch();
        boolean forced = respawnConfig.forced;
        BlockState blockState = level.getBlockState(pos);
        Block block = blockState.getBlock();
        if (block instanceof RespawnAnchorBlock && (forced || blockState.getValue(RespawnAnchorBlock.CHARGE) > 0) && RespawnAnchorBlock.canSetSpawn(level, pos)
            )
         {
            Optional<Vec3> standUpPosition = RespawnAnchorBlock.findStandUpPosition(EntityType.PLAYER, level, pos);
            if (!forced && consumeSpawnBlock && standUpPosition.isPresent()) {
                level.setBlock(pos, blockState.setValue(RespawnAnchorBlock.CHARGE, blockState.getValue(RespawnAnchorBlock.CHARGE) - 1), 3);
            }

            return standUpPosition.map(p -> ServerPlayer.RespawnPosAngle.of(p, pos, 0.0F));
        } else if (block instanceof BedBlock && level.environmentAttributes().getValue(EnvironmentAttributes.BED_RULE, pos).canSetSpawn(level)) {
            return BedBlock.findStandUpPosition(EntityType.PLAYER, level, pos, blockState.getValue(BedBlock.FACING), yaw)
                .map(p -> ServerPlayer.RespawnPosAngle.of(p, pos, 0.0F));
        } else if (!forced) {
            return Optional.empty();
        } else {
            boolean freeBottom = block.isPossibleToRespawnInThis(blockState);
            BlockState topState = level.getBlockState(pos.above());
            boolean freeTop = topState.getBlock().isPossibleToRespawnInThis(topState);
            return freeBottom && freeTop
                ? Optional.of(new ServerPlayer.RespawnPosAngle(new Vec3(pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5), yaw, pitch))
                : Optional.empty();
        }
    }

    public void showEndCredits() {
        this.unRide();
        this.level().removePlayerImmediately(this, Entity.RemovalReason.CHANGED_DIMENSION);
        if (!this.wonGame) {
            this.wonGame = true;
            this.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.WIN_GAME, 0.0F));
            this.seenCredits = true;
        }
    }

    public @Nullable ServerPlayer teleport(TeleportTransition transition) {
        if (this.isRemoved()) {
            return null;
        } else {
            if (transition.missingRespawnBlock()) {
                this.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.NO_RESPAWN_BLOCK_AVAILABLE, 0.0F));
            }

            ServerLevel newLevel = transition.newLevel();
            ServerLevel oldLevel = this.level();
            ResourceKey<Level> lastDimension = oldLevel.dimension();
            if (!transition.asPassenger()) {
                this.removeVehicle();
            }

            if (newLevel.dimension() == lastDimension) {
                this.connection.teleport(PositionMoveRotation.of(transition), transition.relatives());
                this.connection.resetPosition();
                transition.postTeleportTransition().onTransition(this);
                return this;
            } else {
                this.isChangingDimension = true;
                LevelData levelData = newLevel.getLevelData();
                this.connection.send(new ClientboundRespawnPacket(this.createCommonSpawnInfo(newLevel), (byte)3));
                this.connection.send(new ClientboundChangeDifficultyPacket(levelData.getDifficulty(), levelData.isDifficultyLocked()));
                PlayerList playerList = this.server.getPlayerList();
                playerList.sendPlayerPermissionLevel(this);
                oldLevel.removePlayerImmediately(this, Entity.RemovalReason.CHANGED_DIMENSION);
                this.unsetRemoved();
                ProfilerFiller profiler = Profiler.get();
                profiler.push("moving");
                if (lastDimension == Level.OVERWORLD && newLevel.dimension() == Level.NETHER) {
                    this.enteredNetherPosition = this.position();
                }

                profiler.pop();
                profiler.push("placing");
                this.setServerLevel(newLevel);
                this.connection.teleport(PositionMoveRotation.of(transition), transition.relatives());
                this.connection.resetPosition();
                newLevel.addDuringTeleport(this);
                profiler.pop();
                this.triggerDimensionChangeTriggers(oldLevel);
                this.stopUsingItem();
                this.connection.send(new ClientboundPlayerAbilitiesPacket(this.getAbilities()));
                playerList.sendLevelInfo(this, newLevel);
                playerList.sendAllPlayerInfo(this);
                playerList.sendActivePlayerEffects(this);
                transition.postTeleportTransition().onTransition(this);
                this.lastSentExp = -1;
                this.lastSentHealth = -1.0F;
                this.lastSentFood = -1;
                this.teleportSpectators(transition, oldLevel);
                return this;
            }
        }
    }

    @Override
    public void forceSetRotation(float yRot, boolean relativeY, float xRot, boolean relativeX) {
        super.forceSetRotation(yRot, relativeY, xRot, relativeX);
        this.connection.send(new ClientboundPlayerRotationPacket(yRot, relativeY, xRot, relativeX));
    }

    private void triggerDimensionChangeTriggers(ServerLevel oldLevel) {
        ResourceKey<Level> oldKey = oldLevel.dimension();
        ResourceKey<Level> newKey = this.level().dimension();
        CriteriaTriggers.CHANGED_DIMENSION.trigger(this, oldKey, newKey);
        if (oldKey == Level.NETHER && newKey == Level.OVERWORLD && this.enteredNetherPosition != null) {
            CriteriaTriggers.NETHER_TRAVEL.trigger(this, this.enteredNetherPosition);
        }

        if (newKey != Level.NETHER) {
            this.enteredNetherPosition = null;
        }
    }

    @Override
    public boolean broadcastToPlayer(ServerPlayer player) {
        if (player.isSpectator()) {
            return this.getCamera() == this;
        } else {
            return this.isSpectator() ? false : super.broadcastToPlayer(player);
        }
    }

    @Override
    public void take(Entity entity, int orgCount) {
        super.take(entity, orgCount);
        this.containerMenu.broadcastChanges();
    }

    @Override
    public Either<Player.BedSleepingProblem, Unit> startSleepInBed(BlockPos pos) {
        Direction direction = this.level().getBlockState(pos).getValue(HorizontalDirectionalBlock.FACING);
        if (!this.isSleeping() && this.isAlive()) {
            BedRule rule = this.level().environmentAttributes().getValue(EnvironmentAttributes.BED_RULE, pos);
            boolean canSleep = rule.canSleep(this.level());
            boolean canSetSpawn = rule.canSetSpawn(this.level());
            if (!canSetSpawn && !canSleep) {
                return Either.left(rule.asProblem());
            } else if (!this.bedInRange(pos, direction)) {
                return Either.left(Player.BedSleepingProblem.TOO_FAR_AWAY);
            } else if (this.bedBlocked(pos, direction)) {
                return Either.left(Player.BedSleepingProblem.OBSTRUCTED);
            } else {
                if (canSetSpawn) {
                    this.setRespawnPosition(
                        new ServerPlayer.RespawnConfig(LevelData.RespawnData.of(this.level().dimension(), pos, this.getYRot(), this.getXRot()), false), true
                    );
                }

                if (!canSleep) {
                    return Either.left(rule.asProblem());
                } else {
                    if (!this.isCreative()) {
                        double hRange = 8.0;
                        double vRange = 5.0;
                        Vec3 bedCenter = Vec3.atBottomCenterOf(pos);
                        List<Monster> monsters = this.level()
                            .getEntitiesOfClass(
                                Monster.class,
                                new AABB(
                                    bedCenter.x() - 8.0,
                                    bedCenter.y() - 5.0,
                                    bedCenter.z() - 8.0,
                                    bedCenter.x() + 8.0,
                                    bedCenter.y() + 5.0,
                                    bedCenter.z() + 8.0
                                ),
                                monster -> monster.isPreventingPlayerRest(this.level(), this)
                            );
                        if (!monsters.isEmpty()) {
                            return Either.left(Player.BedSleepingProblem.NOT_SAFE);
                        }
                    }

                    Either<Player.BedSleepingProblem, Unit> result = super.startSleepInBed(pos).ifRight(unit -> {
                        this.awardStat(Stats.SLEEP_IN_BED);
                        CriteriaTriggers.SLEPT_IN_BED.trigger(this);
                    });
                    if (!this.level().canSleepThroughNights()) {
                        this.sendOverlayMessage(Component.translatable("sleep.not_possible"));
                    }

                    this.level().updateSleepingPlayerList();
                    return result;
                }
            }
        } else {
            return Either.left(Player.BedSleepingProblem.OTHER_PROBLEM);
        }
    }

    @Override
    public void startSleeping(BlockPos bedPosition) {
        this.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));
        super.startSleeping(bedPosition);
    }

    private boolean bedInRange(BlockPos pos, Direction direction) {
        return this.isReachableBedBlock(pos) || this.isReachableBedBlock(pos.relative(direction.getOpposite()));
    }

    private boolean isReachableBedBlock(BlockPos bedBlockPos) {
        Vec3 bedBlockCenter = Vec3.atBottomCenterOf(bedBlockPos);
        return Math.abs(this.getX() - bedBlockCenter.x()) <= 3.0
            && Math.abs(this.getY() - bedBlockCenter.y()) <= 2.0
            && Math.abs(this.getZ() - bedBlockCenter.z()) <= 3.0;
    }

    private boolean bedBlocked(BlockPos pos, Direction direction) {
        BlockPos above = pos.above();
        return !this.freeAt(above) || !this.freeAt(above.relative(direction.getOpposite()));
    }

    @Override
    public void stopSleepInBed(boolean forcefulWakeUp, boolean updateLevelList) {
        if (this.isSleeping()) {
            this.level().getChunkSource().sendToTrackingPlayersAndSelf(this, new ClientboundAnimatePacket(this, 2));
        }

        super.stopSleepInBed(forcefulWakeUp, updateLevelList);
        if (this.connection != null) {
            this.connection.teleport(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
        }
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        return super.isInvulnerableTo(level, source) || this.isChangingDimension() && !source.is(DamageTypes.ENDER_PEARL) || !this.connection.hasClientLoaded();
    }

    @Override
    protected void onChangedBlock(ServerLevel level, BlockPos pos) {
        if (!this.isSpectator()) {
            super.onChangedBlock(level, pos);
        }
    }

    @Override
    protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
        if (this.spawnExtraParticlesOnFall && onGround && this.fallDistance > 0.0) {
            Vec3 centered = pos.getCenter().add(0.0, 0.5, 0.0);
            int particles = (int)Mth.clamp(50.0 * this.fallDistance, 0.0, 200.0);
            this.level()
                .sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, onState), centered.x, centered.y, centered.z, particles, 0.3F, 0.3F, 0.3F, 0.15F);
            this.spawnExtraParticlesOnFall = false;
        }

        super.checkFallDamage(ya, onGround, onState, pos);
    }

    @Override
    public void onExplosionHit(@Nullable Entity explosionCausedBy) {
        super.onExplosionHit(explosionCausedBy);
        this.currentExplosionCause = explosionCausedBy;
        this.setIgnoreFallDamageFromCurrentImpulse(explosionCausedBy != null && explosionCausedBy.is(EntityType.WIND_CHARGE), this.position());
    }

    @Override
    protected void pushEntities() {
        if (this.level().tickRateManager().runsNormally()) {
            super.pushEntities();
        }
    }

    @Override
    public void openTextEdit(SignBlockEntity sign, boolean isFrontText) {
        this.connection.send(new ClientboundBlockUpdatePacket(this.level(), sign.getBlockPos()));
        this.connection.send(new ClientboundOpenSignEditorPacket(sign.getBlockPos(), isFrontText));
    }

    @Override
    public void openDialog(Holder<Dialog> dialog) {
        this.connection.send(new ClientboundShowDialogPacket(dialog));
    }

    private void nextContainerCounter() {
        this.containerCounter = this.containerCounter % 100 + 1;
    }

    @Override
    public OptionalInt openMenu(@Nullable MenuProvider provider) {
        if (provider == null) {
            return OptionalInt.empty();
        } else {
            if (this.containerMenu != this.inventoryMenu) {
                this.closeContainer();
            }

            this.nextContainerCounter();
            AbstractContainerMenu menu = provider.createMenu(this.containerCounter, this.getInventory(), this);
            if (menu == null) {
                if (this.isSpectator()) {
                    this.sendOverlayMessage(Component.translatable("container.spectatorCantOpen").withStyle(ChatFormatting.RED));
                }

                return OptionalInt.empty();
            } else {
                this.connection.send(new ClientboundOpenScreenPacket(menu.containerId, menu.getType(), provider.getDisplayName()));
                this.initMenu(menu);
                this.containerMenu = menu;
                return OptionalInt.of(this.containerCounter);
            }
        }
    }

    @Override
    public void sendMerchantOffers(int containerId, MerchantOffers offers, int merchantLevel, int merchantXp, boolean showProgressBar, boolean canRestock) {
        this.connection.send(new ClientboundMerchantOffersPacket(containerId, offers, merchantLevel, merchantXp, showProgressBar, canRestock));
    }

    @Override
    public void openHorseInventory(AbstractHorse horse, Container container) {
        if (this.containerMenu != this.inventoryMenu) {
            this.closeContainer();
        }

        this.nextContainerCounter();
        int inventoryColumns = horse.getInventoryColumns();
        this.connection.send(new ClientboundMountScreenOpenPacket(this.containerCounter, inventoryColumns, horse.getId()));
        this.containerMenu = new HorseInventoryMenu(this.containerCounter, this.getInventory(), container, horse, inventoryColumns);
        this.initMenu(this.containerMenu);
    }

    @Override
    public void openNautilusInventory(AbstractNautilus nautilus, Container container) {
        if (this.containerMenu != this.inventoryMenu) {
            this.closeContainer();
        }

        this.nextContainerCounter();
        int inventoryColumns = nautilus.getInventoryColumns();
        this.connection.send(new ClientboundMountScreenOpenPacket(this.containerCounter, inventoryColumns, nautilus.getId()));
        this.containerMenu = new NautilusInventoryMenu(this.containerCounter, this.getInventory(), container, nautilus, inventoryColumns);
        this.initMenu(this.containerMenu);
    }

    @Override
    public void openItemGui(ItemStack itemStack, InteractionHand hand) {
        if (itemStack.has(DataComponents.WRITTEN_BOOK_CONTENT)) {
            if (WrittenBookContent.resolveForItem(itemStack, ResolutionContext.create(this.createCommandSourceStack()), this.registryAccess())) {
                this.containerMenu.broadcastChanges();
            }

            this.connection.send(new ClientboundOpenBookPacket(hand));
        }
    }

    @Override
    public void openCommandBlock(CommandBlockEntity commandBlock) {
        this.connection.send(ClientboundBlockEntityDataPacket.create(commandBlock, BlockEntity::saveCustomOnly));
    }

    @Override
    public void closeContainer() {
        this.connection.send(new ClientboundContainerClosePacket(this.containerMenu.containerId));
        this.doCloseContainer();
    }

    @Override
    public void doCloseContainer() {
        this.containerMenu.removed(this);
        this.inventoryMenu.transferState(this.containerMenu);
        this.containerMenu = this.inventoryMenu;
    }

    @Override
    public void rideTick() {
        double preX = this.getX();
        double preY = this.getY();
        double preZ = this.getZ();
        super.rideTick();
        this.checkRidingStatistics(this.getX() - preX, this.getY() - preY, this.getZ() - preZ);
    }

    public void checkMovementStatistics(double dx, double dy, double dz) {
        if (!this.isPassenger() && !didNotMove(dx, dy, dz)) {
            if (this.isSwimming()) {
                int distance = Math.round((float)Math.sqrt(dx * dx + dy * dy + dz * dz) * 100.0F);
                if (distance > 0) {
                    this.awardStat(Stats.SWIM_ONE_CM, distance);
                    this.causeFoodExhaustion(0.01F * distance * 0.01F);
                }
            } else if (this.isEyeInFluid(FluidTags.WATER)) {
                int distance = Math.round((float)Math.sqrt(dx * dx + dy * dy + dz * dz) * 100.0F);
                if (distance > 0) {
                    this.awardStat(Stats.WALK_UNDER_WATER_ONE_CM, distance);
                    this.causeFoodExhaustion(0.01F * distance * 0.01F);
                }
            } else if (this.isInWater()) {
                int horizontalDistance = Math.round((float)Math.sqrt(dx * dx + dz * dz) * 100.0F);
                if (horizontalDistance > 0) {
                    this.awardStat(Stats.WALK_ON_WATER_ONE_CM, horizontalDistance);
                    this.causeFoodExhaustion(0.01F * horizontalDistance * 0.01F);
                }
            } else if (this.onClimbable()) {
                if (dy > 0.0) {
                    this.awardStat(Stats.CLIMB_ONE_CM, (int)Math.round(dy * 100.0));
                }
            } else if (this.onGround()) {
                int horizontalDistance = Math.round((float)Math.sqrt(dx * dx + dz * dz) * 100.0F);
                if (horizontalDistance > 0) {
                    if (this.isSprinting()) {
                        this.awardStat(Stats.SPRINT_ONE_CM, horizontalDistance);
                        this.causeFoodExhaustion(0.1F * horizontalDistance * 0.01F);
                    } else if (this.isCrouching()) {
                        this.awardStat(Stats.CROUCH_ONE_CM, horizontalDistance);
                        this.causeFoodExhaustion(0.0F * horizontalDistance * 0.01F);
                    } else {
                        this.awardStat(Stats.WALK_ONE_CM, horizontalDistance);
                        this.causeFoodExhaustion(0.0F * horizontalDistance * 0.01F);
                    }
                }
            } else if (this.isFallFlying()) {
                int distance = Math.round((float)Math.sqrt(dx * dx + dy * dy + dz * dz) * 100.0F);
                this.awardStat(Stats.AVIATE_ONE_CM, distance);
            } else {
                int horizontalDistance = Math.round((float)Math.sqrt(dx * dx + dz * dz) * 100.0F);
                if (horizontalDistance > 25) {
                    this.awardStat(Stats.FLY_ONE_CM, horizontalDistance);
                }
            }
        }
    }

    private void checkRidingStatistics(double dx, double dy, double dz) {
        if (this.isPassenger() && !didNotMove(dx, dy, dz)) {
            int distance = Math.round((float)Math.sqrt(dx * dx + dy * dy + dz * dz) * 100.0F);
            Entity vehicle = this.getVehicle();
            if (vehicle instanceof AbstractMinecart) {
                this.awardStat(Stats.MINECART_ONE_CM, distance);
            } else if (vehicle instanceof AbstractBoat) {
                this.awardStat(Stats.BOAT_ONE_CM, distance);
            } else if (vehicle instanceof Pig) {
                this.awardStat(Stats.PIG_ONE_CM, distance);
            } else if (vehicle instanceof AbstractHorse) {
                this.awardStat(Stats.HORSE_ONE_CM, distance);
            } else if (vehicle instanceof Strider) {
                this.awardStat(Stats.STRIDER_ONE_CM, distance);
            } else if (vehicle instanceof HappyGhast) {
                this.awardStat(Stats.HAPPY_GHAST_ONE_CM, distance);
            } else if (vehicle instanceof AbstractNautilus) {
                this.awardStat(Stats.NAUTILUS_ONE_CM, distance);
            }
        }
    }

    private static boolean didNotMove(double dx, double dy, double dz) {
        return dx == 0.0 && dy == 0.0 && dz == 0.0;
    }

    @Override
    public void awardStat(Stat<?> stat, int count) {
        this.stats.increment(this, stat, count);
        this.level().getScoreboard().forAllObjectives(stat, this, score -> score.add(count));
    }

    @Override
    public void resetStat(Stat<?> stat) {
        this.stats.setValue(this, stat, 0);
        this.level().getScoreboard().forAllObjectives(stat, this, ScoreAccess::reset);
    }

    @Override
    public int awardRecipes(Collection<RecipeHolder<?>> recipes) {
        return this.recipeBook.addRecipes(recipes, this);
    }

    @Override
    public void triggerRecipeCrafted(RecipeHolder<?> recipe, List<ItemStack> itemStacks) {
        CriteriaTriggers.RECIPE_CRAFTED.trigger(this, recipe.id(), itemStacks);
    }

    @Override
    public void awardRecipesByKey(List<ResourceKey<Recipe<?>>> recipeIds) {
        List<RecipeHolder<?>> recipes = recipeIds.stream()
            .flatMap(id -> this.server.getRecipeManager().byKey((ResourceKey<Recipe<?>>)id).stream())
            .collect(Collectors.toList());
        this.awardRecipes(recipes);
    }

    @Override
    public int resetRecipes(Collection<RecipeHolder<?>> recipe) {
        return this.recipeBook.removeRecipes(recipe, this);
    }

    @Override
    public void jumpFromGround() {
        super.jumpFromGround();
        this.awardStat(Stats.JUMP);
        if (this.isSprinting()) {
            this.causeFoodExhaustion(0.2F);
        } else {
            this.causeFoodExhaustion(0.05F);
        }
    }

    @Override
    public void giveExperiencePoints(int i) {
        if (i != 0) {
            super.giveExperiencePoints(i);
            this.lastSentExp = -1;
        }
    }

    public void disconnect() {
        this.disconnected = true;
        this.ejectPassengers();
        if (this.isSleeping()) {
            this.stopSleepInBed(true, false);
        }
    }

    public boolean hasDisconnected() {
        return this.disconnected;
    }

    public void resetSentInfo() {
        this.lastSentHealth = -1.0E8F;
    }

    @Override
    protected void completeUsingItem() {
        if (!this.useItem.isEmpty() && this.isUsingItem()) {
            this.connection.send(new ClientboundEntityEventPacket(this, (byte)9));
            super.completeUsingItem();
        }
    }

    @Override
    public void lookAt(EntityAnchorArgument.Anchor anchor, Vec3 pos) {
        super.lookAt(anchor, pos);
        this.connection.send(new ClientboundPlayerLookAtPacket(anchor, pos.x, pos.y, pos.z));
    }

    public void lookAt(EntityAnchorArgument.Anchor fromAnchor, Entity entity, EntityAnchorArgument.Anchor toAnchor) {
        Vec3 pos = toAnchor.apply(entity);
        super.lookAt(fromAnchor, pos);
        this.connection.send(new ClientboundPlayerLookAtPacket(fromAnchor, entity, toAnchor));
    }

    public void restoreFrom(ServerPlayer oldPlayer, boolean restoreAll) {
        this.wardenSpawnTracker = oldPlayer.wardenSpawnTracker;
        this.chatSession = oldPlayer.chatSession;
        this.gameMode.setGameModeForPlayer(oldPlayer.gameMode.getGameModeForPlayer(), oldPlayer.gameMode.getPreviousGameModeForPlayer());
        this.onUpdateAbilities();
        this.getAttributes().assignBaseValues(oldPlayer.getAttributes());
        if (restoreAll) {
            this.getAttributes().assignPermanentModifiers(oldPlayer.getAttributes());
            this.setHealth(oldPlayer.getHealth());
            this.foodData = oldPlayer.foodData;

            for (MobEffectInstance effect : oldPlayer.getActiveEffects()) {
                this.addEffect(new MobEffectInstance(effect));
            }

            this.transferInventoryXpAndScore(oldPlayer);
            this.portalProcess = oldPlayer.portalProcess;
        } else {
            this.setHealth(this.getMaxHealth());
            if (this.level().getGameRules().get(GameRules.KEEP_INVENTORY) || oldPlayer.isSpectator()) {
                this.transferInventoryXpAndScore(oldPlayer);
            }
        }

        this.enchantmentSeed = oldPlayer.enchantmentSeed;
        this.enderChestInventory = oldPlayer.enderChestInventory;
        this.getEntityData().set(DATA_PLAYER_MODE_CUSTOMISATION, oldPlayer.getEntityData().get(DATA_PLAYER_MODE_CUSTOMISATION));
        this.lastSentExp = -1;
        this.lastSentHealth = -1.0F;
        this.lastSentFood = -1;
        this.recipeBook.copyOverData(oldPlayer.recipeBook);
        this.seenCredits = oldPlayer.seenCredits;
        this.enteredNetherPosition = oldPlayer.enteredNetherPosition;
        this.chunkTrackingView = oldPlayer.chunkTrackingView;
        this.requestedDebugSubscriptions = oldPlayer.requestedDebugSubscriptions;
        this.setShoulderEntityLeft(oldPlayer.getShoulderEntityLeft());
        this.setShoulderEntityRight(oldPlayer.getShoulderEntityRight());
        this.setLastDeathLocation(oldPlayer.getLastDeathLocation());
        this.waypointIcon().copyFrom(oldPlayer.waypointIcon());
    }

    private void transferInventoryXpAndScore(Player oldPlayer) {
        this.getInventory().replaceWith(oldPlayer.getInventory());
        this.experienceLevel = oldPlayer.experienceLevel;
        this.totalExperience = oldPlayer.totalExperience;
        this.experienceProgress = oldPlayer.experienceProgress;
        this.setScore(oldPlayer.getScore());
    }

    @Override
    protected void onEffectAdded(MobEffectInstance effect, @Nullable Entity source) {
        super.onEffectAdded(effect, source);
        this.connection.send(new ClientboundUpdateMobEffectPacket(this.getId(), effect, true));
        if (effect.is(MobEffects.LEVITATION)) {
            this.levitationStartTime = this.tickCount;
            this.levitationStartPos = this.position();
        }

        CriteriaTriggers.EFFECTS_CHANGED.trigger(this, source);
    }

    @Override
    protected void onEffectUpdated(MobEffectInstance effect, boolean doRefreshAttributes, @Nullable Entity source) {
        super.onEffectUpdated(effect, doRefreshAttributes, source);
        this.connection.send(new ClientboundUpdateMobEffectPacket(this.getId(), effect, false));
        CriteriaTriggers.EFFECTS_CHANGED.trigger(this, source);
    }

    @Override
    protected void onEffectsRemoved(Collection<MobEffectInstance> effects) {
        super.onEffectsRemoved(effects);

        for (MobEffectInstance effect : effects) {
            this.connection.send(new ClientboundRemoveMobEffectPacket(this.getId(), effect.getEffect()));
            if (effect.is(MobEffects.LEVITATION)) {
                this.levitationStartPos = null;
            }
        }

        CriteriaTriggers.EFFECTS_CHANGED.trigger(this, null);
    }

    @Override
    public void teleportTo(double x, double y, double z) {
        this.connection.teleport(new PositionMoveRotation(new Vec3(x, y, z), Vec3.ZERO, 0.0F, 0.0F), Relative.union(Relative.DELTA, Relative.ROTATION));
    }

    @Override
    public void teleportRelative(double dx, double dy, double dz) {
        this.connection.teleport(new PositionMoveRotation(new Vec3(dx, dy, dz), Vec3.ZERO, 0.0F, 0.0F), Relative.ALL);
    }

    @Override
    public boolean teleportTo(ServerLevel level, double x, double y, double z, Set<Relative> relatives, float newYRot, float newXRot, boolean resetCamera) {
        if (this.isSleeping()) {
            this.stopSleepInBed(true, true);
        }

        if (resetCamera) {
            this.setCamera(this);
        }

        boolean success = super.teleportTo(level, x, y, z, relatives, newYRot, newXRot, resetCamera);
        if (success) {
            this.setYHeadRot(relatives.contains(Relative.Y_ROT) ? this.getYHeadRot() + newYRot : newYRot);
            this.connection.resetFlyingTicks();
        }

        return success;
    }

    @Override
    public void snapTo(double x, double y, double z) {
        super.snapTo(x, y, z);
        this.connection.resetPosition();
    }

    @Override
    public void crit(Entity entity) {
        this.level().getChunkSource().sendToTrackingPlayersAndSelf(this, new ClientboundAnimatePacket(entity, 4));
    }

    @Override
    public void magicCrit(Entity entity) {
        this.level().getChunkSource().sendToTrackingPlayersAndSelf(this, new ClientboundAnimatePacket(entity, 5));
    }

    @Override
    public void onUpdateAbilities() {
        if (this.connection != null) {
            this.connection.send(new ClientboundPlayerAbilitiesPacket(this.getAbilities()));
            this.updateInvisibilityStatus();
        }
    }

    public ServerLevel level() {
        return (ServerLevel)super.level();
    }

    public boolean setGameMode(GameType mode) {
        boolean wasSpectator = this.isSpectator();
        if (!this.gameMode.changeGameModeForPlayer(mode)) {
            return false;
        } else {
            this.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.CHANGE_GAME_MODE, mode.getId()));
            if (mode == GameType.SPECTATOR) {
                this.removeEntitiesOnShoulder();
                this.stopRiding();
                this.stopUsingItem();
                EnchantmentHelper.stopLocationBasedEffects(this);
            } else {
                this.setCamera(this);
                if (wasSpectator) {
                    EnchantmentHelper.runLocationChangedEffects(this.level(), this);
                }
            }

            this.onUpdateAbilities();
            this.updateEffectVisibility();
            return true;
        }
    }

    @Override
    public GameType gameMode() {
        return this.gameMode.getGameModeForPlayer();
    }

    public CommandSource commandSource() {
        return this.commandSource;
    }

    public CommandSourceStack createCommandSourceStack() {
        return new CommandSourceStack(
            this.commandSource(),
            this.position(),
            this.getRotationVector(),
            this.level(),
            this.permissions(),
            this.getPlainTextName(),
            this.getDisplayName(),
            this.server,
            this
        );
    }

    @Override
    public void sendSystemMessage(Component message) {
        this.sendSystemMessage(message, false);
    }

    @Override
    public void sendOverlayMessage(Component message) {
        this.sendSystemMessage(message, true);
    }

    public void sendBuildLimitMessage(boolean isTooHigh, int limit) {
        this.sendOverlayMessage(Component.translatable(isTooHigh ? "build.tooHigh" : "build.tooLow", limit).withStyle(ChatFormatting.RED));
    }

    public void sendSpawnProtectionMessage(BlockPos pos) {
        this.sendOverlayMessage(Component.translatable("build.spawn_protection", pos.toShortString()).withStyle(ChatFormatting.RED));
    }

    public void sendSystemMessage(Component message, boolean overlay) {
        if (this.acceptsSystemMessages(overlay)) {
            this.connection
                .send(
                    new ClientboundSystemChatPacket(message, overlay),
                    PacketSendListener.exceptionallySend(
                        () -> {
                            if (this.acceptsSystemMessages(false)) {
                                int truncatedMessageSize = 256;
                                String contents = message.getString(256);
                                Component contentsMsg = Component.literal(contents).withStyle(ChatFormatting.YELLOW);
                                return new ClientboundSystemChatPacket(
                                    Component.translatable("multiplayer.message_not_delivered", contentsMsg).withStyle(ChatFormatting.RED), false
                                );
                            } else {
                                return null;
                            }
                        }
                    )
                );
        }
    }

    public void sendChatMessage(OutgoingChatMessage message, boolean filtered, ChatType.Bound chatType) {
        if (this.acceptsChatMessages()) {
            message.sendToPlayer(this, filtered, chatType);
        }
    }

    public String getIpAddress() {
        return this.connection.getRemoteAddress() instanceof InetSocketAddress ipSocketAddress
            ? InetAddresses.toAddrString(ipSocketAddress.getAddress())
            : "<unknown>";
    }

    public void updateOptions(ClientInformation information) {
        this.language = information.language();
        this.requestedViewDistance = information.viewDistance();
        this.chatVisibility = information.chatVisibility();
        this.canChatColor = information.chatColors();
        this.textFilteringEnabled = information.textFilteringEnabled();
        this.allowsListing = information.allowsListing();
        this.particleStatus = information.particleStatus();
        this.getEntityData().set(DATA_PLAYER_MODE_CUSTOMISATION, (byte)information.modelCustomisation());
        this.getEntityData().set(DATA_PLAYER_MAIN_HAND, information.mainHand());
    }

    public ClientInformation clientInformation() {
        int modelCustomization = this.getEntityData().get(DATA_PLAYER_MODE_CUSTOMISATION);
        return new ClientInformation(
            this.language,
            this.requestedViewDistance,
            this.chatVisibility,
            this.canChatColor,
            modelCustomization,
            this.getMainArm(),
            this.textFilteringEnabled,
            this.allowsListing,
            this.particleStatus
        );
    }

    public boolean canChatInColor() {
        return this.canChatColor;
    }

    public ChatVisiblity getChatVisibility() {
        return this.chatVisibility;
    }

    private boolean acceptsSystemMessages(boolean overlay) {
        return this.chatVisibility == ChatVisiblity.HIDDEN ? overlay : true;
    }

    private boolean acceptsChatMessages() {
        return this.chatVisibility == ChatVisiblity.FULL;
    }

    public int requestedViewDistance() {
        return this.requestedViewDistance;
    }

    public void sendServerStatus(ServerStatus status) {
        this.connection.send(new ClientboundServerDataPacket(status.description(), status.favicon().map(ServerStatus.Favicon::iconBytes)));
    }

    @Override
    public PermissionSet permissions() {
        return this.server.getProfilePermissions(this.nameAndId());
    }

    public void resetLastActionTime() {
        this.lastActionTime = Util.getMillis();
    }

    public ServerStatsCounter getStats() {
        return this.stats;
    }

    public ServerRecipeBook getRecipeBook() {
        return this.recipeBook;
    }

    @Override
    protected void updateInvisibilityStatus() {
        if (this.isSpectator()) {
            this.removeEffectParticles();
            this.setInvisible(true);
        } else {
            super.updateInvisibilityStatus();
        }
    }

    public Entity getCamera() {
        return (Entity)(this.camera == null ? this : this.camera);
    }

    public void setCamera(@Nullable Entity newCamera) {
        Entity oldCamera = this.getCamera();
        this.camera = (Entity)(newCamera == null ? this : newCamera);
        if (oldCamera != this.camera) {
            if (this.camera.level() instanceof ServerLevel level) {
                this.teleportTo(level, this.camera.getX(), this.camera.getY(), this.camera.getZ(), Set.of(), this.getYRot(), this.getXRot(), false);
            }

            if (newCamera != null) {
                this.level().getChunkSource().move(this);
            }

            this.connection.send(new ClientboundSetCameraPacket(this.camera));
            this.connection.resetPosition();
        }
    }

    @Override
    protected void processPortalCooldown() {
        if (!this.isChangingDimension) {
            super.processPortalCooldown();
        }
    }

    public long getLastActionTime() {
        return this.lastActionTime;
    }

    public @Nullable Component getTabListDisplayName() {
        return null;
    }

    public int getTabListOrder() {
        return 0;
    }

    @Override
    public void swing(InteractionHand hand) {
        super.swing(hand);
        this.resetAttackStrengthTicker();
    }

    public boolean isChangingDimension() {
        return this.isChangingDimension;
    }

    public void hasChangedDimension() {
        this.isChangingDimension = false;
    }

    public PlayerAdvancements getAdvancements() {
        return this.advancements;
    }

    public ServerPlayer.@Nullable RespawnConfig getRespawnConfig() {
        return this.respawnConfig;
    }

    public void copyRespawnPosition(ServerPlayer player) {
        this.setRespawnPosition(player.respawnConfig, false);
    }

    public void setRespawnPosition(ServerPlayer.@Nullable RespawnConfig respawnConfig, boolean showMessage) {
        if (showMessage && respawnConfig != null && !respawnConfig.isSamePosition(this.respawnConfig)) {
            this.sendSystemMessage(SPAWN_SET_MESSAGE);
        }

        this.respawnConfig = respawnConfig;
    }

    public SectionPos getLastSectionPos() {
        return this.lastSectionPos;
    }

    public void setLastSectionPos(SectionPos lastSectionPos) {
        this.lastSectionPos = lastSectionPos;
    }

    public ChunkTrackingView getChunkTrackingView() {
        return this.chunkTrackingView;
    }

    public void setChunkTrackingView(ChunkTrackingView chunkTrackingView) {
        this.chunkTrackingView = chunkTrackingView;
    }

    @Override
    public ItemEntity drop(ItemStack itemStack, boolean randomly, boolean thrownFromHand) {
        ItemEntity entity = super.drop(itemStack, randomly, thrownFromHand);
        if (thrownFromHand) {
            ItemStack droppedItemStack = entity != null ? entity.getItem() : ItemStack.EMPTY;
            if (!droppedItemStack.isEmpty()) {
                this.awardStat(Stats.ITEM_DROPPED.get(droppedItemStack.getItem()), itemStack.getCount());
                this.awardStat(Stats.DROP);
            }
        }

        return entity;
    }

    public TextFilter getTextFilter() {
        return this.textFilter;
    }

    public void setServerLevel(ServerLevel level) {
        this.setLevel(level);
        this.gameMode.setLevel(level);
    }

    private static @Nullable GameType readPlayerMode(ValueInput playerInput, String modeTag) {
        return playerInput.read(modeTag, GameType.LEGACY_ID_CODEC).orElse(null);
    }

    private GameType calculateGameModeForNewPlayer(@Nullable GameType loadedGameType) {
        GameType forcedGameType = this.server.getForcedGameType();
        if (forcedGameType != null) {
            return forcedGameType;
        } else {
            return loadedGameType != null ? loadedGameType : this.server.getDefaultGameType();
        }
    }

    private void storeGameTypes(ValueOutput playerOutput) {
        playerOutput.store("playerGameType", GameType.LEGACY_ID_CODEC, this.gameMode.getGameModeForPlayer());
        GameType previousGameMode = this.gameMode.getPreviousGameModeForPlayer();
        playerOutput.storeNullable("previousPlayerGameType", GameType.LEGACY_ID_CODEC, previousGameMode);
    }

    @Override
    public boolean isTextFilteringEnabled() {
        return this.textFilteringEnabled;
    }

    public boolean shouldFilterMessageTo(ServerPlayer serverPlayer) {
        return serverPlayer == this ? false : this.textFilteringEnabled || serverPlayer.textFilteringEnabled;
    }

    @Override
    public boolean mayInteract(ServerLevel level, BlockPos pos) {
        return super.mayInteract(level, pos) && level.mayInteract(this, pos);
    }

    @Override
    protected void updateUsingItem(ItemStack useItem) {
        CriteriaTriggers.USING_ITEM.trigger(this, useItem);
        super.updateUsingItem(useItem);
    }

    public void drop(boolean all) {
        Inventory inventory = this.getInventory();
        ItemStack removed = inventory.removeFromSelected(all);
        this.containerMenu
            .findSlot(inventory, inventory.getSelectedSlot())
            .ifPresent(slotIndex -> this.containerMenu.setRemoteSlot(slotIndex, inventory.getSelectedItem()));
        if (this.useItem.isEmpty()) {
            this.stopUsingItem();
        }

        this.drop(removed, false, true);
    }

    @Override
    public void handleExtraItemsCreatedOnUse(ItemStack extraItems) {
        if (!this.getInventory().add(extraItems)) {
            this.drop(extraItems, false);
        }
    }

    public boolean allowsListing() {
        return this.allowsListing;
    }

    @Override
    public Optional<WardenSpawnTracker> getWardenSpawnTracker() {
        return Optional.of(this.wardenSpawnTracker);
    }

    public void setSpawnExtraParticlesOnFall(boolean toggle) {
        this.spawnExtraParticlesOnFall = toggle;
    }

    @Override
    public void onItemPickup(ItemEntity entity) {
        super.onItemPickup(entity);
        Entity thrower = entity.getOwner();
        if (thrower != null) {
            CriteriaTriggers.THROWN_ITEM_PICKED_UP_BY_PLAYER.trigger(this, entity.getItem(), thrower);
        }
    }

    public void setChatSession(RemoteChatSession chatSession) {
        this.chatSession = chatSession;
    }

    public @Nullable RemoteChatSession getChatSession() {
        return this.chatSession != null && this.chatSession.hasExpired() ? null : this.chatSession;
    }

    @Override
    public void indicateDamage(double xd, double zd) {
        this.hurtDir = (float)(Mth.atan2(zd, xd) * 180.0F / (float)Math.PI - this.getYRot());
        this.connection.send(new ClientboundHurtAnimationPacket(this));
    }

    @Override
    public boolean startRiding(Entity entityToRide, boolean force, boolean sendEventAndTriggers) {
        if (super.startRiding(entityToRide, force, sendEventAndTriggers)) {
            entityToRide.positionRider(this);
            this.connection.teleport(new PositionMoveRotation(this.position(), Vec3.ZERO, 0.0F, 0.0F), Relative.ROTATION);
            if (entityToRide instanceof LivingEntity livingEntity) {
                this.server.getPlayerList().sendActiveEffects(livingEntity, this.connection);
            }

            this.connection.send(new ClientboundSetPassengersPacket(entityToRide));
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void removeVehicle() {
        Entity oldVehicle = this.getVehicle();
        super.removeVehicle();
        if (oldVehicle instanceof LivingEntity livingEntity) {
            for (MobEffectInstance effect : livingEntity.getActiveEffects()) {
                this.connection.send(new ClientboundRemoveMobEffectPacket(oldVehicle.getId(), effect.getEffect()));
            }
        }

        if (oldVehicle != null) {
            this.connection.send(new ClientboundSetPassengersPacket(oldVehicle));
        }
    }

    public CommonPlayerSpawnInfo createCommonSpawnInfo(ServerLevel level) {
        return new CommonPlayerSpawnInfo(
            level.dimensionTypeRegistration(),
            level.dimension(),
            BiomeManager.obfuscateSeed(level.getSeed()),
            this.gameMode.getGameModeForPlayer(),
            this.gameMode.getPreviousGameModeForPlayer(),
            level.isDebug(),
            level.isFlat(),
            this.getLastDeathLocation(),
            this.getPortalCooldown(),
            level.getSeaLevel()
        );
    }

    public void setRaidOmenPosition(BlockPos raidOmenPosition) {
        this.raidOmenPosition = raidOmenPosition;
    }

    public void clearRaidOmenPosition() {
        this.raidOmenPosition = null;
    }

    public @Nullable BlockPos getRaidOmenPosition() {
        return this.raidOmenPosition;
    }

    @Override
    public Vec3 getKnownMovement() {
        Entity vehicle = this.getVehicle();
        return vehicle != null && vehicle.getControllingPassenger() != this ? vehicle.getKnownMovement() : this.lastKnownClientMovement;
    }

    @Override
    public Vec3 getKnownSpeed() {
        Entity vehicle = this.getVehicle();
        return vehicle != null && vehicle.getControllingPassenger() != this ? vehicle.getKnownSpeed() : this.lastKnownClientMovement;
    }

    public void setKnownMovement(Vec3 lastKnownClientMovement) {
        this.lastKnownClientMovement = lastKnownClientMovement;
    }

    @Override
    protected float getEnchantedDamage(Entity entity, float dmg, DamageSource damageSource) {
        return EnchantmentHelper.modifyDamage(this.level(), this.getWeaponItem(), entity, damageSource, dmg);
    }

    @Override
    public void onEquippedItemBroken(Item brokenItem, EquipmentSlot inSlot) {
        super.onEquippedItemBroken(brokenItem, inSlot);
        this.awardStat(Stats.ITEM_BROKEN.get(brokenItem));
    }

    public Input getLastClientInput() {
        return this.lastClientInput;
    }

    public void setLastClientInput(Input lastClientInput) {
        this.lastClientInput = lastClientInput;
    }

    public Vec3 getLastClientMoveIntent() {
        float leftIntent = this.lastClientInput.left() == this.lastClientInput.right() ? 0.0F : (this.lastClientInput.left() ? 1.0F : -1.0F);
        float forwardIntent = this.lastClientInput.forward() == this.lastClientInput.backward() ? 0.0F : (this.lastClientInput.forward() ? 1.0F : -1.0F);
        return getInputVector(new Vec3(leftIntent, 0.0, forwardIntent), 1.0F, this.getYRot());
    }

    public void registerEnderPearl(ThrownEnderpearl enderPearl) {
        this.enderPearls.add(enderPearl);
    }

    public void deregisterEnderPearl(ThrownEnderpearl enderPearl) {
        this.enderPearls.remove(enderPearl);
    }

    public Set<ThrownEnderpearl> getEnderPearls() {
        return this.enderPearls;
    }

    public CompoundTag getShoulderEntityLeft() {
        return this.shoulderEntityLeft;
    }

    protected void setShoulderEntityLeft(CompoundTag tag) {
        this.shoulderEntityLeft = tag;
        this.setShoulderParrotLeft(extractParrotVariant(tag));
    }

    public CompoundTag getShoulderEntityRight() {
        return this.shoulderEntityRight;
    }

    protected void setShoulderEntityRight(CompoundTag tag) {
        this.shoulderEntityRight = tag;
        this.setShoulderParrotRight(extractParrotVariant(tag));
    }

    public long registerAndUpdateEnderPearlTicket(ThrownEnderpearl enderpearl) {
        if (enderpearl.level() instanceof ServerLevel enderPearlLevel) {
            ChunkPos chunkPos = enderpearl.chunkPosition();
            this.registerEnderPearl(enderpearl);
            enderPearlLevel.resetEmptyTime();
            return placeEnderPearlTicket(enderPearlLevel, chunkPos) - 1L;
        } else {
            return 0L;
        }
    }

    public static long placeEnderPearlTicket(ServerLevel level, ChunkPos chunk) {
        level.getChunkSource().addTicketWithRadius(TicketType.ENDER_PEARL, chunk, 2);
        return TicketType.ENDER_PEARL.timeout();
    }

    public void requestDebugSubscriptions(Set<DebugSubscription<?>> subscriptions) {
        this.requestedDebugSubscriptions = Set.copyOf(subscriptions);
    }

    public Set<DebugSubscription<?>> debugSubscriptions() {
        return !this.server.debugSubscribers().hasRequiredPermissions(this) ? Set.of() : this.requestedDebugSubscriptions;
    }

    public record RespawnConfig(LevelData.RespawnData respawnData, boolean forced) {
        public static final Codec<ServerPlayer.RespawnConfig> CODEC = RecordCodecBuilder.create(
            i -> i.group(
                    LevelData.RespawnData.MAP_CODEC.forGetter(ServerPlayer.RespawnConfig::respawnData),
                    Codec.BOOL.optionalFieldOf("forced", false).forGetter(ServerPlayer.RespawnConfig::forced)
                )
                .apply(i, ServerPlayer.RespawnConfig::new)
        );

        private static ResourceKey<Level> getDimensionOrDefault(ServerPlayer.@Nullable RespawnConfig respawnConfig) {
            return respawnConfig != null ? respawnConfig.respawnData().dimension() : Level.OVERWORLD;
        }

        public boolean isSamePosition(ServerPlayer.@Nullable RespawnConfig other) {
            return other != null && this.respawnData.globalPos().equals(other.respawnData.globalPos());
        }
    }

    private record RespawnPosAngle(Vec3 position, float yaw, float pitch) {
        public static ServerPlayer.RespawnPosAngle of(Vec3 position, BlockPos lookAtBlockPos, float pitch) {
            return new ServerPlayer.RespawnPosAngle(position, calculateLookAtYaw(position, lookAtBlockPos), pitch);
        }

        private static float calculateLookAtYaw(Vec3 position, BlockPos lookAtBlockPos) {
            Vec3 lookDirection = Vec3.atBottomCenterOf(lookAtBlockPos).subtract(position).normalize();
            return (float)Mth.wrapDegrees(Mth.atan2(lookDirection.z, lookDirection.x) * 180.0F / (float)Math.PI - 90.0);
        }
    }

    public record SavedPosition(Optional<ResourceKey<Level>> dimension, Optional<Vec3> position, Optional<Vec2> rotation) {
        public static final MapCodec<ServerPlayer.SavedPosition> MAP_CODEC = RecordCodecBuilder.mapCodec(
            i -> i.group(
                    Level.RESOURCE_KEY_CODEC.optionalFieldOf("Dimension").forGetter(ServerPlayer.SavedPosition::dimension),
                    Vec3.CODEC.optionalFieldOf("Pos").forGetter(ServerPlayer.SavedPosition::position),
                    Vec2.CODEC.optionalFieldOf("Rotation").forGetter(ServerPlayer.SavedPosition::rotation)
                )
                .apply(i, ServerPlayer.SavedPosition::new)
        );
        public static final ServerPlayer.SavedPosition EMPTY = new ServerPlayer.SavedPosition(Optional.empty(), Optional.empty(), Optional.empty());
    }
}
