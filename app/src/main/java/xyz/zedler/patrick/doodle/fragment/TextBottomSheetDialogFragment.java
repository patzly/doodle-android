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
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import xyz.zedler.patrick.doodle.Constants;
import xyz.zedler.patrick.doodle.databinding.FragmentBottomsheetTextBinding;
import xyz.zedler.patrick.doodle.util.IconUtil;
import xyz.zedler.patrick.doodle.util.ResUtil;

public class TextBottomSheetDialogFragment extends BaseBottomSheetDialogFragment {

	private final static String TAG = "TextBottomSheetDialog";

	private FragmentBottomsheetTextBinding binding;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater,
							 ViewGroup container,
							 Bundle savedInstanceState) {
		binding = FragmentBottomsheetTextBinding.inflate(
				inflater, container, false
		);

		Context context = getContext();
		Bundle bundle = getArguments();
		assert context != null && bundle != null;

		binding.textTextTitle.setText(
				bundle.getString(Constants.EXTRA.TITLE)
		);

		String link = bundle.getString(Constants.EXTRA.LINK);
		if (link != null) {
			binding.frameTextOpenLink.setOnClickListener(v -> {
				IconUtil.start(binding.imageTextOpenLink);
				new Handler(Looper.getMainLooper()).postDelayed(
						() -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link))),
						500
				);
			});
		} else {
			binding.frameTextOpenLink.setVisibility(View.GONE);
		}

		binding.textText.setText(
				ResUtil.readFromFile(context, bundle.getString(Constants.EXTRA.FILE))
		);

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
