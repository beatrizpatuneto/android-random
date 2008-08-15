package org.devtcg.games.solitaire.game;

import java.io.IOException;
import java.util.Random;

import org.devtcg.games.solitaire.R;
import org.devtcg.games.solitaire.model.Card;
import org.devtcg.games.solitaire.model.CardStack;
import org.devtcg.games.solitaire.model.CardStackObserver;
import org.devtcg.games.solitaire.model.Deck;
import org.devtcg.games.solitaire.view.CardStackView;
import org.devtcg.games.solitaire.view.CardView;

import android.view.View;
import android.view.ViewInflate;
import android.view.View.OnClickListener;

public class Klondike extends Game
{
	public static final String TAG = "Klondike";
	
	public static final String TMP_STATE_FILE = "gamestate";
	
	protected View mRoot;

	protected Deck mDeck;
	protected CardStack mWaste;
	protected CardStack[] mTableau = new CardStack[7];
	protected CardStack[] mFoundation = new CardStack[4];
	
	/**
	 * Foundation reference, indexed by suit ordinal for fast searching.  The
	 * main foundation model is ordered to reflect how the user has position
	 * the cards.
	 */
	protected CardStack[] mFoundationIndex = new CardStack[4];

	protected CardStackView mDeckView;
	protected CardStackView mWasteView; 
	protected CardStackView[] mTableauView = new CardStackView[7];
	protected CardStackView[] mFoundationView = new CardStackView[4];

	/** 
	 * Flag indicating which of the foundation stacks have been filled.  The
	 * game is won when all 4 are full.  This is simply a 4 bit flag, where
	 * each bit position represents a suit corresponding to its ordinal.
	 */ 
	private int mWinFlag = 0;

	/** The stack that we are currently holding. */
	protected CardStackView mHolding;
	
	@Override
	public String getName()
	{
		return TAG;
	}

    @Override
    public void init(GameManager mgr)
    {
        super.init(mgr);

        ViewInflate inflate = ViewInflate.from(mgr);
        View v = inflate.inflate(R.layout.klondike, null, null);

		initViews(v);

		mRoot = v;
    }

    private void initViews(View v)
    {
    	mDeckView = (CardStackView)v.findViewById(R.id.deck);
    	mDeckView.setCardOrientation(CardStackView.Orientation.SINGLE);
    	mDeckView.setOnClickListener(mDeckClick);

    	mWasteView = (CardStackView)v.findViewById(R.id.dealt);
    	mWasteView.setCardOrientation(CardStackView.Orientation.SINGLE);
    	mWasteView.setOnClickListener(mDealtClick);

        mTableauView[0] = (CardStackView)v.findViewById(R.id.stack1);
        mTableauView[1] = (CardStackView)v.findViewById(R.id.stack2);
        mTableauView[2] = (CardStackView)v.findViewById(R.id.stack3);
        mTableauView[3] = (CardStackView)v.findViewById(R.id.stack4);
        mTableauView[4] = (CardStackView)v.findViewById(R.id.stack5);
        mTableauView[5] = (CardStackView)v.findViewById(R.id.stack6);
        mTableauView[6] = (CardStackView)v.findViewById(R.id.stack7);

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
    }
    
	@Override
	public View getGameView()
	{
		return mRoot;
	}
	
    @Override
    public void newGame(long seed)
    {
    	/* Initialize models. */
        mDeck = new Deck();
        mDeck.shuffle(new Random(seed));
        mDeckView.connectToCardStack(mDeck, new KlondikeObserver(mDeckView));

        mWaste = new CardStack();
        mWasteView.connectToCardStack(mWaste, new KlondikeObserver(mWasteView));
        
        for (int i = 0; i < mTableau.length; i++)
        {
        	mTableau[i] = new CardStack(i + 4);
        	mTableau[i].addAll(mDeck.deal(i + 1, false));
        	mTableau[i].flipTopCard(true);
        	mTableauView[i].connectToCardStack(mTableau[i],
        	  new KlondikeObserver(mTableauView[i]));
        }

        for (int i = 0; i < mFoundation.length; i++)
        {
        	mFoundation[i] = new CardStack(13);
    		mFoundationView[i].connectToCardStack(mFoundation[i],
    		  new KlondikeObserver(mFoundationView[i]));
        }
    }

	@Override
    public boolean loadGame(GameInputStream in)
	  throws IOException
    {
		mDeck = Deck.valueOf(in.readCardStack());
        mDeckView.connectToCardStack(mDeck, new KlondikeObserver(mDeckView));
        mWaste = in.readCardStack();
        mWasteView.connectToCardStack(mWaste, new KlondikeObserver(mWasteView));

        mFoundation = in.readCardStacks();

    	for (int i = 0; i < mFoundation.length; i++)
    	{
        	mFoundationView[i].connectToCardStack(mFoundation[i],
              new KlondikeObserver(mFoundationView[i]));
    	}

    	mTableau = in.readCardStacks();

        for (int i = 0; i < mTableau.length; i++)
        {
        	mTableauView[i].connectToCardStack(mTableau[i],
              new KlondikeObserver(mTableauView[i]));
        }

		return true;
    }

	@Override
    public boolean saveGame(GameOutputStream out)
	  throws IOException
    {
		out.writeCardStack(mDeck);
		out.writeCardStack(mWaste);
		out.writeCardStacks(mFoundation);
		out.writeCardStacks(mTableau);
		
		return true;
    }

    private final OnClickListener mDeckClick = new OnClickListener()
    {
    	public void onClick(View v)
    	{
    		if (mDeck.size() == 0)
    		{
    			int n = mWaste.size();
    			
    			/* Nothing to do, back out. */
    			if (n == 0)
    				return;

    			/* Make sure all cards are flipped down. */
    			mWaste.flipTopCard(false);

    			while (n-- > 0)
    			{
    				Card card = mWaste.remove(n);
    				mDeck.add(card);
    			}
    		}

			mWaste.flipTopCard(false);

			int deal = Math.min(3, mDeck.size());

			mWaste.addAll(mDeck.deal(deal, false));
			flipTopCardUpThenAutoplay(mWaste);

			releaseHolding();
    	}
    };
    
    private CardStack findEmptyFoundation()
    {
    	for (int i = 0; i < mFoundation.length; i++)
    	{
    		if (mFoundation[i].size() == 0)
    			return mFoundation[i];
    	}
    	
    	return null;
    }
    
    private boolean flipTopCardUpThenAutoplay(CardStack stack)
    {
    	Card play = stack.flipTopCard(true);

    	if (play == null)
    		return false;

    	Card.Suit playSuit = play.getSuit();
    	CardStack mine = mFoundationIndex[playSuit.ordinal()];

    	/* Nothing here already, throw up the ace. */
    	if (mine == null)
    	{
    		if (play.getRank() != Card.Rank.ACE)
    			return false;
    		
    		mine = findEmptyFoundation();
    		mFoundationIndex[playSuit.ordinal()] = mine;
    		playFoundationAndCheckWin(mine, stack);
    		return true;
    	}
    	/* Carefully check for a safe autoplay move. */
    	else
    	{
    		Card f = mine.peekTop();

    		int playRank = play.getRankOrdinal();

    		/* First check that the move is legal. */
    		if ((f.getRankOrdinal() + 1) != playRank)
    			return false;
    		
    		/* Then make sure it is safe by checking the ranks of opposing
    		 * suit colors. */
    		CardStack[] opp = new CardStack[2];

    		if (Card.isSuitRed(playSuit) == true)
    		{
    			opp[0] = mFoundationIndex[Card.Suit.SPADES.ordinal()];
    			opp[1] = mFoundationIndex[Card.Suit.CLUBS.ordinal()];
    		}
    		else
    		{
    			opp[0] = mFoundationIndex[Card.Suit.HEARTS.ordinal()];
    			opp[1] = mFoundationIndex[Card.Suit.DIAMONDS.ordinal()];
    		}

    		for (int i = 0; i < opp.length; i++)
    		{
    			int oppRank = (opp[i] != null) ?
				  opp[i].peekTop().getRankOrdinal() : 0;

				/* This would not be a safe move, abort. */
				if (oppRank + 2 < playRank)
					return false;
    		}

    		playFoundationAndCheckWin(mine, stack);
    		return true;
    	}
    }
    
    /**
     * Scan the play field (tableau only) for a playable card to the supplied
     * foundation.  The waste is not scanned here as there are other conditions
     * which will result in autoplay checking there.
     * 
     * @return
     *   True if a card was autoplayed; false otherwise.
     */
    private boolean scanAutoplay(CardStack foundation)
    {
    	Card f = foundation.peekTop();

    	/* Huh, why were we asked to autoplay an empty foundation? */
    	if (f == null)
    		return false;

    	Card.Suit targetSuit = f.getSuit(); 
    	int targetRank = f.getRankOrdinal() + 1;

    	for (int i = 0; i < mTableau.length; i++)
    	{
    		Card check = mTableau[i].peekTop();
    		
    		if (check != null &&
    		    check.getSuit() == targetSuit &&
    		    check.getRankOrdinal() == targetRank)
    		{
    			playFoundationAndCheckWin(foundation, mTableau[i]);
    			return true;
    		}
    	}

    	return false;
    }

    private boolean playFoundationAndCheckWin(CardStack foundation, CardStack playTop)
    {
		Card play = playTop.removeTop();
		foundation.add(play);

		if (checkWin(foundation) == true)
		{
			won();
			return true;
		}

		/* XXX: There is a bug here wherein if we win during autoplay (which
		 * is likely!) we won't return the correct result from the initial
		 * invocation of this function.  It isn't a problem in practice
		 * because we don't check the return value, but just something to
		 * consider. */
		flipTopCardUpThenAutoplay(playTop);
		scanAutoplay(foundation);

		return false;
    }
    
    private final OnClickListener mDealtClick = new OnClickListener()
    {
    	public void onClick(View v)
    	{
			if (mWaste.size() >= 0)
				setHolding(mWasteView);
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

			if (acetop == null)
			{
				if (card.getRank() == Card.Rank.ACE)
				{
					mFoundationIndex[card.getSuit().ordinal()] = acestack;
					playFoundationAndCheckWin(acestack, stack);
				}
			}
			else
			{
				if (card.getRankOrdinal() == acetop.getRankOrdinal() + 1)
					playFoundationAndCheckWin(acestack, stack);
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
					flipTopCardUpThenAutoplay(src);
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
			CardView view = new CardView(mManager);
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
}
