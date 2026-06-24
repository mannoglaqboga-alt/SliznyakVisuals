package moscow.elegant.systems.modules.modules.visuals;

import moscow.elegant.systems.event.EventListener;
import moscow.elegant.systems.event.impl.render.Render3DEvent;
import moscow.elegant.systems.modules.api.ModuleCategory;
import moscow.elegant.systems.modules.api.ModuleInfo;
import moscow.elegant.systems.modules.impl.BaseModule;
import moscow.elegant.systems.setting.settings.BooleanSetting;
import moscow.elegant.systems.setting.settings.ColorSetting;
import moscow.elegant.systems.setting.settings.SliderSetting;
import moscow.elegant.utility.colors.ColorRGBA;
import moscow.elegant.utility.colors.Colors;
import moscow.elegant.utility.render.Chinahat;
import moscow.elegant.utility.render.RenderUtility;
import net.minecraft.client.network.AbstractClientPlayerEntity;

@ModuleInfo(
   name = "Chinahat",
   desc = "modules.descriptions.chinahat",
   category = ModuleCategory.VISUALS
)
public class ChinahatModule extends BaseModule {
   private final BooleanSetting renderOwnPlayer = new BooleanSetting(this, "modules.settings.chinahat.render_own").enabled(false);
   private final BooleanSetting renderPlayers = new BooleanSetting(this, "modules.settings.chinahat.render_players").enabled(true);

   // For self
   private final SliderSetting width = new SliderSetting(this, "modules.settings.chinahat.width")
      .min(0.2F).max(3.0F).step(0.1F).currentValue(0.7F);
   private final SliderSetting height = new SliderSetting(this, "modules.settings.chinahat.height")
      .min(0.05F).max(0.8F).step(0.05F).currentValue(0.25F);
   private final BooleanSetting syncWithTheme = new BooleanSetting(this, "modules.settings.chinahat.sync_with_theme").enabled(true);
   private final ColorSetting color = new ColorSetting(this, "modules.settings.chinahat.color", () -> this.syncWithTheme.isEnabled())
      .color(new ColorRGBA(100.0F, 150.0F, 200.0F, 255.0F))
      .alpha(true);
   private final SliderSetting alpha = new SliderSetting(this, "modules.settings.chinahat.alpha")
      .min(0.0F).max(1.0F).step(0.05F).currentValue(0.85F);
   private final SliderSetting heightOffset = new SliderSetting(this, "modules.settings.chinahat.height_offset")
      .min(-0.5F).max(0.5F).step(0.05F).currentValue(0.1F);

   // For others
   private final SliderSetting widthOthers = new SliderSetting(this, "modules.settings.chinahat.width_others")
      .min(0.2F).max(3.0F).step(0.1F).currentValue(1.0F);
   private final SliderSetting heightOthers = new SliderSetting(this, "modules.settings.chinahat.height_others")
      .min(0.05F).max(0.8F).step(0.05F).currentValue(0.25F);
   private final ColorSetting colorOthers = new ColorSetting(this, "modules.settings.chinahat.color_others")
      .color(new ColorRGBA(100.0F, 150.0F, 200.0F, 255.0F))
      .alpha(true);
   private final SliderSetting alphaOthers = new SliderSetting(this, "modules.settings.chinahat.alpha_others")
      .min(0.0F).max(1.0F).step(0.05F).currentValue(0.85F);

   private final EventListener<Render3DEvent> onRender3D = event -> {
      if (mc.world == null || mc.player == null) {
         return;
      }

      if (!Interface.showChinahat()) {
         return;
      }

      RenderUtility.setupRender3D(true);

      for (AbstractClientPlayerEntity player : mc.world.getPlayers()) {
         if (player == mc.player && !this.renderOwnPlayer.isEnabled()) {
            continue;
         }
         if (player != mc.player && !this.renderPlayers.isEnabled()) {
            continue;
         }

         boolean isSelf = player == mc.player;
         float w = isSelf ? this.width.getCurrentValue() : this.widthOthers.getCurrentValue();
         float h = isSelf ? this.height.getCurrentValue() : this.heightOthers.getCurrentValue();
         ColorRGBA col = isSelf 
            ? (this.syncWithTheme.isEnabled() ? Colors.getAccentColor() : this.color.getColor()) 
            : this.colorOthers.getColor();
         float a = isSelf ? this.alpha.getCurrentValue() : this.alphaOthers.getCurrentValue();
         float off = isSelf ? this.heightOffset.getCurrentValue() : 0.1F;

         Chinahat.render(
            player,
            event.getMatrices(),
            mc.gameRenderer.getCamera(),
            event.getTickDelta(),
            w,
            h,
            col,
            0.3F + a * 0.7F,
            true,
            off
         );
      }

      RenderUtility.endRender3D();
   };

   public SliderSetting getWidth() {
      return this.width;
   }

   public SliderSetting getHeight() {
      return this.height;
   }

   public ColorSetting getColor() {
      return this.color;
   }

   public SliderSetting getAlpha() {
      return this.alpha;
   }

   public SliderSetting getHeightOffset() {
      return this.heightOffset;
   }
}
