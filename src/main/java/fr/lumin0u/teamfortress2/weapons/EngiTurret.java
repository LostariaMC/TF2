package fr.lumin0u.teamfortress2.weapons;

import fr.lumin0u.teamfortress2.game.TFPlayer;
import fr.lumin0u.teamfortress2.weapons.types.EngiTurretType;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

public class EngiTurret extends Weapon<EngiTurretType>
{
	public static final int RELOAD_TICKS = 7*20;
	
	private Block block;
	private float yaw, pitch;
	
	private BlockDisplay hopper;
	private BlockDisplay cauldron;
	
	private int mortarReloadTicks;
	
	public EngiTurret(EngiTurretType type, TFPlayer owner, int slot) {
		super(type, owner, slot);
	}
	
	public void place(Block block) {
		this.block = block;
		
		block.setType(Material.MUD_BRICK_SLAB);
		
		//build mortar
		
		hopper = (BlockDisplay) block.getWorld().spawnEntity(block.getLocation(), EntityType.BLOCK_DISPLAY);
		hopper.setBlock(Material.HOPPER.createBlockData());
		
		cauldron = (BlockDisplay) block.getWorld().spawnEntity(block.getLocation(), EntityType.BLOCK_DISPLAY);
		cauldron.setBlock(Material.CAULDRON.createBlockData());
		
		updateDirection(0, 0);
	}
	
	public void updateDirection(float yaw, float pitch) {
		
		hopper.setRotation(yaw, pitch + 90);
		cauldron.setRotation(yaw, pitch + 90);
		
		hopper.teleport(block.getLocation().add(0, 2.4, 0).add(getDirection().multiply(-0.2)));
		cauldron.teleport(block.getLocation().add(0, 2.4, 0).add(getDirection().multiply(0.6)));
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
	
	public void shoot() {
		if(mortarReloadTicks > 0) {
			return;
		}
		
		
	}
}
