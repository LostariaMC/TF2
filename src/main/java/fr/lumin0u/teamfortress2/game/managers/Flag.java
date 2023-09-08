package fr.lumin0u.teamfortress2.game.managers;

import fr.lumin0u.teamfortress2.TF;
import fr.lumin0u.teamfortress2.game.TFPlayer;
import fr.lumin0u.teamfortress2.game.TFTeam;
import fr.lumin0u.teamfortress2.util.ExpValues;
import fr.lumin0u.teamfortress2.util.TFSound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.Title.Times;
import net.kyori.adventure.util.Ticks;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

import java.util.function.Predicate;

public class Flag
{
	private final TFTeam team;
	private ArmorStand flagStand;
	private FlagState flagState = FlagState.SAFE;
	private TFPlayer capturer;
	
	public Flag(TFTeam team) {
		this.team = team;
	}
	
	public void spawnEntity() {
		flagStand = (ArmorStand) team.getFlagLocation().getWorld().spawnEntity(team.getFlagLocation(), EntityType.ARMOR_STAND);
		flagStand.setGravity(false);
		flagStand.setInvulnerable(true);
		flagStand.getEquipment().setHelmet(team.getFlagItem());
		flagStand.setGlowing(true);
		flagStand.setVisible(true);
	}
	
	public ArmorStand getFlagStand() {
		return flagStand;
	}
	
	public TFTeam getTeam() {
		return team;
	}
	
	public void onPlayerDeath(TFPlayer player) {
		if(player.is(capturer)) {
			fall();
		}
	}
	
	public void capture(TFPlayer player) {
		flagState = FlagState.CAPTURED;
		capturer = player;
		capturer.getEntity().setGlowing(true);
		
		flagStand.setGravity(false);
		flagStand.setGlowing(false);
		flagStand.setVisible(false);
		flagStand.setSmall(true);
		
		CTFManager.getInstance().getScoreboardUpdater().updateFlagState(team);
		
		player.getTeam().getOnlinePlayers().forEach(TFSound.OTHER_FLAG_CAPTURED::playTo);
		team.getOnlinePlayers().forEach(p -> {
			TFSound.MY_FLAG_CAPTURED.playTo(p);
			p.toBukkit().showTitle(Title.title(Component.empty(), Component.text("§cBannière capturée"), Times.times(Ticks.duration(5), Ticks.duration(40), Ticks.duration(5))));
		});
	}
	
	public void fall() {
		flagState = FlagState.LAYING;
		
		flagStand.setGravity(true);
		flagStand.setGlowing(true);
		flagStand.setVisible(true);
		flagStand.setSmall(false);
		
		CTFManager.getInstance().getScoreboardUpdater().updateFlagState(team);
		
		team.getOnlinePlayers().forEach(p -> {
			p.toBukkit().showTitle(Title.title(Component.empty(), Component.text("§eBannière au sol"), Times.times(Ticks.duration(5), Ticks.duration(40), Ticks.duration(5))));
		});
		capturer.getEntity().setGlowing(false);
		capturer = null;
	}
	
	public void teleportBack(boolean silent) {
		flagState = FlagState.SAFE;
		
		flagStand.setGravity(false);
		flagStand.setGlowing(true);
		flagStand.setVisible(true);
		flagStand.setSmall(false);
		
		flagStand.teleport(team.getFlagLocation());
		
		CTFManager.getInstance().getScoreboardUpdater().updateFlagState(team);
		
		/*if(!silent) {
			team.getOnlinePlayers().forEach(p -> {
				p.toBukkit().showTitle(Title.title(Component.empty(), Component.text("§aBannière de retour"), Times.times(Ticks.duration(5), Ticks.duration(40), Ticks.duration(5))));
			});
		}*/
		
		if(capturer != null) {
			capturer.getEntity().setGlowing(false);
			capturer = null;
		}
	}
	
	public void logic() {
		CTFManager gm = CTFManager.getInstance();
		
		if(flagState != FlagState.CAPTURED) {
			gm.getOnlinePlayers().stream()
					.filter(Predicate.not(TFPlayer::isDead))
					.filter(player -> !team.equals(player.getTeam()))
					.filter(player -> player.getLocation().distance(flagStand.getLocation()) < 3)
					.findAny()
					.ifPresent(this::capture);
		}
		if(flagState == FlagState.LAYING) {
			gm.getOnlinePlayers().stream()
					.filter(Predicate.not(TFPlayer::isDead))
					.filter(player -> team.equals(player.getTeam()))
					.filter(player -> player.getLocation().distance(flagStand.getLocation()) < 3)
					.findAny()
					.ifPresent(player -> teleportBack(false));
		}
		
		if(flagState == FlagState.CAPTURED) {
			
			flagStand.teleport(capturer.getLocation().add(0, 1.9, 0));
			
			if(gm.getFlag(capturer.getTeam()).getState() == FlagState.SAFE && capturer.getLocation().distance(capturer.getTeam().getFlagLocation()) < 3) {
				capturer.getTeam().incrementFlagCaptureCount();
				capturer.toCosmox().addStatistic("flagCaptureCount", 1);
				
				team.getOnlinePlayers().forEach(p -> {
					p.toBukkit().showTitle(Title.title(Component.text("§cOups..."), Component.text("§eBannière de retour"), Times.times(Ticks.duration(5), Ticks.duration(40), Ticks.duration(5))));
				});
				capturer.getTeam().getOnlinePlayers().forEach(p -> {
					p.toBukkit().showTitle(Title.title(Component.text("§a+1"), Component.text("§aBannière ennemie capturée"), Times.times(Ticks.duration(5), Ticks.duration(40), Ticks.duration(5))));
				});
				
				Bukkit.broadcastMessage(TF.getInstance().getCosmoxGame().getPrefix() + "§eL'équipe " + capturer.getTeam().cosmoxTeam().getPrefix() + " §ea capturé une bannière !");
				
				if(capturer.getTeam().getFlagCaptureCount() == 3) {
					gm.endGame(null, capturer.getTeam());
				}
				capturer.getTeam().getOnlinePlayers().forEach(p -> p.toCosmox().addMolecules(ExpValues.FLAG_CAPTURE_CTF, "Capture de bannière"));
				
				teleportBack(true);
			}
		}
		
		if(flagStand.getLocation().getY() < -64) {
			teleportBack(false);
		}
	}
	
	public FlagState getState() {
		return flagState;
	}
	
	public enum FlagState
	{
		SAFE("§aSafe"),
		CAPTURED("§cCapturée"),
		LAYING("§eAu sol");
		
		private final String display;
		
		FlagState(String display) {
			this.display = display;
		}
		
		public String getDisplay() {
			return display;
		}
	}
}
