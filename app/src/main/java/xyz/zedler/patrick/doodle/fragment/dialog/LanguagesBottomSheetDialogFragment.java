/*
 * This file is part of Doodle Android.
 *
 * Doodle Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Doodle Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Doodle Android. If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (c) 2019-2022 by Patrick Zedler
 */

package xyz.zedler.patrick.doodle.fragment.dialog;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import java.util.Locale;
import java.util.Objects;
import xyz.zedler.patrick.doodle.Constants;
import xyz.zedler.patrick.doodle.Constants.DEF;
import xyz.zedler.patrick.doodle.Constants.PREF;
import xyz.zedler.patrick.doodle.R;
import xyz.zedler.patrick.doodle.activity.MainActivity;
import xyz.zedler.patrick.doodle.adapter.LanguageAdapter;
import xyz.zedler.patrick.doodle.databinding.FragmentBottomsheetLanguagesBinding;
import xyz.zedler.patrick.doodle.fragment.OtherFragment;
import xyz.zedler.patrick.doodle.model.Language;
import xyz.zedler.patrick.doodle.util.LocaleUtil;
import xyz.zedler.patrick.doodle.util.SystemUiUtil;

public class LanguagesBottomSheetDialogFragment extends BaseBottomSheetDialogFragment
    implements LanguageAdapter.LanguageAdapterListener {

  private static final String TAG = "LanguagesBottomSheet";

  private FragmentBottomsheetLanguagesBinding binding;
  private MainActivity activity;

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater,
      ViewGroup container,
      Bundle savedInstanceState) {
    binding = FragmentBottomsheetLanguagesBinding.inflate(
        inflater, container, false
    );

    activity = (MainActivity) requireActivity();

    binding.textLanguagesTitle.setText(getString(R.string.action_language_select));
    binding.textLanguagesDescription.setText(getString(R.string.other_language_description));
    binding.textLanguagesDescription.setVisibility(View.VISIBLE);

    binding.recyclerLanguages.setLayoutManager(
        new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
    );
    binding.recyclerLanguages.setAdapter(
        new LanguageAdapter(LocaleUtil.getLanguages(activity), getLanguageCode(), this)
    );

    return binding.getRoot();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    binding = null;
  }

  @Override
  public void onItemRowClicked(@Nullable Language language) {
    String previousCode = getSharedPrefs().getString(PREF.LANGUAGE, DEF.LANGUAGE);
    String selectedCode = language != null ? language.getCode() : null;
    getSharedPrefs().edit().putString(Constants.PREF.LANGUAGE, selectedCode).apply();

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (!Objects.equals(previousCode, selectedCode)) {
        performHapticClick();
        dismiss();
        activity.restartToApply(150);
      }
    } else {
      if (Objects.equals(previousCode, selectedCode)) {
        return;
      } else if (previousCode == null || selectedCode == null) {
        Locale localeDevice = LocaleUtil.getNearestSupportedLocale(
            activity, LocaleUtil.getDeviceLocale()
        );
        String codeToCompare = previousCode == null ? selectedCode : previousCode;
        if (Objects.equals(localeDevice.toString(), codeToCompare)) {
          OtherFragment fragment = (OtherFragment) activity.getCurrentFragment();
          fragment.setLanguage(language);
          dismiss();
        } else {
          dismiss();
          activity.restartToApply(150);
        }
      } else {
        dismiss();
        activity.restartToApply(150);
      }
      performHapticClick();
    }

    if (Objects.equals(previousCode, selectedCode)) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        // Simply restart to apply language change
        performHapticClick();
        dismiss();
        activity.restartToApply(150);
      }
      return;
    } else if (previousCode == null || selectedCode == null) {
      Locale localeDevice = LocaleUtil.getNearestSupportedLocale(
          activity, LocaleUtil.getDeviceLocale()
      );
      String codeToCompare = previousCode == null ? selectedCode : previousCode;
      if (Objects.equals(localeDevice.toString(), codeToCompare)) {
        OtherFragment fragment = (OtherFragment) activity.getCurrentFragment();
        fragment.setLanguage(language);
        dismiss();
      } else {
        dismiss();
        activity.restartToApply(150);
      }
    } else {
      dismiss();
      activity.restartToApply(150);
    }
    performHapticClick();
  }

  private String getLanguageCode() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      LocaleListCompat locales = AppCompatDelegate.getApplicationLocales();
      if (!locales.isEmpty()) {
        Locale locale = locales.get(0);
        if (locale != null) {
          return LocaleUtil.getNearestSupportedLocale(activity, locale).toLanguageTag();
        } else {
          return Locale.getDefault().toLanguageTag();
        }
      } else {
        return null;
      }
    } else {
      return getSharedPrefs().getString(PREF.LANGUAGE, DEF.LANGUAGE);
    }
  }

  @Override
  public void applyBottomInset(int bottom) {
    binding.recyclerLanguages.setPadding(
        0, SystemUiUtil.dpToPx(requireContext(), 8),
        0, SystemUiUtil.dpToPx(requireContext(), 8) + bottom
    );
  }

  @NonNull
  @Override
  public String toString() {
    return TAG;
  }
}
