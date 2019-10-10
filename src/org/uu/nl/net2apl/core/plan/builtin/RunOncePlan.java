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
	public final Object execute(final PlanToAgentInterface planInterface) throws PlanExecutionError {
		Object planAction = executeOnce(planInterface);
		setFinished(true);
		return planAction;
	}
	
	public abstract Object executeOnce(final PlanToAgentInterface planInterface) throws PlanExecutionError;
}