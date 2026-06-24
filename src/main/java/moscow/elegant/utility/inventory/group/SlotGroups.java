package moscow.elegant.utility.inventory.group;

import moscow.elegant.utility.inventory.group.impl.ArmorSlotsGroup;
import moscow.elegant.utility.inventory.group.impl.HotbarSlotsGroup;
import moscow.elegant.utility.inventory.group.impl.InventorySlotsGroup;
import moscow.elegant.utility.inventory.group.impl.OffhandSlotGroup;
import moscow.elegant.utility.inventory.slots.ArmorSlot;
import moscow.elegant.utility.inventory.slots.HotbarSlot;
import moscow.elegant.utility.inventory.slots.InventorySlot;
import moscow.elegant.utility.inventory.slots.OffhandSlot;

public class SlotGroups {
   private SlotGroups() {
   }

   public static SlotGroup<HotbarSlot> hotbar() {
      return new HotbarSlotsGroup();
   }

   public static SlotGroup<InventorySlot> inventory() {
      return new InventorySlotsGroup();
   }

   public static SlotGroup<ArmorSlot> armor() {
      return new ArmorSlotsGroup();
   }

   public static SlotGroup<OffhandSlot> offhand() {
      return new OffhandSlotGroup();
   }
}
