package xyz.zedler.patrick.doodle.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

import xyz.zedler.patrick.doodle.R;

public class TextBottomSheetDialogFragment extends BottomSheetDialogFragment {

	private final static boolean DEBUG = false;
	private final static String TAG = "TextBottomSheetDialog";

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		return new BottomSheetDialog(requireContext(), R.style.Theme_Doodle_BottomSheetDialog);
	}

	@Override
	public View onCreateView(
			LayoutInflater inflater,
			ViewGroup container,
			Bundle savedInstanceState
	) {
		View view = inflater.inflate(
				R.layout.fragment_bottomsheet_text,
				container,
				false
		);

		Context context = getContext();
		Bundle bundle = getArguments();
		assert context != null && bundle != null;

		String file = bundle.getString("file") + ".txt";
		String fileLocalized = bundle.getString("file") + "-" + Locale.getDefault().getLanguage() + ".txt";
		if(readFromFile(context, fileLocalized) != null) file = fileLocalized;

		Log.i(TAG, "onCreateView: " + fileLocalized);

		((TextView) view.findViewById(R.id.text_text_title)).setText(bundle.getString("title"));

		FrameLayout frameLayoutLink = view.findViewById(R.id.frame_text_open_link);
		String link = bundle.getString("link");
		if (link != null) {
			frameLayoutLink.setOnClickListener(v -> {
				((Animatable) ((ImageView) view.findViewById(R.id.image_text_open_link)).getDrawable()).start();
				new Handler().postDelayed(
						() -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link))),
						500
				);
			});
		} else {
			frameLayoutLink.setVisibility(View.GONE);
		}

		((TextView) view.findViewById(R.id.text_text)).setText(readFromFile(context, file));

		return view;
	}

	private String readFromFile(Context context, String file) {
		StringBuilder text = new StringBuilder();
		try {
			InputStream inputStream = context.getAssets().open(file);
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			for(String line; (line = bufferedReader.readLine()) != null;) {
				text.append(line).append('\n');
			}
			text.deleteCharAt(text.length() - 1);
			inputStream.close();
		} catch (FileNotFoundException e) {
			if(DEBUG) Log.e(TAG, "readFromFile: \"" + file + "\" not found!");
			return null;
		} catch (Exception e) {
			if(DEBUG) Log.e(TAG, "readFromFile: " + e.toString());
		}
		return text.toString();
	}
}
