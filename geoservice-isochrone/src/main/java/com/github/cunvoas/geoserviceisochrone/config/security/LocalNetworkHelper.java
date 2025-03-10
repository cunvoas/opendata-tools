package com.github.cunvoas.geoserviceisochrone.config.security;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

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


	
	private static String getSubnet() throws UnknownHostException {
	    InetAddress localHost = InetAddress.getLocalHost();
	    System.out.println(localHost);
	    byte[] ipAddr = localHost.getAddress();
	    return String.format("%d.%d.%d", (ipAddr[0] & 0xFF), (ipAddr[1] & 0xFF), (ipAddr[2] & 0xFF));
	}
}
