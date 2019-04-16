package uk.ac.ebi.uniprot.indexer.uniprotkb;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import uk.ac.ebi.uniprot.indexer.common.utils.Constants;

import java.util.concurrent.TimeUnit;

@Slf4j
public class UniProtKBLogJobListener implements JobExecutionListener {
    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("Job {} starting ...", Constants.SUPPORTING_DATA_INDEX_JOB);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        log.info("Job {} completed.", Constants.SUPPORTING_DATA_INDEX_JOB);

        long durationMillis = jobExecution.getEndTime().getTime() - jobExecution.getStartTime().getTime();

        String duration =
                String.format("%d hrs, %d min, %d sec",
                              TimeUnit.MILLISECONDS.toHours(durationMillis),
                              TimeUnit.MILLISECONDS.toMinutes(durationMillis) -
                                      TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(durationMillis)),
                              TimeUnit.MILLISECONDS.toSeconds(durationMillis) -
                                      TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(durationMillis))
                );

        log.info("=====================================================");
        log.info("                Job Statistics                 ");
        log.info("Job name      : {}", jobExecution.getJobInstance().getJobName());
        log.info("Exit status   : {}", jobExecution.getExitStatus().getExitCode());
        log.info("Start time    : {}", jobExecution.getStartTime());
        log.info("End time      : {}", jobExecution.getEndTime());
        log.info("Duration      : {}", duration);
    }
}
