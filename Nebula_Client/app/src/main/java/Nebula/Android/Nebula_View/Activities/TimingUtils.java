package Nebula.Android.Nebula_View.Activities;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class TimingUtils {
    private static final Map<String, Long> times = new HashMap<>();

    /** Marca o início de um evento */
    public static void start(String tag) {
        times.put(tag, System.nanoTime());
    }

    /** Marca o fim do evento e loga a duração em ms */
    public static void stop(String tag) {
        Long start = times.get(tag);
        if (start != null) {
            double durationMs = (System.nanoTime() - start) / 1_000_000.0;
            Log.d("LifecycleTimer", tag + " levou " + durationMs + " ms");
            times.remove(tag);
        } else {
            Log.w("LifecycleTimer", "Nenhum start() encontrado para " + tag);
        }
    }
}
