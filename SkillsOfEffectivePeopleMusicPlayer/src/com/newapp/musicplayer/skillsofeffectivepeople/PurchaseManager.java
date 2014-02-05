package com.newapp.musicplayer.skillsofeffectivepeople;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.newapp.musicplayer.skillsofeffectivepeople.R;
import com.newapp.musicplayer.skillsofeffectivepeople.util.IabHelper;
import com.newapp.musicplayer.skillsofeffectivepeople.util.IabResult;
import com.newapp.musicplayer.skillsofeffectivepeople.util.Inventory;
import com.newapp.musicplayer.skillsofeffectivepeople.util.Purchase;

public class PurchaseManager {

	// The helper object
	private IabHelper mHelper;
	// Debug MusicPlayerActivity.TAG, for logging

	// Does the user have the premium upgrade?
	private static boolean isPremium = false;
	private MusicPlayerActivity activity;

	// SKUs for our products: the premium upgrade (non-consumable) and gas
	// (consumable)
	static final String SKU_PREMIUM = "premium_access";

	// (arbitrary) request code for the purchase flow
	static final int RC_REQUEST = 10001;

	public PurchaseManager(MusicPlayerActivity activity) {
		this.activity = activity;
		String base64EncodedPublicKey = activity.getResources().getString(
				R.string.public_key);

		loadData();

		// Create the helper, passing it our context and the public key to
		// verify signatures with
		Log.d(MusicPlayerActivity.TAG, "Creating IAB helper.");
		mHelper = new IabHelper(activity, base64EncodedPublicKey);

		// enable debug logging (for a production application, you should set
		// this to false).
		// mHelper.enableDebugLogging(true);

		mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
			public void onIabSetupFinished(IabResult result) {
				Log.d(MusicPlayerActivity.TAG, "Setup finished.");

				if (!result.isSuccess()) {
					complain(PurchaseManager.this.activity
							.getString(R.string.problem_setting_up_in_app_billing));
					return;
				}

				// Have we been disposed of in the meantime? If so, quit.
				if (mHelper == null)
					return;

				// IAB is fully set up. Now, let's get an inventory of stuff we
				// own.
				Log.d(MusicPlayerActivity.TAG,
						"Setup successful. Querying inventory.");
				mHelper.queryInventoryAsync(mGotInventoryListener);
			}
		});

	}

	public IabHelper getmHelper() {
		return mHelper;
	}

	// Listener that's called when we finish querying the items and
	// subscriptions we own
	IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
		public void onQueryInventoryFinished(IabResult result,
				Inventory inventory) {
			Log.d(MusicPlayerActivity.TAG, "Query inventory finished.");

			// Have we been disposed of in the meantime? If so, quit.
			if (mHelper == null)
				return;

			// Is it a failure?
			if (result.isFailure()) {
				complain(activity.getString(R.string.failed_to_query_inventory));
				return;
			}

			Log.d(MusicPlayerActivity.TAG, "Query inventory was successful.");

			/*
			 * Check for items we own. Notice that for each purchase, we check
			 * the developer payload to see if it's correct! See
			 * verifyDeveloperPayload().
			 */

			// Do we have the premium upgrade?
			Purchase premiumPurchase = inventory.getPurchase(SKU_PREMIUM);
			isPremium = (premiumPurchase != null && verifyDeveloperPayload(premiumPurchase));
			Log.d(MusicPlayerActivity.TAG, "User is "
					+ (isPremium ? "PREMIUM" : "NOT PREMIUM"));

			activity.updateUi();
			activity.setWaitScreen(false);
			Log.d(MusicPlayerActivity.TAG,
					"Initial inventory query finished; enabling main UI.");
		}
	};

	// User clicked the "Upgrade to Premium" button.
	public void onUpgradeAppButtonClicked() {
		Log.d(MusicPlayerActivity.TAG,
				"Upgrade button clicked; launching purchase flow for upgrade.");
		activity.setWaitScreen(true);

		/*
		 * TODO: for security, generate your payload here for verification. See
		 * the comments on verifyDeveloperPayload() for more info. Since this is
		 * a SAMPLE, we just use an empty string, but on a production app you
		 * should carefully generate this.
		 */
		String payload = "";

		mHelper.launchPurchaseFlow(activity, SKU_PREMIUM, RC_REQUEST,
				mPurchaseFinishedListener, payload);
	}

	/** Verifies the developer payload of a purchase. */
	boolean verifyDeveloperPayload(Purchase p) {
		// String payload = p.getDeveloperPayload();

		/*
		 * TODO: verify that the developer payload of the purchase is correct.
		 * It will be the same one that you sent when initiating the purchase.
		 * 
		 * WARNING: Locally generating a random string when starting a purchase
		 * and verifying it here might seem like a good approach, but this will
		 * fail in the case where the user purchases an item on one device and
		 * then uses your app on a different device, because on the other device
		 * you will not have access to the random string you originally
		 * generated.
		 * 
		 * So a good developer payload has these characteristics:
		 * 
		 * 1. If two different users purchase an item, the payload is different
		 * between them, so that one user's purchase can't be replayed to
		 * another user.
		 * 
		 * 2. The payload must be such that you can verify it even when the app
		 * wasn't the one who initiated the purchase flow (so that items
		 * purchased by the user on one device work on other devices owned by
		 * the user).
		 * 
		 * Using your own server to store and verify developer payloads across
		 * app installations is recommended.
		 */

		return true;
	}

	// Callback for when a purchase is finished
	IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
		public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
			Log.d(MusicPlayerActivity.TAG, "Purchase finished: " + result
					+ ", purchase: " + purchase);

			// if we were disposed of in the meantime, quit.
			if (mHelper == null)
				return;

			if (result.isFailure()) {
				complain(activity.getString(R.string.error_purchasing));
				activity.setWaitScreen(false);
				return;
			}
			if (!verifyDeveloperPayload(purchase)) {
				complain(activity
						.getString(R.string.authenticity_verification_failed));
				activity.setWaitScreen(false);
				return;
			}

			Log.d(MusicPlayerActivity.TAG, "Purchase successful.");

			if (purchase.getSku().equals(SKU_PREMIUM)) {
				// bought the premium upgrade!
				Log.d(MusicPlayerActivity.TAG,
						"Purchase is premium upgrade. Congratulating user.");
				alert(activity.getResources()
						.getString(R.string.congratulating));
				isPremium = true;
				saveData();
				activity.updateUi();
				activity.setWaitScreen(false);
			}
		}
	};

	void complain(String message) {
		Log.e(MusicPlayerActivity.TAG, "**** skillsofeffectivepeopleMusicPlayer Error: "
				+ message);
		alert(activity.getResources().getString(R.string.error) + " " + message);
	}

	void alert(String message) {
		AlertDialog.Builder bld = new AlertDialog.Builder(activity);
		bld.setMessage(message);
		bld.setNeutralButton("OK", null);
		bld.create().show();
	}

	void saveData() {
		SharedPreferences.Editor spe = activity.getPreferences(
				Context.MODE_PRIVATE).edit();
		spe.putBoolean("premium", isPremium);
		spe.commit();
	}

	void loadData() {
		SharedPreferences preferences = activity
				.getPreferences(Context.MODE_PRIVATE);
		isPremium = preferences.getBoolean("premium", false);
		activity.updateUi();
		Log.d(MusicPlayerActivity.TAG, "Loaded data: PREMIUM = " + isPremium);
	}

	public static boolean isPremium() {
		return isPremium;
	}

}
