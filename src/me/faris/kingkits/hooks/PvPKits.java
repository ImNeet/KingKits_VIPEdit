package me.faris.kingkits.hooks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import me.faris.kingkits.KingKits;
import me.faris.kingkits.listeners.commands.SetKit;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.permissions.Permission;
import org.bukkit.potion.PotionEffect;

public class PvPKits {
	private static KingKits plugin = null;

	/**
	 * Ignore this method, it's just for PvPKits plugin.
	 */
	public PvPKits(KingKits pvpKitsPlugin) {
		plugin = pvpKitsPlugin;
	}

	/**
	 * Get KingKit's logger.
	 */
	public static Logger getPluginLogger() {
		return plugin.getLogger();
	}

	/**
	 * Returns if a player has a kit.
	 *
	 * @param player
	 *            The player to check.
	 */
	public static boolean hasKit(Player player) {
		if (player == null) return false;
		return hasKit(player.getName());
	}

	/**
	 * Returns if a player has a kit.
	 *
	 * @param player
	 *            The player to check.
	 */
	public static boolean hasKit(String player) {
		boolean hasKit = false;
		if (plugin.usingKits.containsKey(player)) hasKit = true;
		return hasKit;
	}

	/**
	 * Returns if a player has a kit.
	 *
	 * @param player
	 *            The player to check.
	 * @param ignoreOPs
	 *            Input true if you should ignore OPs.
	 */
	public static boolean hasKit(Player player, boolean ignoreOPs) {
		if (player == null) return false;
		return hasKit(player.getName(), ignoreOPs);
	}

	/**
	 * Returns if a player has a kit.
	 *
	 * @param player
	 *            The player to check.
	 * @param ignoreOPs
	 *            Input true if you should ignore OPs.
	 */
	public static boolean hasKit(String player, boolean ignoreOPs) {
		boolean hasKit = false;
		if (ignoreOPs) {
			if (plugin.playerKits.containsKey(player)) {
				hasKit = true;
			}
		} else {
			if (plugin.usingKits.containsKey(player)) {
				hasKit = true;
			}
		}
		return hasKit;
	}

	/**
	 * Get player's kit : Returns null if the player is null or doesn't have a kit.
	 *
	 * @param player
	 *            The player to get the kit name from.
	 */
	public static String getKit(Player player) {
		if (player == null) return null;
		return getKit(player.getName());
	}

	/**
	 * Get player's kit : Returns null if the player doesn't have a kit.
	 *
	 * @param player
	 *            The player to get the kit name from.
	 */
	public static String getKit(String player) {
		String kit = null;
		if (hasKit(player)) {
			kit = plugin.usingKits.get(player);
		}
		return kit;
	}

	/**
	 * Get players using a specific kit : Returns an empty list if the kit doesn't exist or no players are using that kit.
	 * 
	 * @param kitName
	 *            The kit to obtain the list of players.
	 */
	public static List<String> getPlayersUsingKit(String kitName) {
		List<String> playersUsingKit = new ArrayList<String>();
		List<String> playersInKitMap = new ArrayList<String>(plugin.usingKits.keySet());
		for (int pos = 0; pos < plugin.usingKits.size(); pos++) {
			String kit = plugin.usingKits.get(pos);
			if (kitName.equalsIgnoreCase(kit)) {
				playersUsingKit.add(playersInKitMap.get(pos));
			}
		}
		return playersUsingKit;
	}

	/**
	 * Get players using and their kits : Returns an empty map if no one is using a kit.
	 */
	public static Map<String, String> getPlayersAndKits() {
		return plugin.usingKits;
	}

	/**
	 * Returns if a kit exists.
	 * Note: Case sensitive.
	 * 
	 * @param kitName
	 *            The kit name to check.
	 */
	public static boolean kitExists(String kitName) {
		boolean kitExists = false;
		List<String> kitList = new ArrayList<String>();
		if (plugin.getKitsConfig().contains("Kits")) kitList = plugin.getKitsConfig().getStringList("Kits");
		List<String> kitListLC = new ArrayList<String>();
		for (int pos = 0; pos < kitList.size(); pos++)
			kitListLC.add(kitList.get(pos).toLowerCase());
		if (kitListLC.contains(kitName.toLowerCase())) kitExists = true;
		return kitExists;
	}

	/**
	 * Remove a player from a kit.
	 * Note: Doesn't clear the player's inventory.
	 * 
	 * @param player
	 *            The player to remove from a kit.
	 */
	public static void removePlayer(Player player) {
		if (player != null) removePlayer(player.getName());
	}

	/**
	 * Remove a player from a kit.
	 * Note: Doesn't clear the player's inventory.
	 * 
	 * @param player
	 *            The player's name to remove from a kit.
	 */
	public static void removePlayer(String player) {
		if (hasKit(player, false)) {
			plugin.usingKits.remove(player);
			plugin.playerKits.remove(player);
		}
	}

	/**
	 * Remove a player from a kit.
	 * Note: Clears a player's inventory.
	 * 
	 * @param pluginLogger
	 *            KingKit's logger.
	 * @param player
	 *            The player who's kit is to be changed.
	 * @param kit
	 *            The kit that the player should be set as.
	 */
	public static void setPlayerKit(Logger pluginLogger, String player, String kit) {
		boolean useSyso = false;
		if (pluginLogger == null) useSyso = true;
		if (kitExists(kit)) {
			Player target = Bukkit.getPlayerExact(player);
			if (target != null) {
				if (target.isOnline()) {
					if (hasKit(target.getName(), false)) {
						target.getInventory().clear();
						target.getInventory().setArmorContents(null);
						removePlayer(target.getName());
					}
					try {
						SetKit.setKitPlayerKit(plugin, target, kit);
					} catch (Exception ex) {
						String msg = "Error, couldn't set the player's kit to " + kit + ".";
						if (useSyso) System.out.println(msg);
						else pluginLogger.info(msg);
						String msg2 = "Error Log: \n" + ex.getMessage();
						if (useSyso) System.out.println(msg2);
						else pluginLogger.info(msg2);
					}
				} else {
					String msg = "Target player '" + player + "' is not online/does not exist.";
					if (useSyso) System.out.println(msg);
					else pluginLogger.info(msg);
				}
			} else {
				String msg = "Target player '" + player + "' is not online/does not exist.";
				if (useSyso) System.out.println(msg);
				else pluginLogger.info(msg);
			}
		} else {
			String msg = "Kit " + kit + " doesn't exist.";
			if (useSyso) System.out.println(msg);
			else pluginLogger.info(msg);
		}
	}

	/**
	 * Create a kit.
	 * Returns if the creation of the kit is successful.
	 * 
	 * @param kitName
	 *            The kit name.
	 * @param itemsInKit
	 *            The items in the kit.
	 * @param potionEffects
	 *            The potion effects in the kit. Set to null if there are none.
	 * @param guiItem
	 *            The item to be shown in the GUI Inventory when using GUI mode. Set to null if you want it to be a diamond sword.
	 * @param costOfKit
	 *            The cost of the kit.
	 */
	@SuppressWarnings("deprecation")
	public static boolean createKit(String kitName, List<ItemStack> itemsInKit, List<PotionEffect> potionEffects, ItemStack guiItem, double costOfKit) {
		boolean containsKit = plugin.getKitsConfig().contains(kitName);
		if (!itemsInKit.isEmpty()) {
			if (containsKit) {
				List<String> currentKits = plugin.getKitsConfig().getStringList("Kits");
				List<String> currentKitsLC = KingKits.toLowerCaseList(currentKits);
				if (currentKitsLC.contains(kitName.toLowerCase())) kitName = currentKits.get(currentKitsLC.indexOf(kitName.toLowerCase()));

				List<String> configItems = plugin.getKitsConfig().getStringList(kitName);
				boolean modifiedE = false;
				boolean modifiedL = false;
				boolean modifiedD = false;
				for (String itemInKit : configItems) {
					try {
						int itemID = Integer.parseInt(itemInKit.split(" ")[0]);
						if (plugin.getEnchantsConfig().contains(kitName + " " + itemID)) {
							plugin.getEnchantsConfig().set(kitName + " " + itemID, null);
							modifiedE = true;
						}
						if (plugin.getLoresConfig().contains(kitName + " " + itemID)) {
							plugin.getLoresConfig().set(kitName + " " + itemID, null);
							modifiedL = true;
						}
						if (plugin.getDyesConfig().contains(kitName + " " + itemID)) {
							plugin.getDyesConfig().set(kitName + " " + itemID, null);
							modifiedD = true;
						}
					} catch (Exception ex) {
						continue;
					}
				}
				plugin.getKitsConfig().set(kitName, null);
				plugin.saveKitsConfig();
				plugin.reloadKitsConfig();
				plugin.getGuiItemsConfig().set(kitName, null);
				plugin.saveGuiItemsConfig();
				plugin.reloadGuiItemsConfig();
				plugin.getCPKConfig().set(kitName, null);
				plugin.saveCPKConfig();
				plugin.reloadCPKConfig();
				if (modifiedE) {
					plugin.saveEnchantsConfig();
					plugin.reloadEnchantsConfig();
				}
				if (modifiedL) {
					plugin.saveLoresConfig();
					plugin.reloadLoresConfig();
				}
				if (modifiedD) {
					plugin.saveDyesConfig();
					plugin.reloadDyesConfig();
				}
				plugin.getPotionsConfig().set(kitName, null);
				plugin.savePotionsConfig();
				plugin.reloadPotionsConfig();
			}
			List<String> strItemsInKit = new ArrayList<String>();
			for (ItemStack itemInKit : itemsInKit) {
				if (itemInKit.hasItemMeta()) {
					if (itemInKit.getItemMeta().hasDisplayName()) strItemsInKit.add(itemInKit.getType().getId() + " " + itemInKit.getAmount() + " " + itemInKit.getDurability() + " " + itemInKit.getItemMeta().getDisplayName());
					else strItemsInKit.add(itemInKit.getType().getId() + " " + itemInKit.getAmount() + " " + itemInKit.getDurability());
				} else strItemsInKit.add(itemInKit.getType().getId() + " " + itemInKit.getAmount() + " " + itemInKit.getDurability());
				if (!itemInKit.getEnchantments().isEmpty()) {
					for (Entry<Enchantment, Integer> itemE : itemInKit.getEnchantments().entrySet()) {
						plugin.getEnchantsConfig().set(kitName + " " + itemInKit.getType().getId(), itemE.getKey().getName() + " " + itemE.getValue());
					}
					plugin.saveEnchantsConfig();
					plugin.reloadEnchantsConfig();
				}
				if (itemInKit.hasItemMeta()) {
					if (itemInKit.getItemMeta().hasLore()) {
						plugin.getLoresConfig().set(kitName + " " + itemInKit.getType().getId(), itemInKit.getItemMeta().getLore());
						plugin.saveLoresConfig();
						plugin.reloadLoresConfig();
					}
				}
				if (itemInKit.getType() == Material.LEATHER_HELMET || itemInKit.getType() == Material.LEATHER_CHESTPLATE || itemInKit.getType() == Material.LEATHER_LEGGINGS || itemInKit.getType() == Material.LEATHER_BOOTS) {
					try {
						if (itemInKit.hasItemMeta()) {
							if (itemInKit.getItemMeta() instanceof LeatherArmorMeta) {
								LeatherArmorMeta armorMeta = (LeatherArmorMeta) itemInKit.getItemMeta();
								if (armorMeta.getColor() != null) {
									plugin.getDyesConfig().set(kitName + " " + itemInKit.getType().getId(), armorMeta.getColor().asRGB());
									plugin.saveDyesConfig();
									plugin.reloadDyesConfig();
								}
							}
						}
					} catch (Exception ex) {
					}
				}
			}

			if (potionEffects != null) {
				if (!potionEffects.isEmpty()) {
					List<String> strPotionEffects = new ArrayList<String>();
					for (PotionEffect potionEffect : potionEffects) {
						strPotionEffects.add(potionEffect.getType().getName() + " " + (potionEffect.getDuration() / 20) + " " + potionEffect.getAmplifier());
					}
					plugin.getPotionsConfig().addDefault(kitName, strPotionEffects);
					plugin.savePotionsConfig();
					plugin.reloadPotionsConfig();
				}
			}

			if (guiItem != null) {
				if (guiItem.getType() != Material.AIR) plugin.getGuiItemsConfig().set(kitName, guiItem.getType().getId());
				else plugin.getGuiItemsConfig().set(kitName, Material.DIAMOND_SWORD.getId());
			} else plugin.getGuiItemsConfig().set(kitName, Material.DIAMOND_SWORD.getId());
			plugin.saveGuiItemsConfig();
			plugin.reloadGuiItemsConfig();

			if (costOfKit < 0D) costOfKit *= -1D;
			plugin.getCPKConfig().set(kitName, costOfKit);
			plugin.saveCPKConfig();
			plugin.reloadCPKConfig();

			List<String> nKitList = new ArrayList<String>();
			if (plugin.getKitsConfig().contains("Kits")) nKitList = plugin.getKitsConfig().getStringList("Kits");
			if (!containsKit) nKitList.add(kitName);
			plugin.getKitsConfig().set("Kits", nKitList);
			plugin.getKitsConfig().set(kitName, strItemsInKit);
			plugin.saveKitsConfig();
			plugin.reloadKitsConfig();

			try {
				plugin.getServer().getPluginManager().addPermission(new Permission("kingkits.kits." + kitName.toLowerCase()));
			} catch (Exception ex) {
			}
			return true;
		}
		return false;
	}

	/** 
	 * Delete a kit.
	 * Returns if the deletion of the kit is successful.
	 * 
	 * @param kitName
	 *            The name of the kit to be deleted.
	 */
	public static boolean deleteKit(String kitName) {
		List<String> kits = plugin.getKitsConfig().getStringList("Kits");
		List<String> kitsLC = new ArrayList<String>();
		for (String kit : kits)
			kitsLC.add(kit.toLowerCase());
		if (kitsLC.contains(kitName.toLowerCase())) {
			kitName = kits.get(kitsLC.indexOf(kitName.toLowerCase()));
			List<String> itemsInKit = plugin.getKitsConfig().getStringList(kitName);
			for (String item : itemsInKit) {
				try {
					String[] itemSplit = item.split(" ");
					int itemID = Integer.parseInt(itemSplit[0]);
					if (plugin.getEnchantsConfig().contains(kitName + " " + itemID)) {
						plugin.getEnchantsConfig().set(kitName + " " + itemID, null);
						plugin.saveEnchantsConfig();
						plugin.reloadEnchantsConfig();
					}
					if (plugin.getLoresConfig().contains(kitName + " " + itemID)) {
						plugin.getLoresConfig().set(kitName + " " + itemID, null);
						plugin.saveEnchantsConfig();
						plugin.reloadEnchantsConfig();
					}
					if (plugin.getDyesConfig().contains(kitName + " " + itemID)) {
						plugin.getDyesConfig().set(kitName + " " + itemID, null);
						plugin.saveDyesConfig();
						plugin.reloadDyesConfig();
					}
				} catch (Exception ex) {
					continue;
				}
			}
			List<String> kitList = plugin.getKitsConfig().getStringList("Kits");
			kitList.remove(kitName);
			plugin.getKitsConfig().set("Kits", kitList);
			plugin.getKitsConfig().set(kitName, null);
			plugin.saveKitsConfig();
			plugin.reloadKitsConfig();
			if (plugin.getPotionsConfig().contains(kitName)) {
				plugin.getPotionsConfig().set(kitName, null);
				plugin.savePotionsConfig();
				plugin.reloadKitsConfig();
			}
			if (plugin.getGuiItemsConfig().contains(kitName)) {
				plugin.getGuiItemsConfig().set(kitName, null);
				plugin.saveGuiItemsConfig();
				plugin.reloadGuiItemsConfig();
			}
			if (plugin.getCPKConfig().contains(kitName)) {
				plugin.getCPKConfig().set(kitName, null);
				plugin.saveCPKConfig();
				plugin.reloadCPKConfig();
			}
			return true;
		}
		return false;
	}
}