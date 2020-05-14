package eu.xenit.alfresco;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;
import org.springframework.scheduling.quartz.CronTriggerBean;
import org.springframework.scheduling.quartz.JobDetailBean;

public class AntiIdleTriggerRegistry extends AbstractLifecycleBean {
    private static final Logger LOG = LoggerFactory.getLogger(AntiIdleTriggerRegistry.class);

    @Autowired
    private Scheduler scheduler;

    @Autowired
    @Qualifier("eu.xenit.alfresco.AntiIdleTrigger")
    private CronTriggerBean trigger;

    @Autowired
    @Qualifier("eu.xenit.alfresco.AntiIdleJobDetail")
    private JobDetailBean jobDetail;

    @Override
    protected void onBootstrap(ApplicationEvent event) {
        try {
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    @Override
    protected void onShutdown(ApplicationEvent event) {}
}
