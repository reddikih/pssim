package sim.datalayout.factory;

import sim.datalayout.LayoutManager;
import sim.datalayout.MAIDLayoutManager;

public class MAIDLayoutManagerFactory extends LayoutManagerFactory {

	@Override
	protected LayoutManager createManager() {
		return new MAIDLayoutManager();
	}

}
