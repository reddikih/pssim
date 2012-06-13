package sim.datalayout.factory;

import sim.datalayout.LayoutManager;
import sim.datalayout.RAPoSDALayoutManager;

public class RAPoSDALayoutManagerFactory extends LayoutManagerFactory {

	@Override
	protected LayoutManager createManager() {
		return new RAPoSDALayoutManager();
	}

}
