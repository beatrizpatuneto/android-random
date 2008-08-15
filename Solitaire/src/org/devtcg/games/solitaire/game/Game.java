package org.devtcg.games.solitaire.game;

import java.io.IOException;

import android.content.Context;
import android.view.View;

/**
 * Base class for all solitaire games.
 */
public abstract class Game
{
	/** Current game's randomization seed.  Used to support {@link restartGame}. */
	protected long mSeed = -1;

	/** Application context. */
	protected Context mContext;

	/** Game manager governing us.  Used to retrieve global preferences. */
	protected GameManager mManager;

	/**
	 * Treat this as your constructor.
	 * 
	 * TODO: Improve this crummy design by changing GameManager to an
	 * ActivityGroup and and using the local activity manager.  Game would
	 * then extend Activity.
	 * 
	 * @param mgr
	 * 
	 * @return
	 *   Should be a CardTableView.
	 */
	public void init(GameManager mgr)
	{
		mManager = mgr;
	}

	/**
	 * Access the current game seed.
	 */
	public long getSeed()
	{
		return mSeed;
	}

	/**
	 * Set the seed to use for the next game.  Generally only used to load game state.
	 */
	public void setSeed(long seed)
	{
		mSeed = seed; 
	}

	/**
	 * Simple convenience method to set the seed to a sensible value for a new game.
	 * 
	 * @return
	 *   The new seed.
	 */
	public long setDefaultSeed()
	{
		mSeed = System.currentTimeMillis() + hashCode();
		return mSeed;
	}

	/** @deprecated Going away soon when we switch to Activity abstraction. */
	public abstract View getGameView();

	/**
	 * Deal a new game with the specified seed.  Used to restart a game with
	 * the same seed, rather than create a new game.
	 */
	public abstract void newGame(long seed);

	/**
	 * Deal a new game by generating a new seed.
	 */
	public void newGame()
	{
		newGame(setDefaultSeed());
	}
	
	/**
	 * Convenience called when the game has been won.
	 */
	public void won()
	{
		mManager.onWin(this);
	}
	
	/**
	 * Serialize the game state for persistent storage.
	 * 
	 * @param out
	 *   Special game state stream to write to.
	 * 
	 * @return
	 *   True if the game state was written; false if the game doesn't support saves. 
	 */
	public abstract boolean saveGame(GameOutputStream out)
	  throws IOException;

	/**
	 * Load game state.  Apply the reverse of {@link saveGame} here.
	 * 
	 * @return
	 *   True if the game state was loaded; false otherwise.  If false, expect
	 *   a call to {@link newGame} to follow.
	 */
	public abstract boolean loadGame(GameInputStream in)
	  throws IOException;
}
