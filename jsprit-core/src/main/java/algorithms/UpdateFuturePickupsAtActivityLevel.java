package algorithms;

import algorithms.StateManager.StateImpl;
import basics.route.PickupActivity;
import basics.route.ServiceActivity;
import basics.route.TourActivity;
import basics.route.VehicleRoute;

class UpdateFuturePickupsAtActivityLevel implements ReverseActivityVisitor, StateUpdater {
	private StateManager stateManager;
	private int futurePicks = 0;
	private VehicleRoute route;
	
	public UpdateFuturePickupsAtActivityLevel(StateManager stateManager) {
		super();
		this.stateManager = stateManager;
	}

	@Override
	public void begin(VehicleRoute route) {
		this.route = route;
	}

	@Override
	public void visit(TourActivity act) {
		stateManager.putActivityState(act, StateIdFactory.FUTURE_PICKS, new StateImpl(futurePicks));
		if(act instanceof PickupActivity || act instanceof ServiceActivity){
			futurePicks += act.getCapacityDemand();
		}
		assert futurePicks <= route.getVehicle().getCapacity() : "sum of pickups must not be > vehicleCap";
		assert futurePicks >= 0 : "sum of pickups must not < 0";
	}

	@Override
	public void finish() {
		futurePicks = 0;
		route = null;
	}
}