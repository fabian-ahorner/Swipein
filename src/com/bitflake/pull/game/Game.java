package com.bitflake.pull.game;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.bitflake.managers.LOG;

public class Game {
	public static String getKey(GameMarking marking, int mode, int time) {
		ConcurrentMap<Integer, AtomicInteger> map = new ConcurrentHashMap<Integer, AtomicInteger>();
		for (int i = 0; i < marking.dots.length; i++) {
			map.putIfAbsent(marking.dots[i], new AtomicInteger(0));
			map.get(marking.dots[i]).incrementAndGet();
		}

		Entry<Integer, AtomicInteger>[] aC = map.entrySet().toArray(
				new Entry[map.size()]);

		if (aC.length > 1)
			Arrays.sort(aC, new Comparator<Entry<Integer, AtomicInteger>>() {
				@Override
				public int compare(Entry<Integer, AtomicInteger> lhs,
						Entry<Integer, AtomicInteger> rhs) {
					return lhs.getValue().get() - rhs.getValue().get();
				}
			});

		String sC = "";
		for (int i = 0; i < aC.length; i++) {
			sC += " " + Integer.toHexString(aC[i].getKey()) + "x"
					+ aC[i].getValue().get();
		}
		String key = mode + " " + marking.rows + "x" + marking.columns + " "
				+ time + "ms" + sC;
		LOG.d(Game.class, "GameKey: " + key);
		return key;
	}
}
