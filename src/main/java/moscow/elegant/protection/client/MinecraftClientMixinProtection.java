package moscow.elegant.protection.client;

import moscow.elegant.elegant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.kotopushka.compiler.sdk.annotations.VMProtect;
import ru.kotopushka.compiler.sdk.enums.VMProtectType;

public class MinecraftClientMixinProtection {
   @VMProtect(
      type = VMProtectType.MUTATION
   )
   public static void init() {
      AuthManager.initialize();
      elegant.INSTANCE.initialize();
   }

   @VMProtect(
      type = VMProtectType.MUTATION
   )
   public static void shutdown() {
      AuthManager.shutdown();
      elegant.INSTANCE.shutdown();
   }

   public static void updateTitle(CallbackInfoReturnable<String> cir) {
      if (!elegant.INSTANCE.isPanic()) {
         String title = "%s %s (%s)".formatted("SliznyakVisual", "2.0.6", "by 126id");
         cir.setReturnValue(title);
      }
   }
}
