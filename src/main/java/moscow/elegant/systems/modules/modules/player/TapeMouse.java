package moscow.elegant.systems.modules.modules.player;

import moscow.elegant.systems.event.EventListener;
import moscow.elegant.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.elegant.systems.modules.api.ModuleCategory;
import moscow.elegant.systems.modules.api.ModuleInfo;
import moscow.elegant.systems.modules.impl.BaseModule;
import moscow.elegant.systems.setting.settings.ModeSetting;
import moscow.elegant.systems.setting.settings.SliderSetting;
import moscow.elegant.utility.time.Timer;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;

@ModuleInfo(
   name = "Tape Mouse",
   category = ModuleCategory.PLAYER,
   desc = "modules.descriptions.tape_mouse"
)
public class TapeMouse extends BaseModule {
   private final ModeSetting mouseButton = new ModeSetting(this, "Кнопка мыши");
   private final ModeSetting.Value leftButton = new ModeSetting.Value(this.mouseButton, "Левая кнопка").select();
   private final ModeSetting.Value rightButton = new ModeSetting.Value(this.mouseButton, "Правая кнопка");
   private final SliderSetting delay = new SliderSetting(this, "Задержка").min(1.0F).max(100.0F).step(1.0F).currentValue(50.0F).suffix(" мс");
   private final Timer clickTimer = new Timer();
   private float lastDelay = 50.0F;
   private final EventListener<ClientPlayerTickEvent> onTick = event -> {
      if (mc.player != null && mc.currentScreen == null) {
         float currentDelay = this.delay.getCurrentValue();
         if (currentDelay != this.lastDelay) {
            this.clickTimer.reset();
            this.lastDelay = currentDelay;
         }

         long delayMs = (long)currentDelay;
         if (this.clickTimer.finished(delayMs)) {
            if (this.leftButton.isSelected()) {
               this.performLeftClick();
            } else if (this.rightButton.isSelected()) {
               this.performRightClick();
            }

            this.clickTimer.reset();
         }
      }
   };

   private void performLeftClick() {
      if (mc.interactionManager != null) {
         if (mc.targetedEntity != null) {
            mc.interactionManager.attackEntity(mc.player, mc.targetedEntity);
            mc.player.swingHand(Hand.MAIN_HAND);
         } else if (mc.crosshairTarget != null) {
            switch (mc.crosshairTarget.getType()) {
               case BLOCK:
                  mc.interactionManager.updateBlockBreakingProgress(((BlockHitResult)mc.crosshairTarget).getBlockPos(), ((BlockHitResult)mc.crosshairTarget).getSide());
                  mc.player.swingHand(Hand.MAIN_HAND);
                  break;
               case ENTITY:
                  if (mc.targetedEntity != null) {
                     mc.interactionManager.attackEntity(mc.player, mc.targetedEntity);
                     mc.player.swingHand(Hand.MAIN_HAND);
                  }
                  break;
               default:
                  mc.player.swingHand(Hand.MAIN_HAND);
            }
         }
      }
   }

   private void performRightClick() {
      if (mc.interactionManager != null) {
         if (mc.crosshairTarget != null) {
            switch (mc.crosshairTarget.getType()) {
               case BLOCK:
                  mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, (BlockHitResult)mc.crosshairTarget);
                  break;
               case ENTITY:
                  if (mc.targetedEntity != null) {
                     mc.interactionManager.interactEntity(mc.player, mc.targetedEntity, Hand.MAIN_HAND);
                  }
                  break;
               default:
                  mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            }
         } else {
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
         }

         mc.player.swingHand(Hand.MAIN_HAND);
      }
   }

   @Override
   public void onEnable() {
      this.clickTimer.reset();
   }
}
