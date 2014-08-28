package com.bitflake.pull.views;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.FrameLayout;

import com.bitflake.managers.utils.AnimationAdapter;
import com.bitflake.managers.utils.AnimationHider;
import com.bitflake.pull.R;
import com.bitflake.pull.game.MarkingGenerator;
import com.bitflake.pull.game.MarkingGenerator.Dot;

public class GameField extends FrameLayout {

	public interface SatisfactionListener {
		public void onSatisfaction();
	}

	private FieldLayout fieldLayout;
	private DotView[] dots;
	private Rect hitRect = new Rect();
	private SparseArray<DotView> touchedDots = new SparseArray<>();
	private SatisfactionListener satisfactionListener;
	private boolean isDemoMode;
	// private AnimatorSet demoAnimation;
	private View indicator;
	private AnimationHider indicatorHider;
	private Context context;
	private ConnectionGridView connectionView;

	public void setSatisfactionListener(
			SatisfactionListener satisfactionListener) {
		this.satisfactionListener = satisfactionListener;
	}

	public GameField(Context context) {
		super(context);
	}

	public GameField(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public GameField(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void enableDemoMode() {
		this.isDemoMode = true;
	}

	public void init(MarkingGenerator markingGenerator) {
		fieldLayout = new FieldLayout(getContext(), markingGenerator);
		connectionView = new ConnectionGridView(getContext(), fieldLayout);
		this.addView(connectionView);
		this.addView(fieldLayout);
		fieldLayout.setClipChildren(false);
		this.setClipChildren(false);

		Dot[] initDots = markingGenerator.getDots();
		this.dots = new DotView[initDots.length];
		for (int i = 0; i < initDots.length; i++) {
			Dot initDot = initDots[i];
			DotView dot = new DotView(getContext(), initDot.color);
			this.addView(dot);
			dots[i] = dot;
		}

		int indicatorSize = (int) (getResources().getDisplayMetrics().density * 48);
		indicator = new View(getContext());
		this.addView(indicator, new LayoutParams(indicatorSize, indicatorSize));
		indicator.setBackgroundResource(R.drawable.indicator);
		indicator.setVisibility(View.INVISIBLE);

		indicatorHider = new AnimationHider(View.INVISIBLE, indicator);
		this.getViewTreeObserver().addOnPreDrawListener(
				new OnPreDrawListener() {
					@Override
					public boolean onPreDraw() {
						getViewTreeObserver().removeOnPreDrawListener(this);
						reset();
						return true;
					}
				});
		// this.addOnLayoutChangeListener(new OnLayoutChangeListener() {
		//
		// @Override
		// public void onLayoutChange(View v, int left, int top, int right,
		// int bottom, int oldLeft, int oldTop, int oldRight,
		// int oldBottom) {
		// if(demoAnimation!=null){
		// // demoAnimation.end();
		// }
		// }
		// });
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (fieldLayout == null || !isEnabled()
				|| event.getPointerId(event.getActionIndex()) > 0)
			return false;
		int id = event.getPointerId(event.getActionIndex());
		float x = event.getX(event.getActionIndex());
		float y = event.getY(event.getActionIndex());
		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_POINTER_DOWN: {
			FieldView field = getTouchedField((int) x, (int) y);
			if (field != null && field.hasDot()) {
				getParent().requestDisallowInterceptTouchEvent(true);
				DotView dot = field.getDot();
				fieldLayout.registerDot(field, dot);
				connectionView.showConnections(dot);
				fieldLayout.showAcceptingFields(dot);
				dot.onTouchDown(x - hitRect.left, y - hitRect.top);
				touchedDots.put(id, dot);
				dot.bringToFront();
				fieldLayout.showBorder(field);
				return true;
			}
		}
			break;
		case MotionEvent.ACTION_MOVE: {
			// for (int i = 0; i < event.getPointerCount(); i++) {
			id = event.getPointerId(event.getActionIndex());
			DotView dot = touchedDots.get(id);
			if (dot != null) {
				x = event.getX();
				y = event.getY();
				FieldView field = getTouchedField((int) x, (int) y);

				dot.setX(x - dot.getWidth() / 2);
				dot.setY(y - dot.getHeight() / 2);

				if (field != null) {
					if (field.accepts(dot)) {
						dot.setField(field);
					}
				}
				return true;
			}
			// }
		}
			break;
		case MotionEvent.ACTION_POINTER_UP:
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP: {
			if (event.getPointerCount() <= 1)
				getParent().requestDisallowInterceptTouchEvent(false);

			FieldView field = getTouchedField((int) x, (int) y);
			DotView dot = touchedDots.get(id);
			touchedDots.remove(id);
			if (dot != null) {
				if (field != null && field.accepts(dot)) {
					dot.moveTo(field);
				} else {
					dot.moveBack();
				}
				fieldLayout.hideBorder();
				fieldLayout.unregisterDot(dot.getField(), dot);
				fieldLayout.hideAcceptingFields(dot);
				connectionView.hideConnections();
				dot.onTouchUp();
				checkSatisfaction();
				return true;
			}

			break;
		}
		}

		return false;
	}

	private void checkSatisfaction() {
		if (fieldLayout.isSatisfied()) {
			connectionView.hideConnectionsImediate();
			isDemoMode = false;
			do {
				fieldLayout.nextMarking();
			} while (fieldLayout.isSatisfied());
			if (satisfactionListener != null)
				satisfactionListener.onSatisfaction();
		}
	}

	private Runnable demoRunable = new Runnable() {
		private AnimatorSet demoAnimation;

		@Override
		public void run() {
			if (isDemoMode) {
				if (touchedDots.size() == 0
						&& (demoAnimation == null || !demoAnimation.isRunning())) {
					demoAnimation = getDemoAnimation();
					demoAnimation.start();
				}
				postDelayed(this, 5000);
			}
		}
	};

	private AnimatorSet getDemoAnimation() {
		FieldView startField = fieldLayout.getField(1);
		FieldView endFiield = fieldLayout.getField(0);
		final float startX = startField.getX()
				+ (startField.getWidth() - indicator.getWidth()) / 2;
		final float startY = startField.getY()
				+ (startField.getHeight() - indicator.getHeight()) / 2;
		float endX = endFiield.getX()
				+ (endFiield.getWidth() - indicator.getWidth()) / 2;
		float endY = endFiield.getY()
				+ (endFiield.getHeight() - indicator.getHeight()) / 2;

		// In Animation
		PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat(View.ALPHA,
				0, 1);
		PropertyValuesHolder pvhSX = PropertyValuesHolder.ofFloat(View.SCALE_X,
				2f, 1f);
		PropertyValuesHolder pvhSY = PropertyValuesHolder.ofFloat(View.SCALE_Y,
				2f, 1f);
		ObjectAnimator in = ObjectAnimator.ofPropertyValuesHolder(indicator,
				alpha, pvhSX, pvhSY);
		in.setDuration(300);
		in.addListener(new AnimationAdapter() {
			@Override
			public void onAnimationStart(Animator arg0) {
				indicator.setX(startX);
				indicator.setY(startY);
				indicator.setVisibility(View.VISIBLE);
				indicator.bringToFront();
			}
		});

		// Move Animation
		PropertyValuesHolder pvhX = PropertyValuesHolder.ofFloat(View.X,
				startX, endX);
		PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat(View.Y,
				startY, endY);
		pvhSX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.1f, 1f);
		pvhSY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 0.9f, 1f);
		ObjectAnimator move = ObjectAnimator.ofPropertyValuesHolder(indicator,
				pvhX, pvhY, pvhSX, pvhSY);
		move.setDuration(300);
		move.setStartDelay(500);
		move.removeAllListeners();

		// Out Animation
		alpha = PropertyValuesHolder.ofFloat(View.ALPHA, 1, 0);
		pvhSX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.2f);
		pvhSY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.2f);
		ObjectAnimator out = ObjectAnimator.ofPropertyValuesHolder(indicator,
				alpha, pvhSX, pvhSY);
		out.setDuration(300);
		out.setStartDelay(300);
		// out.addListener(indicatorHider);

		AnimatorSet set = new AnimatorSet();
		set.playSequentially(in, move, out);
		return set;
	}

	public FieldView getTouchedField(int tX, int tY) {
		if (isDemoMode) {
			// Only field 1 and 2 are enabled
			for (int i = 0; i < 2; i++) {
				FieldView field = fieldLayout.getField(i, 0);
				field.getHitRect(hitRect);
				if (hitRect.contains((int) tX, (int) tY)) {
					return field;
				}
			}
			return null;
		}
		for (int x = 0; x < fieldLayout.getColumns(); x++) {
			for (int y = 0; y < fieldLayout.getRows(); y++) {
				FieldView field = fieldLayout.getField(x, y);
				field.getHitRect(hitRect);
				if (hitRect.contains((int) tX, (int) tY)) {
					return field;
				}
			}
		}
		return null;
	}

	public void reset() {
		int fieldPos = 1;
		for (DotView dot : dots) {
			FieldView field = fieldLayout.getField(fieldPos);
			dot.moveTo(field);
			// dot.reset();
			if (fieldPos == 1) {
				field.setColor(MarkingGenerator.DEFAULT_COLOR);
				fieldLayout.getField(0).setColor(dot.getColor());
			} else {
				field.setColor(dot.getColor());
			}
			fieldPos++;
		}
		for (; fieldPos < fieldLayout.getChildCount(); fieldPos++) {
			FieldView field = fieldLayout.getField(fieldPos);
			field.setColor(MarkingGenerator.DEFAULT_COLOR);
		}

		enableDemoMode();
		postDelayed(demoRunable, 1000);
	}

}
