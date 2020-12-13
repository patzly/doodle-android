package xyz.zedler.patrick.doodle;

import android.app.ActivityManager;
import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.Objects;

import xyz.zedler.patrick.doodle.behavior.ScrollBehavior;
import xyz.zedler.patrick.doodle.fragment.TextBottomSheetDialogFragment;
import xyz.zedler.patrick.doodle.service.LiveWallpaperService;

public class SettingsActivity extends AppCompatActivity
		implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

	private final static boolean DEBUG = false;
	private final static String TAG = "SettingsActivity";

	private SharedPreferences sharedPrefs;

	private long lastClick = 0;
	private MaterialButton buttonSet;
	private MaterialCardView cardViewDoodle, cardViewNature, cardViewNeon, cardViewGeometric;
	private MaterialCardView cardViewBlack, cardViewWhite, cardViewOrange;
	private ImageView imageViewNightMode;
	private LinearLayout linearLayoutVariant, linearLayoutFollowSystem, linearLayoutFollowSystemContainer;
	private SwitchMaterial switchNightMode, switchFollowSystem;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
		setContentView(R.layout.activity_settings);

		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		findViewById(R.id.frame_back).setOnClickListener(v -> {
			if (SystemClock.elapsedRealtime() - lastClick < 1000) return;
			lastClick = SystemClock.elapsedRealtime();
			finish();
		});

		new ScrollBehavior().setUpScroll(
				this,
				R.id.app_bar,
				R.id.linear_app_bar,
				R.id.scroll,
				true
		);

		buttonSet = findViewById(R.id.button_set);
		buttonSet.setEnabled(!isWallpaperServiceRunning());
		buttonSet.setBackgroundColor(
				getResources().getColor(
						isWallpaperServiceRunning()
								? R.color.secondary_disabled
								: R.color.secondary
				)
		);
		buttonSet.setTextColor(
				getResources().getColor(
						isWallpaperServiceRunning()
								? R.color.on_secondary_disabled
								: R.color.on_secondary
				)
		);
		cardViewDoodle = findViewById(R.id.card_doodle);
		cardViewNature = findViewById(R.id.card_nature);
		cardViewNeon = findViewById(R.id.card_neon);
		cardViewGeometric = findViewById(R.id.card_geometric);
		cardViewBlack = findViewById(R.id.card_black);
		cardViewWhite = findViewById(R.id.card_white);
		cardViewOrange = findViewById(R.id.card_orange);
		switchNightMode = findViewById(R.id.switch_night_mode);
		switchNightMode.setChecked(sharedPrefs.getBoolean("night_mode", true));
		switchNightMode.setOnCheckedChangeListener(this);
		switchFollowSystem = findViewById(R.id.switch_follow_system);
		switchFollowSystem.setChecked(sharedPrefs.getBoolean("follow_system", true));
		switchFollowSystem.setOnCheckedChangeListener(this);
		switchFollowSystem.setEnabled(switchNightMode.isChecked());
		boolean active = Objects.requireNonNull(
				sharedPrefs.getString("theme", "doodle")
		).equals("doodle");
		linearLayoutVariant = findViewById(R.id.linear_variant);
		linearLayoutVariant.setAlpha(active ? 1 : 0.5f);
		if (!active) {
			for (int i = 0; i < linearLayoutVariant.getChildCount(); i++) {
				linearLayoutVariant.getChildAt(i).setEnabled(false);
			}
		}
		linearLayoutFollowSystem = findViewById(R.id.linear_follow_system);
		linearLayoutFollowSystem.setEnabled(switchNightMode.isChecked());
		linearLayoutFollowSystemContainer = findViewById(R.id.linear_follow_system_container);
		linearLayoutFollowSystemContainer.setAlpha(switchNightMode.isChecked() ? 1 : 0.5f);
		imageViewNightMode = findViewById(R.id.image_night_mode);
		imageViewNightMode.setImageResource(
				sharedPrefs.getBoolean("night_mode", true)
						? R.drawable.ic_round_night_mode_off_anim
						: R.drawable.ic_round_night_mode_on_anim

		);

		RadioGroup radioGroupParallax = findViewById(R.id.radio_group_parallax);
		switch (sharedPrefs.getInt("parallax", 100)) {
			case 0:
				radioGroupParallax.check(R.id.radio_none);
				break;
			case 100:
				radioGroupParallax.check(R.id.radio_little);
				break;
			case 200:
				radioGroupParallax.check(R.id.radio_much);
				break;
		}
		radioGroupParallax.setOnCheckedChangeListener((RadioGroup group, int checkedId) -> {
			startAnimatedIcon(R.id.image_parallax);
			switch (checkedId) {
				case R.id.radio_none:
					sharedPrefs.edit().putInt("parallax", 0).apply();
					break;
				case R.id.radio_little:
					sharedPrefs.edit().putInt("parallax", 100).apply();
					break;
				case R.id.radio_much:
					sharedPrefs.edit().putInt("parallax", 200).apply();
					break;
			}
		});

		RadioGroup radioGroupSize = findViewById(R.id.radio_group_size);
		radioGroupSize.check(
				sharedPrefs.getBoolean("size_big", false)
						? R.id.radio_big
						: R.id.radio_default
		);
		radioGroupSize.setOnCheckedChangeListener((RadioGroup group, int checkedId) -> {
			startAnimatedIcon(R.id.image_size);
			switch (checkedId) {
				case R.id.radio_default:
					sharedPrefs.edit().putBoolean("size_big", false).apply();
					break;
				case R.id.radio_big:
					sharedPrefs.edit().putBoolean("size_big", true).apply();
					break;
			}
		});

		refreshSelectionTheme(Objects.requireNonNull(sharedPrefs.getString("theme", "doodle")));
		refreshSelectionVariant(Objects.requireNonNull(sharedPrefs.getString("variant", "black")));

		setOnClickListeners(
				R.id.linear_help,
				R.id.card_doodle,
				R.id.card_nature,
				R.id.card_neon,
				R.id.card_geometric,
				R.id.card_black,
				R.id.card_white,
				R.id.card_orange,
				R.id.linear_night_mode,
				R.id.linear_follow_system,
				R.id.linear_changelog,
				R.id.linear_developer,
				R.id.linear_license_material_components,
				R.id.linear_license_material_icons,
				R.id.linear_license_roboto,
				R.id.button_set,
				R.id.linear_rate,
				R.id.linear_feedback
		);
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (!isWallpaperServiceRunning()) {
			buttonSet.setEnabled(true);
			buttonSet.setBackgroundColor(getResources().getColor(R.color.secondary));
			buttonSet.setTextColor(getResources().getColor(R.color.on_secondary));
		}

		sharedPrefs.edit().putBoolean("should_refresh", true).apply();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		if (requestCode == 1) {
			if (resultCode == RESULT_OK) {
				buttonSet.setEnabled(false);
				buttonSet.setBackgroundColor(getResources().getColor(R.color.secondary_disabled));
				buttonSet.setTextColor(getResources().getColor(R.color.on_secondary_disabled));
			}
			if (resultCode == RESULT_CANCELED) {
				buttonSet.setEnabled(true);
				buttonSet.setBackgroundColor(getResources().getColor(R.color.secondary));
				buttonSet.setTextColor(getResources().getColor(R.color.on_secondary));
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void setOnClickListeners(@IdRes int... viewIds) {
		for (int viewId : viewIds) {
			findViewById(viewId).setOnClickListener(this);
		}
	}

	@Override
	public void onClick(View v) {

		if (SystemClock.elapsedRealtime() - lastClick < 400){
			return;
		}
		lastClick = SystemClock.elapsedRealtime();

		switch(v.getId()) {
			case R.id.linear_help:
				startAnimatedIcon(R.id.image_help);
				showTextBottomSheet("help", R.string.category_help, 0);
				break;
			case R.id.card_doodle:
				startAnimatedIcon(R.id.image_theme);
				refreshSelectionTheme("doodle");
				break;
			case R.id.card_nature:
				startAnimatedIcon(R.id.image_theme);
				refreshSelectionTheme("nature");
				break;
			case R.id.card_neon:
				startAnimatedIcon(R.id.image_theme);
				refreshSelectionTheme("neon");
				break;
			case R.id.card_geometric:
				startAnimatedIcon(R.id.image_theme);
				refreshSelectionTheme("geometric");
				break;
			case R.id.card_black:
				//startAnimatedIcon(R.id.image_theme);
				refreshSelectionVariant("black");
				break;
			case R.id.card_white:
				//startAnimatedIcon(R.id.image_theme);
				refreshSelectionVariant("white");
				break;
			case R.id.card_orange:
				//startAnimatedIcon(R.id.image_theme);
				refreshSelectionVariant("orange");
				break;
			case R.id.linear_night_mode:
				switchNightMode.setChecked(!switchNightMode.isChecked());
				break;
			case R.id.linear_follow_system:
				startAnimatedIcon(R.id.image_follow_system);
				if (switchNightMode.isChecked()) {
					switchFollowSystem.setChecked(!switchFollowSystem.isChecked());
				}
				break;
			case R.id.linear_changelog:
				startAnimatedIcon(R.id.image_changelog);
				showTextBottomSheet("changelog", R.string.info_changelog, 0);
				break;
			case R.id.linear_developer:
				startAnimatedIcon(R.id.image_developer);
				new Handler().postDelayed(
						() -> startActivity(
								new Intent(
										Intent.ACTION_VIEW,
										Uri.parse(
												"http://play.google.com/store/apps/dev?id=8122479227040208191"
										)
								)
						), 300
				);
				break;
			case R.id.linear_feedback:
				startAnimatedIcon(R.id.image_feedback);
				startActivity(
						Intent.createChooser(
								new Intent(Intent.ACTION_SENDTO).setData(
										Uri.parse(
												"mailto:patrick@zedler.xyz" +
														"?subject=" + Uri.encode("Feedback@Doodle") +
														"&body=" + Uri.encode(
														"\n\n" + System.getProperty("os.version") +
																"$" + android.os.Build.VERSION.SDK_INT +
																"\n" + android.os.Build.MODEL +
																" (" + android.os.Build.DEVICE +
																"), " + android.os.Build.MANUFACTURER
												)
										)
								), "Send email"
						)
				);
				break;
			case R.id.linear_rate:
				startAnimatedIcon(R.id.image_rate);
				Uri uri = Uri.parse("market://details?id=" + getPackageName());
				Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
				goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
						Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
						Intent.FLAG_ACTIVITY_MULTIPLE_TASK |
						Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
				try {
					startActivity(goToMarket);
				} catch (ActivityNotFoundException e) {
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
							"http://play.google.com/store/apps/details?id=" + getPackageName()
					)));
				}
				break;
			case R.id.linear_license_material_components:
				startAnimatedIcon(R.id.image_license_material_components);
				showTextBottomSheet(
						"apache",
						R.string.license_material_components,
						R.string.license_material_components_link
				);
				break;
			case R.id.linear_license_material_icons:
				startAnimatedIcon(R.id.image_license_material_icons);
				showTextBottomSheet(
						"apache",
						R.string.license_material_icons,
						R.string.license_material_icons_link
				);
				break;
			case R.id.linear_license_roboto:
				startAnimatedIcon(R.id.image_license_roboto);
				showTextBottomSheet(
						"apache",
						R.string.license_roboto,
						R.string.license_roboto_link
				);
				break;
			case R.id.button_set:
				startActivityForResult(
						new Intent()
								.setAction(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
								.putExtra(
										WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
										new ComponentName(
												"xyz.zedler.patrick.doodle",
												"xyz.zedler.patrick.doodle.service.LiveWallpaperService"
										)
								),
						1
				);
				break;
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
			case R.id.switch_night_mode:
				startAnimatedIcon(R.id.image_night_mode);
				switchFollowSystem.setEnabled(isChecked);
				linearLayoutFollowSystem.setEnabled(isChecked);
				linearLayoutFollowSystemContainer.animate().alpha(isChecked ? 1 : 0.5f).setDuration(200).start();
				sharedPrefs.edit().putBoolean("night_mode", isChecked).apply();
				new Handler().postDelayed(() -> imageViewNightMode.setImageResource(
						isChecked
								? R.drawable.ic_round_night_mode_off_anim
								: R.drawable.ic_round_night_mode_on_anim

				), 300);
				break;
			case R.id.switch_follow_system:
				sharedPrefs.edit().putBoolean("follow_system", isChecked).apply();
				break;
		}
	}

	private void refreshSelectionTheme(String selection) {
		MaterialCardView mcvSelected, mcv1, mcv2, mcv3;
		switch (selection) {
			case "nature":
				mcvSelected = cardViewNature;
				mcv1 = cardViewDoodle;
				mcv2 = cardViewNeon;
				mcv3 = cardViewGeometric;
				break;
			case "neon":
				mcvSelected = cardViewNeon;
				mcv1 = cardViewDoodle;
				mcv2 = cardViewNature;
				mcv3 = cardViewGeometric;
				break;
			case "geometric":
				mcvSelected = cardViewGeometric;
				mcv1 = cardViewDoodle;
				mcv2 = cardViewNature;
				mcv3 = cardViewNeon;
				break;
			default:
				mcvSelected = cardViewDoodle;
				mcv1 = cardViewNature;
				mcv2 = cardViewNeon;
				mcv3 = cardViewGeometric;
				break;
		}
		mcvSelected.setStrokeColor(getResources().getColor(R.color.secondary));
		mcvSelected.setChecked(true);
		mcv1.setStrokeColor(getResources().getColor(R.color.stroke));
		mcv1.setChecked(false);
		mcv2.setStrokeColor(getResources().getColor(R.color.stroke));
		mcv2.setChecked(false);
		mcv3.setStrokeColor(getResources().getColor(R.color.stroke));
		mcv3.setChecked(false);
		linearLayoutVariant.animate().alpha(selection.equals("doodle") ? 1 : 0.5f).setDuration(200).start();
		for (int i = 0; i < linearLayoutVariant.getChildCount(); i++) {
			linearLayoutVariant.getChildAt(i).setEnabled(selection.equals("doodle"));
		}
		sharedPrefs.edit().putString("theme", selection).apply();
	}

	private void refreshSelectionVariant(String selection) {
		MaterialCardView mcvSelected, mcv1, mcv2;
		switch (selection) {
			case "white":
				mcvSelected = cardViewWhite;
				mcv1 = cardViewBlack;
				mcv2 = cardViewOrange;
				break;
			case "orange":
				mcvSelected = cardViewOrange;
				mcv1 = cardViewBlack;
				mcv2 = cardViewWhite;
				break;
			default:
				mcvSelected = cardViewBlack;
				mcv1 = cardViewWhite;
				mcv2 = cardViewOrange;
				break;
		}
		mcvSelected.setStrokeColor(getResources().getColor(R.color.secondary));
		mcvSelected.setChecked(true);
		mcv1.setStrokeColor(getResources().getColor(R.color.stroke));
		mcv1.setChecked(false);
		mcv2.setStrokeColor(getResources().getColor(R.color.stroke));
		mcv2.setChecked(false);
		sharedPrefs.edit().putString("variant", selection).apply();
	}

	private boolean isWallpaperServiceRunning() {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		if (manager != null) {
			for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
				if (LiveWallpaperService.class.getName().equals(service.service.getClassName())) {
					return true;
				}
			}
		}
		return false;
	}

	private void showTextBottomSheet(String file, @StringRes int title, @StringRes int link) {
		Fragment textBottomSheetDialogFragment = new TextBottomSheetDialogFragment();
		Bundle bundle = new Bundle();
		bundle.putString("title", getString(title));
		bundle.putString("file", file);
		if (link != 0) {
			bundle.putString("link", getString(link));
		}
		textBottomSheetDialogFragment.setArguments(bundle);
		getSupportFragmentManager().beginTransaction()
				.add(textBottomSheetDialogFragment, "text")
				.commit();
	}

	private void startAnimatedIcon(int viewId) {
		try {
			((Animatable) ((ImageView) findViewById(viewId)).getDrawable()).start();
		} catch (ClassCastException e) {
			if (DEBUG) Log.e(TAG, "startAnimatedIcon() requires AVD!");
		}
	}
}
