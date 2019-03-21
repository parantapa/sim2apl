package org.uu.nl.net2apl.core.fipa;

/**
 * FIPA complient agent states
 * {@see http://www.fipa.org/specs/fipa00023/SC00023J.html} Section 5.1
 */
public enum FIPAAgentState {

    /**
     String constant for the <code>initiated</code> agent life-cycle
     state.

     When the agent is created or installed, it is put in <code>INITIATED</code> state.
     */
    INITIATED(false, false),

    /**
     String constant for the <code>active</code> agent life-cycle
     state.

     This state means the deliberation cycle of the agent should continue.
     */
    ACTIVE(false, false),

    /**
     String constant for the <code>suspended</code> agent life-cycle
     state.

     The agent can be put in the <code>SUSPENDED</code> state by the <code>SUSPEND</code> action, which can only be
     initiated by the agent itself, or by the Agent Messaging Service (AMS). From this state, the agent can be brought
     back in the <code>ACTIVE</code> state with the <code>RESUME</code> action, which can only be initiated by the AMS.
     */
    SUSPENDED(true, true),

    /**
     String constant for the <code>waiting</code> agent life-cycle
     state.

     This can only be initiated by an agent. The agent can be brought back in the active state with the
     <code>WAKE UP</code> action, which also can only be Agent itself.
     */
    WAITING(true, true),

    /**
     String constant for the <code>transit</code> agent life-cycle
     state.

     This state is only relevant for mobile agents {@see http://www.fipa.org/specs/fipa00005/index.html}. When the agent
     transports itself to another location, it puts itself in the <code>TRANSIT</code> state through the <code>MOVE</code>
     action. In this state, no deliberation can take place. The agent can be brought back to the <code>ACTIVE</code>
     state through the <code>EXECUTE</code> action. Both actions can be executed only by the agent.
     */
    TRANSIT(true, false);

	FIPAAgentState(boolean sleep, boolean activateOnMessage) {
	    this.sleep = sleep;
	    this.activateOnMessage = activateOnMessage;
    }

    private boolean sleep;
	private boolean activateOnMessage;

    /**
     * Get whether this state should suspend all operation of the agent
     * @return True iff all operation of the agent should be suspended in this state until further notice
     */
	public boolean getShouldSleep() {
	    return this.sleep;
    }

    /**
     * If the agent receives a message, should the agent be brought back into the <code>ACTIVE</code> state from this
     * state? I.e. can the AMS move the agent from this state to the <code>ACTIVE</code> state?
     * @return True iff agent should be brought into active state
     */
    public boolean activateOnMessage() {
	    return this.activateOnMessage;
    }

}
