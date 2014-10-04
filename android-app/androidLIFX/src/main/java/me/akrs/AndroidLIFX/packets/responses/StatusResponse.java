package me.akrs.AndroidLIFX.packets.responses;

import me.akrs.AndroidLIFX.utils.BulbStatus;

public class StatusResponse extends StandardResponse {
	
	private short hue;
	private short saturation;
	private short luminance;
	private short temperature;
	private BulbStatus status;
	private String name;

	public StatusResponse (byte[] packet) {
		super(packet);
		
		this.hue = StandardResponse.toShort(packet, 36);
		this.saturation = StandardResponse.toShort(packet, 38);
		this.luminance = StandardResponse.toShort(packet, 40);
		this.temperature = StandardResponse.toShort(packet, 42);
		this.status = BulbStatus.fromData(packet[46]);
		
		StringBuilder sb = new StringBuilder();
		for(int i = 48; i < this.getLength(); i++) {
			sb.append((char)packet[i]);
		}
		
		this.name = sb.toString().trim();
		
	}

	public short getHue () {
		return hue;
	}

	public short getSaturation () {
		return saturation;
	}

	public short getLuminance () {
		return luminance;
	}

	public short getTemperature () {
		return temperature;
	}

	public BulbStatus getStatus () {
		return status;
	}

	public String getName () {
		return name;
	}
}
