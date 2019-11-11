package nl.uu.cs.iss.ga.sim2apl.core.tick;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import nl.uu.cs.iss.ga.sim2apl.core.deliberation.DeliberationRunnable;

/**
 * An Agent Thread is responsible for producing events on behalf of a specific set of agents.
 */
public class MatrixAgentThread implements Runnable {
    private static final Logger LOG = Logger.getLogger(MatrixAgentThread.class.getName());
    
    private static final int UPDATE_BATCH_SIZE = 100000;
    
    private int agentproc_id = -1;
    private MatrixRPCProxy proxy = null;
    
    public Random random = null;
    public int cur_round = -1;
    
    BlockingQueue<List<DeliberationRunnable>> inq = null;
    private ExecutorService executor = null;
    private Thread thread = null;
    
    MatrixAgentThread(int agentproc_id, String address, int port, ExecutorService executor) {
        LOG.info(String.format("Creating agent thread: %d", agentproc_id));

        this.agentproc_id = agentproc_id;
        this.proxy = new MatrixRPCProxy(address, port);
        this.random = new Random();
        this.inq = new LinkedBlockingQueue<>(1);
        this.executor = executor;

        int seed = this.proxy.get_agentproc_seed(agentproc_id);
        this.random.setSeed(seed);

        this.thread = new Thread(this);
        this.thread.start();
    }
    
    @Override
    public void run() {
        Gson gson = new Gson();
        Type arrayListStringType = new TypeToken<ArrayList<String>>(){}.getType();
        
        try {
            while (true) {
                cur_round = this.proxy.can_we_start_yet(agentproc_id);
                LOG.info(String.format("Agent %d received round %d", agentproc_id, cur_round));
                if (cur_round == -1) {
                    LOG.info(String.format("Agent thread %d: stopping", agentproc_id));
                    return;
                }
                
                long startTime = System.currentTimeMillis();
                long produceTime = 0;
                long sendTime = 0;
                
                List<DeliberationRunnable> runnables = inq.take();
                JsonArray updates = new JsonArray();
                for(DeliberationRunnable dr : runnables) {
                    try {
                        long produceStart = System.currentTimeMillis();
                        List<Object> currentAgentActions = this.executor.submit(dr).get();
                        currentAgentActions = currentAgentActions.stream().filter(Objects::nonNull).collect(Collectors.toList());
                        
                        ArrayList<String> currentAgentActionStrings = new ArrayList<>();
                        for (Object action: currentAgentActions) {
                            currentAgentActionStrings.add((String) action);
                        }
                        String agentID = dr.getAgentID().toString();
                        
                        JsonObject update = new JsonObject();
                        update.addProperty("agentID", agentID);
                        update.add("actions", gson.toJsonTree(currentAgentActionStrings, arrayListStringType));
                        updates.add(update);
                        
                        produceTime += System.currentTimeMillis() - produceStart;
                        
                        if (updates.size() >= UPDATE_BATCH_SIZE) {
                            long sendStart = System.currentTimeMillis();
                            this.proxy.register_events(agentproc_id, updates);
                            updates = new JsonArray();
                            sendTime += System.currentTimeMillis() - sendStart;
                        }
                    } catch (InterruptedException | ExecutionException ex) {
                        LOG.severe("Error running runnable: " + ex.toString());
                        ex.printStackTrace();
                    }
                }
                if (updates.size() > 0) {
                    long sendStart = System.currentTimeMillis();
                    this.proxy.register_events(agentproc_id, updates);
                    sendTime += System.currentTimeMillis() - sendStart;
                }
                long stepDuration = (long) (System.currentTimeMillis() - startTime);
                LOG.info(String.format("Agent thread %d: Round %d: Event production took %d ms (%d, %d)", agentproc_id, cur_round, stepDuration, produceTime, sendTime));
            }
        } catch (InterruptedException ex) {
            LOG.severe("Got Interrupted:" + ex.toString());
            throw new RuntimeException("Got Interrupted: " + ex.toString());
        }
    }
}
