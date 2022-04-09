package dev.bleach.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

@Mixin(PlayerInventory.class)
public class MixinPlayerInventory {
	
	@Shadow public ItemStack[] main;
	@Shadow public int selectedSlot;
	
	@Overwrite public int getInvMaxStackAmount() {
		return 1;
	}

	// Limit pick selecting to only the current slot
	@Overwrite
	public void method_8422(Item item, int i, boolean bl, boolean bl2) {
		this.main[this.selectedSlot] = new ItemStack(item, 1, i);
	}
	
	// No slot scrolling
	@Overwrite
	public void method_3134(int i) {
	}
	
	@Overwrite
	private int method_3138(ItemStack itemStack) {
		return main[selectedSlot] != null &&
				main[selectedSlot].getItem() == itemStack.getItem() &&
				main[selectedSlot].isDamaged() &&
				main[selectedSlot].count < main[selectedSlot].getMaxCount() &&
				main[selectedSlot].count < getInvMaxStackAmount() &&
				(!main[selectedSlot].isUnbreakable() || main[selectedSlot].getMeta() == itemStack.getMeta()) &&
				ItemStack.equalsIgnoreDamage(main[selectedSlot], itemStack) ? selectedSlot : -1;
	}
	
	@Overwrite
	public int method_3146() {
		return main[selectedSlot] == null ? selectedSlot : -1;
	}
}
