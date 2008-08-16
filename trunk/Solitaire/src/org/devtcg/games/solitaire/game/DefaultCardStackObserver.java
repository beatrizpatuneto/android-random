package org.devtcg.games.solitaire.game;

import org.devtcg.games.solitaire.model.Card;
import org.devtcg.games.solitaire.model.CardStack;
import org.devtcg.games.solitaire.model.CardStackObserver;
import org.devtcg.games.solitaire.view.CardStackView;
import org.devtcg.games.solitaire.view.CardView;

import android.content.Context;

public class DefaultCardStackObserver extends CardStackObserver
{
	protected Context mContext;
	protected CardStackView mView;

	public DefaultCardStackObserver(Context ctx, CardStackView view)
	{
		super();
		mView = view;
		mContext = ctx;
	}

	@Override
	protected void onAdd(CardStack stack, Card card)
	{
		CardView view = new CardView(mContext);
		view.setCard(card);
		mView.addCard(view);
		mView.invalidate();
	}

	@Override
	protected void onRemove(CardStack stack, int pos)
	{
		CardView view = (CardView)mView.getChildAt(pos);
		mView.removeCard(pos);
		mView.invalidate();
	}
}
