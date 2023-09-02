package fr.lumin0u.teamfortress2.weapons;

import fr.lumin0u.teamfortress2.TF;
import fr.lumin0u.teamfortress2.TFEntity;
import fr.lumin0u.teamfortress2.game.managers.GameManager;
import fr.lumin0u.teamfortress2.game.TFPlayer;
import fr.lumin0u.teamfortress2.util.ComplexEntity;
import fr.lumin0u.teamfortress2.util.TFSound;
import fr.lumin0u.teamfortress2.util.Utils;
import fr.lumin0u.teamfortress2.weapons.types.WeaponTypes;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class EngiTurret extends PlacedBlockWeapon {
	public static final int RELOAD_TICKS = 7 * 20;
	
	private float yaw, pitch;
	
	private ComplexEntity turretHead;
	private ComplexEntity turretGrounded;
	private ArmorStand tipDisplay;
	private final List<Obus> obuses = new ArrayList<>();
	
	private int mortarReloadTicks;
	
	private Controller controller;
	private boolean removed;
	
	private static final String READY_TO_SHOOT = "Prêt à tirer";
	
	public EngiTurret(TFPlayer owner, PlaceableWeapon weapon, Block block) {
		super(owner, weapon, block);
		controller = new Controller(owner, weapon.getSlot());
	}
	
	@Override
	public void place(BlockFace against) {
		block.setType(Material.MUD_BRICK_SLAB);
		block.getRelative(BlockFace.UP).setBlockData(Material.MUD_BRICK_WALL.createBlockData(), false);
		
		//build mortar
		
		BlockDisplay hopper = (BlockDisplay) block.getWorld().spawnEntity(block.getLocation(), EntityType.BLOCK_DISPLAY);
		hopper.setBlock(Material.HOPPER.createBlockData());
		
		BlockDisplay cauldron = (BlockDisplay) block.getWorld().spawnEntity(block.getLocation(), EntityType.BLOCK_DISPLAY);
		cauldron.setBlock(Material.CAULDRON.createBlockData());
		
		turretHead = new ComplexEntity(getHeadLocation(),
//				new ComplexEntity.ComplexDisplayPart(hopper, new Vector(0, 0, -0.2), 0, 90),
//				new ComplexEntity.ComplexDisplayPart(cauldron, new Vector(0, 0, 0.6), 0, 90)
				new ComplexEntity.ComplexDisplayPart(hopper, new Vector(-0.5, 0, -0.2 - 0.5), 0, 90),
				new ComplexEntity.ComplexDisplayPart(cauldron, new Vector(-0.5, 0, 0.6 - 0.5), 0, 90)
		);
		
		BlockDisplay mudWallDisplay = (BlockDisplay) block.getWorld().spawnEntity(block.getLocation(), EntityType.BLOCK_DISPLAY);
		mudWallDisplay.setBlock(Material.MUD_BRICK_WALL.createBlockData());
		
		turretGrounded = new ComplexEntity(block.getLocation(),
				new ComplexEntity.ComplexDisplayPart(mudWallDisplay, new Vector(0, 0, 0), 0, 0)
		);
		
		updateDirection(0, 0);
		
		tipDisplay = (ArmorStand) block.getWorld().spawnEntity(block.getLocation().toCenterLocation(), EntityType.ARMOR_STAND);
		tipDisplay.setCustomName("Construction...");
		tipDisplay.setCustomNameVisible(true);
		tipDisplay.setInvisible(true);
		tipDisplay.setInvulnerable(true);
		tipDisplay.setGravity(false);
		tipDisplay.setCanMove(false);
		
		mortarReloadTicks = 1;
		
		new BukkitRunnable() {
			int tick = 0;
			int nextSound = 0;
			@Override
			public void run() {
				if(removed) {
					cancel();
					return;
				}
				
				final int constructTime = 100;
				final int soundDuration = 40;
				
				tick += 5;
				nextSound -= 5;
				
				if(nextSound <= 0 && tick + soundDuration < constructTime) {
					TFSound.TURRET_CONSTRUCT.play(block.getLocation());
					nextSound = new Random().nextInt(30) + 25;
				}
				
				if(tick >= constructTime) {
					owner.giveWeapon(controller);
					mortarReloadTicks = 0;
					tipDisplay.setCustomName(READY_TO_SHOOT);
					TFSound.TURRET_CONSTRUCT_READY.play(block.getLocation());
					TFSound.TURRET_READY.playTo(owner);
					cancel();
					return;
				}
			}
		}.runTaskTimer(TF.getInstance(), 5, 5);
	}
	
	private void reload() {
		mortarReloadTicks = RELOAD_TICKS;
		
		new BukkitRunnable() {
			@Override
			public void run() {
				mortarReloadTicks--;
				if(removed) {
					cancel();
					return;
				}
				
				final String dashes = "-".repeat((mortarReloadTicks % 20) / 4);
				tipDisplay.setCustomName(dashes + " " + (mortarReloadTicks / 20) + " " + dashes);
				
				if(mortarReloadTicks == 0) {
					cancel();
					tipDisplay.setCustomName(READY_TO_SHOOT);
					TFSound.TURRET_READY.playTo(owner);
				}
			}
		}.runTaskTimer(TF.getInstance(), 1, 1);
	}
	
	public boolean isReloading() {
		return mortarReloadTicks > 1;
	}
	
	public void shoot() {
		if(mortarReloadTicks > 0) {
			return;
		}
		
		TFSound.TURRET_SHOOT.play(getHeadLocation());
		
		block.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, getHeadLocation(), 1);
		obuses.add(new Obus());
		reload();
	}
	
	@Override
	public void destroy() {
		owner.removeWeapon(controller);
		turretGrounded.remove();
		turretHead.remove();
		tipDisplay.remove();
		block.setType(Material.AIR);
		block.getRelative(BlockFace.UP).setType(Material.AIR);
		removed = true;
	}
	
	public void updateDirection(float yaw, float pitch) {
		pitch = max(-45, min(45, pitch));
		turretHead.setRotation(yaw, pitch);
		this.yaw = yaw;
		this.pitch = pitch;
	}
	
	/* as of Location#getDirection */
	public Vector getDirection() {
		return Utils.yawPitchToDirection(yaw, pitch);
	}
	
	public Location getHeadLocation() {
		return block.getLocation().add(0.5, 2.4, 0.5);
	}
	
	@Override
	public void onWalkOn(TFEntity walker) {
	}
	
	public Controller getController() {
		return controller;
	}
	
	public class Controller extends Weapon {
		public Controller(TFPlayer owner, int slot) {
			super(WeaponTypes.MANETTE, owner, slot);
		}
		
		private void ifCloseEnough(Runnable runnable) {
			if(owner.getLocation().distance(block.getLocation()) < 10) {
				runnable.run();
			}
			else {
				owner.toBukkit().sendActionBar("§cTrop loin du canon !");
			}
		}
		
		@Override
		public void rightClick(RayTraceResult info) {
			if(lastActionDate + getType().getActionDelay() < TF.currentTick()) {
				ifCloseEnough(() -> {
					Location eyeLoc = owner.toBukkit().getEyeLocation();
					updateDirection(eyeLoc.getYaw(), eyeLoc.getPitch());
					TFSound.TURRET_DIRECTION.playTo(owner);
				});
				lastActionDate = TF.currentTick();
			}
		}
		
		@Override
		public void leftClick(RayTraceResult info) {
			ifCloseEnough(EngiTurret.this::shoot);
		}
	}
	
	private class Obus extends ComplexEntity {
		
		private Location loc;
		private Vector velocity;
		private boolean removed;
		
		private Obus() {
			super(getHeadLocation(), createObus(getHeadLocation()));
			
			this.loc = getHeadLocation();
			this.velocity = EngiTurret.this.getDirection().clone().multiply(2.5);
			
			new BukkitRunnable() {
				@Override
				public void run() {
					tick();
					
					if(Obus.this.removed) {
						cancel();
					}
				}
			}.runTaskTimer(TF.getInstance(), 1, 1);
		}
		
		private static Collection<ComplexDisplayPart> createObus(Location loc) {
			/*BlockDisplay stalagmite = (BlockDisplay) loc.getWorld().spawnEntity(loc, EntityType.BLOCK_DISPLAY);
			stalagmite.setBlock(Material.POINTED_DRIPSTONE.createBlockData(data -> ((PointedDripstone) data).setThickness(PointedDripstone.Thickness.MIDDLE)));*/
			
			ArmorStand blackstoneArmorstand = (ArmorStand) loc.getWorld().spawnEntity(loc.add(0, -1.7, 0), EntityType.ARMOR_STAND);
			blackstoneArmorstand.setGravity(false);
			blackstoneArmorstand.setInvulnerable(true);
			blackstoneArmorstand.setInvisible(true);
			blackstoneArmorstand.getEquipment().setHelmet(new ItemStack(Material.BLACKSTONE), true);
			
			return List.of(new ComplexDisplayPart(blackstoneArmorstand, new Vector(0, -1.7, 0), 0, 90) {
				@Override
				public void setRotation(float yaw, float pitch) {
					EulerAngle eu = new EulerAngle(0, pitch / 18 * Math.PI, 0);
					((ArmorStand)entity()).setHeadPose(eu);
				}
			});
		}
		
		public void tick() {
			final Vector gravity = new Vector(0, -1.8 / 20.0, 0);
			
			if(!loc.getBlock().equals(getHeadLocation().getBlock())) {
				if(loc.getWorld().rayTraceBlocks(loc, velocity, velocity.length()) != null) {
					explode();
					return;
				}
			}
			
			loc.add(velocity);
			velocity.multiply(0.995);
			velocity.add(gravity);
			
			if(velocity.isZero())
				velocity = gravity.clone();
			
			loc.setDirection(velocity);
			teleport(loc);
		}
		
		public void explode() {
			remove();
			
			GameManager.getInstance().explosion(owner, loc, 23, 13, owner::isEnemy, 2.5);
		}
		
		@Override
		public void remove() {
			super.remove();
			Obus.this.removed = true;
		}
	}
	
	@Override
	public boolean isClicked(RayTraceResult rayTrace) {
		return super.isClicked(rayTrace) || (rayTrace.getHitBlock() != null && rayTrace.getHitBlock().equals(block.getRelative(BlockFace.UP)));
	}
}
