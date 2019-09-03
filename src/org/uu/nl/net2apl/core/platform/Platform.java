package org.uu.nl.net2apl.core.platform;

import org.uu.nl.net2apl.core.agent.Agent;
import org.uu.nl.net2apl.core.agent.AgentCreationFailedException;
import org.uu.nl.net2apl.core.agent.AgentID;
import org.uu.nl.net2apl.core.agent.AgentKillSwitch;
import org.uu.nl.net2apl.core.defaults.messenger.DefaultMessenger;
import org.uu.nl.net2apl.core.deliberation.DeliberationRunnable;
import org.uu.nl.net2apl.core.fipa.ams.DirectoryFacilitator;
import org.uu.nl.net2apl.core.logging.ConsoleLogger;
import org.uu.nl.net2apl.core.logging.Loggable;
import org.uu.nl.net2apl.core.messaging.Messenger;

import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * A Platform is a container that maintains the available thread pool, agent factories, 
 * agent kill switches (to stop an agent from outside itself) and a messenger service. 
 * Operating the platform by code is done through an <code>AdminToPlatformInterface</code>.
 * 
 * @author Bas Testerink
 */
public final class Platform {

	private static final int defaultPort = 44444;
	private static Loggable logger = new ConsoleLogger();
	
	public static Loggable getLogger() {
		return logger;
	}
	
	public void setLogger(Loggable logger) {
		Platform.logger = logger;
	}
	
	private static String GetInitialLocalHost() {
		String result = "";
		try {
			result = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException ex) {
			result = "127.0.0.1";
		}
				
		return result;
	}
	

	private String host;
	private int port;
	
	/** The thread pool that is used to execute agents. */
	private final ExecutorService threadPool;
	/** The factories that can produce components from which agents are made. */
	/** Kill switches that can force an agent to stop executing the next time it wants to deliberate. */
	private final Map<AgentID, AgentKillSwitch> agentKillSwitches; 
	/** The messenger that is used for direct communication between agents. */
	private final Messenger<?> messenger; 
	/** The Registered Agents */
	private final Map<AgentID, Agent> registeredAgents;
	private final Map<String, AgentID> test;
	/** Local(!) DirectoryFacilitator(s) */
	private final Map<AgentID, Agent> directoryFacilitators; 	// TODO Don't forget to take the DFs into account when the platform is made distributed!
	/** IDs of Remote DirectoryFascilitators */
	private final Set<AgentID> remoteDfs;
	private final ArrayList<String> remoteHosts;
	private final ArrayList<Integer> remotePorts;
	
	/**
	 * Sets the threadpool to a new FixedThreadPool with the given amount of execution threads. 
	 * @param nrOfExecutionThreads Number of execution threads that are available for executing agents.
	 * @param messenger Messenger that agents will use to communicate.
	 */
	private Platform(final int nrOfExecutionThreads, final Messenger<?> messenger){
		this.threadPool = Executors.newFixedThreadPool(nrOfExecutionThreads);
		this.messenger = messenger;
		this.agentKillSwitches = new HashMap<>(); 
		this.registeredAgents=new HashMap<>();
		this.directoryFacilitators = new HashMap<>();
		this.remoteDfs = new HashSet<>(); 
		this.test = new HashMap<>();
		this.remoteHosts = new ArrayList<>();
		this.remotePorts = new ArrayList<>();
	}

	/**
	 * Create a new <code>Platform</code> and return the administrator's interface
	 * for it. This interface exposes all methods to maintain agent factories,
	 * produce agents, obtain external interfaces to agents and halt agents.
	 * @param nrOfExecutionThreads Maximum number of threads that this platform will use.
	 * @param messenger Messenger for agent to agent communication. Will be the default messenger in case the argument is null.
	 * @return An interface to control the platform.
	 */
	public final static Platform newPlatform(final int nrOfExecutionThreads, final Messenger<?> messenger, String host, int port, ArrayList<String> otherHosts, ArrayList<Integer> otherPorts) {
		if (host == null || host == "") {
			host = GetInitialLocalHost();
		}
		if (port < 0) {
			port = defaultPort;
		}
		Platform platform;
		if(messenger == null){
			platform = new Platform(nrOfExecutionThreads, new DefaultMessenger());
		} else if (! messenger.implementsEncoding()) {
			platform = new Platform(nrOfExecutionThreads, messenger);
		} else {
			platform = new Platform(nrOfExecutionThreads, new NetNode<>(messenger, host, port));
		}
		platform.host = host;
		platform.port = port;
		platform.setLogger(logger);

		if (otherHosts != null && otherPorts != null) {
			platform.remoteHosts.addAll(otherHosts);
			platform.remotePorts.addAll(otherPorts);
		    //platform.remoteDfs.addAll(initialOtherDfs);
		}

		return platform;
	}

	public final static Platform newPlatform(final int nrOfExecutionThreads, final Messenger<?> messenger, String host, int port) {
		return newPlatform(nrOfExecutionThreads, messenger, host, port, null, null);
	}
	
	public final static Platform newPlatform(final int nrOfExecutionThreads, final Messenger<?> messenger) {
		return newPlatform(nrOfExecutionThreads, messenger, GetInitialLocalHost(), defaultPort);
	}
	
	public final String getHost() {
		return host;
	}
	
	public final int getPort() {
		return port;
	}
	
	//////////////////////////
	//// AMS FUNCTIONALITY ///
	//////////////////////////

	public synchronized void register(Agent agent) {
		getLogger().log(getClass(), "Registering agent " + agent.getAID().getUuID());
		
		DeliberationRunnable deliberationRunnable = new DeliberationRunnable(agent, this);
		AgentKillSwitch killSwitch = new AgentKillSwitch(agent);
		this.agentKillSwitches.put(agent.getAID(), killSwitch);
	
		//Register the agent to the platform
		this.registeredAgents.put(agent.getAID(), agent);
		this.test.put(agent.getAID().getUuID(), agent.getAID());
		agent.setPlatform(this);
	
		//Add to platform's messenger
		this.messenger.register(agent);
		scheduleForExecution(deliberationRunnable);
	}
	
	public synchronized void deregister(Agent agent) {
		getLogger().log(getClass(), "Deregistering agent " + agent.getAID().getUuID());
		
		this.agentKillSwitches.remove(agent.getAID());
		this.registeredAgents.remove(agent.getAID());
		this.messenger.deregister(agent.getAID());
		this.test.remove(agent.getAID().getUuID());
		this.directoryFacilitators.remove(agent.getAID()); // <- Just in case it was a DF.
	}
	
	public synchronized void modify(AgentID oldID, Agent agent) {		
		getLogger().log(getClass(), "Modifying agent " + agent.getAID().getUuID());
		
		AgentKillSwitch killSwitch = this.agentKillSwitches.remove(oldID);
		this.agentKillSwitches.put(agent.getAID(), killSwitch);

		this.registeredAgents.remove(oldID);
		this.registeredAgents.put(agent.getAID(), agent);
	
        if (directoryFacilitators.containsKey(oldID)) {
    		this.directoryFacilitators.remove(oldID);
    		this.directoryFacilitators.put(agent.getAID(), agent);
        }

		this.messenger.deregister(oldID);
		this.messenger.register(agent);
	}
	
	public synchronized void updateNickName(AgentID agentID) {
		Agent agent = registeredAgents.get(agentID);
		agent.setAID(agentID);
	}
	
	public Agent search(AgentID id) {
		return this.registeredAgents.get(id);
	} 
	
	public String getDescription() {
		// TODO: Platform description.
		return "";
	}
	
	/// END AMS FUNCTIONALITY ///
	
	// TODO(rbu) modify / deregister / etc.
	
	public DirectoryFacilitator newDirectoryFacilitator() throws AgentCreationFailedException, URISyntaxException {
		DirectoryFacilitator df = new DirectoryFacilitator(this, this.remoteDfs);
//		directoryFacilitators.put(df.getAID(), df);
		
		if (this.messenger instanceof NetNode) {
			NetNode nn = (NetNode) this.messenger;

//			for (AgentID remoteDF : this.remoteDfs) {
//			nn.requestRemoteID(remoteDF.getHost(), remoteDF.getPort());
//		}	
			int n = Math.min(this.remoteHosts.size(), this.remotePorts.size());
			for (int i = 0; i < n; ++i) {
				nn.requestRemoteID(this.remoteHosts.get(i), this.remotePorts.get(i));
			}
			// TODO: Don't sent this to a DF you've already sent to.
		}
		
		return df;
	}
	
	public DirectoryFacilitator newDirectoryFacilitator(AgentID agentID) throws AgentCreationFailedException {
		DirectoryFacilitator df = new DirectoryFacilitator(this, this.remoteDfs, agentID);
		return df;
	}
	
	public void registerDirectoryFacilitator(DirectoryFacilitator df) throws AgentCreationFailedException {
		directoryFacilitators.put(df.getAID(), df);
	}
	
	public Set<AgentID> getLocalDirectoryFacilitators() {
		return directoryFacilitators.keySet();
	}
	
	////////////////////////////////
	//// EXECUTION FUNCTIONALITY ///
	////////////////////////////////

	/**
	 * Will schedule the deliberation runnable (that executes an agent's deliberation cycle)
	 * for execution in the thread pool. If the pool is already shut down, then the agent will
	 * be killed.
	 * @param deliberationRunnable Deliberation cycle to be executed sometime in the future.
	 */
	public final void scheduleForExecution(final DeliberationRunnable deliberationRunnable){
		boolean scheduled = false;
		synchronized(this.threadPool){
			if(!this.threadPool.isShutdown()){
				scheduled = true;
				ThreadPoolExecutor exec = (ThreadPoolExecutor) this.threadPool;
				if(exec.getQueue().contains(deliberationRunnable)) {
					// TODO: Dirty hack, but don't reschedule already scheduled runnable( instance)s to avoid grid lock
					return;
				}
				this.threadPool.execute(deliberationRunnable);
			}
		}
		// If the thread pool was already shut down, then kill the agent
		if(!scheduled){
			killAgent(deliberationRunnable.getAgentID());
		}
	}

	/**
	 * Removes the agent's references in the platform and notifies the agent so that it will
	 * stop executing after the current/next deliberation cycle.
	 * @param agentID ID of the agent to be killed.
	 */
	public final void killAgent(final AgentID agentID){  
		AgentKillSwitch killSwitch;
		synchronized(this.agentKillSwitches){
			killSwitch = this.agentKillSwitches.remove(agentID);
		}
		if(killSwitch != null){// It's okay if the switch is null. In that case the agent was already killed in the past.
			killSwitch.killAgent(); 
		}
		synchronized(this.registeredAgents){
			this.registeredAgents.remove(agentID);
		}
	}

	/**
	 * Will cause all scheduled deliberation cycles to execute, but no more new cycles
	 * are allowed. Those cycles which want to execute after this call will have their
	 * agent be killed.
	 */
	public final void haltPlatform() {
		getLogger().log(getClass(), "Halting platform");
		
		synchronized(this.threadPool){ // Synchronized, otherwise an agent could be scheduled after a shutdown
			this.threadPool.shutdown();
		}
	}
	
	public List<AgentID> getLocalAgentsList(){
		return new ArrayList<>(this.registeredAgents.keySet()); //return new ArrayList<>(this.registeredAgents.values());
	}
	public Set<AgentID> getLocalAgentsSet(){
		return new HashSet<>(this.registeredAgents.keySet()); //return new ArrayList<>(this.registeredAgents.values());
	}
	public Map<AgentID,Agent> getAgents(){
		return this.registeredAgents;
	}
	public Agent getLocalAgent(String localname) throws URISyntaxException {
		return this.registeredAgents.get(this.test.get(localname));
	}
	public Agent getLocalAgent(UUID localname) {
		return this.registeredAgents.get(this.test.get(localname.toString()));
	}
	public Agent getLocalAgent(AgentID aid) throws URISyntaxException{
		return this.registeredAgents.get(aid);
	}
	
	@SuppressWarnings("rawtypes")
	public Messenger getMessenger() {
		return messenger;
	}

}