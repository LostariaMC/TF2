package fr.lumin0u.teamfortress2.game;

import fr.lumin0u.teamfortress2.TF;
import fr.lumin0u.teamfortress2.util.ImmutableItemStack;
import fr.lumin0u.teamfortress2.util.ItemBuilder;
import fr.lumin0u.teamfortress2.util.Utils;
import fr.worsewarn.cosmox.api.players.WrappedPlayer;
import fr.worsewarn.cosmox.game.teams.Team;
import fr.worsewarn.cosmox.tools.map.GameMap;
import net.kyori.adventure.text.format.TextColor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;

import java.util.*;
import java.util.stream.Collectors;

public class TFTeam
{
	private static final Map<Team, Material> TEAM_BLOCKS = Map.of(
			Team.RED, Material.REDSTONE_BLOCK,
			Team.BLUE, Material.LAPIS_BLOCK);
	private static final Map<Team, String> TEAM_ID = Map.of(
			Team.RED, "red",
			Team.BLUE, "blue");
	
	private final Team cosmoxTeam;
	private final String name;
	private final TextColor nkaColor;
	private final ChatColor chatColor;
	private final Location spawnpoint;
	private final BoundingBox safeZone;
	private final Material blockInCart;
	private final ImmutableItemStack chestplate;
	private final ImmutableItemStack leggings;
	private final ImmutableItemStack boots;
	private final Block railsStart;
	private final Block railsEnd;
	private final List<Block> rails;
	
	private double payloadProgression;
	private int kills;
	
	public TFTeam(Team cosmoxTeam, String name, TextColor nkaColor, ChatColor chatColor, Location spawnpoint, BoundingBox safeZone, Material blockInCart, Block railsStart, Block railsEnd) {
		this.cosmoxTeam = cosmoxTeam;
		this.name = name;
		this.nkaColor = nkaColor;
		this.chatColor = chatColor;
		this.spawnpoint = spawnpoint;
		this.safeZone = safeZone;
		this.blockInCart = blockInCart;
		this.railsStart = railsStart;
		this.railsEnd = railsEnd;
		List<Block> tempRails = new ArrayList<>();
		PayloadsManager.getRailsIteratorBetween(railsStart, railsEnd).forEachRemaining(tempRails::add);
		this.rails = Collections.unmodifiableList(tempRails);
		
		this.chestplate = new ItemBuilder(Material.LEATHER_CHESTPLATE).setDisplayName(chatColor + "VOUS ETES " + name.toUpperCase()).setLeatherColor(cosmoxTeam.getMaterialColor()).buildImmutable();
		this.leggings = new ItemBuilder(Material.LEATHER_LEGGINGS).setDisplayName(chatColor + "VOUS ETES " + name.toUpperCase()).setLeatherColor(cosmoxTeam.getMaterialColor()).buildImmutable();
		this.boots = new ItemBuilder(Material.LEATHER_BOOTS).setDisplayName(chatColor + "VOUS ETES " + name.toUpperCase()).setLeatherColor(cosmoxTeam.getMaterialColor()).buildImmutable();
	}
	
	private static final String SPAWN_F = "%sSpawn", SAFEZONE_F = "%sSafeZone", RAILS_START_F = "%sRailsStart", RAILS_END_F = "%sRailsEnd";
	
	public static TFTeam loadTDM(Team cosmoxTeam, GameMap map) {
		ChatColor bungeeColor = cosmoxTeam.getColor().asBungee();
		List<Location> safezone = map.getCuboid(SAFEZONE_F.formatted(TEAM_ID.get(cosmoxTeam)));
		return new TFTeam(cosmoxTeam,
				cosmoxTeam.getName(),
				Utils.nkaColor(bungeeColor),
				bungeeColor,
				map.getLocation(SPAWN_F.formatted(TEAM_ID.get(cosmoxTeam))),
				BoundingBox.of(safezone.get(0), safezone.get(1)),
				null, null, null);
	}
	
	public static TFTeam loadPayloads(Team cosmoxTeam, GameMap map) {
		ChatColor bungeeColor = cosmoxTeam.getColor().asBungee();
		List<Location> safezone = map.getCuboid(SAFEZONE_F.formatted(TEAM_ID.get(cosmoxTeam)));
		return new TFTeam(cosmoxTeam,
				cosmoxTeam.getName(),
				Utils.nkaColor(bungeeColor),
				bungeeColor,
				map.getLocation(SPAWN_F.formatted(TEAM_ID.get(cosmoxTeam))),
				BoundingBox.of(safezone.get(0), safezone.get(1)),
				TEAM_BLOCKS.get(cosmoxTeam),
				map.getLocation(RAILS_START_F.formatted(TEAM_ID.get(cosmoxTeam))).getBlock(),
				map.getLocation(RAILS_END_F.formatted(TEAM_ID.get(cosmoxTeam))).getBlock());
	}
	
	public String getCharFR() {
		return String.valueOf(name.toCharArray()[0]).toUpperCase();
	}
	
	public String getName(boolean f) {
		if(f && name.equals("bleu"))
			return name + "e";
		else
			return name;
	}
	
	public TextColor getNkaColor() {
		return nkaColor;
	}
	
	public ChatColor getChatColor() {
		return chatColor;
	}
	
	public Set<TFPlayer> getPlayers() {
		return TF.getInstance().getPlayers().stream().filter(p -> this.equals(p.getTeam())).collect(Collectors.toSet());
	}
	
	public Set<TFPlayer> getOnlinePlayers() {
		return TF.getInstance().getPlayers().stream().filter(p -> this.equals(p.getTeam()) && p.isOnline()).collect(Collectors.toSet());
	}
	
	public Location getSpawnpoint() {
		return spawnpoint;
	}
	
	public BoundingBox getSafeZone() {
		return safeZone;
	}
	
	public ItemStack getChestplate() {
		return chestplate.clone();
	}
	
	public ItemStack getLeggings() {
		return leggings.clone();
	}
	
	public ItemStack getBoots() {
		return boots.clone();
	}
	
	public Material getBlockInCart() {
		return blockInCart;
	}
	
	public Team cosmoxTeam() {
		return cosmoxTeam;
	}
	
	@Override
	public String toString() {
		return "Team [name=" + name + "]";
	}
	
	public int getKills() {
		return kills;
	}
	
	public void incrementKills() {
		this.kills++;
	}
	
	public Block getRailsStart() {
		return railsStart;
	}
	
	public Block getRailsEnd() {
		return railsEnd;
	}
	
	public double getPayloadProgression() {
		return payloadProgression;
	}
	
	public void setPayloadProgression(double payloadProgression) {
		this.payloadProgression = payloadProgression;
	}
	
	public List<Block> getRails() {
		return rails;
	}
}
