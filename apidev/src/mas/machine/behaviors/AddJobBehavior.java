package mas.machine.behaviors;

import jade.core.behaviours.Behaviour;
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
	private long processingTime;
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

				if(this.machineSimulator == null) {
					this.machineSimulator = (Simulator) getDataStore().
							get(Simulator.simulatorStoreName);
				}
				double ProcessingTimeInSeconds = comingJob.getCurrentOperationProcessTime()/1000.0;
						
			/*	log.info("Job No : '" + comingJob.getJobNo() + "' loading with" +
						"processing time before loading/unloading: " + comingJob.getCurrentOperationProcessTime());*/

				
				double newProcessingTime =
						Methods.normalRandom(ProcessingTimeInSeconds,/*
						getCurrentOperationProcessTime gives time in milliseconds*/
								ProcessingTimeInSeconds*machineSimulator.getPercentProcessingTimeVariation())+
								Methods.getLoadingTime(machineSimulator.getMeanLoadingTime(),
										machineSimulator.getSdLoadingTime()) +
										Methods.getunloadingTime(machineSimulator.getMeanUnloadingTime(),
												machineSimulator.getSdUnloadingTime());

				comingJob.setCurrentOperationProcessingTime((long)(1000*newProcessingTime)) ; //this will introduce error
				//ex. 0.1 with be converted into 0

				processingTime = comingJob.getCurrentOperationProcessTime();
				
				if(processingTime<=0){
					processingTime=1;// 1 milliseconds
					log.info("ATTENTION : -ve processing time");
				} //if processingTime=0, this behviour goes into infinite loop
				

				log.info("Job No : '" + comingJob.getJobNo() + "' loading with" +
						"processing time : " + comingJob.getCurrentOperationProcessTime());

				machineSimulator.setStatus(MachineStatus.PROCESSING);

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
			block(15);
			break;

		case 2:
			if( processingTime <= 0) {
				IsJobComplete = true;
				log.info("Job No:" + comingJob.getJobNo() +" operation "+(comingJob.getCurrentOperationNumber()+1)+"/"+comingJob.getOperations().size()+" completed");
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
//			log.info("remProcessingTime="+processingTime);
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
