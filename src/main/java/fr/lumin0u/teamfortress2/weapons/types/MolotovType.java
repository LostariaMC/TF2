package fr.lumin0u.teamfortress2.weapons.types;

import com.google.common.collect.ImmutableList.Builder;
import fr.lumin0u.teamfortress2.FireDamageCause;
import fr.lumin0u.teamfortress2.TFEntity;
import fr.lumin0u.teamfortress2.game.GameManager;
import fr.lumin0u.teamfortress2.game.TFPlayer;
import fr.lumin0u.teamfortress2.util.ItemBuilder;
import fr.lumin0u.teamfortress2.util.TFSound;
import fr.lumin0u.teamfortress2.weapons.ThrownExplosive;
import fr.lumin0u.teamfortress2.weapons.Weapon;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public final class MolotovType extends WeaponType
{
	private final double radius = 7;
	private final double fireDmg = 2.1;
	private final int fireDuration = 180;
	
	public MolotovType() {
		super(true, Material.SPLASH_POTION, "Cocktail Molotov", 1, -1, -1);
	}
	
	@Override
	public ItemBuilder buildItem(Weapon weapon) {
		return super.buildItem(weapon).setPotionColor(Color.ORANGE);
	}
	
	@Override
	protected Builder<String> loreBuilder() {
		return super.loreBuilder()
				.add(RADIUS_LORE.formatted(radius))
				.add(CUSTOM_LORE.formatted("Enflamme vos ennemis"))
				.add(DURATION_LORE.formatted((float) ((float) fireDuration / 20f)))
				.add(FIRE_DMG_LORE.formatted(fireDmg));
	}
	
	@Override
	public void rightClickAction(TFPlayer player, Weapon weapon, RayTraceResult info) {
		
		Vector direction = player.getEyeLocation().getDirection();
		Location rocketLoc = player.getEyeLocation().add(direction);
		TFSound.MOLOTOV_THROW.play(rocketLoc);
		
		ThrownPotion potion = (ThrownPotion) player.toBukkit().getWorld().spawnEntity(rocketLoc, EntityType.SPLASH_POTION, SpawnReason.CUSTOM, p -> {
			PotionMeta meta = ((ThrownPotion)p).getPotionMeta();
			meta.setColor(Color.ORANGE);
			((ThrownPotion)p).setPotionMeta(meta);
		});
		potion.setVelocity(direction.clone().multiply(0.8));
		
		weapon.useAmmo();
		
		new ThrownExplosive(player, this, 200, true) {
			@Override
			public void tick() {}
			
			@Override
			public void explode() {
				Location loc = potion.getLocation();
				for(TFEntity ent : GameManager.getInstance().getLivingEntities()) {
					if(player.isEnemy(ent) && ent.getLocation().distanceSquared(loc) < radius*radius) {
						ent.setFireCause(new FireDamageCause(false, player, fireDmg));
						ent.getEntity().setFireTicks(fireDuration);
					}
				}
				loc.getWorld().spawnParticle(Particle.FLAME, loc, 100, 0, 0, 0, 0.3, null, true);
				remove();
			}
			
			@Override
			public Entity getEntity() {
				return potion;
			}
		};
	}
	
	@Override
	public void leftClickAction(TFPlayer player, Weapon weapon, RayTraceResult info) {}
}
