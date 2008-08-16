package org.devtcg.games.solitaire.view;

import java.util.Map;

import org.devtcg.games.solitaire.R;
import org.devtcg.games.solitaire.model.Card;
import org.devtcg.games.solitaire.model.CardStack;
import org.devtcg.games.solitaire.model.CardStackObserver;

import android.content.Context;
import android.content.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

public class CardStackView extends ViewGroup
{
	public static final String TAG = "CardStackView";

	private Rect mRect;
	private Paint mBorder;
	private Paint mBack;
	private Paint mSelected;

	/** Connected card stack, if any. */
	protected CardStack mStack;

	public enum Orientation
	{
		HORIZONTAL, VERTICAL, SINGLE;
		public static Orientation get(int ordinal) { return values()[ordinal]; }
	}

	protected Orientation mOrientation;

	private static final int STACK_OFFSET = 16;

	public CardStackView(Context context)
	{
		super(context);
		init();

		setCardOrientation(Orientation.VERTICAL);
	}

	public CardStackView(Context context, AttributeSet attrs, Map inflateParams)
	{
		super(context, attrs, inflateParams);
		init();

		/* XXX: This doesn't work but I don't understand why. */
		Resources.StyledAttributes a = context.obtainStyledAttributes(attrs,
		  R.styleable.CardStackView);

		int orientation = a.getInt(R.styleable.CardStackView_card_orientation,
		  Orientation.VERTICAL.ordinal());

		Log.d(TAG, "orientation=" + orientation);

		setCardOrientation(Orientation.get(orientation));
	}
	
	private void init()
	{
		mBorder = new Paint();
		mBorder.setStyle(Paint.Style.STROKE);
		mBorder.setColor(0xff000000);

		mBack = new Paint();
		mBack.setStyle(Paint.Style.FILL);
		mBack.setColor(0xff578132);

		mSelected = new Paint();
		mSelected.setStyle(Paint.Style.STROKE);
		mSelected.setColor(0xffe3b705);

		mRect = new Rect();
	}

	/**
	 * Sets the stack orientation.  Default is VERTICAL.
	 * 
	 * @param orientation
	 *   What direction the cards move away from each other.  Either HORIZONTAL or VERTICAL.
	 */
	public void setCardOrientation(Orientation orientation)
	{
		mOrientation = orientation;
	}
	
	public void connectToCardStack(CardStack stack, CardStackObserver o)
	{
		stack.registerObserver(o);
		mStack = stack;

		removeAllViews();

		for (Card card: stack)
		{
			CardView view = new CardView(mContext);
			view.setCard(card);
			addCard(view);
		}
	}
	
	public CardStack getCardStack()
	{
		return mStack;
	}

	public void addCard(CardView view)
	{
		view.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		addView(view);
		
		invalidate();
	}

	public void removeCard(int position)
	{
		removeViewAt(position);
		invalidate();
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b)
	{
		int n = getChildCount();
		int x = mPaddingLeft, xadj = 0;
		int y = mPaddingTop, yadj = 0;

		if (n == 0)
			return;
		
		switch (mOrientation)
		{
		case HORIZONTAL:
			xadj = STACK_OFFSET;
			break;
		case VERTICAL:
			yadj = STACK_OFFSET;
			break;
		}

		for (int i = 0; i < n; i++)
		{
			View child = getChildAt(i);

			child.layout(x, y,
			  x + child.getMeasuredWidth(),
			  y + child.getMeasuredHeight());

			x += xadj;
			y += yadj;
		}
	}

	@Override
	protected void onMeasure(int widthSpec, int heightSpec)
	{
		int n = getChildCount();
		int paddingWidth = mPaddingLeft + mPaddingRight;
		int paddingHeight = mPaddingTop + mPaddingBottom;
		
		if (n == 0)
		{
			Drawable card = getResources().getDrawable(R.drawable.card);

			setMeasuredDimension(card.getIntrinsicWidth() + paddingWidth,
			  card.getIntrinsicHeight() + paddingHeight);

			return;
		}

		if (mOrientation == Orientation.SINGLE)
		{
			int w = 0;
			int h = 0;

			View lastChild = getChildAt(n - 1);
			lastChild.measure(widthSpec, heightSpec);

			w = lastChild.getMeasuredWidth() + paddingWidth;
			h = lastChild.getMeasuredHeight() + paddingHeight;

			setMeasuredDimension(w, h);
		}
		else
		{
			int vardim = 0;
			int fixeddim = 0;

			for (int i = 0; i < n; i++)
			{
				CardView child = (CardView)getChildAt(i);

				if (mOrientation == Orientation.VERTICAL)
				{
					child.measure(widthSpec, 
					  View.MeasureSpec.makeMeasureSpec(child.getDesiredHeight(),
					    View.MeasureSpec.EXACTLY));

					if (i == (n - 1))
						vardim += child.getMeasuredHeight() + paddingHeight;
					else
						vardim += STACK_OFFSET;

					if (fixeddim == 0)
						fixeddim = child.getMeasuredWidth() + paddingWidth;
				}
			}

			setMeasuredDimension(fixeddim, vardim);
		}
	}

	@Override
	protected void dispatchDraw(Canvas canvas)
	{
		Rect r = mRect;
		getDrawingRect(r);

		if (getChildCount() == 0)
		{
			int pl = mPaddingLeft;
			int pt = mPaddingTop;
			int pr = mPaddingRight;
			int pb = mPaddingBottom;

			canvas.drawLine(r.left + pl + 1, r.top + pt, r.right - 1 - pr, r.top + pt, mBorder);
			canvas.drawLine(r.left + pl + 1, r.bottom - 1 - pb, r.right - 1 - pr, r.bottom - 1 - pb, mBorder);
			canvas.drawLine(r.left + pl, r.top + 1 + pt, r.left + pl, r.bottom - 1 - pb, mBorder);
			canvas.drawLine(r.right - 1, r.top + 1 + pt, r.right - 1 - pr, r.bottom - 1 - pb, mBorder);

			canvas.drawRect(r.left + pl + 1, r.top + 1 + pt, r.right - 1 - pr, r.bottom - 1 - pb, mBack);
		}

		/* TODO: In a stack, we shouldn't need to draw each card in full.  In
		 * every case, we can save drawing area, but we don't have an elegant
		 * way to propogate the clipping yet. */
		super.dispatchDraw(canvas);

		if (isSelected() == true)
		{
			canvas.drawLine(r.left + 1, r.top, r.right - 1, r.top, mSelected);
			canvas.drawLine(r.left + 1, r.bottom - 1, r.right - 1, r.bottom - 1, mSelected);
			canvas.drawLine(r.left, r.top + 1, r.left, r.bottom - 1, mSelected);
			canvas.drawLine(r.right - 1, r.top + 1, r.right - 1, r.bottom - 1, mSelected);
		}
	}
}
