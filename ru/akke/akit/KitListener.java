package ru.akke.akit;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class KitListener implements Listener {
	
	private AmazingKit pl;
	
	public KitListener(AmazingKit p) {
		this.pl = p;
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onJoin(PlayerJoinEvent event) {
		if (event.getPlayer().getLastPlayed() > 0) {
			return;
		}
		if (pl.FirstJoinKit.equalsIgnoreCase("none")) {
			return;
		}
		event.getPlayer().performCommand("kit " + pl.FirstJoinKit);
	}
}
