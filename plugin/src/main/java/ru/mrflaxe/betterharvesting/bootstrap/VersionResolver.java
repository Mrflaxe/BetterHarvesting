package ru.mrflaxe.betterharvesting.bootstrap;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;

import ru.mrflaxe.betterharvesting.version.VersionContext;

/**
 * Picks the right {@link VersionContext} for the running server.
 *
 * The registry is sorted from newest to oldest. Each entry carries the minimum
 * supported MC version and the fully qualified class name of the implementation;
 * we walk the list and load the first whose {@code MIN_VERSION} is &le; the
 * server's version.
 *
 * Class names are referenced as strings, not as imports, so that classes
 * compiled for a newer Java release are never resolved by the JVM until the
 * matching server version actually selects them.
 */
public final class VersionResolver {

    private static final Entry[] REGISTRY = new Entry[] {
        new Entry(new int[] {26, 1, 0}, "ru.mrflaxe.betterharvesting.v26_1.V26_1VersionContext"),
        new Entry(new int[] {1, 20, 5}, "ru.mrflaxe.betterharvesting.v1_20_5.V1_20_5VersionContext"),
        new Entry(new int[] {1, 14, 0}, "ru.mrflaxe.betterharvesting.v1_14.V1_14VersionContext"),
    };

    private VersionResolver() {}

    public static VersionContext resolve(Logger logger) {
        String rawVersion = Bukkit.getBukkitVersion();
        int[] server = parseVersion(rawVersion);

        for (Entry entry : REGISTRY) {
            if (compare(server, entry.minVersion) >= 0) {
                VersionContext ctx = instantiate(entry.className);
                logger.info("Selected " + simpleName(entry.className)
                        + " for server version " + rawVersion);
                return ctx;
            }
        }

        Entry fallback = REGISTRY[REGISTRY.length - 1];
        logger.log(Level.WARNING, "Could not match server version ''{0}'' to any known context; "
                + "falling back to {1}.",
                new Object[] { rawVersion, simpleName(fallback.className) });
        return instantiate(fallback.className);
    }

    private static VersionContext instantiate(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            return (VersionContext) clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to load version context: " + className, e);
        }
    }

    static int[] parseVersion(String raw) {
        String head = raw;
        int dash = head.indexOf('-');
        if (dash >= 0) {
            head = head.substring(0, dash);
        }
        String[] parts = head.split("\\.");
        int[] result = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                result[i] = Integer.parseInt(parts[i]);
            } catch (NumberFormatException e) {
                result[i] = 0;
            }
        }
        return result;
    }

    static int compare(int[] a, int[] b) {
        int length = Math.max(a.length, b.length);
        for (int i = 0; i < length; i++) {
            int left = i < a.length ? a[i] : 0;
            int right = i < b.length ? b[i] : 0;
            if (left != right) {
                return left < right ? -1 : 1;
            }
        }
        return 0;
    }

    private static String simpleName(String fqcn) {
        int dot = fqcn.lastIndexOf('.');
        return dot < 0 ? fqcn : fqcn.substring(dot + 1);
    }

    private static final class Entry {
        final int[] minVersion;
        final String className;

        Entry(int[] minVersion, String className) {
            this.minVersion = minVersion;
            this.className = className;
        }
    }
}
