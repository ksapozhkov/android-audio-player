package com.newapp.musicplayer.skillsofeffectivepeople;

import com.newapp.musicplayer.skillsofeffectivepeople.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

public class AppRater {

	private static final String MARKET_RATE_URL = "https://play.google.com/store/apps/details?id=";

	public static void showRateDialog(final Context mContext,
			final SharedPreferences pref) {
		final SharedPreferences.Editor editor = pref.edit();
		String appName = mContext.getResources().getString(R.string.app_name);

		AlertDialog.Builder bld = new AlertDialog.Builder(mContext);
		bld.setIcon(R.drawable.rate);
		bld.setTitle(appName);
		bld.setMessage(mContext.getResources().getString(R.string.rate_text));
		bld.setPositiveButton(mContext.getResources().getString(R.string.rate),
				new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						mContext.startActivity(new Intent(Intent.ACTION_VIEW,
								Uri.parse(MARKET_RATE_URL
										+ mContext.getPackageName())));
						if (editor != null) {
							editor.putBoolean("alreadyrated", true);
							editor.commit();
						}
					}
				});
		bld.setNegativeButton(mContext.getResources()
				.getString(R.string.cancel), new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (editor != null) {
					editor.putBoolean("alreadyrated", true);
					editor.commit();
				}

			}

		});
		bld.create().show();
	}
}
