package de.kxmischesdomi.boatcontainer.common.entity;

import de.kxmischesdomi.boatcontainer.common.registry.ModEntities;
import de.kxmischesdomi.boatcontainer.common.registry.ModItems;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HasCustomInventoryScreen;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;

/**
 * @author KxmischesDomi | https://github.com/kxmischesdomi
 * @since 1.0
 */
public class ChestBoatEntity extends BoatWithBlockEntity implements HasCustomInventoryScreen, ContainerEntity {

	private static final int CONTAINER_SIZE = 27;
	private NonNullList<ItemStack> itemStacks = NonNullList.withSize(27, ItemStack.EMPTY);
	@Nullable
	private ResourceLocation lootTable;
	private long lootTableSeed;

	public ChestBoatEntity(EntityType<? extends Boat> entityType, Level world) {
		super(entityType, world);
	}

	public ChestBoatEntity(EntityType<? extends Boat> type, Level world, double x, double y, double z) {
		super(type, world, x, y, z);
	}

	@Override
	public BlockState getDisplayBlockState() {
		return Blocks.CHEST.defaultBlockState();
	}

	@Override
	protected float getSinglePassengerXOffset() {
		return 0.15f;
	}

	@Override
	protected int getMaxPassengers() {
		return 1;
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		this.addChestVehicleSaveData(compoundTag);
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		this.readChestVehicleSaveData(compoundTag);
	}

	@Override
	public void destroy(DamageSource damageSource) {
		super.destroy(damageSource);
		this.chestVehicleDestroyed(damageSource, this.level(), this);
	}

	@Override
	public void remove(Entity.RemovalReason removalReason) {
		if (!this.level().isClientSide && removalReason.shouldDestroy()) {
			Containers.dropContents(this.level(), this, (Container)this);
		}
		super.remove(removalReason);
	}

	@Override
	public InteractionResult interact(Player player, InteractionHand interactionHand) {
		if (!this.canAddPassenger(player) || player.isSecondaryUseActive()) {
			InteractionResult interactionResult = this.interactWithContainerVehicle(player);
			if (interactionResult.consumesAction()) {
				this.gameEvent(GameEvent.CONTAINER_OPEN, player);
				PiglinAi.angerNearbyPiglins(player, true);
			}
			return interactionResult;
		}
		return super.interact(player, interactionHand);
	}

	@Override
	public void openCustomInventoryScreen(Player player) {
		player.openMenu(this);
		if (!player.level().isClientSide) {
			this.gameEvent(GameEvent.CONTAINER_OPEN, player);
			PiglinAi.angerNearbyPiglins(player, true);
		}
	}

	@Override
	public ItemStack getPickResult() {
		int ordinal = getVariant().ordinal();
		if (ModItems.CHEST_BOAT.length > ordinal) {
			return new ItemStack(ModItems.ENDER_CHEST_BOAT[ordinal]);
		}
		return super.getPickResult();
	}

	@Override
	public void clearContent() {
		this.clearChestVehicleContent();
	}

	@Override
	public int getContainerSize() {
		return 27;
	}

	@Override
	public ItemStack getItem(int i) {
		return this.getChestVehicleItem(i);
	}

	@Override
	public ItemStack removeItem(int i, int j) {
		return this.removeChestVehicleItem(i, j);
	}

	@Override
	public ItemStack removeItemNoUpdate(int i) {
		return this.removeChestVehicleItemNoUpdate(i);
	}

	@Override
	public void setItem(int i, ItemStack itemStack) {
		this.setChestVehicleItem(i, itemStack);
	}

	@Override
	public SlotAccess getSlot(int i) {
		return this.getChestVehicleSlot(i);
	}

	@Override
	public void setChanged() {
	}

	@Override
	public boolean stillValid(Player player) {
		return this.isChestVehicleStillValid(player);
	}

	@Override
	@Nullable
	public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
		if (this.lootTable == null || !player.isSpectator()) {
			this.unpackLootTable(inventory.player);
			return ChestMenu.threeRows(i, inventory, this);
		}
		return null;
	}

	public void unpackLootTable(@Nullable Player player) {
		this.unpackChestVehicleLootTable(player);
	}

	@Override
	@Nullable
	public ResourceLocation getLootTable() {
		return this.lootTable;
	}

	@Override
	public void setLootTable(@Nullable ResourceLocation resourceLocation) {
		this.lootTable = resourceLocation;
	}

	@Override
	public long getLootTableSeed() {
		return this.lootTableSeed;
	}

	@Override
	public void setLootTableSeed(long l) {
		this.lootTableSeed = l;
	}

	@Override
	public NonNullList<ItemStack> getItemStacks() {
		return this.itemStacks;
	}

	@Override
	public void clearItemStacks() {
		this.itemStacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
	}

	@Override
	public void stopOpen(Player player) {
		this.level().gameEvent(GameEvent.CONTAINER_CLOSE, this.position(), GameEvent.Context.of(player));
	}

}
