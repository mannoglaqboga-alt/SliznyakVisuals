package moscow.elegant.mixin.minecraft.client;

import moscow.elegant.elegant;
import moscow.elegant.protection.client.MinecraftClientMixinProtection;
import moscow.elegant.systems.event.impl.game.GameTickEvent;
import moscow.elegant.utility.render.penis.PenisAtlas;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({MinecraftClient.class})
public class MinecraftClientMixin {
   @Shadow
   private int itemUseCooldown;

   @Inject(
      method = {"tick()V"},
      at = {@At("HEAD")}
   )
   public void tick(CallbackInfo ci) {
      elegant.getInstance().getEventManager().triggerEvent(new GameTickEvent());
   }

   @Inject(
      method = {"<init>(Lnet/minecraft/client/RunArgs;)V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/MinecraftClient;onResolutionChanged()V"
      )}
   )
   public void initializeClient(RunArgs args, CallbackInfo ci) {
      MinecraftClientMixinProtection.init();
   }

   @Inject(
      method = {"<init>(Lnet/minecraft/client/RunArgs;)V"},
      at = {@At("RETURN")}
   )
   public void endInitialize(RunArgs args, CallbackInfo ci) {
      try {
         PenisAtlas atlas = PenisAtlas.getOrCreateAtlasFor(16, 16);
         atlas.registerAnimationFromPenisFile(elegant.id("penises/combat.penis"));
         atlas.registerAnimationFromPenisFile(elegant.id("penises/movement.penis"));
         atlas.registerAnimationFromPenisFile(elegant.id("penises/visuals.penis"));
         atlas.registerAnimationFromPenisFile(elegant.id("penises/player.penis"));
         atlas.registerAnimationFromPenisFile(elegant.id("penises/other.penis"));
         atlas.registerAnimationFromPenisFile(elegant.id("penises/search.penis"));
         atlas.buildAtlas();
         PenisAtlas atlas12 = PenisAtlas.getOrCreateAtlasFor(12, 12);
         atlas12.registerAnimationFromPenisFile(elegant.id("penises/check_enable.penis"));
         atlas12.registerAnimationFromPenisFile(elegant.id("penises/check_disable.penis"));
         atlas12.buildAtlas();
      } catch (Exception var5) {
         System.err.println("Ошибка при загрузке анимаций: " + var5.getMessage());
         var5.printStackTrace();
      }
   }

   @Inject(
      method = {"stop()V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/MinecraftClient;close()V",
         shift = Shift.AFTER
      )}
   )
   public void shutdownClient(CallbackInfo ci) {
      MinecraftClientMixinProtection.shutdown();
   }

   @Inject(
      method = {"getWindowTitle()Ljava/lang/String;"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void changeWindowTitle(CallbackInfoReturnable<String> cir) {
      MinecraftClientMixinProtection.updateTitle(cir);
   }
}
