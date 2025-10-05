package Mercury.Android.Mercury_View.Activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.Choreographer;
import android.view.Display;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Mercury.Android.R;

public class Performance_Activity extends AppCompatActivity {


        private TextView resultText;
        private TextView fpsText;
        private TextView metricsText;
        private Button btnMainThread;
        private Button btnExecutor;
        private Button btnThread;
        private Button btnReset;

        private Handler mainHandler;
        private ExecutorService executorService;

        // Métricas de performance
        private float deviceRefreshRate = 60f;
        private long lastFrameTime = 0;
        private List<Long> frameTimes = new ArrayList<>();
        private int droppedFrames = 0;
        private int totalFrames = 0;
        private long testStartTime = 0;
        private boolean isTestRunning = false;

        private DecimalFormat df = new DecimalFormat("#.##");

        private Choreographer.FrameCallback frameCallback = new Choreographer.FrameCallback() {
            @Override
            public void doFrame(long frameTimeNanos) {
                if (!isTestRunning) return;

                long currentTime = frameTimeNanos / 1_000_000; // Converte para ms

                if (lastFrameTime > 0) {
                    long frameTime = currentTime - lastFrameTime;
                    frameTimes.add(frameTime);
                    totalFrames++;

                    // Detecta frame dropped (tempo maior que o esperado)
                    float expectedFrameTime = 1000f / deviceRefreshRate;
                    if (frameTime > expectedFrameTime * 1.5f) {
                        droppedFrames++;
                    }

                    // Mantém apenas os últimos 60 frames para cálculo
                    if (frameTimes.size() > 60) {
                        frameTimes.remove(0);
                    }

                    updateMetrics();
                }

                lastFrameTime = currentTime;
                Choreographer.getInstance().postFrameCallback(this);
            }
        };

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            setTheme(androidx.appcompat.R.style.Theme_AppCompat);
            super.onCreate(savedInstanceState);
            setContentView(R.layout.performance_test);

            resultText = findViewById(R.id.resultText);
            fpsText = findViewById(R.id.fpsText);
            metricsText = findViewById(R.id.metricsText);
            btnMainThread = findViewById(R.id.btnMainThread);
            btnExecutor = findViewById(R.id.btnExecutor);
            btnThread = findViewById(R.id.btnThread);
            btnReset = findViewById(R.id.btnReset);

            mainHandler = new Handler(Looper.getMainLooper());
            executorService = Executors.newSingleThreadExecutor();

            // Detecta o refresh rate do dispositivo
            Display display = getWindowManager().getDefaultDisplay();
            deviceRefreshRate = display.getRefreshRate();

            startMonitoring();

            btnMainThread.setOnClickListener(v -> testMainThread());
            btnExecutor.setOnClickListener(v -> testWithExecutor());
            btnThread.setOnClickListener(v -> testWithThread());
            btnReset.setOnClickListener(v -> resetMetrics());
        }

        @Override
        protected void onDestroy() {
            super.onDestroy();
            isTestRunning = false;
            Choreographer.getInstance().removeFrameCallback(frameCallback);
            executorService.shutdown();
        }

        private void startMonitoring() {
            isTestRunning = true;
            testStartTime = SystemClock.elapsedRealtime();
            lastFrameTime = 0;
            Choreographer.getInstance().postFrameCallback(frameCallback);
        }

        private void updateMetrics() {
            if (frameTimes.isEmpty()) return;

            // Calcula FPS atual
            long sum = 0;
            for (Long time : frameTimes) {
                sum += time;
            }
            float avgFrameTime = sum / (float) frameTimes.size();
            float currentFps = 1000f / avgFrameTime;

            // Calcula variação (jank)
            float variance = 0;
            for (Long time : frameTimes) {
                variance += Math.pow(time - avgFrameTime, 2);
            }
            variance = variance / frameTimes.size();
            float stdDev = (float) Math.sqrt(variance);

            // Calcula percentual de frames dropped
            float droppedPercent = totalFrames > 0 ?
                    (droppedFrames * 100f / totalFrames) : 0;

            // Tempo total de teste
            long elapsedTime = (SystemClock.elapsedRealtime() - testStartTime) / 1000;

            // Atualiza UI
            String fpsColor = currentFps >= deviceRefreshRate * 0.9f ? "🟢" :
                    currentFps >= deviceRefreshRate * 0.6f ? "🟡" : "🔴";

            fpsText.setText(fpsColor + " FPS: " + df.format(currentFps) +
                    " / " + df.format(deviceRefreshRate));

            String metricsInfo = "📊 MÉTRICAS DETALHADAS\n\n" +
                    "⏱️ Tempo de teste: " + elapsedTime + "s\n" +
                    "🎯 Refresh rate: " + df.format(deviceRefreshRate) + " Hz\n" +
                    "📈 FPS médio: " + df.format(currentFps) + "\n" +
                    "⚡ Frame time: " + df.format(avgFrameTime) + " ms\n" +
                    "📉 Frames dropped: " + droppedFrames + " (" + df.format(droppedPercent) + "%)\n" +
                    "📊 Total frames: " + totalFrames + "\n" +
                    "🔀 Variação (jank): " + df.format(stdDev) + " ms\n" +
                    getPerformanceRating(currentFps, droppedPercent, stdDev);

            metricsText.setText(metricsInfo);
        }

        private String getPerformanceRating(float fps, float droppedPercent, float jank) {
            String rating;
            String emoji;

            if (fps >= deviceRefreshRate * 0.95f && droppedPercent < 1 && jank < 3) {
                rating = "EXCELENTE";
                emoji = "🌟";
            } else if (fps >= deviceRefreshRate * 0.85f && droppedPercent < 5 && jank < 5) {
                rating = "BOM";
                emoji = "✅";
            } else if (fps >= deviceRefreshRate * 0.60f && droppedPercent < 15) {
                rating = "REGULAR";
                emoji = "⚠️";
            } else {
                rating = "RUIM";
                emoji = "❌";
            }

            return "\n" + emoji + " Performance: " + rating;
        }

        private void resetMetrics() {
            droppedFrames = 0;
            totalFrames = 0;
            frameTimes.clear();
            testStartTime = SystemClock.elapsedRealtime();
            resultText.setText("Métricas resetadas! Pronto para novo teste.");
        }

        // Teste pesado na thread principal (RUIM - vai travar a UI)
        private void testMainThread() {
            resultText.setText("⏳ Processando na Main Thread...\n(Observe o FPS cair!)");
            btnMainThread.setEnabled(false);

            mainHandler.postDelayed(() -> {
                long startTime = System.currentTimeMillis();
                long result = heavyComputation();
                long time = System.currentTimeMillis() - startTime;

                resultText.setText("❌ MAIN THREAD\n\n" +
                        "Resultado: " + result + "\n" +
                        "Tempo: " + time + "ms\n\n" +
                        "⚠️ UI travou durante o processamento!\n" +
                        "Verifique os frames dropped acima.");

                btnMainThread.setEnabled(true);
            }, 100);
        }

        // Teste com ExecutorService (BOM - UI responsiva)
        private void testWithExecutor() {
            resultText.setText("⏳ Processando com ExecutorService...\n(UI deve continuar responsiva)");
            btnExecutor.setEnabled(false);

            executorService.execute(() -> {
                long startTime = System.currentTimeMillis();
                long result = heavyComputation();
                long time = System.currentTimeMillis() - startTime;

                runOnUiThread(() -> {
                    resultText.setText("✅ EXECUTOR SERVICE\n\n" +
                            "Resultado: " + result + "\n" +
                            "Tempo: " + time + "ms\n\n" +
                            "✓ UI permaneceu responsiva!\n" +
                            "FPS mantido durante processamento.");
                    btnExecutor.setEnabled(true);
                });
            });
        }

        // Teste com Thread customizada (BOM - UI responsiva)
        private void testWithThread() {
            resultText.setText("⏳ Processando com Thread separada...\n(UI deve continuar responsiva)");
            btnThread.setEnabled(false);

            new Thread(() -> {
                long startTime = System.currentTimeMillis();
                long result = heavyComputation();
                long time = System.currentTimeMillis() - startTime;

                runOnUiThread(() -> {
                    resultText.setText("✅ THREAD CUSTOMIZADA\n\n" +
                            "Resultado: " + result + "\n" +
                            "Tempo: " + time + "ms\n\n" +
                            "✓ UI permaneceu responsiva!\n" +
                            "FPS mantido durante processamento.");
                    btnThread.setEnabled(true);
                });
            }).start();
        }

        // Simulação de processamento pesado
        private long heavyComputation() {
            long sum = 0;
            for (int i = 1; i <= 50_000_000; i++) {
                sum += (i % 7);
            }

            try {
                Thread.sleep(1000); // Simula operação de I/O
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return sum;
        }
    }


