package uk.ac.ebi.uniprot.indexer.literature.steps;


import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.data.solr.core.SolrTemplate;
import uk.ac.ebi.uniprot.domain.literature.LiteratureEntry;
import uk.ac.ebi.uniprot.indexer.common.config.UniProtSolrOperations;
import uk.ac.ebi.uniprot.indexer.common.utils.Constants;
import uk.ac.ebi.uniprot.indexer.common.writer.SolrDocumentWriter;
import uk.ac.ebi.uniprot.indexer.literature.processor.LiteratureLoadProcessor;
import uk.ac.ebi.uniprot.indexer.literature.reader.LiteratureLineMapper;
import uk.ac.ebi.uniprot.indexer.literature.reader.LiteratureRecordSeparatorPolicy;
import uk.ac.ebi.uniprot.search.SolrCollection;
import uk.ac.ebi.uniprot.search.document.literature.LiteratureDocument;

import java.io.IOException;
import java.sql.SQLException;

/**
 * @author lgonzales
 */
@Configuration
public class LiteratureLoadStep {

    @Autowired
    private StepBuilderFactory steps;

    @Autowired
    private UniProtSolrOperations solrOperations;

    @Value(("${ds.import.chunk.size}"))
    private Integer chunkSize;

    @Value(("${indexer.literature.file.path}"))
    private Resource literatureFile;

    @Bean(name = "IndexLiteratureStep")
    public Step indexLiterature(StepExecutionListener stepListener, ChunkListener chunkListener,
                                @Qualifier("LiteratureReader") ItemReader<LiteratureEntry> literatureReader,
                                @Qualifier("LiteratureProcessor") ItemProcessor<LiteratureEntry, LiteratureDocument> literatureProcessor,
                                @Qualifier("LiteratureWriter") ItemWriter<LiteratureDocument> literatureWriter) {
        return this.steps.get(Constants.LITERATURE_INDEX_STEP)
                .<LiteratureEntry, LiteratureDocument>chunk(this.chunkSize)
                .reader(literatureReader)
                .processor(literatureProcessor)
                .writer(literatureWriter)
                .listener(stepListener)
                .listener(chunkListener)
                .build();
    }

    @Bean(name = "LiteratureReader")
    public FlatFileItemReader<LiteratureEntry> literatureReader() throws IOException {
        FlatFileItemReader<LiteratureEntry> reader = new FlatFileItemReader<>();
        reader.setResource(literatureFile);
        reader.setLinesToSkip(1);
        reader.setLineMapper(getLiteratureLineMapper());
        reader.setRecordSeparatorPolicy(getLiteratureRecordSeparatorPolice());

        return reader;
    }

    @Bean(name = "LiteratureWriter")
    public ItemWriter<LiteratureDocument> literatureWriter() {
        return new SolrDocumentWriter<>(this.solrOperations, SolrCollection.literature);
    }

    @Bean(name = "LiteratureProcessor")
    public ItemProcessor<LiteratureEntry, LiteratureDocument> literatureProcessor() throws SQLException {
        return new LiteratureLoadProcessor(solrOperations);
    }

    private LiteratureRecordSeparatorPolicy getLiteratureRecordSeparatorPolice() {
        LiteratureRecordSeparatorPolicy policy = new LiteratureRecordSeparatorPolicy();
        policy.setSuffix("\n//");
        policy.setIgnoreWhitespace(true);
        return policy;
    }

    private LiteratureLineMapper getLiteratureLineMapper() {
        return new LiteratureLineMapper();
    }

}