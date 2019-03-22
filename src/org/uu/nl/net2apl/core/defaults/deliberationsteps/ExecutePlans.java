package org.uu.nl.net2apl.core.defaults.deliberationsteps;
 

import org.uu.nl.net2apl.core.agent.Agent;
import org.uu.nl.net2apl.core.deliberation.DeliberationStepException;
import org.uu.nl.net2apl.core.plan.Plan;
import org.uu.nl.net2apl.core.plan.PlanExecutionError;
import org.uu.nl.net2apl.core.platform.Platform;

/**
 * Deliberation step for executing the current plans of the agent.
 * @author Bas Testerink
 */
public final class ExecutePlans extends DefaultDeliberationStep { 
	
	public  ExecutePlans(final Agent agent){
		super(agent);
	}
	
	/** This steps executes by going through each of the agent's plans. If the plan is finished 
	 * after its execution, then it is removed. If an error occurs, then a plan execution error
	 * will be inserted as an internal trigger. */
	@Override
	public final void execute() throws DeliberationStepException {
		for(Plan plan : super.agent.getPlans()){ 
			try {
				super.agent.executePlan(plan);
				if(plan.isFinished())
					super.agent.removePlan(plan);
			} catch(PlanExecutionError executionError){ 
				// NOTE: if a plan has an execution error, and a goal is being pursued by the plan, then the goal still is 
				// flagged as being pursued. Therefore it is important to ALWAYS have repair plan schemes for failed goal plan schemes.

				Class c;
				try {
					c = Class.forName(executionError.getStackTrace()[0].getClassName());
				} catch(Exception e) {
					c = getClass();
				}
				Platform.getLogger().log(c, String.format(
						"Error during exeuction of plan %s. IMPORTANT: If this plan is associated with a goal, the plan will NOT BE READOPTED and needs a recovery plan",
						plan.getClass().toString()));
				Platform.getLogger().log(c, executionError);

				super.agent.removePlan(plan); // Remove plan from execution
				super.agent.addInternalTrigger(executionError); // Add the error
			}
		}
	}
}
