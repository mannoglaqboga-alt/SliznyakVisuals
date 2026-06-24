package moscow.elegant.systems.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import lombok.Generated;
import moscow.elegant.elegant;
import moscow.elegant.systems.file.FileManager;
import moscow.elegant.systems.localization.Localizator;
import moscow.elegant.systems.modules.Module;
import moscow.elegant.systems.modules.exception.UnknownModuleException;
import moscow.elegant.systems.modules.modules.other.Sounds;
import moscow.elegant.systems.modules.modules.visuals.MenuModule;
import moscow.elegant.systems.notifications.NotificationType;
import moscow.elegant.systems.setting.Setting;
import moscow.elegant.ui.hud.HudElement;
import moscow.elegant.utility.game.MessageUtility;
import moscow.elegant.utility.interfaces.IMinecraft;
import moscow.elegant.utility.sounds.ClientSounds;
import net.minecraft.text.Text;

public class ConfigFile implements IMinecraft {
   private List<Module> modules = elegant.getInstance().getModuleManager().getModules();
   private File file;
   private String fileName;

   public ConfigFile(String fileName) {
      this.fileName = fileName;
      File configsFolder = new File(FileManager.DIRECTORY, "configs");
      if (!configsFolder.exists()) {
         configsFolder.mkdir();
      }

      this.file = new File(configsFolder, fileName + ".%s".formatted("myth"));
   }

   public void load() {
      if (!this.file.exists()) {
         elegant.LOGGER.warn("Config file not found: {}", this.file.getAbsolutePath());
      } else {
         try {
            try (BufferedReader reader = new BufferedReader(new FileReader(this.file))) {
               JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();

               // Load HUD elements (positions, visibility etc.) - do this even for older formats
               if (jsonObject.has("hudElements")) {
                  for (JsonElement elemObj : jsonObject.getAsJsonArray("hudElements")) {
                     JsonObject elementObject = elemObj.getAsJsonObject();
                     if (!elementObject.has("name")) continue;
                     String name = elementObject.get("name").getAsString();
                     float x = elementObject.get("x").getAsFloat();
                     float y = elementObject.get("y").getAsFloat();
                     boolean showing = elementObject.get("showing").getAsBoolean();
                     HudElement element = elegant.getInstance().getHud().getElementByName(name);
                     if (element != null) {
                        element.setX(x);
                        element.setY(y);
                        element.setShowing(showing);
                        if (elementObject.has("settings")) {
                           JsonObject settingsObject = elementObject.getAsJsonObject("settings");
                           for (Setting setting : element.getSettings()) {
                              if (settingsObject.has(setting.getName())) {
                                 setting.load(settingsObject.get(setting.getName()));
                              }
                           }
                        }
                     }
                  }
               }

               if (!jsonObject.has("modules")) {
                  elegant.LOGGER.warn("Invalid config format: missing 'modules' array in {}", this.fileName);
                  return;
               }

               JsonArray modulesArray = jsonObject.getAsJsonArray("modules");
               int loadedModules = 0;

               for (JsonElement moduleElement : modulesArray) {
                  JsonObject moduleObject = moduleElement.getAsJsonObject();
                  if (moduleObject.has("name")) {
                     String moduleName = moduleObject.get("name").getAsString();
                     boolean enabled = moduleObject.has("enabled") && moduleObject.get("enabled").getAsBoolean();
                     int key = moduleObject.has("key") ? moduleObject.get("key").getAsInt() : 0;

                     try {
                        Module module = elegant.getInstance().getModuleManager().getModule(moduleName);
                        if (!(module instanceof MenuModule)) {
                           module.setEnabled(enabled, true);
                           module.setKey(key);
                        }

                        if (moduleObject.has("settings")) {
                           JsonObject settingsObject = moduleObject.getAsJsonObject("settings");

                           for (Setting setting : module.getSettings()) {
                              if (settingsObject.has(setting.getName())) {
                                 setting.load(settingsObject.get(setting.getName()));
                              }
                           }
                        }

                        loadedModules++;
                     } catch (UnknownModuleException var16) {
                     }
                  }
               }

               ClientSounds.MODULE.play(elegant.getInstance().getModuleManager().getModule(Sounds.class).getVolume().getCurrentValue(), 1.0F);
               elegant.getInstance().getNotificationManager().addNotification(NotificationType.SUCCESS, Localizator.translate("configs.loaded"));
               elegant.LOGGER.info("Loaded {} modules from config {}", loadedModules, this.fileName);
               elegant.getInstance().getConfigManager().setCurrent(this);
            }

            return;
         } catch (UnknownModuleException var181) {
            elegant.getInstance().getNotificationManager().addNotification(NotificationType.SUCCESS, Localizator.translate("configs.loaded"));
         } catch (Exception var19) {
            elegant.LOGGER.error("Failed to load config file {}: {}", this.fileName, var19.getMessage());
         }
      }
   }

   public void save() {
      try {
         if (!this.file.exists() && !this.file.createNewFile()) {
            throw new IOException("Failed to create config file: " + this.file.getAbsolutePath());
         }

         JsonObject json = new JsonObject();
         JsonArray modulesJsonArray = this.getModulesJsonArray();
         json.add("modules", modulesJsonArray);
         json.add("hudElements", this.getHudElementsJsonArray());
         FileWriter fileWriter = new FileWriter(this.file);

         try {
            fileWriter.write(FileManager.GSON.toJson(json));
         } catch (Throwable var7) {
            try {
               fileWriter.close();
            } catch (Throwable var6) {
               var7.addSuppressed(var6);
            }

            throw var7;
         }

         fileWriter.close();
         if (!this.fileName.equals("autosave")) {
            elegant.getInstance().getConfigManager().setCurrent(this);
         }

         elegant.LOGGER.info("Successfully saved config " + this.fileName);
      } catch (IOException var81) {
         elegant.LOGGER.error("Failed to save config file", var81);
      }
   }

   public void delete() {
      if (this.file.exists() && this.file.delete()) {
         elegant.getInstance().getConfigManager().getConfigFiles().remove(this);
         MessageUtility.info(Text.of("Конфиг " + this.fileName + " успешно удален"));
         elegant.LOGGER.info("Config file deleted: {}", this.file.getAbsolutePath());
      } else {
         MessageUtility.error(Text.of("Произошла ошибка при удалении"));
         elegant.LOGGER.warn("Failed to delete config file: {}", this.file.getAbsolutePath());
      }
   }

   private JsonArray getModulesJsonArray() {
      JsonArray modulesJsonArray = new JsonArray();

      for (Module module : this.modules) {
         JsonObject moduleObject = new JsonObject();
         moduleObject.addProperty("name", module.getName());
         moduleObject.addProperty("enabled", module.isEnabled());
         moduleObject.addProperty("key", module.getKey());
         moduleObject.add("settings", this.getSettingsJsonObject(module));
         modulesJsonArray.add(moduleObject);
      }

      return modulesJsonArray;
   }

   private JsonObject getSettingsJsonObject(Module module) {
      JsonObject settingsObject = new JsonObject();

      for (Setting setting : module.getSettings()) {
         settingsObject.add(setting.getName(), setting.save());
      }

      return settingsObject;
   }

   private JsonArray getHudElementsJsonArray() {
      JsonArray hudElementsArray = new JsonArray();

      for (HudElement element : elegant.getInstance().getHud().getElements()) {
         JsonObject elementObject = new JsonObject();
         elementObject.addProperty("name", element.getName());
         elementObject.addProperty("x", element.getX());
         elementObject.addProperty("y", element.getY());
         elementObject.addProperty("showing", element.isShowing());
         elementObject.add("settings", this.getHudSettingsJsonObject(element));
         hudElementsArray.add(elementObject);
      }

      return hudElementsArray;
   }

   private JsonObject getHudSettingsJsonObject(HudElement element) {
      JsonObject settingsObject = new JsonObject();

      for (Setting setting : element.getSettings()) {
         settingsObject.add(setting.getName(), setting.save());
      }

      return settingsObject;
   }

   @Generated
   public String getFileName() {
      return this.fileName;
   }
}
