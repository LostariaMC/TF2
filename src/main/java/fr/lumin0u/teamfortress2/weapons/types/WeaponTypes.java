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

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class WeaponTypes
{
	static final AtomicInteger DROPPED_ITEM_COUNT = new AtomicInteger(0);
	
	public static final WeaponType CANON_SCIE = new ShotgunType(false, Material.GOLDEN_SHOVEL, "Canon scié", 2, 48, 14, 4, 12, Math.PI / 50, 0.15, 3);
	
	// mine -> candle
	public static final MeleeWeaponType CLUB = new MeleeWeaponType(false, Material.GOLDEN_SWORD, "Batte", 1, -1, 3, 0.3);
	public static final GunType DEFENSEUR = new GunType(false, Material.WOODEN_HOE, "Défenseur", 1, 20, -1, 2, 30, Math.PI / 200, 0.25);
	public static final WeaponType SCOUT_RACE = new WeaponType(true, Material.DRAGON_BREATH, "Scout race", 1, -1, -1)
	{
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
	public static final WeaponType RED_BUTTON = new WeaponType(true, Material.TOTEM_OF_UNDYING, "Invincibilité", 1, -1, -1)
	{
		
		private Vector randomPositionIn(BoundingBox box) {
			Random r = new Random();
			return new Vector(r.nextDouble(box.getMinX(), box.getMaxX()), r.nextDouble(box.getMinY(), box.getMaxY()), r.nextDouble(box.getMinZ(), box.getMaxZ()));
		}
		
		@Override
		public void rightClickAction(TFPlayer player, Weapon weapon, RayTraceResult info) {
			final int duration = 5 * 20;
			player.setEngiInvicible(true);
			weapon.useAmmo();
			((PlaceableWeapon) player.getWeapon(MINE)).pickupAmmo();
			
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
	public static final WeaponType CLE_A_MOLETTE = new MeleeWeaponType(false, Material.SHEARS, "Clé à molette", 1, -1, 2, 0.2)
	{
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
	public static final WeaponType TRAMPOLINE = new PlaceableWeaponType(false, Material.LIGHT_WEIGHTED_PRESSURE_PLATE, "Trampoline", 1)
	{
		@Override
		public PlacedBlockWeapon placeBlock(TFPlayer player, PlaceableWeapon weapon, Block block) {
			PlacedBlockWeapon trampo = new PlacedBlockWeapon(player, weapon, block)
			{
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
	public static final WeaponType MINE = new PlaceableWeaponType(false, Material.STONE_PRESSURE_PLATE, "Mine", 3)
	{
		@Override
		public PlacedBlockWeapon placeBlock(TFPlayer player, PlaceableWeapon weapon, Block block) {
			PlacedBlockWeapon mine = new PlacedBlockWeapon(player, weapon, block)
			{
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
	public static final WeaponType HEALTH_POTION = new WeaponType(false, Material.POTION, "Potion de vie", 1, 7 * 20, -1)
	{
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
	public static final WeaponType MANETTE = new WeaponType(false, Material.COMPARATOR, "Manette", 1, -1, 10)
	{
		@Override
		public void rightClickAction(TFPlayer player, Weapon weapon, RayTraceResult info) { // handled in the weapon class
		}
		
		@Override
		public void leftClickAction(TFPlayer player, Weapon weapon, RayTraceResult info) {
		}
	};
	public static final RocketLauncherType ROCKET_LAUNCHER = new RocketLauncherType();
	public static final ShotgunType STD_SHOTGUN = new ShotgunType(false, Material.IRON_SHOVEL, "Fusil à pompe", 1, 64, -1, 3.5, 15, Math.PI / 40, 0.1, 5);
	public static final WeaponType FLASHBANG = new WeaponType(false, Material.FIREWORK_ROCKET, "Grenade flash", 1, 15 * 20 + 18, -1)
	{
		@Override
		public void rightClickAction(TFPlayer player, Weapon weapon, RayTraceResult info) {
			Vector direction = player.getEyeLocation().getDirection();
			Location loc = player.getEyeLocation().add(direction);
			
			Item item = (Item) player.toBukkit().getWorld().dropItem(loc, new ItemBuilder(Material.ENDER_EYE).setDisplayName(DROPPED_ITEM_COUNT.incrementAndGet() + "").build());
			item.setVelocity(direction);
			item.setPickupDelay(Integer.MAX_VALUE);
			
			weapon.useAmmo();
			
			new ThrownExplosive(player, FLASHBANG, 20, false)
			{
				@Override
				public void tick() {}
				
				@Override
				public void explode() {
					Location loc = item.getLocation();
					
					GameManager.getInstance().getLivingEntities().forEach(entity -> {
						if(entity.getLocation().distance(loc) < 5) {
							final int duration = 20 * 5;
							
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
					
					Firework fw = (Firework) loc.getWorld().spawn(loc, Firework.class);
					
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
	public static final GunType SCAVENGER = new GunType(true, Material.NETHERITE_HOE, "Scavenger", 2, -1, 10, 0, 100, 0, 0, true)
	{
		@Override
		public void onEntityHit(Hit hit) {
			hit.hitEntity().setPoisonSource(hit.player());
			int duration = 100;
			duration += Optional.ofNullable(hit.hitEntity().getEntity().getPotionEffect(PotionEffectType.POISON)).map(PotionEffect::getDuration).orElse(0);
			hit.hitEntity().getEntity().addPotionEffect(new PotionEffect(PotionEffectType.POISON, duration, 1, false, true, true));
		}
		
		@Override
		public void particle(Location l, int i) {
			Random r = new Random();
			//for(int i = 0; i < 3; i++)
			l.getWorld().spawnParticle(Particle.GLOW, l.clone().add(new Vector(r.nextDouble() - 0.5, r.nextDouble() - 0.5, r.nextDouble() - 0.5).multiply(0.0)), 1);
		}
	};
	public static final WeaponType DYNAMITE = new WeaponType(false, Material.RED_CANDLE, "Dynamite", 1, 158, -1)
	{
		@Override
		public void rightClickAction(TFPlayer player, Weapon weapon, RayTraceResult info) {
			Vector direction = player.getEyeLocation().getDirection();
			Location loc = player.getEyeLocation().add(direction);
			
			Item item = (Item) player.toBukkit().getWorld().dropItem(loc, new ItemBuilder(Material.RED_CANDLE).setDisplayName(DROPPED_ITEM_COUNT.incrementAndGet() + "").build());
			item.setVelocity(direction.clone().multiply(0.6));
			item.setPickupDelay(Integer.MAX_VALUE);
			
			weapon.useAmmo();
			
			new ThrownExplosive(player, SMOKE, 35, false)
			{
				@Override
				public void tick() {}
				
				@Override
				public void explode() {
					Location loc = item.getLocation();
					GameManager.getInstance().explosion(player, loc, 15, 6, player::isEnemy, 0.7);
					remove();
				}
				
				@Override
				public Entity getEntity() {
					return item;
				}
			};
		}
		
		@Override
		public void leftClickAction(TFPlayer player, Weapon weapon, RayTraceResult info) {}
	};
	public static final WeaponType SMOKE = new WeaponType(false, Material.BRUSH, "Fumigène", 1, 278, -1)
	{
		@Override
		public void rightClickAction(TFPlayer player, Weapon weapon, RayTraceResult info) {
			Vector direction = player.getEyeLocation().getDirection();
			Location loc = player.getEyeLocation().add(direction);
			
			Item item = (Item) player.toBukkit().getWorld().dropItem(loc, new ItemBuilder(Material.BRUSH).setDisplayName(DROPPED_ITEM_COUNT.incrementAndGet() + "").build());
			item.setVelocity(direction.clone().multiply(0.6));
			item.setPickupDelay(Integer.MAX_VALUE);
			
			weapon.useAmmo();
			
			new ThrownExplosive(player, SMOKE, 35, false)
			{
				@Override
				public void tick() {}
				
				@Override
				public void explode() {
					//List<Location> smokeBlocks = new ArrayList<>();
					Map<BlockDisplay, Integer> smokeDisplayTimes = new HashMap<>();
					Location loc = item.getLocation();
					for(int x = 0; x < 11; x++) {
						for(int y = 0; y < 11; y++) {
							for(int z = 0; z < 11; z++) {
								Location l = loc.clone().add(x - 5.5, y - 5.5, z - 5.5);
								if(l.distanceSquared(loc) < 25.01/* =5*5 */) {
									/*if(l.getBlock().getType() == Material.AIR) {
										smokeBlocks.add(l);
										l.getBlock().setType(Material.TRIPWIRE);
									}*/
									
									BlockDisplay smokeDisplay = (BlockDisplay) l.getWorld().spawnEntity(l.toCenterLocation(), EntityType.BLOCK_DISPLAY);
									smokeDisplay.setBlock(Material.COBWEB.createBlockData());
									smokeDisplayTimes.put(smokeDisplay, (int) new Random().nextGaussian(130, 5));
								}
							}
						}
					}
					//final int duration = 120;
					new BukkitRunnable()
					{
						int i = 0;
						@Override
						public void run() {
							i++;
							smokeDisplayTimes.keySet().forEach(blockDisplay -> blockDisplay.setRotation(new Random().nextFloat() * 360, new Random().nextFloat() * 180 - 90));
							for(BlockDisplay e : new HashSet<>(smokeDisplayTimes.keySet())) {
								if(smokeDisplayTimes.get(e) < i) {
									smokeDisplayTimes.remove(e);
									e.remove();
								}
							}
							
							if(smokeDisplayTimes.isEmpty()) {
								cancel();
								return;
							}
						}
					}.runTaskTimer(TF.getInstance(), 1, 1);
					remove();
				}
				
				@Override
				public Entity getEntity() {
					return item;
				}
			};
		}
		
		@Override
		public void leftClickAction(TFPlayer player, Weapon weapon, RayTraceResult info) {}
	};
	public static final FlareGunType FLARE_GUN = new FlareGunType();
	public static final StrikerType STRIKER = new StrikerType();
	public static final GunType TORNADO = new GunType(false, Material.GOLDEN_HOE, "La Tornade", 1, -1, 3, 1.5, 25, Math.PI / 80, 0.1) {
		@Override
		public Weapon createWeapon(TFPlayer owner, int slot) {
			return new Scopeable(TORNADO, owner, slot) {
				@Override
				public void scopeEffect() {
					updateItem();
				}
				@Override
				public void unscopeEffect() {
					updateItem();
				}
			};
		}
		
		@Override
		public ItemBuilder buildItem(Weapon weapon) {
			return super.buildItem(weapon).setGlow(((Scopeable)weapon).isScoping());
		}
		
		@Override
		public void rightClickAction(TFPlayer player, Weapon weapon, RayTraceResult info) {
			boolean scoping = ((Scopeable)weapon).isScoping();
			
			Location source = player.getEyeLocation();
			Vector direction = player.getEyeLocation().getDirection();
			player.toBukkit().setVelocity(player.toBukkit().getVelocity().subtract(direction.clone().multiply(0.05)));
			
			double j = player.heavyBulletNb().getAndIncrement();
			double radius = 0.20;
			
			Vector x1 = new Vector(direction.getZ(), 0d, direction.getX()).normalize();
			Vector x2 = direction.clone().crossProduct(x1).normalize();
			source.add(x1.clone().multiply(radius * Math.sin(j / 10 * Math.PI * 2d))).add(x2.clone().multiply(radius * Math.cos(j / 10 * Math.PI * 2d)));
			
			shoot(player, source, direction, weapon, scoping ? inaccuracy / 2 : inaccuracy, this::particle, GameManager.getInstance().getLivingEntities());
			
			weapon.useAmmo();
		}
	};
	public static final MeleeWeaponType MACHETE = new MeleeWeaponType(false, Material.STONE_SWORD, "Machette", 1, -1, 3, 0.15);
	public static final WeaponType BEAST_FURY = new WeaponType(true, Material.MUTTON, "Beast Fury", 1, -1, -1) {
		
		@Override
		public void rightClickAction(TFPlayer player, Weapon weapon, RayTraceResult info) {
			player.toBukkit().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 2));
			player.toBukkit().addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, 300, 1));
			
			weapon.useAmmo();
		}
		
		@Override
		public void leftClickAction(TFPlayer player, Weapon weapon, RayTraceResult info) {}
	};
	
	
	private WeaponTypes() {
	}
}
