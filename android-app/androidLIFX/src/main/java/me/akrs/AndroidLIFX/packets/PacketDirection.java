package me.akrs.AndroidLIFX.packets;

public enum PacketDirection {
	APP_TO_ONE_BULB,APP_TO_ALL_BULBS,BULB_TO_APP,UNKNOWN;
	
	public byte toData () {
		switch(this){
		case APP_TO_ONE_BULB:
			return 0x14;
		case APP_TO_ALL_BULBS:
			return 0x34;
		case BULB_TO_APP:
			return 0x54;
		default:
			return 0x00;
		}
	}
	
	public static PacketDirection fromData (byte data) {
		switch(data){
		case 0x14:
			return APP_TO_ONE_BULB;
		case 0x34:
			return APP_TO_ALL_BULBS;
		case 0x54:
			return BULB_TO_APP;
		default:
			return UNKNOWN;
		}
	}
}
