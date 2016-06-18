package de.kreth.clubhelperclient.core;

import org.springframework.stereotype.Component;

@Component
public class RemoteHolder {

	private String remoteUrl = "http://localhost:8081/clubhelperbackend";
	
	public synchronized void setRemoteUrl(String remoteUrl) {
		this.remoteUrl = remoteUrl;
	}
	
	public synchronized String getRemoteUrl() {
		return remoteUrl;
	}
}
