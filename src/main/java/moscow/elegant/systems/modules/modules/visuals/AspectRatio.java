package moscow.elegant.systems.modules.modules.visuals;

import moscow.elegant.systems.modules.api.ModuleCategory;
import moscow.elegant.systems.modules.api.ModuleInfo;
import moscow.elegant.systems.modules.impl.BaseModule;
import moscow.elegant.systems.setting.settings.SliderSetting;

@ModuleInfo(
   name = "Aspect Ratio",
   category = ModuleCategory.VISUALS,
   desc = "modules.descriptions.aspect_ratio"
)
public class AspectRatio extends BaseModule {
   private final SliderSetting ratio = new SliderSetting(this, "modules.settings.aspect_ratio.ratio").step(0.001F).min(0.5F).max(5.0F).currentValue(1.7777F);
   private static int renderingHandsCount = 0;

   @Override
   public void onEnable() {
   }

   @Override
   public void onDisable() {
   }

   public float getRatio() {
      return this.ratio.getCurrentValue();
   }

   public static void setRenderingHands(boolean rendering) {
      if (rendering) {
         renderingHandsCount++;
      } else if (renderingHandsCount > 0) {
         renderingHandsCount--;
      }
   }

   public static boolean isRenderingHands() {
      return renderingHandsCount > 0;
   }
}
