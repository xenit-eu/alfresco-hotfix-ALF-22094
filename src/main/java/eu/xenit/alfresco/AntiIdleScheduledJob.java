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

        // Extract the Job executer to use
        Object executerObj = jobData.get("jobExecuter");
        if (executerObj == null || !(executerObj instanceof AntiIdleScheduledJobExecuter)) {
            throw new AlfrescoRuntimeException(
                    "ScheduledJob data must contain valid 'Executer' reference");
        }

        final AntiIdleScheduledJobExecuter jobExecuter = (AntiIdleScheduledJobExecuter) executerObj;

        AuthenticationUtil.runAs(() -> {
            jobExecuter.execute();
            return null;
        }, AuthenticationUtil.getSystemUserName());
    }
}