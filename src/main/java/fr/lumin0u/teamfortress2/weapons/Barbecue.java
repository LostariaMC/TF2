package fr.lumin0u.teamfortress2.weapons;

import com.google.common.collect.ImmutableList.Builder;
import fr.lumin0u.teamfortress2.FireDamageCause;
import fr.lumin0u.teamfortress2.TF;
import fr.lumin0u.teamfortress2.TFEntity;
import fr.lumin0u.teamfortress2.game.GameManager;
import fr.lumin0u.teamfortress2.game.TFPlayer;
import fr.lumin0u.teamfortress2.util.TFSound;
import fr.lumin0u.teamfortress2.weapons.types.GunType.Hit;
import fr.lumin0u.teamfortress2.weapons.types.WeaponType;
import fr.lumin0u.teamfortress2.weapons.types.WeaponTypes;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public final class Barbecue extends Weapon
{
	public Barbecue(TFPlayer owner, int slot) {
		super(WeaponTypes.BARBECUE, owner, slot);
		
		addBukkitTask(new BukkitRunnable() {
			long lastAmmoGift;
			@Override
			public void run() {
				if(TF.currentTick() - lastActionDate > 45 && TF.currentTick() - lastAmmoGift > 8 && ammo < type.getMaxAmmo()) {
					ammo++;
					lastAmmoGift = TF.currentTick();
					updateItem();
				}
			}
		}.runTaskTimer(TF.getInstance(), 1, 1));
	}
	
	@Override
	public void reload(int ticks) {}
	
	@Override
	public void rightClick(RayTraceResult info) {
		if(ammo > 1 && lastActionDate + getType().getActionDelay() < TF.currentTick()) {
			type.rightClickAction(owner, this, info);
			lastActionDate = TF.currentTick();
		}
	}
	
	public static final class BarbecueType extends WeaponType
	{
		private final double range = 5;
		private final double fireDmg = 1.2;
		private final int fireDuration = 80;
		
		public BarbecueType() {
			super(false, Material.CAMPFIRE, "Barbecue", 32, -1, 4);
		}
		
		@Override
		public Weapon createWeapon(TFPlayer owner, int slot) {
			return new Barbecue(owner, slot);
		}
		
		@Override
		protected Builder<String> loreBuilder() {
			return super.loreBuilder()
					.add(RANGE_LORE.formatted(range))
					.add(CUSTOM_LORE.formatted("Enflamme vos ennemis"))
					.add(DURATION_LORE.formatted((float) ((float) fireDuration / 20f)))
					.add(FIRE_DMG_LORE.formatted(fireDmg));
		}
		
		@Override
		public void rightClickAction(TFPlayer player, Weapon weapon, RayTraceResult info) {
			weapon.useAmmo();
			
			Location source = player.toBukkit().getEyeLocation();
			Vector direction = source.getDirection();
			TFSound.BARBECUE.play(source);
			
			for(TFEntity ent : GameManager.getInstance().getLivingEntities())
			{
				double distance = ent.getLocation().distance(player.getLocation());
				
				if(!player.isEnemy(ent) || distance > 5)
					continue;
				
				double m = distance / 5;
				BoundingBox bodyBox = ent.getBodyBox().clone().expand(m);
				BoundingBox headBox = ent.getHeadBox().clone().expand(m);
				
				RayTraceResult bodyCollision = bodyBox.rayTrace(source.toVector(), direction, range);
				RayTraceResult headCollision = headBox.rayTrace(source.toVector(), direction, range);
				
				if(bodyCollision != null || headCollision != null) {
					ent.setFireCause(new FireDamageCause(false, player, fireDmg));
					ent.getEntity().setFireTicks(fireDuration);
				}
			}
			
			for(double i = 0; i < 5; i++)
			{
				for(int j = 0; j < 22; j++)
				{
					Vector x1 = new Vector(-direction.getZ(), 0d, direction.getX()).normalize();
					Vector x2 = direction.clone().crossProduct(x1).normalize();
					
					Location l = source.clone().add(direction.clone().multiply(0.8*i+1)).add(x1.clone().multiply((i/8+0.4) * Math.sin((double)j / 20 * Math.PI * 2d))).add(x2.clone().multiply((i/8+0.4) * Math.cos((double)j / 20 * Math.PI * 2d)));
					
					l.getWorld().spawnParticle(Particle.FLAME, l, 1, 0, 0, 0, 0.00001);
				}
			}
		}
		
		@Override
		public void leftClickAction(TFPlayer player, Weapon weapon, RayTraceResult info) {}
	}
}
