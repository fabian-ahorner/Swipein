package com.bitflake.pull.game;

public abstract class MarkingGenerator {
	public abstract boolean next();

	public static final int DEFAULT_COLOR = 0xFF555555;

	public static class Dot {
		public final int color;
		public final int initX, initY;

		public Dot(int color, int initX, int initY) {
			super();
			this.color = color;
			this.initX = initX;
			this.initY = initY;
		}
	}

	protected final Dot[] dots;
	protected final int columns;
	protected final int rows;
	protected final int[][] marking;

	public MarkingGenerator(int columns, int rows, Dot[] dots) {
		this.rows = rows;
		this.columns = columns;
		this.dots = dots;
		this.marking = new int[columns][rows];
		clearMarking();
		next();
	}

	protected void clearMarking() {
		for (int x = 0; x < columns; x++) {
			for (int y = 0; y < rows; y++) {
				marking[x][y] = DEFAULT_COLOR;
			}
		}
	}

	public int[][] getMarking() {
		return marking;
	}

	public int getRows() {
		return rows;
	}

	public int getColumns() {
		return columns;
	}

	public Dot[] getDots() {
		return dots;
	}
}
