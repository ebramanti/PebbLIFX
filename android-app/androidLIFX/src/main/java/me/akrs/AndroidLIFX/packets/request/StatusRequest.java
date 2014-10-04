package me.akrs.AndroidLIFX.packets.request;

import me.akrs.AndroidLIFX.packets.PacketType;
import me.akrs.AndroidLIFX.utils.MacAddress;

public class StatusRequest extends StandardRequest {

	public StatusRequest(MacAddress gatewayMac) {
		super(null, gatewayMac, PacketType.GET_STATUS_REQUEST, 36);
	}
	
	
	@Override
	public byte getByte(int position) {
		if (position <= 32) {return super.getByte(position);} //The standard request takes care of most things
		
		return 0;
	}

	public byte[] getBytes() {
		byte[] bytes = new byte[this.getLength()];
		
		//Loop throw all bytes and add them to the byte array
		for(int i=0; i<this.getLength(); i++){
			bytes[i] = this.getByte(i);
		}
		
		return bytes;
	} 
}
