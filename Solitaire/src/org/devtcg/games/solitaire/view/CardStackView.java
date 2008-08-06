package org.devtcg.games.solitaire.view;

import java.util.Map;

import org.devtcg.games.solitaire.R;

import android.content.Context;
import android.content.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class CardStackView extends ViewGroup
{
	private Rect mRect;
	private Paint mBorder;
	private Paint mBack;
	private Paint mSelected;
	
	public enum Orientation
	{
		HORIZONTAL, VERTICAL, SINGLE;
		public static Orientation get(int ordinal) { return values()[ordinal]; }
	}

	public enum Visibility
	{
		TOP_CARD_ONLY, ALL_CARDS, NONE;
		public static Visibility get(int ordinal) { return values()[ordinal]; }
	}

	protected Orientation mOrientation;
	protected Visibility mVisibility;
	
	public CardStackView(Context context)
	{
		super(context);
		init();

		setCardOrientation(Orientation.VERTICAL);
		setCardVisibility(Visibility.TOP_CARD_ONLY);
	}
	
	public CardStackView(Context context, AttributeSet attrs, Map inflateParams)
	{
		super(context, attrs, inflateParams);
		init();

		Resources.StyledAttributes a = context.obtainStyledAttributes(attrs,
		  R.styleable.CardStackView);

		int orientation = a.getInt(R.styleable.CardStackView_card_orientation,
		  Orientation.VERTICAL.ordinal());

		setCardOrientation(Orientation.get(orientation));

		int visibility = a.getInt(R.styleable.CardStackView_card_visibility,
		  Visibility.TOP_CARD_ONLY.ordinal());

		setCardVisibility(Visibility.get(visibility));
	}
	
	private void init()
	{
		mBorder = new Paint();
		mBorder.setStyle(Paint.Style.STROKE);
		mBorder.setColor(0xff000000);

		mBack = new Paint();
		mBack.setStyle(Paint.Style.FILL);
		mBack.setColor(0xff30ad00);

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

	/**
	 * Sets the parameters for the card stack visbility.  Default is TOP_CARD_ONLY.
	 * 
	 * @param visibility
	 *   Sets which cards in the stack are drawn face up.
	 */
	public void setCardVisibility(Visibility visibility)
	{
		int n;

		mVisibility = visibility;

		if ((n = getChildCount()) == 0)
			return;

		/* Kind of a micro optimization here, but whatever. */
		switch (mVisibility)
		{
		case ALL_CARDS:
			for (int i = 0; i < n; i++)
				((CardView)getChildAt(i)).setFaceUp(true);
			break;
		case TOP_CARD_ONLY:
			for (int i = 0; i < n - 1; i++)
				((CardView)getChildAt(i)).setFaceUp(false);
			((CardView)getChildAt(n - 1)).setFaceUp(true);
			break;
		case NONE:
			for (int i = 0; i < n - 1; i++)
				((CardView)getChildAt(i)).setFaceUp(false);
			break;
		}

		invalidate();
	}

	public void addCard(CardView view)
	{
		switch (mVisibility)
		{
		case ALL_CARDS:
			view.setFaceUp(true);
			break;
		case NONE:
			view.setFaceUp(false);
			break;
		case TOP_CARD_ONLY:
			int n = getChildCount();
			if (n > 0)
				((CardView)getChildAt(n - 1)).setFaceUp(false);
			view.setFaceUp(true);
			break;
		}
		
		view.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		addView(view);
		
		invalidate();
	}

	public void removeCard(int position)
	{
		removeViewAt(position);
		
		if (mVisibility == Visibility.TOP_CARD_ONLY)
		{
			int n = getChildCount();

			if (n > 0)
				((CardView)getChildAt(n - 1)).setFaceUp(true);
		}
		
		invalidate();
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b)
	{
		int n = getChildCount();
		int x = 0, xadj = 0;
		int y = 0, yadj = 0;

		if (n == 0)
			return;
		
		switch (mOrientation)
		{
		case HORIZONTAL:
			xadj = 4;
			break;
		case VERTICAL:
			yadj = 4;
			break;
		}

		for (int i = 0; i < n; i++)
		{
			View child = getChildAt(i);
			
			child.layout(x, y,
			  x + child.getMeasuredWidth(), y + child.getMeasuredHeight());

			x += xadj;
			y += yadj;
		}
	}

	@Override
	protected void onMeasure(int widthSpec, int heightSpec)
	{
		int n = getChildCount();
		
		if (n == 0)
		{
			setMeasuredDimension(30, 40);
			return;
		}

		if (mOrientation == Orientation.SINGLE)
		{
			int w = 0;
			int h = 0;

			if (n > 0)
			{
				View lastChild = getChildAt(n - 1);
				lastChild.measure(widthSpec, heightSpec);
				
				w = lastChild.getMeasuredWidth();
				h = lastChild.getMeasuredHeight();
			}

			setMeasuredDimension(w, h);
		}
		else
		{
			int vardim = 0;
			int fixeddim = 0;

			for (int i = 0; i < n; i++)
			{
				View child = getChildAt(i);

				if (mOrientation == Orientation.VERTICAL)
				{
					if (i == (n - 1))
						vardim += 40;
					else
						vardim += 4;

					child.measure(widthSpec,
					  View.MeasureSpec.makeMeasureSpec(40, View.MeasureSpec.EXACTLY));

					if (fixeddim == 0)
						fixeddim = child.getMeasuredWidth();
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
			canvas.drawLine(r.left + 1, r.top, r.right - 1, r.top, mBorder);
			canvas.drawLine(r.left + 1, r.bottom - 1, r.right - 1, r.bottom - 1, mBorder);
			canvas.drawLine(r.left, r.top + 1, r.left, r.bottom - 1, mBorder);
			canvas.drawLine(r.right - 1, r.top + 1, r.right - 1, r.bottom - 1, mBorder);

			canvas.drawRect(r.left + 1, r.top + 1, r.right - 1, r.bottom - 1, mBack);
		}

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
