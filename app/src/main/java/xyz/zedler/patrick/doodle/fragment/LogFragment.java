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
 * Copyright (c) 2019-2025 by Patrick Zedler
 */

package xyz.zedler.patrick.doodle.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;
import com.google.android.material.snackbar.Snackbar;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import xyz.zedler.patrick.doodle.R;
import xyz.zedler.patrick.doodle.activity.MainActivity;
import xyz.zedler.patrick.doodle.behavior.ScrollBehavior;
import xyz.zedler.patrick.doodle.behavior.SystemBarBehavior;
import xyz.zedler.patrick.doodle.databinding.FragmentLogBinding;
import xyz.zedler.patrick.doodle.util.ViewUtil;

public class LogFragment extends BaseFragment implements OnClickListener {

  private final static String TAG = LogFragment.class.getSimpleName();

  private FragmentLogBinding binding;
  private MainActivity activity;
  private final Executor backgroundExecutor = Executors.newSingleThreadExecutor();

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState
  ) {
    binding = FragmentLogBinding.inflate(inflater, container, false);
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
    systemBarBehavior.setAppBar(binding.appBarLog);
    systemBarBehavior.setScroll(binding.scrollLog, binding.constraintLog);
    systemBarBehavior.setAdditionalBottomInset(activity.getFabTopEdgeDistance());
    systemBarBehavior.setUp();

    new ScrollBehavior().setUpScroll(binding.appBarLog, binding.scrollLog, true);

    binding.toolbarLog.setNavigationOnClickListener(getNavigationOnClickListener());
    binding.toolbarLog.setOnMenuItemClickListener(item -> {
      int id = item.getItemId();
      if (getViewUtil().isClickDisabled(id)) {
        return false;
      }
      performHapticClick();
      if (id == R.id.action_reload) {
        ViewUtil.startIcon(item.getIcon());
        loadLogcat(log -> binding.textLog.setText(log));
      }
      return true;
    });

    ViewUtil.setOnClickListeners(
        this,
        binding.buttonLogCopy,
        binding.buttonLogFeedback
    );

    new Handler(Looper.getMainLooper()).postDelayed(
        () -> loadLogcat(log -> binding.textLog.setText(log)), 10
    );
  }

  @Override
  public void onClick(View v) {
    int id = v.getId();
    if (getViewUtil().isClickDisabled(id)) {
      return;
    }
    performHapticClick();

    if (id == R.id.button_log_copy) {
      String logcat = binding.textLog.getText().toString();
      ClipboardManager cm = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
      cm.setPrimaryClip(ClipData.newPlainText(logcat, logcat));
      activity.showSnackbar(
          activity.getSnackbar(R.string.msg_copied_to_clipboard, Snackbar.LENGTH_SHORT)
      );
    } else if (id == R.id.button_log_feedback) {
      activity.showFeedbackBottomSheet();
    }
  }

  private void loadLogcat(Consumer<String> onLogLoaded) {
    backgroundExecutor.execute(() -> {
      StringBuilder log = new StringBuilder();
      try {
        Process process = Runtime.getRuntime().exec("logcat -d *:E -t 300 ");
        try (BufferedReader bufferedReader = new BufferedReader(
            new InputStreamReader(process.getInputStream()))) {
          String line;
          while ((line = bufferedReader.readLine()) != null) {
            log.append(line).append('\n');
          }
        } finally {
          process.destroy();
        }
      } catch (IOException ignored) {}
      activity.runOnUiThread(() -> onLogLoaded.accept(log.toString()));
    });
  }
}
