package moscow.elegant.utility.inventory.group.impl;

import java.util.List;
import moscow.elegant.utility.inventory.group.SlotGroup;
import moscow.elegant.utility.inventory.slots.OffhandSlot;

public class OffhandSlotGroup extends SlotGroup<OffhandSlot> {
   public OffhandSlotGroup() {
      super(List.of(new OffhandSlot()));
   }
}
