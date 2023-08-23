package fr.lumin0u.teamfortress2.weapons.types;

import fr.lumin0u.teamfortress2.TF;
import fr.lumin0u.teamfortress2.TFEntity;
import fr.lumin0u.teamfortress2.game.GameManager;
import fr.lumin0u.teamfortress2.game.TFPlayer;
import fr.lumin0u.teamfortress2.util.ItemBuilder;
import fr.lumin0u.teamfortress2.weapons.*;
import org.bukkit.*;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Particle.DustOptions;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public final class WeaponTypes
{
	public static final WeaponType CANON_SCIE = new ShotgunType(false, Material.GOLDEN_SHOVEL, "Canon scié", 2, 48, 14, 4, 12, Math.PI / 50, 0.15, 3);
	
	// mine -> candle
	public static final MeleeWeaponType BATTE = new MeleeWeaponType(false, Material.GOLDEN_SWORD, "Batte", 1, -1, 3, 0.3);
	public static final GunType DEFENSEUR = new GunType(false, Material.WOODEN_HOE, "Défenseur", 1, 20, -1, 2, 30, Math.PI / 200, 0.25);
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
	public static final MeleeWeaponType KUKRI = new MeleeWeaponType(true, Material.IRON_SWORD, "Kukri", 2, -1, 12, 0.3);
	public static final EngiTurretType TURRET = new EngiTurretType();
	public static final WeaponType RED_BUTTON = new WeaponType(true, Material.TOTEM_OF_UNDYING, "Invincibilité", 1, -1, -1) {
		
		private Vector randomPositionIn(BoundingBox box) {
			Random r = new Random();
			return new Vector(r.nextDouble(box.getMinX(), box.getMaxX()), r.nextDouble(box.getMinY(), box.getMaxY()), r.nextDouble(box.getMinZ(), box.getMaxZ()));
		}
		
		@Override
		public void rightClickAction(TFPlayer player, Weapon weapon, RayTraceResult info) {
			final int duration = 5 * 20;
			player.setEngiInvicible(true);
			weapon.useAmmo();
			((PlaceableWeapon)player.getWeapon(MINE)).pickupAmmo();
			
			new BukkitRunnable()
			{
				int counter = 0;
				@Override
				public void run() {
					if(!player.isOnline() || counter == duration || player.isDead()) {
						cancel();
						return;
					}
					World w = player.toBukkit().getWorld();
					for(int i = 0; i < 3; i++) {
						w.spawnParticle(Particle.REDSTONE, randomPositionIn(player.toBukkit().getBoundingBox().expand(0.5)).toLocation(w), 1, 0, 0, 0, 0, new DustOptions(Color.RED, 0.5f), true);
					}
					counter++;
				}
			}.runTaskTimer(TF.getInstance(), 1, 1);
			
			Bukkit.getScheduler().runTaskLater(TF.getInstance(), () -> player.setEngiInvicible(false), duration);
		}
		
		@Override
		public void leftClickAction(TFPlayer player, Weapon weapon, RayTraceResult info) {
		}
	};
	public static final WeaponType CLE_A_MOLETTE = new MeleeWeaponType(false, Material.SHEARS, "Clé à molette", 1, -1, 2, 0.2) {
		@Override
		public void rightClickAction(TFPlayer player, Weapon wrench, RayTraceResult info) {
			//if(info != null && info.getHitBlock() != null) {
				player.getWeapons().stream()
						.filter(w -> w.getType() instanceof PlaceableWeaponType)
						.flatMap(w -> new ArrayList<>(((PlaceableWeapon) w).getBlocks()).stream()) // new ArrayList... => concurrent modification
						.filter(block -> block.isClicked(info))
						.forEach(block -> {
							block.getWeapon().getType().wrenchPickup(player, block.getWeapon(), block);
							player.toBukkit().swingMainHand();
						});
			//}
		}
	};
	public static final WeaponType TRAMPOLINE = new PlaceableWeaponType(false, Material.LIGHT_WEIGHTED_PRESSURE_PLATE, "Trampoline", 1) {
		@Override
		public PlacedBlockWeapon placeBlock(TFPlayer player, PlaceableWeapon weapon, Block block) {
			PlacedBlockWeapon trampo = new PlacedBlockWeapon(player, weapon, block) {
				ArmorStand armorStand;
				@Override
				public void place() {
					block.setType(Material.LIGHT_WEIGHTED_PRESSURE_PLATE, false);
					armorStand = (ArmorStand) block.getWorld().spawnEntity(block.getLocation().add(0.5, 0, 0.5), EntityType.ARMOR_STAND);
					armorStand.setInvulnerable(true);
					armorStand.setInvisible(true);
					armorStand.setCanMove(false);
					armorStand.setSmall(true);
					armorStand.getEquipment().setBoots(new ItemBuilder(Material.LEATHER_BOOTS).setLeatherColor(player.getTeam().cosmoxTeam().getMaterialColor()).build());
					armorStand.getEquipment().setLeggings(new ItemBuilder(Material.LEATHER_LEGGINGS).setLeatherColor(player.getTeam().cosmoxTeam().getMaterialColor()).build());
					armorStand.setCustomName(player.getTeam().cosmoxTeam().getColor() + "^^^^^^");
					armorStand.setCustomNameVisible(true); //done compile
				}
				
				@Override
				public void destroy() {
					block.setType(Material.AIR, false);
					armorStand.remove();
				}
				
				@Override
				public boolean isClicked(RayTraceResult rayTrace) {
					return super.isClicked(rayTrace) || (rayTrace.getHitEntity() != null && rayTrace.getHitEntity().equals(armorStand));
				}
				
				@Override
				public void onWalkOn(TFEntity walker) {
					if(!owner.isEnemy(walker)) {
						walker.getEntity().setVelocity(walker.getEntity().getVelocity().setY(1));
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
					block.setType(Material.STONE_PRESSURE_PLATE, false);
				}
				
				@Override
				public void destroy() {
					block.setType(Material.AIR, false);
				}
				
				@Override
				public void onWalkOn(TFEntity walker) {
					if(player.isEnemy(walker)) {
						GameManager.getInstance().explosion(owner, block.getLocation().add(0.5, 0.1, 0.5), 10, 5, player::isEnemy, 0);
						weapon.removeBlock(this);
					}
				}
			};
			mine.place();
			return mine;
		}
	};
	public static final GunType SNIPER = new Sniper.SniperType();
	public static final GunType MITRAILLETTE = new GunType(false, Material.IRON_HOE, "Carabine du Nettoyeur", 1, 9, -1, 2, 30, Math.PI / 90, 0.4);
	public static final WeaponType HEALTH_POTION = new WeaponType(false, Material.POTION, "Potion de vie", 1, 7*20, -1) {
		@Override
		public void rightClickAction(TFPlayer player, Weapon weapon, RayTraceResult info) {
			player.toBukkit().setHealth(Math.min(player.toBukkit().getHealth() + 8, player.getKit().getMaxHealth()));
			weapon.useAmmo();
		}
		
		@Override
		public void leftClickAction(TFPlayer player, Weapon weapon, RayTraceResult info) {}
		
		@Override
		public ItemBuilder buildItem(Weapon weapon) {
			return super.buildItem(weapon).setPotionColor(PotionEffectType.HEAL.getColor());
		}
	};
	public static final WeaponType MANETTE = new WeaponType(false, Material.COMPARATOR, "Manette", 1, -1, 10) {
		@Override
		public void rightClickAction(TFPlayer player, Weapon weapon, RayTraceResult info) { // handled in the weapon class
		}
		@Override
		public void leftClickAction(TFPlayer player, Weapon weapon, RayTraceResult info) {
		}
	};
	public static final RocketLauncherType ROCKET_LAUNCHER = new RocketLauncherType();
	public static final ShotgunType STD_SHOTGUN = new ShotgunType(false, Material.IRON_SHOVEL, "Fusil à pompe", 1, 64, -1, 4, 15, Math.PI / 40, 0.1, 5);
	public static final WeaponType FLASHBANG = new WeaponType(false, Material.FIREWORK_ROCKET, "Grenade flash", 1, 15*20+18, -1) {
		static final AtomicInteger flashcount = new AtomicInteger(0);
		@Override
		public void rightClickAction(TFPlayer player, Weapon weapon, RayTraceResult info) {
			Vector direction = player.getEyeLocation().getDirection();
			Location loc = player.getEyeLocation().add(direction);
			
			Item item = (Item) player.toBukkit().getWorld().dropItem(loc, new ItemBuilder(Material.ENDER_EYE).setDisplayName(flashcount.incrementAndGet() + "").build());
			item.setVelocity(direction);
			item.setPickupDelay(Integer.MAX_VALUE);
			
			weapon.useAmmo();
			
			new ThrownExplosive(player, FLASHBANG, 20) {
				@Override
				public void tick() {}
				
				@Override
				public void explode() {
					Location loc = item.getLocation();
					
					GameManager.getInstance().getLivingEntities().forEach(entity -> {
						if(entity.getLocation().distance(loc) < 5) {
							final int duration = 20*5;
							
							entity.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, duration, 0, false, false));
							
							if(entity instanceof TFPlayer hitPlayer) {
								hitPlayer.toBukkit().getInventory().setHelmet(new ItemStack(Material.BLACK_CONCRETE));
								
								Bukkit.getScheduler().runTaskLater(TF.getInstance(), () -> {
									if(!hitPlayer.isDead()) {
										hitPlayer.toBukkit().getInventory().setHelmet(new ItemStack(Material.AIR));
									}
								}, duration);
							}
						}
					});
					
					Firework fw = (Firework)loc.getWorld().spawn(loc, Firework.class);
					
					FireworkMeta fwMeta = fw.getFireworkMeta();
					fwMeta.addEffect(FireworkEffect.builder().trail(false).with(Type.BALL).withColor(Color.WHITE).build());
					fw.setFireworkMeta(fwMeta);
					Bukkit.getScheduler().runTaskLater(TF.getInstance(), fw::detonate, 1);
					
					remove();
				}
				
				@Override
				public Entity getEntity() {
					return item;
				}
			};
		}
		
		@Override
		public void leftClickAction(TFPlayer player, Weapon weapon, RayTraceResult info) {
		}
	};
	public static final GunType SCAVENGER = new GunType(true, Material.NETHERITE_HOE, "Scavenger", 2, -1, 10, 0, 100, 0, 0, true) {
		@Override
		public void onEntityHit(Hit hit) {
			hit.hitEntity().setPoisonSource(hit.player());
			int duration = 100;
			duration += Optional.ofNullable(hit.hitEntity().getEntity().getPotionEffect(PotionEffectType.POISON)).map(PotionEffect::getDuration).orElse(0);
			hit.hitEntity().getEntity().addPotionEffect(new PotionEffect(PotionEffectType.POISON, duration, 1, false, true, true));
		}
		
		@Override
		public void particle(Location l) {
			Random r = new Random();
			//for(int i = 0; i < 3; i++)
				l.getWorld().spawnParticle(Particle.GLOW, l.clone().add(new Vector(r.nextDouble()-0.5, r.nextDouble()-0.5, r.nextDouble()-0.5).multiply(0.2)), 1);
		}
	};
	
	private WeaponTypes() {
	}
}
