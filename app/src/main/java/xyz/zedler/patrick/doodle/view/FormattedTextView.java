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

package xyz.zedler.patrick.doodle.view;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.text.HtmlCompat;
import androidx.core.widget.TextViewCompat;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.divider.MaterialDivider;
import xyz.zedler.patrick.doodle.R;
import xyz.zedler.patrick.doodle.util.ResUtil;
import xyz.zedler.patrick.doodle.util.SystemUiUtil;

public class FormattedTextView extends LinearLayout {

  private final Context context;

  public FormattedTextView(Context context) {
    super(context);
    this.context = context;
    init();
  }

  public FormattedTextView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    this.context = context;
    init();
  }

  public void setText(String text, String... highlights) {
    removeAllViews();

    for (String part : text.split("\n\n")) {
      for (String highlight : highlights) {
        part = part.replaceAll(highlight, "<b>" + highlight + "</b>");
      }
      part = part.replaceAll("\n", "<br/>");

      if (part.startsWith("#")) {
        String[] h = part.split(" ");
        addView(getHeadline(h[0].length(), part.substring(h[0].length() + 1)));
      } else if (part.startsWith("- ")) {
        String[] bullets = part.trim().split("- ");
        for (int i = 1; i < bullets.length; i++) {
          addView(getBullet(bullets[i], i == bullets.length - 1));
        }
      } else if (part.startsWith("? ")) {
        addView(getMessage(part.substring(2), false));
      } else if (part.startsWith("! ")) {
        addView(getMessage(part.substring(2), true));
      } else if (part.startsWith("---")) {
        addView(getDivider());
      } else {
        addView(getParagraph(part));
      }
    }
  }

  private void init() {
    setOrientation(VERTICAL);
    int padding = SystemUiUtil.dpToPx(context, 16);
    setPadding(padding, padding, padding, 0);
  }

  private TextView getParagraph(String text) {
    TextView textView = new TextView(
        context, null, 0, R.style.Widget_Doodle_TextView_Paragraph
    );
    textView.setLayoutParams(getVerticalLayoutParams(16));
    textView.setTextColor(ResUtil.getColorAttr(context, R.attr.colorOnBackground));
    textView.setText(HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_LEGACY));
    return textView;
  }

  private TextView getHeadline(int h, String title) {
    TextView textView = new TextView(
        context, null, 0, R.style.Widget_Doodle_TextView
    );
    textView.setLayoutParams(getVerticalLayoutParams(16));
    textView.setText(HtmlCompat.fromHtml(title, HtmlCompat.FROM_HTML_MODE_LEGACY));
    boolean isMedium = false;
    int resId;
    switch (h) {
      case 1:
        resId = R.style.TextAppearance_Material3_HeadlineLarge;
        break;
      case 2:
        resId = R.style.TextAppearance_Material3_HeadlineMedium;
        break;
      case 3:
        resId = R.style.TextAppearance_Material3_HeadlineSmall;
        break;
      case 4:
        resId = R.style.TextAppearance_Material3_TitleLarge;
        break;
      default:
        resId = R.style.TextAppearance_Material3_TitleMedium;
        isMedium = true;
        break;
    }
    TextViewCompat.setTextAppearance(textView, resId);
    textView.setTypeface(
        ResourcesCompat.getFont(context, isMedium ? R.font.jost_medium : R.font.jost_book)
    );
    textView.setTextColor(ResUtil.getColorAttr(context, R.attr.colorOnBackground));
    return textView;
  }

  private View getDivider() {
    MaterialDivider divider = new MaterialDivider(context);
    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
        SystemUiUtil.dpToPx(context, 56), ViewGroup.LayoutParams.WRAP_CONTENT
    );
    layoutParams.setMargins(
        0, SystemUiUtil.dpToPx(context, 8), 0, SystemUiUtil.dpToPx(context, 24)
    );
    divider.setLayoutParams(layoutParams);
    return divider;
  }

  private LinearLayout getBullet(String text, boolean isLast) {
    int bulletSize = SystemUiUtil.dpToPx(context, 4);

    View viewBullet = new View(context);
    FrameLayout.LayoutParams paramsBullet = new FrameLayout.LayoutParams(bulletSize, bulletSize);
    paramsBullet.rightMargin = SystemUiUtil.dpToPx(context, 8);
    paramsBullet.leftMargin = SystemUiUtil.dpToPx(context, 8);
    paramsBullet.gravity = Gravity.CENTER_VERTICAL;
    viewBullet.setLayoutParams(paramsBullet);

    GradientDrawable shape = new GradientDrawable();
    shape.setShape(GradientDrawable.OVAL);
    shape.setSize(bulletSize, bulletSize);
    shape.setColor(ResUtil.getColorAttr(context, R.attr.colorOnBackground));
    viewBullet.setBackground(shape);

    TextView textViewHeight = new TextView(
        context, null, 0, R.style.Widget_Doodle_TextView
    );
    textViewHeight.setLayoutParams(
        new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
    );
    textViewHeight.setText("E");
    textViewHeight.setVisibility(INVISIBLE);

    FrameLayout frameLayout = new FrameLayout(context);
    frameLayout.setLayoutParams(
        new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
    );
    frameLayout.addView(viewBullet);
    frameLayout.addView(textViewHeight);

    TextView textView = new TextView(
        context, null, 0, R.style.Widget_Doodle_TextView_Paragraph
    );
    LinearLayout.LayoutParams paramsText = new LinearLayout.LayoutParams(
        0, ViewGroup.LayoutParams.WRAP_CONTENT
    );
    paramsText.weight = 1;
    textView.setLayoutParams(paramsText);

    if (text.trim().endsWith("<br/>")) {
      text = text.trim().substring(0, text.length() - 5);
    }
    textView.setText(HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_LEGACY));


    LinearLayout linearLayout = new LinearLayout(context);
    linearLayout.setLayoutParams(
        getVerticalLayoutParams(isLast ? 16 : 8)
    );

    linearLayout.addView(frameLayout);
    linearLayout.addView(textView);

    // Long texts are not translated
    /*if (ResUtil.isLayoutRtl(context)) {
      linearLayout.addView(textView);
      linearLayout.addView(frameLayout);
    } else {
      linearLayout.addView(frameLayout);
      linearLayout.addView(textView);
    }*/
    return linearLayout;
  }

  private MaterialCardView getMessage(String text, boolean useErrorColors) {
    int colorSurface = ResUtil.getColorAttr(
        context, useErrorColors ? R.attr.colorErrorContainer : R.attr.colorSurfaceVariant
    );
    int colorOnSurface = ResUtil.getColorAttr(
        context, useErrorColors ? R.attr.colorOnErrorContainer : R.attr.colorOnSurfaceVariant
    );
    MaterialCardView cardView = new MaterialCardView(context);
    cardView.setLayoutParams(getVerticalLayoutParams(16));
    int padding = SystemUiUtil.dpToPx(context, 16);
    cardView.setContentPadding(padding, padding, padding, padding);
    cardView.setCardBackgroundColor(colorSurface);
    cardView.setStrokeWidth(0);
    cardView.setRadius(padding);

    TextView textView = getParagraph(text);
    textView.setLayoutParams(getVerticalLayoutParams(0));
    textView.setTextColor(colorOnSurface);
    cardView.addView(textView);
    return cardView;
  }

  private LinearLayout.LayoutParams getVerticalLayoutParams(int bottom) {
    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
    );
    layoutParams.setMargins(0, 0, 0, SystemUiUtil.dpToPx(context, bottom));
    return layoutParams;
  }
}
