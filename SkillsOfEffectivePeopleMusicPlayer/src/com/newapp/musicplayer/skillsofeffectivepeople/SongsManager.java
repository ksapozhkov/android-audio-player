package com.newapp.musicplayer.skillsofeffectivepeople;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.newapp.musicplayer.skillsofeffectivepeople.R;
import com.newapp.musicplayer.skillsofeffectivepeople.enums.PaymentSongType;
import com.newapp.musicplayer.skillsofeffectivepeople.enums.StorageSongType;


public class SongsManager {
	// SDCard Path
	final String MEDIA_PATH = new String("file:///android_asset/");
	private List<SongObject> playListObjectList = null;
	private static SongsManager INSTANCE = null;

	// Constructor
	public static SongsManager getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new SongsManager();
		}
		return INSTANCE;
	}

	/**
	 * Function to read all mp3 files from assets and store the details in
	 * ArrayList
	 */
	public List<SongObject> getPlayListObjectList(Activity act) {
		if (playListObjectList == null) {
			playListObjectList = new ArrayList<SongObject>();

			String[] urls = act.getResources().getStringArray(R.array.urls);
			String[] names = act.getResources().getStringArray(R.array.names);
			String[] payment = act.getResources().getStringArray(
					R.array.payment);

			int currentSongIndex = 0;
			AssetManager am = act.getAssets();
			try {
				// populate local audio files
				String[] list = am.list("songs");
				for (String name : list) {
					SongObject playListObject = new SongObject();
					AssetFileDescriptor descriptor = am.openFd("songs/" + name);
					String trueName = getStringResourceByName(name, act);
					playListObject.setTrueSongName(trueName);
					playListObject.setSongsDescriptor(descriptor);
					playListObject.setStorageSongType(StorageSongType.LOCAL);
					playListObject.setPaymentSongType(getPaymentType(payment,
							currentSongIndex));
					playListObjectList.add(playListObject);
					currentSongIndex++;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			int i = 0;
			for (String url : urls) {
				SongObject playListObject = new SongObject();
				playListObject.setTrueSongName(names[i]);
				playListObject.setUrl(url);
				playListObject.setStorageSongType(StorageSongType.INTERNET);
				playListObject.setPaymentSongType(getPaymentType(payment,
						currentSongIndex));
				playListObjectList.add(playListObject);
				i++;
				currentSongIndex++;
			}
		}
		return playListObjectList;
	}

	private PaymentSongType getPaymentType(String[] payment,
			int currentSongIndex) {
		PaymentSongType paymentSongType = payment[currentSongIndex]
				.equals("free") ? PaymentSongType.FREE : PaymentSongType.PAID;
		return paymentSongType;
	}

	/**
	 * 
	 * @param aString
	 *            - name of the song file
	 * @param a
	 *            - activity
	 * @return real name of the song
	 */
	private String getStringResourceByName(String aString, Activity a) {
		String packageName = a.getPackageName();
		int resId = a.getResources().getIdentifier(aString, "string",
				packageName);
		return a.getString(resId);
	}

}
