package de.survivalnight.luna.rtp.logic;

import de.survivalnight.luna.rtp.Rtp;
import org.bukkit.*;
import org.bukkit.block.Block;

import java.util.Random;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class SafeLocationFinder {

    private static final Random random = new Random();
    private static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool();

    public static void findSafeLocationAsync(World world, int range, Consumer<Location> callback) {
        tryFindSafeLocation(world, range, callback, 5000, true); // 5 Sekunden Timeout + 1 Retry erlaubt
    }

    private static void tryFindSafeLocation(World world, int range, Consumer<Location> callback, long timeoutMs, boolean allowRetry) {
        THREAD_POOL.submit(() -> {
            long startTime = System.currentTimeMillis();

            for (int attempts = 0; attempts < 1000; attempts++) {
                if (System.currentTimeMillis() - startTime > timeoutMs) {
                    if (allowRetry) {
                        // nochmal versuchen (nur 1x)
                        tryFindSafeLocation(world, range, callback, timeoutMs, false);
                    } else {
                        Bukkit.getScheduler().runTask(Rtp.getInstance(), () -> callback.accept(null));
                    }
                    return;
                }

                int x = random.nextInt(range * 2) - range;
                int z = random.nextInt(range * 2) - range;


                if (isEmptyChunk(world, x, z)) continue;

                int y = getSurfaceY(world, x, z);
                if (y == -1) continue;

                Location loc = new Location(world, x + 0.5, y, z + 0.5);
                if (isSafe(loc)) {
                    Bukkit.getScheduler().runTask(Rtp.getInstance(), () -> callback.accept(loc));
                    return;
                }
            }


            if (allowRetry) {
                tryFindSafeLocation(world, range, callback, timeoutMs, false);
            } else {
                Bukkit.getScheduler().runTask(Rtp.getInstance(), () -> callback.accept(null));
            }
        });
    }

    private static boolean isEmptyChunk(World world, int x, int z) {
        Chunk chunk = world.getChunkAt(x >> 4, z >> 4);

        for (int y = world.getMinHeight(); y < world.getMaxHeight(); y += 8) {
            Block block = chunk.getBlock(x & 15, y, z & 15);
            if (!block.isEmpty()) return false;
        }

        return true;
    }

    private static int getSurfaceY(World world, int x, int z) {
        World.Environment env = world.getEnvironment();

        if (env == World.Environment.NETHER) {
            for (int y = 30; y < 118; y++) {
                if (hasEnoughSpace(world, x, y, z)) return y;
            }
            return -1;
        }

        if (env == World.Environment.THE_END) {
            for (int y = 50; y < world.getMaxHeight() - 2; y++) {
                Material ground = world.getBlockAt(x, y - 1, z).getType();
                if ((ground == Material.END_STONE || ground == Material.OBSIDIAN)
                        && hasEnoughSpace(world, x, y, z)) {
                    return y;
                }
            }
            return -1;
        }

        int y = world.getHighestBlockYAt(x, z);
        if (y <= world.getMinHeight() + 1) return -1;
        return hasEnoughSpace(world, x, y + 1, z) ? y + 1 : -1;
    }

    private static boolean hasEnoughSpace(World world, int x, int y, int z) {
        return world.getBlockAt(x, y, z).getType().isAir()
                && world.getBlockAt(x, y + 1, z).getType().isAir();
    }

    private static boolean isSafe(Location loc) {
        World world = loc.getWorld();
        if (world == null) return false;
        Material ground = loc.clone().subtract(0, 1, 0).getBlock().getType();
        if (ground.name().contains("LAVA") || ground.name().contains("WATER") ||
                ground == Material.CACTUS || ground == Material.FIRE || ground == Material.AIR) return false;

        if (world.getEnvironment() == World.Environment.NETHER && loc.getY() >= 120) return false;

        return true;
    }
}
