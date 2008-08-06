package org.devtcg.games.solitaire.model;

import java.util.Observable;

public class CardStackObservable extends Observable
{
	public CardStackObservable()
	{
		super();
	}

	public static class Action
	{
		public static final int ADD = 0;
		public static final int REMOVE = 1;

		public int action;
		public Card card;
		public int cardPos;

		public Action(int action, Card card)
		{
			this.action = action;
			this.card = card;
		}
		
		public Action(int action, int position)
		{
			this.action = action;
			this.cardPos = position;
		}
		
		public String toString()
		{
			StringBuilder b = new StringBuilder();
			
			b.append("{action=").append(action).append(", ");
			b.append(", card=");
			
			if (card != null)
				b.append(card.toString());
			else
				b.append(cardPos);
			
			b.append("}");
			
			return b.toString();
		}
	}

	public void addCard(Card card)
	{
		setChanged();
		notifyObservers(new Action(Action.ADD, card));
	}

	public void removeCard(Card card)
	{
		setChanged();
		notifyObservers(new Action(Action.REMOVE, card));
	}
	
	public void removeCard(int position)
	{
		setChanged();
		notifyObservers(new Action(Action.REMOVE, position));
	}
}
