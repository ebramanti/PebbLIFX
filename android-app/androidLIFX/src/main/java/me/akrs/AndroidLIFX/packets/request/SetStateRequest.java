package me.akrs.AndroidLIFX.packets.request;

import me.akrs.AndroidLIFX.packets.PacketType;
import me.akrs.AndroidLIFX.utils.MacAddress;
import me.akrs.AndroidLIFX.utils.java.Logger;

public class SetStateRequest extends StandardRequest {
	
	private short hue;
	private short saturation;
	private short luminance;
	private short whiteColor;
	private short fadeTime;
	
	public SetStateRequest (MacAddress mac, MacAddress gatewayMac, short hue, short saturation, short luminance, short whiteColor, short fadeTime) { //If mac == null the message will be broadcasted
		super(mac, gatewayMac, PacketType.SET_STATE_REQUEST, 48);
		Logger.log("Setting state for bulb with mac: " + mac, Logger.DEBUG);
		Logger.log("Brightness is " + luminance, Logger.DEBUG);
		this.hue = hue;
		this.saturation = saturation;
		this.luminance = luminance;
		this.whiteColor = whiteColor;
		this.fadeTime = fadeTime;
	}

	@Override
	public byte getByte(int position) {
		if(position<=32){return super.getByte(position);} //The standard request takes care of most things

		switch(position){
		case 37:
			return (byte)(Short.reverseBytes(this.hue) >>> 8);
		case 38:
			return (byte)(Short.reverseBytes(this.hue));
		case 39:
			return (byte)(Short.reverseBytes(this.saturation) >>> 8);
		case 40:
			return (byte)(Short.reverseBytes(this.saturation));
		case 41:
			return (byte)(Short.reverseBytes(this.luminance) >>> 8);
		case 42:
			return (byte)(Short.reverseBytes(this.luminance));
		case 43:
			return (byte)(Short.reverseBytes(this.whiteColor) >>> 8);
		case 44:
			return (byte)(Short.reverseBytes(this.whiteColor));
		case 45:
			return (byte)(Short.reverseBytes(this.fadeTime) >>> 8);
		case 46:
			return (byte)(Short.reverseBytes(this.fadeTime));
		}

		return 0;
	}

	public byte[] getBytes() {
		byte[] bytes = new byte[this.getLength()];
		
		//Loop throw all bytes and add them to the byte array
		for(int i=0;i<this.getLength();i++){
			bytes[i] = this.getByte(i);
		}
		
		return bytes;
	}
}
