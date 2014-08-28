package com.bitflake.pull;

import java.util.ArrayList;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.bitflake.pull.fragments.GameFragment;
import com.bitflake.pull.game.GameMarking;
import com.bitflake.pull.views.DotRootView;

public class GamePagerAdapter extends FragmentPagerAdapter {

	private FragmentManager fragmentManager;
	private ArrayList<GameMarking> gameMarkings = new ArrayList<>();

	public GamePagerAdapter(Context context, FragmentManager fm, int maxWidth,
			int maxHeight) {
		super(fm);
		GameMarking[] markings = new GameMarking[] {//
				new GameMarking(R.string.leaderboard_surival_2x2_2, 2, 2,
						0xFFFFFF00, 0xFF00FFFF),//
				new GameMarking(R.string.leaderboard_surival_3x3_3, 3, 3,
						GameMarking.generateDots(0xFFFFFF00, 1, 0xFFFF00FF, 1,
								0xFF00FFFF, 1)),//
				new GameMarking(R.string.leaderboard_surival_2x3_3, 2, 3,
						GameMarking.generateDots(0xFFFFFF00, 1, 0xFFFF00FF, 1,
								0xFF00FFFF, 1)),//
				new GameMarking(R.string.leaderboard_surival_2x4_4, 2, 4,
						GameMarking.generateDots(0xFFFFFF00, 2, 0xFFFF00FF, 1,
								0xFF00FFFF, 1)),//
				new GameMarking(R.string.leaderboard_surival_3x3_5, 3, 3,
						GameMarking.generateDots(0xFFFFFF00, 2, 0xFFFF00FF, 2,
								0xFF00FFFF, 1)),//
				new GameMarking(R.string.leaderboard_surival_3x4_6, 3, 4,
						GameMarking.generateDots(0xFFFFFF00, 2, 0xFFFF00FF, 2,
								0xFF00FFFF, 2)),//
		};
		float density = context.getResources().getDisplayMetrics().density;
		int maxRows = (int) (maxWidth / (density * DotRootView.DOT_SIZE));
		int maxColumns = (int) (maxHeight / (density * DotRootView.DOT_SIZE));
		this.fragmentManager = fm;
		for (int i = 0; i < markings.length; i++) {
			GameMarking marking = markings[i];
			if (marking.rows <= maxRows && marking.columns <= maxColumns)
				gameMarkings.add(marking);
		}
	}

	@Override
	public Fragment getItem(int i) {
		return new GameFragment(gameMarkings.get(i));
	}

	@Override
	public int getCount() {
		return gameMarkings.size();
	}

	public GameFragment getActiveFragment(ViewPager container, int position) {
		String name = makeFragmentName(container.getId(), position);
		return (GameFragment) fragmentManager.findFragmentByTag(name);
	}

	private static String makeFragmentName(int viewId, int index) {
		return "android:switcher:" + viewId + ":" + index;
	}

	public GameMarking getMarking(int pos) {
		return gameMarkings.get(pos);
	}

}
