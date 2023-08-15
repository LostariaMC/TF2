package fr.lumin0u.teamfortress2.game;

import fr.lumin0u.teamfortress2.*;
import fr.lumin0u.teamfortress2.util.ItemBuilder;
import fr.lumin0u.teamfortress2.weapons.Weapon;
import fr.lumin0u.teamfortress2.weapons.types.WeaponType;
import fr.worsewarn.cosmox.api.players.WrappedPlayer;
import fr.worsewarn.cosmox.game.teams.Team;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static fr.lumin0u.teamfortress2.Kit.*;

public class TFPlayer extends WrappedPlayer implements TFEntity
{
	protected FireCause fireCause;
	private TFPlayer poisonSource;
	
	private Kit kit;
	private Kit nextKit;
	private TFTeam team;
	
	private List<Weapon> weapons;
	
	private boolean inScope;
	private boolean heavyRage;
	private final AtomicInteger heavyBulletNb;
	
	/*private Location c4Location;
	private TurretInfo turretInfo;
	private Location trampoLocation;
	private final List<Location> mineLocations;*/
	
	private boolean spyInvisible;
	private WrappedPlayer disguise;
	
	private boolean engiInvicible;
	
	private boolean energized;
	private boolean canDoubleJump;
	
	private final HashMap<TFPlayer, Integer> lastDamagers;
	private long lastHitDate;
	private int killCount;
	
	//private RClickingPlayerTask rClickingTask;
	private boolean dead;
	
	public TFPlayer(UUID uuid) {
		super(uuid);
		
		/*rClickingTask = new RClickingPlayerTask(this);
		rClickingTask.runTaskTimer(TF.getInstance(), 1, 1);*/
		
		this.nextKit = TF.getInstance().getKitFromRedis(this);
		setKit(nextKit);
		weapons = new ArrayList<>();
		
		this.lastDamagers = new HashMap<>();
		this.lastHitDate = 0;
		
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
	
	public boolean canDamage(TFEntity entity) {
		return team != null && !team.equals(entity.getTeam());
	}
	
	public void setTeam(TFTeam team) {
		this.team = team;
		
		// TODO update scoreboard ?
	}
	
	/**
	 * @return the weapon of the given type the player has. <br />
	 * if the player has not a weapon of this type, returns null
	 * */
	public <T extends WeaponType, E extends Weapon> E getWeapon(T type) {
		return (E) weapons.stream().filter(w -> w.getType().equals(type)).findAny().orElse(null);
	}
	
	public <T extends WeaponType, E extends Weapon> Optional<E> getWeaponInHand() {
		return (Optional<E>) weapons.stream().filter(w -> w.getType().isItem(toBukkit().getInventory().getItemInMainHand())).findAny();
	}
	
	public void giveWeapon(Weapon weapon) {
		weapon.giveItem();
		weapons.add(weapon);
	}
	
	public void removeWeapon(Weapon weapon) {
		weapon.giveItem();
		weapons.remove(weapon);
	}
	
	public void removeWeapon(WeaponType weaponType) {
		weapons.removeIf(w -> w.getType().equals(weaponType));
	}
	
	public boolean hasWeapon(Weapon weapon) {
		return weapons.contains(weapon);
	}
	
	public List<Weapon> getWeapons() {
		return new ArrayList<>(weapons);
	}
	
	@Override
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
	
	public void setKit(Kit kit) {
		if(kit == Kit.RANDOM)
			this.kit = Kit.getRealRandomKit(new Random());
		else
			this.kit = kit;
	}
	
	public Kit getNextKit() {
		return nextKit;
	}
	
	public void setNextKit(Kit nextKit) {
		this.nextKit = nextKit;
	}
	
	/*public Location getC4Location() {
		return c4Location;
	}
	
	public void setC4Location(Location c4Location) {
		this.c4Location = c4Location;
	}
	
	public TurretInfo getTurretInfo() {
		return turretInfo;
	}
	
	public void setTurretInfo(TurretInfo turretInfo) {
		this.turretInfo = turretInfo;
	}
	
	public Location getTrampoLocation() {
		return trampoLocation;
	}
	
	public void setTrampoLocation(Location trampoLocation) {
		this.trampoLocation = trampoLocation;
	}
	
	public List<Location> getMineLocations() {
		return mineLocations;
	}*/
	
	public HashMap<TFPlayer, Integer> getLastDamagers() {
		return lastDamagers;
	}
	
	public void addDamager(TFPlayer damager, double damage) {
		lastDamagers.put(damager, (lastDamagers.get(damager) != null ? lastDamagers.get(damager) : 0) + (int) (damage * 70));
	}
	
	public long getLastHitDate() {
		return lastHitDate;
	}
	
	public void setLastHitDate(long lastHitDate) {
		this.lastHitDate = lastHitDate;
	}
	
	public AtomicInteger heavyBulletNb() {
		return heavyBulletNb;
	}
	
	public void setRandomKit(boolean randomKit) {
		this.nextKit = Kit.RANDOM;
	}
	
	public boolean isEnergized() {
		return energized;
	}
	
	public void setEnergized(boolean energized) {
		this.energized = energized;
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
	public LivingEntity getEntity() {
		return toBukkit();
	}
	
	@Override
	public boolean isDead() {
		return dead;
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
	public void damage(TFPlayer damager, double amount, Vector knockback) {
		if(!isOnline())
			return;
		
		if(isEngiInvicible() || getTeam().getSafeZone().contains(getLocation().toVector()))
			return;
		
		addDamager(damager, amount);
		
		if(toBukkit().getHealth() <= amount) {
			toBukkit().setGameMode(GameMode.SPECTATOR);
			new ArrayList<>(weapons).forEach(this::removeWeapon);
		} else {
			toBukkit().damage(amount);
		}
		
		toBukkit().setVelocity(toBukkit().getVelocity().multiply(0.5).add(knockback));
	}
	
	public void respawn(Location location) {
		toBukkit().teleport(location);
		toBukkit().setGameMode(GameMode.ADVENTURE);
		
		setKit(nextKit);
		
		PlayerInventory inv = toBukkit().getInventory();
		inv.clear();
		inv.setBoots(team.getBoots());
		inv.setLeggings(team.getLeggings());
		inv.setChestplate(team.getChestplate());
		
		WeaponType[] weapons = getKit().getWeapons();
		for(int i = 0; i < weapons.length; i++) {
			WeaponType type = weapons[i];
			type.createWeapon(this, i).giveItem();
		}
		
		inv.setItem(7, TF.LOCKED_ULT_ITEM);
		inv.setItem(8, TF.MENU_ITEM);
	}
}
