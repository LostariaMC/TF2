package fr.lumin0u.teamfortress2.weapons.types;

import fr.lumin0u.teamfortress2.TF;
import fr.lumin0u.teamfortress2.TFEntity;
import fr.lumin0u.teamfortress2.game.GameManager;
import fr.lumin0u.teamfortress2.game.TFPlayer;
import fr.lumin0u.teamfortress2.weapons.PlaceableWeapon;
import fr.lumin0u.teamfortress2.weapons.PlacedBlockWeapon;
import fr.lumin0u.teamfortress2.weapons.Sniper;
import fr.lumin0u.teamfortress2.weapons.Weapon;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public final class Weapons {
	public static final WeaponType CANON_SCIE = new ShotgunType(false, Material.GOLDEN_SHOVEL, "Canon scié", 2, 48, 14, 4, 12, Math.PI / 30, 0.01, 3);
	
	// mine -> candle
	public static final MeleeWeaponType BATTE = new MeleeWeaponType(false, Material.GOLDEN_SWORD, "Batte", 1, -1, 3, 0.03);
	public static final GunType DEFENSEUR = new GunType(false, Material.WOODEN_PICKAXE, "Défenseur", 1, 20, -1, 3, 30, Math.PI / 100, 0.01);
	public static final WeaponType SCOUT_RACE = new WeaponType(true, Material.DRAGON_BREATH, "Scout race", 1, -1, -1) {
		@Override
		public void rightClickAction(TFPlayer player, Weapon weapon, RayTraceResult info) {
			final int duration = 10 * 20;
			
			player.toBukkit().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, 1, false, true, true));
			player.setEnergized(true);
			weapon.useAmmo();
			
			Bukkit.getScheduler().runTaskLater(TF.getInstance(), () -> player.setEnergized(false), duration);
		}
		
		@Override
		public void leftClickAction(TFPlayer player, Weapon weapon, RayTraceResult info) {
		}
	};
	public static final MeleeWeaponType KUKRI = new MeleeWeaponType(true, Material.IRON_SWORD, "Kukri", 2, -1, 12, 0.03);
	public static final EngiTurretType TURRET = EngiTurretType.INSTANCE;
	public static final WeaponType MANETTE = EngiTurretType.MANETTE;
	public static final WeaponType RED_BUTTON = new WeaponType(true, Material.HEART_OF_THE_SEA, "Invincibilité", 1, -1, -1) {
		@Override
		public void rightClickAction(TFPlayer player, Weapon weapon, RayTraceResult info) {
			final int duration = 5 * 20;
			player.setEngiInvicible(true);
			weapon.useAmmo();
			
			Bukkit.getScheduler().runTaskLater(TF.getInstance(), () -> player.setEngiInvicible(false), duration);
		}
		
		@Override
		public void leftClickAction(TFPlayer player, Weapon weapon, RayTraceResult info) {
		}
	};
	public static final WeaponType CLE_A_MOLETTE = new MeleeWeaponType(false, Material.IRON_NUGGET, "Clé à molette", 1, -1, 4, 0.01) {
		@Override
		public void rightClickAction(TFPlayer player, Weapon wrench, RayTraceResult info) {
			if(info != null && info.getHitBlock() != null) {
				player.getWeapons().stream()
						.filter(w -> w.getType() instanceof PlaceableWeaponType)
						.filter(w -> ((PlaceableWeapon) w).getBlocks().stream().anyMatch(block -> block.isClicked(info)))
						//.flatMap(w -> ((PlaceableWeapon)w).getBlocks().stream())
						.forEach(w -> {
							((PlaceableWeaponType) w.getType()).wrenchPickup(player, wrench, info.getHitBlock());
						});
			}
		}
	};
	public static final WeaponType TRAMPOLINE = new PlaceableWeaponType(false, Material.LIGHT_WEIGHTED_PRESSURE_PLATE, "Trampoline", 1) {
		@Override
		public PlacedBlockWeapon placeBlock(TFPlayer player, PlaceableWeapon weapon, Block block) {
			PlacedBlockWeapon trampo = new PlacedBlockWeapon(player, weapon, block) {
				@Override
				public void place() {
					block.setType(Material.LIGHT_WEIGHTED_PRESSURE_PLATE);
				}
				
				@Override
				public void destroy() {
					block.setType(Material.AIR);
				}
				
				@Override
				public void onWalkOn(TFEntity walker) {
					if(owner.equals(walker)) {
						owner.toBukkit().setVelocity(player.toBukkit().getVelocity().setY(0.5));
					}
				}
			};
			trampo.place();
			return trampo;
		}
	};
	public static final WeaponType MINE = new PlaceableWeaponType(false, Material.STONE_PRESSURE_PLATE, "Mine", 3) {
		@Override
		public PlacedBlockWeapon placeBlock(TFPlayer player, PlaceableWeapon weapon, Block block) {
			PlacedBlockWeapon mine = new PlacedBlockWeapon(player, weapon, block) {
				@Override
				public void place() {
					block.setType(Material.STONE_PRESSURE_PLATE);
				}
				
				@Override
				public void destroy() {
					block.setType(Material.AIR);
				}
				
				@Override
				public void onWalkOn(TFEntity walker) {
					if(player.canDamage(walker)) {
						GameManager.getInstance().explosion(owner, block.getLocation().add(0.5, 0.1, 0.5), 10, 5, player::canDamage, 0);
						destroy();
					}
				}
			};
			mine.place();
			return mine;
		}
	};
	// TODO real values
	public static final WeaponType SNIPER = new Sniper.SniperType();
	
	private Weapons() {
	}
}
