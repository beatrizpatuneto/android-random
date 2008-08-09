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
import android.widget.Toast;

public class Klondike extends Activity
{
	public static final String TAG = "Klondike";

	protected Deck mDeck;
	protected CardStack mDealt;
	protected CardStack[] mTableau;
	protected CardStack[] mFoundation;

	protected CardStackView mDeckView;
	protected CardStackView mDealtView; 
	protected CardStackView[] mFoundationView;
	protected CardStackView[] mTableauView;

	/** Flag indicating which of the foundation stacks have been filled.  The
	 *  game is won when all 4 are full.  This is simply a 4 bit flag, where each
	 *  bit position represents a suit corresponding to its ordinal. */ 
	private int mWinFlag = 0;

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
    	mDeckView = (CardStackView)findViewById(R.id.deck);
    	mDeckView.setCardOrientation(CardStackView.Orientation.SINGLE);
    	mDeckView.setOnClickListener(mDeckClick);
    	
    	mDealtView = (CardStackView)findViewById(R.id.dealt);
    	mDealtView.setCardOrientation(CardStackView.Orientation.SINGLE);
    	mDealtView.setOnClickListener(mDealtClick);

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
        	view.setCardOrientation(CardStackView.Orientation.SINGLE);
        }
    }

    private void newGame()
    {
    	/* Initialize models. */
        mDeck = new Deck();
        mDeck.shuffle();
        mDeckView.connectToCardStack(mDeck, new KlondikeObserver(mDeckView));

        mDealt = new CardStack();
        mDealtView.connectToCardStack(mDealt, new KlondikeObserver(mDealtView));
        
        mTableau = new CardStack[7];

        for (int i = 0; i < mTableau.length; i++)
        {
        	mTableau[i] = new CardStack(i + 4);
        	mTableau[i].addAll(mDeck.deal(i + 1, false));
        	mTableau[i].flipTopCard(true);
        	mTableauView[i].connectToCardStack(mTableau[i],
        	  new KlondikeObserver(mTableauView[i]));
        }

        mFoundation = new CardStack[4];

        for (int i = 0; i < mFoundation.length; i++)
        {
        	mFoundation[i] = new CardStack(13);
    		mFoundationView[i].connectToCardStack(mFoundation[i],
    		  new KlondikeObserver(mFoundationView[i]));
        }

        mDealt.add(mDeck.draw());

        Log.d(TAG, "Deck:");

        for (int i = 0; i < mDeck.size(); i++)
        {
        	Card card = mDeck.get(i);
        	Log.d(TAG, "  Card " + i + ": " + card + " (" + card.isFaceUp() + ")");
        }
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

    private final OnClickListener mDeckClick = new OnClickListener()
    {
    	public void onClick(View v)
    	{
    		if (mDeck.size() == 0)
    		{
    			int n = mDealt.size();
    			
    			/* Make sure all cards are flipped down. */
    			mDealt.flipTopCard(false);

    			while (n-- > 0)
    			{
    				Card card = mDealt.remove(n);
    				mDeck.add(card);
    			}
    		}
    		
			mDealt.flipTopCard(false);
			
			//int deal = Math.min(3, mDeck.size());
			int deal = 1;
			
			mDealt.addAll(mDeck.deal(deal, false));
			mDealt.flipTopCard(true);
			
			releaseHolding();
    	}
    };
    
    private final OnClickListener mDealtClick = new OnClickListener()
    {
    	public void onClick(View v)
    	{
			if (mDealt.size() >= 0)
				setHolding(mDealtView);
    	}
    };
    
    private final OnClickListener mFoundationClick = new OnClickListener()
    {
		public void onClick(View v)
		{
			CardStackView vv = (CardStackView)v;
			CardStack acestack = vv.getCardStack();
			
			if (mHolding == null)
				return;
			
			CardStack stack = mHolding.getCardStack();
			Card card = stack.peekTop();

			releaseHolding();

			if (card == null)
				return;

			Card acetop = acestack.peekTop();
			int rank;

			if (acetop == null)
				rank = Card.Rank.ACE.rankOrdinal();
			else
				rank = acetop.getRankOrdinal() + 1;

			if (card.getRankOrdinal() == rank &&
			    (acetop == null || acetop.getSuit() == card.getSuit()))
			{
				stack.removeTop();
				stack.flipTopCard(true);
				acestack.add(card);

				if (checkWin(acestack) == true)
				{
					Toast.makeText(Klondike.this, "You WIN!", Toast.LENGTH_LONG).show();
					newGame();
				}
			}
		}
    };

    private final OnClickListener mTableauClick = new OnClickListener()
    {
    	public void onClick(View v)
    	{
			CardStackView vv = (CardStackView)v;

			if (mHolding != null)
			{
				CardStack src = mHolding.getCardStack();
				CardStack dst = vv.getCardStack();

				int pos = findLegalTableauMove(src, dst);

				releaseHolding();
				
				if (pos >= 0)
				{
					int n = src.size();

					for (int i = pos; i < n; i++)
					{
						Card card = src.remove(pos);
						dst.add(card);
					}

					/* Check that we haven't now removed the top card from this
					 * stack, leaving an unflipped new top. */
					src.flipTopCard(true);
				}
				else
				{
					if (vv.getCardStack().size() > 0)
						setHolding(vv);						
				}
			}
			else
			{
				if (vv.getCardStack().size() > 0)
					setHolding(vv);
			}
    	}
    };

    /**
     * Checks for an returns a legal move between two tableau stacks.
     *  
     * @param src
     *   Stack to move from.
     *   
     * @param dst
     *   Stack to move to.
     *   
     * @return
     *   If found, the move position in <code>src</code> is returned; otherwise, -1.
     */
    private int findLegalTableauMove(CardStack src, CardStack dst)
    {
    	int srcn = src.size();

    	if (srcn == 0)
    		return -1;
    	
    	Card dsttop = dst.peekTop();

    	int targetOrd;
    	boolean targetIsRed;
    	
    	if (dsttop != null)
    	{
    		targetOrd = dsttop.getRankOrdinal() - 1;
    		targetIsRed = Card.isSuitBlack(dsttop.getSuit());
    	}
    	else
    	{
    		targetOrd = Card.Rank.KING.rankOrdinal();
    		targetIsRed = false; /* Irrelevant... */
    	}
    	
    	if (targetOrd < 2)
    		return -1;

    	/* TODO: Optimize. */
    	for (int i = srcn - 1; i >= 0; i--)
    	{
    		Card check = src.get(i);

    		if (check.isFaceUp() == false)
    			break;

    		if (check.getRankOrdinal() == targetOrd)
    		{
    			if (targetOrd == Card.Rank.KING.rankOrdinal())
    				return i;
    			else
    			{
    				if (targetIsRed == Card.isSuitRed(check.getSuit()))
    					return i;
    				else
    					break;
    			}
    		}
    	}

    	return -1;
    }

	private void setHolding(CardStackView stack)
	{
		if (mHolding == stack)
			return;

		releaseHolding();
		stack.setSelected(true);
		mHolding = stack;
	}

	private void releaseHolding()
	{
		if (mHolding != null)
		{
			mHolding.setSelected(false);
			mHolding = null;
		}
	}
	
	private boolean checkWin(CardStack foundation)
	{
		if (foundation.size() < 13)
			return false;
		
		int bit = foundation.peekTop().getSuit().ordinal();
		
		assert (mWinFlag & (1 << bit)) == 0;
		mWinFlag |= (1 << bit);

		/* Returns true if the game is won; false otherwise. */
		return (mWinFlag == 0xf);
	}
	
    public class KlondikeObserver extends CardStackObserver
    {
    	protected CardStackView mView;

    	public KlondikeObserver(CardStackView view)
    	{
    		super();
    		mView = view;
    	}

		@Override
		protected void onAdd(CardStack stack, Card card)
		{
			Log.d(TAG, mView + ": Adding " + card);
			CardView view = new CardView(Klondike.this);
			view.setCard(card);
			mView.addCard(view);
			mView.invalidate();
		}

		@Override
		protected void onRemove(CardStack stack, int pos)
		{
			CardView view = (CardView)mView.getChildAt(pos);
			Log.d(TAG, mView + ": Removing " + view.getCard());
			mView.removeCard(pos);
			mView.invalidate();
		}
    }
}
