package edu.kit.tm.pseprak2.alushare.presenter;


import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Canvas;
import android.graphics.PorterDuff;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;


public class QRCodeGenerator {
    private String content;

    public QRCodeGenerator(String content) {
        this.content = content;

    }

    public Bitmap generateQRCode() {
        QRCodeWriter writer = new QRCodeWriter();
        try {

            BitMatrix matrix = writer.encode(this.content, BarcodeFormat.QR_CODE, 512, 512);
            Bitmap bitmap = Bitmap.createBitmap(matrix.getWidth(), matrix.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas tempCanvas = new Canvas(bitmap);
            tempCanvas.drawColor(0, PorterDuff.Mode.CLEAR);

            for (int x = 0; x < matrix.getWidth(); x++) {
                for (int y = 0; y < matrix.getHeight(); y++) {
                    bitmap.setPixel(x, y, matrix.get(x, y) ? Color.BLACK : Color.TRANSPARENT);

                }
            }

            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

}

