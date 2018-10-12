package org.uu.nl.net2apl.core.fipa.ams;

import org.uu.nl.net2apl.core.agent.AgentID;
import org.uu.nl.net2apl.core.agent.Trigger;

public class ReceiveRemoteAddress implements Trigger {

	private AgentID agentID;
	public AgentID getAgentID() { return agentID; }
	
	public ReceiveRemoteAddress(AgentID agentID) {
		this.agentID = agentID;
	}
}
