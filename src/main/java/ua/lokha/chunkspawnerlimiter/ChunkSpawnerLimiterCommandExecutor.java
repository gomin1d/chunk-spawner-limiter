package ua.lokha.chunkspawnerlimiter;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ChunkSpawnerLimiterCommandExecutor implements CommandExecutor, TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(
                    "§e=========[ChunkSpawnerLimiter]=========" +
                            "\n§4/chunkspawnerlimiter reload §7- перезагрузить конфиг"
            );
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            Main.getInstance().reloadConfig();
            Main.getInstance().reloadConfigParams();
            sender.sendMessage("§eКонфиг перезагружен.");
            return true;
        }

        sender.sendMessage("§cАргумент команды не найден.");
        return true;
    }

    private static class ChunkData {
        private String world;
        private int x;
        private int z;
        private long size;

        public ChunkData(String world, int x, int z, long size) {
            this.world = world;
            this.x = x;
            this.z = z;
            this.size = size;
        }
    }

    @SuppressWarnings({"LambdaBodyCanBeCodeBlock"})
    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return filterTabResponse(Arrays.asList("reload"), args);
        }

        return Collections.emptyList();
    }

    private static List<String> filterTabResponse(List<String> list, String[] args) {
        return list.stream()
                .filter(el -> StringUtils.containsIgnoreCase(el, args[args.length - 1]))
                .collect(Collectors.toList());
    }
}
