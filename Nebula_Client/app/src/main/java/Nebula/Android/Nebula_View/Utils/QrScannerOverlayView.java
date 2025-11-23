package Nebula.Android.Nebula_View.Utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class QrScannerOverlayView extends View {

    private Paint maskPaint;
    private Paint framePaint;
    private Paint transparentPaint;
    private RectF scanRect;
    private float cornerRadius = 0f;
    private float frameStrokeWidth = 2f;
    private int maskColor = 0xAA000000;
    private int frameColor = 0xAA606060;
    private float topSpaceRatio = 0.15f;
    private float bottomSpaceRatio = 0.25f;

    public QrScannerOverlayView(Context context) {
        super(context);
        init();
    }

    public QrScannerOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public QrScannerOverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Paint para a máscara escura
        maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        maskPaint.setColor(maskColor);
        maskPaint.setStyle(Paint.Style.FILL);

        // Paint para o quadrado transparente
        transparentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        transparentPaint.setColor(Color.TRANSPARENT);
        transparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        // Paint para a borda do quadrado
        framePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        framePaint.setColor(frameColor);
        framePaint.setStyle(Paint.Style.STROKE);
        framePaint.setStrokeWidth(frameStrokeWidth);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = canvas.getWidth();
        int height = canvas.getHeight();

        // Calcula o tamanho do quadrado central
        int scanSize = (int) (Math.min(width, height) * 0.7f);

        // Centraliza horizontalmente
        int left = (width - scanSize) / 2;

        // Calcula a posição vertical considerando espaços assimétricos
        int availableHeight = height - scanSize;
        int topSpace = (int) (availableHeight * (topSpaceRatio / (topSpaceRatio + bottomSpaceRatio)));
        int top = topSpace;

        scanRect = new RectF(left, top, left + scanSize, top + scanSize);

        // Salva a camada atual
        int saveCount = canvas.saveLayer(0, 0, width, height, null);

        // Desenha a máscara escura sobre toda a tela
        canvas.drawRect(0, 0, width, height, maskPaint);

        // Recorta o quadrado transparente
        canvas.drawRoundRect(scanRect, cornerRadius, cornerRadius, transparentPaint);

        // Restaura a camada
        canvas.restoreToCount(saveCount);

        // Desenha a borda do quadrado
        canvas.drawRoundRect(scanRect, cornerRadius, cornerRadius, framePaint);

        // Desenha os cantos decorativos
        drawCorners(canvas);
    }

    private void drawCorners(Canvas canvas) {
        float cornerLength = 50f;
        float cornerThickness = 6f;

        Paint cornerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cornerPaint.setColor(frameColor);
        cornerPaint.setStyle(Paint.Style.STROKE);
        cornerPaint.setStrokeWidth(cornerThickness);
        cornerPaint.setStrokeCap(Paint.Cap.ROUND);

        // Canto superior esquerdo
        canvas.drawLine(scanRect.left, scanRect.top + cornerLength,
                scanRect.left, scanRect.top, cornerPaint);
        canvas.drawLine(scanRect.left, scanRect.top,
                scanRect.left + cornerLength, scanRect.top, cornerPaint);

        // Canto superior direito
        canvas.drawLine(scanRect.right - cornerLength, scanRect.top,
                scanRect.right, scanRect.top, cornerPaint);
        canvas.drawLine(scanRect.right, scanRect.top,
                scanRect.right, scanRect.top + cornerLength, cornerPaint);

        // Canto inferior esquerdo
        canvas.drawLine(scanRect.left, scanRect.bottom - cornerLength,
                scanRect.left, scanRect.bottom, cornerPaint);
        canvas.drawLine(scanRect.left, scanRect.bottom,
                scanRect.left + cornerLength, scanRect.bottom, cornerPaint);

        // Canto inferior direito
        canvas.drawLine(scanRect.right - cornerLength, scanRect.bottom,
                scanRect.right, scanRect.bottom, cornerPaint);
        canvas.drawLine(scanRect.right, scanRect.bottom,
                scanRect.right, scanRect.bottom - cornerLength, cornerPaint);
    }

    // Métodos para customização
    public void setMaskColor(int color) {
        this.maskColor = color;
        maskPaint.setColor(color);
        invalidate();
    }

    public void setFrameColor(int color) {
        this.frameColor = color;
        framePaint.setColor(color);
        invalidate();
    }

    public void setCornerRadius(float radius) {
        this.cornerRadius = radius;
        invalidate();
    }

    public void setFrameStrokeWidth(float width) {
        this.frameStrokeWidth = width;
        framePaint.setStrokeWidth(width);
        invalidate();
    }

    public void setSpaceRatios(float topRatio, float bottomRatio) {
        this.topSpaceRatio = topRatio;
        this.bottomSpaceRatio = bottomRatio;
        invalidate();
    }

    public RectF getScanRect() {
        return scanRect;
    }
}