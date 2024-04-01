package eu.arrowhead.application.skeleton.consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import ai.aitia.arrowhead.application.library.ArrowheadService;
import ai.aitia.arrowhead.application.library.config.ApplicationInitListener;
import eu.arrowhead.common.core.CoreSystem;
import javax.annotation.Resource;
import java.util.Map;

@Component
public class ConsumerApplicationInitListener extends ApplicationInitListener {
	
	@Autowired
	private ArrowheadService arrowheadService;

	@Resource(
			name = "arrowheadContext"
	)
	private Map<String, Object> arrowheadContext;
	
	private final Logger logger = LogManager.getLogger(ConsumerApplicationInitListener.class);

	@Override
	protected void customInit(final ContextRefreshedEvent event) {
		
		//Checking the availability of necessary core systems
		checkCoreSystemReachability(CoreSystem.SERVICEREGISTRY);
		checkCoreSystemReachability(CoreSystem.ORCHESTRATOR);
		
		//Initialize Arrowhead Context
		arrowheadService.updateCoreServiceURIs(CoreSystem.ORCHESTRATOR);

		//TODO: implement here any custom behavior on application start up

		MiddlewareSetup setup = new MiddlewareSetup(arrowheadService);

		setup.run();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public void customDestroy() {
		//TODO: implement here any custom behavior on application shutdown
	}
}
