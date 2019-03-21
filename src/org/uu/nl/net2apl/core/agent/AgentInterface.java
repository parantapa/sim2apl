package org.uu.nl.net2apl.core.agent;

import org.uu.nl.net2apl.core.fipa.FIPAAgentState;
import org.uu.nl.net2apl.core.fipa.MessageInterface;

public interface AgentInterface {
	void receiveMessage(MessageInterface message);
	void forceStop();
	
	FIPAAgentState getState();
}
