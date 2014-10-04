package me.akrs.AndroidLIFX.packets.responses;

import java.util.Arrays;

import me.akrs.AndroidLIFX.packets.PacketDirection;
import me.akrs.AndroidLIFX.packets.PacketType;
import me.akrs.AndroidLIFX.utils.MacAddress;

public class StandardResponse {
	private int length;
	private PacketDirection direction;
	private MacAddress targetBulb;
	private MacAddress gatewayBulb;
	private PacketType packetType;
	
	public StandardResponse (byte[] packet) {
		this.length = packet[0];
		
		//Set the direction of the data
		this.direction = PacketDirection.fromData(packet[3]);
		this.targetBulb = new MacAddress(Arrays.copyOfRange(packet, 8, 14));
		this.gatewayBulb = new MacAddress(Arrays.copyOfRange(packet, 16, 22));
		
		this.packetType = PacketType.fromData(packet[32]);
	}

	public PacketDirection getDirection () {
		return direction;
	}

	public MacAddress getTargetBulb () {
		return targetBulb;
	}

	public MacAddress getGatewayBulb () {
		return gatewayBulb;
	}

	public PacketType getPacketType () {
		return packetType;
	}
	
	public int getLength () {
		return this.length;
	}
	
	protected static short toShort (byte[] bytes, int startByte) {
		return (short) (bytes[startByte] + bytes[startByte + 1]*0x0100);
	}
	
	public String toString () {
		return packetType.toString();
	}
}
