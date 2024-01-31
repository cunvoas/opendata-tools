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

@RestController
@RequestMapping("/barcodes")
public class BarcodesController {
	@Autowired
	private BarcodeHelper barcodeHelper;

	@GetMapping(value = "/qrcode/{barcode}", produces = MediaType.IMAGE_PNG_VALUE)
	public ResponseEntity<BufferedImage> barbecueEAN13Barcode(@PathVariable("barcode") String barcode)
			throws Exception {
		
		return new ResponseEntity(barcodeHelper.generateQRCodeImage(barcode), HttpStatus.OK);
	}
}