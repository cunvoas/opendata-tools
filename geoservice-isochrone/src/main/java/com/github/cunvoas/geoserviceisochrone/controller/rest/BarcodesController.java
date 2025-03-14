package com.github.cunvoas.geoserviceisochrone.controller.rest;

import java.awt.image.BufferedImage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.cunvoas.geoserviceisochrone.extern.helper.BarcodeHelper;

/**
 * REsT Controler for QRcode.
 */
@RestController
@RequestMapping("/mvc/barcodes")
public class BarcodesController {
	
	@Autowired
	private BarcodeHelper barcodeHelper;

	/**
	 * gen QRcode
	 * @param barcode qrcode value
	 * @return BufferedImage
	 * @throws Exception
	 */
	@GetMapping(value = "/qrcode/{barcode}", produces = MediaType.IMAGE_PNG_VALUE)
	public ResponseEntity<BufferedImage> barcode(@PathVariable("barcode") String barcode)
			throws Exception {
		
		return new ResponseEntity<BufferedImage>(barcodeHelper.generateQRCodeImage(barcode), HttpStatus.OK);
	}
}