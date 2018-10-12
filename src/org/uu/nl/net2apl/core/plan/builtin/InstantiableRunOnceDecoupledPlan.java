package org.uu.nl.net2apl.core.plan.builtin;

import org.uu.nl.net2apl.core.agent.PlanToAgentInterface;
import org.uu.nl.net2apl.core.agent.Trigger;
import org.uu.nl.net2apl.core.plan.PlanExecutionError;
/**
 * This class is a decoupled plan which executes only once and can be constructed with a DecoupledPlanBodyInterface. It is used by the PlanToAgentInterface 
 * built-in functionalities. 
 * 
 * @author Bas Testerink
 * @param <T>
 */
public final class InstantiableRunOnceDecoupledPlan<T extends Trigger> extends DecoupledPlan {
	private final DecoupledPlanBodyInterface<T> body;
	
	public InstantiableRunOnceDecoupledPlan(final DecoupledPlanBodyInterface<T> body){
		this.body = body;
	}
	
	@Override
	public final void execute(final Trigger trigger, final PlanToAgentInterface planInterface) throws PlanExecutionError {
		try {
			@SuppressWarnings("unchecked")
			T cast = (T) trigger;  
			this.body.execute(cast, planInterface);
			this.setFinished(true);
		} catch(ClassCastException e){
			e.printStackTrace();
			throw new PlanExecutionError(); // TODO: design and implement a proper plan execution error that hints that the selector should ensure the correct type
		}
	}
	
}
