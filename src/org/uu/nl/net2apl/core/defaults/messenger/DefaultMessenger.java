package org.uu.nl.net2apl.core.defaults.messenger;

import java.util.HashMap;
import java.util.Map;

import org.uu.nl.net2apl.core.agent.Agent;
import org.uu.nl.net2apl.core.agent.AgentID;
import org.uu.nl.net2apl.core.fipa.MessageInterface;
import org.uu.nl.net2apl.core.messaging.Messenger;
/**
 * The default messenger is a very simple implementation for communication between 
 * agents on the same JVM instance. 
 * 
 * @author Bas Testerink
 */
public final class DefaultMessenger implements Messenger<MessageInterface> { 
	/** Stores the interfaces to agents to inject messages. */
	private final Map<AgentID, Agent> agents;

	public DefaultMessenger(){
		this.agents = new HashMap<AgentID,Agent>(); 
	} 

	/** Store the agent interface. */
	@Override
	public final void register(Agent agent){ 
		synchronized(this.agents){ 
			this.agents.put(agent.getAID(), agent);
		}
	}

	/** Remove the agent interface from the messenger. */
	@Override
	public final void deregister(final AgentID agentID){ 
		synchronized(this.agents){ 
			this.agents.remove(agentID); 
		}
	} 
	
	/** Grab the agent interface of the receiver and add the message in the receiving agent. */
	public final void sendMessage(final AgentID receiver, final MessageInterface message) throws MessageReceiverNotFoundException{
		synchronized(this.agents){
			Agent agent = this.agents.get(receiver);
			if(agent == null){
				//TODO send message to sender that receiver is unknown instead of exception
				throw new MessageReceiverNotFoundException("Trying to send to non-existent agent "+receiver+".");
			} else {
				agent.receiveMessage(message);
			}
		} 
	}

	@Override
	public void agentDied(AgentID agentID) {
		this.deregister(agentID);		
	}

	@Override
	public void deliverMessage(AgentID receiver, MessageInterface message) throws MessageReceiverNotFoundException {
		this.sendMessage(receiver, message);
		
	}

	@Override
	public void deliverMessage(MessageInterface message) throws MessageReceiverNotFoundException {
		if(message.getReceiver()!=null && message.getReceiver().size()>0) {
			for (AgentID toID : message.getReceiver()) {
				this.sendMessage(toID, message);
			}
		}else {
			for (AgentID agentID : agents.keySet()) {
				this.sendMessage(agentID, message);
			}
		}
	}
	
	@Override
	public boolean implementsEncoding() {
		return false;
	}
	
	@Override
	public byte[] encodeMessage(MessageInterface message) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("DefaultMessage does not implement encode/decode.");
	}

	@Override
	public MessageInterface decodeMessage(byte[] asBytes) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("DefaultMessage does not implement encode/decode.");
	}
} 