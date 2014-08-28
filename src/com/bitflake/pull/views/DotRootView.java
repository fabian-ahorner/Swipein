package com.bitflake.pull.views;

import android.content.Context;
import android.view.View;

public class DotRootView extends View {
	public static final int DOT_SIZE = 96;
	protected final int rootSize;
	protected final float density;

	public DotRootView(Context context) {
		super(context);
		this.density = context.getResources().getDisplayMetrics().density;
		this.rootSize = (int) (density * DOT_SIZE);
	}


	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(rootSize, rootSize);
	}
}
