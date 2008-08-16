package org.devtcg.games.solitaire.game.rules;

import java.io.IOException;
import java.util.Random;

import org.devtcg.games.solitaire.R;
import org.devtcg.games.solitaire.game.DefaultCardStackObserver;
import org.devtcg.games.solitaire.game.Game;
import org.devtcg.games.solitaire.game.GameInputStream;
import org.devtcg.games.solitaire.game.GameManager;
import org.devtcg.games.solitaire.game.GameOutputStream;
import org.devtcg.games.solitaire.model.Card;
import org.devtcg.games.solitaire.model.CardStack;
import org.devtcg.games.solitaire.model.Deck;
import org.devtcg.games.solitaire.view.CardStackView;
import org.devtcg.games.solitaire.view.CardView;

import android.view.View;
import android.view.ViewInflate;
import android.view.View.OnClickListener;

public class Freecell extends Game
{
	public static final String TAG = "Freecell";
	
	protected View mRoot;

	protected CardStack mFoundation[] = new CardStack[4];
	protected CardStack mTableau[] = new CardStack[8];

	protected CardView mFreecellView[] = new CardView[4];
	protected CardStackView mFoundationView[] = new CardStackView[mFoundation.length];
	protected CardStackView mTableauView[] = new CardStackView[mTableau.length];

	@Override
	public String getName()
	{
		return TAG;
	}

	@Override
	public void init(GameManager mgr)
	{
		super.init(mgr);

		View v = ViewInflate.from(mgr).inflate(R.layout.freecell, null, null);

        mFreecellView[0] = (CardView)v.findViewById(R.id.freecell1);
        mFreecellView[1] = (CardView)v.findViewById(R.id.freecell2);
        mFreecellView[2] = (CardView)v.findViewById(R.id.freecell3);
        mFreecellView[3] = (CardView)v.findViewById(R.id.freecell4);

        for (int i = 0; i < mFreecellView.length; i++)
        {
        	CardView view = mFreecellView[i];
        	view.setOnClickListener(mFreecellClick);
        }

        mTableauView[0] = (CardStackView)v.findViewById(R.id.stack1);
        mTableauView[1] = (CardStackView)v.findViewById(R.id.stack2);
        mTableauView[2] = (CardStackView)v.findViewById(R.id.stack3);
        mTableauView[3] = (CardStackView)v.findViewById(R.id.stack4);
        mTableauView[4] = (CardStackView)v.findViewById(R.id.stack5);
        mTableauView[5] = (CardStackView)v.findViewById(R.id.stack6);
        mTableauView[6] = (CardStackView)v.findViewById(R.id.stack7);
        mTableauView[7] = (CardStackView)v.findViewById(R.id.stack8);

        for (int i = 0; i < mTableauView.length; i++)
        {
        	CardStackView view = mTableauView[i];
        	view.setOnClickListener(mTableauClick);
        }
        
        mFoundationView[0] = (CardStackView)v.findViewById(R.id.ace1);
        mFoundationView[1] = (CardStackView)v.findViewById(R.id.ace2);
        mFoundationView[2] = (CardStackView)v.findViewById(R.id.ace3);
        mFoundationView[3] = (CardStackView)v.findViewById(R.id.ace4);

        for (int i = 0; i < mFoundationView.length; i++)
        {
        	CardStackView view = mFoundationView[i];        	
        	view.setOnClickListener(mFoundationClick);
        	view.setCardOrientation(CardStackView.Orientation.SINGLE);
        }

		mRoot = v;
	}
	
	@Override
	public View getGameView()
	{
		return mRoot;
	}

	@Override
	public void newGame(long seed)
	{
		Deck deck = new Deck();
		deck.shuffle(new Random(seed));

		for (int i = 0; i < mTableau.length; i++)
		{
			mTableau[i] = new CardStack(10);
			mTableau[i].addAll(deck.deal((i < 4) ? 7 : 6, true));
			mTableauView[i].connectToCardStack(mTableau[i],
			  new DefaultCardStackObserver(mManager, mTableauView[i]));
		}

		for (int i = 0; i < mFreecellView.length; i++)
			mFreecellView[i].setCard(null);

		for (int i = 0; i < mFoundationView.length; i++)
		{
			mFoundation[i] = new CardStack(13);
			mFoundationView[i].connectToCardStack(mFoundation[i],
			  new DefaultCardStackObserver(mManager, mFoundationView[i]));
		}
	}

	@Override
	public boolean saveGame(GameOutputStream out)
	  throws IOException
	{
		return false;
	}

	@Override
	public boolean loadGame(GameInputStream in)
	  throws IOException
	{
		return false;
	}

	private final OnClickListener mTableauClick = new OnClickListener()
	{
		public void onClick(View v)
		{
			
		}
	};
	
	private final OnClickListener mFreecellClick = new OnClickListener()
	{
		public void onClick(View v)
		{
			
		}
	};
	
	private final OnClickListener mFoundationClick = new OnClickListener()
	{
		public void onClick(View v)
		{
			
		}
	};
}
