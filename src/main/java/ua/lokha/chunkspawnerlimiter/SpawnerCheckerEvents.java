package ua.lokha.chunkspawnerlimiter;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.Chunk;
import net.minecraft.server.v1_12_R1.TileEntity;
import net.minecraft.server.v1_12_R1.TileEntityMobSpawner;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_12_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_12_R1.block.CraftCreatureSpawner;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.SpawnerSpawnEvent;

import java.util.ArrayList;
import java.util.List;

public class SpawnerCheckerEvents implements Listener  {

    /**
     * Чанки, которые были уже проверены за последнее время.
     */
    private LongSet chunks = new LongOpenHashSet();

    /**
     * Очистка проверенных чанков, вызывается таймером.
     */
    public void cleanUp() {
        chunks.clear();
    }

    @EventHandler
    public void on(SpawnerSpawnEvent event) {
        CraftCreatureSpawner spawner = (CraftCreatureSpawner) event.getSpawner();
        if (spawner == null) { // в некоторых случаях бывает null. почему? не знаю
            return;
        }
        long chunkKey = asLong(spawner.getX() >> 4, spawner.getZ() >> 4);
        if (chunks.contains(chunkKey)) {
            return; // уже проверяли за последний период времени
        }
        long start = System.currentTimeMillis();
        chunks.add(chunkKey);

        Chunk chunk = ((CraftChunk) spawner.getChunk()).getHandle();
        World world = spawner.getWorld();
        try {
            int count = 0;
            List<TileEntity> doRemove = null;
            for (TileEntity value : chunk.tileEntities.values()) {
                if (value instanceof TileEntityMobSpawner) {
                    if (++count > Main.getInstance().getLimitSpawners()) {
                        if (doRemove == null) {
                            doRemove = new ArrayList<>();
                        }
                        doRemove.add(value);
                    }
                }
            }

            if (doRemove != null) {
                for (TileEntity value : doRemove) {
                    BlockPosition pos = value.getPosition();
                    world.getBlockAt(pos.getX(), pos.getY(), pos.getZ()).setType(Material.AIR);
                }
            }

            if (count > Main.getInstance().getLimitSpawners()) {
                Main.getInstance().getLogger().warning("Чанк world " + world.getName() + ", " +
                        "x " + chunk.locX + ", z " + chunk.locZ + " (" +
                        "/tp " + (chunk.locX << 4) + " 100 " + (chunk.locZ << 4) + ") " +
                        "превысил лимит по количеству спавнеров " + count + ">" + Main.getInstance().getLimitSpawners() +
                        ", удалили лишние спавнеры.");

                // всем игрокам в радиусе 48 блоков пишем сообщение про удаленный чанк
                for (Player player : world.getPlayers()) {
                    Location loc = player.getLocation();
                    if (!hasDistance2D(spawner.getX(), spawner.getZ(), loc.getX(), loc.getZ(), 48)) {
                        player.sendMessage(Main.getInstance().getMessage());
                    }
                }
            }
        } catch (Exception e) {
            Main.getInstance().getLogger().severe("Ошибка обработки чанка " +
                    chunk.locX + " " + chunk.locZ + " в мире " + world.getName());
            e.printStackTrace();
        }

        long left = System.currentTimeMillis() - start;
        if (left > Main.getInstance().getCheckWarnTimeMillis()) {
            Main.getInstance().getLogger().warning("Проверка чанка " + chunk.locX + " " + chunk.locZ + " в мире " + world.getName() + " заняла " + left + " ms " +
                    "(в конфиге можно настроить порог времени для лога этого сообщения).");
        }
    }

    public static boolean hasDistance2D(double x1, double z1, double x2, double z2, double distance) {
        return Math.abs(x1 - x2) >= distance
                || Math.abs(z1 - z2) >= distance;
    }

    public static long asLong(int x, int z) {
        return (long)x & 4294967295L | ((long)z & 4294967295L) << 32;
    }
}
