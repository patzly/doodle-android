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
 * Copyright (c) 2019-2024 by Patrick Zedler
 */

package xyz.zedler.patrick.doodle.fragment.dialog;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout.LayoutParams;

import androidx.annotation.NonNull;

import xyz.zedler.patrick.doodle.Constants.PREF;
import xyz.zedler.patrick.doodle.R;
import xyz.zedler.patrick.doodle.databinding.FragmentBottomsheetFeedbackBinding;
import xyz.zedler.patrick.doodle.util.ResUtil;
import xyz.zedler.patrick.doodle.util.ViewUtil;

public class FeedbackBottomSheetDialogFragment extends BaseBottomSheetDialogFragment
        implements OnClickListener {

    private static final String TAG = "FeedbackBottomSheet";

    private FragmentBottomsheetFeedbackBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle state) {
        binding = FragmentBottomsheetFeedbackBinding.inflate(inflater, container, false);

        binding.linearFeedbackRate.setBackground(
                ViewUtil.getRippleBgListItemSurface(requireContext())
        );
        binding.linearFeedbackEmail.setBackground(
                ViewUtil.getRippleBgListItemSurface(requireContext())
        );
        binding.linearFeedbackGithub.setBackground(
                ViewUtil.getRippleBgListItemSurface(requireContext())
        );
        binding.linearFeedbackShare.setBackground(
                ViewUtil.getRippleBgListItemSurface(requireContext())
        );

        ViewUtil.setOnClickListeners(
                this,
                binding.linearFeedbackRate,
                binding.linearFeedbackEmail,
                binding.linearFeedbackGithub,
                binding.linearFeedbackShare
        );

        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);

        if (getSharedPrefs().getInt(PREF.FEEDBACK_POP_UP_COUNT, 1) != 0) {
            getSharedPrefs().edit().putInt(PREF.FEEDBACK_POP_UP_COUNT, 0).apply();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (getViewUtil().isClickDisabled(id)) {
            return;
        }
        performHapticClick();

        if (id == R.id.linear_feedback_rate) {
            ViewUtil.startIcon(binding.imageFeedbackRate);
            Uri uri = Uri.parse(
                    "market://details?id=" + requireActivity().getApplicationContext().getPackageName()
            );
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK |
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            try {
                startActivity(goToMarket);
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
                        "http://play.google.com/store/apps/details?id="
                                + requireActivity().getApplicationContext().getPackageName()
                )));
            }
            dismiss();
        } else if (id == R.id.linear_feedback_email) {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(
                    Uri.parse(
                            "mailto:"
                                    + getString(R.string.app_mail)
                                    + "?subject=" + Uri.encode("Feedback@Doodle")
                    )
            );
            startActivity(Intent.createChooser(intent, getString(R.string.action_send_feedback)));
            dismiss();
        } else if (id == R.id.linear_feedback_github) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.app_github))));
            dismiss();
        } else if (id == R.id.linear_feedback_share) {
            ResUtil.share(requireContext(), R.string.msg_share);
            dismiss();
        }
    }

    @Override
    public void applyBottomInset(int bottom) {
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, bottom);
        binding.linearFeedbackContainer.setLayoutParams(params);
    }

    @NonNull
    @Override
    public String toString() {
        return TAG;
    }
}
