package mas.maintenance.plan;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import mas.util.ID;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import bdi4jade.plan.PlanBody;
import bdi4jade.plan.PlanInstance;
import bdi4jade.plan.PlanInstance.EndState;

public class RegisterMaintenanceAgentServicePlan extends OneShotBehaviour implements PlanBody{
	private static final long serialVersionUID = 1L;
	private Logger log;

	@Override
	public EndState getEndState() {
		return EndState.SUCCESSFUL;
	}

	@Override
	public void init(PlanInstance pInstance) {
		log = LogManager.getLogger();
	}

	@Override
	public void action() {
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(myAgent.getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType(ID.Maintenance.Service);
		sd.setName(myAgent.getLocalName());
		dfd.addServices(sd);
		try {
			DFService.register(myAgent, dfd);
			log.info(myAgent.getLocalName() + " registered with DF");
			
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
	}
}
