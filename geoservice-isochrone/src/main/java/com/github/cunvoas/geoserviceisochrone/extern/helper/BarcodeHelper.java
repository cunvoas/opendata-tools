package com.github.cunvoas.geoserviceisochrone.extern.helper;

import java.awt.image.BufferedImage;

import org.jfree.graphics2d.svg.SVGGraphics2D;
import org.jfree.graphics2d.svg.ViewBox;
import org.springframework.stereotype.Component;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * @author cus
 * @see https://www.baeldung.com/java-generating-barcodes-qr-codes
 * @see https://dzone.com/articles/how-to-create-a-qr-code-svg-using-zxing-and-jfrees
 */
@Component
public class BarcodeHelper {
	
	private int width=300;
	private int height=300;
	
	public BufferedImage generateQRCodeImage(String barcodeText) throws Exception {
	    QRCodeWriter barcodeWriter = new QRCodeWriter();
	    BitMatrix bitMatrix =  barcodeWriter.encode(barcodeText, BarcodeFormat.QR_CODE, width, height);

	    return MatrixToImageWriter.toBufferedImage(bitMatrix);
	}

	public String getQRCodeSvg(BufferedImage qrCodeImage, String targetUrl, boolean withViewBox) {
		SVGGraphics2D g2 = new SVGGraphics2D(width, height);
		g2.drawImage(qrCodeImage, 0, 0, width, height, null);

		ViewBox viewBox = null;
		if (withViewBox) {
			viewBox = new ViewBox(0, 0, width, height);
		}
		return g2.getSVGElement(null, true, viewBox, null, null);
	}
}
