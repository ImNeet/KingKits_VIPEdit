package me.faris.kingkits.listeners.event.custom;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerKitEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	private Player thePlayer = null;
	private String kitName = null;
	private String oldKit = null;

	/**
	 * Create a new PlayerKitEvent instance.
	 * @param player - The player.
	 * @param kitName - The new kit.
	 **/
	public PlayerKitEvent(Player player, String kitName) {
		this.thePlayer = player;
		this.kitName = kitName;
	}

	/**
	 * Create a new PlayerKitEvent instance.
	 * @param player - The player.
	 * @param kitName - The new kit.
	 * @param oldKit - The previous kit the player was.
	 */
	public PlayerKitEvent(Player player, String kitName, String oldKit) {
		this.thePlayer = player;
		this.kitName = kitName;
		this.oldKit = oldKit;
	}

	/** Returns the player **/
	public Player getPlayer() {
		return this.thePlayer;
	}

	/** Returns the player's old kit' name **/
	public String getOldKit() {
		return this.oldKit;
	}

	/** Returns the kit's name **/
	public String getKit() {
		return this.kitName;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
