package mas.machine.behaviors;

import jade.core.behaviours.Behaviour;

import java.util.Date;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import mas.job.job;
import mas.machine.MachineStatus;
import mas.machine.Methods;
import mas.machine.Simulator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AddJobBehavior extends Behaviour {

	private static final long serialVersionUID = 1L;
	private job comingJob;
	boolean IsJobComplete;
	private String maintJobID = "0";
	private Logger log;
	private int step = 0;
	private double processingTime;
	private Simulator machineSimulator = null;
	private ScheduledThreadPoolExecutor executor;

	public AddJobBehavior(job comingJob) {
		this.comingJob = comingJob;
		this.IsJobComplete = false;
		log = LogManager.getLogger();
	}

	public void action() {

		switch(step) {
		// in step 0 generate processing times
		case 0:
			if(! comingJob.getJobID().equals(maintJobID)) {

				//				log.info("Job No : '" + comingJob.getJobNo() + "' loading with" +
				//						"processing time : " + comingJob.getProcessingTime());
				if(this.machineSimulator == null) {
					this.machineSimulator = (Simulator) getDataStore().
							get(Simulator.simulatorStoreName);
				}
				double newProcessingTime =
						Methods.normalRandom(comingJob.getProcessingTime(),
								comingJob.getProcessingTime()*machineSimulator.getPercentProcessingTimeVariation())+
								Methods.getLoadingTime(machineSimulator.getMeanLoadingTime(),
										machineSimulator.getSdLoadingTime()) +
										Methods.getunloadingTime(machineSimulator.getMeanUnloadingTime(),
												machineSimulator.getSdUnloadingTime());

				comingJob.setProcessingTime((long)newProcessingTime) ;

				processingTime = comingJob.getProcessingTime();

				log.info("Job No : '" + comingJob.getJobNo() + "' loading with" +
						"processing time : " + comingJob.getProcessingTime());

				//				log.info("Simulator is " + sim);
				machineSimulator.setStatus(MachineStatus.PROCESSING);

				comingJob.setStartTime(System.currentTimeMillis());

				if( processingTime > 0 ) {
					executor = new ScheduledThreadPoolExecutor(1);
					executor.scheduleAtFixedRate(new timeProcessing(), 0,
							Simulator.TIME_STEP, TimeUnit.MILLISECONDS);
					step = 1;
				}
			}
			else if (comingJob.getJobID().equals(maintJobID)) {
				log.info("Maintenance Job loading");
				IsJobComplete = true;
				HandlePreventiveMaintenanceBehavior pm = 
						new HandlePreventiveMaintenanceBehavior(comingJob);
				pm.setDataStore(this.getDataStore());
				myAgent.addBehaviour(pm);
			}
			else if(comingJob.getJobID().equals(maintJobID)) { 
				log.info("Inspection Job loading");
				IsJobComplete = true;
				HandleInspectionJobBehavior inspector = 
						new HandleInspectionJobBehavior(comingJob);
				inspector.setDataStore(this.getDataStore());
				myAgent.addBehaviour(inspector);
			}
			break;

		case 1:
			// block for some time in order to avoid too much CPU usage
			// this won't affect working of the behavior however
			block(10);
			break;

		case 2:
			if( processingTime <= 0) {
				IsJobComplete = true;
				log.info("Job No:" + comingJob.getJobNo() + " completed");
				ProcessJobBehavior process = new ProcessJobBehavior(comingJob);
				process.setDataStore(getDataStore());
				myAgent.addBehaviour(process);
				machineSimulator.setStatus(MachineStatus.IDLE);
			}

			break;
		}
	}

	@Override
	public boolean done() {
		return (IsJobComplete);
	}

	class timeProcessing implements Runnable {

		@Override
		public void run() {

			/**
			 * If machine is failed it won't do anything.
			 * Executor will just keep scheduling this task
			 * 
			 */
			if( processingTime > 0 &&
					machineSimulator.getStatus() != MachineStatus.FAILED ) {

				processingTime = processingTime - Simulator.TIME_STEP; 
				machineSimulator.AgeComponents(Simulator.TIME_STEP);
			} else if( processingTime <= 0 &&
					machineSimulator.getStatus() != MachineStatus.FAILED ) {
				step = 2;
				executor.shutdown();
			} 
		}
	}
}
