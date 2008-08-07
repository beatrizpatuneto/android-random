package org.devtcg.games.solitaire.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Simple deck and card library.
 */
public class Deck extends CardStack
{
	public static final int STANDARD_DECK_SIZE = 52;

	/**
	 * Create an unshuffled deck of the standard size (52).
	 */
	public Deck()
	{
		this(STANDARD_DECK_SIZE);
	}

	/**
	 * Create an unshuffled deck of the specified size. 
	 */
	public Deck(int cards)
	{
		super(cards);

		for (Card.Suit suit: Card.Suit.values())
		{
			for (Card.Rank rank: Card.Rank.values())
				add(new Card(suit, rank, false));
		}
	}
	
	/**
	 * Shuffle the deck.
	 */
	public void shuffle()
	{
		int n = size();

		Random rand = new Random();

		/* Knuth shuffle. */
		while (--n > 0)
		{
			int k = rand.nextInt(n + 1);
			Card tmp = get(n);
			set(n, get(k));
			set(k, tmp);
		}
	}
	
	/**
	 * Deal a single card from the "top" of the deck.
	 * 
	 * @note This removes the card from the deck, reducing it's size.
	 */
	public Card draw()
	{
		return removeTop();
	}
	
	/**
	 * Draws a card from the specified position in the deck.
	 * 
	 * @see peek(int)
	 */
	public Card draw(int pos)
	{
		return remove(pos);
	}
	
	public List<Card> deal(int cards)
	{
		return deal(cards, true);
	}

	/**
	 * Draws multiple cards from the "top" of the deck.
	 * 
	 * @param cards
	 *   Number of cards to deal from the deck.
	 *   
	 * @param faceUp
	 *   Should cards be dealt face up or down?  Always changes the state of
	 *   the cards dealt.
	 */
	public List<Card> deal(int cards, boolean faceUp)
	{
		int n = size();

		ArrayList<Card> l = new ArrayList<Card>(cards);

		if (n < cards)
			return null;

		for (int i = n - cards; i < n; i++)
		{
			Card c = get(i);
			c.setFaceUp(faceUp);
			l.add(c);
		}

		removeRange(n - cards, n);
		return l;
	}

	/**
	 * Places a card on the "top" of the deck.
	 */
	public void unDraw(Card card)
	{
		add(card);
	}	
}
