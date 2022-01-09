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

import android.app.WallpaperManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout.LayoutParams;
import androidx.annotation.NonNull;
import xyz.zedler.patrick.doodle.R;
import xyz.zedler.patrick.doodle.activity.MainActivity;
import xyz.zedler.patrick.doodle.databinding.FragmentBottomsheetSetBinding;
import xyz.zedler.patrick.doodle.util.ViewUtil;

public class SetBottomSheetDialogFragment extends BaseBottomSheetDialogFragment
    implements OnClickListener {

  private static final String TAG = "SetBottomSheet";

  private FragmentBottomsheetSetBinding binding;
  private MainActivity activity;

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle state) {
    binding = FragmentBottomsheetSetBinding.inflate(inflater, container, false);

    activity = (MainActivity) requireActivity();

    ViewUtil.setOnClickListeners(
        this,
        binding.buttonSetDirectly,
        binding.buttonSetPicker
    );

    return binding.getRoot();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    binding = null;
  }

  @Override
  public void onClick(View v) {
    int id = v.getId();
    if (id == R.id.button_set_directly && getViewUtil().isClickEnabled()) {
      performHapticClick();
      activity.setWallpaperDirectly();
      dismiss();
    } else if (id == R.id.button_set_picker && getViewUtil().isClickEnabled()) {
      performHapticClick();
      try {
        Intent intent = new Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
      } catch (Exception e) {
        activity.showSnackbar(R.string.msg_picker_missing);
        Log.e(TAG, "onClick: ", e);
      }
      dismiss();
    }
  }

  @Override
  public void applyBottomInset(int bottom) {
    LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    params.setMargins(0, 0, 0, bottom);
    binding.linearSetContainer.setLayoutParams(params);
  }

  @NonNull
  @Override
  public String toString() {
    return TAG;
  }
}
