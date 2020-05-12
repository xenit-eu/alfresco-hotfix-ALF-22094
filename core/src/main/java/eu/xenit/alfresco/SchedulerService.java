package eu.xenit.alfresco;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

public class SchedulerService {
    private SchedulerService() {}
    public static void executeJob(JobExecutionContext context, String jobExecutorName) {
        JobDataMap jobData = context.getJobDetail().getJobDataMap();

        // Extract the Job executor to use
        Object executorObj = jobData.get(jobExecutorName);
        if (!(executorObj instanceof AntiIdleScheduledJobExecutor)) {
            throw new AlfrescoRuntimeException("ScheduledJob data must contain valid 'Executor' reference");
        }

        final AntiIdleScheduledJobExecutor jobExecutor = (AntiIdleScheduledJobExecutor) executorObj;

        AuthenticationUtil.runAs(() -> {
            jobExecutor.execute();
            return null;
        }, AuthenticationUtil.getSystemUserName());
    }
}
