package org.uniprot.store.indexer.unirule;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.uniprot.store.indexer.common.config.DataSourceConfig;
import org.uniprot.store.indexer.common.config.SolrRepositoryConfig;
import org.uniprot.store.indexer.common.config.UniProtSolrClient;
import org.uniprot.store.indexer.common.utils.Constants;
import org.uniprot.store.job.common.listener.WriteRetrierLogJobListener;
import org.uniprot.store.search.SolrCollection;

/**
 * @author sahmad
 * @date: 14 May 2020
 */
@Configuration
@Import({DataSourceConfig.class, SolrRepositoryConfig.class})
public class UniRuleIndexJob {
    private final JobBuilderFactory jobBuilderFactory;
    private final UniProtSolrClient uniProtSolrClient;

    @Autowired
    public UniRuleIndexJob(
            JobBuilderFactory jobBuilderFactory, UniProtSolrClient uniProtSolrClient) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.uniProtSolrClient = uniProtSolrClient;
    }

    @Bean
    public Job indexJob(
            Step uniRuleProteinCountSQLStep,
            Step indexUniRuleStep,
            WriteRetrierLogJobListener writeRetrierLogJobListener) {
        return this.jobBuilderFactory
                .get(Constants.UNIRULE_INDEX_JOB)
                .start(uniRuleProteinCountSQLStep) // load protein count from DB
                .next(indexUniRuleStep) // index uniRule entry
                .listener(writeRetrierLogJobListener)
                .listener(
                        new JobExecutionListener() {
                            @Override
                            public void beforeJob(JobExecution jobExecution) {
                                // no-op
                            }

                            @Override
                            public void afterJob(JobExecution jobExecution) {
                                uniProtSolrClient.commit(SolrCollection.unirule);
                            }
                        })
                .build();
    }
}
