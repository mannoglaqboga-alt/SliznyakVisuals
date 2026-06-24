package moscow.elegant.ui.hud.impl;

import moscow.elegant.elegant;
import moscow.elegant.framework.base.UIContext;
import moscow.elegant.framework.msdf.Font;
import moscow.elegant.framework.msdf.Fonts;
import moscow.elegant.framework.objects.BorderRadius;
import moscow.elegant.framework.objects.MouseButton;
import moscow.elegant.systems.modules.modules.visuals.Interface;
import moscow.elegant.systems.setting.settings.BooleanSetting;
import moscow.elegant.systems.setting.settings.ModeSetting;
import moscow.elegant.systems.theme.Theme;
import moscow.elegant.ui.hud.HudElement;
import moscow.elegant.utility.animation.base.Animation;
import moscow.elegant.utility.animation.base.Easing;
import moscow.elegant.utility.colors.ColorRGBA;
import moscow.elegant.utility.colors.Colors;
import moscow.elegant.utility.game.EntityUtility;
import moscow.elegant.utility.game.TextUtility;
import moscow.elegant.utility.gui.GuiUtility;
import moscow.elegant.utility.math.MathUtility;
import moscow.elegant.utility.time.Timer;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.StreamSupport;

public class TargetHud extends HudElement {

   private final Animation animation = new Animation(200L, 0.0F, Easing.BAKEK);
   private final Timer stopWatch = new Timer();
   private LivingEntity lastTarget;
   private Item lastItem = Items.AIR;

   private float healthBar;
   private float absorptionWidthSmooth;
   private float lastTotalHp = 0.0F;
   private final List<HealingText> healingTexts = new ArrayList<>();

   // Using item slide-in animation
   private final Animation usingAnim = new Animation(200L, 0.0F, Easing.BAKEK);

   private final Animation contentAnim = new Animation(300L, 0.0F, Easing.BAKEK_SIZE);
   private final Animation healthAnim = new Animation(300L, 0.0F, Easing.BAKEK);
   private final Animation goldenAnim = new Animation(300L, 0.0F, Easing.BAKEK);
   private final Animation numberAnim = new Animation(300L, 0.0F, Easing.FIGMA_EASE_IN_OUT);
   private final Animation hurtAnim = new Animation(250L, 0.0F, Easing.BAKEK);

   public TargetHud() {
      super("hud.targethud", "icons/hud/target.png");
   }

   @Override
   public void update(UIContext context) {
      // Adaptive width (longer in length), narrow height
      float baseWidth = 140.0F;
      if (lastTarget != null) {
         String name = lastTarget.getName().getString();
         Font nameFont = Fonts.MEDIUM.getFont(7.5F);
         float nameW = nameFont.width(name);
         baseWidth = Math.max(120.0F, 28F + 20F + nameW + 52F);
      }
      this.width = Math.min(135.0F, baseWidth);  // еще уже по ширине
      this.height = 34.0F;   // чтобы armor не залазил на HP и бар

      // Healing detection
      if (lastTarget != null) {
         float currentTotalHp = getTotalHp(lastTarget);
         if (currentTotalHp > lastTotalHp + 0.05f && lastTotalHp != 0.0F) {
            float healed = currentTotalHp - lastTotalHp;
            if (healed > 0.01F) {
               String healStr = "+" + formatHp(healed);
               healingTexts.add(new HealingText(healStr));
            }
         }
         lastTotalHp = currentTotalHp;
      } else {
         lastTotalHp = 0.0F;
         healingTexts.clear();
      }

      super.update(context);
   }

   private float getTotalHp(LivingEntity entity) {
      float base = entity instanceof PlayerEntity p ? EntityUtility.getHealth(p) : entity.getHealth();
      return base + entity.getAbsorptionAmount();
   }

   private String formatHp(float hp) {
      return TextUtility.formatNumber(hp).replace(",", ".");
   }

   @Override
   protected void renderComponent(UIContext context) {
      LivingEntity target = getTarget();
      if (target != null) {
         lastTarget = target;
         animation.setDirection(true); // forwards
      } else if (mc.currentScreen instanceof ChatScreen) {
         lastTarget = mc.player;
         animation.setDirection(true);
      } else if (stopWatch.finished(500)) {
         animation.setDirection(false); // backwards
      }

      if (lastTarget == null) return;

      // Update main animation
      boolean showNow = lastTarget != null || mc.currentScreen instanceof ChatScreen;
      animation.update(showNow ? 1.0F : 0.0F);

      // Update using item anim
      boolean using = lastTarget.isUsingItem();
      usingAnim.setDirection(using ? true : false);
      usingAnim.update(using ? 1.0F : 0.0F);
      if (using && !lastTarget.getActiveItem().isEmpty()) {
         lastItem = lastTarget.getActiveItem().getItem();
      }

      // Update health numbers
      float totalHp = getTotalHp(lastTarget);
      float baseHp = lastTarget.getHealth();
      float absorption = lastTarget.getAbsorptionAmount();
      float maxHp = lastTarget.getMaxHealth();

      healthAnim.update(maxHp <= 0 ? 0 : totalHp / (maxHp + (absorption > 0 ? 20 : 0)));
      numberAnim.update(totalHp);

      // Hurt
      hurtAnim.update(lastTarget.hurtTime > 0 ? 1.0F : 0.0F);

      // Golden apple
      boolean eatingGolden = lastTarget.isUsingItem();
      if (eatingGolden) {
         Item active = lastTarget.getActiveItem().getItem();
         eatingGolden = active == Items.GOLDEN_APPLE || active == Items.ENCHANTED_GOLDEN_APPLE;
      }
      goldenAnim.update(eatingGolden ? 1.0F : 0.0F);

      if (animation.getValue() < 0.01F) return;

      float alpha = animation.getValue();

      boolean dark = elegant.getInstance().getThemeManager().getCurrentTheme() == Theme.DARK;
      float g = Interface.glass();

      // Background
      context.drawShadow(
         x - 4, y - 4, width + 8, height + 8,
         12.0F, BorderRadius.all(7.0F),
         ColorRGBA.BLACK.withAlpha(55.0F * alpha)
      );

      if (Interface.showMinimalizm()) {
         context.drawBlurredRect(
            x, y, width, height, 10.0F, 7.0F,
            BorderRadius.all(7.0F),
            ColorRGBA.WHITE.withAlpha(255.0F * alpha * Interface.minimalizm())
         );
      }

      if (Interface.showGlass()) {
         context.drawLiquidGlass(
            x, y, width, height,
            7.0F,
            Interface.getDistortion() - 0.06F,
            BorderRadius.all(7.0F),
            Colors.getLiquidGlassColor().withAlpha(255.0F * alpha * Interface.glass())
         );
      }

      ColorRGBA bg = Colors.getBackgroundColor().withAlpha(255.0F * (dark ? 0.8F - 0.6F * g : 0.7F) * alpha);
      context.drawSquircle(x, y, width, height, 7.0F, BorderRadius.all(7.0F), bg);

      // Vertical divider
      float dividerX = x + 27.0F;
      context.drawRoundedRect(
         dividerX, y + 2.5F, 0.7F, height - 5.0F,
         BorderRadius.ZERO,
         Colors.getOutlineColor().withAlpha(160 * alpha)
      );

      // Horizontal divider under name area
      context.drawRoundedRect(
         x + 28, y + 11.5F, width - 34, 0.5F,
         BorderRadius.ZERO,
         Colors.getOutlineColor().withAlpha(120 * alpha)
      );

      // === HEAD + HURT GLOW ===
      float headX = x + 4.0F;
      float headY = y + 4.0F;
      float headSize = 22.0F;

      context.drawShadow(
         headX - 1, headY - 1, headSize + 2, headSize + 2,
         5.0F, BorderRadius.all(5.0F),
         ColorRGBA.BLACK.withAlpha(60 * alpha)
      );

      if (lastTarget instanceof AbstractClientPlayerEntity player) {
         context.drawHead(player, headX, headY, headSize, BorderRadius.all(4.0F), ColorRGBA.WHITE.withAlpha(255 * alpha));
      } else {
         boolean isDark = elegant.getInstance().getThemeManager().getCurrentTheme() == Theme.DARK;
         context.drawRoundedTexture(
            elegant.id(isDark ? "icons/hud/whodark.png" : "icons/hud/who.png"),
            headX, headY, headSize, headSize,
            BorderRadius.all(4.0F),
            ColorRGBA.WHITE.withAlpha(255 * alpha)
         );
      }

      // Red hurt glow on head (semi-transparent)
      if (hurtAnim.getValue() > 0.05F) {
         ColorRGBA red = new ColorRGBA(230, 35, 35, 80 * hurtAnim.getValue() * alpha);
         context.drawRoundedRect(headX, headY, headSize, headSize, BorderRadius.all(4.0F), red);
      }

      // === CONTENT ===
      float contentX = x + 28.0F;
      float nameY = y + 4.0F;
      float hpY = y + 13.0F;

      Font nameFont = Fonts.MEDIUM.getFont(8.0F);
      Font smallFont = Fonts.MEDIUM.getFont(7.0F);

      String name = lastTarget.getName().getString();
      float nameWidth = nameFont.width(name);

      if (nameWidth > width - 75) {
         // Scissor for long names
         context.enableScissor((int)contentX, (int)y, (int)(width - 42), (int)height);
         context.drawFadeoutText(
            nameFont, name, contentX + 1, nameY,
            Colors.getTextColor().withAlpha(255 * alpha),
            0.6F, 0.95F, width - 80
         );
         context.disableScissor();
      } else {
         context.drawText(nameFont, name, contentX + 1, nameY, Colors.getTextColor().withAlpha(255 * alpha));
      }

      // HP text with color
      String mainHp = formatHp(totalHp) + " hp";
      ColorRGBA hpCol = baseHp >= 10 ? new ColorRGBA(85, 255, 85) : new ColorRGBA(255, 85, 85);
      context.drawText(smallFont, mainHp, contentX + 1, hpY, hpCol);

      // Absorption text
      if (absorption > 0.01F) {
         String absStr = " + " + formatHp(absorption) + " hp";
         float absX = contentX + 2 + smallFont.width(mainHp);
         context.drawText(smallFont, absStr, absX, hpY, new ColorRGBA(255, 220, 60));
      }

      // === HEALTH + ABSORPTION BARS ===
      float barX = contentX + 1;
      float barY = y + 22.0F;
      float barWidth = width - 33;
      float barH = 2.3F;

      // Absorption bar
      if (absorption > 0 || absorptionWidthSmooth > 0.3F) {
         float targetAbs = MathHelper.clamp((absorption / (maxHp + 20)) * barWidth, 0, barWidth);
         absorptionWidthSmooth = MathUtility.interpolate(absorptionWidthSmooth, targetAbs, 0.12);
         context.drawRoundedRect(
            barX, barY - 0.9F, absorptionWidthSmooth, 1.5F,
            BorderRadius.all(0.6F),
            new ColorRGBA(255, 215, 60, 210 * alpha)
         );
      }

      // Health bar bg
      context.drawRoundedRect(
         barX, barY, barWidth, barH,
         BorderRadius.all(0.8F),
         new ColorRGBA(8, 8, 18, 180 * alpha)
      );

      // Health fill
      float healthW = barWidth * Math.clamp(healthAnim.getValue(), 0f, 1f);
      context.drawRoundedRect(
         barX, barY, healthW, barH,
         BorderRadius.all(0.8F),
         Colors.getAccentColor().withAlpha(255 * alpha)
      );

      // Golden stripe when eating gapple
      if (goldenAnim.getValue() > 0.05F) {
         ColorRGBA gold = new ColorRGBA(255, 195, 50, 175 * goldenAnim.getValue() * alpha);
         float gW = Math.min(barWidth, barWidth * 0.32F + healthW * 0.12F);
         context.drawRoundedRect(barX, barY, gW, barH, BorderRadius.all(0.8F), gold);
      }

      // === HEALING TEXTS (+X hp) ===
      float healX = contentX + smallFont.width(mainHp) + (absorption > 0 ? 32 : 4);
      Iterator<HealingText> it = healingTexts.iterator();
      while (it.hasNext()) {
         HealingText ht = it.next();
         ht.anim.update(1.0F); // drive animation forward
         if (ht.isFinished()) {
            it.remove();
            continue;
         }
         float progress = ht.getValue();
         float offY = 5.5F * progress;
         int a = (int) (255 * (1f - progress) * alpha);
         context.drawText(
            smallFont,
            ht.text,
            healX, hpY - offY,
            new ColorRGBA(70, 255, 110, a)
         );
         healX += smallFont.width(ht.text) + 3;
      }

      // === ARMOR ROW AT BOTTOM (tight, no overlap with hp) ===
      List<ItemStack> armorItems = StreamSupport.stream(lastTarget.getEquippedItems().spliterator(), false)
         .filter(s -> !s.isEmpty())
         .toList();

      if (!armorItems.isEmpty()) {
         float armorSpacing = 9.5F;
         float armorStartX = x + (width - armorItems.size() * armorSpacing) / 2f;
         float armorY = y + height - 9.0F;

         // tiny bg for armor
         context.drawRoundedRect(
            armorStartX - 2, armorY - 1,
            armorItems.size() * armorSpacing + 4, 8.0F,
            BorderRadius.all(3.0F),
            Colors.getBackgroundColor().withAlpha(70 * alpha)
         );

         float ix = armorStartX;
         for (ItemStack stack : armorItems) {
            context.drawItem(stack, ix, armorY, 0.42F);
            ix += armorSpacing;
         }
      }

      // === USING ITEM SIDE PANEL (left slide) ===
      float usingProgress = usingAnim.getValue();

      if (usingProgress > 0.02F && lastItem != Items.AIR) {
         float panelW = 18;
         float panelH = 18;
         float panelX = x - panelW * usingProgress + 1;
         float panelY = y + 5.0F;

         // Glass panel (smaller for narrow hud)
         if (Interface.showGlass()) {
            context.drawLiquidGlass(
               panelX, panelY, panelW, panelH,
               9.0F, 0.04F, BorderRadius.all(9.0F),
               Colors.getLiquidGlassColor().withAlpha(200 * usingProgress)
            );
         } else {
            context.drawRoundedRect(
               panelX, panelY, panelW, panelH,
               BorderRadius.all(9.0F),
               new ColorRGBA(30, 30, 36, 190 * usingProgress)
            );
         }

         // Draw item
         float itemScale = 0.6F;
         context.drawItem(lastItem.getDefaultStack(), panelX + 3.0F, panelY + 3.0F, itemScale);

         // Simple progress indicator (arc approximation using small segments)
         float useTime = lastTarget.getItemUseTime() + mc.getRenderTickCounter().getTickDelta(false);
         float maxUse = 32; // reasonable default
         try {
            maxUse = lastItem.getMaxUseTime(lastTarget.getActiveItem(), lastTarget);
         } catch (Exception ignored) {}
         if (maxUse <= 0) maxUse = 32;

         float prog = MathHelper.clamp(useTime / maxUse * 360f, 0, 360);

         // Draw progress ring (simple 16 segment approach)
         float cx = panelX + panelW / 2f;
         float cy = panelY + panelH / 2f;
         float radius = 7.5f;
         int segments = 14;
         ColorRGBA ringCol = Colors.getAccentColor().withAlpha(220 * usingProgress);

         for (int i = 0; i < segments; i++) {
            float a1 = (float) (i * 360.0 / segments - 90);
            float a2 = (float) ((i + 1) * 360.0 / segments - 90);
            if (a2 > prog) break;

            float rad1 = (float) Math.toRadians(a1);
            float rad2 = (float) Math.toRadians(a2);

            float x1 = cx + (float) Math.cos(rad1) * radius;
            float y1 = cy + (float) Math.sin(rad1) * radius;
            float x2 = cx + (float) Math.cos(rad2) * radius;
            float y2 = cy + (float) Math.sin(rad2) * radius;

            // small rect segments for ring (smaller panel)
            context.drawRoundedRect(
               Math.min(x1, x2) - 0.6f,
               Math.min(y1, y2) - 0.6f,
               Math.abs(x2 - x1) + 1.2f,
               Math.abs(y2 - y1) + 1.2f,
               BorderRadius.all(0.5f),
               ringCol
            );
         }
      }
   }

   private LivingEntity getTarget() {
      if (mc.world == null) return null;
      Object current = elegant.getInstance().getTargetManager().getCurrentTarget();
      LivingEntity main = current instanceof LivingEntity ? (LivingEntity) current : null;

      if (main != null) {
         stopWatch.reset();
         return main;
      } else if (mc.targetedEntity instanceof LivingEntity e) {
         stopWatch.reset();
         return e;
      } else if (lastTarget != null && !stopWatch.finished(2500)) {
         return lastTarget;
      } else {
         return (mc.currentScreen instanceof ChatScreen) ? mc.player : null;
      }
   }

   @Override
   public boolean show() {
      if (!Interface.showTargetHud()) return false;
      if (mc.world == null) return false; // avoid rendering on title screen / early load
      LivingEntity t = getTarget();
      return t != null && !t.isInvisible();
   }

   // Inner healing text (floating + fading)
   private static class HealingText {
      public final String text;
      private final Animation anim;

      public HealingText(String text) {
         this.text = text;
         this.anim = new Animation(650L, 0.0F, Easing.FIGMA_EASE_IN_OUT);
         this.anim.setValue(0f);
         this.anim.setDirection(true); // forwards
      }

      public float getValue() {
         return anim.getValue();
      }

      public boolean isFinished() {
         return anim.isDone();
      }
   }
}
