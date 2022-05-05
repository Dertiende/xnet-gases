package terrails.xnetgases;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import mcjty.rftoolsbase.api.xnet.channels.IConnectable;
import mcjty.xnet.XNet;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import terrails.xnetgases.helper.ChannelModule;
import terrails.xnetgases.module.gas.GasChannelModule;
import terrails.xnetgases.module.gas.GasUtils;
import terrails.xnetgases.module.infuse.InfuseChannelModule;
import terrails.xnetgases.module.infuse.InfuseUtils;
import terrails.xnetgases.module.logic.XGLogicChannelModule;
import terrails.xnetgases.module.pigment.PigmentChannelModule;
import terrails.xnetgases.module.pigment.PigmentUtils;
import terrails.xnetgases.module.slurry.SlurryChannelModule;
import terrails.xnetgases.module.slurry.SlurryUtils;

import java.nio.file.Path;
import java.util.Arrays;

@Mod(XNetGases.MOD_ID)
public class XNetGases {

    public static final String MOD_ID = "xnetgases";
    public static final Logger LOGGER = LogManager.getLogger();

    private static final ForgeConfigSpec CONFIG_SPEC;

    private static final ChannelModule[] MODULES = {
            new GasChannelModule(),
            new InfuseChannelModule(),
            new XGLogicChannelModule(),
            new PigmentChannelModule(),
            new SlurryChannelModule()
    };

    public XNetGases() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CONFIG_SPEC, "xnetgases.toml");
        final IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::setup);
    }

    private void setup(final FMLCommonSetupEvent event) {
        loadConfig(FMLPaths.CONFIGDIR.get().resolve("xnetgases.toml"));

        Arrays.stream(MODULES).forEach(XNet.xNetApi::registerChannelType);

        XNet.xNetApi.registerConnectable((reader, connectorPos, blockPos, tile, direction) -> {
            if (GasUtils.getGasHandlerFor(tile, direction).isPresent()
                    || SlurryUtils.getSlurryHandlerFor(tile, direction).isPresent()
                    || InfuseUtils.getInfuseHandlerFor(tile, direction).isPresent()
                    || PigmentUtils.getPigmentHandlerFor(tile, direction).isPresent()) {
                return IConnectable.ConnectResult.YES;
            } else return IConnectable.ConnectResult.DEFAULT;
        });
    }

    static {
        final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("General settings").push("general");
        Arrays.stream(MODULES).forEach(module -> module.setupConfig(builder));
        CONFIG_SPEC = builder.pop().build();
    }

    private static void loadConfig(Path path) {
        XNetGases.LOGGER.debug("Loading config file {}", path);

        final CommentedFileConfig configData = CommentedFileConfig.builder(path)
                .sync()
                .autosave()
                .writingMode(WritingMode.REPLACE)
                .build();

        XNetGases.LOGGER.debug("Built TOML config for {}", path.toString());
        configData.load();
        XNetGases.LOGGER.debug("Loaded TOML config for {}", path.toString());
        CONFIG_SPEC.setConfig(configData);
    }
}
