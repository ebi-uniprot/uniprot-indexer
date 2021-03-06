package org.uniprot.store.indexer.publication.uniprotkb;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.uniprot.store.indexer.common.config.SolrRepositoryConfig;
import org.uniprot.store.indexer.common.config.UniProtSolrClient;
import org.uniprot.store.indexer.common.utils.Constants;
import org.uniprot.store.indexer.publication.common.LargeScaleSolrFieldQuery;
import org.uniprot.store.indexer.publication.common.PublicationJobExecutionListener;

/**
 * @author sahmad
 * @created 16/12/2020
 */
@Configuration
@Import({SolrRepositoryConfig.class})
public class UniProtKBPublicationJob {
    private final JobBuilderFactory jobBuilderFactory;

    @Autowired
    public UniProtKBPublicationJob(
            JobBuilderFactory jobBuilderFactory, UniProtSolrClient uniProtSolrClient) {
        this.jobBuilderFactory = jobBuilderFactory;
    }

    @Bean("indexUniProtKBPublicationJob")
    public Job indexUniProtKBPublicationData(
            @Qualifier("cacheLargeScaleStep") Step cacheLargeScaleStep,
            @Qualifier("indexUniProtKBPublicationStep") Step indexUniProtKBPublicationStep,
            PublicationJobExecutionListener publicationJobExecutionListener) {
        return this.jobBuilderFactory
                .get(Constants.UNIPROTKB_PUBLICATION_JOB_NAME)
                .start(cacheLargeScaleStep)
                .next(indexUniProtKBPublicationStep)
                .listener(publicationJobExecutionListener)
                .build();
    }

    @Bean("largeScaleSolrFieldName")
    public LargeScaleSolrFieldQuery largeScaleSolrFieldName() {
        return LargeScaleSolrFieldQuery.UNIPROT_KB;
    }
}
