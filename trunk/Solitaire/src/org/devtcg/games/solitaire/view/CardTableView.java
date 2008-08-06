package org.devtcg.games.solitaire.view;

import java.util.Map;

import org.devtcg.games.solitaire.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Paint;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.PaintDrawable;
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

	public CardTableView(Context context, AttributeSet attrs, Map inflateParams)
	{
		super(context, attrs, inflateParams);
		init();
	}

	public CardTableView(Context context, AttributeSet attrs, Map inflateParams, int defStyle)
	{
		super(context, attrs, inflateParams, defStyle);
		init();
	}
	
	protected void init()
	{
		BitmapDrawable d = (BitmapDrawable)getResources().getDrawable(R.drawable.baize);
		setBackground(new TileBitmapDrawable(d.getBitmap()));
	}

	public static class TileBitmapDrawable extends PaintDrawable
	{
		public TileBitmapDrawable(Bitmap bmp)
		{
			super();
			setBitmap(bmp);
		}

		public TileBitmapDrawable()
		{
			super();
		}

		public void setBitmap(Bitmap bmp)
		{
			Paint p = new Paint(Paint.FILTER_BITMAP_FLAG);
			p.setShader(new BitmapShader(bmp, TileMode.REPEAT, TileMode.REPEAT));
			setPaint(p);
		}
	}
}
