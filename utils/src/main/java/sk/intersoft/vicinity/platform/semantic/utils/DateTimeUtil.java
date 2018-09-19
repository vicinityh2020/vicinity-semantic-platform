package sk.intersoft.vicinity.platform.semantic.utils;

public class DateTimeUtil {

    public static long millis() {
        return System.currentTimeMillis();
    }


    public static long duration(long ms) {
        return (millis() - ms);
    }

    public static String hours(long ms) {
        return ((ms / (1000 * 60 * 60)) % 24)+"";
    }

    public static String minutes(long ms) {
        return ((ms / (1000 * 60)) % 60)+"";
    }

    public static String seconds(long ms) {
        return ((ms / 1000) % 60)+"";
    }

    public static String format(long time) {
        return time + "ms :: "+hours(time)+":"+minutes(time)+":"+seconds(time);
    }

}
