package com.github.cunvoas.geoserviceisochrone.config.security;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Utilitaire pour la gestion et la vérification des adresses IP locales et réseaux privés.
 * <p>
 * Permet d'obtenir le sous-réseau local et de gérer des listes blanches ou privées d'adresses IP.
 * </p>
 * @TODO Compléter les méthodes pour une gestion avancée des réseaux locaux.
 */
public class LocalNetworkHelper {

//	public static void main(String[] args) throws Exception {
//		System.out.println(getSubnet());
//	}

	/**
	 * Retourne le sous-réseau local de la machine.
	 * @return le sous-réseau local sous forme de chaîne (ex: 192.168.1)
	 * @throws UnknownHostException si l'adresse locale ne peut être déterminée
	 */
	private static String getSubnet() throws UnknownHostException {
	    InetAddress localHost = InetAddress.getLocalHost();
	    System.out.println(localHost);
	    byte[] ipAddr = localHost.getAddress();
	    return String.format("%d.%d.%d", (ipAddr[0] & 0xFF), (ipAddr[1] & 0xFF), (ipAddr[2] & 0xFF));
	}
}