package mas.machine.behaviors;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import mas.machine.MachineStatus;
import mas.machine.Simulator;

public class SimulatorStatusListener 
implements PropertyChangeListener{

	private transient Logger log;
	private Simulator sim;

	public SimulatorStatusListener(Simulator sim) {
		this.sim = sim;
		log = LogManager.getLogger();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {

		if(evt.getPropertyName().equals("Machine status")) {
			if(evt.getNewValue().equals(MachineStatus.FAILED)) {
				log.info("Simulator is in failed state :" );
				
				sim.addBehaviour(new HandleSimulatorFailedBehavior());
			}
		}
	}
}