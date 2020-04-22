package eu.xenit.alfresco;

import org.alfresco.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AntiIdleScheduledJobExecuter {
    private static final Logger LOG = LoggerFactory.getLogger(AntiIdleScheduledJobExecuter.class);

    /**
     * Public API access
     */
    private ServiceRegistry serviceRegistry;

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    /**
     * Executer implementation
     */
    public void execute() {
        LOG.error("Running the scheduled job");
    }
}
