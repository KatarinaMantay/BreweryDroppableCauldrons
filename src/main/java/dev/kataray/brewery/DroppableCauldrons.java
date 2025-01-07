package dev.kataray.brewery;

import com.dre.brewery.BCauldron;
import com.dre.brewery.BIngredients;
import com.dre.brewery.BreweryPlugin;
import com.dre.brewery.api.addons.AddonInfo;
import com.dre.brewery.api.addons.BreweryAddon;
import com.dre.brewery.recipe.BCauldronRecipe;
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
import org.bukkit.inventory.ItemStack;

// Idea:
// Listen for dropped items, check if the item is in a BCauldron block when it hits the ground <-- Needs a runnable for this?
// If it's in a cauldron, add it to the cauldron as if the player had right-clicked and despawn the item.
// ALSO: Add config option to disable adding items by interacting with the cauldron? <-- Might not be possible within an addon, would have to manually edit BreweryX for this.
@AddonInfo(
        name = "DroppableCauldrons",
        version = "1.3",
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
        ItemStack itemStack = droppedItem.getItemStack();

        // Quick check if it's even a possible ingredient
        if (!BCauldronRecipe.acceptedMaterials.contains(itemStack.getType()) && !itemStack.hasItemMeta()) {
            return;
        }

        final int[] ticks = {0};

        BreweryPlugin.getScheduler().runTaskTimer(droppedItem.getLocation(), () -> {
            Location loc = droppedItem.getLocation();
            if (ticks[0]++ > MAX_RUNNABLE_TICKS || droppedItem.isDead()) {
                return;
            }

            if (MaterialUtil.isWaterCauldron(loc.getBlock().getType()) &&
                    MaterialUtil.getFillLevel(loc.getBlock()) > 0) {

                // Check if it's a valid ingredient for the cauldron
                if (!BCauldron.ingredientAdd(loc.getBlock(), itemStack, e.getPlayer())) {
                    return;
                }

                PlayerInteractEvent fakeInteractEvent = new PlayerInteractEvent(
                        e.getPlayer(),
                        Action.RIGHT_CLICK_BLOCK,
                        itemStack.clone(),
                        loc.getBlock(),
                        BlockFace.UP,
                        EquipmentSlot.HAND
                );

                BCauldron.clickCauldron(fakeInteractEvent);
                droppedItem.remove();
            }
        }, 1L, 1L);
    }
}
