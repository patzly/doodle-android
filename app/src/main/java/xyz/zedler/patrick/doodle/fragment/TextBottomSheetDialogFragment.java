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
 * Copyright (c) 2020-2021 by Patrick Zedler
 */

package xyz.zedler.patrick.doodle.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout.LayoutParams;
import androidx.annotation.NonNull;
import xyz.zedler.patrick.doodle.Constants;
import xyz.zedler.patrick.doodle.databinding.FragmentBottomsheetTextBinding;
import xyz.zedler.patrick.doodle.util.ResUtil;
import xyz.zedler.patrick.doodle.util.ViewUtil;

public class TextBottomSheetDialogFragment extends BaseBottomSheetDialogFragment {

  private final static String TAG = "TextBottomSheet";

  private FragmentBottomsheetTextBinding binding;

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle state) {
    binding = FragmentBottomsheetTextBinding.inflate(inflater, container, false);

    Context context = getContext();
    Bundle bundle = getArguments();
    assert context != null && bundle != null;

    binding.textTextTitle.setText(
        bundle.getString(Constants.EXTRA.TITLE)
    );

    String link = bundle.getString(Constants.EXTRA.LINK);
    if (link != null) {
      binding.frameTextOpenLink.setOnClickListener(v -> {
        ViewUtil.startIcon(binding.imageTextOpenLink);
        new Handler(Looper.getMainLooper()).postDelayed(
            () -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link))),
            500
        );
      });
    } else {
      binding.textTextTitle.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
      binding.frameTextOpenLink.setVisibility(View.GONE);
    }

    binding.textText.setText(
        ResUtil.getRawText(context, bundle.getInt(Constants.EXTRA.FILE))
    );

    return binding.getRoot();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    binding = null;
  }

  @Override
  public void applyBottomInset(int bottom) {
    LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    params.setMargins(0, 0, 0, bottom);
    binding.textText.setLayoutParams(params);
  }

  @NonNull
  @Override
  public String toString() {
    return TAG;
  }
}
