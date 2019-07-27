package uk.ac.ebi.uniprot.datastore.app;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Created 28/05/19
 *
 * @author Edd
 */
@ConditionalOnProperty(prefix = "uniprot.job", name = "name", havingValue = "uniprotkb")
@ComponentScan(basePackages = {"uk.ac.ebi.uniprot.datastore.uniprotkb"})
@Configuration
public class UniProtKBStoreJobConfig {
}