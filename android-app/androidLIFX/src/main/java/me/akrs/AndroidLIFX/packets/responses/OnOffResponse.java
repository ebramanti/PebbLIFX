package me.akrs.AndroidLIFX.packets.responses;

import me.akrs.AndroidLIFX.utils.BulbStatus;

public class OnOffResponse extends StandardResponse {
	
	private BulbStatus status;
	
	public OnOffResponse (byte[] packet) {
		super(packet);
		
		this.status = BulbStatus.fromData(packet[36]);
	}

	public BulbStatus getStatus() {
		return status;
	}
}
