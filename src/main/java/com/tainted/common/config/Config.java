package com.tainted.common.config;

import com.tainted.Sleep;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;


public class Config {
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final CommonConfig COMMON;

    static {
        Pair<CommonConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(CommonConfig::new);
        COMMON = specPair.getLeft();
        COMMON_SPEC = specPair.getRight();
    }

    public static class CommonConfig {

        private static final List<String> defaultList = new ArrayList<>(List.of("minecraft:creeper", "minecraft:zombie", "minecraft:spider"));
        public final String CATEGORY_GENERAL = "general";
        public final ForgeConfigSpec.IntValue SLEEP_TIMER;
        public final ForgeConfigSpec.BooleanValue PLAYER_CHECK;
        public final ForgeConfigSpec.BooleanValue ENABLE_LIST;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> MOB_LIST;
        public final String CATEGORY_RANGES = "ranges";
        public final ForgeConfigSpec.BooleanValue ENABLE_SCALING;
        public final ForgeConfigSpec.IntValue VERTICAL_RANGE;
        public final ForgeConfigSpec.IntValue HORIZONTAL_RANGE;

        public CommonConfig(ForgeConfigSpec.Builder builder) {
            String modid = Sleep.MODID;
            builder.comment("General Settings").push(CATEGORY_GENERAL);
            SLEEP_TIMER = builder
                    .comment("""
                            How long does the player need to be sleeping in order
                            for entity despawning to occur? | Default: 50""")
                    .translation(modid + ".config." + "SLEEP_TIMER")
                    .defineInRange("sleepTimer", 50, 1, 100);
            PLAYER_CHECK = builder
                    .comment("""
                            Should check and disable entity despawning around other
                            non-sleeping players withing range? | Default: true""")
                    .translation(modid + ".config." + "ENABLE_PLAYER_CHECK")
                    .define("playerCheck", true);
            ENABLE_LIST = builder
                    .comment("Enables individual mob despawns instead of group despawning. | Default: false")
                    .translation(modid + ".config." + "ENABLE_LIST")
                    .define("enableList", false);
            MOB_LIST = builder
                    .comment("""
                            List of mobs to despawn.
                            Formatting: ["minecraft:creeper", "minecraft:zombie", "minecraft:spider", "modid:entityname"]""")
                    .translation(modid + ".config." + "MOB_LIST")
                    .defineListAllowEmpty(List.of("mobs"), () -> defaultList, entity -> (entity instanceof String string && ResourceLocation.isValidResourceLocation(string)));
            builder.pop();

            builder.comment("Range Settings").push(CATEGORY_RANGES);
            ENABLE_SCALING = builder
                    .comment("Should scaling based on difficulty be enabled? | Default: true")
                    .translation(modid + ".config." + "ENABLED_SCALING")
                    .define("enableScaling", true);
            VERTICAL_RANGE = builder
                    .comment("""
                            Vertical range to check for mobs to despawn. | Default: 16
                            Scaling: EASY = base | NORMAL = base / 2 | HARD = base / 4""")
                    .translation(modid + ".config." + "VERTICAL_RANGE")
                    .defineInRange("verticalRange", () -> 16, 0, 64);
            HORIZONTAL_RANGE = builder
                    .comment("""
                            Horizontal range to check for mobs to despawn. | Default: 64
                            Scaling: EASY = base | NORMAL = base / 2 | HARD = base / 4""")
                    .translation(modid + ".config." + "HORIZONTAL_RANGE")
                    .defineInRange("horizontalRange", () -> 64, 0, 256);
            builder.pop();
        }
    }

}