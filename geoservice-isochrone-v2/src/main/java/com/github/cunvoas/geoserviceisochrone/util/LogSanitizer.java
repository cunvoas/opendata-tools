package com.github.cunvoas.geoserviceisochrone.util;

/**
 * Neutralise CR, LF et TAB avant journalisation pour éviter l'injection dans les logs (CWE-117).
 * shortcut: ESC et U+2028 non neutralisés, étendre si les logs sont affichés dans un terminal.
 */
public class LogSanitizer {

	public static String sanitize(Object value) {
		return String.valueOf(value).replace('\n', '_')
				.replace('\r', '_')
				.replace('\t', '_');
	}
}
