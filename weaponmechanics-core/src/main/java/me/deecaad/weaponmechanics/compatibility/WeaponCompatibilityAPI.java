package me.deecaad.weaponmechanics.compatibility;

import com.cjcrafter.foliascheduler.util.MinecraftVersions;
import me.deecaad.core.compatibility.CompatibilitySetup;
import me.deecaad.weaponmechanics.WeaponMechanics;
import org.bukkit.Bukkit;

public class WeaponCompatibilityAPI {

    private static final String PACKAGE = "me.deecaad.weaponmechanics.compatibility";

    /**
     * The newest compatibility class bundled with this jar. Used as a last resort when the server
     * version is too new to be recognized (MinecraftVersions reports a protocol of -1).
     */
    private static final String LATEST = "v26_3_R1";

    private static IWeaponCompatibility weaponCompatibility;
    private static boolean initialized;

    public static IWeaponCompatibility getWeaponCompatibility() {
        if (!initialized) {
            initialized = true;
            weaponCompatibility = resolve();
        }
        return weaponCompatibility;
    }

    private static IWeaponCompatibility resolve() {
        MinecraftVersions.Version current = MinecraftVersions.getCurrent();
        IWeaponCompatibility compatibility = new CompatibilitySetup().getCompatibleVersion(IWeaponCompatibility.class, PACKAGE);
        if (compatibility != null)
            return compatibility;

        // Versions unknown to MinecraftVersions have a protocol of -1, so try the first revision
        compatibility = tryLoad("v" + current.getMajor() + "_" + current.getMinor() + "_R1");
        if (compatibility != null)
            return compatibility;

        compatibility = tryLoad(LATEST);
        if (compatibility != null) {
            WeaponMechanics.getInstance().getDebugger().warning("Server version " + current + " (" + Bukkit.getBukkitVersion() + ") is not officially supported",
                    "Falling back to compatibility class " + compatibility.getClass().getSimpleName() + ", some features may not work correctly");
            return compatibility;
        }

        WeaponMechanics.getInstance().getDebugger().severe("Unsupported server version: " + current + " (" + Bukkit.getBukkitVersion() + ")",
                "The protocol version " + current.toProtocolString() + " has no compatibility class...",
                "If you are using a new version of Minecraft, you might need to update your plugins!",
                "!!! CRITICAL ERROR !!!");
        return null;
    }

    private static IWeaponCompatibility tryLoad(String className) {
        try {
            Class<?> clazz = Class.forName(PACKAGE + "." + className, true, IWeaponCompatibility.class.getClassLoader());
            return (IWeaponCompatibility) clazz.getConstructor().newInstance();
        } catch (ClassNotFoundException e) {
            return null;
        } catch (ReflectiveOperationException | LinkageError e) {
            WeaponMechanics.getInstance().getDebugger().warning("Failed to load compatibility class " + className + ": " + e);
            return null;
        }
    }
}
