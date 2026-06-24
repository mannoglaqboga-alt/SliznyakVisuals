package moscow.elegant.mixin.minecraft.client.gui.overlay;

import moscow.elegant.elegant;
import moscow.elegant.systems.modules.modules.visuals.Animations;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(PlayerListHud.class)
public abstract class PlayerListHudMixin {

   @Shadow
   @Final
   private MinecraftClient client;

   @Unique
   private long tabOpenTime = 0;

   @Unique
   private boolean wasTabOpen = false;

   @Unique
   private Animations getAnimations() {
      try {
         return elegant.getInstance().getModuleManager().getModule(Animations.class);
      } catch (Exception ignored) {
         return null;
      }
   }

   // Track when tab is opened for animation start
   @Inject(method = "render(Lnet/minecraft/client/gui/DrawContext;ILnet/minecraft/scoreboard/Scoreboard;Lnet/minecraft/scoreboard/ScoreboardObjective;)V", at = @At("HEAD"))
   private void onRenderTabStart(DrawContext context, int scaledWindowWidth, net.minecraft.scoreboard.Scoreboard scoreboard, net.minecraft.scoreboard.ScoreboardObjective objective, CallbackInfo ci) {
      Animations anims = getAnimations();
      if (anims == null || !anims.isTabAnimationEnabled()) return;

      boolean tabOpen = client.options.playerListKey.isPressed();
      if (tabOpen && !wasTabOpen) {
         tabOpenTime = System.currentTimeMillis();
      }
      wasTabOpen = tabOpen;
   }

   // Apply smooth scale/fade animation when tab appears
   @Inject(
      method = "render(Lnet/minecraft/client/gui/DrawContext;ILnet/minecraft/scoreboard/Scoreboard;Lnet/minecraft/scoreboard/ScoreboardObjective;)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;push()V")
   )
   private void applyTabAnimation(DrawContext context, int scaledWindowWidth, net.minecraft.scoreboard.Scoreboard scoreboard, net.minecraft.scoreboard.ScoreboardObjective objective, CallbackInfo ci) {
      Animations anims = getAnimations();
      if (anims == null || !anims.isTabAnimationEnabled()) return;

      float progress = getTabAnimProgress(anims);

      float scale = 0.7F + (progress * 0.3F);
      float alpha = progress;

      // Center scale animation
      int centerX = scaledWindowWidth / 2;
      context.getMatrices().push();
      context.getMatrices().translate(centerX, 0, 0);
      context.getMatrices().scale(scale, scale, 1.0F);
      context.getMatrices().translate(-centerX, 0, 0);

      // We can't easily apply alpha to the whole tab without more invasive changes,
      // but the scale already gives a nice "pop in" effect.
   }

   @Inject(
      method = "render(Lnet/minecraft/client/gui/DrawContext;ILnet/minecraft/scoreboard/Scoreboard;Lnet/minecraft/scoreboard/ScoreboardObjective;)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;pop()V", shift = At.Shift.BEFORE)
   )
   private void popTabAnimation(DrawContext context, int scaledWindowWidth, net.minecraft.scoreboard.Scoreboard scoreboard, net.minecraft.scoreboard.ScoreboardObjective objective, CallbackInfo ci) {
      Animations anims = getAnimations();
      if (anims != null && anims.isTabAnimationEnabled()) {
         context.getMatrices().pop();
      }
   }

   @Unique
   private float getTabAnimProgress(Animations anims) {
      if (!anims.isTabAnimationEnabled()) return 1.0F;

      long elapsed = System.currentTimeMillis() - tabOpenTime;
      float duration = 220F / Math.max(0.1F, anims.tabAnimSpeed.getCurrentValue());
      float p = Math.min(1.0F, elapsed / duration);
      // Ease out
      return (float) (1 - Math.pow(1 - p, 3));
   }

   // Optional: per-player entry animation (simple delay per row)
   // We can leave advanced per-entry for future if needed.
}
