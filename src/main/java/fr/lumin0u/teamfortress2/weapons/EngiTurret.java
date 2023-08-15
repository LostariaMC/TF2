package fr.lumin0u.teamfortress2.weapons;

import fr.lumin0u.teamfortress2.TF;
import fr.lumin0u.teamfortress2.TFEntity;
import fr.lumin0u.teamfortress2.game.GameManager;
import fr.lumin0u.teamfortress2.game.TFPlayer;
import fr.lumin0u.teamfortress2.util.ComplexEntity;
import fr.lumin0u.teamfortress2.weapons.types.EngiTurretType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.PointedDripstone;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EngiTurret extends PlacedBlockWeapon {
	public static final int RELOAD_TICKS = 7 * 20;
	
	private Block block;
	private float yaw, pitch;
	
	private ComplexEntity turretHead;
	private ComplexEntity turretGrounded;
	private final List<Obus> obuses = new ArrayList<>();
	
	private int mortarReloadTicks;
	
	public EngiTurret(TFPlayer owner, PlaceableWeapon weapon, Block block) {
		super(owner, weapon, block);
	}
	
	@Override
	public void place() {
		block.setType(Material.MUD_BRICK_SLAB);
		block.getRelative(BlockFace.UP).setBlockData(Material.MUD_BRICK_WALL.createBlockData(), false);
		
		//build mortar
		
		BlockDisplay hopper = (BlockDisplay) block.getWorld().spawnEntity(block.getLocation(), EntityType.BLOCK_DISPLAY);
		hopper.setBlock(Material.HOPPER.createBlockData());
		
		BlockDisplay cauldron = (BlockDisplay) block.getWorld().spawnEntity(block.getLocation(), EntityType.BLOCK_DISPLAY);
		cauldron.setBlock(Material.CAULDRON.createBlockData());
		
		turretHead = new ComplexEntity(
				new ComplexEntity.ComplexDisplayPart(hopper, new Vector(0, 0, -0.2), 0, 90),
				new ComplexEntity.ComplexDisplayPart(cauldron, new Vector(0, 0, 0.6), 0, 90)
		);
		
		BlockDisplay mudWallDisplay = (BlockDisplay) block.getWorld().spawnEntity(block.getLocation(), EntityType.BLOCK_DISPLAY);
		mudWallDisplay.setBlock(Material.MUD_BRICK_WALL.createBlockData());
		
		turretGrounded = new ComplexEntity(
				new ComplexEntity.ComplexDisplayPart(mudWallDisplay, new Vector(0, 0, 0), 0, 0)
		);
		turretGrounded.teleport(block.getLocation());
		
		updateDirection(0, 0);
	}
	
	@Override
	public void destroy() {
		turretGrounded.remove();
		turretHead.remove();
		block.setType(Material.AIR);
		block.getRelative(BlockFace.UP).setType(Material.AIR);
	}
	
	public void updateDirection(float yaw, float pitch) {
		Location loc = getHeadLocation();
		loc.setYaw(yaw);
		loc.setPitch(pitch);
		turretHead.teleport(loc);
	}
	
	/* as of Location#getDirection */
	public Vector getDirection() {
		Vector vector = new Vector();
		double rotX = yaw;
		double rotY = pitch;
		vector.setY(-Math.sin(Math.toRadians(rotY)));
		double xz = Math.cos(Math.toRadians(rotY));
		vector.setX(-xz * Math.sin(Math.toRadians(rotX)));
		vector.setZ(xz * Math.cos(Math.toRadians(rotX)));
		return vector;
	}
	
	public Location getHeadLocation() {
		return block.getLocation().add(0, 2.4, 0);
	}
	
	public void shoot() {
		if(mortarReloadTicks > 0) {
			return;
		}
		
		obuses.add(new Obus());
	}
	
	@Override
	public void onWalkOn(TFEntity walker) {
	}
	
	private class Obus extends ComplexEntity {
		
		private Location loc;
		private Vector velocity;
		private boolean removed;
		
		private Obus() {
			super(createObus(getHeadLocation()));
			
			this.loc = getHeadLocation();
			this.velocity = getDirection();
			
			new BukkitRunnable() {
				@Override
				public void run() {
					tick();
					
					if(removed) {
						cancel();
					}
				}
			}.runTaskTimer(TF.getInstance(), 1, 1);
		}
		
		private static Collection<ComplexDisplayPart> createObus(Location loc) {
			BlockDisplay stalagmite = (BlockDisplay) loc.getWorld().spawnEntity(loc, EntityType.BLOCK_DISPLAY);
			stalagmite.setBlock(Material.POINTED_DRIPSTONE.createBlockData(data -> ((PointedDripstone) data).setThickness(PointedDripstone.Thickness.MIDDLE)));
			
			return List.of(new ComplexDisplayPart(stalagmite, new Vector(0, 0, -0.3), 0, 90));
		}
		
		public void tick() {
			final Vector gravity = new Vector(0, -9.8 / 20.0, 0);
			
			if(!loc.getBlock().equals(getHeadLocation().getBlock())) {
				if(loc.getWorld().rayTraceBlocks(loc, velocity, velocity.length()) != null) {
					explode();
					return;
				}
			}
			
			loc.add(velocity);
			velocity.multiply(0.99);
			velocity.add(gravity);
			
			if(velocity.isZero())
				velocity = gravity.clone();
			
			loc.setDirection(velocity);
			teleport(loc);
		}
		
		public void explode() {
			remove();
			removed = true;
			
			GameManager.getInstance().explosion(owner, loc, 23, 13, owner::canDamage, 1);
		}
	}
	
	@Override
	public boolean isClicked(RayTraceResult rayTrace) {
		return super.isClicked(rayTrace) || (rayTrace.getHitBlock() != null && rayTrace.getHitBlock().equals(block.getRelative(BlockFace.UP)));
	}
}
