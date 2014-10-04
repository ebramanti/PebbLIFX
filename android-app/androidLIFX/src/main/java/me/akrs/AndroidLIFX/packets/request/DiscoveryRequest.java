package me.akrs.AndroidLIFX.packets.request;

public class DiscoveryRequest {	
	public byte getLength() {
		return 36; //The Discovery request is always 36 bytes
	}

	public byte getByte (int position) {
		switch(position){
		case 0:
			return this.getLength();
		case 3:
			return 0x34;
		case 32:
			return 0x02;
		}
		
		//If nothing else is specified the byte should be zero
		return 0;
	}

	public byte[] getBytes () {
		byte[] bytes = new byte[this.getLength()];
		
		//Loop throw all bytes and add them to the byte array
		for(int i = 0;i < this.getLength(); i++){
			bytes[i] = this.getByte(i);
		}
		
		return bytes;
	}
}
