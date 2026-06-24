package moscow.elegant.systems.modules.modules.player;

import java.util.function.Predicate;
import moscow.elegant.elegant;
import moscow.elegant.systems.event.EventListener;
import moscow.elegant.systems.event.impl.game.StartBreakBlockEvent;
import moscow.elegant.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.elegant.systems.event.impl.window.KeyPressEvent;
import moscow.elegant.systems.modules.api.ModuleCategory;
import moscow.elegant.systems.modules.api.ModuleInfo;
import moscow.elegant.systems.modules.impl.BaseModule;
import moscow.elegant.systems.setting.settings.BooleanSetting;
import moscow.elegant.systems.setting.settings.SliderSetting;
import moscow.elegant.utility.inventory.InventoryUtility;
import moscow.elegant.utility.inventory.ItemSlot;
import moscow.elegant.utility.inventory.group.SlotGroup;
import moscow.elegant.utility.inventory.group.SlotGroups;
import moscow.elegant.utility.rotations.MoveCorrection;
import moscow.elegant.utility.rotations.Rotation;
import moscow.elegant.utility.rotations.RotationHandler;
import moscow.elegant.utility.rotations.RotationPriority;
import moscow.elegant.utility.time.Timer;
import net.minecraft.util.Hand;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;

@ModuleInfo(
   name = "Mine Helper",
   category = ModuleCategory.PLAYER,
   desc = "Помощник в шахте"
)
public class MineHelper extends BaseModule {
   private final BooleanSetting save = new BooleanSetting(this, "Сохранять кирку", "Не дает сломать блок, если предмет достиг определенной прочности").enable();
   public final SliderSetting percent = new SliderSetting(this, "Прочность").step(1.0F).min(1.0F).max(70.0F).currentValue(10.0F).suffix("%%");
   private final Timer timer = new Timer();
   private boolean rotate;
   private final EventListener<KeyPressEvent> onKeyPressEvent = event -> {
      if (mc.currentScreen == null) {
         ;
      }
   };
   private final EventListener<StartBreakBlockEvent> onStartBreakBlockEvent = event -> {
      if (mc.player != null) {
         ItemStack currentStack = mc.player.getMainHandStack();
         if (this.isValidPickaxe(currentStack)) {
            double durabilityPercent = this.getDurabilityPercent(currentStack);
            if (this.save.isEnabled() && !(durabilityPercent >= this.percent.getCurrentValue())) {
               event.cancel();
            }
         }
      }
   };
   private final EventListener<ClientPlayerTickEvent> onUpdateEvent = event -> {
      if (mc.player != null && this.rotate) {
         elegant.getInstance()
            .getRotationHandler()
            .rotate(new Rotation(mc.player.getYaw(), 90.0F), MoveCorrection.SILENT, 180.0F, 180.0F, 180.0F, RotationPriority.USE_ITEM);
      }
   };

   private void repairPickaxeWithBottle() {
      if (mc.player != null && mc.currentScreen == null) {
         ItemStack pickaxe = mc.player.getMainHandStack();
         if (!this.isValidPickaxe(pickaxe) || pickaxe.getDamage() == 0) {
            this.rotate = false;
         } else if (this.ensureBottleInOffhand()) {
            this.useExperienceBottle();
         }
      }
   }

   private boolean ensureBottleInOffhand() {
      ItemStack offhand = mc.player.getOffHandStack();
      if (offhand.getItem() == Items.EXPERIENCE_BOTTLE) {
         return true;
      } else {
         SlotGroup<ItemSlot> searchArea = SlotGroups.inventory().and(SlotGroups.hotbar());
         ItemSlot bottleSlot = searchArea.findItem((Predicate<ItemStack>)(stack -> stack.getItem() == Items.EXPERIENCE_BOTTLE));
         if (bottleSlot == null) {
            this.rotate = false;
            return false;
         } else {
            InventoryUtility.moveItem(bottleSlot, InventoryUtility.getOffHandSlot());
            return true;
         }
      }
   }

   private void useExperienceBottle() {
      if (mc.player.getOffHandStack().getItem() != Items.EXPERIENCE_BOTTLE) {
         mc.options.useKey.setPressed(false);
         this.rotate = false;
      } else {
         RotationHandler rotation = elegant.getInstance().getRotationHandler();
         mc.interactionManager
            .sendSequencedPacket(
               mc.world,
               sequence -> new PlayerInteractItemC2SPacket(Hand.OFF_HAND, sequence, rotation.getServerRotation().getYaw(), rotation.getServerRotation().getYaw())
            );
      }
   }

   private boolean isValidPickaxe(ItemStack stack) {
      return stack != null && stack.isDamageable() && stack.getItem() instanceof PickaxeItem;
   }

   private double getDurabilityPercent(ItemStack stack) {
      return (double)(stack.getMaxDamage() - stack.getDamage()) / stack.getMaxDamage() * 100.0;
   }
}
