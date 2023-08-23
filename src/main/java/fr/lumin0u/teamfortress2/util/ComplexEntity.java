package fr.lumin0u.teamfortress2.util;

import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.utility.MinecraftReflection;
import fr.lumin0u.teamfortress2.TF;
import fr.worsewarn.cosmox.API;
import fr.worsewarn.cosmox.api.players.WrappedPlayer;
import net.minecraft.network.protocol.game.PacketPlayOutEntity.PacketPlayOutEntityLook;
import net.minecraft.network.protocol.game.PacketPlayOutEntityHeadRotation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ComplexEntity
{
	private final Set<ComplexDisplayPart> parts;
	private Location location;
	
	public ComplexEntity(Location loc, ComplexDisplayPart... parts) {
		this(loc, Arrays.asList(parts));
		teleport(loc);
	}
	
	public ComplexEntity(Location loc, Collection<ComplexDisplayPart> parts) {
		this.parts = new HashSet<>(parts);
		this.location = loc;
	}
	
	public void teleport(Location location) {
		setLocation(location);
		setRotation(location.getYaw(), location.getPitch());
	}
	
	public void setLocation(Location location) {
		this.location = location.clone().setDirection(getDirection());
		
		updateLocation();
	}
	
	public void setRotation(float yaw, float pitch) {
		this.location.setYaw(yaw);
		this.location.setPitch(pitch);
		
		updateLocation();
	}
	
	public void setRotation(Vector direction) {
		this.location.setDirection(direction);
		
		updateLocation();
	}
	
	private void updateLocation() {
		parts.forEach(part -> {
			
			Vector front = getDirection();
			Vector right = Utils.yawPitchToDirection(Utils.directionToYaw(front) + 90, 0);
			Vector up = new Vector(0, 1, 0);
			
			Vector offset = part.offset().clone();
			offset.rotateAroundAxis(up, -getYaw() / 180 * Math.PI);
			offset.rotateAroundAxis(right, -getPitch() / 180 * Math.PI);
			Location entLocation = location.clone().add(offset);
			
			entLocation.setYaw(getYaw() + part.yawOffset());
			entLocation.setPitch(getPitch() + part.pitchOffset());
			part.entity().teleport(entLocation);
			float itsYaw = getYaw() + part.yawOffset();
			float itsPitch = getPitch() + part.pitchOffset();
			/*if(itsPitch > 90) {
				itsYaw = 180 + itsYaw;
				itsPitch = 180 - itsPitch;
			}
			else if(itsPitch < -90) {
				itsYaw = 180 + itsYaw;
				itsPitch = 180 - itsPitch;
			}*/
			part.setRotation(itsYaw, itsPitch);
		});
	}
	
	public void rotate(float yaw, float pitch) {
		setRotation(this.getYaw() + yaw, this.getPitch() + pitch);
	}
	
	public Location getLocation() {
		return location.clone();
	}
	
	public float getYaw() {
		return location.getYaw();
	}
	
	public float getPitch() {
		return location.getPitch();
	}
	
	public void remove() {
		parts.forEach(ComplexDisplayPart::remove);
	}
	
	public Vector getDirection() {
		return location.getDirection();
	}
	
	public Collection<ComplexDisplayPart> getParts() {
		return new HashSet<>(parts);
	}
	
	public static class ComplexDisplayPart
	{
		private final Entity entity;
		private final Vector offset;
		private final float yawOffset;
		private final float pitchOffset;
		private float yaw, pitch;
		private PacketAdapter packetListener;
		
		public ComplexDisplayPart(Entity entity, Vector offset, float yawOffset, float pitchOffset) {
			this.entity = entity;
			this.offset = offset;
			this.yawOffset = yawOffset;
			this.pitchOffset = pitchOffset;
			
			packetListener = new PacketAdapter(TF.getInstance(), Server.ENTITY_LOOK, Server.REL_ENTITY_MOVE_LOOK) {
				@Override
				public void onPacketSending(PacketEvent event) {
					if(event.getPacket().getIntegers().read(0) == entity.getEntityId()) {
						event.getPacket().getBytes().write(0, (byte) (yaw / 360 * 256));
						event.getPacket().getBytes().write(1, (byte) (pitch / 360 * 256));
					}
				}
			};
			
			API.instance().getProtocolManager().addPacketListener(packetListener);
		}
		
		public void setRotation(float yaw, float pitch) {
			this.yaw = yaw;
			this.pitch = pitch;
			entity.setRotation(yaw, pitch);
		}
		
		public Entity entity() {return entity;}
		
		public Vector offset() {return offset;}
		
		public float yawOffset() {return yawOffset;}
		
		public float pitchOffset() {return pitchOffset;}
		
		private void remove() {
			entity.remove();
			API.instance().getProtocolManager().removePacketListener(packetListener);
		}
	}
}
