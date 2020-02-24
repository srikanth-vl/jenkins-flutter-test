package com.vassarlabs.prod.common.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

public class NetUtils {

	/**
	 * Returns hostName
	 * @return
	 */
	public static String getHostName() {
		String hostname = "";
		try {
		    hostname = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException ex) {
		    System.out.println("Inside NetUtils - getHostName() ::Hostname can not be resolved");
		    ex.printStackTrace();
		}
		return hostname;
	}
	
	/**
	 * Returns IP Address 
	 * @return
	 */
	public static String getIPAddress() {
		String ipAddr = "";
		try {
			Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
			while(en.hasMoreElements()) {
			    NetworkInterface ni=(NetworkInterface) en.nextElement();
			    if (ni.isLoopback() || !ni.isUp()) {
			        continue;
			    }
			    Enumeration<InetAddress> ee = ni.getInetAddresses();
			    while(ee.hasMoreElements()) {
			        InetAddress ia= (InetAddress) ee.nextElement();
			        //Skip hexadecimal format
			        if(ia.getHostAddress().matches("[0-9.]+")) { 
			        	ipAddr = ia.getHostAddress();
			        }
			    }
			 }
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return ipAddr;
	}
}