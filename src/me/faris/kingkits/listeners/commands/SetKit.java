package me.faris.kingkits.listeners.commands;

import java.util.ArrayList;
import java.util.List;

import me.faris.kingkits.KingKits;
import me.faris.kingkits.listeners.event.custom.PlayerKitEvent;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SetKit {
	private static KingKits pl;

	@SuppressWarnings("deprecation")
	public static void setKitPlayerKit(KingKits plugin, Player p, String kitName) throws Exception {
		if (plugin == null | p == null || kitName == null) return;

		pl = plugin;
		if (plugin.configValues.pvpWorlds.contains("All") || plugin.configValues.pvpWorlds.contains(p.getWorld().getName())) {
			if (plugin.getKitsConfig().contains("Kits")) {
				List<String> kitList = plugin.getKitsConfig().getStringList("Kits");
				List<String> kitListLC = new ArrayList<String>();
				for (int pos0 = 0; pos0 < kitList.size(); pos0++) {
					kitListLC.add(kitList.get(pos0).toLowerCase());
				}
				if (kitListLC.contains(kitName.toLowerCase())) {
					try {
						kitName = kitList.get(kitList.indexOf(kitName));
					} catch (Exception ex) {
						try {
							kitName = kitName.substring(0, 0).toUpperCase() + kitName.substring(1);
						} catch (Exception ex2) {
						}
					}
					if (p.hasPermission("kingkits.kits." + kitName.toLowerCase())) {
						if (plugin.configValues.oneKitPerLife) {
							if (plugin.configValues.opBypass) {
								if (!p.isOp()) {
									if (plugin.playerKits.containsKey(p.getName())) {
										p.sendMessage(r("&6You have already chosen a kit!"));
										return;
									}
								}
							} else {
								if (plugin.usingKits.containsKey(p.getName())) {
									p.sendMessage(r("&6You have already chosen a kit!"));
									return;
								}
							}
						}
						if (plugin.configValues.vaultValues.useEconomy && plugin.configValues.vaultValues.useCostPerKit) {
							if(!p.hasPermission("kingkits.kit.vipbypass")) {
								try {
									net.milkbowl.vault.economy.Economy economy = (net.milkbowl.vault.economy.Economy) plugin.vault.getEconomy();
									double kitCost = 0D;
									if (plugin.getCPKConfig().contains(kitName)) kitCost = plugin.getCPKConfig().getDouble(kitName);
									else kitCost = plugin.configValues.vaultValues.costPerKit;
									if (kitCost < 0) kitCost *= -1;
									if (economy.hasAccount(p.getName())) {
										if (economy.getBalance(p.getName()) >= kitCost) {
											economy.withdrawPlayer(p.getName(), kitCost);
											if (kitCost != 0) p.sendMessage(ChatColor.GREEN + plugin.getEconomyMessage(kitCost));
										} else {
											p.sendMessage(ChatColor.GREEN + "You do not have enough money to change kits.");
											return;
										}
									} else {
										p.sendMessage(ChatColor.GREEN + "You do not have enough money to change kits.");
										return;
									}
								} catch (Exception ex) {
								}
							}
						}
						String oldKit = plugin.playerKits.containsKey(p.getName()) ? plugin.playerKits.get(p.getName()) : null;
						p.getInventory().clear();
						p.getInventory().setArmorContents(null);
						p.setGameMode(GameMode.SURVIVAL);
						for (PotionEffect potionEffect : p.getActivePotionEffects())
							p.removePotionEffect(potionEffect.getType());
						List<String> itemsL = plugin.getKitsConfig().getStringList(kitName);
						for (int pos = 0; pos < itemsL.size(); pos++) {
							String itemInL = itemsL.get(pos);
							String[] split = itemInL.split(" ");
							String strItemID = "";
							String strAmount = "";
							String strDataVal = "";
							String strItemName = "";
							if (split.length == 0) continue;
							if (split.length == 1) continue;
							if (split.length > 2) {
								strItemID = split[0];
								strAmount = split[1];
								strDataVal = split[2];
							}
							if (split.length > 3) {
								for (int pos2 = 3; pos2 < split.length; pos2++) {
									try {
										if (pos2 == split.length - 1) strItemName += split[pos2];
										else strItemName += split[pos2] + " ";
									} catch (Exception ex) {
										break;
									}
								}
							}
							int itemID = 0;
							int amount = 0;
							short dataVal = 0;
							if (isNumeric(strItemID)) {
								itemID = Integer.parseInt(strItemID);
							} else {
								continue;
							}
							if (isNumeric(strAmount)) {
								amount = Integer.parseInt(strAmount);
							} else {
								amount = 1;
							}
							if (isShort(strDataVal)) {
								dataVal = Short.valueOf(strDataVal);
							} else {
								dataVal = 0;
							}
							ItemStack itemToGive = null;
							try {
								itemToGive = new ItemStack(itemID, amount);
								if (dataVal != 0) itemToGive.setDurability(dataVal);
							} catch (Exception ex) {
								continue;
							}
							if (strItemName != "") {
								if (itemToGive.getItemMeta() != null) {
									ItemMeta itemMeta = itemToGive.getItemMeta();
									itemMeta.setDisplayName(strItemName);
									itemToGive.setItemMeta(itemMeta);
								}
							}
							if (plugin.getEnchantsConfig().contains(kitName + " " + itemToGive.getType().getId())) {
								List<String> lEnchantments = plugin.getEnchantsConfig().getStringList(kitName + " " + itemToGive.getType().getId());
								for (int pos2 = 0; pos2 < lEnchantments.size(); pos2++) {
									String enchantmentP = lEnchantments.get(pos2);
									String[] eSplit = enchantmentP.split(" ");
									String strEnchantment = "";
									String strLevel = "";
									int level = 1;
									if (eSplit.length > 0) {
										strEnchantment = eSplit[0];
									}
									if (eSplit.length > 1) {
										strLevel = eSplit[1];
									}
									try {
										level = Integer.parseInt(strLevel);
									} catch (Exception ex) {
										level = 1;
									}
									Enchantment eToAdd = Enchantment.getByName(strEnchantment);
									if (eToAdd != null) itemToGive.addUnsafeEnchantment(eToAdd, level);
									else continue;
								}
							}
							if (plugin.getLoresConfig().contains(kitName + " " + itemToGive.getType().getId())) {
								List<String> itemLores = plugin.getLoresConfig().getStringList(kitName + " " + itemToGive.getType().getId());
								if (itemToGive.getItemMeta() != null) {
									ItemMeta itemMeta = itemToGive.getItemMeta();
									itemMeta.setLore(itemLores);
									itemToGive.setItemMeta(itemMeta);
								}
							}
							if (plugin.getDyesConfig().contains(kitName + " " + itemToGive.getType().getId())) {
								try {
									if (itemToGive.getItemMeta() != null) {
										if (itemToGive.getItemMeta() instanceof LeatherArmorMeta) {
											LeatherArmorMeta armorMeta = (LeatherArmorMeta) itemToGive.getItemMeta();
											if (armorMeta.getColor() != null) {
												int itemRGB = plugin.getDyesConfig().getInt(kitName + " " + itemToGive.getType().getId());
												armorMeta.setColor(Color.fromRGB(itemRGB));
												itemToGive.setItemMeta(armorMeta);
											}
										}
									}
								} catch (Exception ex) {
								}
							}
							if (itemToGive.getType() == Material.DIAMOND_HELMET || itemToGive.getType() == Material.IRON_HELMET || itemToGive.getType() == Material.CHAINMAIL_HELMET || itemToGive.getType() == Material.GOLD_HELMET || itemToGive.getType() == Material.LEATHER_HELMET || itemToGive.getType() == Material.SKULL_ITEM) {
								p.getInventory().setHelmet(itemToGive);
							} else if (itemToGive.getType() == Material.DIAMOND_CHESTPLATE || itemToGive.getType() == Material.IRON_CHESTPLATE || itemToGive.getType() == Material.CHAINMAIL_CHESTPLATE || itemToGive.getType() == Material.GOLD_CHESTPLATE || itemToGive.getType() == Material.LEATHER_CHESTPLATE) {
								p.getInventory().setChestplate(itemToGive);
							} else if (itemToGive.getType() == Material.DIAMOND_LEGGINGS || itemToGive.getType() == Material.IRON_LEGGINGS || itemToGive.getType() == Material.CHAINMAIL_LEGGINGS || itemToGive.getType() == Material.GOLD_LEGGINGS || itemToGive.getType() == Material.LEATHER_LEGGINGS) {
								p.getInventory().setLeggings(itemToGive);
							} else if (itemToGive.getType() == Material.DIAMOND_BOOTS || itemToGive.getType() == Material.IRON_BOOTS || itemToGive.getType() == Material.CHAINMAIL_BOOTS || itemToGive.getType() == Material.GOLD_BOOTS || itemToGive.getType() == Material.LEATHER_BOOTS) {
								p.getInventory().setBoots(itemToGive);
							} else {
								p.getInventory().addItem(itemToGive);
							}
						}
						if (plugin.getPotionsConfig().contains(kitName)) {
							List<String> strPotions = plugin.getPotionsConfig().getStringList(kitName);
							for (int pos0 = 0; pos0 < strPotions.size(); pos0++) {
								String strPotionName = strPotions.get(pos0);
								String strPotion = "";
								String strTime = "";
								String strAmplifier = "";
								String[] split = strPotionName.split(" ");
								int time = 0;
								int amplifier = 0;
								if (split.length > 0) {
									strPotion = split[0];
								} else {
									continue;
								}
								if (split.length > 1) {
									strAmplifier = split[1];
									if (isNumeric(strAmplifier)) {
										amplifier = Integer.parseInt(strAmplifier);
									} else {
										if (strAmplifier.equalsIgnoreCase("I")) amplifier = 0;
										else if (strAmplifier.equalsIgnoreCase("II")) amplifier = 1;
										else if (strAmplifier.equalsIgnoreCase("III")) amplifier = 2;
										else if (strAmplifier.equalsIgnoreCase("IV")) amplifier = 3;
										else if (strAmplifier.equalsIgnoreCase("V")) amplifier = 4;
										else if (strAmplifier.equalsIgnoreCase("VI")) amplifier = 5;
										else if (strAmplifier.equalsIgnoreCase("VII")) amplifier = 6;
										else if (strAmplifier.equalsIgnoreCase("VIII")) amplifier = 7;
										else if (strAmplifier.equalsIgnoreCase("X")) amplifier = 8;
										else amplifier = 0;
									}
								} else {
									strAmplifier = "0";
									amplifier = 0;
								}
								if (split.length > 2) {
									strTime = split[2];
									if (isNumeric(strTime)) time = Integer.parseInt(strTime);
									else time = 300;
								} else {
									time = 300;
								}
								PotionEffectType pet = null;
								if (isNumeric(strPotion)) {
									if (PotionEffectType.getById(Integer.parseInt(strPotion)) != null) {
										pet = PotionEffectType.getById(Integer.parseInt(strPotion));
									}
								} else {
									if (PotionEffectType.getByName(strPotion) != null) {
										pet = PotionEffectType.getByName(strPotion);
									}
								}
								if (pet != null) {
									if (amplifier < 0) amplifier *= -1;
									p.addPotionEffect(new PotionEffect(pet, time * 20, amplifier));
								}
							}
						}
						p.updateInventory();
						if (plugin.configValues.commandToRun.length() > 0) {
							String cmdToRun = plugin.configValues.commandToRun;
							cmdToRun = cmdToRun.replaceAll("<kit>", kitName);
							cmdToRun = cmdToRun.replaceAll("<player>", p.getName());
							p.getServer().dispatchCommand(p.getServer().getConsoleSender(), cmdToRun);
						}
						plugin.playerKits.remove(p.getName());
						plugin.usingKits.remove(p.getName());
						plugin.hasKit.remove(p.getName());
						if (plugin.configValues.opBypass) {
							if (!p.isOp()) {
								plugin.playerKits.put(p.getName(), kitName);
							}
						} else {
							plugin.playerKits.put(p.getName(), kitName);
						}
						plugin.usingKits.put(p.getName(), kitName);
						plugin.hasKit.put(p.getName(), kitName);
						if (plugin.configValues.customMessages != "" && plugin.configValues.customMessages != "''") p.sendMessage(r(plugin.configValues.customMessages).replaceAll("<kit>", kitName));
						p.getServer().getPluginManager().callEvent(new PlayerKitEvent(p, kitName, oldKit));
					} else {
						p.sendMessage(r("&cYou do not have permission to use the kit &4" + kitName + "&c."));
					}
				} else {
					p.sendMessage(r("&4" + kitName + " &6does not exist."));
				}
			} else {
				p.sendMessage(r("&4" + kitName + " &6does not exist."));
			}
		}
	}

	private static String r(String val) {
		return pl.replaceAllColours(val);
	}

	private static boolean isNumeric(String val) {
		try {
			@SuppressWarnings("unused")
			int i = Integer.parseInt(val);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	private static boolean isShort(String val) {
		try {
			@SuppressWarnings("unused")
			short i = Short.parseShort(val);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

}
