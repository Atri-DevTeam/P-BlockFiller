package top.yzljc.blockFiller.item;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import top.yzljc.blockFiller.BlockFiller;

import java.util.*;

/**
 * @Author YZ_Ljc_
 * @ClassName BlockFiller
 * @Created_at 2026/04/17
 * @Project BlockFiller
 * @Package top.yzljc.blockFiller.item
 */
public class BlockFillerItem implements Listener {
    private final Map<UUID, Area> data = new HashMap<>();

    private static ItemStack createItem() {
        ItemStack item = new ItemStack(Material.STICK);
        var metaData = item.getItemMeta();
        if (metaData != null) {
            metaData.setDisplayName("§6魔法填充工具");
            metaData.getPersistentDataContainer().set(new NamespacedKey(BlockFiller.getInstance(), "block_filler"), PersistentDataType.STRING, "special");
            item.setItemMeta(metaData);
        }
        return item;
    }

    private boolean isBlockFiller(ItemStack item) {
        if (item == null || item.getType() != Material.STICK || !item.hasItemMeta()) {
            return false;
        }
        var metaData = item.getItemMeta();
        return metaData != null && metaData.getPersistentDataContainer().has(new NamespacedKey(BlockFiller.getInstance(), "block_filler"), PersistentDataType.STRING);
    }

    public static void getBlockFiller(Player player) {
        player.getInventory().addItem(createItem());
    }

    private void startPrint(Area area, Player player) {
        Location p1 = area.getA();
        Location p2 = area.getB();

        if (p1 == null || p2 == null) {
            player.sendMessage("§c请先选两个点！");
            return;
        }

        if (!p1.getWorld().equals(p2.getWorld())) {
            player.sendMessage("§c两个点不在同一世界！");
            return;
        }

        var world = p1.getWorld();

        int minX = Math.min(p1.getBlockX(), p2.getBlockX());
        int maxX = Math.max(p1.getBlockX(), p2.getBlockX());
        int minY = Math.min(p1.getBlockY(), p2.getBlockY());
        int maxY = Math.max(p1.getBlockY(), p2.getBlockY());
        int minZ = Math.min(p1.getBlockZ(), p2.getBlockZ());
        int maxZ = Math.max(p1.getBlockZ(), p2.getBlockZ());

        int volume = (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
        if (volume > 200000) {
            player.sendMessage("§c区域太大了！");
            return;
        }

        Block source = p1.clone().add(0, 1, 0).getBlock();
        Material blockType = source.getType();
        var sound = source.getBlockData().getSoundGroup().getPlaceSound();

        List<Location> list = new ArrayList<>();

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    list.add(new Location(world, x, y, z));
                }
            }
        }

        Vector dir = p2.toVector().subtract(p1.toVector()).normalize();

        list.sort(Comparator.comparingDouble(loc ->
                loc.toVector().subtract(p1.toVector()).dot(dir)
        ));

        player.sendMessage("§a开始填充，共 " + list.size() + " 个方块");

        new BukkitRunnable() {

            int index = 0;

            @Override
            public void run() {

                int count = 0;

                while (count < 40) {
                    if (index >= list.size()) {
                        cancel();
                        player.sendMessage("§a填充完成！");
                        return;
                    }

                    Location loc = list.get(index);

                    world.getBlockAt(loc).setType(blockType);

                    float pitch = 0.8f + (float) Math.random() * 0.4f;
                    player.playSound(loc, sound, 0.5f, pitch);

                    index++;
                    count++;
                }
            }

        }.runTaskTimer(BlockFiller.getInstance(), 0, 1);


        player.sendMessage(
                Component.text("已填充区域内的方块为: ", NamedTextColor.GREEN)
                        .append(Component.text(blockType.name(), NamedTextColor.YELLOW))
        );
        data.remove(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerUsing(PlayerInteractEvent event) {
        if (!isBlockFiller(event.getItem())) return;
        var player = event.getPlayer();
        data.putIfAbsent(player.getUniqueId(), new Area());
        Area area = data.get(player.getUniqueId());
        if (event.getAction().isLeftClick()) {
            if (event.getClickedBlock() != null) {
                area.setA(event.getClickedBlock().getLocation());

                player.sendMessage(
                        Component.text("已记录位置A: ", NamedTextColor.GOLD)
                                .append(Component.text(
                                        area.getA().getBlockX() + ", " +
                                                area.getA().getBlockY() + ", " +
                                                area.getA().getBlockZ(),
                                        NamedTextColor.YELLOW
                                ))
                );
            }
        }

        if (event.getAction().isRightClick()) {

            if (event.getPlayer().isSneaking()) {
                startPrint(area, player);

                player.sendMessage(
                        Component.text("正在填充方块...", NamedTextColor.RED)
                );
                return;
            }

            if (event.getClickedBlock() != null) {
                area.setB(event.getClickedBlock().getLocation());

                player.sendMessage(
                        Component.text("已记录位置B: ", NamedTextColor.GOLD)
                                .append(Component.text(
                                        area.getB().getBlockX() + ", " +
                                                area.getB().getBlockY() + ", " +
                                                area.getB().getBlockZ(),
                                        NamedTextColor.YELLOW
                                ))
                );
            }
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        data.remove(uuid);
    }

    @Getter
    @Setter
    private class Area {
        private Location A;
        private Location B;

        public boolean isFinish() {
            return A != null && B != null;
        }
    }

}