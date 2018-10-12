package org.uu.nl.net2apl.core.plan.builtin;

import org.uu.nl.net2apl.core.agent.AgentContextInterface;
import org.uu.nl.net2apl.core.agent.Trigger;
/**
 *	An interface for creating FunctionalPlanSchemes. 
 *  @author Bas Testerink
 */
public interface FunctionalPlanSchemeInterface {
	/** Get the plan for a given trigger and context interface. Return SubPlanInterface.UNINSTANTIATED if the plan did not fire. */
	public SubPlanInterface getPlan(Trigger trigger, AgentContextInterface contextInterface);
}
