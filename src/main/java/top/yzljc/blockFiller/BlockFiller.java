package top.yzljc.blockFiller;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import top.yzljc.blockFiller.command.GetBlockFillerCommand;
import top.yzljc.blockFiller.item.BlockFillerItem;

public final class BlockFiller extends JavaPlugin {
    @Getter
    private static BlockFiller instance;
    @Getter
    private BlockFillerItem item;

    @Override
    public void onEnable() {
        instance = this;
        this.item = new BlockFillerItem();
        getServer().getPluginManager().registerEvents(new BlockFillerItem(), this);
        getCommand("blockfiller").setExecutor(new GetBlockFillerCommand());
        getLogger().info("BlockFiller 已启用！");
    }

    @Override
    public void onDisable() {
        instance = null;
        getLogger().info("BlockFiller 已禁用！");
    }
}
