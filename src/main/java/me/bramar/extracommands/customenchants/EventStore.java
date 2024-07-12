package me.bramar.extracommands.customenchants;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public class EventStore {
	private EventType type;
	private Player p;
	private Event e;

	public Player getPlayer() {
		return p;
	}
	public EventStore setPlayer(Player p) {
		this.p = p;
		return this;
	}
	
	public <T extends Event> T cast(Class<T>... event) {
		return (T) this.e;
	}
	
	public EventType getType() {
		return type;
	}

	public Event getUncastedEvent() {
		return e;
	}
	public EventStore(EventType type, Event e) {
		this(type);
		this.e = e;
	}
	public EventStore(EventType type) {
		this.type = type;
	}
}
