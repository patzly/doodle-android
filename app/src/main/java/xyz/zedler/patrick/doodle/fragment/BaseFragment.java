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

package xyz.zedler.patrick.doodle.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View.OnClickListener;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import xyz.zedler.patrick.doodle.R;
import xyz.zedler.patrick.doodle.activity.MainActivity;
import xyz.zedler.patrick.doodle.util.ResUtil;
import xyz.zedler.patrick.doodle.util.ViewUtil;

public class BaseFragment extends Fragment {

  private MainActivity activity;
  private ViewUtil viewUtil;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    activity = (MainActivity) requireActivity();
    viewUtil = new ViewUtil();
  }

  public SharedPreferences getSharedPrefs() {
    return activity.getSharedPrefs();
  }

  public ViewUtil getViewUtil() {
    return viewUtil;
  }

  public void navigate(NavDirections directions) {
    activity.navigate(directions);
  }

  public void navigateToFragment(NavDirections directions, boolean useSliding) {
    activity.navigateToFragment(directions, useSliding);
  }

  public void navigateUp() {
    activity.navigateUp();
  }

  public void performHapticClick() {
    activity.performHapticClick();
  }

  public void performHapticHeavyClick() {
    activity.performHapticHeavyClick();
  }

  public OnClickListener getNavigationOnClickListener() {
    return v -> {
      if (viewUtil.isClickEnabled(v.getId())) {
        performHapticClick();
        navigateUp();
      }
    };
  }

  public Toolbar.OnMenuItemClickListener getOnMenuItemClickListener() {
    return item -> {
      int id = item.getItemId();
      if (viewUtil.isClickDisabled(id)) {
        return false;
      }
      performHapticClick();

      if (id == R.id.action_feedback) {
        activity.showFeedbackBottomSheet();
      } else if (id == R.id.action_help) {
        activity.showTextBottomSheet(R.raw.help, R.string.action_help);
      } else if (id == R.id.action_share) {
        ResUtil.share(activity, R.string.msg_share);
      }
      return true;
    };
  }
}