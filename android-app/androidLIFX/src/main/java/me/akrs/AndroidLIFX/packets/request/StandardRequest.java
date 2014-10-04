package me.akrs.AndroidLIFX.packets.request;

import me.akrs.AndroidLIFX.packets.PacketType;
import me.akrs.AndroidLIFX.utils.MacAddress;

public class StandardRequest {
	private MacAddress macAddress;
	private MacAddress gatewayMac;
	private PacketType type;
	private int length;
	
	public StandardRequest (MacAddress mac, MacAddress gatewayMac, PacketType packetType, int length) {
		this.macAddress = mac;
		this.gatewayMac = gatewayMac;
		this.type = packetType;
		this.length = length;
	}
	
	public byte getByte (int position) {
		switch (position) {
		case 0:
			return (byte)this.length;
		case 3:
			if (this.macAddress == null)
				return 0x34;
			else
				return 0x14;
		case 8: case 9: case 10: case 11: case 12: case 13:
			if (this.macAddress == null) {
				return 0x00;
			} else {
				return this.macAddress.getAddressByte(position-8);
			}
		case 16: case 17: case 18: case 19: case 20: case 21:
			return this.gatewayMac.getAddressByte(position-16);
		case 32:
			return type.toData();
		default:
			return 0;
		}
	}
	
	public int getLength () {
		return this.length;
	}
}

