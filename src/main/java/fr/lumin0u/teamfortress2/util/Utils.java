package fr.lumin0u.teamfortress2.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Location;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class Utils
{
	public static <T> T choice(List<T> list) {
		return choice(list, new Random());
	}
	
	public static <T> T choice(List<T> list, Random random) {
		return list.get(random.nextInt(list.size()));
	}
	
	public static Vector yawPitchToDirection(float yaw, float pitch) {
		Vector vector = new Vector();
		vector.setY(-Math.sin(Math.toRadians(pitch)));
		double xz = Math.cos(Math.toRadians(pitch));
		vector.setX(-xz * Math.sin(Math.toRadians(yaw)));
		vector.setZ(xz * Math.cos(Math.toRadians(yaw)));
		return vector;
	}
	
	public static float directionToYaw(Vector direction) {
		final double _2PI = 2 * Math.PI;
		final double x = direction.getX();
		final double z = direction.getZ();
		
		if (x == 0 && z == 0) {
			return 0;
		}
		
		double theta = Math.atan2(-x, z);
		return (float) Math.toDegrees((theta + _2PI) % _2PI);
	}
	
	public static float directionToPitch(Vector direction) {
		final double _2PI = 2 * Math.PI;
		final double x = direction.getX();
		final double z = direction.getZ();
		
		if (x == 0 && z == 0) {
			return direction.getY() > 0 ? -90 : 90;
		}
		double x2 = NumberConversions.square(x);
		double z2 = NumberConversions.square(z);
		double xz = Math.sqrt(x2 + z2);
		
		return (float) Math.toDegrees(Math.atan(-direction.getY() / xz));
	}
	
	public static Collector<Component, ?, Component> joiningComponentsCollector(Component delimiter) {
		return new Collector<Component, TextComponent.Builder, Component>() {
			@Override
			public Supplier<TextComponent.Builder> supplier() {
				return Component::text;
			}
			
			@Override
			public BiConsumer<TextComponent.Builder, Component> accumulator() {
				return (b, c) -> b.append(delimiter).append(c);
			}
			
			@Override
			public BinaryOperator<TextComponent.Builder> combiner() {
				return (b1, b2) -> b1.append(delimiter).append(b2);
			}
			
			@Override
			public Function<TextComponent.Builder, Component> finisher() {
				return ComponentBuilder::build;
			}
			
			@Override
			public Set<Characteristics> characteristics() {
				return Set.of();
			}
		};
	}
}
