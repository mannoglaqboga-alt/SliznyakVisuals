package moscow.elegant.systems.modules.modules.visuals;

import lombok.Generated;
import moscow.elegant.systems.modules.api.ModuleCategory;
import moscow.elegant.systems.modules.api.ModuleInfo;
import moscow.elegant.systems.modules.impl.BaseModule;
import moscow.elegant.systems.setting.settings.BooleanSetting;
import moscow.elegant.systems.setting.settings.ColorSetting;
import moscow.elegant.systems.setting.settings.RangeSetting;
import moscow.elegant.systems.setting.settings.SliderSetting;
import moscow.elegant.utility.colors.ColorRGBA;
import moscow.elegant.utility.colors.Colors;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.block.enums.CameraSubmersionType;

@ModuleInfo(
   name = "Custom Fog",
   category = ModuleCategory.VISUALS
)
public class CustomFog extends BaseModule {
   private final RangeSetting distance = new RangeSetting(this, "modules.settings.custom_fog.distance")
      .min(1.0F)
      .max(100.0F)
      .step(1.0F)
      .firstValue(10.0F)
      .secondValue(50.0F);
   private final SliderSetting density = new SliderSetting(this, "modules.settings.custom_fog.density")
      .min(0.1F)
      .max(2.0F)
      .step(0.05F)
      .currentValue(1.0F);
   private final BooleanSetting syncWithTheme = new BooleanSetting(this, "modules.settings.custom_fog.sync_with_theme").enabled(true);
   private final ColorSetting fogColor = new ColorSetting(this, "modules.settings.custom_fog.color", () -> this.syncWithTheme.isEnabled())
      .color(Colors.getAccentColor())
      .alpha(true);

   public boolean shouldModifyFog(Camera camera) {
      if (this.isEnabled() && mc.world != null && mc.player != null) {
         Entity entity = camera.getFocusedEntity();
         if (camera.getSubmersionType() == CameraSubmersionType.WATER) {
            return false;
         } else if (camera.getSubmersionType() == CameraSubmersionType.LAVA) {
            return false;
         } else if (camera.getSubmersionType() == CameraSubmersionType.POWDER_SNOW) {
            return false;
         } else {
            if (entity instanceof LivingEntity livingEntity) {
               if (livingEntity.hasStatusEffect(StatusEffects.BLINDNESS)) {
                  return false;
               }

               if (livingEntity.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
                  return false;
               }
            }

            return true;
         }
      } else {
         return false;
      }
   }

   @Generated
   public RangeSetting getDistance() {
      return this.distance;
   }

   @Generated
   public SliderSetting getDensity() {
      return this.density;
   }

   @Generated
   public ColorSetting getFogColor() {
      return this.fogColor;
   }

   public ColorRGBA getFogColorValue() {
      return this.syncWithTheme.isEnabled() ? Colors.getAccentColor() : this.fogColor.getColor();
   }

   public float getFogDensity() {
      return this.density.getCurrentValue();
   }
}
