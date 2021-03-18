/*
 * This file is part of Doodle Android.
 * Doodle Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * Doodle Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with Doodle Android. If not, see <http://www.gnu.org/licenses/>.
 * Copyright (c) 2021 by Patrick Zedler
 */

package xyz.zedler.patrick.doodle.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import xyz.zedler.patrick.doodle.R;
import xyz.zedler.patrick.doodle.databinding.FragmentBottomsheetTextBinding;
import xyz.zedler.patrick.doodle.util.BulletUtil;
import xyz.zedler.patrick.doodle.util.ResUtil;

public class ChangelogBottomSheetDialogFragment extends BaseBottomSheetDialogFragment {

  private final static String TAG = "ChangelogBottomSheetDialog";

  private FragmentBottomsheetTextBinding binding;

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater,
      ViewGroup container,
      Bundle savedInstanceState) {
    binding = FragmentBottomsheetTextBinding.inflate(
        inflater, container, false
    );

    Context context = getContext();
    assert context != null;

    binding.textTextTitle.setText(R.string.info_changelog);

    binding.textText.setText(
        BulletUtil.makeBulletList(
            getContext(),
            6,
            2,
            "- ",
            ResUtil.readFromFile(getContext(), "changelog"),
            getResources().getStringArray(R.array.changelog_highlights)
        ),
        TextView.BufferType.SPANNABLE
    );

    binding.frameTextOpenLink.setVisibility(View.GONE);

    return binding.getRoot();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    binding = null;
  }

  @NonNull
  @Override
  public String toString() {
    return TAG;
  }
}
