package org.uu.nl.net2apl.core.tick;

import org.uu.nl.net2apl.core.agent.AgentID;
import org.uu.nl.net2apl.core.deliberation.DeliberationRunnable;

import java.util.HashMap;
import java.util.List;

/**
 *
 */
public interface TickExecutor {

    /**
     * Schedules the deliberation cycle of an agent for the next tick
     * @param agentDeliberationRunnable Deliberation cycle to schedule
     * @return True if deliberation cycle could be scheduled. This can
     * fail if the deliberation cycle already exists on the queue,
     * in which case this method returns false without rescheduling the
     * deliberation cycle
     */
    boolean scheduleForNextTick(DeliberationRunnable agentDeliberationRunnable);

    HashMap<AgentID, List<Object>> doTick();

    long getCurrentTick();

    boolean isRunning();

    int getLastTickDuration();

    List<AgentID> getScheduledAgents();

    int getNofScheduledAgents();
}
