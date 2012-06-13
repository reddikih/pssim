package sim.datalayout.factory;

import sim.datalayout.DefaultLayoutManager;
import sim.datalayout.LayoutManager;

public class DefaultLayoutManagerFactory extends LayoutManagerFactory {

	@Override
	protected LayoutManager createManager() {
		return new DefaultLayoutManager();
	}

}
