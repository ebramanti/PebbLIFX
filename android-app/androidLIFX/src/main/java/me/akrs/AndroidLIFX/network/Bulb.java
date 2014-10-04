package me.akrs.AndroidLIFX.network;

import java.io.IOException;

import me.akrs.AndroidLIFX.packets.request.OnOffRequest;
import me.akrs.AndroidLIFX.packets.request.SetLabelRequest;
import me.akrs.AndroidLIFX.packets.request.SetStateRequest;
import me.akrs.AndroidLIFX.utils.BulbStatus;
import me.akrs.AndroidLIFX.utils.MacAddress;
import me.akrs.AndroidLIFX.utils.java.Logger;

public class Bulb {
	private BulbNetwork net;
	private MacAddress macAddress;
	protected short hue;
	protected short saturation;
	protected short luminance;
	protected short temperature;
	protected short fadeTime;
	protected BulbStatus status;
	protected String name;
	
	public Bulb (MacAddress mac, BulbNetwork net) {
		this.macAddress = mac;
		this.net = net;
		this.fadeTime = 1;
	}
	
	public Bulb (MacAddress mac, BulbNetwork net, short fadeTime) {
		this.macAddress = mac;
		this.net = net;
		this.fadeTime = fadeTime;
	}
	
	public MacAddress getMacAddress () {
		return macAddress;
	}
	
	public String toString () {
		return "Bulb: Name:" + this.getName() + " Mac:" + this.macAddress.toString();
	}

	public short getHue () {
		return hue;
	}
	
	public void setHue (short hue) throws IOException {
		this.setHue(hue, (short)-1);
	}

	public void setHue (short hue, short saturation) throws IOException {
		this.saturation = saturation;
		this.hue = hue;
		this.setState();
	}

	public short getSaturation () {
		return saturation;
	}

	public void setSaturation (short saturation) {
		this.saturation = saturation;
		try {
			this.setState();
		} catch (IOException e) {
			Logger.log("Unable to set hue", e);
		}
	}

	public short getLuminance() {
		return luminance;
	}

	public void setLuminance (short luminance) {
		this.luminance = luminance;
		try {
			this.setState();
		} catch (IOException e) {
			Logger.log("Unable to set hue", e);
		}
	}

	public short getTemperature () {
		return temperature;
	}

	public void setTemperature (short temperature) {
		this.temperature = temperature;
		try {
			this.setState();
		} catch (IOException e) {
			Logger.log("Unable to set hue", e);
		}
	}

	public BulbStatus getStatus () {
		return status;
	}

	public void setStatus (BulbStatus status) throws IOException {
		this.status = status;
		net.gatewayOutStream.write(new OnOffRequest(this.macAddress, net.gatewayMac, this.status).getBytes());
	}

	public String getName () {
		return name;
	}
	
	public void on () throws IOException {
		this.setStatus(BulbStatus.ON);
	}
	
	public void off () throws IOException {
		this.setStatus(BulbStatus.OFF);
	}
	
	public void setBrightness (short s) throws IOException {
		this.luminance = s;
		this.setState();
	}

	public void setName (String name) {
		this.name = name;
		try {
			net.gatewayOutStream.write(new SetLabelRequest(this.macAddress, this.net.gatewayMac, this.name).getBytes());
		} catch (IOException e) {
			Logger.log("Unable to set name", e);
		}
	}

	public boolean equals (Object o) {
		if (o == null || !(o instanceof Bulb)) {
			return false;
		}
		
		Bulb b = (Bulb)o;
		return this.macAddress.equal(b.macAddress);
	}
	
	public void setState (short hue, short saturation, short luminance, short whiteColor, short fadeTime) throws IOException {
		this.hue = hue;
		this.saturation = saturation;
		this.luminance = luminance;
		this.temperature = whiteColor;
		this.fadeTime = fadeTime;
		this.setState();
	}
	
	private void setState () throws IOException {
		net.gatewayOutStream.write(new SetStateRequest(this.macAddress, net.gatewayMac, this.hue, this.saturation, this.luminance, this.temperature, this.fadeTime).getBytes());
	}
}
