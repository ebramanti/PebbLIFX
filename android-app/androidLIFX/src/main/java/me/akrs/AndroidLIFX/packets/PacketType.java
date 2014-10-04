package me.akrs.AndroidLIFX.packets;

public enum PacketType {
	DISCOVER_REQUEST,DISCOVER_RESPONSE,ON_OFF_REQUEST,ON_OFF_RESPONSE,GET_LABEL_REQUEST,SET_LABEL_REQUEST,LABEL_RESPONSE,GET_STATUS_REQUEST,SET_STATE_REQUEST,SET_ABS_BRIGHTNESS_REQUEST,SET_REL_BRIGHTNESS_REQUEST,STATUS_RESPONSE,UNKNOWN;
	
	public byte toData() {
		switch (this) {
		case DISCOVER_REQUEST:
			return 0x02;
		case DISCOVER_RESPONSE:
			return 0x03;
		case ON_OFF_REQUEST:
			return 0x15;
		case ON_OFF_RESPONSE:
			return 0x16;
		case GET_LABEL_REQUEST:
			return 0x17;
		case SET_LABEL_REQUEST:
			return 0x18;
		case LABEL_RESPONSE:
			return 0x19;
		case GET_STATUS_REQUEST:
			return 0x65;
		case SET_STATE_REQUEST:
			return 0x66;
		case SET_ABS_BRIGHTNESS_REQUEST:
			return 0x68;
		case SET_REL_BRIGHTNESS_REQUEST:
			return 0x69;
		case STATUS_RESPONSE:
			return 0x6B;
		default:
			return 0x00;
		}
	}
	
	public static PacketType fromData (byte data) {
		switch (data) {
		case 0x02:
			return DISCOVER_REQUEST;
		case 0x03:
			return DISCOVER_RESPONSE;
		case 0x15:
			return ON_OFF_REQUEST;
		case 0x16:
			return ON_OFF_RESPONSE;
		case 0x17:
			return GET_LABEL_REQUEST;
		case 0x18:
			return SET_LABEL_REQUEST;
		case 0x19:
			return LABEL_RESPONSE;
		case 0x65:
			return GET_STATUS_REQUEST;
		case 0x68:
			return SET_ABS_BRIGHTNESS_REQUEST;
		case 0x69:
			return SET_REL_BRIGHTNESS_REQUEST;
		case 0x66:
			return SET_STATE_REQUEST;
		case 0x6B:
			return STATUS_RESPONSE;
		default:
			return UNKNOWN;
		}
	}
	
}
