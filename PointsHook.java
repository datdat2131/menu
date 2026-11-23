package vn.astrax.menu;

import java.util.UUID;

/** Kết nối PlayerPoints bằng reflection (không bắt buộc phải có plugin này khi chạy). */
public final class PointsHook {
    private PointsHook() {}

    private static Object api() {
        try {
            Class<?> main = Class.forName("org.black_ixx.playerpoints.PlayerPoints");
            Object inst = main.getMethod("getInstance").invoke(null);
            return inst == null ? null : main.getMethod("getAPI").invoke(inst);
        } catch (Throwable t) {
            return null;
        }
    }

    public static boolean available() {
        return api() != null;
    }

    /** Số poin hiện có, -1 nếu không đọc được. */
    public static int look(UUID id) {
        Object a = api();
        if (a == null) return -1;
        try {
            Class<?> c = Class.forName("org.black_ixx.playerpoints.PlayerPointsAPI");
            Object r = c.getMethod("look", UUID.class).invoke(a, id);
            return r instanceof Number n ? n.intValue() : -1;
        } catch (Throwable t) {
            return -1;
        }
    }

    /** Trừ poin. true nếu trừ thành công. */
    public static boolean take(UUID id, int amount) {
        Object a = api();
        if (a == null) return false;
        try {
            Class<?> c = Class.forName("org.black_ixx.playerpoints.PlayerPointsAPI");
            Object r = c.getMethod("take", UUID.class, int.class).invoke(a, id, amount);
            return r instanceof Boolean b && b;
        } catch (Throwable t) {
            return false;
        }
    }
}
