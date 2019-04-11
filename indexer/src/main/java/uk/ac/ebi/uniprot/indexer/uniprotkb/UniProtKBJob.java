package uk.ac.ebi.uniprot.indexer.uniprotkb;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;

import static uk.ac.ebi.uniprot.indexer.common.utils.Constants.UNIPROTKB_INDEX_JOB;

/**
 * Created 10/04/19
 *
 * @author Edd
 */
public class UniProtKBJob {
    private final JobBuilderFactory jobBuilderFactory;

    public UniProtKBJob(JobBuilderFactory jobBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
    }

    public Job uniProtKBIndexingJob(Step uniProtKBIndexingMainFFStep, JobExecutionListener jobListener) {
        return this.jobBuilderFactory.get(UNIPROTKB_INDEX_JOB)
                .start(uniProtKBIndexingMainFFStep)
                .listener(jobListener)
                .build();
    }
}
