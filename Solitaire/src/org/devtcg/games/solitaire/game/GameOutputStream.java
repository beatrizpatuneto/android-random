package org.devtcg.games.solitaire.game;

import java.io.DataOutputStream;
import java.io.OutputStream;

public class GameOutputStream extends DataOutputStream
{
	public GameOutputStream(OutputStream out)
	{
		super(out);
	}
}
