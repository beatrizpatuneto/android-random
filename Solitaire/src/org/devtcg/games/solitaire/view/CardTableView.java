package org.devtcg.games.solitaire.view;

import org.devtcg.games.solitaire.R;

import android.content.Context;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Simple view which draws the card table background.
 */
public class CardTableView extends FrameLayout
{
	public CardTableView(Context context)
	{
		super(context);
		init();
	}

	public CardTableView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init();
	}
	
	protected void init()
	{
		BitmapDrawable d = (BitmapDrawable)getResources().getDrawable(R.drawable.baize);
		d.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
		setBackgroundDrawable(d);
	}
}
