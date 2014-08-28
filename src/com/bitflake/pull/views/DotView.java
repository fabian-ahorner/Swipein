package com.bitflake.pull.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;

import com.bitflake.pull.R;

public class DotView extends DotRootView {
	private int color = 0xFFFFFFFF;
	private Paint dotColorPaint;
	private RectF circle = new RectF();
	private float size;
	private FieldView currentField;
	private float touchDownX;
	private float touchDownY;
	private Paint borderPaint;
	private Bitmap glossy;

	public DotView(Context context, int color) {
		super(context);
		this.color = color;
		glossy = BitmapFactory.decodeResource(getResources(),
				R.drawable.dot_glossy);

		dotColorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		dotColorPaint.setStyle(Style.FILL);
		dotColorPaint.setColor(color);
		dotColorPaint.setAlpha(0xFF);

		borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		borderPaint.setStyle(Style.STROKE);
		borderPaint.setColor(0xFFAAAAAA);
		borderPaint.setStrokeWidth(2);
		borderPaint.setAlpha(0xFF);
		this.size = rootSize * 0.6f;

	}

	public void setField(FieldView field) {
		if (currentField != null && currentField != field
				&& currentField.getDot() == this) {
			currentField.setCurrentDot(null);
		}
		this.currentField = field;
		field.setCurrentDot(this);
	}

	public void moveTo(FieldView field) {
		if (field != currentField) {
			setField(field);
		}
		float x = field.getX() - (field.getWidth() - getWidth()) / 2;
		float y = field.getY() - (field.getHeight() - getHeight()) / 2;
		if (getX() == 0 && getY() == 0) {
			setX(x);
			setY(y);
		} else {
			animate().x(x).y(y);
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		circle.left = (w - size) / 2;
		circle.right = (w + size) / 2;
		circle.top = (h - size) / 2;
		circle.bottom = (h + size) / 2;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawOval(circle, dotColorPaint);
		canvas.drawBitmap(glossy, null, circle, null);
		canvas.drawOval(circle, borderPaint);
	}

	public void onTouchDown(float x, float y) {
		this.touchDownX = x;
		this.touchDownY = y;
		animate().scaleX(1.2f).scaleY(1.2f)
				.setInterpolator(new OvershootInterpolator());
	}

	public void onTouchUp() {
		this.touchDownX = 0;
		this.touchDownY = 0;
		animate().scaleX(1f).scaleY(1f)
				.setInterpolator(new LinearInterpolator());
	}

	public void moveBack() {
		moveTo(currentField);
	}

	public void reset() {
		float x = currentField.getX() - (currentField.getWidth() - getWidth())
				/ 2;
		float y = currentField.getY()
				- (currentField.getHeight() - getHeight()) / 2;
		setX(x);
		setY(y);
	}

	public int getColor() {
		return color;
	}

	public FieldView getField() {
		return currentField;
	}

	public float getCenterX() {
		return getX();
	}
}
