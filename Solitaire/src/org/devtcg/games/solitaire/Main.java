package org.devtcg.games.solitaire;

import org.devtcg.games.solitaire.model.Card;
import org.devtcg.games.solitaire.model.CardStackObserver;
import org.devtcg.games.solitaire.model.Deck;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class Main extends Activity
{
	public static final String TAG = "Main";
	
    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        setContentView(R.layout.main);

        Deck deck = new Deck();
        deck.shuffle();

        deck.registerObserver(new CardStackObserver() {
			protected void onAdd(Card card)
			{
				Log.d(TAG, "Added card: " + card);
			}

			protected void onRemove(int pos)
			{
				Log.d(TAG, "Removed card: " + pos);
			}
        });

        Card c = deck.draw();
        Log.d(TAG, "Drew 1st card: " + c);
        c = deck.draw();
        Log.d(TAG, "Drew 2nd card: " + c);
    }
}