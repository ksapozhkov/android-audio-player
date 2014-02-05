package com.newapp.musicplayer.skillsofeffectivepeople;

import com.newapp.musicplayer.skillsofeffectivepeople.enums.PaymentSongType;
import com.newapp.musicplayer.skillsofeffectivepeople.enums.StorageSongType;

import android.content.res.AssetFileDescriptor;

/**
 * Created with IntelliJ IDEA. User: VOIN Date: 18.09.13 Time: 7:13 To change
 * this template use File | Settings | File Templates.
 */
public class SongObject {

	private String trueSongName;
	private AssetFileDescriptor songsDescriptor;
	private StorageSongType storageSongType;
	private PaymentSongType paymentSongType;
	private String url;

	public AssetFileDescriptor getSongsDescriptor() {
		return songsDescriptor;
	}

	public void setSongsDescriptor(AssetFileDescriptor songsDescriptor) {
		this.songsDescriptor = songsDescriptor;
	}

	public String getTrueSongName() {
		return trueSongName;
	}

	public void setTrueSongName(String trueSongName) {
		this.trueSongName = trueSongName;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public PaymentSongType getPaymentSongType() {
		return paymentSongType;
	}

	public void setPaymentSongType(PaymentSongType paymentSongType) {
		this.paymentSongType = paymentSongType;
	}

	public StorageSongType getStorageSongType() {
		return storageSongType;
	}

	public void setStorageSongType(StorageSongType storageSongType) {
		this.storageSongType = storageSongType;
	}

}
