package org.uu.nl.net2apl.core.deliberation;
 
import org.uu.nl.net2apl.core.agent.Agent;
import org.uu.nl.net2apl.core.agent.AgentID;
import org.uu.nl.net2apl.core.plan.PlanExecutionError;
import org.uu.nl.net2apl.core.platform.Platform; 
/**
 * A deliberation runnable implements how an agent is executed. This is done by 
 * grabbing the agent's deliberation cycle and execute each step. Then, if the 
 * agent is done, it will not reschedule itself, otherwise it will reschedule itself.
 *
 * @author Bas Testerink
 */
public final class DeliberationRunnable implements Runnable { 
	/** Interface to obtain the relevant agent's data. */
	private final Agent agent;
	/** Interface to the relevant platform functionalities. */
	private final Platform platform;

	/**
	 * Creation of the deliberation runnable will also result in the setting of a self-rescheduler for this runnable  
	 * through the agent interface. 
	 * @param agent
	 * @param platform
	 */
	public DeliberationRunnable(final Agent agent, final Platform platform){
		this.agent = agent;
		this.platform = platform;
		this.agent.setSelfRescheduler(new SelfRescheduler(this));
	}

	/**
	 * Run the deliberation cycle of the agent once. Will ask the platform
	 * to execute again sometime in the future if the agent is not done
	 * according to its <code>isDone</code> method. The agent is killed
	 * in case it is done, or if a <code>DeliberationStepException</code> occurs.
	 * If the agent is done or if a deliberation step exception occurs, then it will be
	 * killed and removed from the platform.
	 */
	@Override
	public void run(){
		if(!this.agent.isDone()){ // Check first if agent was killed outside of this runnable
			try {   
				// Go through the cycle and execute each step.
				// Note that the deliberation cycle cannot change at runtime.  
				for(DeliberationStep step : this.agent.getDeliberationCycle()){
					step.execute();
				}

				// If all deliberation steps are finished, then check whether
				// the agent is done, so it can be killed.
				if(this.agent.isDone()){
					Platform.getLogger().log(DeliberationRunnable.class, String.format(
							"Agent %s is done and will be shut down",
							agent.getAID().getUuID()));
					initiateShutdown(this.agent);
				} else {
					if (!this.agent.checkSleeping()) { // If the agents goes to sleep then it will be woken upon any external input (message, external trigger)
						reschedule();
					} else {
						Platform.getLogger().log(DeliberationRunnable.class, String.format("Agent %s going to sleep",
								agent.getAID().getUuID()));
					}
				}
			} catch(DeliberationStepException exception){ 
				// Deliberation exceptions should not occur. The agent is 
				// killed and removed from the platform. All proxy's are
				// notified of the agent's death. The rest of the multi-
				// agent system will continue execution by default.
				Platform.getLogger().log(getClass(), exception);
				this.platform.killAgent(this.agent.getAID());
			}
		} else {
			initiateShutdown(agent);
		}
	}

	/** Perform shutdown plans, and kill agent **/
	private void initiateShutdown(Agent agent) {
		agent.getShutdownPlans().forEach(
				plan -> {
					try {
						agent.executePlan(plan);
					} catch (PlanExecutionError ex) {
						Platform.getLogger().log(plan.getClass(), ex);
						/*TODO?*/
					}
				}
		);
		this.platform.killAgent(agent.getAID());
	}
	
	/** Returns the id of the agent to which this runnable belongs. */
	public final AgentID getAgentID(){ return this.agent.getAID(); }
	
	/** Reschedule this deliberation runnable so it will be executed again in the future. */
	public final synchronized void reschedule(){
		this.platform.scheduleForExecution(this);
	} 
}