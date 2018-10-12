package org.uu.nl.net2apl.core.defaults.deliberationsteps;

import java.util.List;

import org.uu.nl.net2apl.core.agent.Agent;
import org.uu.nl.net2apl.core.deliberation.DeliberationStepException;
import org.uu.nl.net2apl.core.fipa.MessageInterface;
import org.uu.nl.net2apl.core.plan.PlanScheme;

/**
 * Step that applies the message plan schemes to the messages.
 * @author Bas Testerink
 */
public final class ApplyMessagePlanSchemes extends DefaultDeliberationStep { 
	
	public  ApplyMessagePlanSchemes(final Agent agent){
		super(agent);
	}
	
	/** Simply grab the messages and message plan schemes and try their application. */
	@Override
	public final void execute() throws DeliberationStepException{
		List<MessageInterface> messages = super.agent.getAllMessages(); 
		super.applyTriggerInterceptors(messages, super.agent.getMessageInterceptors());
		List<PlanScheme> planSchemes = super.agent.getMessagePlanSchemes();
		super.applyPlanSchemes(messages, planSchemes);
	}
}