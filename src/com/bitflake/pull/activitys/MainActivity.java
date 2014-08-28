package com.bitflake.pull.activitys;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bitflake.managers.FontManager;
import com.bitflake.managers.LOG;
import com.bitflake.managers.settings.IntegerObservable;
import com.bitflake.managers.utils.AnimationAdapter;
import com.bitflake.managers.utils.AnimationHider;
import com.bitflake.managers.utils.ImmersiveHelper;
import com.bitflake.managers.utils.OrientationHelper;
import com.bitflake.pull.ApplicationMain;
import com.bitflake.pull.ApplicationMain.TrackerName;
import com.bitflake.pull.GamePagerAdapter;
import com.bitflake.pull.R;
import com.bitflake.pull.fragments.GameFragment;
import com.bitflake.pull.game.GameMarking;
import com.bitflake.pull.gplay.BaseGameActivity;
import com.bitflake.pull.views.LockableViewPager;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.games.Games;
import com.viewpagerindicator.CirclePageIndicator;

public class MainActivity extends BaseGameActivity implements OnClickListener,
		OnPageChangeListener {
	private static final int AD_FREQUENZY = 3;
	private LockableViewPager pager;
	private View controlls;
	private GamePagerAdapter adapter;
	private boolean isInGameMode = false;
	private AnimationHider controllViewHider;
	private View bPrev;
	private View bNext;
	private TextView statusView1;
	private String newValue1;
	private String newValue2;
	private String currentLeaderboardId;
	private TextView statusView2;
	private View statusContainer;
	private InterstitialAd interstitial;
	private String MY_AD_UNIT_ID = "ca-app-pub-5524191645709408/8184388089";

	private IntegerObservable adCount = new IntegerObservable("adCount", 0);
	private boolean showLeaderboard;
	private String easterEggLink = "https://www.google.at/webhp?sourceid=chrome-instant&ion=1&espv=2&ie=UTF-8#q=the%20answer%20to%20life%20the%20universe%20and%20everything";
	private ImageButton bPause;
	private AnimationHider pauseButtonViewHider;
	private View containerPause;
	private View tPaused;
	private AnimationHider pauseContainerViewHider;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// set requested clients (games and cloud save)
		setRequestedClients(BaseGameActivity.CLIENT_GAMES
				| BaseGameActivity.CLIENT_APPSTATE);

		// enable debug log, if applicable
		enableDebugLog(true);

		// call BaseGameActivity's onCreate()
		super.onCreate(savedInstanceState);

		getWindow().getDecorView().setBackgroundResource(R.color.background);
		OrientationHelper.lockDefaultOrientation(this);

		setContentView(R.layout.activity_main);
		pager = (LockableViewPager) findViewById(R.id.main_pager);
		controlls = findViewById(R.id.main_controlls);
		controlls.setPadding(0, 0, 0,
				ImmersiveHelper.getNavigationBarHeight(this));

		statusView1 = (TextView) findViewById(R.id.main_status1);
		statusView2 = (TextView) findViewById(R.id.main_status2);
		statusContainer = findViewById(R.id.main_status);
		FontManager.getInstance().applyDefaultFont(statusContainer);

		findViewById(R.id.main_leaderboard).setOnClickListener(this);
		bPrev = findViewById(R.id.main_prev);
		bPrev.setOnClickListener(this);
		bPause = (ImageButton) findViewById(R.id.main_pause);
		bPause.setOnClickListener(this);
		bNext = findViewById(R.id.main_next);
		bNext.setOnClickListener(this);
		FontManager.getInstance().applyDefaultFont(controlls);

		containerPause = findViewById(R.id.main_pause_container);
		tPaused = findViewById(R.id.main_paused);
		FontManager.getInstance().applyDefaultFont(tPaused);

		controllViewHider = new AnimationHider(View.INVISIBLE, controlls);
		pauseButtonViewHider = new AnimationHider(View.INVISIBLE, bPause);
		pauseContainerViewHider = new AnimationHider(View.INVISIBLE,
				containerPause, tPaused);

		pager.getViewTreeObserver().addOnPreDrawListener(
				new OnPreDrawListener() {

					@Override
					public boolean onPreDraw() {
						pager.getViewTreeObserver().removeOnPreDrawListener(
								this);
						adapter = new GamePagerAdapter(getApplicationContext(),
								getSupportFragmentManager(), pager.getWidth(),
								pager.getHeight());
						pager.setAdapter(adapter);
						CirclePageIndicator circleIndicator = ((CirclePageIndicator) findViewById(R.id.main_indicator));
						circleIndicator.setViewPager(pager);
						circleIndicator
								.setOnPageChangeListener(MainActivity.this);
						onPageSelected(0);
						stopGame();
						return false;
					}
				});

		pager.setOnPageChangeListener(this);

		interstitial = new InterstitialAd(this);
		interstitial.setAdUnitId(MY_AD_UNIT_ID);

		// Create ad request.
		AdRequest adRequest = new AdRequest.Builder().build();

		// Begin loading your interstitial.
		interstitial.loadAd(adRequest);
	}

	@Override
	protected void onStart() {
		super.onStart();
		// mClient.connect();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.main_status:
			Intent browserIntent = new Intent(Intent.ACTION_VIEW,
					Uri.parse(easterEggLink));
			startActivity(browserIntent);
			break;
		case R.id.main_pause:
			if (isInGameMode) {
				GameFragment gameFragment = adapter.getActiveFragment(pager,
						pager.getCurrentItem());
				if (gameFragment.isPaused()) {
					this.resumeGame();
				} else {
					this.pauseGame();
				}
			}
			break;
		case R.id.main_prev:
			if (pager.getCurrentItem() > 0)
				pager.setCurrentItem(pager.getCurrentItem() - 1);
			break;
		case R.id.main_next:
			if (pager.getCurrentItem() < adapter.getCount() - 1)
				pager.setCurrentItem(pager.getCurrentItem() + 1);
			break;
		case R.id.main_leaderboard:
			if (isSignedIn()) {
				startActivityForResult(Games.Leaderboards.getLeaderboardIntent(
						getApiClient(), currentLeaderboardId), 0);
			} else {
				beginUserInitiatedSignIn();
				showLeaderboard = true;
			}
			break;
		}
	}

	private void pauseGame() {
		GameFragment gameFragment = adapter.getActiveFragment(pager,
				pager.getCurrentItem());
		gameFragment.pause();
		containerPause.setAlpha(0);
		containerPause.setVisibility(View.VISIBLE);
		containerPause.animate().alpha(1).setListener(null);
		tPaused.setY(-tPaused.getHeight());
		tPaused.setVisibility(View.VISIBLE);

		int immersiveError = ImmersiveHelper.getNavigationBarHeight(this) / 2;
		tPaused.animate().translationY(-immersiveError)
				.setInterpolator(new DecelerateInterpolator());

		bPause.animate().y(tPaused.getBottom() - immersiveError)
				.x((containerPause.getWidth() - bPause.getWidth()) / 2)
				.rotation(360).setInterpolator(new OvershootInterpolator());
		bPause.postDelayed(new Runnable() {
			@Override
			public void run() {
				bPause.setImageResource(R.drawable.ic_action_play);
			}
		}, 100);
		ImmersiveHelper.showSystemUI(this);
	}

	private void resumeGame() {
		GameFragment gameFragment = adapter.getActiveFragment(pager,
				pager.getCurrentItem());
		gameFragment.resume();
		ImmersiveHelper.startImersiveSticky(this);
		// showPauseButton();
		containerPause.animate().alpha(0).setListener(pauseContainerViewHider);
		tPaused.animate().y(-tPaused.getHeight())
				.setInterpolator(new AccelerateInterpolator());
		bPause.animate().translationX(0).translationY(0).rotation(0)
				.setInterpolator(new OvershootInterpolator());
		bPause.postDelayed(new Runnable() {
			@Override
			public void run() {
				bPause.setImageResource(R.drawable.ic_action_pause);
			}
		}, 100);
	}

	private void showPauseButton() {
		bPause.setImageResource(R.drawable.ic_action_pause);
		rotateIn(bPause);
	}

	private void rotateIn(View view) {
		view.setScaleX(0);
		view.setScaleY(0);
		view.setRotation(360);
		view.setVisibility(View.VISIBLE);
		view.animate().rotation(0).scaleX(1).scaleY(1)
				.setInterpolator(new OvershootInterpolator()).setListener(null);
	}

	private void rotateOut(View view, AnimatorListener listener) {
		view.animate().rotation(360).scaleX(0).scaleY(0)
				.setInterpolator(new AnticipateInterpolator())
				.setListener(listener);
	}

	private void hidePauseButton() {
		rotateOut(bPause, pauseButtonViewHider);
	}

	public void startGame() {
		GameFragment gameFragment = adapter.getActiveFragment(pager,
				pager.getCurrentItem());
		ImmersiveHelper.startImersiveSticky(this);
		// gameFragment.setGameMode(GameFragment.GAME_MODE_SURIVAL, 10 * 1000);
		controlls.animate().y(pager.getHeight()).setListener(controllViewHider);
		isInGameMode = true;
		pager.setPagingEnabled(false);

		GameMarking marking = gameFragment.getMarking();
		String gameName = marking.columns + "x" + marking.rows + " ("
				+ marking.dots.length + ")";
		Tracker t = ApplicationMain.getTracker(TrackerName.APP_TRACKER);
		t.setScreenName("/Game/" + gameName);
		t.send(new HitBuilders.AppViewBuilder().build());
		showPauseButton();
	}

	private void stopGame() {
		if (adapter == null)
			return;
		isInGameMode = false;
		GameFragment gameFragment = adapter.getActiveFragment(pager,
				pager.getCurrentItem());
		if (gameFragment != null)
			gameFragment.reset();
		controlls.setVisibility(View.VISIBLE);
		controlls.animate().translationY(0).setListener(null);
		pager.setPagingEnabled(true);
		onPageSelected(pager.getCurrentItem());
		ImmersiveHelper.showSystemUI(this);
		hidePauseButton();
	}

	@Override
	public void onBackPressed() {
		if (!isInGameMode) {
			super.onBackPressed();
		} else {
			stopGame();
			containerPause.animate().alpha(0)
					.setListener(pauseContainerViewHider);
			tPaused.animate().y(-tPaused.getHeight())
					.setInterpolator(new AccelerateInterpolator());
			bPause.animate().translationX(0).translationY(0).rotation(0);
			rotateOut(bPause, pauseButtonViewHider);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (isInGameMode)
			pauseGame();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		stopGame();
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
	}

	@Override
	public void onPageScrolled(int position, float positionOffset,
			int positionOffsetPixels) {
	}

	@Override
	public void onPageSelected(int page) {
		if (page == 0) {
			bPrev.animate().x(-bPrev.getWidth());
		} else if (page == adapter.getCount() - 1) {
			bNext.animate().x(pager.getWidth() + bNext.getWidth());
		} else {
			bPrev.animate().translationX(0);
			bNext.animate().translationX(0);
		}
		currentLeaderboardId = getString(adapter.getMarking(
				pager.getCurrentItem()).getId());
		IntegerObservable record = new IntegerObservable(currentLeaderboardId,
				0);
		GameFragment gameFragment = adapter.getActiveFragment(pager,
				pager.getCurrentItem());
		if (gameFragment != null)
			gameFragment.reset();
		setValue(record.get());
	}

	private void setValue(Integer value) {
		if (value == null || value == 0)
			setValue("");
		else {
			setValue(String.valueOf(value / 1000),
					"." + String.format("%03d", value % 1000));
		}
	}

	public void setValue(String newValue1, String newValue2) {
		this.newValue1 = newValue1;
		this.newValue2 = newValue2;
		statusContainer.animate().scaleX(0).scaleY(0).setStartDelay(0)
				.setDuration(300).setInterpolator(new AnticipateInterpolator())
				.setListener(valueChangeListener);
		if (newValue2 == null && "42".equals(newValue1)) {
			statusContainer.setOnClickListener(this);
			statusContainer.setClickable(true);
		} else {
			statusContainer.setOnClickListener(null);
			statusContainer.setClickable(false);
		}
	};

	public void setValue(String newValue1) {
		setValue(newValue1, null);
	};

	private AnimationAdapter valueChangeListener = new AnimationAdapter() {
		@Override
		public void onAnimationEnd(Animator arg0) {
			super.onAnimationEnd(arg0);
			if (newValue1 == null || newValue1.equals("")) {
				statusView1.setVisibility(View.GONE);
			} else {
				statusView1.setVisibility(View.VISIBLE);
				statusView1.setText(newValue1);
			}
			if (newValue2 == null || newValue2.equals("")) {
				statusView2.setVisibility(View.GONE);
			} else {
				statusView2.setVisibility(View.VISIBLE);
				statusView2.setText(newValue2);
			}
			statusContainer.animate().setListener(null).scaleX(1).scaleY(1)
					.setDuration(400)
					.setInterpolator(new OvershootInterpolator());
		}
	};

	@Override
	public void onSignInFailed() {
		LOG.d(this, "Fail");
		showLeaderboard = false;
		// getGameHelper().beginUserInitiatedSignIn();
	}

	@Override
	public void onSignInSucceeded() {
		if (showLeaderboard) {
			startActivityForResult(Games.Leaderboards.getLeaderboardIntent(
					getApiClient(), currentLeaderboardId), 0);
			showLeaderboard = false;
		}
	}

	public void showAd() {
		if (interstitial.isLoaded()) {
			if ((adCount.get() + 1) % AD_FREQUENZY == 0)
				interstitial.show();
			adCount.increment();
		}
	}

	public void finishGame() {
		isInGameMode = false;
	}

}
