package org.uu.nl.net2apl.core.tick;

import org.uu.nl.net2apl.core.agent.AgentID;
import org.uu.nl.net2apl.core.platform.Platform;

import java.util.HashMap;
import java.util.List;

public class DefaultSimulationEngine {

    private final long nIterations;
    private final TickHookProcessor tickHookProcessor;
    private final Platform platform;
    private final TickExecutor executor;

    public DefaultSimulationEngine(Platform platform, TickHookProcessor hookProcessor, long nIterations) {
        this.nIterations = nIterations;
        this.platform = platform;
        this.tickHookProcessor = hookProcessor;
        this.executor = platform.getTickExecutor();
    }

    public boolean start() {
        for(long i = 0; i < this.nIterations; i++) {
            doTick();
        }
        return true;
    }

    private void doTick() {
        long tick = this.executor.getCurrentTick();
        this.tickHookProcessor.tickPreHook(tick);
        HashMap<AgentID, List<Object>> agentActions = this.executor.doTick();
        this.tickHookProcessor.tickPostHook(tick, executor.getLastTickDuration(), agentActions);
    }
}
