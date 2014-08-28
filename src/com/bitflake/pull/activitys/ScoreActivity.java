package com.bitflake.pull.activitys;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.TextView;

import com.bitflake.managers.FontManager;
import com.bitflake.managers.settings.IntegerObservable;
import com.bitflake.pull.ApplicationMain;
import com.bitflake.pull.ApplicationMain.TrackerName;
import com.bitflake.pull.R;
import com.bitflake.pull.game.GameMarking;
import com.bitflake.pull.gplay.BaseGameActivity;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.games.Games;

public class ScoreActivity extends BaseGameActivity implements OnClickListener {
	public static final String START_VALUE = "value";
	public static final String START_MARKING = "marking";
	private int value;
	private String leaderboardID;
	private boolean showLeaderboard;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_score);
		View cValue = (View) findViewById(R.id.score_value_container);
		TextView tValue = (TextView) findViewById(R.id.score_value);
		TextView tValue2 = (TextView) findViewById(R.id.score_value2);
		TextView tOldValue = (TextView) findViewById(R.id.score_old);

		Button bRestart = (Button) findViewById(R.id.score_restart);
		Button bLeaderboard = (Button) findViewById(R.id.score_leaderboard);

		FontManager fm = FontManager.getInstance();
		fm.applyDefaultFont(tValue);
		fm.applyDefaultFont(tOldValue);
		fm.applyDefaultFont(bRestart);
		fm.applyDefaultFont(bLeaderboard);

		bRestart.setOnClickListener(this);
		bLeaderboard.setOnClickListener(this);

		Intent intent = getIntent();
		value = intent.getIntExtra(START_VALUE, 0);
		tValue.setText(String.valueOf(value / 1000));
		tValue2.setText("." + String.format("%03d", value % 1000));

		cValue.setScaleX(0);
		cValue.setScaleY(0);
		cValue.animate().setStartDelay(500)
				.setInterpolator(new OvershootInterpolator()).scaleX(1)
				.scaleY(1).setDuration(500);
		this.setResult(RESULT_CANCELED);

		View root = tValue.getRootView();
		root.setAlpha(0);
		root.animate().alpha(1);

		GameMarking marking = GameMarking.fromBundle(intent
				.getBundleExtra(START_MARKING));
		leaderboardID = getString(marking.getId());

		if (leaderboardID == null) {
			bLeaderboard.setVisibility(View.GONE);
		}

		IntegerObservable record = new IntegerObservable(leaderboardID, 0);
		int recordValue = record.get();
		if (recordValue > 0) {
			tOldValue.setText(getString(
					R.string.old_score,
					String.valueOf(record.get() / 1000) + "."
							+ String.format("%03d", record.get() % 1000)));
		} else {
			tOldValue.setVisibility(View.GONE);
		}

		if (record.get() < value) {
			record.set(value);
		}

		Tracker t = ApplicationMain.getTracker(TrackerName.APP_TRACKER);

		// Set screen name.
		// Where path is a String representing the screen name.
		t.setScreenName("/Game/Score");

		// Send a screen view.
		t.send(new HitBuilders.AppViewBuilder().build());
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.score_restart:
			setResult(RESULT_OK);
			finish();
			return;
		case R.id.score_leaderboard:
			showLeaderboard = true;
			if (isSignedIn()) {
				startActivityForResult(Games.Leaderboards.getLeaderboardIntent(
						getApiClient(), leaderboardID), 0);
			} else {
				beginUserInitiatedSignIn();
			}
			return;
		default:
			break;
		}
	}

	@Override
	public void onSignInFailed() {
		showLeaderboard = false;
	}

	@Override
	public void onSignInSucceeded() {
		if (leaderboardID != null) {
			Games.Leaderboards
					.submitScore(getApiClient(), leaderboardID, value);
			if (showLeaderboard) {
				startActivityForResult(Games.Leaderboards.getLeaderboardIntent(
						getApiClient(), leaderboardID), 0);
			}
		}
	}
}
