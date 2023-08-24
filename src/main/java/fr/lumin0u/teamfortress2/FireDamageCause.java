package fr.lumin0u.teamfortress2;

import fr.lumin0u.teamfortress2.game.TFPlayer;

public record FireDamageCause(boolean unknown, TFPlayer damager, double damage)
{
	public static final FireDamageCause WILD_FIRE = new FireDamageCause(true, null, 1);
}
