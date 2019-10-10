package org.uu.nl.net2apl.core.tick;

import org.uu.nl.net2apl.core.agent.AgentID;

import java.util.HashMap;
import java.util.List;

public interface TickHookProcessor {

    void tickPreHook(long startingTick);

    void tickPostHook(long finishedTick, int tickDuration, HashMap<AgentID, List<Object>> producedAgentActions);

}
