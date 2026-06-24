package moscow.elegant.systems.modules.modules.visuals;

import moscow.elegant.systems.modules.api.ModuleCategory;
import moscow.elegant.systems.modules.api.ModuleInfo;
import moscow.elegant.systems.modules.impl.BaseModule;
import moscow.elegant.systems.setting.settings.BooleanSetting;
import moscow.elegant.systems.setting.settings.ModeSetting;
import moscow.elegant.systems.setting.settings.SliderSetting;

@ModuleInfo(
   name = "Animations",
   category = ModuleCategory.VISUALS,
   desc = "Анимации и кастомизация чата и таба"
)
public class Animations extends BaseModule {

   // Chat Animation
   public final BooleanSetting chatAnimation = new BooleanSetting(this, "modules.settings.animations.chat_animation").enable();
   public final ModeSetting chatAnimStyle = new ModeSetting(this, "modules.settings.animations.chat_style");
   public ModeSetting.Value slide;
   public ModeSetting.Value fade;
   public ModeSetting.Value slideFade;
   public final SliderSetting chatAnimSpeed = new SliderSetting(this, "modules.settings.animations.chat_speed")
      .min(0.1F).max(3.0F).step(0.1F).currentValue(1.0F);

   // Tab Animation
   public final BooleanSetting tabAnimation = new BooleanSetting(this, "modules.settings.animations.tab_animation").enable();
   public final SliderSetting tabAnimSpeed = new SliderSetting(this, "modules.settings.animations.tab_speed")
      .min(0.1F).max(3.0F).step(0.1F).currentValue(1.0F);

   // Custom Chat
   public final BooleanSetting customChat = new BooleanSetting(this, "modules.settings.animations.custom_chat").enable();
   public final SliderSetting chatWidth = new SliderSetting(this, "modules.settings.animations.chat_width")
      .min(0.4F).max(2.0F).step(0.05F).currentValue(1.0F);
   public final SliderSetting chatHeight = new SliderSetting(this, "modules.settings.animations.chat_height")
      .min(5F).max(200F).step(1F).currentValue(40F);
   public final SliderSetting chatScale = new SliderSetting(this, "modules.settings.animations.chat_scale")
      .min(0.5F).max(2.0F).step(0.05F).currentValue(1.0F);
   public final SliderSetting chatOpacity = new SliderSetting(this, "modules.settings.animations.chat_opacity")
      .min(0.0F).max(1.0F).step(0.05F).currentValue(1.0F);

   // For animation timing (used by mixin)
   private long lastChatMessageTime = 0;

   public Animations() {
      // Initialize chat animation style values
      this.slide = new ModeSetting.Value(this.chatAnimStyle, "modules.settings.animations.chat_style.slide").select();
      this.fade = new ModeSetting.Value(this.chatAnimStyle, "modules.settings.animations.chat_style.fade");
      this.slideFade = new ModeSetting.Value(this.chatAnimStyle, "modules.settings.animations.chat_style.slide_fade");
   }

   public void onNewChatMessage() {
      this.lastChatMessageTime = System.currentTimeMillis();
   }

   public long getLastChatMessageTime() {
      return lastChatMessageTime;
   }

   public float getChatAnimProgress() {
      if (!this.isEnabled() || !chatAnimation.isEnabled()) return 1.0F;

      long elapsed = System.currentTimeMillis() - lastChatMessageTime;
      float duration = 280F / chatAnimSpeed.getCurrentValue();
      float progress = Math.min(1.0F, elapsed / duration);
      return progress;
   }

   // Getters for mixin
   public boolean isChatAnimationEnabled() {
      return this.isEnabled() && chatAnimation.isEnabled();
   }

   public boolean isTabAnimationEnabled() {
      return this.isEnabled() && tabAnimation.isEnabled();
   }

   public boolean isCustomChatEnabled() {
      return this.isEnabled() && customChat.isEnabled();
   }
}
