package org.uu.nl.net2apl.core.agent;

import org.uu.nl.net2apl.core.fipa.MessageInterface;

public interface AgentInterface {
	public void receiveMessage(MessageInterface message);
	public void forceStop();
	
	public String getState();
	
	public void setState(String state);

}
