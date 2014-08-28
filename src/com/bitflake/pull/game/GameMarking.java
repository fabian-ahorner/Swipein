package com.bitflake.pull.game;

import android.os.Bundle;

public class GameMarking {
	private static final String KEY_COLUMNS = "columns";
	private static final String KEY_ROWS = "rows";
	private static final String KEY_DOTS = "dots";
	private static final String KEY_ID = "id";

	public final int rows;
	public final int columns;
	public final int[] dots;
	private int id;

	public GameMarking(int id, int rows, int columns, int... dots) {
		this.rows = rows;
		this.columns = columns;
		this.dots = dots;
		this.id = id;
	}

	public Bundle getBundle() {
		Bundle b = new Bundle();
		b.putInt(KEY_ID, id);
		b.putInt(KEY_COLUMNS, columns);
		b.putInt(KEY_ROWS, rows);
		b.putIntArray(KEY_DOTS, dots);
		return b;
	}

	public static GameMarking fromBundle(Bundle b) {
		return new GameMarking(b.getInt(KEY_ID), b.getInt(KEY_ROWS),
				b.getInt(KEY_COLUMNS), b.getIntArray(KEY_DOTS));
	}

	public static int[] generateDots(int... colors) {
		assert colors.length % 2 == 0;
		int cnt = 0;
		for (int i = 1; i < colors.length; i += 2) {
			cnt += colors[i];
		}
		int[] dots = new int[cnt];
		cnt = 0;
		for (int i = 0; i < colors.length; i += 2) {
			for (int j = 0; j < colors[i + 1]; j++) {
				dots[cnt++] = colors[i];
			}
		}
		return dots;
	}

	public int getId() {
		return id;
	}
}
