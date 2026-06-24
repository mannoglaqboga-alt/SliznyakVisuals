package moscow.elegant.systems.friends;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import moscow.elegant.elegant;
import moscow.elegant.systems.localization.Localizator;
import moscow.elegant.utility.game.EntityUtility;
import moscow.elegant.utility.game.MessageUtility;
import moscow.elegant.utility.interfaces.IMinecraft;
import net.minecraft.text.Text;
import ru.kotopushka.compiler.sdk.annotations.Compile;

public class FriendManager implements IMinecraft {
   private final List<String> friends = new ArrayList<>();

   public void add(String name) {
      if (name == null || name.trim().isEmpty()) {
         MessageUtility.error(Text.of("Укажи никнейм"));
         return;
      }
      if (elegant.getInstance().getTargetManager().getTarget().contains(name)) {
         MessageUtility.error(Text.of(Localizator.translate("commands.friends.target")));
      } else if (this.friends.contains(name)) {
         MessageUtility.info(Text.of(Localizator.translate("commands.friends.exists", name)));
      } else if (name.equalsIgnoreCase(mc.getSession().getUsername())) {
         MessageUtility.error(Text.of(Localizator.translate("commands.friends.self")));
      } else {
         this.friends.add(name);
         MessageUtility.info(Text.of(Localizator.translate("commands.friends.added", name)));
         if (EntityUtility.isInGame()) {
            elegant.getInstance().getFileManager().writeFile("client");
         }
      }
   }

   public void remove(String name) {
      if (name == null || name.trim().isEmpty()) {
         MessageUtility.error(Text.of("Укажи никнейм"));
         return;
      }
      if (this.friends.contains(name)) {
         this.friends.remove(name);
         MessageUtility.info(Text.of(Localizator.translate("commands.friends.removed", name)));
      } else {
         MessageUtility.info(Text.of(Localizator.translate("commands.friends.not_exists", name)));
      }

      elegant.getInstance().getFileManager().writeFile("client");
   }

   @Compile
   public void clear() {
      if (this.friends.isEmpty()) {
         MessageUtility.error(Text.of(Localizator.translate("commands.friends.empty")));
      } else {
         this.friends.clear();
         MessageUtility.info(Text.of("Список друзей успешно очищен!"));
         elegant.getInstance().getFileManager().writeFile("client");
      }
   }

   public List<String> listFriends() {
      return Collections.unmodifiableList(this.friends);
   }

   public boolean isFriend(String name) {
      return this.friends.contains(name);
   }
}
