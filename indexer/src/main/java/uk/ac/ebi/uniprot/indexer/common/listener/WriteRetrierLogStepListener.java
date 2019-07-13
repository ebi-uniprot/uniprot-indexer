/*
 * Created by sahmad on 29/01/19 19:28
 * UniProt Consortium.
 * Copyright (c) 2002-2019.
 *
 */

package uk.ac.ebi.uniprot.indexer.common.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ExecutionContext;
import uk.ac.ebi.uniprot.indexer.common.utils.Constants;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class WriteRetrierLogStepListener implements StepExecutionListener {
    private ExecutorService executorService = null;

    public WriteRetrierLogStepListener() {
    }

    public WriteRetrierLogStepListener(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info("Step '{}' starting.", stepExecution.getStepName());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        ExecutionContext executionContext = stepExecution.getJobExecution().getExecutionContext();
        // TODO: 13/07/19 get to write counter and wait if not zero
        if (executionContext.containsKey(Constants.ENTRIES_TO_WRITE_COUNTER)) {
            AtomicInteger entriesToWriteCounter = (AtomicInteger) executionContext
                    .get(Constants.ENTRIES_TO_WRITE_COUNTER);
            System.out.println("**** " + entriesToWriteCounter.get());

            int timeoutCounter = 0;
            int timeoutMax = 2;
            while (entriesToWriteCounter.get() != 0 && timeoutCounter++ != timeoutMax) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    // do nothing
                }
            }
        }

//        try {
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }


        AtomicInteger failedCountAtomicInteger = (AtomicInteger) executionContext
                .get(Constants.INDEX_FAILED_ENTRIES_COUNT_KEY);
        int failedCount = -1;
        if (failedCountAtomicInteger != null) {
            failedCount = failedCountAtomicInteger.get();
            if (failedCount > 0) {
                stepExecution.setExitStatus(ExitStatus.FAILED);
                stepExecution.setStatus(BatchStatus.FAILED);
                stepExecution.getJobExecution().setStatus(BatchStatus.FAILED);
                stepExecution.getJobExecution().setExitStatus(ExitStatus.FAILED);
            }
        }

        AtomicInteger writtenCountAtomicInteger = (AtomicInteger) executionContext
                .get(Constants.INDEX_WRITTEN_ENTRIES_COUNT_KEY);
        int writtenCount = -1;
        if (writtenCountAtomicInteger != null) {
            writtenCount = writtenCountAtomicInteger.get();
        }

        log.info("=====================================================");
        log.info("                   Step Statistics                   ");
        log.info("Step name      : {}", stepExecution.getStepName());
        log.info("Exit status    : {}", stepExecution.getExitStatus().getExitCode());
        log.info("Read count     : {}", stepExecution.getReadCount());
        log.info("Write count    : {}", writtenCount);
        log.info("Failed count   : {}", failedCount);
        log.info("=====================================================");
        return stepExecution.getExitStatus();
    }
}
