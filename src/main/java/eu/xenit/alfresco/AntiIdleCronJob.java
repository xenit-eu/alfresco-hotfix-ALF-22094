package eu.xenit.alfresco;

import java.time.LocalDateTime;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@EnableScheduling
public class AntiIdleCronJob {

    protected static final Log log = LogFactory.getLog(AntiIdleCronJob.class);

    public void init() {
        log.error("init got called");
    }

    @Scheduled(fixedRate = 1000)
    protected void doStuff() {
        log.error("I am doing something, time is " + LocalDateTime.now());
    }
}
