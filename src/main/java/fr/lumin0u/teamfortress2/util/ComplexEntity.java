package fr.lumin0u.teamfortress2.util;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.util.*;

public class ComplexEntity {
	private final Set<ComplexDisplayPart> parts;
	private Location location;
	
	public ComplexEntity(ComplexDisplayPart... parts) {
		this(Arrays.asList(parts));
	}
	
	public ComplexEntity(Collection<ComplexDisplayPart> parts) {
		this.parts = new HashSet<>(parts);
	}
	
	public void teleport(Location location) {
		setLocation(location);
		setRotation(location.getYaw(), location.getPitch());
	}
	
	public void setLocation(Location location) {
		this.location.subtract(this.location).add(location.toVector());
		this.location.setWorld(location.getWorld());
		
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
			Location entLocation = location.clone().add(location.getDirection().add(part.offset()));
			entLocation.setYaw(getYaw() + part.yawOffset());
			entLocation.setPitch(getPitch() + part.pitchOffset());
			part.entity().teleport(entLocation);
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
		parts.forEach(part -> part.entity().remove());
	}
	
	public Collection<ComplexDisplayPart> getParts() {
		return new HashSet<>(parts);
	}
	
	public static record ComplexDisplayPart(Entity entity, Vector offset, float yawOffset, float pitchOffset) {}
}
