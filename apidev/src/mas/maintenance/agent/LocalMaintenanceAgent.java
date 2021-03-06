package mas.maintenance.agent;

import jade.core.AID;
import mas.util.AgentUtil;
import mas.util.ID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import bdi4jade.core.Capability;

public class LocalMaintenanceAgent extends AbstractLocalMaintenanceAgent {

	private static final long serialVersionUID = 1L;
	private Logger log;
	public static int prevMaintPeriod = 50000;

	@Override
	protected void init() {
		super.init();
		log = LogManager.getLogger();

		// Add capability to agent 
		Capability bCap = new MaitenanceBasicCapability();
		addCapability(bCap);
		
		AID bba = AgentUtil.findBlackboardAgent(this);
		bCap.getBeliefBase().updateBelief(
				ID.Maintenance.BeliefBaseConst.blackboardAgentAID, bba);

	}
}
