package org.devtcg.games.solitaire.game;

import org.devtcg.games.solitaire.R;
import org.devtcg.games.solitaire.model.Card;
import org.devtcg.games.solitaire.model.CardStack;
import org.devtcg.games.solitaire.model.CardStackObserver;
import org.devtcg.games.solitaire.model.Deck;
import org.devtcg.games.solitaire.view.CardStackView;
import org.devtcg.games.solitaire.view.CardView;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

public class Klondike extends Activity
{
	public static final String TAG = "Klondike";
	
	protected Deck mDeck;
	protected CardStack[] mTableau;
	protected CardStack[] mFoundation;

	protected CardStackView[] mFoundationView;
	protected CardStackView[] mTableauView;

	/** Indicates the stack that we are currently holding. */
	protected CardStackView mHolding;

    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        setContentView(R.layout.klondike);
        
        initViews();
        
        if (icicle == null)
        	newGame();
        else
        	loadGame(icicle);
    }
    
    private void initViews()
    {
        /* Initialize views. */
        mTableauView = new CardStackView[7];
        mTableauView[0] = (CardStackView)findViewById(R.id.stack1);
        mTableauView[1] = (CardStackView)findViewById(R.id.stack2);
        mTableauView[2] = (CardStackView)findViewById(R.id.stack3);
        mTableauView[3] = (CardStackView)findViewById(R.id.stack4);
        mTableauView[4] = (CardStackView)findViewById(R.id.stack5);
        mTableauView[5] = (CardStackView)findViewById(R.id.stack6);
        mTableauView[6] = (CardStackView)findViewById(R.id.stack7);

        for (int i = 0; i < mTableauView.length; i++)
        {
        	CardStackView view = mTableauView[i];
        	view.setOnClickListener(mTableauClick);
        }

        mFoundationView = new CardStackView[4];
        mFoundationView[0] = (CardStackView)findViewById(R.id.ace1);
        mFoundationView[1] = (CardStackView)findViewById(R.id.ace2);
        mFoundationView[2] = (CardStackView)findViewById(R.id.ace3);
        mFoundationView[3] = (CardStackView)findViewById(R.id.ace4);

        for (int i = 0; i < mFoundationView.length; i++)
        {
        	CardStackView view = mFoundationView[i];        	
        	view.setOnClickListener(mFoundationClick);
        }
    }

    private void newGame()
    {
    	/* Initialize models. */
        mDeck = new Deck();
        mDeck.shuffle();

        mTableau = new CardStack[7];

        for (int i = 0; i < mTableau.length; i++)
        {
        	mTableau[i] = new CardStack(i + 4);
    		mTableau[i].registerObserver(new KlondikeObserver(mTableauView[i]));
        	mTableau[i].addAll(mDeck.deal(i + 1));
        }

        mFoundation = new CardStack[4];
        
        for (int i = 0; i < mFoundation.length; i++)
        {
        	mFoundation[i] = new CardStack(13);
    		mFoundation[i].registerObserver(new KlondikeObserver(mFoundationView[i]));
        }
        
        Log.d(TAG, "Deck:");
        
        for (int i = 0; i < mDeck.size(); i++)
        	Log.d(TAG, "  Card " + i + ": " + mDeck.get(i));
    }
    
    private void loadGame(Bundle icicle)
    {
    	/* TODO: Unserialize and load game state. */
    }

    @Override
    protected void onFreeze(Bundle icicle)
    {
    	/* TODO: Serialize and save game state. */
    }
    
    private final OnClickListener mFoundationClick = new OnClickListener()
    {
		public void onClick(View v)
		{
		}
    };

    private final OnClickListener mTableauClick = new OnClickListener()
    {
    	public void onClick(View v)
    	{
			CardStackView vv = (CardStackView)v;

			setHolding(vv);

			int n = vv.getChildCount();
			Card card = null;

			if (n > 0)
				card = ((CardView)vv.getChildAt(n - 1)).getCard();

			Log.d(TAG, "Tableau click: " + card);
    	}
    };

	private void setHolding(CardStackView stack)
	{
		if (mHolding == stack)
			return;

		if (mHolding != null)
			mHolding.setSelected(false);

		stack.setSelected(true);
		mHolding = stack;
	}
    
    public class KlondikeObserver extends CardStackObserver
    {
    	protected CardStackView mView;
    	
    	public KlondikeObserver(CardStackView view)
    	{
    		mView = view;
    	}

		@Override
		protected void onAdd(Card card)
		{
			CardView view = new CardView(Klondike.this);
			view.setCard(card);
			mView.addCard(view);
		}

		@Override
		protected void onRemove(int pos)
		{
			mView.removeCard(pos);
		}
    }
}
