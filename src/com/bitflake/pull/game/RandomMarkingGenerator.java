package com.bitflake.pull.game;

public class RandomMarkingGenerator extends MarkingGenerator {

	public RandomMarkingGenerator(int columns, int rows, int... colors) {
		super(columns, rows, generateDots(columns, rows, colors));
	}

	public RandomMarkingGenerator(GameMarking gamMarking) {
		this(gamMarking.rows, gamMarking.columns, gamMarking.dots);
	}

	private static Dot[] generateDots(int columns, int rows, int[] colors) {
		Dot[] dots = new Dot[colors.length];
		for (int i = 0; i < colors.length; i++) {
			dots[i] = new Dot(colors[i], i % columns, i / columns);
		}
		return dots;
	}

	@Override
	public boolean next() {
		clearMarking();
		for (int i = 0; i < dots.length; i++) {
			int x, y;
			do {
				x = (int) (Math.random() * columns);
				y = (int) (Math.random() * rows);
			} while (marking[x][y] != DEFAULT_COLOR);
			marking[x][y] = dots[i].color;
		}
		return true;
	}
}
