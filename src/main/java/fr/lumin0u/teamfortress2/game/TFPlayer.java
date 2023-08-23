package fr.lumin0u.teamfortress2.game;

import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.events.PacketContainer;
import fr.lumin0u.teamfortress2.FireCause;
import fr.lumin0u.teamfortress2.Kit;
import fr.lumin0u.teamfortress2.TF;
import fr.lumin0u.teamfortress2.TFEntity;
import fr.lumin0u.teamfortress2.util.ItemBuilder;
import fr.lumin0u.teamfortress2.util.Items;
import fr.lumin0u.teamfortress2.weapons.Weapon;
import fr.lumin0u.teamfortress2.weapons.types.WeaponType;
import fr.worsewarn.cosmox.api.players.WrappedPlayer;
import fr.worsewarn.cosmox.game.teams.Team;
import fr.worsewarn.cosmox.tools.chat.Messages;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import net.minecraft.network.protocol.game.ClientboundHurtAnimationPacket;
import net.minecraft.network.protocol.game.PacketPlayOutUpdateHealth;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class TFPlayer extends WrappedPlayer implements TFEntity
{
	protected FireCause fireCause;
	private TFPlayer poisonSource;
	private boolean leftSafeZone;
	
	private Kit kit;
	private Kit nextKit;
	private TFTeam team;
	
	private List<Weapon> weapons;
	
	private boolean inScope;
	private boolean heavyRage;
	private final AtomicInteger heavyBulletNb;
	
	private boolean spyInvisible;
	private WrappedPlayer disguise;
	
	private boolean engiInvicible;
	
	private boolean energized;
	private boolean canDoubleJump;
	
	private final Map<TFPlayer, Long> lastDamagers;
	private long lastMeleeHitDate;
	private int killCount;
	
	//private RClickingPlayerTask rClickingTask;
	private boolean dead;
	private boolean hasJoinedGameBefore;
	
	public TFPlayer(UUID uuid) {
		super(uuid);
		
		/*rClickingTask = new RClickingPlayerTask(this);
		rClickingTask.runTaskTimer(TF.getInstance(), 1, 1);*/
		
		this.nextKit = TF.getInstance().getKitFromRedis(this);
		setKit(nextKit);
		weapons = new ArrayList<>();
		
		this.lastDamagers = new HashMap<>();
		this.lastMeleeHitDate = 0;
		
		this.inScope = false;
		
		this.engiInvicible = false;
		
		this.disguise = this;
		this.spyInvisible = false;
		
		this.heavyBulletNb = new AtomicInteger(0);
		this.heavyRage = false;
		
		this.canDoubleJump = false;
		this.energized = false;
	}
	
	public static TFPlayer of(Object player) {
		return WrappedPlayer.of(player).to(TFPlayer.class);
	}
	
	/**
	 * if a player is not a spectator, then it has a team
	 * */
	public boolean isSpectator() {
		return toCosmox().isTeam(Team.SPEC);
	}
	
	public boolean isEnemy(TFEntity entity) {
		return (team != null && !team.equals(entity.getTeam())) || GameManager.getInstance().isFriendlyFire();
	}
	
	public void setTeam(TFTeam team) {
		this.team = team;
	}
	
	/**
	 * @return the weapon of the given type the player has. <br />
	 * if the player has not a weapon of this type, returns null
	 * */
	public <T extends WeaponType, E extends Weapon> E getWeapon(T type) {
		return (E) weapons.stream().filter(w -> w.getType().equals(type)).findAny().orElse(null);
	}
	
	public <T extends WeaponType, E extends Weapon> Optional<E> getOptWeapon(T type) {
		return (Optional<E>) weapons.stream().filter(w -> w.getType().equals(type)).findAny();
	}
	
	public <T extends WeaponType, E extends Weapon> Optional<E> getWeaponInHand() {
		return (Optional<E>) weapons.stream().filter(w -> w.getType().isItem(toBukkit().getInventory().getItemInMainHand())).findAny();
	}
	
	public <T extends WeaponType, E extends Weapon> Optional<E> getWeaponInSlot(int slot) {
		return (Optional<E>) weapons.stream().filter(w -> w.getType().isItem(toBukkit().getInventory().getItem(slot))).findAny();
	}
	
	public void giveWeapon(Weapon weapon) {
		weapon.giveItem();
		weapons.add(weapon);
	}
	
	public void removeWeapon(Weapon weapon) {
		weapon.remove();
		weapons.remove(weapon);
	}
	
	public void removeWeapon(WeaponType weaponType) {
		weapons.removeIf(w -> w.getType().equals(weaponType));
	}
	
	public boolean hasWeapon(Weapon weapon) {
		return weapons.contains(weapon);
	}
	
	public Weapon getUltimate() {
		return getWeapon(getKit().getSpecial());
	}
	
	public List<Weapon> getWeapons() {
		return new ArrayList<>(weapons);
	}
	
	@Override
	@Nullable
	public TFTeam getTeam() {
		return team;
	}
	
	public boolean isRandomKit() {
		return nextKit == Kit.RANDOM;
	}
	
	public boolean isInScope() {
		return inScope;
	}
	
	public void setInScope(boolean inScope) {
		this.inScope = inScope;
	}
	
	public boolean isHeavyRage() {
		return heavyRage;
	}
	
	public void setHeavyRage(boolean heavyRage) {
		this.heavyRage = heavyRage;
	}
	
	public boolean isSpyInvisible() {
		return spyInvisible;
	}
	
	public void setSpyInvisible(boolean spyInvisible) {
		this.spyInvisible = spyInvisible;
	}
	
	public WrappedPlayer getDisguise() {
		return disguise;
	}
	
	public void setDisguise(WrappedPlayer disguise) {
		this.disguise = disguise;
	}
	
	public boolean isEngiInvicible() {
		return engiInvicible;
	}
	
	public void setEngiInvicible(boolean engiInvicible) {
		this.engiInvicible = engiInvicible;
	}
	
	public boolean canDoubleJump() {
		return canDoubleJump;
	}
	
	public void setCanDoubleJump(boolean canDoubleJump) {
		this.canDoubleJump = canDoubleJump;
	}
	
	public Kit getKit() {
		return kit;
	}
	
	private void setKit(Kit kit) {
		if(kit == Kit.RANDOM)
			this.kit = Kit.getRealRandomKit(new Random());
		else
			this.kit = kit;
	}
	
	public Kit getNextKit() {
		return nextKit;
	}
	
	public void setNextKit(Kit nextKit, boolean redis) {
		if(redis)
			TF.getInstance().setKitInRedis(this, nextKit);
		this.nextKit = nextKit;
	}
	
	public void addDamager(TFPlayer damager, double damage) {
		lastDamagers.put(damager, TF.currentTick() + lastDamagers.getOrDefault(damager, 0L) + (long) (damage * 60));
	}
	
	public boolean lifeBeenImpactedRecentlyBy(TFPlayer damager) {
		return lastDamagers.getOrDefault(damager, 0L) > TF.currentTick();
	}
	
	public boolean canBeMeleeHit() {
		return TF.currentTick() > lastMeleeHitDate + 10;
	}
	
	public void setLastMeleeHitDate(long lastMeleeHitDate) {
		this.lastMeleeHitDate = lastMeleeHitDate;
	}
	
	public AtomicInteger heavyBulletNb() {
		return heavyBulletNb;
	}
	
	public boolean isEnergized() {
		return energized;
	}
	
	public void setEnergized(boolean energized) {
		this.energized = energized;
	}
	
	public boolean isInSafeZone() {
		return getTeam() != null && getTeam().getSafeZone().contains(getLocation().toVector());
	}
	
	public boolean hasLeftSafeZone() {
		return leftSafeZone;
	}
	
	public void setHasLeftSafeZone(boolean leftSafeZone) {
		this.leftSafeZone = leftSafeZone;
	}
	
	/*public RClickingPlayerTask getrClickingTask() {
		return rClickingTask;
	}
	
	public void setrClickingTask(RClickingPlayerTask rClickingTask) {
		this.rClickingTask = rClickingTask;
	}*/
	
	@Override
	public Location getLocation() {
		return toBukkit().getLocation();
	}
	
	@Override
	public Location getEyeLocation() {
		return toBukkit().getEyeLocation();
	}
	
	@Override
	public LivingEntity getEntity() {
		return toBukkit();
	}
	
	/**
	 * @return true if the player is dead or if the player is not playing (is a spectator)
	 * */
	@Override
	public boolean isDead() {
		return dead || isSpectator() || !isOnline();
	}
	
	public String getListName() {
		return (getTeam() == null ? "§7" : getTeam().getPrefix()) + getName() + " §6" + getKit().getSymbol();
	}
	
	public void setDead(boolean dead) {
		this.dead = dead;
	}
	
	public int getKillCount() {
		return killCount;
	}
	
	public boolean hasJoinedBefore() {
		return hasJoinedGameBefore;
	}
	
	public void setHasJoinedGameBefore(boolean hasJoinedGameBefore) {
		this.hasJoinedGameBefore = hasJoinedGameBefore;
	}
	
	@Override
	public FireCause getFireCause() {
		return fireCause;
	}
	
	@Override
	public void setFireCause(FireCause fireCause) {
		this.fireCause = fireCause;
	}
	
	@Override
	public TFPlayer getPoisonSource() {
		return poisonSource;
	}
	
	@Override
	public void setPoisonSource(TFPlayer poisonSource) {
		this.poisonSource = poisonSource;
	}
	
	@Override
	public boolean damage(TFPlayer damager, double amount, Vector knockback) {
		if(!isOnline())
			return false;
		
		if(isEngiInvicible() || isInSafeZone())
			return false;
		
		amount = Math.min(amount, toBukkit().getHealth());
		
		if(damager != null)
			addDamager(damager, amount);
		
		if(toBukkit().getHealth() <= amount) {
			die(damager);
		} else {
			toBukkit().setHealth(toBukkit().getHealth() - amount);
			
			//toBukkit().damage(0);
			
			PacketContainer packetDamageEvent = new PacketContainer(Server.DAMAGE_EVENT,
					new ClientboundDamageEventPacket(toBukkit().getEntityId(), 0, 0, 0, Optional.empty()));
			
			PacketContainer packetHurtAnimation = new PacketContainer(Server.HURT_ANIMATION, new ClientboundHurtAnimationPacket(toBukkit().getEntityId(), 0f));
			PacketContainer packetUpdateHealth = new PacketContainer(Server.UPDATE_HEALTH, new PacketPlayOutUpdateHealth((float) toBukkit().getHealth() / (float) getKit().getMaxHealth() * 20f, 20, 0f));
			
			for(WrappedPlayer watcher : WrappedPlayer.of(Bukkit.getOnlinePlayers())) {
				watcher.sendPacket(packetDamageEvent);
			}
			
			this.sendPacket(packetHurtAnimation);
			this.sendPacket(packetUpdateHealth);
			
			toBukkit().getWorld().playSound(getLocation(), Sound.ENTITY_PLAYER_HURT, 1, 1);
		}
		
		toBukkit().setVelocity(toBukkit().getVelocity().multiply(0.5).add(knockback));
		
		return true;
	}
	
	public void die(TFPlayer killer) {
		die(killer, false);
	}
	
	public void die(TFPlayer killer, boolean disconnect) {
		if(dead)
			return;
		
		new ArrayList<>(weapons).forEach(this::removeWeapon);
		
		List<TFPlayer> trueLastDamagers = lastDamagers.keySet().stream()
				.filter(this::lifeBeenImpactedRecentlyBy)
				.sorted((p1, p2) -> Long.compare(lastDamagers.get(p1), lastDamagers.get(p2)))
				.toList();
		lastDamagers.clear();
		
		for(TFPlayer lastDamager : trueLastDamagers) {
			lastDamager.getUltimate().onOwnerDoKill();
		}
		
		if(GameManager.getInstance().getGameType().isTDM()) {
			TDMManager.getInstance().onSingleKill(this, trueLastDamagers.isEmpty() ? null : trueLastDamagers.get(0).getTeam());
		}
		
		if(!disconnect) {
			dead = true;
			
			toBukkit().setGameMode(GameMode.SPECTATOR);
			
			Bukkit.broadcastMessage(TF.getInstance().getCosmoxGame().getPrefix()
					+ Messages.BROADCAST_KILL.formatted(getListName(), trueLastDamagers.stream().map(TFPlayer::getListName).collect(Collectors.joining("§7, "))));
			
			if(killer != null) {
				Bukkit.getScheduler().runTaskLater(TF.getInstance(), () -> {
					if(isOnline() && killer.isOnline()) {
						toBukkit().setSpectatorTarget(killer.toBukkit());
					}
				}, 20);
			}
			
			Bukkit.getScheduler().runTaskLater(TF.getInstance(), () -> {
				dead = false;
				if(isOnline()) {
					respawn(GameManager.getInstance().findSpawnLocation(this));
				}
			}, 60);
		}
	}
	
	public void respawn(Location location) {
		toBukkit().teleport(location);
		toBukkit().setGameMode(GameMode.ADVENTURE);
		toBukkit().setSaturation(0);
		toBukkit().setFoodLevel(20);
		toBukkit().setFoodLevel(20);
		
		new ArrayList<>(weapons).forEach(this::removeWeapon);
		setKit(nextKit);
		leftSafeZone = false;
		
		PlayerInventory inv = toBukkit().getInventory();
		inv.clear();
		inv.setBoots(team.getBoots());
		inv.setLeggings(team.getLeggings());
		inv.setChestplate(team.getChestplate());
		
		WeaponType[] weapons = getKit().getWeapons();
		for(int i = 0; i < weapons.length; i++) {
			WeaponType type = weapons[i];
			giveWeapon(type.createWeapon(this, i));
		}
		
		giveWeapon(getKit().getSpecial().createWeapon(this, 6));
		
		inv.setItem(6, Items.LOCKED_ULT_ITEM);
		inv.setItem(7, new ItemBuilder(Material.DRIED_KELP).setDisplayName("§7?").setLore("§7Cet item ne sert a rien...").build());
		inv.setItem(8, Items.MENU_ITEM);
		
		toBukkit().setWalkSpeed(getKit().getSpeed());
		toBukkit().getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(getKit().getMaxHealth());
		toBukkit().setHealth(getKit().getMaxHealth());
		
		toBukkit().setAllowFlight(getKit().equals(Kit.SCOUT));
	}
}
