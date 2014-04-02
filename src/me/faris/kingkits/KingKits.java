package me.faris.kingkits;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.faris.kingkits.Updater.UpdateResult;
import me.faris.kingkits.Updater.UpdateType;
import me.faris.kingkits.hooks.Plugin;
import me.faris.kingkits.hooks.PvPKits;
import me.faris.kingkits.listeners.commands.CreateKitCommand;
import me.faris.kingkits.listeners.commands.DeleteKitCommand;
import me.faris.kingkits.listeners.commands.KingKitsCommand;
import me.faris.kingkits.listeners.commands.KitCommand;
import me.faris.kingkits.listeners.commands.RefillCommand;
import me.faris.kingkits.listeners.commands.RenameKitCommand;
import me.faris.kingkits.listeners.event.PlayerListener;
import me.faris.kingkits.values.CommandValues;
import me.faris.kingkits.values.ConfigValues;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

@SuppressWarnings({ "unused", "deprecation" })
public class KingKits extends JavaPlugin {
	// Class Variables
	private KingKits plugin;
	private Plugin pvpPlugin = new Plugin(this);
	private PvPKits pvpKits = new PvPKits(this);
	private Updater updater = null;
	public Permissions permissions = new Permissions();
	public CommandValues cmdValues = new CommandValues();
	public ConfigValues configValues = new ConfigValues();
	public Vault vault = new Vault(this);

	// Plugin Variables
	private Logger logger;
	public Map<String, String> usingKits = new HashMap<String, String>();
	public Map<String, String> playerKits = new HashMap<String, String>();
	public Map<String, String> hasKit = new HashMap<String, String>();
	public Map<String, Object> playerScores = new HashMap<String, Object>();
	public Map<Player, Player> compassTargets = new HashMap<Player, Player>();
	public Map<String, Long> playerKillstreaks = new HashMap<String, Long>();

	// Listeners
	private PlayerListener pListener = new PlayerListener(this);
	private KingKitsCommand cmdKingKits = new KingKitsCommand(this);
	private KitCommand cmdKitL = new KitCommand(this);
	private CreateKitCommand cmdKitC = new CreateKitCommand(this);
	private DeleteKitCommand cmdKitD = new DeleteKitCommand(this);
	private RenameKitCommand cmdKitR = new RenameKitCommand(this);
	private RefillCommand cmdRefill = new RefillCommand(this);

	public void onEnable() {
		// Clear all lists
		this.usingKits.clear();
		this.playerKits.clear();
		this.hasKit.clear();
		this.playerScores.clear();
		this.compassTargets.clear();
		this.playerKillstreaks.clear();

		// Initialise variables
		this.plugin = this;
		this.logger = this.getLogger();
		this.logger.info(this.getDescription().getFullName());
		this.loadConfiguration();

		// Update commands
		if (this.cmdKingKits == null) this.cmdKingKits = new KingKitsCommand(this);
		if (this.cmdKitL == null) this.cmdKitL = new KitCommand(this);
		if (this.cmdKitC == null) this.cmdKitC = new CreateKitCommand(this);
		if (this.cmdKitD == null) this.cmdKitD = new DeleteKitCommand(this);
		if (this.cmdRefill == null) this.cmdRefill = new RefillCommand(this);
		if (this.cmdKitR == null) this.cmdKitR = new RenameKitCommand(this);

		// Register commands
		this.getCommand("kingkits").setExecutor(this.cmdKingKits);
		this.getCommand("kingkits").setAliases(Arrays.asList("kk"));
		this.getCommand("pvpkit").setExecutor(this.cmdKitL);
		this.getCommand("createkit").setExecutor(this.cmdKitC);
		this.getCommand("deletekit").setExecutor(this.cmdKitD);
		this.getCommand("refill").setExecutor(this.cmdRefill);
		this.getCommand("refill").setAliases(Arrays.asList("soup"));
		this.getCommand("renamekit").setExecutor(this.cmdKitR);

		// Register permissions
		for (Permission registeredPerm : this.permissions.permissionsList)
			this.getServer().getPluginManager().addPermission(registeredPerm);
		this.getServer().getPluginManager().registerEvents(this.pListener, this);

		// Check for updates
		if (this.configValues.checkForUpdates) {
			this.updater = new Updater(this, 56371, this.getFile(), Updater.UpdateType.NO_DOWNLOAD, false);
			if (this.updater.getResult() == UpdateResult.UPDATE_AVAILABLE) {
				String title = "============================================";
				String titleSpace = "                                            ";
				this.logger.info(title);
				try {
					this.logger.info(titleSpace.substring(0, titleSpace.length() / 2 - "KingKits".length() + 3) + "KingKits" + titleSpace.substring(0, titleSpace.length() / 2 - "KingKits".length()));
				} catch (Exception ex) {
					this.logger.info("KingKits");
				}
				this.logger.info(title);
				this.logger.info("A new version is available: " + this.updater.getLatestName());
				this.logger.info("Your current version: KingKits v" + this.getDescription().getVersion());
				if (this.configValues.automaticUpdates) {
					this.logger.info("Downloading " + this.updater.getLatestName() + "...");
					this.updater = new Updater(this, 56371, this.getFile(), UpdateType.NO_VERSION_CHECK, false);
					UpdateResult updateResult = this.updater.getResult();
					if (updateResult == UpdateResult.FAIL_APIKEY) this.logger.info("Download failed: Improperly configured the server's API key in the configuration");
					else if (updateResult == UpdateResult.FAIL_DBO) this.logger.info("Download failed: Could not connect to BukkitDev.");
					else if (updateResult == UpdateResult.FAIL_DOWNLOAD) this.logger.info("Download failed: Could not download the file.");
					else if (updateResult == UpdateResult.FAIL_NOVERSION) this.logger.info("Download failed: The latest version has an incorrect title.");
					else this.logger.info("The latest version of KingKits has been downloaded.");
				} else {
					this.logger.info("Download it from: " + this.updater.getLatestFileLink());
				}
			}
		}
	}

	public void onDisable() {
		// Initialise variables
		this.plugin = this;
		this.logger = this.getLogger();

		// Clear inventories on reload
		if (this.configValues.clearInvsOnReload) {
			for (int pos = 0; pos < this.getServer().getOnlinePlayers().length; pos++) {
				Player target = this.getServer().getOnlinePlayers()[pos];
				if (target != null) {
					if (PvPKits.hasKit(target.getName(), false)) {
						target.getInventory().clear();
						target.getInventory().setArmorContents(null);
					}
				}
			}
		}

		// Clear all lists
		this.usingKits.clear();
		this.playerKits.clear();
		this.hasKit.clear();
		this.playerScores.clear();
		this.compassTargets.clear();
		this.playerKillstreaks.clear();

		// Unregister all permissions
		for (Permission registeredPerm : this.permissions.permissionsList)
			this.getServer().getPluginManager().removePermission(registeredPerm);

		this.logger.info(this.getDescription().getFullName() + " by KingFaris10 is now disabled.");
	}

	// Load Configurations
	public void loadConfiguration() {
		try {
			this.getConfig().options().header("KingKits" + " Configuration");
			this.getConfig().addDefault("Op bypass", true);
			this.getConfig().addDefault("PvP Worlds", Arrays.asList("All"));
			this.getConfig().addDefault("Enable kits command", true);
			this.getConfig().addDefault("Enable create kits command", true);
			this.getConfig().addDefault("Enable delete kits command", true);
			this.getConfig().addDefault("Enable rename kits command", true);
			this.getConfig().addDefault("Enable refill command", true);
			this.getConfig().addDefault("List kits on join", true);
			this.getConfig().addDefault("Use permissions on join", true);
			this.getConfig().addDefault("Use permissions for kit list", true);
			this.getConfig().addDefault("Kit list mode", "Text");
			this.getConfig().addDefault("Remove items on leave", true);
			this.getConfig().addDefault("Drop items on death", false);
			this.getConfig().addDefault("Drop items", false);
			this.getConfig().addDefault("Drop animations", Arrays.asList(Material.BOWL.getId()));
			this.getConfig().addDefault("Clear inventories on reload", true);
			this.getConfig().addDefault("One kit per life", false);
			this.getConfig().addDefault("Check for updates", true);
			this.getConfig().addDefault("Automatically update", false);
			this.getConfig().addDefault("Enable score", true);
			this.getConfig().addDefault("Score chat prefix", "&6[&a<score>&6]");
			this.getConfig().addDefault("Score per kill", 2);
			this.getConfig().addDefault("Max score", Integer.MAX_VALUE);
			this.getConfig().addDefault("Remove potion effects on leave", true);
			this.getConfig().addDefault("Set compass target to nearest player", true);
			this.getConfig().addDefault("Quick soup", true);
			this.getConfig().addDefault("Requires kit to use refill", true);
			this.getConfig().addDefault("Command to run when changing kits", "");
			this.getConfig().addDefault("Disable block placing and breaking", false);
			this.getConfig().addDefault("Disable death messages", false);
			this.getConfig().addDefault("Lock hunger level", true);
			this.getConfig().addDefault("Custom message", "&6You have chosen the kit &c<kit>&6.");
			this.getConfig().addDefault("Disable gamemode while using a kit", false);
			this.getConfig().addDefault("Enable killstreaks", false);
			this.getConfig().addDefault("Disable item breaking", true);
			this.getConfig().addDefault("Kit menu on join", false);
			this.getConfig().addDefault("Scoreboards", true);
			this.getConfig().addDefault("Clear items on kit creation", true);
			this.getConfig().options().copyDefaults(true);
			this.getConfig().options().copyHeader(true);
			this.saveConfig();

			this.configValues.opBypass = this.getConfig().getBoolean("Op bypass");
			this.configValues.pvpWorlds = this.getConfig().getStringList("PvP Worlds");
			this.cmdValues.pvpKits = this.getConfig().getBoolean("Enable kits command");
			this.cmdValues.createKits = this.getConfig().getBoolean("Enable create kits command");
			this.cmdValues.deleteKits = this.getConfig().getBoolean("Enable delete kits command");
			this.cmdValues.renameKits = this.getConfig().getBoolean("Enable rename kits command");
			this.cmdValues.refillKits = this.getConfig().getBoolean("Enable refill command");
			this.configValues.listKitsOnJoin = this.getConfig().getBoolean("List kits on join");
			this.configValues.kitListMode = this.getConfig().getString("Kit list mode");
			this.configValues.kitListPermissions = this.getConfig().getBoolean("Use permissions on join");
			this.configValues.cmdKitListPermissions = this.getConfig().getBoolean("Use permissions for kit list");
			this.configValues.removeItemsOnLeave = this.getConfig().getBoolean("Remove items on leave");
			this.configValues.dropItemsOnDeath = this.getConfig().getBoolean("Drop items on death");
			this.configValues.dropItems = this.getConfig().getBoolean("Drop items");
			this.configValues.dropAnimations = this.getConfig().getIntegerList("Drop animations");
			this.configValues.clearInvsOnReload = this.getConfig().getBoolean("Clear inventories on reload");
			this.configValues.oneKitPerLife = this.getConfig().getBoolean("One kit per life");
			this.configValues.checkForUpdates = this.getConfig().getBoolean("Check for updates");
			this.configValues.automaticUpdates = this.getConfig().getBoolean("Automatically update");
			this.configValues.removePotionEffectsOnLeave = this.getConfig().getBoolean("Remove potion effects on leave");
			this.configValues.rightClickCompass = this.getConfig().getBoolean("Set compass target to nearest player");
			this.configValues.quickSoup = this.getConfig().getBoolean("Quick soup");
			this.configValues.quickSoupKitOnly = this.getConfig().getBoolean("Requires kit to use refill");
			this.configValues.banBlockBreakingAndPlacing = this.getConfig().getBoolean("Disable block placing and breaking");
			this.configValues.disableDeathMessages = this.getConfig().getBoolean("Disable death messages");
			this.configValues.lockHunger = this.getConfig().getBoolean("Lock hunger level");
			this.configValues.customMessages = this.getConfig().getString("Custom message");
			this.configValues.commandToRun = this.getConfig().getString("Command to run when changing kits");
			this.configValues.disableGamemode = this.getConfig().getBoolean("Disable gamemode while using a kit");
			this.configValues.killstreaks = this.getConfig().getBoolean("Enable killstreaks");
			this.configValues.disableItemBreaking = this.getConfig().getBoolean("Disable item breaking");
			this.configValues.kitMenuOnJoin = this.getConfig().getBoolean("Kit menu on join");
			this.configValues.scoreboards = this.getConfig().getBoolean("Scoreboards");
			this.configValues.removeItemsOnCreateKit = this.getConfig().getBoolean("Clear items on kit creation");

			this.configValues.scores = this.getConfig().getBoolean("Enable score");
			this.configValues.scoreIncrement = this.getConfig().getInt("Score per kill");
			String scoreChatPrefix = this.getConfig().getString("Score chat prefix");
			if (!scoreChatPrefix.contains("<score>")) {
				this.getConfig().set("Score chat prefix", "&6[&a<score>&6]");
				this.saveConfig();
			}
			this.configValues.scoreFormat = this.getConfig().getString("Score chat prefix");
			this.configValues.maxScore = this.getConfig().getInt("Max score");

			this.loadPvPKits();
			this.loadEconomy();
			this.loadKillstreaks();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void loadPvPKits() {
		try {
			this.getKitsConfig().options().header("KingKits Kits Configuration | View http://bit.ly/PKKits for information.");

			List<String> defaultKits = new ArrayList<String>();
			defaultKits.add("Default");
			this.getKitsConfig().addDefault("Kits", defaultKits);
			this.getKitsConfig().addDefault("First run", true);
			if (this.getKitsConfig().getBoolean("First run")) {
				List<String> defaultKit = new ArrayList<String>();
				defaultKit.add(Material.IRON_SWORD.getId() + " 1 0 Default Kit Sword");
				defaultKit.add(Material.GOLDEN_APPLE.getId() + " 2 0");
				defaultKit.add(Material.IRON_HELMET.getId() + " 1 0");
				defaultKit.add(Material.LEATHER_CHESTPLATE.getId() + " 1 0");
				defaultKit.add(Material.IRON_LEGGINGS.getId() + " 1 0");
				defaultKit.add(Material.IRON_BOOTS.getId() + " 1 0");
				this.getKitsConfig().addDefault("Default", defaultKit);
				this.getKitsConfig().set("First run", false);
			}
			this.getKitsConfig().options().copyDefaults(true);
			this.getKitsConfig().options().copyHeader(true);
			this.saveKitsConfig();

			this.setupPermissions(true);
			this.loadEnchantments();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void loadEnchantments() {
		try {
			this.getEnchantsConfig().options().header("KingKits Enchantments Configuration | View http://bit.ly/PKEnchantments for information.");
			List<String> defaultSwordKit = new ArrayList<String>();
			defaultSwordKit.add(Enchantment.DURABILITY.getName() + " 3");
			this.getEnchantsConfig().addDefault("Default " + Material.IRON_SWORD.getId(), defaultSwordKit);
			this.getEnchantsConfig().options().copyDefaults(true);
			this.getEnchantsConfig().options().copyHeader(true);
			this.saveEnchantsConfig();

			this.loadLores();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void loadLores() {
		try {
			this.getLoresConfig().options().header("KingKits Lore Configuration | View http://bit.ly/PKLores for information.");
			List<String> defaultSwordKit = new ArrayList<String>();
			defaultSwordKit.add("§6Iron Slayer");
			this.getLoresConfig().addDefault("Default " + Material.IRON_SWORD.getId(), defaultSwordKit);
			this.getLoresConfig().options().copyDefaults(true);
			this.getLoresConfig().options().copyHeader(true);
			this.saveLoresConfig();

			this.loadPotions();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void loadPotions() {
		try {
			this.getPotionsConfig().options().header("KingKits Potions Configuration | View http://bit.ly/PKPotions for information.");
			List<String> defaultKit = new ArrayList<String>();
			defaultKit.add(PotionEffectType.SPEED.getName() + " II 10");
			this.getPotionsConfig().addDefault("Default", defaultKit);
			this.getPotionsConfig().options().copyDefaults(true);
			this.getPotionsConfig().options().copyHeader(true);
			this.savePotionsConfig();

			this.loadDyes();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void loadDyes() {
		try {
			this.getDyesConfig().options().header("KingKits Dye Configuration | View http://bit.ly/PKDyes for information.");
			this.getDyesConfig().addDefault("Default " + Material.LEATHER_CHESTPLATE.getId(), DyeColor.RED.getColor().asRGB());
			this.getDyesConfig().options().copyDefaults(true);
			this.getDyesConfig().options().copyHeader(true);
			this.saveDyesConfig();

			this.loadGuiItems();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void loadGuiItems() {
		try {
			this.getGuiItemsConfig().options().header("KingKits Gui Items Configuration");
			this.getGuiItemsConfig().addDefault("Default", Material.DIAMOND_SWORD.getId());
			this.getGuiItemsConfig().options().copyDefaults(true);
			this.getGuiItemsConfig().options().copyHeader(true);
			this.saveGuiItemsConfig();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void loadScore() {
		try {
			this.getScoresConfig().options().header("KingKits Score Configuration");
			Map<String, Integer> scores = new HashMap<String, Integer>();
			scores.put("Player1", 2);
			if (this.getScoresConfig().getConfigurationSection("Scores") == null) this.getScoresConfig().createSection("Scores", scores);
			this.getScoresConfig().options().copyDefaults(true);
			this.getScoresConfig().options().copyHeader(true);
			this.saveScoresConfig();

			this.playerScores = this.getScoresConfig().getConfigurationSection("Scores").getValues(true);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void loadEconomy() {
		this.getEconomyConfig().options().header("KingKits Economy Configuration");
		this.getEconomyConfig().addDefault("Use economy", false);
		this.getEconomyConfig().addDefault("Enable cost per kit", false);
		this.getEconomyConfig().addDefault("Enable cost per refill", false);
		this.getEconomyConfig().addDefault("Cost per kit", 50.00);
		this.getEconomyConfig().addDefault("Cost per refill", 2.50);
		this.getEconomyConfig().addDefault("Currency", "dollars");
		this.getEconomyConfig().addDefault("Message", "&a<money> <currency> was taken from your balance.");
		this.getEconomyConfig().addDefault("Enable money per kill", false);
		this.getEconomyConfig().addDefault("Money per kill", 5.00);
		this.getEconomyConfig().addDefault("Money per kill message", "&aYou received <money> <currency> for killing <target>.");
		this.getEconomyConfig().addDefault("Enable money per death", false);
		this.getEconomyConfig().addDefault("Money per death", 5.00);
		this.getEconomyConfig().addDefault("Money per death message", "&aYou lost <money> <currency> for being killed by <killer>.");
		this.getEconomyConfig().options().copyDefaults(true);
		this.getEconomyConfig().options().copyHeader(true);
		this.saveEconomyConfig();

		this.configValues.vaultValues.useEconomy = this.getEconomyConfig().getBoolean("Use economy");

		boolean useCostPerKit = this.getEconomyConfig().getBoolean("Enable cost per kit");
		if (useCostPerKit && !this.configValues.vaultValues.useEconomy) {
			this.getEconomyConfig().set("Enable cost per kit", false);
			this.saveEconomyConfig();
			this.reloadEconomyConfig();
		}
		this.configValues.vaultValues.useCostPerKit = this.getEconomyConfig().getBoolean("Enable cost per kit");

		boolean useCostPerRefill = this.getEconomyConfig().getBoolean("Enable cost per refill");
		if (useCostPerRefill && !this.configValues.vaultValues.useEconomy) {
			this.getEconomyConfig().set("Enable cost per refill", false);
			this.saveEconomyConfig();
			this.reloadEconomyConfig();
		}
		this.configValues.vaultValues.useCostPerRefill = this.getEconomyConfig().getBoolean("Enable cost per refill");

		this.configValues.vaultValues.costPerKit = this.getEconomyConfig().getDouble("Cost per kit");
		this.configValues.vaultValues.costPerRefill = this.getEconomyConfig().getDouble("Cost per refill");

		boolean useMoneyPerKill = this.getEconomyConfig().getBoolean("Enable money per kill");
		if (useMoneyPerKill && !this.configValues.vaultValues.useEconomy) {
			this.getEconomyConfig().set("Enable money per kill", false);
			this.saveEconomyConfig();
			this.reloadEconomyConfig();
		}
		this.configValues.vaultValues.useMoneyPerKill = this.getEconomyConfig().getBoolean("Enable money per kill");
		this.configValues.vaultValues.moneyPerKill = this.getEconomyConfig().getDouble("Money per kill");

		boolean useMoneyPerDeath = this.getEconomyConfig().getBoolean("Enable money per death");
		if (useMoneyPerDeath && !this.configValues.vaultValues.useEconomy) {
			this.getEconomyConfig().set("Enable money per death", false);
			this.saveEconomyConfig();
			this.reloadEconomyConfig();
		}
		this.configValues.vaultValues.useMoneyPerDeath = this.getEconomyConfig().getBoolean("Enable money per death");
		this.configValues.vaultValues.moneyPerDeath = this.getEconomyConfig().getDouble("Money per death");

		this.loadCostPerKit();
	}

	private void loadCostPerKit() {
		this.getCPKConfig().options().header("KingKits Cost Per Kit Configuration | View http://bit.ly/PKCPK for information.");
		double defaultCPK = this.getEconomyConfig().getDouble("Cost per kit");
		this.getCPKConfig().addDefault("Default", defaultCPK);
		List<String> kits = this.getKitsConfig().getStringList("Kits");
		for (String kit : kits) {
			if (!this.getCPKConfig().contains(kit)) this.getCPKConfig().addDefault(kit, defaultCPK);
		}
		this.getCPKConfig().options().copyDefaults(true);
		this.getCPKConfig().options().copyHeader(true);
		this.saveCPKConfig();
	}

	private void loadKillstreaks() {
		this.getKillstreaksConfig().options().header("KingKits Killstreak Configuration");
		this.getKillstreaksConfig().addDefault("First run", true);
		if (this.getKillstreaksConfig().getBoolean("First run")) {
			this.getKillstreaksConfig().set("Killstreak 9001", Arrays.asList("broadcast &c<player>&a's killstreak is over 9000!"));
			this.getKillstreaksConfig().set("First run", false);
		}
		this.getKillstreaksConfig().options().copyDefaults(true);
		this.getKillstreaksConfig().options().copyHeader(true);
		this.saveKillstreaksConfig();
	}

	public boolean checkConfig() {
		String scoreChatPrefix = this.getConfig().getString("Score chat prefix");
		if (!scoreChatPrefix.contains("<score>")) {
			this.getConfig().set("Score chat prefix", "&6[&a<score>&6]");
			this.saveConfig();
			return false;
		}
		return true;
	}

	private void setupPermissions(boolean unregisterFirst) {
		if (unregisterFirst) {
			try {
				if (this.plugin.getKitsConfig().contains("Kits")) {
					List<String> kitNames = plugin.getKitsConfig().getStringList("Kits");
					for (int pos = 0; pos < kitNames.size(); pos++) {
						String kit = kitNames.get(pos);
						if (kit.split(" ").length > 1) kit = kit.split(" ")[0];
						try {
							this.getServer().getPluginManager().removePermission(new Permission("kingkits.kits." + kit.toLowerCase()));
						} catch (Exception ex) {
						}
					}
				}
			} catch (Exception ex) {
			}
		}
		try {
			if (plugin.getKitsConfig().contains("Kits")) {
				List<String> kitNames = plugin.getKitsConfig().getStringList("Kits");
				for (int pos = 0; pos < kitNames.size(); pos++) {
					String kit = kitNames.get(pos);
					if (kit.split(" ").length > 1) kit = kit.split(" ")[0];
					try {
						this.getServer().getPluginManager().addPermission(new Permission("kingkits.kits." + kit.toLowerCase()));
					} catch (Exception ex) {
						this.getLogger().info("Couldn't register the permission node: " + "kingkits.kits." + kit.toLowerCase());
						this.getLogger().info("This error probably occured because it's already registered.");
					}
				}
			}
		} catch (Exception ex) {
		}
	}

	public String getEconomyMessage(double amount) {
		this.reloadEconomyConfig();
		String message = this.getEconomyConfig().getString("Message");
		String currency = this.getEconomyConfig().getString("Currency");
		if (amount == 1) if (currency.contains("s")) if (currency.lastIndexOf('s') == message.length()) currency = this.replaceLast(currency, "s", "");
		message = message.replaceAll("<currency>", currency);
		message = message.replaceAll("<money>", String.valueOf(amount));
		return this.replaceAllColours(message);
	}

	public String getMPKMessage(Player killer, double amount) {
		this.reloadEconomyConfig();
		String message = this.getEconomyConfig().getString("Money per kill message");
		String currency = this.getEconomyConfig().getString("Currency");
		if (amount == 1) if (currency.contains("s")) if (currency.lastIndexOf('s') == message.length() - 1) currency = this.replaceLast(currency, "s", "");
		message = message.replaceAll("<currency>", currency);
		message = message.replaceAll("<money>", String.valueOf(amount));
		message = message.replaceAll("<target>", killer.getName());
		return this.replaceAllColours(message);
	}

	public String getMPDMessage(Player dead, double amount) {
		this.reloadEconomyConfig();
		String message = this.getEconomyConfig().getString("Money per death message");
		String currency = this.getEconomyConfig().getString("Currency");
		if (amount == 1) if (currency.contains("s")) if (currency.lastIndexOf('s') == message.length() - 1) currency = this.replaceLast(currency, "s", "");
		message = message.replaceAll("<currency>", currency);
		message = message.replaceAll("<money>", String.valueOf(amount));
		message = message.replaceAll("<killer>", dead.getKiller().getName());
		return this.replaceAllColours(message);
	}

	private String replaceLast(String s, String character, String targetChar) {
		String string = s;
		if (string.contains(character)) {
			StringBuilder b = new StringBuilder(string);
			b.replace(string.lastIndexOf(character), string.lastIndexOf(character) + 1, targetChar);
			string = b.toString();
		}
		return string;
	}

	public String replaceAllColours(String val) {
		String message = ChatColor.translateAlternateColorCodes('&', val);
		message = ChatColor.translateAlternateColorCodes('§', message);
		return message;
	}

	public static List<String> toLowerCaseList(List<String> normalList) {
		List<String> list = new ArrayList<String>();
		for (String s : normalList)
			list.add(s.toLowerCase());
		return list;
	}

	private FileConfiguration KitsConfig = null;
	private File customKitsConfig = null;

	public void reloadKitsConfig() {
		if (this.customKitsConfig == null) {
			this.customKitsConfig = new File(this.getDataFolder(), "kits/config.yml");
		}
		this.KitsConfig = YamlConfiguration.loadConfiguration(this.customKitsConfig);

		InputStream defConfigStream = this.getResource("kits/config.yml");
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			this.KitsConfig.setDefaults(defConfig);
		}
	}

	public FileConfiguration getKitsConfig() {
		if (this.KitsConfig == null) {
			this.reloadKitsConfig();
		}
		return this.KitsConfig;
	}

	public void saveKitsConfig() {
		if (this.KitsConfig == null || this.customKitsConfig == null) {
			return;
		}
		try {
			this.getKitsConfig().save(this.customKitsConfig);
		} catch (IOException ex) {
			this.getLogger().log(Level.SEVERE, "Could not save the Kits config as " + this.customKitsConfig.getName(), ex);
		}
	}

	private FileConfiguration EnchantmentsConfig = null;
	private File customEnchantmentsConfig = null;

	public void reloadEnchantsConfig() {
		if (this.customEnchantmentsConfig == null) {
			this.customEnchantmentsConfig = new File(this.getDataFolder(), "kits/enchantments.yml");
		}
		this.EnchantmentsConfig = YamlConfiguration.loadConfiguration(this.customEnchantmentsConfig);

		InputStream defConfigStream = this.getResource("kits/enchantments.yml");
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			this.EnchantmentsConfig.setDefaults(defConfig);
		}
	}

	public FileConfiguration getEnchantsConfig() {
		if (this.EnchantmentsConfig == null) {
			this.reloadEnchantsConfig();
		}
		return this.EnchantmentsConfig;
	}

	public void saveEnchantsConfig() {
		if (this.EnchantmentsConfig == null || this.customEnchantmentsConfig == null) {
			return;
		}
		try {
			this.getEnchantsConfig().save(this.customEnchantmentsConfig);
		} catch (IOException ex) {
			this.getLogger().log(Level.SEVERE, "Could not save the Enchantments config as " + this.customEnchantmentsConfig.getName(), ex);
		}
	}

	private FileConfiguration LoresConfig = null;
	private File customLoresConfig = null;

	public void reloadLoresConfig() {
		if (this.customLoresConfig == null) {
			this.customLoresConfig = new File(this.getDataFolder(), "kits/lores.yml");
		}
		this.LoresConfig = YamlConfiguration.loadConfiguration(this.customLoresConfig);

		InputStream defConfigStream = this.getResource("kits/lores.yml");
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			this.LoresConfig.setDefaults(defConfig);
		}
	}

	public FileConfiguration getLoresConfig() {
		if (this.LoresConfig == null) {
			this.reloadLoresConfig();
		}
		return this.LoresConfig;
	}

	public void saveLoresConfig() {
		if (this.LoresConfig == null || this.customLoresConfig == null) {
			return;
		}
		try {
			this.getLoresConfig().save(this.customLoresConfig);
		} catch (IOException ex) {
			this.getLogger().log(Level.SEVERE, "Could not save the Lores config as " + customLoresConfig.getName(), ex);
		}
	}

	private FileConfiguration PotionsConfig = null;
	private File customPotionsConfig = null;

	public void reloadPotionsConfig() {
		if (this.customPotionsConfig == null) {
			this.customPotionsConfig = new File(this.getDataFolder(), "kits/potions.yml");
		}
		this.PotionsConfig = YamlConfiguration.loadConfiguration(this.customPotionsConfig);

		InputStream defConfigStream = this.getResource("kits/potions.yml");
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			this.PotionsConfig.setDefaults(defConfig);
		}
	}

	public FileConfiguration getPotionsConfig() {
		if (this.PotionsConfig == null) {
			this.reloadPotionsConfig();
		}
		return this.PotionsConfig;
	}

	public void savePotionsConfig() {
		if (this.PotionsConfig == null || this.customPotionsConfig == null) {
			return;
		}
		try {
			this.getPotionsConfig().save(this.customPotionsConfig);
		} catch (IOException ex) {
			this.getLogger().log(Level.SEVERE, "Could not save the Potions config as " + customPotionsConfig.getName(), ex);
		}
	}

	private FileConfiguration DyeConfig = null;
	private File customDyeConfig = null;

	public void reloadDyesConfig() {
		if (this.customDyeConfig == null) {
			this.customDyeConfig = new File(this.getDataFolder(), "kits/dyes.yml");
		}
		this.DyeConfig = YamlConfiguration.loadConfiguration(this.customDyeConfig);

		InputStream defConfigStream = this.getResource("kits/dyes.yml");
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			this.DyeConfig.setDefaults(defConfig);
		}
	}

	public FileConfiguration getDyesConfig() {
		if (this.DyeConfig == null) {
			this.reloadDyesConfig();
		}
		return this.DyeConfig;
	}

	public void saveDyesConfig() {
		if (this.DyeConfig == null || this.customDyeConfig == null) {
			return;
		}
		try {
			this.getDyesConfig().save(this.customDyeConfig);
		} catch (IOException ex) {
			this.getLogger().log(Level.SEVERE, "Could not save the Dyes config as " + this.customDyeConfig.getName(), ex);
		}
	}

	private FileConfiguration GuiItemsConfig = null;
	private File customGuiItemsConfig = null;

	public void reloadGuiItemsConfig() {
		if (this.customGuiItemsConfig == null) {
			this.customGuiItemsConfig = new File(this.getDataFolder(), "kits/guiitems.yml");
		}
		this.GuiItemsConfig = YamlConfiguration.loadConfiguration(this.customGuiItemsConfig);

		InputStream defConfigStream = this.getResource("kits/guiitems.yml");
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			this.GuiItemsConfig.setDefaults(defConfig);
		}
	}

	public FileConfiguration getGuiItemsConfig() {
		if (this.GuiItemsConfig == null) {
			this.reloadGuiItemsConfig();
		}
		return this.GuiItemsConfig;
	}

	public void saveGuiItemsConfig() {
		if (this.GuiItemsConfig == null || this.customGuiItemsConfig == null) {
			return;
		}
		try {
			this.getGuiItemsConfig().save(this.customGuiItemsConfig);
		} catch (IOException ex) {
			this.getLogger().log(Level.SEVERE, "Could not save the Gui Items config as " + this.customGuiItemsConfig.getName(), ex);
		}
	}

	private FileConfiguration ScoresConfig = null;
	private File customScoresConfig = null;

	public void reloadScoresConfig() {
		if (this.customScoresConfig == null) {
			this.customScoresConfig = new File(this.getDataFolder(), "scores.yml");
		}
		this.ScoresConfig = YamlConfiguration.loadConfiguration(this.customScoresConfig);

		InputStream defConfigStream = this.getResource("scores.yml");
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			this.ScoresConfig.setDefaults(defConfig);
		}
	}

	public FileConfiguration getScoresConfig() {
		if (this.ScoresConfig == null) {
			this.reloadScoresConfig();
		}
		return this.ScoresConfig;
	}

	public void saveScoresConfig() {
		if (this.ScoresConfig == null || this.customScoresConfig == null) {
			return;
		}
		try {
			this.getScoresConfig().save(this.customScoresConfig);
		} catch (IOException ex) {
			this.getLogger().log(Level.SEVERE, "Could not save the Scores config as " + this.customScoresConfig.getName(), ex);
		}
	}

	private FileConfiguration EconomyConfig = null;
	private File customEconomyConfig = null;

	public void reloadEconomyConfig() {
		if (this.customEconomyConfig == null) {
			this.customEconomyConfig = new File(this.getDataFolder(), "economy.yml");
		}
		this.EconomyConfig = YamlConfiguration.loadConfiguration(this.customEconomyConfig);

		InputStream defConfigStream = this.getResource("economy.yml");
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			this.EconomyConfig.setDefaults(defConfig);
		}
	}

	public FileConfiguration getEconomyConfig() {
		if (this.EconomyConfig == null) {
			this.reloadEconomyConfig();
		}
		return this.EconomyConfig;
	}

	public void saveEconomyConfig() {
		if (this.EconomyConfig == null || this.customEconomyConfig == null) {
			return;
		}
		try {
			this.getEconomyConfig().save(this.customEconomyConfig);
		} catch (IOException ex) {
			this.getLogger().log(Level.SEVERE, "Could not save the Economy config as " + this.customEconomyConfig.getName(), ex);
		}
	}

	private FileConfiguration CostPerKitConfig = null;
	private File customCostPerKitConfig = null;

	public void reloadCPKConfig() {
		if (this.customCostPerKitConfig == null) {
			this.customCostPerKitConfig = new File(this.getDataFolder(), "kits/costperkit.yml");
		}
		this.CostPerKitConfig = YamlConfiguration.loadConfiguration(this.customCostPerKitConfig);

		InputStream defConfigStream = this.getResource("kits/costperkit.yml");
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			this.CostPerKitConfig.setDefaults(defConfig);
		}
	}

	public FileConfiguration getCPKConfig() {
		if (this.CostPerKitConfig == null) {
			this.reloadCPKConfig();
		}
		return this.CostPerKitConfig;
	}

	public void saveCPKConfig() {
		if (this.CostPerKitConfig == null || this.customCostPerKitConfig == null) {
			return;
		}
		try {
			this.getCPKConfig().save(this.customCostPerKitConfig);
		} catch (IOException ex) {
			this.getLogger().log(Level.SEVERE, "Could not save the Cost Per Kit config as " + this.customCostPerKitConfig.getName(), ex);
		}
	}

	private FileConfiguration KillstreaksConfig = null;
	private File customKillstreaksConfig = null;

	public void reloadKillstreaksConfig() {
		if (this.customKillstreaksConfig == null) {
			this.customKillstreaksConfig = new File(this.getDataFolder(), "killstreaks.yml");
		}
		this.KillstreaksConfig = YamlConfiguration.loadConfiguration(this.customKillstreaksConfig);

		InputStream defConfigStream = this.getResource("killstreaks.yml");
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			this.KillstreaksConfig.setDefaults(defConfig);
		}
	}

	public FileConfiguration getKillstreaksConfig() {
		if (this.KillstreaksConfig == null) {
			this.reloadKillstreaksConfig();
		}
		return this.KillstreaksConfig;
	}

	public void saveKillstreaksConfig() {
		if (this.KillstreaksConfig == null || this.customKillstreaksConfig == null) {
			return;
		}
		try {
			this.getKillstreaksConfig().save(this.customKillstreaksConfig);
		} catch (IOException ex) {
			this.getLogger().log(Level.SEVERE, "Could not save the Killstreaks config as " + this.customKillstreaksConfig.getName(), ex);
		}
	}

	/** Reloads all the configurations **/
	public void reloadAllConfigs() {
		this.reloadConfig();
		this.reloadKitsConfig();
		this.reloadEnchantsConfig();
		this.reloadLoresConfig();
		this.reloadPotionsConfig();
		this.reloadDyesConfig();
		this.reloadGuiItemsConfig();

		this.reloadScoresConfig();
		this.reloadEconomyConfig();
		this.reloadCPKConfig();
		this.reloadKillstreaksConfig();
	}

}
