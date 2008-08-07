package org.devtcg.games.solitaire.model;

import java.util.Observable;
import java.util.Observer;

public abstract class CardStackObserver implements Observer
{
	public void update(Observable o, Object data)
	{
		CardStackObservable oo = (CardStackObservable)o;		
		CardStackObservable.Action a = (CardStackObservable.Action)data;
		
		CardStack stack = oo.getCardStack();

		switch (a.action)
		{
		case CardStackObservable.Action.ADD:
			onAdd(stack, a.card);
			break;
		case CardStackObservable.Action.REMOVE:
			/* TODO: Handle searching. */
			if (a.card != null)
				throw new RuntimeException("TODO");

			onRemove(stack, a.cardPos);
			break;
		}
	}

	protected abstract void onAdd(CardStack stack, Card card);
	protected abstract void onRemove(CardStack stack, int pos);
}
