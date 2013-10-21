package algorithms;

import java.util.ArrayList;
import java.util.List;

import basics.VehicleRoutingProblem;
import basics.VehicleRoutingProblem.Constraint;
import basics.algo.InsertionListener;
import basics.algo.VehicleRoutingAlgorithmListeners.PrioritizedVRAListener;

public class BestInsertionBuilder implements InsertionStrategyBuilder{

	private VehicleRoutingProblem vrp;
	
	private StateManager stateManager;
	
	private boolean local = true;
	
	private ConstraintManager constraintManager;

	private VehicleFleetManager fleetManager;

	private double weightOfFixedCosts;

	private boolean considerFixedCosts = false;

	private ActivityInsertionCostsCalculator actInsertionCostsCalculator = null;
	
	public BestInsertionBuilder(VehicleRoutingProblem vrp, VehicleFleetManager vehicleFleetManager, StateManager stateManager) {
		super();
		this.vrp = vrp;
		this.stateManager = stateManager;
		this.constraintManager = new ConstraintManager();
		this.fleetManager = vehicleFleetManager;
	}
	
	/**
	 * Adds core constraints, i.e.
	 * 
	 * <p>HardPickupAndDeliveryLoadRouteLevelConstraint<br>
	 * HardTimeWindowActivityLevelConstraint<br>
	 * if(Constraint.DELIVERIES_FIRST) HardPickupAndDeliveryBackhaulActivityLevelConstraint<br>
	 * else HardPickupAndDeliveryActivityLevelConstraint
	 * @return
	 */
	public BestInsertionBuilder addCoreConstraints(){
		constraintManager.addConstraint(new HardPickupAndDeliveryLoadRouteLevelConstraint(stateManager));
		constraintManager.addConstraint(new HardTimeWindowActivityLevelConstraint(stateManager, vrp.getTransportCosts()));
		if(vrp.getProblemConstraints().contains(Constraint.DELIVERIES_FIRST)){
			constraintManager.addConstraint(new HardPickupAndDeliveryBackhaulActivityLevelConstraint(stateManager));
		}
		else{
			constraintManager.addConstraint(new HardPickupAndDeliveryActivityLevelConstraint(stateManager));
		}
		StateUtils.addCoreStateUpdaters(vrp, stateManager);
		return this;
	}

	public BestInsertionBuilder addConstraint(HardActivityLevelConstraint hardActvitiyLevelConstraint){
		constraintManager.addConstraint(hardActvitiyLevelConstraint);
		return this;
	};
	
	public BestInsertionBuilder addConstraint(HardRouteLevelConstraint hardRouteLevelConstraint){
		constraintManager.addConstraint(hardRouteLevelConstraint);
		return this;
	};
	
	//public void setRouteLevel(int forwardLooking, int memory){};
	
	public BestInsertionBuilder setLocalLevel(){
		local = true;
		return this;
	};
	
	public BestInsertionBuilder considerFixedCosts(double weightOfFixedCosts){
		this.weightOfFixedCosts = weightOfFixedCosts;
		this.considerFixedCosts  = true;
		return this;
	}
	
	public void setActivityInsertionCostCalculator(ActivityInsertionCostsCalculator activityInsertionCostsCalculator){
		this.actInsertionCostsCalculator = activityInsertionCostsCalculator;
	};
	
	@Override
	public InsertionStrategy build() {
		List<InsertionListener> iListeners = new ArrayList<InsertionListener>();
		List<PrioritizedVRAListener> algorithmListeners = new ArrayList<PrioritizedVRAListener>();
		CalculatorBuilder calcBuilder = new CalculatorBuilder(iListeners, algorithmListeners);
		addCoreUpdater();
		
		if(local){
			calcBuilder.setLocalLevel();
		}
		else {
			//add CostsUpdater
			
		}
//			calcBuilder.setRouteLevel(forwardLooking, memory);
//		}
		calcBuilder.setConstraintManager(constraintManager);
		calcBuilder.setStates(stateManager);
		calcBuilder.setVehicleRoutingProblem(vrp);
		calcBuilder.setVehicleFleetManager(fleetManager);
		calcBuilder.setActivityInsertionCostsCalculator(actInsertionCostsCalculator);
		if(considerFixedCosts) {
			calcBuilder.considerFixedCosts(weightOfFixedCosts);
		}
		JobInsertionCalculator jobInsertions = calcBuilder.build();
		BestInsertion bestInsertion = new BestInsertion(jobInsertions);
		for(InsertionListener l : iListeners) bestInsertion.addListener(l); 
		return bestInsertion;
	}

	private void addCoreUpdater() {
		if(!hasActivityTimeUpdater()){
			stateManager.addActivityVisitor(new UpdateActivityTimes(vrp.getTransportCosts()));
		}
//		if(!hasLoadUpdater()){
//			stateManager.addActivityVisitor(new UpdateLoadAtActivityLevel(stateManager));
//		}
		
	}

	private boolean hasLoadUpdater() {
		for(StateUpdater updater : stateManager.getStateUpdaters()){
			if(updater instanceof UpdateLoadAtActivityLevel) return true;
		}
		return false;
	}

	private boolean hasActivityTimeUpdater() {
		for(StateUpdater updater : stateManager.getStateUpdaters()){
			if(updater instanceof UpdateActivityTimes) return true;
		}
		return false;
	}

}
