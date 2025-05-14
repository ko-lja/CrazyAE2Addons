package net.oktawia.crazyae2addons;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class CrazyConfig {
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final Common COMMON;

    static {
        Pair<Common, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(Common::new);
        COMMON = pair.getLeft();
        COMMON_SPEC = pair.getRight();
    }

    public static class Common {
        public final ForgeConfigSpec.BooleanValue enableCPP;
        public final ForgeConfigSpec.BooleanValue enablePeacefullSpawner;

        public Common(ForgeConfigSpec.Builder builder) {
            builder.comment("Crazy AE2 Addons - Config").push("general");

            enableCPP = builder
                    .comment("Enables The functionality of pattern providers to set the circuit of the GregTech's machine its pushing to")
                    .define("enableCPP", true);

            enablePeacefullSpawner = builder
                    .comment("Enables Spawner Controller to work also on peacefull mode")
                    .define("enablePeacefullSpawner", true);

            builder.pop();
        }
    }
}
