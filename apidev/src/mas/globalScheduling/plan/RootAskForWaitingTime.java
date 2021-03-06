package mas.globalScheduling.plan;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import mas.job.job;
import mas.util.AgentUtil;
import mas.util.ID;
import mas.util.MessageIds;
import mas.util.ZoneDataUpdate;
import bdi4jade.core.BDIAgent;
import bdi4jade.message.MessageGoal;
import bdi4jade.plan.PlanBody;
import bdi4jade.plan.PlanInstance;
import bdi4jade.plan.PlanInstance.EndState;

public class RootAskForWaitingTime extends Behaviour implements PlanBody {

	private job dummyJob;
	private AID blackboard;
	private int NoOfMachines;
	private String msgReplyID;
	private MessageTemplate mt;
	private int step = 0;
	private int MachineCount;
	protected Logger log;
	private ACLMessage[] WaitingTime;
	private int repliesCnt = 0; // The counter of replies from seller agents
	private job JobToSend;
	private long CumulativeWaitingTime=0;

	@Override
	public void init(PlanInstance PI) {
		log=LogManager.getLogger();
		
		try {
			dummyJob=(job)((MessageGoal)PI.getGoal()).getMessage().getContentObject();
		} catch (UnreadableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		blackboard=(AID)PI.getBeliefBase().getBelief(ID.Blackboard.LocalName).getValue();
		
		msgReplyID=Integer.toString(dummyJob.getJobNo());
		mt=MessageTemplate.and(
				MessageTemplate.MatchConversationId(MessageIds.msgWaitingTime),
				MessageTemplate.MatchReplyWith(msgReplyID));
//		mt=MessageTemplate.MatchConversationId(MessageIds.msgWaitingTime);

	}

	@Override
	public void action() {
		switch (step) {
		case 0:

			this.MachineCount=(int)((BDIAgent)myAgent).getRootCapability().
			getBeliefBase().getBelief(ID.GlobalScheduler.BeliefBaseConst.NoOfMachines).getValue();
			
			if(MachineCount!=0){
				step = 1;
			}
			break;
			
		case 1:
			
			ZoneDataUpdate update=new ZoneDataUpdate.Builder(ID.GlobalScheduler.ZoneData.GetWaitingTime)
			.value(dummyJob).setReplyWith(msgReplyID).Build();
			AgentUtil.sendZoneDataUpdate(blackboard, update, myAgent);
			WaitingTime=new ACLMessage[MachineCount];
			
			step=2;
			break;
			
		case 2:
			try{
				ACLMessage reply = myAgent.receive(mt);
				if (reply != null) {
					WaitingTime[repliesCnt]=reply;
					repliesCnt++;
//					log.info("got waiting time from "+ reply.getSender().getLocalName());

					if (repliesCnt == MachineCount) {				
						step = 3; 
						repliesCnt=0;
					}
				}
				else {
//					log.info("mt="+mt);
					block();
				}
			}
			catch (Exception e3) {

			}
			break;
		case 3:
			try {
				ACLMessage max=getWorstWaitingTime(WaitingTime);
				CumulativeWaitingTime=CumulativeWaitingTime+
						((job)max.getContentObject()).getWaitingTime();
				
				JobToSend=(job)(max.getContentObject());
				
				dummyJob.IncrementOperationNumber();
//				log.info(dummyJob.getCurrentOperationNumber()+"/"+dummyJob.getOperations().size());

				if(dummyJob.getCurrentOperationNumber()<dummyJob.getOperations().size()){
					step=1;
				}
				else{
					step = 4;
				}

			} catch (UnreadableException e) {
				e.printStackTrace();
			}

			
			break;
			
		case 4:
			JobToSend.setCurrentOperationNumber(0);
			JobToSend.setWaitingTime(CumulativeWaitingTime);
			
			if(JobToSend.getWaitingTime()<0){
				log.info("cannot process job no "+JobToSend.getJobNo());
			}
			else{
				log.info("sending waiting time:"+CumulativeWaitingTime+" ms");
				ZoneDataUpdate NegotiationUpdate=new ZoneDataUpdate.Builder(ID.GlobalScheduler.ZoneData.GSAjobsUnderNegaotiation)
				.value(JobToSend).setReplyWith(msgReplyID).Build();
				AgentUtil.sendZoneDataUpdate(blackboard, NegotiationUpdate, myAgent);	
			}
			step=5;
			break;

		}   
	}

	public ACLMessage getWorstWaitingTime(ACLMessage[] WaitingTime ) {
		ACLMessage MaxwaitingTimeMsg=WaitingTime[0]; 
		for(int i = 0; i<WaitingTime.length;i++){

			try {
				if(((job)(WaitingTime[i].getContentObject())).
						getWaitingTime() > ((job)(MaxwaitingTimeMsg.
								getContentObject())).getWaitingTime()){
					MaxwaitingTimeMsg=WaitingTime[i];
				}
			} catch (UnreadableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return MaxwaitingTimeMsg; //return maximum of all waiting times recieved from LSAs
	}

	@Override
	public boolean done() {

		return (step==5);
	}
	
	@Override
	public EndState getEndState() {
		if(step==5){
			return EndState.SUCCESSFUL;
		}
		else{
			return EndState.FAILED;
		}
		
	}
}