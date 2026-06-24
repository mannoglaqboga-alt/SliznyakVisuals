package moscow.elegant.systems.modules.modules.visuals;

import lombok.Generated;
import moscow.elegant.systems.modules.api.ModuleCategory;
import moscow.elegant.systems.modules.api.ModuleInfo;
import moscow.elegant.systems.modules.impl.BaseModule;
import moscow.elegant.systems.setting.settings.BooleanSetting;
import moscow.elegant.systems.setting.settings.SliderSetting;

@ModuleInfo(
   name = "Optimization",
   category = ModuleCategory.VISUALS,
   desc = "modules.descriptions.optimization"
)
public class Optimization extends BaseModule {
   private final BooleanSetting reduceParticles = new BooleanSetting(this, "modules.settings.optimization.reduce_particles").enabled(true);
   private final BooleanSetting noWeather = new BooleanSetting(this, "modules.settings.optimization.no_weather").enabled(true);
   private final BooleanSetting fastRender = new BooleanSetting(this, "modules.settings.optimization.fast_render");
   private final SliderSetting particleMultiplier = new SliderSetting(this, "modules.settings.optimization.particle_multiplier")
      .min(0.0F).max(1.0F).step(0.1F).currentValue(0.5F);

   @Generated
   public boolean shouldReduceParticles() {
      return this.reduceParticles.isEnabled();
   }

   @Generated
   public boolean isNoWeather() {
      return this.noWeather.isEnabled();
   }

   @Generated
   public boolean isFastRender() {
      return this.fastRender.isEnabled();
   }

   @Generated
   public float getParticleMultiplier() {
      return this.particleMultiplier.getCurrentValue();
   }
}