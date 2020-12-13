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

import java.util.Objects;

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
								: R.color.secondary
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
				sharedPrefs.getBoolean("night_mode", true)
		);
		binding.switchFollowSystem.setChecked(
				sharedPrefs.getBoolean("follow_system", true)
		);
		binding.switchFollowSystem.setEnabled(binding.switchNightMode.isChecked());
		boolean active = Objects.requireNonNull(
				sharedPrefs.getString("theme", "doodle")
		).equals("doodle");
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
				sharedPrefs.getBoolean("night_mode", true)
						? R.drawable.ic_round_night_mode_off_anim
						: R.drawable.ic_round_night_mode_on_anim

		);

		switch (sharedPrefs.getInt("parallax", 100)) {
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
				sharedPrefs.edit().putInt("parallax", 0).apply();
			} else if (checkedId == R.id.radio_little) {
				sharedPrefs.edit().putInt("parallax", 100).apply();
			} else if (checkedId == R.id.radio_much) {
				sharedPrefs.edit().putInt("parallax", 200).apply();
			}
		});

		binding.radioGroupSize.check(
				sharedPrefs.getBoolean("size_big", false)
						? R.id.radio_big
						: R.id.radio_default
		);
		binding.radioGroupSize.setOnCheckedChangeListener((RadioGroup group, int checkedId) -> {
			IconUtil.start(binding.imageSize);
			if (checkedId == R.id.radio_default) {
				sharedPrefs.edit().putBoolean("size_big", false).apply();
			} else if (checkedId == R.id.radio_big) {
				sharedPrefs.edit().putBoolean("size_big", true).apply();
			}
		});

		refreshSelectionTheme(sharedPrefs.getString("theme", "doodle"));
		refreshSelectionVariant(sharedPrefs.getString("variant", "black"));

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
					ContextCompat.getColor(this, R.color.secondary)
			);
			binding.buttonSet.setTextColor(
					ContextCompat.getColor(this, R.color.on_secondary)
			);
		}

		sharedPrefs.edit().putBoolean("should_refresh", true).apply();
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
						ContextCompat.getColor(this, R.color.secondary)
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
			refreshSelectionTheme("doodle");
		} else if (id == R.id.card_neon) {
			IconUtil.start(binding.imageTheme);
			refreshSelectionTheme("neon");
		} else if (id == R.id.card_geometric) {
			IconUtil.start(binding.imageTheme);
			refreshSelectionTheme("geometric");
		} else if (id == R.id.card_black) {
			refreshSelectionVariant("black");
		} else if (id == R.id.card_white) {
			refreshSelectionVariant("white");
		} else if (id == R.id.card_orange) {
			refreshSelectionVariant("orange");
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
			sharedPrefs.edit().putBoolean("night_mode", isChecked).apply();
			new Handler().postDelayed(() -> binding.imageNightMode.setImageResource(
					isChecked
							? R.drawable.ic_round_night_mode_off_anim
							: R.drawable.ic_round_night_mode_on_anim

			), 300);
		} else if (id == R.id.switch_follow_system) {
			sharedPrefs.edit().putBoolean("follow_system", isChecked).apply();
		}
	}

	private void refreshSelectionTheme(String selection) {
		MaterialCardView mcvSelected, mcv1, mcv2;
		switch (selection) {
			case "neon":
				mcvSelected = binding.cardNeon;
				mcv1 = binding.cardDoodle;
				mcv2 = binding.cardGeometric;
				break;
			case "geometric":
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
				.alpha(selection.equals("doodle") ? 1 : 0.5f)
				.setDuration(200)
				.start();
		for (int i = 0; i < binding.linearVariant.getChildCount(); i++) {
			binding.linearVariant.getChildAt(i).setEnabled(selection.equals("doodle"));
		}
		sharedPrefs.edit().putString("theme", selection).apply();
	}

	private void refreshSelectionVariant(String selection) {
		MaterialCardView mcvSelected, mcv1, mcv2;
		switch (selection) {
			case "white":
				mcvSelected = binding.cardWhite;
				mcv1 = binding.cardBlack;
				mcv2 = binding.cardOrange;
				break;
			case "orange":
				mcvSelected = binding.cardOrange;
				mcv1 = binding.cardBlack;
				mcv2 = binding.cardOrange;
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
		sharedPrefs.edit().putString("variant", selection).apply();
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
