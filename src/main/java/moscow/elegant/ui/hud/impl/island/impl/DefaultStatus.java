package moscow.elegant.ui.hud.impl.island.impl;

import moscow.elegant.elegant;
import moscow.elegant.framework.base.CustomDrawContext;
import moscow.elegant.framework.msdf.Fonts;
import moscow.elegant.framework.objects.BorderRadius;
import moscow.elegant.systems.setting.settings.SelectSetting;
import moscow.elegant.ui.hud.impl.island.DynamicIsland;
import moscow.elegant.ui.hud.impl.island.IslandStatus;
import moscow.elegant.utility.colors.Colors;
import moscow.elegant.utility.time.Timer;

public class DefaultStatus extends IslandStatus {
   private final Timer promoTimer = new Timer();
   private boolean promoActive;
   private long promoEndTime;
   private final String promoFullText = "наш телеграмм канал: @SliznyakVisual";
   private String currentTypedText = "";
   private long lastCharTime;
   private boolean erasing;
   private final long PROMO_INTERVAL_MS = 120000;
   private final long PROMO_DURATION_MS = 10000;
   private final long CHAR_DELAY_MS = 70;
   private final long HOLD_AFTER_FULL_MS = 2000;

   public DefaultStatus(SelectSetting setting) {
      super(setting, "default");
   }

   @Override
   public void draw(CustomDrawContext context) {
      DynamicIsland island = elegant.getInstance().getHud().getIsland();
      float x = sr.getScaledWidth() / 2.0F - island.getSize().width / 2.0F;
      float y = 7.0F;

      long now = System.currentTimeMillis();

      if (!this.promoActive && this.promoTimer.finished(this.PROMO_INTERVAL_MS)) {
         this.promoActive = true;
         this.promoEndTime = now + this.PROMO_DURATION_MS;
         this.currentTypedText = "";
         this.erasing = false;
         this.lastCharTime = now;
         this.promoTimer.reset();
      }

      if (this.promoActive) {
         if (now >= this.promoEndTime) {
            this.promoActive = false;
            this.currentTypedText = "";
         } else {
            if (!this.erasing && this.currentTypedText.length() < this.promoFullText.length()) {
               if (now - this.lastCharTime >= this.CHAR_DELAY_MS) {
                  this.currentTypedText = this.promoFullText.substring(0, this.currentTypedText.length() + 1);
                  this.lastCharTime = now;
               }
            } else if (!this.erasing && this.currentTypedText.length() == this.promoFullText.length()) {
               if (now - this.lastCharTime >= this.HOLD_AFTER_FULL_MS) {
                  this.erasing = true;
                  this.lastCharTime = now;
               }
            } else if (this.erasing && !this.currentTypedText.isEmpty()) {
               if (now - this.lastCharTime >= this.CHAR_DELAY_MS) {
                  this.currentTypedText = this.promoFullText.substring(0, this.currentTypedText.length() - 1);
                  this.lastCharTime = now;
               }
            } else if (this.erasing && this.currentTypedText.isEmpty()) {
               this.erasing = false;
               this.lastCharTime = now;
            }
         }
      }

      String text = this.promoActive && !this.currentTypedText.isEmpty() ? this.currentTypedText : "SliznyakVisual";
      float width = this.size.width = 20.0F + Fonts.MEDIUM.getFont(7.0F).width(text);
      float height = this.size.height = 15.0F;
      context.drawRoundedRect(x - 6.0F + 10.0F * this.animation.getValue(), y + 4.0F, 7.0F, 7.0F, BorderRadius.all(3.0F), Colors.getAccentColor());
      context.drawText(Fonts.MEDIUM.getFont(7.0F), text, x + 25.0F - 10.0F * this.animation.getValue(), y + 5.0F, Colors.getTextColor());
   }

   @Override
   public boolean canShow() {
      return true;
   }
}
