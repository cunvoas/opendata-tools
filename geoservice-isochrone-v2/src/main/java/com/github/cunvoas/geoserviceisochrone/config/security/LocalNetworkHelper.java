package com.github.cunvoas.geoserviceisochrone.config.security;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utilitaire pour la gestion et la vérification des adresses IP locales et réseaux privés.
 * <p>
 * Permet d'obtenir le sous-réseau local et de gérer des listes blanches ou privées d'adresses IP.
 * </p>
 */
public class LocalNetworkHelper {

	private static List<String> whitelist = new ArrayList<String>();
	static {
		whitelist.add("127.0.0");
		whitelist.add("192.168.1");
		whitelist.add("192.168.8");
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
	    byte[] ipAddr = localHost.getAddress();
	    return String.format("%d.%d.%d", (ipAddr[0] & 0xFF), (ipAddr[1] & 0xFF), (ipAddr[2] & 0xFF));
	}
	
	/**
	 * Indique si le sous-réseau local est dans la liste blanche ou dans une plage privée.
	 * @return vrai si le sous-réseau local est autorisé, faux sinon
	 */
	public boolean isLocalOrWhitelisted() {
		try {
			return matches(getSubnet());
		} catch (UnknownHostException e) {
			return false;
		}
	}

	static boolean matches(String sub) {
		if (whitelist.contains(sub)) {
			return true;
		}
		int[] ip = toOctets(sub);
		for (String[] range : privateNetwork) {
			if (compare(ip, toOctets(range[0])) >= 0 && compare(ip, toOctets(range[1])) <= 0) {
				return true;
			}
		}
		return false;
	}

	private static int[] toOctets(String dotted) {
		return Arrays.stream(dotted.split("\\.")).mapToInt(Integer::parseInt).toArray();
	}

	/** Compare les premiers octets de la borne, les bornes pouvant être plus courtes (ex: 10.0). */
	private static int compare(int[] ip, int[] bound) {
		for (int i = 0; i < bound.length; i++) {
			if (ip[i] != bound[i]) {
				return Integer.compare(ip[i], bound[i]);
			}
		}
		return 0;
	}
}
