package fr.lumin0u.teamfortress2.util;

import java.util.List;
import java.util.Random;

public class Utils
{
	public static <T> T choice(List<T> list)
	{
		return choice(list, new Random());
	}
	public static <T> T choice(List<T> list, Random random)
	{
		return list.get(random.nextInt(list.size()));
	}
}
