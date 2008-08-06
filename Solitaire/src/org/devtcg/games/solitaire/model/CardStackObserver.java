package org.devtcg.games.solitaire.model;

import java.util.Observable;
import java.util.Observer;

public abstract class CardStackObserver implements Observer
{
	public void update(Observable o, Object data)
	{
		CardStackObservable.Action a = (CardStackObservable.Action)data;

		switch (a.action)
		{
		case CardStackObservable.Action.ADD:
			onAdd(a.card);
			break;
		case CardStackObservable.Action.REMOVE:
			/* TODO: Handle searching. */
			if (a.card != null)
				throw new RuntimeException("TODO");

			onRemove(a.cardPos);
			break;
		}
	}

	protected abstract void onAdd(Card card);
	protected abstract void onRemove(int pos);
}
