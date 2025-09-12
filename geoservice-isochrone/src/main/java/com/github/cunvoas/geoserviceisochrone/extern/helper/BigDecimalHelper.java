package com.github.cunvoas.geoserviceisochrone.extern.helper;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Classe utilitaire pour les opérations sur BigDecimal.
 * Fournit des méthodes pour convertir des valeurs Double en BigDecimal avec différents formats.
 */
public class BigDecimalHelper {

	/**
	 * Convertit un Double en BigDecimal en ne gardant que la partie entière.
	 * @param d valeur à convertir
	 * @return BigDecimal représentant la partie entière de d
	 */
	public static BigDecimal integerFromDouble(Double d) {
		NumberFormat formatterInt = new DecimalFormat("#0");
		return new BigDecimal(formatterInt.format(d));
	}

	/**
	 * Convertit un Double en BigDecimal avec deux décimales (centièmes).
	 * @param d valeur à convertir
	 * @return BigDecimal représentant d arrondi à deux décimales
	 */
	public static BigDecimal centileFromDouble(Double d) {
		NumberFormat formatterCent = new DecimalFormat("#0.00");
		return new BigDecimal(formatterCent.format(d));
	}
}