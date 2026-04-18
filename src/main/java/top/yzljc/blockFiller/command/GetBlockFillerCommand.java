package top.yzljc.blockFiller.command;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.yzljc.blockFiller.item.BlockFillerItem;

/**
 * @Author YZ_Ljc_
 * @ClassName GetBlockFillerCommand
 * @Created_at 2026/04/17
 * @Project BlockFiller
 * @Package top.yzljc.blockFiller.command
 */
public class GetBlockFillerCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        var p = sender instanceof Player p1 ? p1 : null;
        if (!sender.hasPermission("blockfiller.use")) {
            sender.sendMessage("§c你没有权限执行这个命令!");
            return true;
        }
        if (p == null) {
            sender.sendMessage("§c这个命令只能由玩家执行!");
            return true;
        }
        BlockFillerItem.getBlockFiller(p);
        sender.sendMessage("§a你获得了选区工具!");
        return true;
    }
}