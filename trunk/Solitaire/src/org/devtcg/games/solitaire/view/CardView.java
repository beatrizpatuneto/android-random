package org.devtcg.games.solitaire.view;

import org.devtcg.games.solitaire.R;
import org.devtcg.games.solitaire.model.Card;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Basic card presentation. 
 */
public class CardView extends View
{
	public static final String TAG = "CardView";
	
	private Rect mRect;
	private Paint mBorder;
	private Paint mBack;
	private Paint mEmpty;
	private Paint mSuitPaint;

	protected Card mCard;
	protected Drawable mCardDrawable;
	protected Drawable mSuitDrawable;
	
	/* mPreferredWidth/Height isn't available to us for some reason? */
	protected int mDesiredWidth;
	protected int mDesiredHeight;

	public CardView(Context context)
	{
		super(context);		
		init();
	}

	public CardView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init();
	}

	private void init()
	{
		mBorder = new Paint();
		mBorder.setStyle(Paint.Style.STROKE);
		mBorder.setColor(0xff000000);
		
		mBack = new Paint();
		mBack.setStyle(Paint.Style.FILL);
		mBack.setColor(0xff557fa4);
		
		mEmpty = new Paint();
		mEmpty.setStyle(Paint.Style.FILL);
		mEmpty.setColor(0xff578132);

		mSuitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mSuitPaint.setTypeface(Typeface.DEFAULT_BOLD);
		//mSuitPaint.setStyle(Paint.Style.STROKE);

		mRect = new Rect();
		
		/* TODO: Use the card drawable... */
		mDesiredWidth = 30;
		mDesiredHeight = 40;
	}

	public void setCard(Card card)
	{
		mCard = card;
		invalidate();
		
		if (card == null)
			return;
		
		switch (mCard.getSuit())
		{
		case DIAMONDS:
		case HEARTS:
			mSuitPaint.setColor(0xfff00f00);
			break;
		case SPADES:
		case CLUBS:
			mSuitPaint.setColor(0xff0f0f0f);
			break;
		}

		mCardDrawable = getResources().getDrawable(R.drawable.card);

		switch (mCard.getSuit())
		{
		case SPADES:
			mSuitDrawable = getResources().getDrawable(R.drawable.suit_spades);
			break;
		case CLUBS:
			mSuitDrawable = getResources().getDrawable(R.drawable.suit_clubs);
			break;
		case HEARTS:
			mSuitDrawable = getResources().getDrawable(R.drawable.suit_hearts);
			break;
		case DIAMONDS:
			mSuitDrawable = getResources().getDrawable(R.drawable.suit_diamonds);
			break;
		}
	}

	public Card getCard()
	{
		return mCard;
	}
	
	public int getDesiredWidth()
	{
		return mDesiredWidth;
	}
	
	public int getDesiredHeight()
	{
		return mDesiredHeight;
	}
	
	@Override
	protected void onMeasure(int widthSpec, int heightSpec)
	{
		setMeasuredDimension(resolveSize(mDesiredWidth, widthSpec),
		  resolveSize(mDesiredHeight, heightSpec));
	}
	
	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);

		Rect r = mRect;
		getDrawingRect(r);
		
		if (mCard == null)
		{
			canvas.drawLine(r.left + 1, r.top, r.right - 1, r.top, mBorder);
			canvas.drawLine(r.left + 1, r.bottom - 1, r.right - 1, r.bottom - 1, mBorder);
			canvas.drawLine(r.left, r.top + 1, r.left, r.bottom - 1, mBorder);
			canvas.drawLine(r.right - 1, r.top + 1, r.right - 1, r.bottom - 1, mBorder);

			r.left++; r.top++; r.right--; r.bottom--;
			canvas.drawRect(r, mEmpty);			
		}
		else if (mCard.isFaceUp() == true)
		{
			mCardDrawable.setBounds(r);
			mCardDrawable.draw(canvas);

			int baseline = mSuitPaint.getFontMetricsInt(null);

			String rankLetter = mCard.getRankAbbr();
			canvas.drawText(rankLetter, 4, baseline - 1, mSuitPaint);
			canvas.drawText(rankLetter, r.right - r.left - 11, r.bottom - r.top - 4, mSuitPaint);

			if (mSuitDrawable != null)
			{
				int w = mSuitDrawable.getIntrinsicWidth();
				int h = mSuitDrawable.getIntrinsicHeight();

				mSuitDrawable.setBounds(r.right - r.left - w - 2, 3, r.right - r.left - 2, 3 + h);
				mSuitDrawable.draw(canvas);

				mSuitDrawable.setBounds(3, r.bottom - r.top - h - 3, 3 + w, r.bottom - r.top - 3);
				mSuitDrawable.draw(canvas);
			}			
		}
		else
		{
			canvas.drawLine(r.left + 1, r.top, r.right - 1, r.top, mBorder);
			canvas.drawLine(r.left + 1, r.bottom - 1, r.right - 1, r.bottom - 1, mBorder);
			canvas.drawLine(r.left, r.top + 1, r.left, r.bottom - 1, mBorder);
			canvas.drawLine(r.right - 1, r.top + 1, r.right - 1, r.bottom - 1, mBorder);

			r.left++; r.top++; r.right--; r.bottom--;
			canvas.drawRect(r, mBack);			
		}
	}
}
