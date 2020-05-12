package eu.xenit.alfresco;

import org.alfresco.schedule.AbstractScheduledLockedJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

public class AntiIdleScheduledJob extends AbstractScheduledLockedJob implements StatefulJob {
    @Override
    public void executeJob(JobExecutionContext context) throws JobExecutionException {
        SchedulerService.executeJob(context, "jobExecutor");
    }
}