package org.uniprot.store.indexer.keyword;

import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.uniprot.store.indexer.common.config.UniProtSolrOperations;
import org.uniprot.store.indexer.common.listener.SolrCommitStepListener;
import org.uniprot.store.indexer.common.utils.Constants;
import org.uniprot.store.indexer.common.writer.SolrDocumentWriter;
import org.uniprot.store.search.SolrCollection;
import org.uniprot.store.search.document.keyword.KeywordDocument;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * @author lgonzales
 */
@Configuration
public class KeywordStatisticsStep {

    @Value(("${database.chunk.size}"))
    private Integer chunkSize;

    @Bean(name = "keywordStatistics")
    public Step keywordStatistics(StepBuilderFactory stepBuilders, StepExecutionListener stepListener,
                                  ChunkListener chunkListener,
                                  ItemReader<KeywordStatisticsReader.KeywordCount> itemKeywordStatisticsReader,
                                  ItemProcessor<KeywordStatisticsReader.KeywordCount, KeywordDocument> itemKeywordStatisticsProcessor,
                                  ItemWriter<KeywordDocument> itemKeywordStatisticsWriter,
                                  UniProtSolrOperations solrOperations) {
        return stepBuilders.get(Constants.KEYWORD_LOAD_STATISTICS_STEP_NAME)
                .<KeywordStatisticsReader.KeywordCount, KeywordDocument>chunk(chunkSize)
                .reader(itemKeywordStatisticsReader)
                .processor(itemKeywordStatisticsProcessor)
                .writer(itemKeywordStatisticsWriter)
                .listener(stepListener)
                .listener(chunkListener)
                .listener(new SolrCommitStepListener(solrOperations))
                .build();
    }

    @Bean(name = "itemKeywordStatisticsReader")
    public ItemReader<KeywordStatisticsReader.KeywordCount> itemKeywordStatisticsReader(@Qualifier("readDataSource") DataSource readDataSource) throws SQLException {
        JdbcCursorItemReader<KeywordStatisticsReader.KeywordCount> itemReader = new JdbcCursorItemReader<>();
        itemReader.setDataSource(readDataSource);
        itemReader.setSql(getStatisticsSQL());
        itemReader.setRowMapper(new KeywordStatisticsReader());

        return itemReader;
    }

    @Bean(name = "itemKeywordStatisticsProcessor")
    public ItemProcessor<KeywordStatisticsReader.KeywordCount, KeywordDocument> itemKeywordStatisticsProcessor() {
        return new KeywordStatisticsProcessor();
    }

    @Bean(name = "itemKeywordStatisticsWriter")
    public ItemWriter<KeywordDocument> itemKeywordStatisticsWriter(UniProtSolrOperations solrOperations) {
        return new SolrDocumentWriter<>(solrOperations, SolrCollection.keyword);
    }

    protected String getStatisticsSQL() {
        return KeywordSQLConstants.KEYWORD_STATISTICS_URL;
    }

}
