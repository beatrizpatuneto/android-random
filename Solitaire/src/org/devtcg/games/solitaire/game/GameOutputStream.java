package org.devtcg.games.solitaire.game;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.devtcg.games.solitaire.model.Card;
import org.devtcg.games.solitaire.model.CardStack;

public class GameOutputStream extends DataOutputStream
{
	public GameOutputStream(OutputStream out)
	{
		super(out);
	}
	
	public void writeCard(Card card)
	  throws IOException
	{
		if (card == null)
		{
			writeInt(-1);
			writeInt(-1);
			writeBoolean(false);
		}
		else
		{
			writeInt(card.getSuit().ordinal());
			writeInt(card.getRankOrdinal());
			writeBoolean(card.isFaceUp());
		}
	}

	public void writeCardStack(CardStack stack)
	  throws IOException
	{
		int n = stack.size();

		writeInt(n);

		for (int i = 0; i < n; i++)
			writeCard(stack.get(i));
	}

	public void writeCardStacks(CardStack[] stacks)
	  throws IOException
	{
		int n = stacks.length;

		writeInt(n);

		for (int i = 0; i < n; i++)
			writeCardStack(stacks[i]);
	}
}
