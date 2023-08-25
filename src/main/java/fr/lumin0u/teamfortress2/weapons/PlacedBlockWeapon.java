package fr.lumin0u.teamfortress2.weapons;

import fr.lumin0u.teamfortress2.TFEntity;
import fr.lumin0u.teamfortress2.game.TFPlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.RayTraceResult;

public abstract class PlacedBlockWeapon {
	
	protected final TFPlayer owner;
	protected final PlaceableWeapon weapon;
	protected final Block block;
	
	public PlacedBlockWeapon(TFPlayer owner, PlaceableWeapon weapon, Block block) {
		this.owner = owner;
		this.weapon = weapon;
		this.block = block;
	}
	
	public Block getBlock() {
		return block;
	}
	
	public PlaceableWeapon getWeapon() {
		return weapon;
	}
	
	public TFPlayer getOwner() {
		return owner;
	}
	
	public abstract void place(BlockFace against);
	
	public abstract void destroy();
	
	public abstract void onWalkOn(TFEntity walker);
	
	public boolean isClicked(RayTraceResult rayTrace) {
		return rayTrace.getHitBlock() != null && rayTrace.getHitBlock().equals(block);
	}
}
