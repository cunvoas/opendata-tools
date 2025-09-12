package com.github.cunvoas.geoserviceisochrone.config.security;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

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

	private static List<String> whitelist = new ArrayList<String>();
	static {
		whitelist.add("127.0.0");
		whitelist.add("127.0.1");
		whitelist.add("192.168.1");
		whitelist.add("172.19.0");
	}
	
	private static List<String[]> privateNetwork = new ArrayList<>();
	static {
		privateNetwork.add(new String[]{"10.0", "10.255"});
		privateNetwork.add(new String[]{"127.0", "127.255"});
		privateNetwork.add(new String[]{"172.16", "172.31"});
		privateNetwork.add(new String[]{"192.168", "192.168"});
	}

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