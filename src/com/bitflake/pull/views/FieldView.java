package com.bitflake.pull.views;

import java.util.HashSet;
import java.util.Set;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import com.bitflake.managers.utils.AnimationAdapter;
import com.bitflake.pull.R;
import com.bitflake.pull.game.MarkingGenerator;

public class FieldView extends DotRootView {
	protected static final int DOT_ALPHA = 0x44;
	private int color = MarkingGenerator.DEFAULT_COLOR;
	private Paint fieldPaint;
	private RectF circle = new RectF();
	private RectF shadowBounds = new RectF();
	private float size;
	private DotView currentDot;
	private ObjectAnimator animDotColor;
	private float animDotColorProgress = 1;
	private Paint dotColorPaint;
	private AnimatorListener animDotColorListener = new AnimationAdapter() {
		@Override
		public void onAnimationEnd(Animator arg0) {
			super.onAnimationEnd(arg0);
			if (currentDot != null) {
				dotColorPaint.setColor(currentDot.getColor());
				dotColorPaint.setAlpha(DOT_ALPHA);
				animDotColor.start();
			}
		}
	};
	private Paint backgroundPaint;
	private Bitmap shadow;
	private int posX;
	private int posY;
	private Set<DotView> accepdedDots = new HashSet<>();
	private int oldColor;
	private ObjectAnimator colorAnimation;
	private float colorProgress = 1;
	private float ringStroke;

	public FieldView(Context context) {
		super(context);
		shadow = BitmapFactory.decodeResource(getResources(),
				R.drawable.field_shadow);
		fieldPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		fieldPaint.setStyle(Style.STROKE);
		fieldPaint.setColor(color);
		ringStroke = density * 8;
		fieldPaint.setStrokeWidth(ringStroke);

		backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		backgroundPaint.setStyle(Style.FILL);
		backgroundPaint
				.setColor(context.getResources().getColor(R.color.inset));

		dotColorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		dotColorPaint.setStyle(Style.FILL);

		animDotColor = ObjectAnimator.ofFloat(this, "animDotColor", 1, 0f);
		animDotColor.setInterpolator(new AccelerateInterpolator());
		animDotColor.setDuration(200);
		this.size = rootSize * 0.8f;
		colorAnimation = ObjectAnimator.ofFloat(this, "colorProgress", 0, 1);
		colorAnimation.setDuration(400);
		colorAnimation.setInterpolator(new OvershootInterpolator());
	}

	public void setColorProgress(float colorProgress) {
		this.colorProgress = colorProgress;
		invalidate();
	}

	public float getColorProgress() {
		return colorProgress;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		circle.left = (w - size) / 2;
		circle.right = (w + size) / 2;
		circle.top = (h - size) / 2;
		circle.bottom = (h + size) / 2;
		shadowBounds.left = circle.left + fieldPaint.getStrokeWidth() / 3;
		shadowBounds.right = circle.right - fieldPaint.getStrokeWidth() / 3;
		shadowBounds.top = circle.top + fieldPaint.getStrokeWidth() / 3;
		shadowBounds.bottom = circle.bottom - fieldPaint.getStrokeWidth() / 3;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawOval(circle, backgroundPaint);
		canvas.drawOval(circle, dotColorPaint);
		canvas.drawCircle(getWidth() / 2, getHeight() / 2, size / 2
				* animDotColorProgress, backgroundPaint);
		canvas.drawBitmap(shadow, null, shadowBounds, null);
		canvas.drawOval(circle, fieldPaint);
		// if (colorProgress >= 1) {
		// fieldPaint.setColor(color);
		// canvas.drawOval(circle, fieldPaint);
		// } else {
		// fieldPaint.setColor(oldColor);
		// canvas.drawOval(circle, fieldPaint);
		// fieldPaint.setColor(color);
		// // fieldPaint.setAlpha((int) (0xFF * colorProgress));
		// // float r = size / 2;
		// fieldPaint.setStrokeWidth(ringStroke * colorProgress *
		// colorProgress);
		// // fieldPaint.setStyle(Style.FILL);
		// // canvas.drawCircle(getWidth() / 2, getHeight() / 2, r *
		// colorProgress,
		// // fieldPaint);
		// canvas.drawOval(circle, fieldPaint);
		// // canvas.drawArc(circle, -90, 360 * colorProgress, false,
		// fieldPaint);
		// fieldPaint.setStrokeWidth(ringStroke);
		// // fieldPaint.setAlpha(0xFF);
		// // }
	}

	public void setCurrentDot(DotView dotView) {
		if (currentDot != dotView) {
			if (currentDot == null) {
				dotColorPaint.setColor(dotView.getColor());
				dotColorPaint.setAlpha(DOT_ALPHA);
				animDotColor.removeListener(animDotColorListener);
				animDotColor.start();
			} else {
				animDotColor.addListener(animDotColorListener);
				animDotColor.reverse();
			}
			this.currentDot = dotView;
		}
	}

	public boolean isSatisfied() {
		return currentDot == null || color == currentDot.getColor();
	}

	public boolean hasDot() {
		return currentDot != null;
	}

	public boolean accepts(DotView dot) {
		return accepdedDots.contains(dot);
	}

	public DotView getDot() {
		return currentDot;
	}

	public float getAnimDotColor() {
		return animDotColorProgress;
	}

	public void setAnimDotColor(float progress) {
		animDotColorProgress = progress;
		invalidate();
	}

	public void setColor(int newColor) {
		if (this.color != newColor) {
			this.oldColor = this.color;
			this.color = newColor;
			// colorAnimation.start();
			animate().rotationX(90).setDuration(200)
					.setInterpolator(new AccelerateInterpolator())
					.setListener(new AnimationAdapter() {
						@Override
						public void onAnimationEnd(Animator arg0) {
							super.onAnimationEnd(arg0);
							setRotationX(-90);
							animate()
									.rotationX(0)
									.setInterpolator(
											new OvershootInterpolator())
									.setListener(null);
							fieldPaint.setColor(color);
							invalidate();
						}
					});
		}
	}

	public void setPosition(int x, int y) {
		this.posX = x;
		this.posY = y;
	}

	public boolean addAcceptedDot(DotView dot) {
		if (currentDot == null || currentDot == dot) {
			return accepdedDots.add(dot);
		} else {
			return false;
		}
	}

	public boolean removeAcceptedDot(DotView dot) {
		return accepdedDots.remove(dot);
	}

	public int getFieldPosX() {
		return posX;
	}

	public int getFieldPosY() {
		return posY;
	}
}
