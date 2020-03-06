package org.uniprot.store.indexer.subcell;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.stream.Collectors;

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
import org.uniprot.core.cv.subcell.SubcellLocationCategory;
import org.uniprot.core.cv.subcell.SubcellularLocationEntry;
import org.uniprot.core.cv.subcell.builder.SubcellularLocationEntryImpl;
import org.uniprot.core.json.parser.subcell.SubcellularLocationJsonConfig;
import org.uniprot.store.indexer.common.utils.Constants;
import org.uniprot.store.indexer.test.config.FakeIndexerSpringBootApplication;
import org.uniprot.store.indexer.test.config.FakeReadDatabaseConfig;
import org.uniprot.store.indexer.test.config.SolrTestConfig;
import org.uniprot.store.job.common.listener.ListenerConfig;
import org.uniprot.store.search.SolrCollection;
import org.uniprot.store.search.document.subcell.SubcellularLocationDocument;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author lgonzales
 * @since 2019-07-11
 */
@ActiveProfiles(profiles = {"job", "offline"})
@ExtendWith(SpringExtension.class)
@SpringBootTest(
        classes = {
            FakeIndexerSpringBootApplication.class,
            SolrTestConfig.class,
            FakeReadDatabaseConfig.class,
            ListenerConfig.class,
            SubcellularLocationJob.class,
            SubcellularLocationLoadStep.class,
            SubcellularLocationJobIT.SubcellularLocationStatisticsStepFake.class,
            SubcellularLocationStatisticsWriter.class,
            SubcellularLocationLoadProcessor.class
        })
// to inject job execution...
class SubcellularLocationJobIT {

    @Autowired private JobLauncherTestUtils jobLauncher;

    @Autowired private SolrTemplate template;

    @Test
    void testSubcellularLocationIndexingJob() throws Exception {
        JobExecution jobExecution = jobLauncher.launchJob();
        assertThat(
                jobExecution.getJobInstance().getJobName(),
                CoreMatchers.is(Constants.SUBCELLULAR_LOCATION_LOAD_JOB_NAME));

        // Validating job and status execution
        BatchStatus status = jobExecution.getStatus();
        assertThat(status, is(BatchStatus.COMPLETED));

        Map<String, StepExecution> stepMap =
                jobExecution.getStepExecutions().stream()
                        .collect(
                                Collectors.toMap(
                                        StepExecution::getStepName,
                                        stepExecution -> stepExecution));

        assertThat(stepMap, is(notNullValue()));
        assertThat(stepMap.containsKey(Constants.SUBCELLULAR_LOCATION_INDEX_STEP), is(true));
        StepExecution step = stepMap.get(Constants.SUBCELLULAR_LOCATION_INDEX_STEP);
        assertThat(step.getReadCount(), is(520));
        assertThat(step.getWriteCount(), is(520));

        // Validating if solr document was written correctly
        SimpleQuery solrQuery = new SimpleQuery("*:*");
        solrQuery.addSort(new Sort(Sort.Direction.ASC, "id"));
        Page<SubcellularLocationDocument> response =
                template.query(
                        SolrCollection.subcellularlocation.name(),
                        solrQuery,
                        SubcellularLocationDocument.class);
        assertThat(response, is(notNullValue()));
        assertThat(response.getTotalElements(), is(520L));

        // validating if can search one single entry with mapped and cited items
        solrQuery = new SimpleQuery("id:SL-0188");
        response =
                template.query(
                        SolrCollection.subcellularlocation.name(),
                        solrQuery,
                        SubcellularLocationDocument.class);
        assertThat(response, is(notNullValue()));
        assertThat(response.getTotalElements(), is(1L));

        SubcellularLocationDocument subcellularLocationDocument = response.getContent().get(0);
        validateSubcellularLocationDocument(subcellularLocationDocument);

        ByteBuffer byteBuffer = subcellularLocationDocument.getSubcellularlocationObj();
        ObjectMapper jsonMapper = SubcellularLocationJsonConfig.getInstance().getFullObjectMapper();
        SubcellularLocationEntry entry =
                jsonMapper.readValue(byteBuffer.array(), SubcellularLocationEntryImpl.class);
        validateSubcellularLocationEntry(entry);
    }

    private void validateSubcellularLocationEntry(SubcellularLocationEntry entry) {
        assertThat(entry, is(notNullValue()));
        assertThat(entry.getId(), is("Nucleolus"));
        assertThat(entry.getCategory(), is(SubcellLocationCategory.LOCATION));
        assertThat(entry.getAccession(), is("SL-0188"));
        assertThat(entry.getContent(), is("Nucleus, nucleolus"));
        assertThat(
                entry.getDefinition(), startsWith("The nucleolus is a non-membrane bound nuclear"));

        assertThat(entry.getGeneOntologies(), is(notNullValue()));
        assertThat(entry.getGeneOntologies().size(), is(1));

        assertThat(entry.getSynonyms(), is(notNullValue()));
        assertThat(entry.getSynonyms().size(), is(1));

        assertThat(entry.getStatistics(), is(notNullValue()));
        assertThat(entry.getStatistics().getReviewedProteinCount(), is(5L));
        assertThat(entry.getStatistics().getUnreviewedProteinCount(), is(6L));
    }

    private void validateSubcellularLocationDocument(
            SubcellularLocationDocument subcellularLocationDocument) {
        assertThat(subcellularLocationDocument, is(notNullValue()));
        assertThat(subcellularLocationDocument.getId(), is(notNullValue()));
        assertThat(subcellularLocationDocument.getId(), is("SL-0188"));
        assertThat(subcellularLocationDocument.getSubcellularlocationObj(), is(notNullValue()));
    }

    @Configuration
    static class SubcellularLocationStatisticsStepFake extends SubcellularLocationStatisticsStep {

        @Override
        protected String getStatisticsSQL() {
            return "SELECT 'Acidocalcisome lumen' as identifier, 10 as reviewedProteinCount, 20  as unreviewedProteinCount from SPTR.DBENTRY where DBENTRY_ID=221555878"
                    + "UNION ALL SELECT 'Nucleolus' as identifier, 5 as reviewedProteinCount, 6  as unreviewedProteinCount  from SPTR.DBENTRY where DBENTRY_ID=221555878 "
                    + "UNION ALL SELECT 'Nucleus lamina' as identifier, 6 as reviewedProteinCount, null  as unreviewedProteinCount  from SPTR.DBENTRY where DBENTRY_ID=221555878 "
                    + "UNION ALL SELECT 'Nucleus matrix' as identifier, 7 as reviewedProteinCount, 8  as unreviewedProteinCount  from SPTR.DBENTRY where DBENTRY_ID=221555878 "
                    + "UNION ALL SELECT 'Perinuclear region' as identifier, 8 as reviewedProteinCount, 9  as unreviewedProteinCount  from SPTR.DBENTRY where DBENTRY_ID=221555878 "
                    + "UNION ALL SELECT 'Nucleoplasm' as identifier, 9 as reviewedProteinCount, 10  as unreviewedProteinCount  from SPTR.DBENTRY where DBENTRY_ID=221555878 ";
        }
    }
}
