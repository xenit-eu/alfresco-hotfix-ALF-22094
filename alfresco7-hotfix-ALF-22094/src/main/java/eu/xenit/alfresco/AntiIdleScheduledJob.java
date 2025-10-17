package eu.xenit.alfresco;

import org.alfresco.schedule.AbstractScheduledLockedJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class AntiIdleScheduledJob extends AbstractScheduledLockedJob {
    @Override
    public void executeJob(JobExecutionContext context) throws JobExecutionException {
        SchedulerService.executeJob(context, "jobExecutor");
    }
}