package fr.lumin0u.teamfortress2.weapons.types;

import fr.lumin0u.teamfortress2.TF;
import fr.lumin0u.teamfortress2.game.TFPlayer;
import fr.lumin0u.teamfortress2.weapons.Weapon;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.RayTraceResult;

public final class Weapons
{
	private Weapons() {}
	
	// mine -> candle
	
	public static final WeaponType CANON_SCIE = new ShotgunType(false, Material.GOLDEN_SHOVEL, "Canon scié", 2, 48, 14, 4, 12, Math.PI / 30, 0.01, 3);
	
	public static final WeaponType BATTE = new MeleeWeaponType(false, Material.GOLDEN_SWORD, "Batte", 1, -1, 3, 0.03);
	
	public static final GunType DEFENSEUR = new GunType(false, Material.WOODEN_PICKAXE, "Défenseur", 1, 20, -1, 3, 30, Math.PI / 100, 0.01);
	
	public static final WeaponType SCOUT_RACE = new WeaponType(true, Material.DRAGON_BREATH, "Scout race", 1, -1, -1) {
		@Override
		public void rightClickAction(TFPlayer player, Weapon<?> weapon, RayTraceResult info) {
			final int duration = 10*20;
			
			player.toBukkit().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, 1, false, true, true));
			player.setEnergized(true);
			
			Bukkit.getScheduler().runTaskLater(TF.getInstance(), () -> player.setEnergized(false), duration);
		}
		@Override
		public void leftClickAction(TFPlayer player, Weapon<?> weapon, RayTraceResult info) {}
	};
	
	public static final WeaponType KUKRI = new MeleeWeaponType(true, Material.IRON_SWORD, "Kukri", 2, -1, 12, 0.03);
	
	public static final WeaponType TURRET = EngiTurretType.INSTANCE;
	
	public static final WeaponType MANETTE = EngiTurretType.MANETTE;
}
