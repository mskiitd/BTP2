package mas.machine.behaviors;

import jade.core.behaviours.OneShotBehaviour;

import java.util.ArrayList;
import java.util.Date;

import mas.job.job;
import mas.job.jobAttribute;
import mas.job.jobDimension;
import mas.job.jobOperation;
import mas.machine.Methods;
import mas.machine.Simulator;

import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ProcessJobBehavior extends OneShotBehaviour{

	private static final long serialVersionUID = 1L;
	private job comingJob;
	private Logger log;
	private Simulator machineSimulator;

	public ProcessJobBehavior(job processJob) {
		this.comingJob = processJob;
		machineSimulator = null;
	}

	@Override
	public void action() {

		log = LogManager.getLogger();

		if(machineSimulator == null) {
			machineSimulator = (Simulator) getDataStore().
					get(Simulator.simulatorStoreName);
		}

		jobOperation ops = comingJob.getCurrentOperation();
		// Assign dimensions to the job
		ArrayList<jobDimension> jDimensions = ops.getjDims();
		int numDims = jDimensions.size();
		int dIndex;

		for(dIndex = 0; dIndex < numDims; dIndex++) {

			jDimensions.get(dIndex).setTargetDimension(
					jDimensions.get(dIndex).getTargetDimension() +
					Methods.normalRandom(machineSimulator.getMean_shift(),
							machineSimulator.getSd_shift()));

			//			jDimensions.get(dIndex).add(jDimensions.get(dIndex) + 3*Simulator.sd_shift);
			//			jDimensions.get(dIndex).add(jDimensions.get(dIndex) - 3*Simulator.sd_shift);
		}
		comingJob.setCurrentOperationDimension(jDimensions);

		// Assign attributes to the job
		ArrayList<jobAttribute> jAttributes = comingJob.getCurrentOperationAttributes();
		int numAttributes = jAttributes.size();
		int AttIndex;

		BinomialDistribution bernoulli =
				new BinomialDistribution(1, machineSimulator.getFractionDefective());

		boolean conforming;

		for(AttIndex = 0; AttIndex < numAttributes; AttIndex++) {

			conforming = (bernoulli.sample()==1)? Boolean.TRUE :Boolean.FALSE;
			jAttributes.get(AttIndex).setConforming(conforming);
		}
		comingJob.setCurrentOperationAttributes(jAttributes);
		//		log.info("Dimensions and attributes assigned");
		comingJob.setCompletionTime(System.currentTimeMillis());
		log.info("start time was "+new Date(comingJob.getStartTimeByCust().getTime()));
		log.info("processed for "+ (comingJob.getCompletionTime().getTime()-comingJob.getStartTimeByCust().getTime()));

		// send completed job to blackboard in handleCompletedJobBehavior
		myAgent.addBehaviour(new HandleCompletedJobBehavior(comingJob));
	}
}
