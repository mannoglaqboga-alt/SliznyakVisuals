package moscow.elegant.mixin.minecraft.client.gui.overlay;

import moscow.elegant.elegant;
import moscow.elegant.systems.modules.modules.visuals.Animations;
import moscow.elegant.systems.setting.settings.ModeSetting;
import moscow.elegant.utility.animation.base.Easing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin {

   @Shadow
   @Final
   private MinecraftClient client;

   @Shadow
   public abstract double getChatScale();

   @Shadow
   public abstract int getWidth();

   @Shadow
   private int scrolledLines;

   @Unique
   private float lastChatAnimProgress = 1.0F;

   // Track when a new message was added for animation
   @Inject(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V", at = @At("HEAD"))
   private void onAddMessage(Text message, MessageSignatureData signature, MessageIndicator indicator, CallbackInfo ci) {
      Animations anims = getAnimations();
      if (anims != null && anims.isChatAnimationEnabled()) {
         anims.onNewChatMessage();
      }
   }

   @Unique
   private Animations getAnimations() {
      try {
         return elegant.getInstance().getModuleManager().getModule(Animations.class);
      } catch (Exception e) {
         return null;
      }
   }

   // === Custom Chat Size / Length ===
   @ModifyVariable(method = "getWidth()I", at = @At("HEAD"), ordinal = 0)
   private int modifyChatWidth(int original) {
      Animations anims = getAnimations();
      if (anims != null && anims.isCustomChatEnabled()) {
         float mult = anims.chatWidth.getCurrentValue();
         return MathHelper.floor(original * mult);
      }
      return original;
   }

   @ModifyVariable(method = "getHeight()I", at = @At("HEAD"), ordinal = 0)
   private int modifyChatHeight(int original) {
      Animations anims = getAnimations();
      if (anims != null && anims.isCustomChatEnabled()) {
         // chatHeight setting represents max visible lines roughly
         return MathHelper.floor(anims.chatHeight.getCurrentValue() * 9); // rough pixels
      }
      return original;
   }

   @ModifyVariable(method = "getChatScale()D", at = @At("HEAD"), ordinal = 0)
   private double modifyChatScale(double original) {
      Animations anims = getAnimations();
      if (anims != null && anims.isCustomChatEnabled()) {
         return original * anims.chatScale.getCurrentValue();
      }
      return original;
   }

   // Increase visible line count (длинна чата)
   @Inject(method = "getVisibleLineCount()I", at = @At("HEAD"), cancellable = true)
   private void getVisibleLineCount(CallbackInfoReturnable<Integer> cir) {
      Animations anims = getAnimations();
      if (anims != null && anims.isCustomChatEnabled()) {
         float lines = anims.chatHeight.getCurrentValue();
         cir.setReturnValue(Math.max(1, MathHelper.floor(lines)));
      }
   }

   // === Chat Animation (slide + fade on new messages) ===
   @Inject(method = "render", at = @At("HEAD"))
   private void applyChatAnimations(DrawContext context, int currentTick, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      Animations anims = getAnimations();
      if (anims == null || !anims.isChatAnimationEnabled()) {
         lastChatAnimProgress = 1.0F;
         return;
      }

      float raw = anims.getChatAnimProgress();
      float eased = Easing.CUBIC_OUT.ease(raw, 0.0F, 1.0F, 1.0F);
      lastChatAnimProgress = eased;

      // Apply slide animation by translating the chat content a bit
      float offsetY = 0;
      ModeSetting.Value current = anims.chatAnimStyle.getValue();
      if (current == anims.slide || current == anims.slideFade) {
         offsetY = (1.0F - lastChatAnimProgress) * 18.0F;
      }

      if (offsetY != 0) {
         context.getMatrices().push();
         context.getMatrices().translate(0, offsetY, 0);
      }
   }

   @Inject(method = "render", at = @At("RETURN"))
   private void popChatAnimations(DrawContext context, int currentTick, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      Animations anims = getAnimations();
      if (anims != null && anims.isChatAnimationEnabled() && lastChatAnimProgress < 1.0F) {
         try {
            context.getMatrices().pop();
         } catch (Exception ignored) {}
      }
   }

   // Simple helpers (used if someone wants more control)
   @Unique
   public float getChatAnimProgressCached() {
      return lastChatAnimProgress;
   }

   @Unique
   public float getCustomChatOpacity() {
      Animations anims = getAnimations();
      if (anims != null && anims.isCustomChatEnabled()) {
         return anims.chatOpacity.getCurrentValue();
      }
      return 1.0F;
   }
}
