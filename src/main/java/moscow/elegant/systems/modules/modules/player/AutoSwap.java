package moscow.elegant.systems.modules.modules.player;

import java.util.Comparator;
import java.util.List;
import moscow.elegant.elegant;
import moscow.elegant.systems.event.EventListener;
import moscow.elegant.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.elegant.systems.event.impl.window.KeyPressEvent;
import moscow.elegant.systems.event.impl.window.MouseEvent;
import moscow.elegant.systems.modules.api.ModuleCategory;
import moscow.elegant.systems.modules.api.ModuleInfo;
import moscow.elegant.systems.modules.impl.BaseModule;
import moscow.elegant.systems.notifications.NotificationType;
import moscow.elegant.systems.setting.settings.BindSetting;
import moscow.elegant.systems.setting.settings.ModeSetting;
import moscow.elegant.utility.game.ItemUtility;
import moscow.elegant.utility.game.server.ServerUtility;
import moscow.elegant.utility.inventory.InventoryUtility;
import moscow.elegant.utility.inventory.ItemSlot;
import moscow.elegant.utility.inventory.group.SlotGroup;
import moscow.elegant.utility.inventory.group.SlotGroups;
import moscow.elegant.utility.inventory.slots.HotbarSlot;
import moscow.elegant.utility.time.Timer;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;

@ModuleInfo(
   name = "Auto Swap",
   category = ModuleCategory.COMBAT
)
public class AutoSwap extends BaseModule {
   private final BindSetting button = new BindSetting(this, "modules.settings.auto_swap.button");
   private final ModeSetting itemMode = new ModeSetting(this, "modules.settings.auto_swap.item");
   private final ModeSetting.Value swapTal = new ModeSetting.Value(this.itemMode, "modules.settings.auto_swap.item.talisman").select();
   private final ModeSetting swapToMode = new ModeSetting(this, "modules.settings.auto_swap.swap_to");
   private final ModeSetting.Value swapToTal = new ModeSetting.Value(this.swapToMode, "modules.settings.auto_swap.swap_to.talisman").select();
   private final Timer timer = new Timer();
   private AutoSwap.SwapTask swapTask;
   private final EventListener<ClientPlayerTickEvent> onTick = event -> {
      if (this.swapTask != null && mc.player != null) {
         switch (this.swapTask.stage) {
            case 0:
               if (this.swapTask.ticksWaited < 2) {
                  this.swapTask.ticksWaited++;
                  return;
               }

               if (mc.currentScreen == null) {
                  mc.setScreen(new InventoryScreen(mc.player));
               }

               this.swapTask.ticksWaited = 0;
               this.swapTask.stage++;
               break;
            case 1:
               if (this.swapTask.ticksWaited < 2) {
                  this.swapTask.ticksWaited++;
                  return;
               }

               this.swapTask.stage++;
               break;
            case 2:
               if (mc.player.getOffHandStack().getItem() != this.swapTask.slot.item()
                  && mc.player.getOffHandStack().getItem() != this.swapTask.slot1.item()) {
                  InventoryUtility.moveToOffHand(this.swapTask.slot);
               } else if (this.swapTask.slot instanceof HotbarSlot hotbarSlot && !mc.player.isUsingItem()) {
                  mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(hotbarSlot.getSlotId()));
                  mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
                  mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
               } else if (this.swapTask.slot1 instanceof HotbarSlot hotbarSlot && !mc.player.isUsingItem()) {
                  mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(hotbarSlot.getSlotId()));
                  mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
                  mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
               } else {
                  this.swapTask.slot.swapTo(this.swapTask.slot1);
               }

               this.swapTask.stage++;
               break;
            case 3:
               mc.setScreen(null);
               this.timer.reset();
               elegant.getInstance()
                  .getNotificationManager()
                  .addNotificationOther(
                     NotificationType.SUCCESS,
                     this.getName(),
                     mc.player
                        .getOffHandStack()
                        .getName()
                        .getString()
                        .replace("[", "")
                        .replace("] ", "")
                        .replace("xxx ", "")
                        .replace(" xxx", "")
                        .replace("123 ", "")
                        .replace(" 123", "")
                  );
               this.swapTask = null;
         }
      }
   };
   private final EventListener<KeyPressEvent> onKeyPressEvent = event -> {
      if (mc.currentScreen == null) {
         if (event.getAction() == 1 && this.button.isKey(event.getKey())) {
            this.startSwap();
         }
      }
   };
   private final EventListener<MouseEvent> onMouseEvent = event -> {
      if (mc.currentScreen == null) {
         if (this.button.isKey(event.getButton())) {
            this.startSwap();
         }
      }
   };

   public AutoSwap() {
      new ModeSetting.Value(this.swapToMode, "modules.settings.auto_swap.swap_to.orb");
      new ModeSetting.Value(this.itemMode, "modules.settings.auto_swap.item.orb");
   }

   private void startSwap() {
      if (mc.player != null) {
         if (mc.currentScreen == null && (!ServerUtility.isST() || this.timer.finished(1000L))) {
            if (this.swapTask == null) {
               SlotGroup<ItemSlot> slotsToSearch = SlotGroups.inventory().and(SlotGroups.hotbar()).and(SlotGroups.offhand());
               List<ItemSlot> slots = slotsToSearch.findItems(this.swapTal.isSelected() ? Items.TOTEM_OF_UNDYING : Items.PLAYER_HEAD);
               List<ItemSlot> slots1 = slotsToSearch.findItems(this.swapToTal.isSelected() ? Items.TOTEM_OF_UNDYING : Items.PLAYER_HEAD);
               ItemSlot slot = slots.stream()
                  .min(Comparator.comparingInt(stack -> ItemUtility.bestFactor(stack.itemStack()) - (stack.getIdForServer() == 45 ? 99 : 0)))
                  .orElse(null);
               ItemSlot slot1 = slots1.stream()
                  .filter(slotW -> slot != slotW)
                  .min(Comparator.comparingInt(stack -> ItemUtility.bestFactor(stack.itemStack()) - (stack.getIdForServer() == 45 ? 99 : 0)))
                  .orElse(null);
               if (slot != null && slot1 != null) {
                  mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, Mode.RELEASE_SHIFT_KEY));
                  this.swapTask = new AutoSwap.SwapTask(slot, slot1);
               }
            }
         }
      }
   }

   @Override
   public boolean isHidden() {
      return super.isHidden() || ServerUtility.isRW() || ServerUtility.isHW();
   }

   @Override
   public void setEnabled(boolean newState, boolean silent) {
      if (!newState || !this.isHidden()) {
         super.setEnabled(newState, silent);
      }
   }

   @Override
   public void tick() {
      if (this.isHidden()) {
         super.setEnabled(false, true);
      }
   }

   private static class SwapTask {
      int stage = 0;
      int ticksWaited = 0;
      final ItemSlot slot;
      final ItemSlot slot1;

      SwapTask(ItemSlot slot, ItemSlot slot1) {
         this.slot = slot;
         this.slot1 = slot1;
      }
   }
}
