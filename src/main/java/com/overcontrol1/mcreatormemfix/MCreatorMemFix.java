package com.overcontrol1.mcreatormemfix;

import com.google.common.base.Stopwatch;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.overcontrol1.mcreatormemfix.asm.MCreatorVariableTransformer;
import it.unimi.dsi.fastutil.Pair;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.service.MixinService;

import java.io.IOException;
import java.nio.file.Files;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Mod(MCreatorMemFix.MOD_ID)
public class MCreatorMemFix {
    public static final Logger LOGGER = LoggerFactory.getLogger("MCreatorMemFix");
    public static final String MOD_ID = "mcreator_mem_fix";

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public MCreatorMemFix(FMLJavaModLoadingContext ctx) {
        ctx.getModEventBus().addListener(this::onCommonSetup);
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        if (MCreatorVariableTransformer.NEEDS_SCANNING) {
            final List<Pair<String, String>> mcreatorMods = new ArrayList<>();
            ModList.get().forEachModContainer((id, container) -> {
                Object mod = container.getMod();
                Class<?> clazz = mod.getClass();
                String simpleName = clazz.getSimpleName();
                String packageName = clazz.getPackageName();
                String variablePackageName = packageName + ".network." + simpleName + "Variables";

                try {
                    MixinService.getService()
                            .getBytecodeProvider().getClassNode(variablePackageName);

                    String simpleNameWithoutMod = simpleName.substring(0, simpleName.length() - 3);

                    mcreatorMods.add(Pair.of(packageName, simpleNameWithoutMod));
                } catch (IOException | ClassNotFoundException ignored) {

                }
            });

            outputToFile(mcreatorMods);

            LOGGER.warn("[MCreatorMemFix] Scanned {} new MCreator mods in this environment. RESTART RECOMMENDED.",
                    mcreatorMods.size());
        }
    }

    private void outputToFile(List<Pair<String, String>> mcreatorMods) {
        var path = MCreatorVariableTransformer.CONFIG_PATH;

        var modsArray = new JsonArray();
        for (var packageClassNamePair : mcreatorMods) {
            var object = new JsonObject();
            object.addProperty("package", packageClassNamePair.first());
            object.addProperty("className", packageClassNamePair.right());

            modsArray.add(object);
        }

        var outerObject = new JsonObject();
        outerObject.add("mods", modsArray);

        try (var writer = new JsonWriter(Files.newBufferedWriter(path))) {
            gson.toJson(outerObject, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
