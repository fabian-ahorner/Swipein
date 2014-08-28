package com.bitflake.pull.views;

import java.util.ArrayList;
import java.util.List;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import com.bitflake.pull.R;

public class ConnectionGridView extends View {

	private static final float MERGE_BOUNDARY = 0.8f;
	private float connectionProgress;
	private Path path1 = new Path();
	private Paint paint;
	private PointF pSrc = new PointF();
	private PointF p2 = new PointF();
	private PointF p3 = new PointF();
	private PointF pDst = new PointF();
	private FieldLayout fieldLayout;
	private int connectionSize;
	private ObjectAnimator showAnimation;
	private Canvas bitmapCanvas;
	private Bitmap bitmap;
	private DotView currentDot;
	private List<Point> connections = new ArrayList<>();
	private List<Point> connectionsRotated = new ArrayList<>();

	public ConnectionGridView(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	public ConnectionGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public ConnectionGridView(Context context) {
		super(context);
		init(context);
	}

	public ConnectionGridView(Context context, FieldLayout fieldLayout) {
		this(context);
		this.fieldLayout = fieldLayout;
	}

	private void init(Context context) {
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(context.getResources().getColor(R.color.inset));
		paint.setAlpha(0xFF);
		paint.setStyle(Style.FILL);
		paint.setStrokeWidth(20);

		showAnimation = ObjectAnimator
				.ofFloat(this, "connectionProgress", 0, 1);
		showAnimation.setInterpolator(new OvershootInterpolator());
		showAnimation.setDuration(500);
		setWillNotDraw(false);

		this.connectionSize = (int) (getResources().getDisplayMetrics().density * DotRootView.DOT_SIZE);
		bitmap = Bitmap.createBitmap(connectionSize * 2 / 3,
				connectionSize * 2 / 3, Config.ARGB_8888);
		bitmapCanvas = new Canvas(bitmap);
	}

	public void setConnectionProgress(float progress) {
		this.connectionProgress = progress;
		invalidate();
	}

	public float getConnectionProgress() {
		return connectionProgress;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		generateConncetionImage();

		for (Point p : connections) {
			FieldView field = fieldLayout.getField(p.x, p.y);
			canvas.save();
			canvas.translate(field.getX() + field.getWidth() / 2, field.getY()
					+ field.getHeight() / 2);
			canvas.drawBitmap(bitmap, -bitmap.getWidth() / 2, connectionSize
					/ 2 - bitmap.getHeight() / 2, paint);
			canvas.restore();
		}

		for (Point p : connectionsRotated) {
			FieldView field = fieldLayout.getField(p.x, p.y);
			canvas.save();
			canvas.translate(field.getX() + field.getWidth() / 2, field.getY()
					+ field.getHeight() / 2);
			canvas.rotate(-90);
			canvas.drawBitmap(bitmap, -bitmap.getWidth() / 2, connectionSize
					/ 2 - bitmap.getHeight() / 2, paint);
			canvas.restore();
		}
	}

	private void generateConncetionImage() {
		bitmapCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

		float w = bitmap.getWidth();
		float h = bitmap.getHeight();

		path1.reset();
		path1.moveTo(0, 0);
		float p;

		pSrc.set(0, 0);
		if (connectionProgress < MERGE_BOUNDARY) {
			p = connectionProgress / MERGE_BOUNDARY;
			float pH = h / 2 * p;
			float d1 = w / 2 * p;

			p2.set(d1, h / 4 * p);
			p3.set(d1, pH);
			pDst.set(w / 2, pH);
			path1.cubicTo(p2.x, p2.y, p3.x, p3.y, pDst.x, pDst.y);

			// drawPoints(canvas);

			p2.set(w - d1, pH);
			p3.set(w - d1, h / 4 * p);
			pDst.set(w, 0);
			path1.cubicTo(p2.x, p2.y, p3.x, p3.y, pDst.x, pDst.y);
		} else {
			p = (connectionProgress - MERGE_BOUNDARY) / (1 - MERGE_BOUNDARY);

			float d = p * w * 0.2f;

			p2.set(w / 2 - d, h / 4);
			pDst.set(w / 2 - d, h / 2);
			path1.cubicTo(p2.x, p2.y, pDst.x, pDst.y, pDst.x, pDst.y);
			// drawPoints(canvas);

			path1.lineTo(w / 2 + d, h / 2);

			p2.set(w / 2 + d, h / 4);
			pDst.set(w, 0);
			path1.cubicTo(w / 2 + d, h / 2, p2.x, p2.y, pDst.x, pDst.y);
		}
		bitmapCanvas.drawPath(path1, paint);
		bitmapCanvas.save();
		bitmapCanvas.translate(w, h);
		bitmapCanvas.rotate(180);
		bitmapCanvas.drawPath(path1, paint);
		bitmapCanvas.restore();
	}

	public void showConnections(DotView dot) {
		currentDot = dot;
		showAnimation.start();
		float[] hsv = new float[3];
		Color.colorToHSV(dot.getColor(), hsv);
		hsv[2] *= 0.78f;
		paint.setColor(Color.HSVToColor(hsv));

		connections.clear();
		connectionsRotated.clear();
		for (int x = 0; x < fieldLayout.getColumns(); x++) {
			for (int y = 0; y < fieldLayout.getRows(); y++) {
				FieldView field = fieldLayout.getField(x, y);
				if (field.accepts(currentDot)) {
					if (y < fieldLayout.getRows() - 1
							&& fieldLayout.getField(x, y + 1).accepts(
									currentDot)) {
						connections.add(new Point(x, y));
					}
					if (x < fieldLayout.getColumns() - 1
							&& fieldLayout.getField(x + 1, y).accepts(
									currentDot)) {
						connectionsRotated.add(new Point(x, y));
					}
				}
			}
		}
	}

	public void hideConnections() {
		showAnimation.reverse();
	}
	
	public void hideConnectionsImediate(){
		connections.clear();
		connectionsRotated.clear();
		showAnimation.cancel();
		invalidate();
	}
}
