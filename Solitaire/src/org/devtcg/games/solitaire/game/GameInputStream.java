package org.devtcg.games.solitaire.game;

import java.io.DataInputStream;
import java.io.InputStream;

public class GameInputStream extends DataInputStream
{
	public GameInputStream(InputStream in)
	{
		super(in);
	}
}
