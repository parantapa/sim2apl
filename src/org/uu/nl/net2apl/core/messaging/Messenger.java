package org.uu.nl.net2apl.core.messaging;
 
import org.uu.nl.net2apl.core.agent.Agent;
import org.uu.nl.net2apl.core.agent.AgentDeathListener;
import org.uu.nl.net2apl.core.agent.AgentID;
import org.uu.nl.net2apl.core.defaults.messenger.MessageReceiverNotFoundException;
import org.uu.nl.net2apl.core.fipa.MessageInterface;
/**
 * Implement a messenger to allow agents to communicate with each other.
 * 
 * @author Bas Testerink
 * @author Mohammad Shafahi
 */
public interface Messenger<T extends MessageInterface> extends AgentDeathListener {
	/** Intended to make the messenger aware of the agents' existence. Registering is required for the agent to send and receive messages. */
	public void register(final Agent agent); 
	
	/** Deregister to announce that this agent will no longer listen to messages that are received (will also disable the possiblity for sending messages). */
	public void deregister(final AgentID agentID); 
	
	/** Delivers a message to the receiver. Will throw an exception if that receiver is unknown to the messenger. 
	 * Take care that if you implement this method, that then the receiving party obtains this message through its 
	 * messenger to agent interface. */
	public void deliverMessage(final AgentID receiver, final T message) throws MessageReceiverNotFoundException;
	
	/** Delivers a message . The message must contain information about the receiver. If not a message will be either broadcasted to all agents or
	 * an exception should be thrown indicating that the receiver is unknown to the messenger. 
	 * Take care that if you implement this method, that then the receiving party obtains this message through its 
	 * messenger to agent interface. */
	public void deliverMessage(final T message) throws MessageReceiverNotFoundException;
	
	public boolean implementsEncoding();
	
	public byte[] encodeMessage(final T message) throws UnsupportedOperationException;
	
	public T decodeMessage(byte[] asBytes) throws UnsupportedOperationException;
}
