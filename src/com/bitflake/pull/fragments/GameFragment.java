package com.bitflake.pull.fragments;

import android.animation.Animator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

import com.bitflake.managers.utils.AnimationAdapter;
import com.bitflake.managers.utils.ImmersiveHelper;
import com.bitflake.pull.R;
import com.bitflake.pull.activitys.MainActivity;
import com.bitflake.pull.activitys.ScoreActivity;
import com.bitflake.pull.game.GameMarking;
import com.bitflake.pull.game.RandomMarkingGenerator;
import com.bitflake.pull.views.GameField;
import com.bitflake.pull.views.GameField.SatisfactionListener;

public class GameFragment extends Fragment implements SatisfactionListener {
	public static final int GAME_MODE_SURIVAL = 0;
	public static final int GAME_MODE_TIMER = 1;
	public static final int GAME_MODE_FREE = 2;

	private static final float TIME_PANALTY = 0.98f;

	public static final String START_TIME = "time";
	public static final String START_MODE = "mode";
	public static final String START_GAME_MARKING = "game";

	private GameField gameField;
	private int satisfactionCount = 0;
	private int initTime = 15000;
	private int time;
	private String newValue;
	private long totalTime;
	private long needetTime;

	private View progressView;
	private int gameMode = GAME_MODE_SURIVAL;
	private GameMarking gameMarking;
	private long lastSatisfactionTime;
	private boolean isGameRunning;
	private MainActivity activity;
	private long progressElapsedTime;
	private float progressPausedY;
	private boolean isPaused;

	public GameFragment() {
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.activity = (MainActivity) activity;
	}

	public GameFragment(GameMarking gameMarking) {
		setArguments(gameMarking.getBundle());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.activity_game, container, false);

		progressView = root.findViewById(R.id.game_progress);
		gameField = (GameField) root.findViewById(R.id.game_field);

		gameMarking = GameMarking.fromBundle(getArguments());

		gameField.init(new RandomMarkingGenerator(gameMarking));
		gameField.setSatisfactionListener(this);

		return root;
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	public void pause() {
		progressView.removeCallbacks(endGame);
		progressElapsedTime = System.nanoTime() - progressStartTime;
		progressPausedY = progressView.getY();
		progressView.animate().cancel();
		isPaused = true;
	}

	public void resume() {
		if (isGameRunning) {
			isPaused = false;
			progressView.setY(progressPausedY);
			startProgress(time - progressElapsedTime / 1000000);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onSatisfaction() {
		if (!isDead) {
			progressElapsedTime = 0;
			if (satisfactionCount == 0) {
				activity.startGame();
				isGameRunning = true;
				isPaused = false;
			}
			satisfactionCount++;
			activity.setValue(String.valueOf(satisfactionCount));
			if (gameMode == GAME_MODE_SURIVAL) {
				if (satisfactionCount > 1) {
					totalTime += time;
					needetTime += (System.nanoTime() - lastSatisfactionTime) / 1000_000;
				}
				lastSatisfactionTime = System.nanoTime();
				this.time *= TIME_PANALTY;
				restartProgress();
			} else if (gameMode == GAME_MODE_TIMER && satisfactionCount == 1) {
				startProgress(time);
			}
		}
	}

	public void restartProgress() {
		progressView.removeCallbacks(endGame);
		progressView.animate().translationY(0)
				.setListener(restartProgressListener).setDuration(200);
	}

	private AnimationAdapter restartProgressListener = new AnimationAdapter() {
		@Override
		public void onAnimationEnd(Animator arg0) {
			super.onAnimationEnd(arg0);
			startProgress(time);
		}
	};

	protected boolean isDead;
	private long progressStartTime;

	public void startProgress(long time) {
		progressView
				.animate()
				.setDuration(time)
				.translationY(progressView.getHeight())
				.setListener(null)
				.setInterpolator(
						gameMode == GAME_MODE_TIMER ? new LinearInterpolator()
								: null);
		progressView.postDelayed(endGame, time);
		progressStartTime = System.nanoTime() - progressElapsedTime;
	}

	private Runnable endGame = new Runnable() {
		@Override
		public void run() {
			isDead = true;
			isGameRunning = false;
			Intent intent = new Intent(getActivity(), ScoreActivity.class);
			intent.putExtra(ScoreActivity.START_VALUE,
					(int) ((satisfactionCount * 1000) + (1 - needetTime
							/ (float) totalTime) * 1000));
			intent.putExtra(ScoreActivity.START_MARKING, getArguments());
			activity.finishGame();
			startActivityForResult(intent, 0);
			activity.showAd();
		}
	};

	public void reset() {
		progressView.removeCallbacks(endGame);
		isGameRunning = false;
		isDead = false;
		gameField.reset();
		satisfactionCount = 0;
		progressView.animate().translationY(0).setListener(null)
				.setDuration(200);
		time = initTime;
		needetTime = 0;
		totalTime = 0;
	}

	public void setGameMode(int gameMode, int time) {
		this.gameMode = gameMode;
		this.initTime = time;
		this.time = initTime;
	}

	public void setEnabled(boolean enabled) {
		gameField.setEnabled(enabled);
		reset();
		if (!enabled) {
			isDead = true;
			progressView.removeCallbacks(endGame);
			progressView.setBackgroundResource(R.color.progress);
		}
	}

	public void setScale(float positionOffset) {
		gameField.setScaleX(positionOffset);
		gameField.setScaleY(positionOffset);
	}

	public GameMarking getMarking() {
		return gameMarking;
	}

	public boolean isGameRunning() {
		return isGameRunning;
	}

	public boolean isPaused() {
		return isPaused;
	}
}
