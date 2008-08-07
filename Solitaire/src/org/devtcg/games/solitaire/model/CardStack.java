package org.devtcg.games.solitaire.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Observer;

public class CardStack extends ArrayList<Card>
{
	protected CardStackObservable mObservable;

	public CardStack()
	{
		super();
		init();
	}

	public CardStack(int initialCapacity)
	{
		super();
		init();
	}

	private void init()
	{
		mObservable = new CardStackObservable();
	}

	public void registerObserver(CardStackObserver o)
	{
		mObservable.addObserver(o);
	}
	
	/**
	 * Examine a card from the "top" of the deck, without removing it.
	 */
	public Card peekTop()
	{
		return peek(size() - 1);
	}
	
	/**
	 * Examine a card from the specified position of the deck.  The bottom of the deck is at
	 * position 0.
	 * 
	 * Valid range:
	 * 
	 * <code>0 &lt;= pos &lt; n</code>
	 * 
	 * Where <code>n</code> is the number of cards in the deck.
	 */
	public Card peek(int pos)
	{
		return get(pos);
	}

	public Card removeTop()
	{
		Card card;
		int n;
		
		if ((n = size()) == 0)
			return null;

		card = remove(n - 1);
		mObservable.removeCard(n - 1);

		return card;
	}

	@Override
	public Card remove(int location)
	{
		Card card = super.remove(location);
		mObservable.removeCard(location);
		return card;
	}

	@Override
	public boolean add(Card card)
	{
		boolean ret = super.add(card);
		mObservable.addCard(card);
		return ret;
	}

	@Override
	public boolean addAll(Collection<? extends Card> collection)
	{
		boolean ret = super.addAll(collection);

		for (Object o: collection)
		{
			if (o instanceof Card)
				mObservable.addCard((Card)o);
		}
			
		return ret;
	}
	
	@Override
	public String toString()
	{
		StringBuilder b = new StringBuilder();

		int n = size();

		for (int i = 0; i < n; i++)
		{
			b.append(i).append(": ");
			b.append(get(i));

			if (i + 1 < n)
				b.append(", ");
		}

		return b.toString();
	}
}
