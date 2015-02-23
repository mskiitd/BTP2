package mas.localScheduling.plan;

import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

import java.io.IOException;
import java.util.ArrayList;

import mas.job.job;
import mas.util.ID;
import mas.util.MessageIds;
import mas.util.ZoneDataUpdate;
import bdi4jade.core.BeliefBase;
import bdi4jade.message.MessageGoal;
import bdi4jade.plan.PlanBody;
import bdi4jade.plan.PlanInstance;
import bdi4jade.plan.PlanInstance.EndState;

/**
 * @author Anand Prajapati
 *
 * this picks a job from the queue and sends it to the machine for processing
 */

public class SendJobPlan extends OneShotBehaviour implements PlanBody {

	private static final long serialVersionUID = 1L;
	private ArrayList<job> jobQueue;
	private BeliefBase bfBase;
	private AID blackboard;

	@Override
	public EndState getEndState() {
		return null;
	}

	@Override
	public void init(PlanInstance pInstance) {

		bfBase = pInstance.getBeliefBase();
		ACLMessage msg = ((MessageGoal)pInstance.getGoal()).getMessage();

		jobQueue = (ArrayList<job>) bfBase.
				getBelief(ID.LocalScheduler.BeliefBaseConst.jobQueue).
				getValue();

		this.blackboard = (AID) bfBase.
				getBelief(ID.LocalScheduler.BeliefBaseConst.blackboardAgent).
				getValue();
	}

	@Override
	public void action() {

		if(jobQueue.size() != 0){

			ZoneDataUpdate jobForMachineUpdate = new ZoneDataUpdate(
					ID.LocalScheduler.ZoneData.jobForMachine,
					jobQueue.get(0));

			jobForMachineUpdate.send(blackboard ,jobForMachineUpdate, myAgent);

			jobQueue.remove(0);			
			/**
			 * update the belief base
			 */
			bfBase.updateBelief(ID.LocalScheduler.BeliefBaseConst.jobQueue, jobQueue);		
		}
	}
}
