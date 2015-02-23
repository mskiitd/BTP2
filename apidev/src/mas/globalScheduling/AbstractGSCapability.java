package mas.globalScheduling;

import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.MessageTemplate;

import java.util.HashSet;
import java.util.Set;














import mas.globalScheduling.goal.GetNoOfMachinesGoal;
import mas.globalScheduling.goal.RegisterAgentGoal;
import mas.globalScheduling.goal.RegisterServiceGoal;
import mas.globalScheduling.goal.RegisterWithBBGoal;
import mas.globalScheduling.plan.*;
import mas.util.ID;
import mas.util.MessageIds;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bdi4jade.belief.Belief;
import bdi4jade.belief.BeliefSet;
import bdi4jade.belief.TransientBelief;
import bdi4jade.belief.TransientBeliefSet;
import bdi4jade.core.BeliefBase;
import bdi4jade.core.Capability;
import bdi4jade.core.PlanLibrary;
import bdi4jade.plan.Plan;
import bdi4jade.util.plan.SimplePlan;

public abstract class AbstractGSCapability  extends Capability {


	private static final long serialVersionUID = 1L;
//	public static final String MACHINES = "machines-in-Shop-floor";
	private Logger log;

	public AbstractGSCapability(){
		super(new BeliefBase(getBeliefs()), new PlanLibrary(getPlans()));

}
	
	public static Set<Belief<?>> getBeliefs() {
		Set<Belief<?>> beliefs = new HashSet<Belief<?>>();

		Belief<AID> BB_AID = 
				new TransientBelief<AID>(ID.Blackboard.LocalName);		
		BB_AID.setValue(new AID(ID.Blackboard.LocalName,false));
		
		BeliefSet<Integer> NoOfMachines=new TransientBeliefSet<Integer>(ID.Blackboard.BeliefBaseConst.NoOfMachines);//no of machines==no of LSA		
		beliefs.add(BB_AID);
		beliefs.add(NoOfMachines);
		
		return beliefs;
	}
	
	public static Set<Plan> getPlans() {
		Set<Plan> plans = new HashSet<Plan>();
		plans.add(new SimplePlan(GetNoOfMachinesGoal.class,GetNoOfMachinesPlan.class));
		plans.add(new SimplePlan(RegisterServiceGoal.class, RegisterServicePlan.class));
		plans.add(new SimplePlan(RegisterAgentGoal.class,RegisterAgentToBlackboard.class));
		plans.add(new SimplePlan(MessageTemplate.MatchConversationId(MessageIds.JobFromCustomer),
				TakeOrder.class));
		plans.add(new SimplePlan(MessageTemplate.MatchConversationId(
				MessageIds.Negotiate),Negotiate.class));
		plans.add(new SimplePlan(MessageTemplate.MatchConversationId(
				MessageIds.OrderConfirmation),AskForWaitingTime.class));
		
//		plans.add(new SimplePlan(AskWaitingTimeGoal.class,AskForWaitingTime.class));
		
		
		return plans;
	}	
	
	@Override
	protected void setup() {
		log=LogManager.getLogger();		
		myAgent.addGoal(new RegisterServiceGoal());
		myAgent.addGoal(new RegisterAgentGoal());
		myAgent.addGoal(new GetNoOfMachinesGoal());
//		log.info(myAgent.getAllGoals());
	//Plan to register with bb is to be implemented by user
	}

}
