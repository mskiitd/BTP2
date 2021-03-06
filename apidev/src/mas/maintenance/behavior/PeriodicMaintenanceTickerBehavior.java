package mas.maintenance.behavior;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;

import java.util.ArrayList;

import mas.job.job;
import mas.job.jobOperation;
import mas.machine.SimulatorInternals;
import mas.maintenance.agent.LocalMaintenanceAgent;
import mas.maintenance.plan.RepairKit;
import mas.util.ID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bdi4jade.core.BeliefBase;

public class PeriodicMaintenanceTickerBehavior extends TickerBehaviour{

	private static final long serialVersionUID = 1L;
	private RepairKit solver;
	private SimulatorInternals myMachine;
	private job maintenanceJob;
	private AID bbAgent;
	private BeliefBase bfBase;
	private Logger log;

	public PeriodicMaintenanceTickerBehavior(Agent a, long period) {
		super(a, period);
	}

	public PeriodicMaintenanceTickerBehavior(Agent a, long period,
			BeliefBase bfBase) {
		super(a, period);
		reset(LocalMaintenanceAgent.prevMaintPeriod);
		this.solver = new RepairKit();
		this.bfBase = bfBase;

		this.myMachine = (SimulatorInternals) bfBase.
				getBelief(ID.Maintenance.BeliefBaseConst.machine).
				getValue();

		this.bbAgent = (AID) bfBase.
				getBelief(ID.Maintenance.BeliefBaseConst.blackboardAgentAID).
				getValue();

		log = LogManager.getLogger();

	}

	@Override
	protected void onTick() {

		if(myMachine == null) {
			this.myMachine = (SimulatorInternals) bfBase.
					getBelief(ID.Maintenance.BeliefBaseConst.machine).
					getValue();
		}

		log.info("Periodic maintenance  : " + myMachine);

		if(myMachine != null) {
			log.info("Periodic maintenance check running !!");
			long startTime = (long) (System.currentTimeMillis() / 1000L);

			solver.setMachine(myMachine);
			long processingTime = (long) solver.totalMaintenanceTime(startTime);
			long duedate = (long) solver.maintenanceJobDueDate(startTime);

			this.maintenanceJob = new job.Builder("0").
					jobGenTime(System.currentTimeMillis()).
					jobPenalty(1).
					jobCPN(1).
					jobDueDateTime(duedate).
					build();

			ArrayList<jobOperation> mainOp = new ArrayList<jobOperation>();
			jobOperation op1 = new jobOperation();
			op1.setProcessingTime(processingTime);

			mainOp.add(op1);

			this.maintenanceJob.setOperations(mainOp);

			if(bbAgent == null) {
				this.bbAgent = (AID) bfBase.
						getBelief(ID.Maintenance.BeliefBaseConst.blackboardAgentAID).
						getValue();
			}

			if(bbAgent != null) {
				myAgent.addBehaviour(new SendMaintenanceJobBehavior
						(this.maintenanceJob,this.bbAgent));
			}

			bfBase.updateBelief(ID.Maintenance.BeliefBaseConst.maintenanceJob,
					maintenanceJob);
		}
	}

}
