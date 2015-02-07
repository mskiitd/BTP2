package mas.maintenance.machineDefinition;

import jade.core.Agent;

import java.util.ArrayList;

public abstract class IMachine extends Agent{

	public abstract ArrayList<IComponent> getComponents();
	public abstract long getStartTime();
	public abstract MachineStatus getStatus();
}
