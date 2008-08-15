package org.devtcg.games.solitaire.game;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import org.devtcg.games.solitaire.R;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.Toast;

public class GameManager extends Activity
{
	public static final String TAG = "GameManager";
	
	public static final String PREFS_LAST_GAME = "lastGame";
	
	protected static final String STATE_FILE = "gamestate";
	
	protected static final int MENU_NEW_GAME = Menu.FIRST;
	protected static final int MENU_RESTART_GAME = Menu.FIRST + 1;
	protected static final int MENU_CHANGE_RULES = Menu.FIRST + 2;

	protected static HashMap<Integer, Class> mGames = new HashMap<Integer, Class>();
	protected Game mCurrent;

	private FrameLayout mRoot;

	@Override
	public void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);
		setContentView(R.layout.main);

		mRoot = (FrameLayout)findViewById(R.id.root);

		registerGame(Klondike.ruleId, Klondike.class);
//		registerGame(Freecell.ruleId, Freecell.class);

		Game game;

		if ((game = tryLoadGame()) == null)
		{
			if ((game = tryNewGame()) == null)
			{
				Toast.makeText(this, "PANIC: Unable to find suitable game class", 
				  Toast.LENGTH_LONG).show();
				finish();
				return;
			}
		}

		mCurrent = game;

		View root = mCurrent.getGameView();
		mRoot.addView(root, new LayoutParams(LayoutParams.FILL_PARENT,
		  LayoutParams.FILL_PARENT));
	}

	protected void registerGame(int ruleId, Class game)
	{
		mGames.put(ruleId, game);
	}

	protected Class lookupGame(int ruleId)
	{
		return mGames.get(ruleId);
	}

	public Game tryLoadGame()
	{
		FileInputStream inf = null;
		GameInputStream in = null;
		
		int ruleId = -1;

		try {
			inf = openFileInput(STATE_FILE);
			in = new GameInputStream(inf);

			ruleId = in.readInt();
			long seed = in.readLong();

			Class gameClass;

			if ((gameClass = lookupGame(ruleId)) == null)
			{
				Log.d(TAG, "Game " + ruleId + " not found, weird.");
				return null;
			}

			Game game = (Game)gameClass.newInstance();
			game.init(this);
			game.setSeed(seed);

			if (game.loadGame(in) == false)
				return null;

			return game;
		} catch (Exception e) {
			Log.d(TAG, "Unable to load saved game from " + STATE_FILE, e);
			return null;
		} finally {
			if (in != null)
				try { in.close(); } catch (IOException e) {}
			else if (inf != null)
				try { inf.close(); } catch (IOException e) {}
		}
	}

	public Game tryNewGame()
	{
		SharedPreferences prefs = getSharedPreferences(TAG, MODE_PRIVATE);
		int ruleId = prefs.getInt(PREFS_LAST_GAME, Klondike.ruleId);

		Class gameClass;

		if ((gameClass = lookupGame(ruleId)) == null)
		{
			Log.d(TAG, "Game " + ruleId + " not found, weird.");
			
			if ((gameClass = lookupGame(Klondike.ruleId)) == null)
				return null;
		}

		try {
			Game game = (Game)gameClass.newInstance();
			game.init(this);
			game.newGame();
			return game;
		} catch (Exception e) {
			Log.d(TAG, "Unable to instantiate game with rule " + ruleId, e);
			return null;
		}
	}

	@Override
	protected void onPause()
	{
		FileOutputStream outf = null;
		GameOutputStream out = null;

		try {
			outf = openFileOutput(STATE_FILE, MODE_PRIVATE);
			out = new GameOutputStream(outf);

			out.writeInt(mCurrent.ruleId);
			out.writeLong(mCurrent.getSeed());
			mCurrent.saveGame(out);
			out.close();
		} catch (IOException e) {
			Log.d(TAG, "Unable to save game state", e);
		} finally {
			if (out != null)
				try { out.close(); } catch (IOException e) {}
			else if (outf != null)
				try { outf.close(); } catch (IOException e) {}
		}

		super.onPause();
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
    	super.onCreateOptionsMenu(menu);
    	
    	menu.add(0, MENU_NEW_GAME, "New Game");
    	menu.add(0, MENU_RESTART_GAME, "Restart Game");
    	menu.add(0, MENU_CHANGE_RULES, "Choose Game");
    	
    	return true;
    }

    @Override
    public boolean onOptionsItemSelected(Menu.Item item)
    {
    	switch (item.getId())
    	{
    	case MENU_NEW_GAME:
    		deleteFile(STATE_FILE);
    		mCurrent.newGame();
    		return true;
    	case MENU_RESTART_GAME:
    		deleteFile(STATE_FILE);
    		mCurrent.newGame(mCurrent.getSeed());
    		return true;
    	case MENU_CHANGE_RULES:
    		/* TODO... */
    		return true;
    	}
    	
    	return super.onOptionsItemSelected(item);
    }
    
    public void onWin(Game game)
    {
    	assert mCurrent == game;
    	
		Toast.makeText(this, "You WIN!", Toast.LENGTH_LONG).show();
		game.newGame();
    }
}
