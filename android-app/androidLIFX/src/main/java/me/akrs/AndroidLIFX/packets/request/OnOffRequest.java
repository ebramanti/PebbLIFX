package me.akrs.AndroidLIFX.packets.request;

import me.akrs.AndroidLIFX.packets.PacketType;
import me.akrs.AndroidLIFX.utils.BulbStatus;
import me.akrs.AndroidLIFX.utils.MacAddress;

public class OnOffRequest extends StandardRequest {
	private BulbStatus status;

	public OnOffRequest (MacAddress mac, MacAddress gatewayMac, BulbStatus status) {
		super(mac, gatewayMac, PacketType.ON_OFF_REQUEST, 38);
		this.status = status;
	}

	public byte getByte(int position) {
		if(position<=32){return super.getByte(position);} //The standard request takes care of most things
		
		if(position == 36){
			if(this.status == BulbStatus.ON) {
				return 0x01;
			} else {
				return 0x00;
			}
		}
		
		return 0;
	}
	
	public byte[] getBytes() {
		byte[] bytes = new byte[this.getLength()];
		
		//Loop throw all bytes and add them to the byte array
		for(int i=0; i < this.getLength(); i++){
			bytes[i] = this.getByte(i);
		}
		
		return bytes;
	}
}
