package fr.lumin0u.teamfortress2.weapons.types;

import com.google.common.collect.ImmutableList.Builder;
import fr.lumin0u.teamfortress2.TFEntity;
import fr.lumin0u.teamfortress2.game.GameManager;
import fr.lumin0u.teamfortress2.game.TFPlayer;
import fr.lumin0u.teamfortress2.weapons.Gun;
import fr.lumin0u.teamfortress2.weapons.Weapon;
import org.bukkit.*;
import org.bukkit.Particle.DustOptions;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.Random;
import java.util.function.Consumer;

public class MeleeWeaponType extends WeaponType
{
	protected final double damage;
	protected final double knockback; // blocks/tick
	
	public MeleeWeaponType(boolean ultimate, Material material, String name, int maxAmmo, int reloadTicks, double damage, double knockback) {
		super(ultimate, material, name, maxAmmo, 0, 0);
		this.damage = damage;
		this.knockback = knockback;
	}
	
	@Override
	public void rightClickAction(TFPlayer player, Weapon weapon, RayTraceResult info) {
	
	}
	
	@Override
	public void leftClickAction(TFPlayer player, Weapon weapon, RayTraceResult info) {
		if(info == null || !(info.getHitEntity() instanceof Player)) {
			return;
		}
		
		TFPlayer victim = TFPlayer.of(info.getHitEntity());
		
		if(player.canDamage(victim)) {
			Vector kb = player.toBukkit().getEyeLocation().getDirection().multiply(knockback);
			victim.damage(player, damage, kb);
		}
		
		weapon.useAmmo();
	}
	
	public double getDamage() {
		return damage;
	}
	
	public double getKnockback() {
		return knockback;
	}
	
	@Override
	protected Builder<String> loreBuilder() {
		return new Builder<String>().add(DAMAGE_LORE.formatted((int) damage));
	}
}
