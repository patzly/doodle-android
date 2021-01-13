package xyz.zedler.patrick.doodle;

import android.app.ActivityManager;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;

import xyz.zedler.patrick.doodle.behavior.ScrollBehavior;
import xyz.zedler.patrick.doodle.databinding.ActivitySettingsBinding;
import xyz.zedler.patrick.doodle.service.LiveWallpaperService;
import xyz.zedler.patrick.doodle.util.ClickUtil;
import xyz.zedler.patrick.doodle.util.IconUtil;

public class SettingsActivity extends AppCompatActivity
		implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

	private final static String TAG = SettingsActivity.class.getSimpleName();

	private ActivitySettingsBinding binding;
	private SharedPreferences sharedPrefs;
	private ClickUtil clickUtil;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
		binding = ActivitySettingsBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		clickUtil = new ClickUtil();

		binding.frameBack.setOnClickListener(v -> {
			if (clickUtil.isDisabled()) return;
			finish();
		});

		new ScrollBehavior().setUpScroll(
				this,
				binding.appBar,
				binding.linearAppBar,
				binding.scroll,
				true
		);

		binding.buttonSet.setEnabled(!isWallpaperServiceRunning());
		binding.buttonSet.setBackgroundColor(
				ContextCompat.getColor(
						this,
						isWallpaperServiceRunning()
								? R.color.secondary_disabled
								: R.color.retro_green_bg_white
				)
		);
		binding.buttonSet.setTextColor(
				ContextCompat.getColor(
						this,
						isWallpaperServiceRunning()
								? R.color.on_secondary_disabled
								: R.color.on_secondary
				)
		);
		binding.switchNightMode.setChecked(
				sharedPrefs.getBoolean(Constants.PREF.NIGHT_MODE, true)
		);
		binding.switchFollowSystem.setChecked(
				sharedPrefs.getBoolean(Constants.PREF.FOLLOW_SYSTEM, true)
		);
		binding.switchFollowSystem.setEnabled(binding.switchNightMode.isChecked());
		boolean active = sharedPrefs.getString(
				Constants.PREF.THEME, Constants.THEME.DOODLE
		).equals(Constants.THEME.DOODLE);
		binding.linearVariant.setAlpha(active ? 1 : 0.5f);
		if (!active) {
			for (int i = 0; i < binding.linearVariant.getChildCount(); i++) {
				binding.linearVariant.getChildAt(i).setEnabled(false);
			}
		}
		binding.linearFollowSystem.setEnabled(binding.switchNightMode.isChecked());
		binding.linearFollowSystemContainer.setAlpha(
				binding.switchNightMode.isChecked() ? 1 : 0.5f
		);
		binding.imageNightMode.setImageResource(
				sharedPrefs.getBoolean(Constants.PREF.NIGHT_MODE, true)
						? R.drawable.ic_round_night_mode_off_anim
						: R.drawable.ic_round_night_mode_on_anim

		);

		switch (sharedPrefs.getInt(Constants.PREF.PARALLAX, 100)) {
			case 0:
				binding.radioGroupParallax.check(R.id.radio_none);
				break;
			case 100:
				binding.radioGroupParallax.check(R.id.radio_little);
				break;
			case 200:
				binding.radioGroupParallax.check(R.id.radio_much);
				break;
		}
		binding.radioGroupParallax.setOnCheckedChangeListener((RadioGroup group, int checkedId) -> {
			IconUtil.start(binding.imageParallax);
			if (checkedId == R.id.radio_none) {
				sharedPrefs.edit().putInt(Constants.PREF.PARALLAX, 0).apply();
			} else if (checkedId == R.id.radio_little) {
				sharedPrefs.edit().putInt(Constants.PREF.PARALLAX, 100).apply();
			} else if (checkedId == R.id.radio_much) {
				sharedPrefs.edit().putInt(Constants.PREF.PARALLAX, 200).apply();
			}
		});

		binding.radioGroupSize.check(
				sharedPrefs.getBoolean(Constants.PREF.SIZE_BIG, false)
						? R.id.radio_big
						: R.id.radio_default
		);
		binding.radioGroupSize.setOnCheckedChangeListener((RadioGroup group, int checkedId) -> {
			IconUtil.start(binding.imageSize);
			if (checkedId == R.id.radio_default) {
				sharedPrefs.edit().putBoolean(Constants.PREF.SIZE_BIG, false).apply();
			} else if (checkedId == R.id.radio_big) {
				sharedPrefs.edit().putBoolean(Constants.PREF.SIZE_BIG, true).apply();
			}
		});

		refreshSelectionTheme(sharedPrefs.getString(Constants.PREF.THEME, Constants.THEME.DOODLE));
		refreshSelectionVariant(
				sharedPrefs.getString(Constants.PREF.VARIANT, Constants.VARIANT.BLACK)
		);

		ClickUtil.setOnClickListeners(
				this,
				binding.linearHelp,
				binding.cardDoodle,
				binding.cardNeon,
				binding.cardGeometric,
				binding.cardBlack,
				binding.cardWhite,
				binding.cardOrange,
				binding.linearNightMode,
				binding.linearFollowSystem,
				binding.linearDeveloper,
				binding.buttonSet
		);

		ClickUtil.setOnCheckedChangeListeners(
				this,
				binding.switchNightMode,
				binding.switchFollowSystem
		);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		binding = null;
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (!isWallpaperServiceRunning()) {
			binding.buttonSet.setEnabled(true);
			binding.buttonSet.setBackgroundColor(
					ContextCompat.getColor(this, R.color.retro_green_bg_white)
			);
			binding.buttonSet.setTextColor(
					ContextCompat.getColor(this, R.color.on_secondary)
			);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		if (requestCode == 1) {
			if (resultCode == RESULT_OK) {
				binding.buttonSet.setEnabled(false);
				binding.buttonSet.setBackgroundColor(
						ContextCompat.getColor(this, R.color.secondary_disabled)
				);
				binding.buttonSet.setTextColor(
						ContextCompat.getColor(this, R.color.on_secondary_disabled)
				);
			}
			if (resultCode == RESULT_CANCELED) {
				binding.buttonSet.setEnabled(true);
				binding.buttonSet.setBackgroundColor(
						ContextCompat.getColor(this, R.color.retro_green_bg_white)
				);
				binding.buttonSet.setTextColor(
						ContextCompat.getColor(this, R.color.on_secondary)
				);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onClick(View v) {
		if (clickUtil.isDisabled()) return;

		int id = v.getId();
		if (id == R.id.card_doodle) {
			IconUtil.start(binding.imageTheme);
			refreshSelectionTheme(Constants.THEME.DOODLE);
		} else if (id == R.id.card_neon) {
			IconUtil.start(binding.imageTheme);
			refreshSelectionTheme(Constants.THEME.NEON);
		} else if (id == R.id.card_geometric) {
			IconUtil.start(binding.imageTheme);
			refreshSelectionTheme(Constants.THEME.GEOMETRIC);
		} else if (id == R.id.card_black) {
			refreshSelectionVariant(Constants.VARIANT.BLACK);
		} else if (id == R.id.card_white) {
			refreshSelectionVariant(Constants.VARIANT.WHITE);
		} else if (id == R.id.card_orange) {
			refreshSelectionVariant(Constants.VARIANT.ORANGE);
		} else if (id == R.id.linear_night_mode) {
			binding.switchNightMode.setChecked(!binding.switchNightMode.isChecked());
		} else if (id == R.id.linear_follow_system) {
			IconUtil.start(binding.imageFollowSystem);
			if (binding.switchNightMode.isChecked()) {
				binding.switchFollowSystem.setChecked(!binding.switchFollowSystem.isChecked());
			}
		} else if (id == R.id.linear_developer) {
			IconUtil.start(binding.imageDeveloper);
			new Handler().postDelayed(
					() -> startActivity(
							new Intent(
									Intent.ACTION_VIEW,
									Uri.parse("http://play.google.com/store/apps/dev" +
											"?id=8122479227040208191"
									)
							)
					), 300
			);
		} else if (id == R.id.button_set) {
			startActivityForResult(
					new Intent()
							.setAction(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
							.putExtra(
									WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
									new ComponentName(
											"xyz.zedler.patrick.doodle",
											"xyz.zedler.patrick.doodle.service." +
													"LiveWallpaperService"
									)
							),
					1
			);
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		int id = buttonView.getId();
		if (id == R.id.switch_night_mode) {
			IconUtil.start(binding.imageNightMode);
			binding.switchFollowSystem.setEnabled(isChecked);
			binding.linearFollowSystem.setEnabled(isChecked);
			binding.linearFollowSystemContainer.animate()
					.alpha(isChecked ? 1 : 0.5f)
					.setDuration(200)
					.start();
			sharedPrefs.edit().putBoolean(Constants.PREF.NIGHT_MODE, isChecked).apply();
			new Handler().postDelayed(() -> binding.imageNightMode.setImageResource(
					isChecked
							? R.drawable.ic_round_night_mode_off_anim
							: R.drawable.ic_round_night_mode_on_anim

			), 300);
		} else if (id == R.id.switch_follow_system) {
			sharedPrefs.edit().putBoolean(Constants.PREF.FOLLOW_SYSTEM, isChecked).apply();
		}
	}

	private void refreshSelectionTheme(String selection) {
		MaterialCardView mcvSelected, mcv1, mcv2;
		switch (selection) {
			case Constants.THEME.NEON:
				mcvSelected = binding.cardNeon;
				mcv1 = binding.cardDoodle;
				mcv2 = binding.cardGeometric;
				break;
			case Constants.THEME.GEOMETRIC:
				mcvSelected = binding.cardGeometric;
				mcv1 = binding.cardDoodle;
				mcv2 = binding.cardNeon;
				break;
			default:
				mcvSelected = binding.cardDoodle;
				mcv1 = binding.cardGeometric;
				mcv2 = binding.cardNeon;
				break;
		}
		mcvSelected.setStrokeColor(ContextCompat.getColor(this, R.color.secondary));
		mcvSelected.setChecked(true);
		mcv1.setStrokeColor(ContextCompat.getColor(this, R.color.stroke));
		mcv1.setChecked(false);
		mcv2.setStrokeColor(ContextCompat.getColor(this, R.color.stroke));
		mcv2.setChecked(false);
		binding.linearVariant.animate()
				.alpha(selection.equals(Constants.THEME.DOODLE) ? 1 : 0.5f)
				.setDuration(200)
				.start();
		for (int i = 0; i < binding.linearVariant.getChildCount(); i++) {
			binding.linearVariant.getChildAt(i).setEnabled(
					selection.equals(Constants.THEME.DOODLE)
			);
		}
		sharedPrefs.edit().putString(Constants.PREF.THEME, selection).apply();
	}

	private void refreshSelectionVariant(String selection) {
		MaterialCardView mcvSelected, mcv1, mcv2;
		switch (selection) {
			case Constants.VARIANT.WHITE:
				mcvSelected = binding.cardWhite;
				mcv1 = binding.cardBlack;
				mcv2 = binding.cardOrange;
				break;
			case Constants.VARIANT.ORANGE:
				mcvSelected = binding.cardOrange;
				mcv1 = binding.cardBlack;
				mcv2 = binding.cardWhite;
				break;
			default:
				mcvSelected = binding.cardBlack;
				mcv1 = binding.cardWhite;
				mcv2 = binding.cardOrange;
				break;
		}
		mcvSelected.setStrokeColor(ContextCompat.getColor(this, R.color.secondary));
		mcvSelected.setChecked(true);
		mcv1.setStrokeColor(ContextCompat.getColor(this, R.color.stroke));
		mcv1.setChecked(false);
		mcv2.setStrokeColor(ContextCompat.getColor(this, R.color.stroke));
		mcv2.setChecked(false);
		sharedPrefs.edit().putString(Constants.PREF.VARIANT, selection).apply();
	}

	@SuppressWarnings("deprecation")
	private boolean isWallpaperServiceRunning() {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		if (manager != null) {
			for (ActivityManager.RunningServiceInfo service
					: manager.getRunningServices(Integer.MAX_VALUE)
			) {
				if (LiveWallpaperService.class.getName().equals(service.service.getClassName())) {
					return true;
				}
			}
		}
		return false;
	}
}
