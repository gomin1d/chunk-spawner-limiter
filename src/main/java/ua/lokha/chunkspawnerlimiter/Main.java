package ua.lokha.chunkspawnerlimiter;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * Главный класс плагина
 */
public class Main extends JavaPlugin {

    @Getter
    private static Main instance;

    @Getter
    private long limitSpawners;
    @Getter
    private int periodTicks;
    @Getter
    private int checkWarnTimeMillis;
    @Getter
    private String message;

    private BukkitTask timer;
    private SpawnerCheckerEvents spawnerCheckerEvents;

    public Main() {
        instance = this;
    }

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.reloadConfigParams();

        spawnerCheckerEvents = new SpawnerCheckerEvents();
        Bukkit.getPluginManager().registerEvents(spawnerCheckerEvents, this);

        this.getCommand("chunkspawnerlimiter").setExecutor(new ChunkSpawnerLimiterCommandExecutor());
    }

    public void reloadConfigParams() {
        if (timer != null) {
            try {
                timer.cancel();
            } catch (Exception ignored) {}
            timer = null;
        }

        limitSpawners = ((Number)this.getConfig().get("limit-spawners", 4)).longValue();
        periodTicks = this.getConfig().getInt("period-ticks", 200);
        checkWarnTimeMillis = this.getConfig().getInt("check-warn-time-millis", 5);
        message = this.getConfig().getString("message").replace("&", "§");

        timer = Bukkit.getScheduler().runTaskTimer(this, () -> spawnerCheckerEvents.cleanUp(), periodTicks, periodTicks);
    }
}
