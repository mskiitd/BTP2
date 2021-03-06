package mas.localScheduling.plan;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import mas.job.job;
import mas.util.AgentUtil;
import mas.util.ID;
import mas.util.ZoneDataUpdate;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.UnreadableException;
import bdi4jade.core.BeliefBase;
import bdi4jade.message.MessageGoal;
import bdi4jade.plan.PlanBody;
import bdi4jade.plan.PlanInstance;
import bdi4jade.plan.PlanInstance.EndState;

public class ReceiveCompletedJobPlan extends OneShotBehaviour implements PlanBody {

	/**
	 * Takes the complete job from the simulator
	 * 
	 */
	
	private static final long serialVersionUID = 1L;
	private job j;
	private ArrayList<job> jobQueue;
	private BeliefBase bfBase;
	private StatsTracker sTracker;
	private Logger log;
	private AID blackboard_AID;
	

	@Override
	public void init(PlanInstance pInstance) {
		
		log = LogManager.getLogger();
		blackboard_AID=(AID)pInstance.getBeliefBase().
				getBelief(ID.LocalScheduler.BeliefBaseConst.blackboardAgent).getValue();
		bfBase = pInstance.getBeliefBase();
		
		try {
			j = (job)((MessageGoal)pInstance.getGoal()).getMessage().getContentObject();
			
		} catch (UnreadableException e) {			
			e.printStackTrace();
		}
		

	}


	@Override
	public void action() {
		bfBase.updateBelief(ID.LocalScheduler.BeliefBaseConst.currentJobOnMachine, null);
		
		j.IncrementOperationNumber();
		ZoneDataUpdate CompletedJobUpdate=new ZoneDataUpdate.Builder(ID.LocalScheduler.ZoneData.finishedJob)
		.value(j).setReplyWith(Integer.toString(j.getJobNo())).Build();
	
		AgentUtil.sendZoneDataUpdate(blackboard_AID, CompletedJobUpdate, myAgent);
	}
	
	@Override
	public EndState getEndState() {
		return EndState.SUCCESSFUL;
	}
}
