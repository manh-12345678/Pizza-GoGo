package Group5_pizza.Pizza_GoGo.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class QRCodeGenerator {

    public static String generateQRCodeImage(String text, String path, int width, int height) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

        Path filePath = new File(path).toPath();
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", filePath);

        return path; 
    }
}
