package com.newapp.musicplayer.skillsofeffectivepeople;

import java.util.List;

import com.newapp.musicplayer.skillsofeffectivepeople.R;
import com.newapp.musicplayer.skillsofeffectivepeople.enums.PaymentSongType;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PlayerListAdapter extends ArrayAdapter<SongObject> {

	int resource;
	String response;
	Context context;

	// Initialize adapter
	public PlayerListAdapter(Context context, int resource,
			List<SongObject> items) {
		super(context, resource, items);
		this.resource = resource;

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		SongObject songObject = getItem(position);

		View v = convertView;
		// to inflate it basically means to render, or show, the view.
		if (v == null) {
			LayoutInflater inflater = (LayoutInflater) getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(resource, null);
		}

		// Get the text boxes from the listitem.xml file
		TextView songTitle = (TextView) v.findViewById(R.id.songTitle);
		LinearLayout layout = (LinearLayout) v.findViewById(R.id.layout);
		// Assign the appropriate data from our alert object above
		songTitle.setText(songObject.getTrueSongName());

		if (songObject.getPaymentSongType().equals(PaymentSongType.FREE)
				|| (PurchaseManager.isPremium())) {
			layout.setBackgroundResource(R.drawable.list_selector);
		} else {
			layout.setBackgroundResource(R.drawable.disabled_item_gradient_bg);
		}

		return v;
	}

}
