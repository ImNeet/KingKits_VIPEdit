package me.faris.kingkits.listeners.commands;

import java.util.ArrayList;
import java.util.List;

import me.faris.kingkits.KingKits;
import me.faris.kingkits.Language;
import me.faris.kingkits.guis.GuiKitMenu;
import me.faris.kingkits.helpers.KitStack;
import me.faris.kingkits.listeners.PlayerCommand;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class KitCommand extends PlayerCommand {

	public KitCommand(KingKits instance) {
		super(instance);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected boolean onCommand(Player p, String command, String[] args) {
		if (command.equalsIgnoreCase("pvpkit") || command.equalsIgnoreCase("kit") || command.equalsIgnoreCase("bim")) {
			if (p.hasPermission(this.getPlugin().permissions.kitUseCommand)) {
				if (this.getPlugin().cmdValues.pvpKits) {
					if (this.getPlugin().configValues.pvpWorlds.contains("All") || this.getPlugin().configValues.pvpWorlds.contains(p.getWorld().getName())) {
						if (args.length == 0) {
							if (!this.getPlugin().configValues.kitListMode.equalsIgnoreCase("Gui") && !this.getPlugin().configValues.kitListMode.equalsIgnoreCase("Menu")) {
								List<String> kitList = new ArrayList<String>();
								if (this.getPlugin().getKitsConfig().contains("Kits")) {
									kitList = this.getPlugin().getKitsConfig().getStringList("Kits");
								}
								p.sendMessage(r("&aKits List (" + kitList.size() + "):"));
								if (!kitList.isEmpty()) {
									for (int kitPos = 0; kitPos < kitList.size(); kitPos++) {
										String kitName = kitList.get(kitPos).split(" ")[0];
										if (p.hasPermission("kingkits.kits." + kitName.toLowerCase())) {
											p.sendMessage(r("&6" + (kitPos + 1) + ". " + kitName));
										} else {
											if (this.getPlugin().configValues.cmdKitListPermissions) p.sendMessage(r("&4" + (kitPos + 1) + ". " + kitName));
										}
									}
								} else {
									p.sendMessage(r("&4There are no kits"));
								}
							} else {
								if (!GuiKitMenu.playerMenus.contains(p.getName())) {
									List<String> kitList = new ArrayList<String>();
									if (this.getPlugin().getKitsConfig().contains("Kits")) kitList = this.getPlugin().getKitsConfig().getStringList("Kits");
									KitStack[] kitStacks = new KitStack[kitList.size()];
									boolean modifiedConfig = false;
									for (int i = 0; i < kitList.size(); i++) {
										String kitName = kitList.get(i);
										try {
											if (kitName.contains(" ")) {
												kitName = kitName.split(" ")[0];
											}
										} catch (Exception ex) {
										}
										try {
											ItemStack itemStack = new ItemStack(Material.DIAMOND_SWORD, 1);
											if (this.getPlugin().getGuiItemsConfig().contains(kitName)) {
												String guiItemSplit[] = this.getPlugin().getGuiItemsConfig().getString(kitName).split(" ");
												if (guiItemSplit.length > 1) {
													try {
														itemStack = new ItemStack(Integer.parseInt(guiItemSplit[0]), 1);
														itemStack.setDurability(Short.parseShort(guiItemSplit[1]));
													} catch (Exception ex) {
														continue;
													}
												} else itemStack = new ItemStack(Integer.parseInt(guiItemSplit[0]), 1);
											} else {
												this.getPlugin().getGuiItemsConfig().set(kitName, itemStack.getType().getId());
												modifiedConfig = true;
											}
											kitStacks[i] = new KitStack(kitName, itemStack);
										} catch (Exception ex) {
											this.getPlugin().getGuiItemsConfig().set(kitName, Material.DIAMOND_SWORD.getId());
											modifiedConfig = true;
											continue;
										}
									}
									if (modifiedConfig) {
										this.getPlugin().saveGuiItemsConfig();
										this.getPlugin().reloadGuiItemsConfig();
									}
									ChatColor menuColour = !kitList.isEmpty() ? ChatColor.DARK_BLUE : ChatColor.RED;
									new GuiKitMenu(p, menuColour + "Choisis tes BIMs !", kitStacks).openMenu();
								}
							}
						} else if (args.length == 1) {
							String kitName = args[0];
							List<String> kitList = this.getPlugin().getKitsConfig().getStringList("Kits");
							List<String> kitListLC = new ArrayList<String>();
							for (int pos0 = 0; pos0 < kitList.size(); pos0++) {
								kitListLC.add(kitList.get(pos0).toLowerCase());
							}
							if (kitListLC.contains(kitName.toLowerCase())) {
								kitName = kitList.get(kitListLC.indexOf(kitName.toLowerCase()));
							}
							try {
								SetKit.setKitPlayerKit(this.getPlugin(), p, kitName);
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						} else {
							p.sendMessage(this.r(Language.CommandLanguage.usageMsg.replaceAll("<usage>", command.toLowerCase() + " [<kit>]")));
						}
					} else {
						p.sendMessage(ChatColor.RED + "You cannot use this command in this world.");
					}
				} else {
					p.sendMessage(ChatColor.RED + "This command is disabled in the configuration.");
				}
			} else {
				this.sendNoAccess(p);
			}
			return true;
		}
		return false;
	}

}
