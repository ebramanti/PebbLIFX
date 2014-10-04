package me.akrs.AndroidLIFX.utils;

import java.util.Arrays;

public class MacAddress {
	byte[] address = new byte[6];
	
	public MacAddress (String mac) { //Create address with format "12:34:56:78:9A:BC" or "12-34-56-78-9A-BC"
		address[0] = (byte) Integer.parseInt(mac.substring(0, 2), 16);
		address[1] = (byte) Integer.parseInt(mac.substring(3, 5), 16);
		address[2] = (byte) Integer.parseInt(mac.substring(6, 8), 16);
		address[3] = (byte) Integer.parseInt(mac.substring(9, 11), 16);
		address[4] = (byte) Integer.parseInt(mac.substring(12, 14), 16);
		address[5] = (byte) Integer.parseInt(mac.substring(15, 17), 16);
	}
	
	public MacAddress (byte[] bytes){
		for (int i = 0; i < 6; i++){
			this.address[i] = bytes[i];
		}
	}
	
	public boolean equal (MacAddress otherAddress) {
		return Arrays.equals(this.getAddress(), otherAddress.getAddress());
	}
	
	public byte[] getAddress () {
		return address;
	}
	
	public byte getAddressByte (int pos) {
		return address[pos];
	}
	
	public String toString () {
	    StringBuilder sb = new StringBuilder();
	    for (byte b : address) {
	        sb.append(String.format("%02X:", b));
	    }
	    sb.setLength(sb.length()-1);
	    return sb.toString();
	}
}