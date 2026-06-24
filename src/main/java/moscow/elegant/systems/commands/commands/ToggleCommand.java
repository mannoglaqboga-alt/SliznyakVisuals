package moscow.elegant.systems.commands.commands;

import java.util.List;
import moscow.elegant.elegant;
import moscow.elegant.systems.commands.Command;
import moscow.elegant.systems.commands.CommandBuilder;
import moscow.elegant.systems.commands.ParameterBuilder;
import moscow.elegant.systems.localization.Localizator;
import moscow.elegant.systems.modules.Module;
import moscow.elegant.utility.game.MessageUtility;
import net.minecraft.text.Text;
import ru.kotopushka.compiler.sdk.annotations.Compile;

public class ToggleCommand {
   @Compile
   public Command command() {
      List<String> moduleNames = elegant.getInstance()
         .getModuleManager()
         .getModules()
         .stream()
         .filter(module -> !module.isHidden())
         .map(module -> module.getName().replace(" ", ""))
         .toList();
      return CommandBuilder.begin("toggle")
         .aliases("t")
         .desc("commands.toggle.description")
         .param("module", (ParameterBuilder<Module> p) -> p.validator(ParameterBuilder.MODULE).suggests(moduleNames))
         .handler(
            context -> {
               Module module = (Module)context.arguments().getFirst();
               module.toggle();
               MessageUtility.info(
                  Text.of(Localizator.translate("commands.toggle." + (module.isEnabled() ? "enabled" : "disabled"), module.getName()))
               );
            }
         )
         .build();
   }
}
