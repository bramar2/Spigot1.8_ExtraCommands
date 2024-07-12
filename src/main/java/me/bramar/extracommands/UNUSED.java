package me.bramar.extracommands;

public class UNUSED {

}

//package me.bramar.extracommands;
//
//import org.bukkit.Bukkit;
//import org.bukkit.ChatColor;
//import org.bukkit.GameMode;
//import org.bukkit.Location;
//import org.bukkit.entity.Player;
//
//import com.comphenix.packetwrapper.WrapperPlayServerOpenSignEditor;
//import com.comphenix.protocol.PacketType;
//import com.comphenix.protocol.ProtocolLibrary;
//import com.comphenix.protocol.events.PacketAdapter;
//import com.comphenix.protocol.events.PacketContainer;
//import com.comphenix.protocol.events.PacketEvent;
//
//import net.dev.eazynick.api.NickManager;
//import net.minecraft.server.v1_8_R3.IChatBaseComponent;
//
//public class UNUSED {
//
//	Player p;
//	String prefix = "";
//	String suffix = "";
//	String nickname = "";
//	String skinname = "";
//	boolean nickset = false;
//	boolean skinset = false;
//	boolean prefixset = false;
//	boolean suffixset = false;
//	GameMode gamemode;
//	Location loc;
//	Main main;
//	boolean done = false;
//	PacketAdapter adapter;
//	@SuppressWarnings("deprecation")
//	public UNUSED(Main main, Player p) {
//		if(done) return;
//		this.main = main;
//		this.adapter = new PacketAdapter(main, PacketType.Play.Server.UPDATE_SIGN) {
//			public void onPacketSending(PacketEvent e) {
//				if(done) return;
//				PacketContainer container = e.getPacket();
//				IChatBaseComponent[] array = (IChatBaseComponent[])container.getModifier().getValues().get(2);
//				IChatBaseComponent chat = array[0];
//				String value = chat.getText();
//				System.out.println(value);
//				for(int i = 1; i < array.length; i++) {
//					System.out.println(array[i]);
//				}
//				if(!nickset) {
//					nickset = true;
//					nickname = value;
//					WrapperPlayServerOpenSignEditor wrapper = new WrapperPlayServerOpenSignEditor(ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.OPEN_SIGN_EDITOR));
//					wrapper.setLocation(new com.comphenix.protocol.wrappers.BlockPosition(-1, 17, 1));
//					wrapper.sendPacket(p);
//				}else if(!skinset) {
//					skinset = true;
//					skinname = value;
//					WrapperPlayServerOpenSignEditor wrapper = new WrapperPlayServerOpenSignEditor(ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.OPEN_SIGN_EDITOR));
//					wrapper.setLocation(new com.comphenix.protocol.wrappers.BlockPosition(-1, 17, 0));
//					wrapper.sendPacket(p);
//				}else if(!prefixset) {
//					prefixset = true;
//					prefix = value;
//					WrapperPlayServerOpenSignEditor wrapper = new WrapperPlayServerOpenSignEditor(ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.OPEN_SIGN_EDITOR));
//					wrapper.setLocation(new com.comphenix.protocol.wrappers.BlockPosition(-2, 17, 1));
//					wrapper.sendPacket(p);
//				}else if(!suffixset) {
//					suffixset = true;
//					suffix = value;
//					finish();
//				}else finish();
//			}
//		};
//		this.p = p;
//		this.gamemode = p.getGameMode();
//		this.loc = p.getLocation();
//		p.teleport(new Location(Bukkit.getWorld("plots"), 0, 200, 0));
//		p.setGameMode(GameMode.SPECTATOR);
//		ProtocolLibrary.getProtocolManager().addPacketListener(adapter);
//	}
//
//	public void finish() {if(done) return;
//		System.out.println("Name: " + nickname + "\nPrefix: " + prefix + "\nSuffix: " + suffix + "\nSkin: " + skinname);
//		ProtocolLibrary.getProtocolManager().removePacketListener(adapter);
//		String prefixClone = new String(prefix);
//		prefix = null;
//		done = true;
////		main..remove(p.getUniqueId());
//		p.teleport(loc);
//		p.setGameMode(gamemode);
//		if(nickname.contains(" ")) {
//			p.sendMessage(ChatColor.RED + "Nicknames can't have spaces!");
//			return;
//		}
//		NickManager api = new NickManager(p);
//		String nicknameClone = new String(nickname);
////		for(char c : UNUSED.ALLOWED_CHARS) {
//			nicknameClone = nicknameClone.replace(Character.toString(c), "");
//		}
//		if(!nicknameClone.equalsIgnoreCase("")) {
//			p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThe nickname can only have characters of &6a-z&c, &6A-Z&c, &60-9&c, and &6_"));
//			return;
//		}
//		try {
//			api.nickPlayer(nickname, (skinname.equalsIgnoreCase("") ? "Steve" : skinname));
//			api.changeSkin((skinname.equalsIgnoreCase("") ? "Steve" : skinname));
//			api.updatePlayer();
//			api.setChatPrefix((!prefixClone.equalsIgnoreCase("") ? prefixClone : "&7"));
//			api.setTagPrefix((!prefixClone.equalsIgnoreCase("") ? prefixClone : "&7"));
//			api.setTabPrefix((!prefixClone.equalsIgnoreCase("") ? prefixClone : "&7"));
//			api.setChatSuffix((!suffix.equalsIgnoreCase("") ? suffix : ""));
//			api.setTagSuffix((!suffix.equalsIgnoreCase("") ? suffix : ""));
//			api.setTabSuffix((!suffix.equalsIgnoreCase("") ? suffix : ""));
//			api.updatePlayer();
//			p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aYou are &cCUSTOM-NICKED &aas: &6" + (!prefixClone.equalsIgnoreCase("") ? prefixClone : "&7") + nickname + (!suffix.equalsIgnoreCase("") ? suffix : "")));
//		}catch(Exception e1) {
//			p.sendMessage(ChatColor.RED + "Error while nicknaming you: " + e1.getMessage());
//		}
//	}
//
//	public void askForInfo() {if(done) return;
//		p.sendMessage(ChatColor.RED + "You are in a SignGUI and you can't move. If you want to cancel this operation, just relog!");
//		WrapperPlayServerOpenSignEditor wrapper = new WrapperPlayServerOpenSignEditor(ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.OPEN_SIGN_EDITOR));
//		wrapper.setLocation(new com.comphenix.protocol.wrappers.BlockPosition(-2, 17, 0));
//		wrapper.sendPacket(p);
//	}
//}
