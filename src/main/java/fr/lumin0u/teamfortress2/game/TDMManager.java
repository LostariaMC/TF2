package fr.lumin0u.teamfortress2.game;

import fr.lumin0u.teamfortress2.Kit;
import fr.lumin0u.teamfortress2.TF;
import fr.lumin0u.teamfortress2.TFEntity;
import fr.lumin0u.teamfortress2.util.ExpValues;
import fr.lumin0u.teamfortress2.weapons.types.WeaponType;
import fr.worsewarn.cosmox.API;
import fr.worsewarn.cosmox.api.players.WrappedPlayer;
import fr.worsewarn.cosmox.game.GameVariables;
import fr.worsewarn.cosmox.game.Phase;
import fr.worsewarn.cosmox.game.teams.Team;
import fr.worsewarn.cosmox.tools.chat.Messages;
import fr.worsewarn.cosmox.tools.map.GameMap;
import fr.worsewarn.cosmox.tools.world.Cuboid;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BoundingBox;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class TDMManager extends GameManager
{
	public TDMManager(GameMap map) {
		super(map, List.of(
				new TFTeam(Team.RED, Material.REDSTONE_BLOCK, map.getLocation("redSpawn"), BoundingBox.of(map.getCuboid("redSafezone").get(0), map.getCuboid("redSafezone").get(1))),
				new TFTeam(Team.BLUE, Material.DIAMOND_BLOCK, map.getLocation("blueSpawn"), BoundingBox.of(map.getCuboid("blueSafezone").get(0), map.getCuboid("blueSafezone").get(1)))
		));
	}
	
	public static TDMManager getInstance() {
		if(TF.getInstance().getGameManager() instanceof TDMManager tdmManager)
			return tdmManager;
		return null;
	}
	
	@Override
	public void updateScoreboard(TFPlayer player) {}
	
	@Override
	public void preStartGame() {
		super.preStartGame();
		
		teams.forEach(team -> {
			team.getOnlinePlayers().forEach(player -> {
				player.toBukkit().teleport(team.getSpawnpoint());
				
				PlayerInventory inv = player.toBukkit().getInventory();
				inv.clear();
				inv.setBoots(team.getBoots());
				inv.setLeggings(team.getLeggings());
				inv.setChestplate(team.getChestplate());
				
				WeaponType[] weapons = player.getKit().getWeapons();
				for(int i = 0; i < weapons.length; i++) {
					WeaponType type = weapons[i];
					type.createWeapon(player, i).giveItem();
				}
				
				inv.setItem(7, TF.LOCKED_ULT_ITEM);
				inv.setItem(8, TF.MENU_ITEM);
			});
		});
	}
	
	@Override
	public void startGame() {
		super.startGame();
		
		teams.forEach(team -> {
			team.getOnlinePlayers().forEach(player -> {
			
			});
		});
	}
	
	@Override
	public void endGame(TFPlayer winner, TFTeam winnerTeam) {
		super.endGame(winner, winnerTeam);
		
		TF.getInstance().getNonSpecPlayers().forEach(player -> {
			if(winnerTeam.equals(player.getTeam())) {
				player.toCosmox().addMolecules(ExpValues.WIN_TDM, "Victoire");
				player.toCosmox().addStatistic(GameVariables.WIN, 1);
				player.toBukkit().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 1000000, 5, false, false, true));
			}
			else {
				player.toCosmox().addMolecules(ExpValues.LOSE_TDM, "Lot de consolation");
				player.toBukkit().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 1000000, 2, false, false, true));
			}
		});
		
		TF.getInstance().getPlayers().stream().filter(WrappedPlayer::isOnline).forEach(player ->
		{
			if(!player.isSpectator()) {
				player.toCosmox().addStatistic(GameVariables.GAMES_PLAYED, 1);
			}
			
			player.toBukkit().sendTitle("§eFin de la partie !", "", 5, 30, 30);
		});
	}
	
	public void onSingleKill(TFPlayer damager, TFPlayer victim) {
	
	}
}
