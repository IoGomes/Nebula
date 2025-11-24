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
                    bmp.setPixel(x, y, matrix.get(x, y) ? Color.WHITE : Color.BLACK);
                }
            }
            return bmp;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }



    public static Bitmap generate(String text, int size) {

        Bitmap qr = generateBasicQR(text, size);
        return qr;
    }
}