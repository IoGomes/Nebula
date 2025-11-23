package Nebula.Android.Nebula_ViewModel.Server_Services;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.Hashtable;

public class Service_G_QrCode {

    private static Bitmap generateBasicQR(String text, int size) {
        try {
            QRCodeWriter writer = new QRCodeWriter();

            Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
            hints.put(EncodeHintType.MARGIN, 0);
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

            BitMatrix matrix = writer.encode(text, BarcodeFormat.QR_CODE, size, size, hints);

            Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    // Invertido: branco onde era preto, preto onde era branco
                    bmp.setPixel(x, y, matrix.get(x, y) ? Color.WHITE : Color.BLACK);
                }
            }
            return bmp;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Bitmap roundCorners(Bitmap src, float radius) {
        Bitmap output = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        // Preenche o fundo com preto primeiro
        canvas.drawColor(Color.BLACK);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        RectF rect = new RectF(0, 0, src.getWidth(), src.getHeight());

        // Desenha o retângulo com cantos arredondados
        canvas.drawRoundRect(rect, radius, radius, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(src, 0, 0, paint);

        return output;
    }

    private static Bitmap addLogo(Bitmap qr, Bitmap logo) {
        Bitmap combined = qr.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(combined);

        int logoSize = qr.getWidth() / 5;
        float centerX = qr.getWidth() / 2f;
        float centerY = qr.getHeight() / 2f;
        float starRadius = logoSize / 2f;

        // Desenha estrela de 4 pontas branca com contorno preto
        Paint starPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        starPaint.setColor(Color.WHITE);
        starPaint.setStyle(Paint.Style.FILL);

        android.graphics.Path starPath = create4PointStar(centerX, centerY, starRadius);
        canvas.drawPath(starPath, starPaint);

        // Desenha contorno preto da estrela
        Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        strokePaint.setColor(Color.BLACK);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(4f);
        canvas.drawPath(starPath, strokePaint);

        // Desenha o logo dentro da estrela (opcional, redimensionado)
        if (logo != null) {
            int innerLogoSize = (int) (logoSize * 0.5f);
            Bitmap scaled = Bitmap.createScaledBitmap(logo, innerLogoSize, innerLogoSize, true);
            int left = (int) (centerX - innerLogoSize / 2f);
            int top = (int) (centerY - innerLogoSize / 2f);
            canvas.drawBitmap(scaled, left, top, null);
        }

        return combined;
    }

    private static android.graphics.Path create4PointStar(float centerX, float centerY, float radius) {
        android.graphics.Path path = new android.graphics.Path();

        float outerRadius = radius;
        float innerRadius = radius * 0.4f;

        for (int i = 0; i < 8; i++) {
            double angle = Math.PI / 4 * i - Math.PI / 2;
            float r = (i % 2 == 0) ? outerRadius : innerRadius;
            float x = centerX + (float) (r * Math.cos(angle));
            float y = centerY + (float) (r * Math.sin(angle));

            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }

        path.close();
        return path;
    }

    public static Bitmap generate(String text, int size, Bitmap logo, float cornerRadius) {

        Bitmap qr = generateBasicQR(text, size);

        Bitmap rounded = roundCorners(qr, cornerRadius);

        if (logo != null) {
            rounded = addLogo(rounded, logo);
        }

        return rounded;
    }
}