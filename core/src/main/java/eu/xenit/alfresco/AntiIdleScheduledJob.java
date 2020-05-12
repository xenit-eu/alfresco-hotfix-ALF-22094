package eu.xenit.alfresco;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.schedule.AbstractScheduledLockedJob;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

public class AntiIdleScheduledJob extends AbstractScheduledLockedJob implements StatefulJob {
    @Override
    public void executeJob(JobExecutionContext context) throws JobExecutionException {
        JobDataMap jobData = context.getJobDetail().getJobDataMap();

        // Extract the Job executor to use
        Object executorObj = jobData.get("jobExecutor");
        if (!(executorObj instanceof AntiIdleScheduledJobExecutor)) {
            throw new AlfrescoRuntimeException(
                    "ScheduledJob data must contain valid 'Executor' reference");
        }

        final AntiIdleScheduledJobExecutor jobExecutor = (AntiIdleScheduledJobExecutor) executorObj;

        AuthenticationUtil.runAs(() -> {
            jobExecutor.execute();
            return null;
        }, AuthenticationUtil.getSystemUserName());
    }
}