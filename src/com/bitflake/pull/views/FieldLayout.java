package com.bitflake.pull.views;

import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.widget.LinearLayout;

import com.bitflake.pull.game.MarkingGenerator;

public class FieldLayout extends LinearLayout {

	private FieldView[][] fields = new FieldView[0][0];
	private MarkingGenerator markingGenerator;
	private Set<FieldView> border = new HashSet<>();
	private FieldView borderSource;

	public FieldLayout(Context context, MarkingGenerator markingGenerator) {
		super(context);
		this.markingGenerator = markingGenerator;
		removeAllViews();
		fields = new FieldView[markingGenerator.getColumns()][markingGenerator
				.getRows()];
		for (int x = 0; x < fields.length; x++) {
			for (int y = 0; y < fields[x].length; y++) {
				fields[x][y] = new FieldView(getContext());
				fields[x][y].setPosition(x, y);
				addView(fields[x][y]);
			}
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (getChildCount() > 0) {
			int fW = getChildAt(0).getMeasuredWidth();
			int fH = getChildAt(0).getMeasuredHeight();
			int startX = (l + r - fW * fields.length) / 2;
			int startY = (t + b - fH * fields[0].length) / 2;

			for (int x = 0; x < fields.length; x++) {
				int fX = startX + fW * x + fW / 2;
				for (int y = 0; y < fields[x].length; y++) {
					int fY = startY + fW * y + fH / 2;
					FieldView field = fields[x][y];
					field.layout(fX - fW / 2, fY - fH / 2, fX + fW / 2, fY + fH
							/ 2);
				}
			}
		}
	}

	public void setField(MarkingGenerator markingGenerator) {

	}

	public void nextMarking() {
		markingGenerator.next();
		applyMarking(markingGenerator.getMarking());
	}

	public void applyMarking(int[][] marking) {
		for (int x = 0; x < fields.length; x++) {
			for (int y = 0; y < fields[x].length; y++) {
				fields[x][y].setColor(marking[x][y]);
			}
		}
	}

	public FieldView getField(int x, int y) {
		return fields[x][y];
	}

	public int getRows() {
		return fields[0].length;
	}

	public int getColumns() {
		return fields.length;
	}

	public boolean isSatisfied() {
		for (int x = 0; x < fields.length; x++) {
			for (int y = 0; y < fields[x].length; y++) {
				if (!fields[x][y].isSatisfied()) {
					return false;
				}
			}
		}
		return true;
	}

	public void registerDot(FieldView field, DotView dot) {
		if (field.addAcceptedDot(dot)) {
			int x = field.getFieldPosX();
			int y = field.getFieldPosY();
			if (x > 0) {
				registerDot(fields[x - 1][y], dot);
			}
			if (x < fields.length - 1) {
				registerDot(fields[x + 1][y], dot);
			}
			if (y > 0) {
				registerDot(fields[x][y - 1], dot);
			}
			if (y < fields[x].length - 1) {
				registerDot(fields[x][y + 1], dot);
			}
		} else if (field.hasDot() && field.getDot() != dot) {
			border.add(field);
		}
	}

	public void showAcceptingFields(DotView dot) {
		for (int x = 0; x < fields.length; x++) {
			for (int y = 0; y < fields[x].length; y++) {
				if (!fields[x][y].hasDot() && !fields[x][y].accepts(dot)) {
					fields[x][y].animate().scaleX(0.8f).scaleY(0.8f);
				}
			}
		}
	}

	public void hideAcceptingFields(DotView dot) {
		for (int x = 0; x < fields.length; x++) {
			for (int y = 0; y < fields[x].length; y++) {
				if (!fields[x][y].hasDot() && !fields[x][y].accepts(dot)) {
					fields[x][y].animate().scaleX(1).scaleY(1);
				}
			}
		}
	}

	public void unregisterDot(FieldView field, DotView dot) {
		if (field.removeAcceptedDot(dot)) {
			int x = field.getFieldPosX();
			int y = field.getFieldPosY();
			if (x > 0) {
				unregisterDot(fields[x - 1][y], dot);
			}
			if (x < fields.length - 1) {
				unregisterDot(fields[x + 1][y], dot);
			}
			if (y > 0) {
				unregisterDot(fields[x][y - 1], dot);
			}
			if (y < fields[x].length - 1) {
				unregisterDot(fields[x][y + 1], dot);
			}
		}
		border.clear();
	}

	public FieldView getField(int fieldPos) {
		return getField(fieldPos % getColumns(), fieldPos / getColumns());
	}

	public void showBorder(FieldView fieldView) {
//		if (borderSource == fieldView)
//			return;
//		borderSource = fieldView;
//
//		for (FieldView field : border) {
//			field.animate().cancel();
//			field.setScaleX(1);
//			field.setScaleY(1);
//			field.animate().scaleX(1.1f).scaleY(1.1f)
//					.setInterpolator(new ShakeInterpolator(2)).setDuration(500);
//		}
	}

	public void hideBorder() {
//		if (borderSource == null)
//			return;
//		borderSource = null;
//		for (FieldView field : border) {
//			field.animate().scaleX(1).scaleY(1).setInterpolator(null);
//		}
	}
}
