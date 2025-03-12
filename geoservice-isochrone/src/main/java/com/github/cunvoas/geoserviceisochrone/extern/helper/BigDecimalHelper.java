package com.github.cunvoas.geoserviceisochrone.extern.helper;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Helper for BigDecimal.
 */
public class BigDecimalHelper {

	public static BigDecimal integerFromDouble(Double d) {
		NumberFormat formatterInt = new DecimalFormat("#0");
		return new BigDecimal(formatterInt.format(d));
	}

	public static BigDecimal centileFromDouble(Double d) {
		NumberFormat formatterCent = new DecimalFormat("#0.00");
		return new BigDecimal(formatterCent.format(d));
	}
}
