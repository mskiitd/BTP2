package mas.globalScheduling.plan;

import java.io.IOException;

import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import mas.blackboard.nameZoneData.NamedZoneData;
import mas.util.AgentUtil;
import mas.util.ID;
import mas.util.MessageIds;
import mas.util.SubscriptionForm;
import bdi4jade.plan.PlanBody;
import bdi4jade.plan.PlanInstance;
import bdi4jade.plan.PlanInstance.EndState;

public class RegisterAgentToBlackboard extends OneShotBehaviour implements PlanBody {
	int step;

	@Override
	public EndState getEndState() {

		return EndState.SUCCESSFUL;
	}

	@Override
	public void init(PlanInstance planInstance) {
		step=0;


	}

	@Override
	public void action() {
		AID bb_aid=AgentUtil.findBlackboardAgent(myAgent);

		ACLMessage msg2=new ACLMessage(ACLMessage.CFP);
		msg2.setConversationId(MessageIds.RegisterMe);

		NamedZoneData ZoneDataName1 = new NamedZoneData.Builder
				(ID.GlobalScheduler.ZoneData.GSAConfirmedOrder).
				MsgID(MessageIds.msgGSAConfirmedOrder).
				build();

		NamedZoneData ZoneDataName2 = new NamedZoneData.Builder(
				ID.GlobalScheduler.ZoneData.askBidForJobFromLSA).
				MsgID(MessageIds.msgaskBidForJobFromLSA).
				build();

		NamedZoneData ZoneDataName3 = new NamedZoneData.Builder(
				ID.GlobalScheduler.ZoneData.GetWaitingTime).
				MsgID(MessageIds.msgGetWaitingTime).
				build();

		NamedZoneData ZoneDataName4 = new NamedZoneData.Builder(
				ID.GlobalScheduler.ZoneData.GSAjobsUnderNegaotiation).
				MsgID(MessageIds.msgGSAjobsUnderNegaotiation).
				build();

		NamedZoneData ZoneDataName5 = new NamedZoneData.Builder(
				ID.GlobalScheduler.ZoneData.jobForLSA).
				MsgID(MessageIds.msgjobForLSA).
				build();
		
		NamedZoneData ZoneDataName6 = new NamedZoneData.Builder(
				ID.GlobalScheduler.ZoneData.QueryRequest).
				MsgID(MessageIds.msgGSAQuery).build();
		
		NamedZoneData ZoneDataName7 = new NamedZoneData.Builder(
				ID.GlobalScheduler.ZoneData.CallBackJobs).
				MsgID(MessageIds.msgCallBackReqByGSA).build();
		
		NamedZoneData ZoneDataName8 = new NamedZoneData.Builder(
				ID.GlobalScheduler.ZoneData.completedJobByGSA).
				MsgID(MessageIds.msgJobCompletion).build();

		NamedZoneData[] ZoneDataNames={ZoneDataName1, ZoneDataName2,
				ZoneDataName3,ZoneDataName4,
				ZoneDataName5,ZoneDataName6, ZoneDataName7 };
		try {
			msg2.setContentObject(ZoneDataNames);
		} catch (IOException e1) {
			e1.printStackTrace();
		}



		AgentUtil.makeZoneBB(myAgent,ZoneDataNames);



		SubscriptionForm subform = new SubscriptionForm();
		AID target = new AID(ID.Customer.LocalName, AID.ISLOCALNAME);
		String[] params = {ID.Customer.ZoneData.customerConfirmedJobs,ID.Customer.ZoneData.newWorkOrderFromCustomer,
				ID.Customer.ZoneData.customerJobsUnderNegotiation};
		subform.AddSubscriptionReq(target, params);
		
		AID target_LSA=new AID(ID.LocalScheduler.LocalName+"#1",AID.ISLOCALNAME);
		String[] LSAparams={ID.LocalScheduler.ZoneData.WaitingTime, ID.LocalScheduler.ZoneData.bidForJob,
				ID.LocalScheduler.ZoneData.finishedJob, ID.LocalScheduler.ZoneData.QueryResponse};
		subform.AddSubscriptionReq(target_LSA, LSAparams);
		

		AID target_LSA2=new AID(ID.LocalScheduler.LocalName+"#2",AID.ISLOCALNAME);
		String[] LSAparams2={ID.LocalScheduler.ZoneData.WaitingTime, ID.LocalScheduler.ZoneData.bidForJob,
				ID.LocalScheduler.ZoneData.finishedJob, ID.LocalScheduler.ZoneData.QueryResponse};
		subform.AddSubscriptionReq(target_LSA2, LSAparams2);
		
		

		AgentUtil.subscribeToParam(myAgent, bb_aid, subform);

	}

}
