package com.github.cunvoas.geoserviceisochrone.util;

/**
 * Conversions de chaînes en nombres sans NumberFormatException non gérée.
 */
public class NumberHelper {

	/** @return la valeur, ou null si absente ou invalide */
	public static Double toDouble(String value) {
		if (value == null) {
			return null;
		}
		try {
			return Double.valueOf(value);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/** @return la valeur, ou null si absente ou invalide */
	public static Long toLong(String value) {
		if (value == null) {
			return null;
		}
		try {
			return Long.valueOf(value);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/** @return la valeur, ou null si absente ou invalide */
	public static Integer toInteger(String value) {
		if (value == null) {
			return null;
		}
		try {
			return Integer.valueOf(value);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/** @throws IllegalArgumentException si la valeur n'est pas un entier */
	public static Integer parseIntOrThrow(String value) {
		Integer ret = toInteger(value);
		if (ret == null) {
			throw new IllegalArgumentException("Entier invalide : " + value);
		}
		return ret;
	}

	/** @throws IllegalArgumentException si la valeur n'est pas un nombre */
	public static Double parseDoubleOrThrow(String value) {
		Double ret = toDouble(value);
		if (ret == null) {
			throw new IllegalArgumentException("Nombre invalide : " + value);
		}
		return ret;
	}
}
