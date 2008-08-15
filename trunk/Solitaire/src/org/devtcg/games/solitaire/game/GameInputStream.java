package org.devtcg.games.solitaire.game;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.devtcg.games.solitaire.model.Card;
import org.devtcg.games.solitaire.model.CardStack;

public class GameInputStream extends DataInputStream
{
	public GameInputStream(InputStream in)
	{
		super(in);
	}
	
	public Card readCard()
	  throws IOException
	{
		int suit = readInt();
		int rank = readInt();
		boolean faceUp = readBoolean();
		
		if (suit == -1 || rank == -1)
			return null;
		else
			return new Card(suit, rank, faceUp);
	}

	public CardStack readCardStack()
	  throws IOException
	{
		int n = readInt();

		if (n < 0)
			return null;

		CardStack stack = new CardStack(n);

		while (n-- > 0)
			stack.add(readCard());

		return stack;
	}

	public CardStack[] readCardStacks()
	  throws IOException
	{
		int n = readInt();
		
		if (n < 0)
			return null;
		
		CardStack[] stacks = new CardStack[n];
		
		for (int i = 0; i < n; i++)
			stacks[i] = readCardStack();
		
		return stacks;
	}
}
