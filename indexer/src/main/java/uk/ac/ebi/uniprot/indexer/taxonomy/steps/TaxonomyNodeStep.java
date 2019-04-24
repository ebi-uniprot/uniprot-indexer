package uk.ac.ebi.uniprot.indexer.taxonomy.steps;

import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.solr.core.SolrTemplate;
import uk.ac.ebi.uniprot.indexer.common.utils.Constants;
import uk.ac.ebi.uniprot.indexer.common.writer.SolrDocumentWriter;
import uk.ac.ebi.uniprot.indexer.taxonomy.readers.TaxonomyNodeReader;
import uk.ac.ebi.uniprot.search.SolrCollection;
import uk.ac.ebi.uniprot.search.document.taxonomy.TaxonomyDocument;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 *
 * @author lgonzales
 */
@Configuration
public class TaxonomyNodeStep {

    @Value(("${database.chunk.size}"))
    private Integer chunkSize;

    @Bean(name = "taxonomyNode")
    public Step taxonomyNode(StepBuilderFactory stepBuilders,StepExecutionListener stepListener,
                                       ChunkListener chunkListener,
                                       ItemReader<TaxonomyDocument> itemTaxonomyNodeReader,
                                       ItemWriter<TaxonomyDocument> itemTaxonomyNodeWriter){
        return stepBuilders.get(Constants.TAXONOMY_LOAD_NODE_STEP_NAME)
                .<TaxonomyDocument, TaxonomyDocument>chunk(chunkSize)
                .reader(itemTaxonomyNodeReader)
                .writer(itemTaxonomyNodeWriter)
                .listener(stepListener)
                .listener(chunkListener)
                .build();
    }

    @Bean(name = "itemTaxonomyNodeReader")
    public ItemReader<TaxonomyDocument> itemTaxonomyNodeReader(DataSource readDataSource) throws SQLException {
        JdbcCursorItemReader<TaxonomyDocument> itemReader = new JdbcCursorItemReader<>();
        itemReader.setDataSource(readDataSource);
        itemReader.setSql("select tax_id,parent_id,hidden,internal,rank,gc_id,mgc_id,ncbi_scientific,ncbi_common," +
                "sptr_scientific,sptr_common,sptr_synonym,sptr_code,tax_code,sptr_ff,superregnum" +
                " from taxonomy.v_public_node where tax_id < 11000"); //TODO: REMOVE WHERE < 11000
        itemReader.setRowMapper(new TaxonomyNodeReader());

        return itemReader;
    }

    @Bean(name = "itemTaxonomyNodeWriter")
    public ItemWriter<TaxonomyDocument> itemTaxonomyNodeWriter(SolrTemplate solrTemplate) {
        return new SolrDocumentWriter<>(solrTemplate, SolrCollection.taxonomy);
    }
}
