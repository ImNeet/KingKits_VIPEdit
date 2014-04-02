package me.faris.kingkits.listeners.commands;

import java.util.Arrays;
import java.util.List;

import me.faris.kingkits.KingKits;
import me.faris.kingkits.Language;
import me.faris.kingkits.helpers.ConfigCommand;
import me.faris.kingkits.listeners.KingCommand;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Scoreboard;

public class KingKitsCommand extends KingCommand {

	private List<ConfigCommand> configCommands = Arrays.asList(cc("Op", "Op bypass"), cc("KitsCMD", "Enable kits command"), cc("CreateKitsCMD", "Enable create kits command"), cc("DeleteKits", "Enable delete kits command"), cc("ListKits", "List kits on join"), cc("KitListMode", "Kit list mode"), cc("KitListPermission", "Use permissions on join"), cc("ListKitsPermission", "Use permissions for kit list"), cc("RemoveItems", "Remove items on leave"), cc("DropItemsDeath", "Drop items on death"), cc("DropItems", "Drop items"), cc("ClearInvReload", "Clear inventories on reload"), cc("OneKitPerLife", "One kit per life"), cc("UpdatesCheck", "Check for updates"), cc("AutomaticUpdates", "Automatically update"), cc("Score", "Enable score"), cc("ScorePerKill", "Score per kill"), cc("MaxScore", "Max score"), cc("RemovePotionEffects", "Remove potion effects on leave"), cc("Compass", "Set compass target to nearest player"), cc("QuickSoup", "Quick soup"), cc("KitRefill", "Requires kit to use refill"), cc("DisableBlockChanging", "Disable block placing and breaking"), cc("DisableDeathMsg", "Disable death messages"), cc("LockHunger", "Lock hunger level"), cc("DisableGM", "Disable gamemode while using a kit"), cc("Killstreaks", "Enable killstreaks"), cc("DisableItemBreaking", "Disable item breaking"), cc("KitMenu", "Kit menu on join"), cc("Scoreboards", "Scoreboards"), cc("ClearItemsOnKitCreation", "Clear items on kit creation"));

	public KingKitsCommand(KingKits pluginInstance) {
		super(pluginInstance);
	}

	@Override
	protected boolean onCommand(CommandSender sender, String command, String[] args) {
		if (command.equalsIgnoreCase("kingkits") || command.equalsIgnoreCase("kk")) {
			try {
				if (args.length == 0) {
					sender.sendMessage(ChatColor.GOLD + "KingKits v" + this.getPlugin().getDescription().getVersion());
				} else if (args.length > 0) {
					String strCommand = args[0];
					if (strCommand.equalsIgnoreCase("reload")) {
						if (sender.isOp()) {
							if (args.length == 1) {
								boolean usingScoreboards = this.getPlugin().configValues.scoreboards;
								this.getPlugin().reloadAllConfigs();
								this.getPlugin().loadConfiguration();

								sender.sendMessage(ChatColor.GOLD + "You reloaded KingKits configurations.");
								if (usingScoreboards && !this.getPlugin().configValues.scoreboards) {
									try {
										for (Player onlineP : sender.getServer().getOnlinePlayers()) {
											onlineP.setScoreboard(null);
										}
									} catch (Exception ex) {
									}
								}
							} else {
								sender.sendMessage(this.r(Language.CommandLanguage.usageMsg.replaceAll("<usage>", command.toLowerCase() + " " + strCommand.toLowerCase())));
							}
						} else {
							this.sendNoAccess(sender);
						}
					} else if (strCommand.equalsIgnoreCase("config")) {
						if (sender.hasPermission(this.getPlugin().permissions.cmdConfigManagement)) {
							if (args.length == 3) {
								String configKey = args[1];
								String configValue = args[2];
								if (this.containsCommand(this.configCommands, configKey)) {
									boolean usingScoreboards = this.getPlugin().configValues.scoreboards;
									sender.sendMessage(this.updateConfig("Config", configKey, configValue));
									if (usingScoreboards && !this.getPlugin().configValues.scoreboards) {
										try {
											for (Player onlineP : sender.getServer().getOnlinePlayers()) {
												onlineP.setScoreboard(null);
											}
										} catch (Exception ex) {
										}
									}
								} else {
									sender.sendMessage(ChatColor.RED + "Invalid config property: " + ChatColor.DARK_RED + configKey);
									sender.sendMessage(ChatColor.RED + "To list all the config properties you can edit type: " + ChatColor.DARK_RED + "/" + command.toLowerCase() + " " + strCommand.toLowerCase() + " list");
								}
							} else if (args.length == 2) {
								String configProperty = args[1];
								if (configProperty.equalsIgnoreCase("list")) {
									sender.sendMessage(ChatColor.GREEN + "KingKits config command property list (" + this.configCommands.size() + "): ");
									for (int i = 0; i < this.configCommands.size(); i++) {
										ConfigCommand configCommand = this.configCommands.get(i);
										sender.sendMessage(ChatColor.GOLD + configCommand.getCommand() + ChatColor.AQUA + " || " + ChatColor.DARK_RED + configCommand.getDescription());
									}
								} else {
									sender.sendMessage(ChatColor.RED + "Usage: " + ChatColor.DARK_RED + "/" + command.toLowerCase() + " " + strCommand.toLowerCase() + " <property> <value>");
								}
							} else {
								sender.sendMessage(this.r(Language.CommandLanguage.usageMsg.replaceAll("<usage>", command.toLowerCase() + " " + strCommand.toLowerCase() + " <property> <value>")));
							}
						} else {
							this.sendNoAccess(sender);
						}
					} else if (strCommand.equalsIgnoreCase("resetscoreboards")) {
						if (sender.isOp()) {
							if (args.length == 0) {
								if (!this.getPlugin().configValues.scoreboards) {
									Scoreboard newScoreboard = sender.getServer().getScoreboardManager().getNewScoreboard();
									for (Player onlinePlayer : sender.getServer().getOnlinePlayers()) {
										Scoreboard pScoreboard = onlinePlayer.getScoreboard();
										if (pScoreboard != null && pScoreboard.getObjective(DisplaySlot.SIDEBAR) != null) {
											String boardTitle = ChatColor.stripColor(pScoreboard.getObjective(DisplaySlot.SIDEBAR).getDisplayName());
											if (boardTitle != null && boardTitle.equalsIgnoreCase("KingKits")) onlinePlayer.setScoreboard(newScoreboard);
										}
									}
									sender.sendMessage(ChatColor.GOLD + "Reset all scoreboards.");
								} else {
									sender.sendMessage(ChatColor.RED + "Scoreboards are enabled.");
								}
							} else {
								sender.sendMessage(this.r(Language.CommandLanguage.usageMsg.replaceAll("<usage>", command.toLowerCase() + " " + strCommand.toLowerCase())));
							}
						} else {
							this.sendNoAccess(sender);
						}
					} else {
						sender.sendMessage(ChatColor.RED + "Unknown KingKits command: " + ChatColor.DARK_RED + strCommand);
					}
				}
			} catch (Exception ex) {
				if (Math.random() < 0.25) ex.printStackTrace();
			}
			return true;
		}
		return false;
	}

	/** Returns a new ConfigCommand object **/
	private static ConfigCommand cc(String name, String description) {
		return new ConfigCommand(name, description);
	}

	/** Returns if a list of config comamnds contains a command (as a string) **/
	private boolean containsCommand(List<ConfigCommand> configCmds, String command) {
		for (ConfigCommand cmd : configCmds) {
			if (cmd.getCommand().equalsIgnoreCase(command)) return true;
		}
		return false;
	}

	/** Updates the config with the property key and value **/
	public String updateConfig(String config, String propertyKey, Object propertyValue) {
		try {
			String key = "";
			if (this.containsCommand(this.configCommands, propertyKey)) {
				for (int i = 0; i < this.configCommands.size(); i++) {
					if (this.configCommands.get(i).getCommand().equalsIgnoreCase(propertyKey)) key = this.configCommands.get(i).getDescription();
				}
				if (key == "") return ChatColor.RED + "Failed to find the key '" + propertyKey + "' in the config.";
			} else return ChatColor.RED + "Failed to find the key '" + propertyKey + "' in the config.";
			if (config.equalsIgnoreCase("Config")) {
				String value = String.valueOf(propertyValue);
				if (this.isBoolean(value)) {
					this.getPlugin().getConfig().set(key, Boolean.parseBoolean(value));
				} else if (this.isDouble(value)) {
					this.getPlugin().getConfig().set(key, Double.parseDouble(value));
				} else if (this.isNumeric(value)) {
					this.getPlugin().getConfig().set(key, Integer.parseInt(value));
				} else {
					this.getPlugin().getConfig().set(key, propertyValue);
				}
				this.getPlugin().saveConfig();
			} else if (config.equalsIgnoreCase("Economy")) {
				String value = String.valueOf(propertyValue);
				if (this.isBoolean(value)) {
					this.getPlugin().getEconomyConfig().set(key, Boolean.parseBoolean(value));
				} else if (this.isDouble(value)) {
					this.getPlugin().getEconomyConfig().set(key, Double.parseDouble(value));
				} else if (this.isNumeric(value)) {
					this.getPlugin().getEconomyConfig().set(key, Integer.parseInt(value));
				} else {
					this.getPlugin().getEconomyConfig().set(key, propertyValue);
				}
				this.getPlugin().saveEconomyConfig();
			}
			if (!this.getPlugin().checkConfig()) return ChatColor.RED + "Could not update " + propertyKey + ".";
			this.getPlugin().reloadAllConfigs();
			this.getPlugin().loadConfiguration();

			return ChatColor.GOLD + "Successfully updated " + propertyKey + " in the config.";
		} catch (Exception ex) {
			if (Math.random() < 0.25) ex.printStackTrace();
			return ChatColor.RED + "Error: Couldn't update the config with the property.";
		}
	}

}
