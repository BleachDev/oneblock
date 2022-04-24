package dev.bleach.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

@Mixin(PlayerInventory.class)
public class MixinPlayerInventory {
	
	@Shadow public DefaultedList<ItemStack> field_15082;
	@Shadow public int selectedSlot;
	
	@Overwrite public int getInvMaxStackAmount() {
		return 1;
	}

	// Limit pick selecting to only the current slot
	@Overwrite
	public void method_13250(ItemStack itemStack) {
		this.field_15082.set(this.selectedSlot, itemStack);
	}
	
	// No slot scrolling
	@Overwrite
	public void method_3134(int i) {
	}
	
	@Overwrite
	public int method_3138(ItemStack itemStack) {
		return field_15082.get(selectedSlot).getCount() != 0 &&
				field_15082.get(selectedSlot).getItem() == itemStack.getItem() &&
				field_15082.get(selectedSlot).isDamaged() &&
				field_15082.get(selectedSlot).getCount() < field_15082.get(selectedSlot).getMaxCount() &&
				field_15082.get(selectedSlot).getCount() < getInvMaxStackAmount() &&
				(!field_15082.get(selectedSlot).isUnbreakable() || field_15082.get(selectedSlot).getMeta() == itemStack.getMeta()) &&
				ItemStack.equalsIgnoreDamage(field_15082.get(selectedSlot), itemStack) ? selectedSlot : -1;
	}
	
	@Overwrite
	public int method_3146() {
		return field_15082.get(selectedSlot).getCount() == 0 ? selectedSlot : -1;
	}
}
