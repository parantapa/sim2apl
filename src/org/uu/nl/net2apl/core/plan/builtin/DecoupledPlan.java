package org.uu.nl.net2apl.core.plan.builtin;

import org.uu.nl.net2apl.core.agent.PlanToAgentInterface;
import org.uu.nl.net2apl.core.agent.Trigger;
import org.uu.nl.net2apl.core.plan.Plan;
import org.uu.nl.net2apl.core.plan.PlanExecutionError;
/**
 * A decoupled plan is a plan where the execute method contains a trigger. The standard use of this class is that the trigger which caused the 
 * plan scheme that instantiated the plan is the trigger which is provided in the execute method at runtime. 
 * 
 * @author Bas Testerink
 */
public abstract class DecoupledPlan extends Plan { 
	/** {@inheritDoc} */
	@Override
	public final void execute(final PlanToAgentInterface planInterface) throws PlanExecutionError {}
	public abstract void execute(final Trigger trigger, final PlanToAgentInterface planInterface) throws PlanExecutionError;
}
