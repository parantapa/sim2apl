package org.uu.nl.net2apl.core.plan.builtin;

import org.uu.nl.net2apl.core.agent.PlanToAgentInterface;
import org.uu.nl.net2apl.core.plan.Plan;
import org.uu.nl.net2apl.core.plan.PlanExecutionError;

/**
 * A plan that is automatically set to finished after it has been executed. 
 * 
 * @author Bas Testerink
 */
public abstract class RunOncePlan extends Plan {
 
	@Override
	public final void execute(final PlanToAgentInterface planInterface) throws PlanExecutionError {
		executeOnce(planInterface);
		setFinished(true);
	}
	
	public abstract void executeOnce(final PlanToAgentInterface planInterface) throws PlanExecutionError; 
}