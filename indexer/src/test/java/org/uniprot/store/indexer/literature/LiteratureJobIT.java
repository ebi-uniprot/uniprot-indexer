package org.uniprot.store.indexer.literature;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.uniprot.core.json.parser.literature.LiteratureJsonConfig;
import org.uniprot.core.literature.LiteratureEntry;
import org.uniprot.core.literature.impl.LiteratureEntryImpl;
import org.uniprot.store.indexer.common.utils.Constants;
import org.uniprot.store.indexer.literature.steps.LiteratureLoadStep;
import org.uniprot.store.indexer.literature.steps.LiteratureMappingStep;
import org.uniprot.store.indexer.literature.steps.LiteratureStatisticsStep;
import org.uniprot.store.indexer.test.config.FakeIndexerSpringBootApplication;
import org.uniprot.store.indexer.test.config.FakeReadDatabaseConfig;
import org.uniprot.store.indexer.test.config.SolrTestConfig;
import org.uniprot.store.job.common.listener.ListenerConfig;
import org.uniprot.store.search.SolrCollection;
import org.uniprot.store.search.document.literature.LiteratureDocument;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * @author lgonzales
 */
@ActiveProfiles(profiles = {"job", "offline"})
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {FakeIndexerSpringBootApplication.class, SolrTestConfig.class, FakeReadDatabaseConfig.class,
        ListenerConfig.class, LiteratureJob.class, LiteratureLoadStep.class, LiteratureMappingStep.class,
        LiteratureJobIT.KeywordStatisticsStepFake.class})
class LiteratureJobIT {

    @Autowired
    private JobLauncherTestUtils jobLauncher;

    @Autowired
    private SolrTemplate template;

    @Test
    void testLiteratureIndexingJob() throws Exception {
        JobExecution jobExecution = jobLauncher.launchJob();
        assertThat(jobExecution.getJobInstance().getJobName(), CoreMatchers.is(Constants.LITERATURE_LOAD_JOB_NAME));


        //Validating job and status execution
        BatchStatus status = jobExecution.getStatus();
        assertThat(status, is(BatchStatus.COMPLETED));

        Map<String, StepExecution> stepMap = jobExecution.getStepExecutions().stream()
                .collect(Collectors.toMap(StepExecution::getStepName, stepExecution -> stepExecution));

        assertThat(stepMap, is(notNullValue()));
        assertThat(stepMap.containsKey(Constants.LITERATURE_INDEX_STEP), is(true));
        StepExecution step = stepMap.get(Constants.LITERATURE_INDEX_STEP);
        assertThat(step.getReadCount(), is(18));
        assertThat(step.getWriteCount(), is(18));

        //Validating if solr document was written correctly
        SimpleQuery solrQuery = new SimpleQuery("*:*");
        solrQuery.addSort(new Sort(Sort.Direction.ASC, "id"));
        Page<LiteratureDocument> response = template
                .query(SolrCollection.literature.name(), solrQuery, LiteratureDocument.class);
        assertThat(response, is(notNullValue()));
        assertThat(response.getTotalElements(), is(18L));


        //validating if can search one single entry with mapped and cited items
        solrQuery = new SimpleQuery("id:11203701");
        response = template
                .query(SolrCollection.literature.name(), solrQuery, LiteratureDocument.class);
        assertThat(response, is(notNullValue()));
        assertThat(response.getTotalElements(), is(1L));

        LiteratureDocument literatureDocument = response.getContent().get(0);
        validateLiteratureDocument(literatureDocument);

        ByteBuffer byteBuffer = literatureDocument.getLiteratureObj();
        ObjectMapper jsonMapper = LiteratureJsonConfig.getInstance().getFullObjectMapper();
        LiteratureEntry entry = jsonMapper.readValue(byteBuffer.array(), LiteratureEntryImpl.class);
        validateLiteratureEntry(entry);
    }

    private void validateLiteratureEntry(LiteratureEntry entry) {
        assertThat(entry, is(notNullValue()));
        assertThat(entry.hasPubmedId(), is(true));
        assertThat(entry.getPubmedId(), is(11203701L));

        assertThat(entry.hasDoiId(), is(true));
        assertThat(entry.getDoiId(), is("10.1006/dbio.2000.9955"));

        assertThat(entry.hasTitle(), is(true));
        assertThat(entry.getTitle(), is("TNF signaling via the ligand-receptor pair ectodysplasin " +
                "and edar controls the function of epithelial signaling centers and is regulated by Wnt " +
                "and activin during tooth organogenesis."));

        assertThat(entry.hasAuthoringGroup(), is(false));

        assertThat(entry.hasAuthors(), is(true));
        assertThat(entry.getAuthors().size(), is(10));

        assertThat(entry.isCompleteAuthorList(), is(true));

        assertThat(entry.hasPublicationDate(), is(true));
        assertThat(entry.getPublicationDate().getValue(), is("2001"));

        assertThat(entry.hasJournal(), is(true));
        assertThat(entry.getJournal().getName(), is("Dev. Biol."));

        assertThat(entry.hasFirstPage(), is(true));
        assertThat(entry.getFirstPage(), is("443"));

        assertThat(entry.hasLastPage(), is(true));
        assertThat(entry.getLastPage(), is("455"));

        assertThat(entry.hasVolume(), is(true));
        assertThat(entry.getVolume(), is("229"));

        assertThat(entry.hasLiteratureMappedReferences(), is(true));
        assertThat(entry.getLiteratureMappedReferences().size(), is(19));

        assertThat(entry.hasStatistics(), is(true));
        assertThat(entry.getStatistics().getMappedProteinCount(), is(19L));
        assertThat(entry.getStatistics().getReviewedProteinCount(), is(1L));
        assertThat(entry.getStatistics().getUnreviewedProteinCount(), is(1L));
    }

    private void validateLiteratureDocument(LiteratureDocument literatureDocument) {
        assertThat(literatureDocument, is(notNullValue()));
        assertThat(literatureDocument.getId(), is(notNullValue()));
        assertThat(literatureDocument.getId(), is("11203701"));
        assertThat(literatureDocument.getDoi(), is("10.1006/dbio.2000.9955"));
        assertThat(literatureDocument.isCitedin(), is(true));
        assertThat(literatureDocument.isMappedin(), is(true));
        assertThat(literatureDocument.getLiteratureObj(), is(notNullValue()));
    }

    @Configuration
    static class KeywordStatisticsStepFake extends LiteratureStatisticsStep {

        @Override
        protected String getStatisticsSQL() {
            return LiteratureSQLConstants.LITERATURE_STATISTICS_SQL.replaceAll("FULL JOIN", "INNER JOIN");
        }

    }

}