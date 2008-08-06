package org.devtcg.games.solitaire.view;

import java.util.Map;

import org.devtcg.games.solitaire.R;
import org.devtcg.games.solitaire.model.Card;
import org.devtcg.games.solitaire.model.Card.Rank;
import org.devtcg.games.solitaire.model.Card.Suit;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Basic card presentation. 
 */
public class CardView extends View
{
	private Rect mRect;
	private Paint mBorder;
	private Paint mBack;
	private Paint mSuitPaint;
	
	protected Card mCard;
	protected Drawable mCardDrawable;
	protected Drawable mSuitDrawable;
	protected boolean mFaceUp = true;
	
	public CardView(Context context)
	{
		super(context);		
		init();
	}

	public CardView(Context context, AttributeSet attrs, Map inflateParams)
	{
		super(context, attrs, inflateParams);
		init();
	}

	private void init()
	{
		mBorder = new Paint();
		mBorder.setStyle(Paint.Style.STROKE);
		mBorder.setColor(0xff000000);
		
		mBack = new Paint();
		mBack.setStyle(Paint.Style.FILL_AND_STROKE);
		mBack.setColor(0xff0f81dd);
		
		mSuitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mSuitPaint.setTypeface(Typeface.DEFAULT_BOLD);
		//mSuitPaint.setStyle(Paint.Style.STROKE);
		
		mRect = new Rect();
		
		setPreferredWidth(30);
		setPreferredHeight(40);
	}

	/**
	 * Set the card to draw.  Specifies suit and rank, not actually the bitmap data.
	 */
	public void setCard(Suit suit, Rank rank)
	{
		setCard(new Card(suit, rank));
	}

	public void setCard(Card card)
	{
		mCard = card;
		
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
		
		//mCardDrawable = getResources().getDrawable(getCardDrawableID(card));
		mCardDrawable = getResources().getDrawable(R.drawable.card);
		
		switch (mCard.getSuit())
		{
		case SPADES:
			mSuitDrawable = getResources().getDrawable(R.drawable.suit_spades);
			break;
		case CLUBS:
			mSuitDrawable = getResources().getDrawable(R.drawable.suit_clubs);
			break;
		default:
			mSuitDrawable = null;
			break;
		}
		
		invalidate();
	}

	private static int getCardDrawableID(Card card)
	{
		int n;
		int id;

		/* Order is ACE, 2, 3, 4, ..., JACK, QUEEN, KING */
		int r = card.getRank().ordinal();
		int s = card.getSuit().ordinal();

		/* Order is ACE, KING, QUEEN, JACK, 10, 9, 8, ... */
		if (r > 0)
			r = 13 - r;

		n = (r * 4) + s + 1;
		Log.d("Solitaire", "card(r=" + r + ",s=" + s + "): " + card + ", got n=" + n);

		switch (n)
		{
		case 1: id = R.drawable.card1; break;
		case 2: id = R.drawable.card2; break;
		case 3: id = R.drawable.card3; break;
		case 4: id = R.drawable.card4; break;
		case 5: id = R.drawable.card5; break;
		case 6: id = R.drawable.card6; break;
		case 7: id = R.drawable.card7; break;
		case 8: id = R.drawable.card8; break;
		case 9: id = R.drawable.card9; break;
		case 10: id = R.drawable.card10; break;
		case 11: id = R.drawable.card11; break;
		case 12: id = R.drawable.card12; break;
		case 13: id = R.drawable.card13; break;
		case 14: id = R.drawable.card14; break;
		case 15: id = R.drawable.card15; break;
		case 16: id = R.drawable.card16; break;
		case 17: id = R.drawable.card17; break;
		case 18: id = R.drawable.card18; break;
		case 19: id = R.drawable.card19; break;
		case 20: id = R.drawable.card20; break;
		case 21: id = R.drawable.card21; break;
		case 22: id = R.drawable.card22; break;
		case 23: id = R.drawable.card23; break;
		case 24: id = R.drawable.card24; break;
		case 25: id = R.drawable.card25; break;
		case 26: id = R.drawable.card26; break;
		case 27: id = R.drawable.card27; break;
		case 28: id = R.drawable.card28; break;
		case 29: id = R.drawable.card29; break;
		case 30: id = R.drawable.card30; break;
		case 31: id = R.drawable.card31; break;
		case 32: id = R.drawable.card32; break;
		case 33: id = R.drawable.card33; break;
		case 34: id = R.drawable.card34; break;
		case 35: id = R.drawable.card35; break;
		case 36: id = R.drawable.card36; break;
		case 37: id = R.drawable.card37; break;
		case 38: id = R.drawable.card38; break;
		case 39: id = R.drawable.card39; break;
		case 40: id = R.drawable.card40; break;
		case 41: id = R.drawable.card41; break;
		case 42: id = R.drawable.card42; break;
		case 43: id = R.drawable.card43; break;
		case 44: id = R.drawable.card44; break;
		case 45: id = R.drawable.card45; break;
		case 46: id = R.drawable.card46; break;
		case 47: id = R.drawable.card47; break;
		case 48: id = R.drawable.card48; break;
		case 49: id = R.drawable.card49; break;
		case 50: id = R.drawable.card50; break;
		case 51: id = R.drawable.card51; break;
		case 52: id = R.drawable.card52; break;
		default: throw new IllegalArgumentException();
		}

		return id;
	}

	public Card getCard()
	{
		return mCard;
	}

	public void setFaceUp(boolean faceUp)
	{
		mFaceUp = faceUp;
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		
		Rect r = mRect;
		getDrawingRect(r);
		
		if (mFaceUp && mCard != null)
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
			
			for (int i = 1; i < 4; i++)
				canvas.drawLine(r.left + 1, r.top + i, r.right - 1, r.top + i, mBack);
		}
	}
}
