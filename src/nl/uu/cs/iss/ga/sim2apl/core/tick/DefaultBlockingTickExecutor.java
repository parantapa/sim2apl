package nl.uu.cs.iss.ga.sim2apl.core.tick;

import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentID;
import nl.uu.cs.iss.ga.sim2apl.core.deliberation.DeliberationRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * A default time step executor that uses a ThreadPoolExecutor to run the agents when the tick needs
 * to be performed.
 */
public class DefaultBlockingTickExecutor implements TickExecutor {

    /** Internal counters **/
    private int tick = 0;
    private int stepDuration;

    /** The ExecutorService that will be used to execute one sense-reason-act step for all scheduled agents **/
    private final ExecutorService executor;

    /** The list of agents scheduled for the next tick **/
    private final ArrayList<DeliberationRunnable> scheduledRunnables;

    /**
     * Default constructor
     * @param nThreads Number of threads to use to execute the agent's sense-reason-act cycles.
     */
    public DefaultBlockingTickExecutor(int nThreads) {
        this.executor = Executors.newFixedThreadPool(nThreads);
        this.scheduledRunnables = new ArrayList<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean scheduleForNextTick(DeliberationRunnable agentDeliberationRunnable) {
        if (!this.scheduledRunnables.contains(agentDeliberationRunnable)) {
            this.scheduledRunnables.add(agentDeliberationRunnable);
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HashMap<AgentID, List<Object>> doTick() {
        ArrayList<DeliberationRunnable> runnables;
        // TODO make sure running can only happen once with some sort of mutex? How to verify if a tick is currently being executed?
        synchronized (this.scheduledRunnables) {
            runnables = new ArrayList<>(this.scheduledRunnables);
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
     * {@inheritDoc}
     */
    @Override
    public int getCurrentTick() {
        return this.tick;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRunning() {
        // TODO
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getLastTickDuration() {
        return this.stepDuration;
    }

    /**
     * {@inheritDoc}
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
     * {@inheritDoc}
     */
    @Override
    public int getNofScheduledAgents() {
        synchronized (this.scheduledRunnables) {
            return this.scheduledRunnables.size();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown() {
        this.executor.shutdown();
    }
}
