package me.faris.kingkits.guis;

import java.util.ArrayList;
import java.util.List;

import me.faris.kingkits.KingKits;
import me.faris.kingkits.helpers.KitStack;
import me.faris.kingkits.hooks.Plugin;
import me.faris.kingkits.listeners.commands.SetKit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GuiKitMenu implements Listener {

	public static List<String> playerMenus = new ArrayList<String>();

	private KingKits thePlugin = null;
	private Player thePlayer = null;
	private String guiTitle = null;
	private KitStack[] guiKitStacks = null;
	private Inventory guiInventory = null;

	/** 
	 * Create a new gui menu instance.
	 * @param player - The player that is using the menu
	 * @param title - The title of the menu
	 * @param kitStacks - The kits in the menu
	 */
	public GuiKitMenu(Player player, String title, KitStack[] kitStacks) {
		this.thePlugin = Plugin.getPlugin();
		this.thePlayer = player;
		this.guiTitle = title;
		this.guiKitStacks = kitStacks;

		this.clickedItem = false;
		this.closedInventory = false;

		if (Plugin.isInitialised()) Bukkit.getPluginManager().registerEvents(this, Plugin.getPlugin());
		else Bukkit.getPluginManager().registerEvents(this, Bukkit.getPluginManager().getPlugin("KingKits"));

		playerMenus.add(player.getName());
	}

	/** Opens the menu for the player **/
	public void openMenu() {
		try {
			this.closeMenu(false);

			int menuSize = 36;
			if (this.guiKitStacks.length > 32) menuSize = 45;
			Inventory menuInventory = this.thePlayer.getServer().createInventory(null, menuSize, this.guiTitle);
			for (int i = 0; i < this.guiKitStacks.length; i++) {
				try {
					ItemStack currentStack = this.guiKitStacks[i].getItemStack();
					if (currentStack != null) {
						if (currentStack.getType() != Material.AIR) {
							if (currentStack.getItemMeta() != null) {
								ItemMeta itemMeta = currentStack.getItemMeta();
								ChatColor kitColour = this.thePlayer.hasPermission("kingkits.kits." + this.guiKitStacks[i].getKitName().toLowerCase()) ? ChatColor.GREEN : ChatColor.DARK_RED;
								itemMeta.setDisplayName(ChatColor.RESET + "" + kitColour + this.guiKitStacks[i].getKitName());
								currentStack.setItemMeta(itemMeta);
							}
							menuInventory.addItem(currentStack);
						}
					}
				} catch (Exception ex) {
					continue;
				}
			}
			this.guiInventory = menuInventory;
			this.thePlayer.openInventory(menuInventory);
		} catch (Exception ex) {
		}
	}

	/** Closes the menu for the player and unregisters the event **/
	public void closeMenu(boolean unregisterEvents) {
		try {
			if (unregisterEvents) {
				InventoryClickEvent.getHandlerList().unregister(this);
				InventoryCloseEvent.getHandlerList().unregister(this);
			}
			this.thePlayer.closeInventory();
			playerMenus.remove(this.thePlayer.getName());
		} catch (Exception ex) {
		}
	}

	/** Returns the player that is opening the menu **/
	public Player getPlayer() {
		return this.thePlayer;
	}

	/** Sets the player that is opening the menu **/
	public GuiKitMenu setPlayer(Player player) {
		this.thePlayer = player;
		return this;
	}

	/** Returns the title of the menu **/
	public String getTitle() {
		return this.guiTitle;
	}

	/** Sets the title of the menu **/
	public GuiKitMenu setTitle(String title) {
		this.guiTitle = title;
		return this;
	}

	/** Returns the kit item stacks **/
	public KitStack[] getKitStacks() {
		return this.guiKitStacks;
	}

	/** Sets the kit item stacks **/
	public GuiKitMenu setKitStacks(KitStack[] kitStacks) {
		this.guiKitStacks = kitStacks;
		return this;
	}

	/** Returns if the player has clicked in the inventory **/
	private boolean clickedItem = false;
	/** Returns if the player has closed the inventory **/
	private boolean closedInventory = false;

	/** Handles when a player clicks an item **/
	@EventHandler
	protected void onPlayerClickSlot(InventoryClickEvent event) {
		try {
			if (this.guiInventory != null && event.getInventory() != null && event.getWhoClicked() != null) {
				if (event.getWhoClicked() instanceof Player) {
					if (event.getSlot() >= 0) {
						if (event.getSlotType() == SlotType.CONTAINER) {
							if (event.getInventory().getTitle().equals(this.guiInventory.getTitle())) {
								event.setCurrentItem(null);
								event.setCancelled(true);
								if (!this.clickedItem) {
									if (this.guiKitStacks.length >= event.getSlot()) SetKit.setKitPlayerKit(this.thePlugin, (Player) event.getWhoClicked(), this.guiKitStacks[event.getSlot()].getKitName());
								}
								this.clickedItem = true;
								this.closedInventory = true;
								this.closeMenu(true);
							}
						}
					}
				}
			}
		} catch (Exception ex) {
			if (event.getInventory() != null && this.guiInventory != null) {
				if (event.getInventory().getTitle().equals(this.guiInventory.getTitle())) {
					event.setCurrentItem(null);
					event.setCancelled(true);
					this.closedInventory = true;
					this.closeMenu(true);
				}
			}
		}
	}

	/** Handles when a player exits the menu **/
	@EventHandler(priority = EventPriority.HIGHEST)
	protected void onPlayerCloseInventory(InventoryCloseEvent event) {
		try {
			if (!this.closedInventory) {
				if (this.guiInventory != null && event.getInventory() != null) {
					if (event.getPlayer() instanceof Player) {
						if (event.getInventory().getTitle().equals(this.guiInventory.getTitle())) this.closeMenu(true);
					}
				}
			}
		} catch (Exception ex) {
		}
	}

}
