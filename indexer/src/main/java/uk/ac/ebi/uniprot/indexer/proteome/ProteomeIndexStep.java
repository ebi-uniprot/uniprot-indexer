package uk.ac.ebi.uniprot.indexer.proteome;

import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import org.springframework.context.annotation.Configuration;
import uk.ac.ebi.uniprot.indexer.common.listener.LogRateListener;
import uk.ac.ebi.uniprot.search.document.proteome.ProteomeDocument;
import uk.ac.ebi.uniprot.xml.jaxb.proteome.Proteome;

/**
 *
 * @author jluo
 * @date: 23 Apr 2019
 *
*/
@Configuration
public class ProteomeIndexStep {
	  private final StepBuilderFactory stepBuilderFactory;
	  @Value(("${solr.indexing.chunkSize}"))
	  private int chunkSize=100;

	    @Autowired
	    public ProteomeIndexStep(StepBuilderFactory stepBuilderFactory) {
	        this.stepBuilderFactory = stepBuilderFactory;
	    }

	    @Bean("ProteomeIndexStep")
	    public Step proteomeIndexViaXmlStep(
	    		 StepExecutionListener stepListener,
                 ChunkListener chunkListener,
	    		 @Qualifier("proteomeXmlReader2")  ItemReader<Proteome> itemReader,
	    		 @Qualifier("ProteomeDocumentProcessor")  ItemProcessor<Proteome, ProteomeDocument> itemProcessor,
	    		 @Qualifier("proteomeItemWriter") ItemWriter<ProteomeDocument> itemWriter) {
	        return this.stepBuilderFactory.get("Proteome_Index_Step")

	                .<Proteome, ProteomeDocument>chunk(chunkSize)
	                .reader(itemReader)
	                .processor(itemProcessor)
	                .writer(itemWriter)
	                .listener(stepListener)
	                .listener(chunkListener)
	                .listener(new LogRateListener<ProteomeDocument>())
	                .build();
	    }
}
