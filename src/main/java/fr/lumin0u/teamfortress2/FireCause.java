package fr.lumin0u.teamfortress2;

import fr.lumin0u.teamfortress2.game.TFPlayer;

public class FireCause
{
	private boolean unknown;
	private TFPlayer damager;
	
	public FireCause(boolean unknown, TFPlayer damager)
	{
		this.unknown = unknown;
		this.damager = damager;
	}

	public boolean isUnknown()
	{
		return unknown;
	}

	public TFPlayer getDamager()
	{
		return damager;
	}
}
