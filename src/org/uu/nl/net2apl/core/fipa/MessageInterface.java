package org.uu.nl.net2apl.core.fipa;

import java.util.Collection;

import org.uu.nl.net2apl.core.agent.AgentID;
import org.uu.nl.net2apl.core.agent.Trigger;

public interface MessageInterface extends Trigger {
	
	public Collection<AgentID> getReceiver();

	public AgentID getSender();
	
	public void addUserDefinedParameter(String key, String value);
	
	public String getUserDefinedParameter(String key);
	
	public String getContent();
}
