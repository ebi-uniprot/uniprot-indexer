package org.uniprot.store.indexer.app;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Created 28/05/19
 *
 * @author Edd
 */
@ConditionalOnProperty(prefix = "uniprot.job", name = "name", havingValue = "disease")
@ComponentScan(basePackages = {"org.uniprot.store.indexer.disease"})
@Configuration
public class DiseaseIndexingJobConfig {}
