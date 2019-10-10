package org.uu.nl.net2apl.core.tick;

import org.uu.nl.net2apl.core.agent.AgentID;
import org.uu.nl.net2apl.core.deliberation.DeliberationRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * A default time step executor that uses a ThreadPoolExecutor to run the agents when the tick needs
 * to be performed. This class is thread-safe
 */
public class DefaulThreadpoolTickExecutor implements TickExecutor {

    private long tick = 0;
    private int stepDuration;

    private final ExecutorService executor;
    private final ArrayList<DeliberationRunnable> scheduledRunnables;

    public DefaulThreadpoolTickExecutor(int nThreads) {
        this.executor = Executors.newFixedThreadPool(nThreads);
        this.scheduledRunnables = new ArrayList<>();
    }

    @Override
    public boolean scheduleForNextTick(DeliberationRunnable agentDeliberationRunnable) {
        if (!this.scheduledRunnables.contains(agentDeliberationRunnable)) {
            this.scheduledRunnables.add(agentDeliberationRunnable);
            return true;
        }
        return false;
    }

    @Override
    public HashMap<AgentID, List<Object>> doTick() {
        SynchronousQueue<DeliberationRunnable> runnables = new SynchronousQueue<>();
        // TODO make sure running can only happen once with some sort of mutex?
        synchronized (this.scheduledRunnables) {
            runnables.addAll(this.scheduledRunnables);
            this.scheduledRunnables.clear();
        }

        HashMap<AgentID, List<Object>> agentPlanActions = new HashMap<>();

        long startTime = System.currentTimeMillis();
        for(DeliberationRunnable dr : runnables) {
            try {
                List<Object> currentAgentActions = this.executor.submit(dr).get();
                agentPlanActions.put(
                        dr.getAgentID(),
                        currentAgentActions.stream().filter(Objects::nonNull).collect(Collectors.toList()));
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        this.stepDuration = (int) (System.currentTimeMillis() - startTime);

        tick++;
        return agentPlanActions;
    }

    /**
     *
     * @return
     */
    @Override
    public long getCurrentTick() {
        return this.tick;
    }

    public boolean isRunning() {
        // TODO
        return false;
    }

    /**
     * Get the time it took to execute the last tick cycle
     * @return  Duration of last tick in milliseconds
     */
    @Override
    public int getLastTickDuration() {
        return this.stepDuration;
    }

    /**
     * Get all agents scheduled for the next tick
     * @return  List of AgentIDs scheduled for the next tick
     */
    @Override
    public List<AgentID> getScheduledAgents() {
        List<AgentID> scheduledAgents = new ArrayList<>();
        synchronized (this.scheduledRunnables) {
            for(DeliberationRunnable runnable : this.scheduledRunnables) {
                scheduledAgents.add(runnable.getAgentID());
            }
        }
        return scheduledAgents;
    }

    /**
     * Get the number of agents scheduled for the next tick
     * @return  Number of agents scheduled for the next tick
     */
    @Override
    public int getNofScheduledAgents() {
        synchronized (this.scheduledRunnables) {
            return this.scheduledRunnables.size();
        }
    }
}
