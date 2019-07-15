package uk.ac.ebi.uniprot.indexer.uniprotkb.step;

import lombok.extern.slf4j.Slf4j;
import net.jodah.failsafe.RetryPolicy;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import uk.ac.ebi.uniprot.cv.chebi.ChebiRepoFactory;
import uk.ac.ebi.uniprot.cv.ec.ECRepoFactory;
import uk.ac.ebi.uniprot.cv.taxonomy.FileNodeIterable;
import uk.ac.ebi.uniprot.cv.taxonomy.TaxonomyMapRepo;
import uk.ac.ebi.uniprot.cv.taxonomy.TaxonomyRepo;
import uk.ac.ebi.uniprot.indexer.common.config.UniProtSolrOperations;
import uk.ac.ebi.uniprot.indexer.common.listener.LogRateListener;
import uk.ac.ebi.uniprot.indexer.common.listener.WriteRetrierLogStepListener;
import uk.ac.ebi.uniprot.indexer.uniprot.go.GoRelationFileReader;
import uk.ac.ebi.uniprot.indexer.uniprot.go.GoRelationFileRepo;
import uk.ac.ebi.uniprot.indexer.uniprot.go.GoTermFileReader;
import uk.ac.ebi.uniprot.indexer.uniprot.pathway.PathwayFileRepo;
import uk.ac.ebi.uniprot.indexer.uniprot.pathway.PathwayRepo;
import uk.ac.ebi.uniprot.indexer.uniprotkb.config.AsyncConfig;
import uk.ac.ebi.uniprot.indexer.uniprotkb.config.UniProtKBConfig;
import uk.ac.ebi.uniprot.indexer.uniprotkb.config.UniProtKBIndexingProperties;
import uk.ac.ebi.uniprot.indexer.uniprotkb.model.UniProtEntryDocumentPair;
import uk.ac.ebi.uniprot.indexer.uniprotkb.processor.UniProtEntryConverter;
import uk.ac.ebi.uniprot.indexer.uniprotkb.processor.UniProtEntryDocumentPairProcessor;
import uk.ac.ebi.uniprot.indexer.uniprotkb.reader.UniProtEntryItemReader;
import uk.ac.ebi.uniprot.indexer.uniprotkb.writer.UniProtEntryDocumentPairWriter;
import uk.ac.ebi.uniprot.search.SolrCollection;
import uk.ac.ebi.uniprot.search.document.suggest.SuggestDocument;

import java.io.File;
import java.util.Map;
import java.util.concurrent.Future;

import static uk.ac.ebi.uniprot.indexer.common.utils.Constants.UNIPROTKB_INDEX_STEP;

/**
 * The main UniProtKB indexing step.
 * <p>
 * Created 10/04/19
 *
 * @author Edd
 */
@Configuration
@Import({UniProtKBConfig.class, AsyncConfig.class, SuggestionStep.class})
@Slf4j
public class UniProtKBStep {
    private final StepBuilderFactory stepBuilderFactory;
    private final UniProtKBIndexingProperties uniProtKBIndexingProperties;
    private final UniProtSolrOperations solrOperations;

    @Autowired
    public UniProtKBStep(StepBuilderFactory stepBuilderFactory,
                         UniProtSolrOperations solrOperations,
                         UniProtKBIndexingProperties indexingProperties) {
        this.stepBuilderFactory = stepBuilderFactory;
        this.solrOperations = solrOperations;
        this.uniProtKBIndexingProperties = indexingProperties;
    }

    @Bean(name = "uniProtKBIndexingMainStep")
    public Step uniProtKBIndexingMainFFStep(WriteRetrierLogStepListener writeRetrierLogStepListener,
                                            @Qualifier("uniProtKB") LogRateListener<UniProtEntryDocumentPair> uniProtKBLogRateListener,
                                            ItemReader<UniProtEntryDocumentPair> entryItemReader,
                                            @Qualifier("uniprotkbAsyncProcessor") ItemProcessor<UniProtEntryDocumentPair, Future<UniProtEntryDocumentPair>> asyncProcessor,
                                            @Qualifier("uniprotkbAsyncWriter") ItemWriter<Future<UniProtEntryDocumentPair>> asyncWriter,
                                            UniProtEntryDocumentPairProcessor uniProtDocumentItemProcessor,
                                            ItemWriter<UniProtEntryDocumentPair> uniProtDocumentItemWriter,
                                            ExecutionContextPromotionListener promotionListener) throws Exception {

        return this.stepBuilderFactory.get(UNIPROTKB_INDEX_STEP)
                .listener(promotionListener)
                .<UniProtEntryDocumentPair, Future<UniProtEntryDocumentPair>>
                        chunk(uniProtKBIndexingProperties.getChunkSize())
                .reader(entryItemReader)
                .processor(asyncProcessor)
                .writer(asyncWriter)
                .listener(writeRetrierLogStepListener)
                .listener(uniProtKBLogRateListener)
                .listener(uniProtDocumentItemProcessor)
                .listener(unwrapProxy(uniProtDocumentItemWriter))
                .build();
    }

    // ---------------------- Readers ----------------------
    @Bean
    public ItemReader<UniProtEntryDocumentPair> entryItemReader() {
        return new UniProtEntryItemReader(uniProtKBIndexingProperties);
    }

    // ---------------------- Processors ----------------------
    @Bean
    public UniProtEntryDocumentPairProcessor uniProtDocumentItemProcessor(Map<String, SuggestDocument> suggestDocuments, GoRelationFileRepo goRelationFileRepo) {
        return new UniProtEntryDocumentPairProcessor(
                new UniProtEntryConverter(
                        createTaxonomyRepo(),
                        goRelationFileRepo,
                        createPathwayRepo(),
                        ChebiRepoFactory.get(uniProtKBIndexingProperties.getChebiFile()),
                        ECRepoFactory.get(uniProtKBIndexingProperties.getEcDir()),
                        suggestDocuments));
    }

    @Bean("uniprotkbAsyncProcessor")
    public ItemProcessor<UniProtEntryDocumentPair, Future<UniProtEntryDocumentPair>> asyncProcessor(
            UniProtEntryDocumentPairProcessor uniProtDocumentItemProcessor,
            @Qualifier("itemProcessorTaskExecutor") ThreadPoolTaskExecutor itemProcessorTaskExecutor) {
        AsyncItemProcessor<UniProtEntryDocumentPair, UniProtEntryDocumentPair> asyncProcessor = new AsyncItemProcessor<>();
        asyncProcessor.setDelegate(uniProtDocumentItemProcessor);
        asyncProcessor.setTaskExecutor(itemProcessorTaskExecutor);

        return asyncProcessor;
    }

    // ---------------------- Writers ----------------------
    @Bean
    public ItemWriter<UniProtEntryDocumentPair> uniProtDocumentItemWriter(RetryPolicy<Object> writeRetryPolicy) {
        return new UniProtEntryDocumentPairWriter(this.solrOperations, SolrCollection.uniprot, writeRetryPolicy);
    }

    @Bean("uniprotkbAsyncWriter")
    public ItemWriter<Future<UniProtEntryDocumentPair>> asyncWriter(ItemWriter<UniProtEntryDocumentPair> uniProtDocumentItemWriter) {
        AsyncItemWriter<UniProtEntryDocumentPair> asyncItemWriter = new AsyncItemWriter<>();
        asyncItemWriter.setDelegate(uniProtDocumentItemWriter);

        return asyncItemWriter;
    }

    // ---------------------- Listeners ----------------------
    @Bean(name = "uniProtKB")
    public LogRateListener<UniProtEntryDocumentPair> uniProtKBLogRateListener() {
        return new LogRateListener<>(uniProtKBIndexingProperties.getUniProtKBLogRateInterval());
    }

    // ---------------------- Source Data Access beans and helpers ----------------------
    /**
     * Needs to be a bean since it contains a @Cacheable annotation within, and Spring
     * will only scan for these annotations inside beans.
     *
     * @return the GoRelationFileRepo
     */
    @Bean
    public GoRelationFileRepo goRelationFileRepo() {
        return new GoRelationFileRepo(
                new GoRelationFileReader(uniProtKBIndexingProperties.getGoDir()),
                new GoTermFileReader(uniProtKBIndexingProperties.getGoDir()));
    }

    @Bean
    UniProtKBIndexingProperties indexingProperties() {
        return uniProtKBIndexingProperties;
    }

    private PathwayRepo createPathwayRepo() {
        return new PathwayFileRepo(uniProtKBIndexingProperties.getPathwayFile());
    }

    private TaxonomyRepo createTaxonomyRepo() {
        return new TaxonomyMapRepo(new FileNodeIterable(new File(uniProtKBIndexingProperties.getTaxonomyFile())));
    }

    /**
     * Checks if the given object is a proxy, and unwraps it if it is.
     *
     * @param bean The object to check
     * @return The unwrapped object that was proxied, else the object
     * @throws Exception any exception caused during unwrapping
     */
    private Object unwrapProxy(Object bean) throws Exception {
        if (AopUtils.isAopProxy(bean) && bean instanceof Advised) {
            Advised advised = (Advised) bean;
            bean = advised.getTargetSource().getTarget();
        }
        return bean;
    }
}