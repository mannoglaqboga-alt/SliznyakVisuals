package moscow.elegant.ui.hud.impl;

import moscow.elegant.elegant;
import moscow.elegant.framework.base.UIContext;
import moscow.elegant.framework.msdf.Fonts;
import moscow.elegant.framework.objects.BorderRadius;
import moscow.elegant.systems.modules.modules.visuals.Interface;
import moscow.elegant.systems.setting.settings.BooleanSetting;
import moscow.elegant.systems.theme.Theme;
import moscow.elegant.ui.hud.HudElement;
import moscow.elegant.utility.colors.ColorRGBA;
import moscow.elegant.utility.colors.Colors;
import net.minecraft.client.network.PlayerListEntry;

public class Watermark extends HudElement {
   private final BooleanSetting showRole = new BooleanSetting(this, "hud.watermark.show_role").enabled(true);
   private final BooleanSetting showPing = new BooleanSetting(this, "hud.watermark.show_ping").enabled(true);
   private final BooleanSetting showFps = new BooleanSetting(this, "hud.watermark.show_fps").enabled(true);

   public Watermark() {
      super("hud.watermark", "icons/hud/watermark.png");
   }

   @Override
   public void update(UIContext context) {
      this.height = 20.0F;

      float padding = 6.0F;
      float pillGap = 4.0F;
      float totalWidth = padding;

      String clientName = Interface.showMinimalizm() ? "Slizen" : "SliznyakVisual";
      float nameFontSize = 7.5F;
      float pillHorizontalPadding = 8.0F;
      float nameWidth = Fonts.MEDIUM.getFont(nameFontSize).width(clientName);
      totalWidth += pillHorizontalPadding * 2.0F + nameWidth;

      if (this.showFps.isEnabled()) {
         String fpsText = mc.getCurrentFps() + " FPS";
         float statFontSize = 7.0F;
         float fpsWidth = Fonts.REGULAR.getFont(statFontSize).width(fpsText);
         totalWidth += pillGap + pillHorizontalPadding * 2.0F + fpsWidth;
      }

      if (this.showPing.isEnabled()) {
         String pingText = this.getPing() + " MS";
         float statFontSize = 7.0F;
         float pingWidth = Fonts.REGULAR.getFont(statFontSize).width(pingText);
         totalWidth += pillGap + pillHorizontalPadding * 2.0F + pingWidth;
      }

      this.width = totalWidth + padding;
      super.update(context);
   }

   @Override
   protected void renderComponent(UIContext context) {
      this.height = 20.0F;

      boolean dark = elegant.getInstance().getThemeManager().getCurrentTheme() == Theme.DARK;
      float g = Interface.glass();

      if (Interface.showMinimalizm()) {
         context.drawBlurredRect(
            this.x, this.y, this.width, this.height, 11.25F, 7.0F,
            BorderRadius.all(7.0F),
            ColorRGBA.WHITE.withAlpha(255.0F * Interface.minimalizm())
         );
      }

      if (Interface.showGlass()) {
         context.drawLiquidGlass(
            this.x, this.y, this.width, this.height, 7.0F, Interface.getDistortion(),
            BorderRadius.all(7.0F),
            Colors.getLiquidGlassColor().withAlpha(255.0F * Interface.glass())
         );
      }

      float padding = 6.0F;
      float pillGap = 4.0F;
      float pillRadius = 6.0F;
      float pillHorizontalPadding = 8.0F;
      float centerY = this.y + this.height / 2.0F;
      float x = this.x + padding;

      float mainA = 160.0F * (dark ? (0.8f - 0.6f * g) : 0.7f);
      context.drawRoundedRect(
         this.x,
         this.y,
         this.width,
         this.height,
         BorderRadius.all(7.0F),
         Colors.getBackgroundColor().withAlpha(mainA)
      );

      String clientName = Interface.showMinimalizm() ? "Slizen" : "SliznyakVisual";
      float nameFontSize = 7.5F;
      float nameWidth = Fonts.MEDIUM.getFont(nameFontSize).width(clientName);
      float namePillWidth = pillHorizontalPadding * 2.0F + nameWidth;

      float nameA = 210.0F * (dark ? (0.8f - 0.6f * g) : 0.7f);
      context.drawRoundedRect(
         x,
         this.y + 2.0F,
         namePillWidth,
         this.height - 4.0F,
         BorderRadius.all(pillRadius),
         Colors.getBackgroundColor().withAlpha(nameA)
      );

      context.drawText(
         Fonts.MEDIUM.getFont(nameFontSize),
         clientName,
         x + pillHorizontalPadding,
         centerY - Fonts.MEDIUM.getFont(nameFontSize).height() / 2.0F,
         Colors.getTextColor()
      );

      x += namePillWidth + pillGap;

      float pillA = 190.0F * (dark ? (0.8f - 0.6f * g) : 0.7f);

      if (this.showFps.isEnabled()) {
         int fps = mc.getCurrentFps();
         String fpsText = fps + " FPS";
         float statFontSize = 7.0F;
         float fpsWidth = Fonts.REGULAR.getFont(statFontSize).width(fpsText);
         float pillWidth = pillHorizontalPadding * 2.0F + fpsWidth;

         context.drawRoundedRect(
            x,
            this.y + 2.0F,
            pillWidth,
            this.height - 4.0F,
            BorderRadius.all(pillRadius),
            Colors.getBackgroundColor().withAlpha(pillA)
         );

         context.drawText(
            Fonts.REGULAR.getFont(statFontSize),
            fpsText,
            x + pillHorizontalPadding,
            centerY - Fonts.REGULAR.getFont(statFontSize).height() / 2.0F,
            Colors.getTextColor()
         );

         x += pillWidth + pillGap;
      }

      if (this.showPing.isEnabled()) {
         int ping = this.getPing();
         String pingText = ping + " MS";
         float statFontSize = 7.0F;
         float pingWidth = Fonts.REGULAR.getFont(statFontSize).width(pingText);
         float pillWidth = pillHorizontalPadding * 2.0F + pingWidth;

         context.drawRoundedRect(
            x,
            this.y + 2.0F,
            pillWidth,
            this.height - 4.0F,
            BorderRadius.all(pillRadius),
            Colors.getBackgroundColor().withAlpha(pillA)
         );

         context.drawText(
            Fonts.REGULAR.getFont(statFontSize),
            pingText,
            x + pillHorizontalPadding,
            centerY - Fonts.REGULAR.getFont(statFontSize).height() / 2.0F,
            this.getPingColor(ping)
         );
      }
   }

   private ColorRGBA getPingColor(int ping) {
      if (ping <= 45) return new ColorRGBA(80f, 255f, 80f, 255f);
      if (ping <= 90) return new ColorRGBA(255f, 220f, 60f, 255f);
      return new ColorRGBA(255f, 80f, 80f, 255f);
   }

   private int getPing() {
      if (mc.player != null && mc.getNetworkHandler() != null) {
         PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
         if (entry != null) return entry.getLatency();
      }
      return 0;
   }

   @Override
   public boolean show() {
      return Interface.showWatermark();
   }
}