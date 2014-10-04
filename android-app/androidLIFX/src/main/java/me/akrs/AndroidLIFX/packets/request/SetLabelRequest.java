package me.akrs.AndroidLIFX.packets.request;

import me.akrs.AndroidLIFX.packets.PacketType;
import me.akrs.AndroidLIFX.utils.MacAddress;

public class SetLabelRequest extends StandardRequest {
	private String label;
	
	public SetLabelRequest(MacAddress mac, MacAddress gatewayMac, String label) {
		super(mac, gatewayMac, PacketType.SET_LABEL_REQUEST, 65);
		this.label = label;
	}
	
	public byte getByte (int position) {
		if (position <= 32) {
			return super.getByte(position);
		}
		
		if (position - 32 > this.label.length()) {
			return (byte) 0;
		}
		
		return (byte) this.label.charAt(position - 32);
	}
	
	public byte[] getBytes() {
		byte[] bytes = new byte[this.getLength()];
		
		//Loop throw all bytes and add them to the byte array
		for(int i = 0; i < this.getLength(); i++){
			bytes[i] = this.getByte(i);
		}
		
		return bytes;
	}
}
