package fr.lumin0u.teamfortress2.util;

import com.comphenix.protocol.utility.MinecraftReflection;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

public class NMSUtils
{
	public NMSUtils() {
	}
	
	public static <T> T getHandle(Object obj, Object... params) {
		try
		{
			return (T) obj.getClass().getMethod("getHandle").invoke(obj, params);
		} catch(ReflectiveOperationException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public static <T extends Entity> T getNMSEntity(org.bukkit.entity.Entity bukkit) {
		try
		{
			return (T) MinecraftReflection.getCraftEntityClass().getDeclaredMethod("getHandle").invoke(bukkit);
		} catch(ReflectiveOperationException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public static <T> void setField(String field, T value, Object instance) {
		Class<?> c = instance.getClass();
		FieldInstance<T> f = getField(field, c);
		if(f == null)
			throw new IllegalArgumentException("Object " + instance.toString() + " does not have field " + field);
		f.set(instance, value);
	}
	
	public static <T> FieldInstance<T> getField(String field, Class<?> clazz) {
		try
		{
			while(Arrays.stream(clazz.getDeclaredFields()).noneMatch(f -> f.getName().equals(field)) && clazz.getSuperclass() != null)
			{
				clazz = clazz.getSuperclass();
			}
			
			if(Arrays.stream(clazz.getDeclaredFields()).noneMatch(f -> f.getName().equals(field)))
				return null;
			
			Field f = clazz.getDeclaredField(field);
			return new FieldInstance<>(f);
		} catch(ReflectiveOperationException e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public static <T> T getFieldValue(String field, Object instance) {
		Class<?> c = instance.getClass();
		FieldInstance<T> f = getField(field, c);
		if(f == null)
			throw new IllegalArgumentException("Object " + instance.toString() + " does not have field " + field);
		return f.get(instance);
	}
	
	public static <T> T getStaticField(String field, Class<?> clazz) throws ReflectiveOperationException {
		FieldInstance<T> f = getField(field, clazz);
		if(f == null)
			throw new IllegalArgumentException("Class " + clazz.getName() + " does not have field " + field);
		return f.get(null);
	}
	
	public static boolean isPrimitive(Class<?> c) {
		return Arrays.asList(Boolean.TYPE, Integer.TYPE, Float.TYPE, Double.TYPE, Long.TYPE, Byte.TYPE, Short.TYPE, Character.TYPE, String.class, Boolean.class, Integer.class, Float.class, Double.class, Long.class, Byte.class, Short.class, Character.class).contains(c);
	}
	/*
	public static Object getPlayerConnection(Player p) throws ReflectiveOperationException
	{
		Object craftPlayer = NMSUtils.getClass("CraftPlayer").cast(p);
		Object entityPlayer = NMSUtils.getClass("CraftPlayer").getDeclaredMethod("getHandle").invoke(craftPlayer);
		Object playerConnection = NMSUtils.getClass("PlayerConnection").cast(entityPlayer.getClass().getField("playerConnection").get(entityPlayer));
		return playerConnection;
	}
	
	public static void sendJSON(Player p, String json)
	{
		try
		{
			Class<?> chatBaseComponent = NMSUtils.getClass("IChatBaseComponent");
			Class<?> packetPlayOutChat = NMSUtils.getClass("PacketPlayOutChat");
			Object toSend = NMSUtils.getClass("ChatSerializer").getMethod("a", String.class).invoke((Object) null, json);
			sendPacket(p, packetPlayOutChat.getConstructor(chatBaseComponent).newInstance(toSend));
		} catch(Exception var5)
		{
			var5.printStackTrace();
		}
	}*/
	
	public record FieldInstance<T>(Field field)
	{
		public FieldInstance(Field field) {
			this.field = field;
			field.setAccessible(true);
		}
		
		
		public T get(Object instance) {
			try
			{
				return (T) field.get(instance);
			} catch(IllegalAccessException e)
			{
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
		
		public void set(Object instance, T value) {
			try
			{
				field.set(instance, value);
			} catch(IllegalAccessException e)
			{
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	}
}
