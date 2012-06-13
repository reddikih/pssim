package sim.datalayout.factory;

import sim.datalayout.LayoutManager;

public abstract class LayoutManagerFactory {

	public LayoutManager create() {
		LayoutManager manager = createManager();
		manager.init();
		return manager;
	}

	abstract protected LayoutManager createManager();
}
