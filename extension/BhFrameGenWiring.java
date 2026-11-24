package com.xj.winemu.sidebar;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class BhFrameGenWiring {
    private static int id(Context c, String name) {
        return c.getResources().getIdentifier(name, "id", c.getPackageName());
    }

    public static void bindFromFragment(Object frag) {
        if (frag == null) return;
        try {
            Method getView = frag.getClass().getMethod("getView");
            Object v = getView.invoke(frag);
            if (v instanceof View) bind((View) v);
        } catch (Throwable ignored) {}
    }

    public static void bind(final View root) {
        if (root == null) return;
        final Context ctx = root.getContext();
        final View toggle = root.findViewById(id(ctx, "switch_frame_gen"));
        if (toggle == null) return;

        final BhFrameGenSettings settings = BhFrameGenSettings.load(ctx);
        setSwitch(toggle, settings.enabled);
        final ImageButton gear = ensureGear(ctx, toggle);
        if (gear != null) {
            gear.setVisibility(settings.enabled ? View.VISIBLE : View.GONE);
            gear.setOnClickListener(v -> BhFrameGenDialog.show(ctx));
        }

        try {
            Class<?> function0 = Class.forName("kotlin.jvm.functions.Function0");
            Object callback = Proxy.newProxyInstance(function0.getClassLoader(),
                    new Class<?>[]{function0}, new InvocationHandler() {
                        @Override public Object invoke(Object proxy, Method method, Object[] args) {
                            if (!"invoke".equals(method.getName())) return null;
                            BhFrameGenSettings current = BhFrameGenSettings.load(ctx);
                            current.enabled = !current.enabled;
                            current.save(ctx);
                            setSwitch(toggle, current.enabled);
                            BhFrameGenWriter.applyFromPrefs(ctx);
                            if (gear != null) gear.setVisibility(current.enabled ? View.VISIBLE : View.GONE);
                            return getKotlinUnit();
                        }
                    });
            Method setter = null;
            for (Method m : toggle.getClass().getMethods()) {
                if ("setClickListener".equals(m.getName()) && m.getParameterTypes().length == 1
                        && m.getParameterTypes()[0].isInterface()) {
                    setter = m;
                    break;
                }
            }
            if (setter != null) setter.invoke(toggle, callback);
        } catch (Throwable ignored) {}
    }

    private static void setSwitch(View toggle, boolean enabled) {
        try { toggle.getClass().getMethod("setSwitch", boolean.class).invoke(toggle, enabled); }
        catch (Exception ignored) {}
    }

    private static Object getKotlinUnit() {
        try { return Class.forName("kotlin.Unit").getField("INSTANCE").get(null); }
        catch (Exception ignored) { return null; }
    }

    private static ImageButton ensureGear(Context ctx, View toggle) {
        View existing = toggle.getRootView().findViewById(id(ctx, "btn_frame_gen_settings"));
        if (existing instanceof ImageButton) return (ImageButton) existing;
        if (!(toggle.getParent() instanceof ViewGroup)) return null;

        ImageButton button = new ImageButton(ctx);
        int gearId = id(ctx, "btn_frame_gen_settings");
        if (gearId != 0) button.setId(gearId);
        int drawable = ctx.getResources().getIdentifier(
                "winemu_ic_setting_focus_white", "drawable", ctx.getPackageName());
        if (drawable != 0) button.setImageResource(drawable);
        button.setContentDescription("Frame Generation settings");
        button.setBackgroundResource(android.R.drawable.btn_default);
        int size = (int) (40 * ctx.getResources().getDisplayMetrics().density + 0.5f);
        ViewGroup parent = (ViewGroup) toggle.getParent();
        ViewGroup.LayoutParams lp;
        if (parent instanceof LinearLayout) {
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(size, size);
            p.rightMargin = (int) (8 * ctx.getResources().getDisplayMetrics().density + 0.5f);
            lp = p;
        } else {
            lp = new ViewGroup.LayoutParams(size, size);
        }
        parent.addView(button, lp);
        return button;
    }
}
