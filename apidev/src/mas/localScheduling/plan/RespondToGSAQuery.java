package mas.localScheduling.plan;

import java.util.ArrayList;
import mas.job.job;
import mas.util.AgentUtil;
import mas.util.ID;
import mas.util.ID.Machine;
import mas.util.JobQueryObject;
import mas.util.ZoneDataUpdate;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.UnreadableException;
import bdi4jade.core.BeliefBase;
import bdi4jade.message.MessageGoal;
import bdi4jade.plan.PlanBody;
import bdi4jade.plan.PlanInstance;
import bdi4jade.plan.PlanInstance.EndState;

public class RespondToGSAQuery extends OneShotBehaviour implements PlanBody {

	private int JobNo;
	private BeliefBase beleifBase;
	private AID machineAID=null;
	private AID blackboard_AID;

	@Override
	public EndState getEndState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void init(PlanInstance PI) {
		try {
			JobNo =((JobQueryObject)((MessageGoal)PI.getGoal()).getMessage().getContentObject()).
					getCurrentJob().getJobNo();
		} catch (UnreadableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		beleifBase=PI.getBeliefBase();
		blackboard_AID=(AID)beleifBase.getBelief(ID.LocalScheduler.BeliefBaseConst.blackboardAgent).
				getValue();
		machineAID=(AID)beleifBase.getBelief(ID.LocalScheduler.BeliefBaseConst.machine).
				getValue();
	}

	@Override
	public void action() {
		JobQueryObject response=new JobQueryObject.Builder().currentJob(null)
				.currentMachine(null).build();
		
		ArrayList<job> jobQ=(ArrayList<job>)beleifBase.getBelief(ID.LocalScheduler.BeliefBaseConst.jobQueue).getValue();
		job currentJob=(job)beleifBase.getBelief(ID.LocalScheduler.BeliefBaseConst.currentJobOnMachine).getValue();
		
		for(int i=0;i<jobQ.size();i++){
			if(JobNo==jobQ.get(i).getJobNo()){
				response=new JobQueryObject.Builder().currentJob(jobQ.get(i))
						.underProcess(false)
						.currentMachine(machineAID).build();
			}
		}
		
		if(currentJob.getJobNo()==JobNo){
			response=new JobQueryObject.Builder().currentJob(currentJob).currentMachine(machineAID)
					.underProcess(true)
					.build();
		}
		
		ZoneDataUpdate queryUpdate=new ZoneDataUpdate.Builder(ID.LocalScheduler.ZoneData.QueryResponse).
				value(response).Build();
		AgentUtil.sendZoneDataUpdate(blackboard_AID, queryUpdate, myAgent);
	}

}
