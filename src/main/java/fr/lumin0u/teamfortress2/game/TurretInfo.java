package fr.lumin0u.teamfortress2.game;

import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.util.Vector;

public record TurretInfo(Block carpet, ArmorStand hologram, Vector direction, int recharge)
{
	public TurretInfo(Block carpet, ArmorStand hologram) {
		this(carpet, hologram, new Vector(1, 0, 0), -1);
	}
}