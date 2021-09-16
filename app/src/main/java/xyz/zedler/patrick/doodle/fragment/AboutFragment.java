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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RawRes;
import androidx.annotation.StringRes;
import xyz.zedler.patrick.doodle.R;
import xyz.zedler.patrick.doodle.activity.MainActivity;
import xyz.zedler.patrick.doodle.behavior.ScrollBehavior;
import xyz.zedler.patrick.doodle.behavior.SystemBarBehavior;
import xyz.zedler.patrick.doodle.databinding.FragmentAboutBinding;
import xyz.zedler.patrick.doodle.util.ResUtil;
import xyz.zedler.patrick.doodle.util.ViewUtil;

public class AboutFragment extends BaseFragment implements OnClickListener {

  private FragmentAboutBinding binding;
  private MainActivity activity;

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState
  ) {
    binding = FragmentAboutBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    activity = (MainActivity) requireActivity();

    SystemBarBehavior systemBarBehavior = new SystemBarBehavior(activity);
    systemBarBehavior.setAppBar(binding.appBarAbout);
    systemBarBehavior.setScroll(binding.scrollAbout, binding.linearAboutContainer);
    systemBarBehavior.setAdditionalBottomInset(activity.getFabTopEdgeDistance());
    systemBarBehavior.setUp();

    new ScrollBehavior(activity).setUpScroll(
        binding.appBarAbout, binding.scrollAbout, true
    );

    binding.toolbarAbout.setOnMenuItemClickListener(item -> {
      int id = item.getItemId();
      if (id == R.id.action_share) {
        ResUtil.share(activity, R.string.msg_share);
        performHapticClick();
        return true;
      } else if (id == R.id.action_feedback) {
        performHapticClick();
        getNavController().navigate(AboutFragmentDirections.actionAboutToFeedbackDialog());
        return true;
      } else {
        return false;
      }
    });

    ViewUtil.setOnClickListeners(
        this,
        binding.frameAboutBack,
        binding.linearAboutChangelog,
        binding.linearAboutDeveloper,
        binding.linearAboutVending,
        binding.linearAboutGithub,
        binding.linearAboutTranslation,
        binding.linearAboutLicenseJost,
        binding.linearAboutLicenseMaterialComponents,
        binding.linearAboutLicenseMaterialIcons
    );
  }

  @Override
  public void onClick(View v) {
    int id = v.getId();
    if (id == R.id.frame_about_back && getViewUtil().isClickEnabled()) {
      performHapticClick();
      getNavController().navigateUp();
    } else if (id == R.id.linear_about_changelog && getViewUtil().isClickEnabled()) {
      performHapticClick();
      getNavController().navigate(AboutFragmentDirections.actionAboutToChangelogDialog());
      ViewUtil.startIcon(binding.imageAboutChangelog);
    } else if (id == R.id.linear_about_developer && getViewUtil().isClickEnabled()) {
      performHapticClick();
      ViewUtil.startIcon(binding.imageAboutDeveloper);
      new Handler(Looper.getMainLooper()).postDelayed(
          () -> startActivity(
              new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.app_website)))
          ), 300
      );
    } else if (id == R.id.linear_about_vending && getViewUtil().isClickEnabled()) {
      performHapticClick();
      ViewUtil.startIcon(binding.imageAboutVending);
      new Handler(Looper.getMainLooper()).postDelayed(
          () -> startActivity(
              new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.app_vending)))
          ), 300
      );
    } else if (id == R.id.linear_about_github && getViewUtil().isClickEnabled()) {
      performHapticClick();
      startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.app_github))));
    } else if (id == R.id.linear_about_translation && getViewUtil().isClickEnabled()) {
      performHapticClick();
      startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.app_translate))));
    } else if (id == R.id.linear_about_license_jost && getViewUtil().isClickEnabled()) {
      performHapticClick();
      ViewUtil.startIcon(binding.imageAboutLicenseJost);
      showLicense(R.raw.license_ofl, R.string.license_jost, R.string.license_jost_link);
    } else if (
        id == R.id.linear_about_license_material_components && getViewUtil().isClickEnabled()
    ) {
      performHapticClick();
      ViewUtil.startIcon(binding.imageAboutLicenseMaterialComponents);
      showLicense(
          R.raw.license_apache,
          R.string.license_material_components,
          R.string.license_material_components_link
      );
    } else if (id == R.id.linear_about_license_material_icons && getViewUtil().isClickEnabled()) {
      performHapticClick();
      ViewUtil.startIcon(binding.imageAboutLicenseMaterialIcons);
      showLicense(
          R.raw.license_apache,
          R.string.license_material_icons,
          R.string.license_material_icons_link
      );
    }
  }

  private void showLicense(@RawRes int file, @StringRes int title, @StringRes int link) {
    AboutFragmentDirections.ActionAboutToTextDialog action
        = AboutFragmentDirections.actionAboutToTextDialog();
    action.setFile(file);
    action.setTitle(title);
    action.setLink(link);
    getNavController().navigate(action);
  }
}