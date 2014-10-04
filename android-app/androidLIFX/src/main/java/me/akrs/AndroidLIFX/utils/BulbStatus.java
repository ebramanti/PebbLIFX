package me.akrs.AndroidLIFX.utils;

public enum BulbStatus {
	ON,OFF,UNKNOWN;
	
	public static BulbStatus fromData(byte data){
		switch (data) {
		case (byte)0xFF:
			return ON;
		case 0x00:
			return OFF;
		default:
			return UNKNOWN;
		}
	}
	
	public static byte[] toData (BulbStatus b) {
		switch (b) {
		case ON:
			return new byte[] {(byte)0, (byte)1};
		default:
			return new byte[] {0, 0};
		}
	}
}
