package dev.kataray.brewery;

import com.dre.brewery.BCauldron;
import com.dre.brewery.BreweryPlugin;
import com.dre.brewery.api.addons.AddonInfo;
import com.dre.brewery.api.addons.BreweryAddon;
import com.dre.brewery.depend.universalScheduler.UniversalRunnable;
import com.dre.brewery.utility.MaterialUtil;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

// Idea:
// Listen for dropped items, check if the item is in a BCauldron block when it hits the ground <-- Needs a runnable for this?
// If it's in a cauldron, add it to the cauldron as if the player had right-clicked and despawn the item.
// ALSO: Add config option to disable adding items by interacting with the cauldron? <-- Might not be possible within an addon, would have to manually edit BreweryX for this.
@AddonInfo(
        name = "DroppableCauldrons",
        version = "1.1",
        description = "Allows players to drop ingredients into BreweryX cauldrons and adds them as ingredients.",
        author = "Jsinco & Kataray"
)
public class DroppableCauldrons extends BreweryAddon implements Listener {

    private static final int MAX_RUNNABLE_TICKS = 90;


    @Override
    public void onAddonEnable() {
        registerListener(this);
    }


    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent e) {
        Item droppedItem = e.getItemDrop();

        new UniversalRunnable() {
            private int runnableTicks = 0;

            @Override
            public void run() {
                Location loc = droppedItem.getLocation();
                if (runnableTicks++ > MAX_RUNNABLE_TICKS || droppedItem.isDead()) {
                    this.cancel();
                } else if (MaterialUtil.isWaterCauldron(loc.getBlock().getType())) {
                    PlayerInteractEvent fakeInteractEvent = new PlayerInteractEvent(
                            e.getPlayer(),
                            Action.RIGHT_CLICK_BLOCK,
                            droppedItem.getItemStack().clone(),
                            loc.getBlock(),
                            BlockFace.UP,
                            EquipmentSlot.HAND
                    );


                    BreweryPlugin.getScheduler().runTask(droppedItem.getLocation(), () -> {
                        BCauldron.clickCauldron(fakeInteractEvent);
                        droppedItem.remove();
                    });
                }
            }
        }.runTaskTimerAsynchronously(getBreweryPlugin(), 1L, 1L);
    }
}