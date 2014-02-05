package com.newapp.musicplayer.skillsofeffectivepeople;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.newapp.musicplayer.skillsofeffectivepeople.enums.PaymentSongType;
import com.newapp.musicplayer.skillsofeffectivepeople.util.IabHelper;

public class MusicPlayerActivity extends Activity implements
		OnCompletionListener, SeekBar.OnSeekBarChangeListener,
		MediaPlayer.OnBufferingUpdateListener, OnPreparedListener {

	// Delay is in milliseconds
	static final int DRAWER_DELAY = 3000;
	static final String TAG = "skillsofeffectivepeopleAudioPlayer";
	public static final String CURRENT_AUDIO_PODCAST_INDEX = "CURRENT_AUDIO_PODCAST_INDEX";
	private final static int LAUNCHES_UNTIL_RATE = 3;
	private static final String LIVE_YOUR_LIFE_URL = "https://play.google.com/store/apps/details?id=com.newapp.musicplayer.krasnova";
	private static final String SUCCESS_LESSONS_URL = "https://play.google.com/store/apps/details?id=com.newapp.musicplayer.andreev";
	private static final String SUCCESS_MINDSET_URL = "https://play.google.com/store/apps/details?id=com.newapp.musicplayer.zimbitskiy.mindset";
	private static final String KAMASUTRA_URL = "https://play.google.com/store/apps/details?id=com.newapp.musicplayer.gandapas";
	private static final String YOGA_MUSIC_URL = "https://play.google.com/store/apps/details?id=com.newapp.musicplayer.dovlatov";
	private static final int MAX_COUNT_TO_SHOW_MORE_APPS_MENU = 1;
	private ImageButton btnPlay;
	private ImageButton btnForward;
	private ImageButton btnBackward;
	private ImageButton btnNext;
	private ImageButton btnPrevious;
	private SeekBar songProgressBar;
	private TextView songTitleLabel;
	private TextView songCurrentDurationLabel;
	private TextView songTotalDurationLabel;
	// Media Player
	private MediaPlayer mp;
	// Handler to update UI timer, progress bar etc,.
	private Handler mHandler = new Handler();

	private SongsManager songManager;
	private Utilities utils;
	private int seekForwardTime = 5000; // 5000 milliseconds
	private int seekBackwardTime = 5000; // 5000 milliseconds
	private int currentSongIndex = -1;
	private List<SongObject> songsList = new ArrayList<SongObject>();
	private int currentLaunchCount;
	private SharedPreferences preferences;
	private ListView playerList;
	private IabHelper mHelper;
	private TextView loadingText;
	private PurchaseManager purchaseManager;
	private PlayerListAdapter adapter;
	private boolean isPreparing = false;
	private DrawerLayout drawerLayout;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.player);

		purchaseManager = new PurchaseManager(this);
		mHelper = purchaseManager.getmHelper();

		preferences = PreferenceManager
				.getDefaultSharedPreferences(MusicPlayerActivity.this);
		// Increment launch counter
		Integer lunchCount = preferences.getInt("launch_count", 0);
		currentLaunchCount = lunchCount + 1;
		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt("launch_count", currentLaunchCount);
		editor.commit();

		// All player buttons
		btnPlay = (ImageButton) findViewById(R.id.btnPlay);
		btnForward = (ImageButton) findViewById(R.id.btnForward);
		btnBackward = (ImageButton) findViewById(R.id.btnBackward);
		btnNext = (ImageButton) findViewById(R.id.btnNext);
		btnPrevious = (ImageButton) findViewById(R.id.btnPrevious);
		songProgressBar = (SeekBar) findViewById(R.id.songProgressBar);
		songTitleLabel = (TextView) findViewById(R.id.songTitle);
		songCurrentDurationLabel = (TextView) findViewById(R.id.songCurrentDurationLabel);
		songTotalDurationLabel = (TextView) findViewById(R.id.songTotalDurationLabel);
		loadingText = (TextView) findViewById(R.id.loading_text);

		initMoreAppButtons();

		songManager = new SongsManager();
		utils = new Utilities();

		// Media Player
		mp = new MediaPlayer();
		mp.setOnCompletionListener(this);
		mp.setOnBufferingUpdateListener(this);
		mp.setOnPreparedListener(this);

		// Listeners
		songProgressBar.setOnSeekBarChangeListener(this);

		// Getting all songs list
		songsList = songManager.getPlayListObjectList(this);

		initPlayerList();

		/**
		 * Play button click event plays a song and changes button to pause
		 * image pauses a song and changes button to play image
		 * */
		btnPlay.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// check for already playing
				if (mp.isPlaying()) {
					if (mp != null) {
						mp.pause();
						// Changing button image to play button
						btnPlay.setImageResource(R.drawable.btn_play);
					}
				} else {
					// Resume song
					if (mp != null) {
						mp.start();
						// Changing button image to pause button
						btnPlay.setImageResource(R.drawable.btn_pause);
					}
				}
			}
		});

		/**
		 * Forward button click event Forwards song specified seconds
		 * */
		btnForward.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (ConnectionManager.isOnline(getApplicationContext())) {
					if (mp.isPlaying()) {
					} else {
						// Resume song
						if (mp != null) {
							mp.start();
							// Changing button image to pause button
							btnPlay.setImageResource(R.drawable.btn_pause);
						}
					}
					// get current song position
					int currentPosition = mp.getCurrentPosition();
					// check if seekForward time is lesser than song duration
					if (currentPosition + seekForwardTime <= mp.getDuration()) {
						// forward song
						mp.seekTo(currentPosition + seekForwardTime);
					} else {
						// forward to end position
						mp.seekTo(mp.getDuration());
					}
				} else
					Toast.makeText(getApplicationContext(),
							getResources().getString(R.string.lost_connection),
							Toast.LENGTH_SHORT).show();
			}

		});

		/**
		 * Backward button click event Backward song to specified seconds
		 * */
		btnBackward.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (ConnectionManager.isOnline(getApplicationContext())) {
					if (mp.isPlaying()) {
					} else {
						// Resume song
						if (mp != null) {
							mp.start();
							// Changing button image to pause button
							btnPlay.setImageResource(R.drawable.btn_pause);
						}
					}

					// get current song position
					int currentPosition = mp.getCurrentPosition();
					// check if seekBackward time is greater than 0 sec
					if (currentPosition - seekBackwardTime >= 0) {
						// forward song
						mp.seekTo(currentPosition - seekBackwardTime);
					} else {
						// backward to starting position
						mp.seekTo(0);
					}
				}
				Toast.makeText(getApplicationContext(),
						getResources().getString(R.string.lost_connection),
						Toast.LENGTH_SHORT).show();
			}
		});

		/**
		 * Next button click event Plays next song by taking currentSongIndex +
		 * 1
		 * */
		btnNext.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// check if next song is there or not
				if (currentSongIndex < (songsList.size() - 1)) {
					playSong(currentSongIndex + 1);
				} else {
					// play first song
					playSong(0);
				}

			}
		});

		/**
		 * Back button click event Plays previous song by currentSongIndex - 1
		 * */
		btnPrevious.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (currentSongIndex > 0) {
					playSong(currentSongIndex - 1);
				} else {
					// play last song
					playSong(songsList.size() - 1);
				}

			}
		});

		playDefaultSong();
	}

	private void initMoreAppButtons() {
		ImageView liveYourLife = (ImageView) findViewById(R.id.live_your_live);
		liveYourLife.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri
						.parse(LIVE_YOUR_LIFE_URL)));
			}

		});

		ImageView successLessons = (ImageView) findViewById(R.id.success_lessons);
		successLessons.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri
						.parse(SUCCESS_LESSONS_URL)));
			}

		});
		ImageView successMindset = (ImageView) findViewById(R.id.success_mindset);
		successMindset.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri
						.parse(SUCCESS_MINDSET_URL)));
			}

		});
		ImageView yogaMusic = (ImageView) findViewById(R.id.yoga_music);
		yogaMusic.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri
						.parse(YOGA_MUSIC_URL)));
			}

		});
		ImageView kamasutra = (ImageView) findViewById(R.id.kamasutra);
		kamasutra.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri
						.parse(KAMASUTRA_URL)));
			}

		});
		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

		if (currentLaunchCount <= MAX_COUNT_TO_SHOW_MORE_APPS_MENU) {
			new Handler().postDelayed(openDrawerRunnable(), DRAWER_DELAY);
		}
	}

	private Runnable openDrawerRunnable() {
		return new Runnable() {

			@Override
			public void run() {
				drawerLayout.openDrawer(Gravity.RIGHT);
			}
		};
	}

	private void initPlayerList() {
		playerList = (ListView) findViewById(R.id.playerList);
		// List<HashMap<String, String>> listmap = new ArrayList<HashMap<String,
		// String>>();
		// for (SongObject song : songsList) {
		// HashMap<String, String> map = new HashMap<String, String>();
		// map.put("songTitle", song.getTrueSongName());
		// listmap.add(map);
		// }
		// ListAdapter adapter = new SimpleAdapter(this, listmap,
		// R.layout.playlist_item, new String[] { "songTitle" },
		// new int[] { R.id.songTitle });
		adapter = new PlayerListAdapter(this, R.layout.playlist_item, songsList);
		playerList.setAdapter(adapter);
		playerList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, View view,
					int position, long id) {
				playSong(position);
			}

		});
	}

	private void playDefaultSong() {
		// load position from Preferences
		int position = preferences.getInt(CURRENT_AUDIO_PODCAST_INDEX, 0);
		playSong(position);
	}

	/**
	 * Function to play a song
	 * 
	 * @param songIndex
	 *            - index of song
	 */
	public void playSong(int songIndex) {
		if ((songIndex >= songsList.size()) || (songIndex < 0)) {
			songIndex = 0;
		}
		SongObject songObject = songsList.get(songIndex);

		if ((songObject.getPaymentSongType().equals(PaymentSongType.FREE))
				|| (PurchaseManager.isPremium())) {

			switch (songObject.getStorageSongType()) {
			case LOCAL:
				currentSongIndex = songIndex;
				playLocalAudio(songObject);
				break;
			case INTERNET:
				if (ConnectionManager.isOnline(this)) {
					setWaitScreen(true);
					currentSongIndex = songIndex;
					playInternetAudio(songObject);
				} else {
					Toast.makeText(getApplicationContext(),
							getResources().getString(R.string.lost_connection),
							Toast.LENGTH_SHORT).show();
				}
				break;
			}
		} else {
			showPurchaseDialog();
		}
	}

	private void showPurchaseDialog() {
		AlertDialog.Builder bld = new AlertDialog.Builder(this);
		bld.setIcon(R.drawable.premium_access);
		bld.setTitle(getResources().getString(R.string.get_premium_access));
		bld.setMessage(getResources().getString(
				R.string.get_premium_access_text));
		bld.setPositiveButton(getResources().getString(R.string.yes),
				new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						purchaseManager.onUpgradeAppButtonClicked();
					}
				});
		bld.setNegativeButton(getResources().getString(R.string.cancel), null);
		bld.create().show();
	}

	/**
	 * Playing audio from Internet
	 * 
	 * @param songObject
	 *            - song to play
	 */
	private void playInternetAudio(SongObject songObject) {
		initPrerequisite(songObject);
		try {
			mp.reset();
			mp.setDataSource(songObject.getUrl());
			mp.prepareAsync();
			// Updating progress bar
			updateProgressBar();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Playing audio from assets directory
	 * 
	 * @param songObject
	 *            - song to play
	 */
	private void playLocalAudio(SongObject songObject) {
		initPrerequisite(songObject);
		try {
			mp.reset();
			mp.setDataSource(songObject.getSongsDescriptor()
					.getFileDescriptor(), songObject.getSongsDescriptor()
					.getStartOffset(), songObject.getSongsDescriptor()
					.getLength());
			mp.prepare();
			mp.start();
			// Updating progress bar
			updateProgressBar();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void initPrerequisite(SongObject songObject) {
		// Displaying Song title
		String songTitle = songObject.getTrueSongName();
		songTitleLabel.setText(songTitle);
		// Changing Button Image to pause image
		btnPlay.setImageResource(R.drawable.btn_pause);
		// set Progress bar values
		songProgressBar.setProgress(0);
		songProgressBar.setSecondaryProgress(0);
		songProgressBar.setMax(100);
		isPreparing = true;
	}

	/**
	 * Update timer on seekbar
	 */
	public void updateProgressBar() {
		mHandler.postDelayed(mUpdateTimeTask, 100);
	}

	/**
	 * Background Runnable thread
	 */
	private Runnable mUpdateTimeTask = new Runnable() {
		public void run() {
			try {
				if (mp != null) {
					long totalDuration = mp.getDuration();
					long currentDuration = mp.getCurrentPosition();

					// Displaying Total Duration time
					if (totalDuration < 10000000) {
						songTotalDurationLabel.setText(""
								+ utils.milliSecondsToTimer(totalDuration));
						// Displaying time completed playing
						songCurrentDurationLabel.setText(""
								+ utils.milliSecondsToTimer(currentDuration));
					} else {
						songTotalDurationLabel.setText("");
						songCurrentDurationLabel.setText("");
					}
					// Updating progress bar
					int progress = (int) (utils.getProgressPercentage(
							currentDuration, totalDuration));
					// Log.d("Progress", ""+progress);
					songProgressBar.setProgress(progress);

					// Running this thread after 100 milliseconds
					mHandler.postDelayed(this, 100);
				}
			} catch (IllegalStateException e) {
				// TODO error
			}
		}
	};

	/**
     *
     */
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromTouch) {

	}

	/**
	 * When user starts moving the progress handler
	 */
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// remove message Handler from updating progress bar
		mHandler.removeCallbacks(mUpdateTimeTask);
	}

	/**
	 * When user stops moving the progress hanlder
	 */
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		mHandler.removeCallbacks(mUpdateTimeTask);
		int totalDuration = mp.getDuration();
		int currentPosition = utils.progressToTimer(seekBar.getProgress(),
				totalDuration);

		// forward or backward to certain seconds
		mp.seekTo(currentPosition);

		// update timer progress again
		updateProgressBar();
	}

	/**
	 * On Song Playing completed
	 * 
	 */
	@Override
	public void onCompletion(MediaPlayer arg0) {
		if (isPreparing) {
			// waiting...
		} else {
			if (currentSongIndex < (songsList.size() - 1)) {
				playSong(currentSongIndex + 1);
				currentSongIndex = currentSongIndex + 1;
			} else {
				// play first song
				playSong(0);
				currentSongIndex = 0;
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mp.release();
		// We're being destroyed. It's important to dispose of the helper here!
		if (mHelper != null) {
			mHelper.dispose();
			mHelper = null;
		}
	}

	@Override
	public void onBufferingUpdate(MediaPlayer mediaPlayer, int percent) {
		/**
		 * Method which updates the SeekBar secondary progress by current song
		 * loading from URL position
		 */
		songProgressBar.setSecondaryProgress(percent);
	}

	private void checkRateStatus() {
		if (!preferences.getBoolean("alreadyrated", false)) {
			if (currentLaunchCount >= LAUNCHES_UNTIL_RATE) {
				if (ConnectionManager.isOnline(this)) {
					AppRater.showRateDialog(this, preferences);
				} else {
					savePosition();
					super.onBackPressed();
				}
			} else {
				savePosition();
				super.onBackPressed();
			}
		} else {
			savePosition();
			super.onBackPressed();
		}
	}

	@Override
	public void onBackPressed() {
		checkRateStatus();
	}

	private void savePosition() {
		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt(CURRENT_AUDIO_PODCAST_INDEX, currentSongIndex);
		editor.commit();
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		setWaitScreen(false);
		isPreparing = false;
		mp.start();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + ","
				+ data);
		if (mHelper == null)
			return;

		// Pass on the activity result to the helper for handling
		if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
			// not handled, so handle it ourselves (here's where you'd
			// perform any handling of activity results not related to in-app
			// billing...
			super.onActivityResult(requestCode, resultCode, data);
		} else {
			Log.d(TAG, "onActivityResult handled by IABUtil.");
		}
	}

	// Enables or disables the "please wait" screen.
	public void setWaitScreen(final boolean set) {
		loadingText.setVisibility(set ? View.VISIBLE : View.GONE);
	}

	/**
	 * Updates UI to reflect model
	 */
	public void updateUi() {
		if (adapter != null) {
			adapter.notifyDataSetChanged();
		}
	}

}