package moscow.elegant.utility.integration;

import moscow.elegant.elegant;
import moscow.elegant.systems.event.EventListener;
import moscow.elegant.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.elegant.systems.notifications.NotificationType;
import moscow.elegant.utility.interfaces.IMinecraft;
import moscow.elegant.utility.inventory.InventoryUtility;
import moscow.elegant.utility.inventory.ItemSlot;
import moscow.elegant.utility.inventory.group.SlotGroup;
import moscow.elegant.utility.inventory.group.SlotGroups;
import moscow.elegant.utility.inventory.slots.HotbarSlot;
import moscow.elegant.utility.time.Timer;
import net.minecraft.util.Hand;
import net.minecraft.item.Item;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;

public class SwapIntegration implements IMinecraft {
   private Item itemToUse = null;
   private HotbarSlot originalSlot = null;
   private boolean isProcessingItem = false;
   private ItemSlot targetSlot = null;
   private final Timer itemUseTimer = new Timer();
   private SwapIntegration.ItemUseState currentState = SwapIntegration.ItemUseState.IDLE;
   private final EventListener<ClientPlayerTickEvent> onTick = event -> {
      if (this.isProcessingItem) {
         this.processItemUse();
      }
   };

   public SwapIntegration() {
      elegant.getInstance().getEventManager().subscribe(this);
   }

   private void processItemUse() {
      if (mc.player != null && mc.world != null && mc.interactionManager != null && mc.player.getItemCooldownManager() != null) {
         switch (this.currentState) {
            case USING_ITEM:
               mc.interactionManager
                  .sendSequencedPacket(
                     mc.world, sequence -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, sequence, mc.player.getYaw(), mc.player.getPitch())
                  );
               this.currentState = SwapIntegration.ItemUseState.RETURNING_SLOT;
               break;
            case RETURNING_SLOT:
               InventoryUtility.selectHotbarSlot(this.originalSlot);
               this.resetUseState();
               break;
            default:
               this.isProcessingItem = false;
               this.currentState = SwapIntegration.ItemUseState.IDLE;
         }
      } else {
         this.isProcessingItem = false;
         this.currentState = SwapIntegration.ItemUseState.IDLE;
      }
   }

   public void useItem(Item itemType) {
      if (mc.player != null && mc.world != null && mc.interactionManager != null && mc.currentScreen == null && !this.isProcessingItem) {
         SlotGroup<HotbarSlot> hotbarGroup = SlotGroups.hotbar();
         HotbarSlot itemSlot = hotbarGroup.findItem(itemType);
         if (itemSlot == null) {
            elegant.getInstance()
               .getNotificationManager()
               .addNotificationOther(NotificationType.ERROR, "Предмет не найден", "Вам необходимо иметь " + itemType.getName().getString() + " в хотбаре");
         } else if (!mc.player.getItemCooldownManager().isCoolingDown(itemSlot.itemStack())) {
            this.itemToUse = itemType;
            this.originalSlot = InventoryUtility.getCurrentHotbarSlot();
            this.targetSlot = itemSlot;
            this.isProcessingItem = true;
            this.currentState = SwapIntegration.ItemUseState.USING_ITEM;
            this.itemUseTimer.reset();
            if (InventoryUtility.getCurrentHotbarSlot().item() != itemType) {
               InventoryUtility.selectHotbarSlot(itemSlot);
            }
         }
      }
   }

   private void resetUseState() {
      this.isProcessingItem = false;
      this.currentState = SwapIntegration.ItemUseState.IDLE;
      this.itemToUse = null;
      this.originalSlot = null;
      this.targetSlot = null;
   }

   private static enum ItemUseState {
      IDLE,
      USING_ITEM,
      RETURNING_SLOT;
   }
}
