package com.xj.winemu.sidebar;

import android.content.Context;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class BhFrameGenWriter {
    public static void applyFromPrefs(Context ctx) {
        BhFrameGenSettings s = BhFrameGenSettings.load(ctx);
        write(resolveControlPath(ctx), s);
        BhVulkanIcdWriter.ensureIcdJson(ctx);
    }

    public static void applyFromPrefsNoContext() {
        try {
            Class<?> at = Class.forName("android.app.ActivityThread");
            Object app = at.getMethod("currentApplication").invoke(null);
            if (app instanceof Context) applyFromPrefs((Context) app);
        } catch (Exception ignored) {}
    }

    public static void write(String controlPath, BhFrameGenSettings s) {
        if (controlPath == null || controlPath.isEmpty() || s == null) return;
        writeByteAt(controlPath, 2, (byte) (s.enabled ? 1 : 0));
        writeMultiplier(controlPath, s.multiplier);
    }

    public static void writeEnabled(String controlPath, boolean enabled) {
        writeByteAt(controlPath, 2, (byte) (enabled ? 1 : 0));
    }

    public static void writeMultiplier(String controlPath, int multiplier) {
        writeByteAt(controlPath, 9, (byte) Math.max(2, Math.min(4, multiplier)));
    }

    public static String resolveControlPath(Context ctx) {
        return new File(ctx.getFilesDir(), "usr/etc/gamescope.control").getAbsolutePath();
    }

    private static void writeByteAt(String controlPath, int offset, byte value) {
        if (controlPath == null || controlPath.isEmpty()) return;
        try (RandomAccessFile raf = new RandomAccessFile(controlPath, "rw")) {
            if (raf.length() < 10) raf.setLength(10);
            FileChannel channel = raf.getChannel();
            MappedByteBuffer buf = channel.map(FileChannel.MapMode.READ_WRITE, 0, 10);
            buf.put(offset, value);
            buf.force();
        } catch (Exception ignored) {}
    }
}
