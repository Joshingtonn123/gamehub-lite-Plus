package com.xj.winemu.sidebar;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class BhFrameGenDialog extends Dialog {
    private final BhFrameGenSettings settings;

    public BhFrameGenDialog(Context context) {
        super(context);
        settings = BhFrameGenSettings.load(context);
    }

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        Window w = getWindow();
        if (w != null) {
            w.requestFeature(Window.FEATURE_NO_TITLE);
            WindowManager.LayoutParams lp = w.getAttributes();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            lp.dimAmount = 0.6f;
            lp.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            w.setAttributes(lp);
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        setContentView(buildView());
    }

    private View buildView() {
        Context c = getContext();
        FrameLayout root = new FrameLayout(c);
        LinearLayout panel = new LinearLayout(c);
        panel.setOrientation(LinearLayout.VERTICAL);
        FrameLayout.LayoutParams panelLp = new FrameLayout.LayoutParams(dp(320),
                ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.END | Gravity.CENTER_VERTICAL);
        panelLp.rightMargin = dp(24);
        panel.setLayoutParams(panelLp);
        panel.setPadding(dp(16), dp(16), dp(16), dp(12));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#ff1f1f24"));
        bg.setCornerRadius(dp(10));
        panel.setBackground(bg);
        root.addView(panel);

        TextView title = new TextView(c);
        title.setText("Frame Generation");
        title.setTextColor(Color.WHITE);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        title.setGravity(Gravity.CENTER);
        panel.addView(title);

        TextView label = new TextView(c);
        label.setText("Frame multiplier");
        label.setTextColor(Color.WHITE);
        label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        LinearLayout.LayoutParams labelLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        labelLp.topMargin = dp(18);
        panel.addView(label, labelLp);

        final TextView value = new TextView(c);
        value.setTextColor(Color.LTGRAY);
        value.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        value.setGravity(Gravity.END);
        value.setText(settings.multiplier + "x");
        panel.addView(value);

        SeekBar slider = new SeekBar(c);
        slider.setMax(2);
        slider.setProgress(settings.multiplier - 2);
        panel.addView(slider, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
                int multiplier = progress + 2;
                value.setText(multiplier + "x");
                if (fromUser) {
                    settings.multiplier = multiplier;
                    settings.save(getContext());
                    BhFrameGenWriter.writeMultiplier(
                            BhFrameGenWriter.resolveControlPath(getContext()), multiplier);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar bar) {}
            @Override public void onStopTrackingTouch(SeekBar bar) {}
        });

        LinearLayout ticks = new LinearLayout(c);
        ticks.setOrientation(LinearLayout.HORIZONTAL);
        for (int n = 2; n <= 4; n++) {
            TextView t = new TextView(c);
            t.setText(n + "x");
            t.setTextColor(Color.LTGRAY);
            t.setGravity(Gravity.CENTER);
            ticks.addView(t, new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        }
        panel.addView(ticks);

        TextView close = new TextView(c);
        close.setText("Close");
        close.setTextColor(Color.WHITE);
        close.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        close.setGravity(Gravity.CENTER);
        close.setBackgroundColor(Color.parseColor("#ff3b82f6"));
        LinearLayout.LayoutParams closeLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(36));
        closeLp.topMargin = dp(18);
        panel.addView(close, closeLp);
        close.setOnClickListener(v -> dismiss());
        return root;
    }

    private int dp(int value) {
        return (int) (value * getContext().getResources().getDisplayMetrics().density + 0.5f);
    }

    public static void show(Context ctx) {
        try {
            BhFrameGenDialog d = new BhFrameGenDialog(ctx);
            d.setCancelable(true);
            d.show();
        } catch (Exception ignored) {}
    }
}
