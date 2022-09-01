package dev.cammiescorner.arcanuscontinuum.common.components;

import dev.cammiescorner.arcanuscontinuum.api.entities.ArcanusEntityAttributes;
import dev.cammiescorner.arcanuscontinuum.common.registry.ArcanusComponents;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.nbt.NbtCompound;

import java.util.UUID;

public class BurnoutComponent implements AutoSyncedComponent, ServerTickingComponent {
	public static final UUID uUID = UUID.fromString("c2223d02-f2f0-4fa9-b9d8-5b2c265a8195");
	private final LivingEntity entity;
	private double burnout;

	public BurnoutComponent(LivingEntity entity) {
		this.entity = entity;
	}

	@Override
	public void serverTick() {
		EntityAttributeInstance burnoutRegenAttr = entity.getAttributeInstance(ArcanusEntityAttributes.BURNOUT_REGEN);
		EntityAttributeInstance attackSpeedAttr = entity.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_SPEED);
		long timer = entity.world.getTime() - ArcanusComponents.getLastCastTime(entity);

		if(burnoutRegenAttr != null && drainBurnout(1, true) && timer % (burnoutRegenAttr.getValue() * 20) == 0)
			drainBurnout(1, false);

		if(attackSpeedAttr != null) {
			if(burnout > 0 && attackSpeedAttr.getModifier(uUID) == null)
				attackSpeedAttr.addPersistentModifier(new EntityAttributeModifier(uUID, "Burnout modifier", -0.5, EntityAttributeModifier.Operation.MULTIPLY_BASE));
			if(burnout <= 0 && attackSpeedAttr.getModifier(uUID) != null)
				attackSpeedAttr.removeModifier(uUID);
		}
	}

	@Override
	public void readFromNbt(NbtCompound tag) {
		burnout = tag.getDouble("Burnout");
	}

	@Override
	public void writeToNbt(NbtCompound tag) {
		tag.putDouble("Burnout", burnout);
	}

	public double getBurnout() {
		return burnout;
	}

	public void setBurnout(double burnout) {
		this.burnout = burnout;
		ArcanusComponents.BURNOUT_COMPONENT.sync(entity);
	}

	public boolean addBurnout(double amount, boolean simulate) {
		if(getBurnout() < ArcanusComponents.getMaxMana(entity)) {
			if(!simulate)
				setBurnout(Math.min(ArcanusComponents.getMaxMana(entity), getBurnout() + amount));

			return true;
		}

		return false;
	}

	public boolean drainBurnout(double amount, boolean simulate) {
		if(getBurnout() - amount >= 0) {
			if(!simulate)
				setBurnout(getBurnout() - amount);

			return true;
		}

		return false;
	}
}
