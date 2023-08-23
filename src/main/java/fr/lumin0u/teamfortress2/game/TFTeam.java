package fr.lumin0u.teamfortress2.game;

import fr.lumin0u.teamfortress2.TF;
import fr.lumin0u.teamfortress2.game.TFPlayer;
import fr.lumin0u.teamfortress2.util.ImmutableItemStack;
import fr.lumin0u.teamfortress2.util.ItemBuilder;
import fr.worsewarn.cosmox.api.players.WrappedPlayer;
import fr.worsewarn.cosmox.game.teams.Team;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class TFTeam
{
	private final Team cosmoxTeam;
	private final String name;
	private final String prefix;
	private final Location spawnpoint;
	private final BoundingBox safeZone;
	private final Material blockInCart;
	private final ImmutableItemStack chestplate;
	private final ImmutableItemStack leggings;
	private final ImmutableItemStack boots;
	
	private int kills;
	
	private WrappedPlayer modifySZ1;
	private WrappedPlayer modifySZ2;
	private WrappedPlayer modifyStartRails;
	private WrappedPlayer modifyEndRails;
	private WrappedPlayer modifyFlag;
	private WrappedPlayer addBifurc;
	private WrappedPlayer modifyFinalTerminus;
	
	public TFTeam(Team cosmoxTeam, Material blockInCart, Location spawnpoint, BoundingBox safeZone) {
		this.cosmoxTeam = cosmoxTeam;
		this.name = cosmoxTeam.getName();
		this.prefix = cosmoxTeam.getColor().toString();
		this.chestplate = new ItemBuilder(Material.LEATHER_CHESTPLATE).setDisplayName(prefix + "VOUS ETES " + name.toUpperCase()).setLeatherColor(cosmoxTeam.getMaterialColor()).buildImmutable();
		this.leggings = new ItemBuilder(Material.LEATHER_LEGGINGS).setDisplayName(prefix + "VOUS ETES " + name.toUpperCase()).setLeatherColor(cosmoxTeam.getMaterialColor()).buildImmutable();
		this.boots = new ItemBuilder(Material.LEATHER_BOOTS).setDisplayName(prefix + "VOUS ETES " + name.toUpperCase()).setLeatherColor(cosmoxTeam.getMaterialColor()).buildImmutable();
		
		this.spawnpoint = spawnpoint;
		this.blockInCart = blockInCart;
		this.safeZone = safeZone;
	}
	
	public String getCharFR() {
		return new String(new char[]{name.toCharArray()[0]}).toUpperCase();
	}
	
	public String getName(boolean f) {
		if(f && name.equals("bleu"))
			return name + "e";
		else
			return name;
	}
	
	public String getPrefix() {
		return prefix;
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
	
	public boolean isModifyingSZ1(WrappedPlayer p) {
		return modifySZ1 != null && modifySZ1.equals(p);
	}
	
	public void setSZ1Modifier(WrappedPlayer p) {
		modifySZ1 = p;
	}
	
	public boolean isModifyingSZ2(WrappedPlayer p) {
		return modifySZ2 != null && modifySZ2.equals(p);
	}
	
	public void setSZ2Modifier(WrappedPlayer p) {
		modifySZ2 = p;
	}
	
	public boolean isModifyingStartRails(WrappedPlayer p) {
		return modifyStartRails != null && modifyStartRails.equals(p);
	}
	
	public void setStartRailsModifier(WrappedPlayer p) {
		modifyStartRails = p;
	}
	
	public boolean isModifyingEndRails(WrappedPlayer p) {
		return modifyEndRails != null && modifyEndRails.equals(p);
	}
	
	public void setEndRailsModifier(WrappedPlayer p) {
		modifyEndRails = p;
	}
	
	public boolean isModifyingFlag(WrappedPlayer p) {
		return modifyFlag != null && modifyFlag.equals(p);
	}
	
	public void setFlagModifier(WrappedPlayer p) {
		modifyFlag = p;
	}
	
	public boolean isModifyingFinalTerminus(WrappedPlayer p) {
		return modifyFinalTerminus != null && modifyFinalTerminus.equals(p);
	}
	
	public void setFinalTerminusModifier(WrappedPlayer p) {
		modifyFinalTerminus = p;
	}
	
	public boolean isAddingBifurc(WrappedPlayer p) {
		return addBifurc != null && addBifurc.equals(p);
	}
	
	public void setBifurcAdder(WrappedPlayer p) {
		addBifurc = p;
	}
	
	public Material getBlockInCart() {
		return blockInCart;
	}
	
	public Team cosmoxTeam() {
		return cosmoxTeam;
	}

//	public boolean isFFATeam()
//	{
//		return ffaTeam;
//	}
//
//	public List<Location> getFFAPoints()
//	{
//		return ffaPoints;
//	}
	
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
}
