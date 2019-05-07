package uk.ac.ebi.uniprot.indexer.uniprotkb;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.ac.ebi.uniprot.indexer.common.listener.ListenerConfig;
import uk.ac.ebi.uniprot.indexer.common.utils.Constants;
import uk.ac.ebi.uniprot.indexer.proteome.ProteomeConfig;
import uk.ac.ebi.uniprot.indexer.test.config.FakeIndexerSpringBootApplication;
import uk.ac.ebi.uniprot.indexer.test.config.TestConfig;
import uk.ac.ebi.uniprot.indexer.uniprotkb.proteome.UniProtKBProteomeIndexStep;
import uk.ac.ebi.uniprot.indexer.uniprotkb.step.UniProtKBStep;
import uk.ac.ebi.uniprot.search.SolrCollection;
import uk.ac.ebi.uniprot.search.document.uniprot.UniProtDocument;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static uk.ac.ebi.uniprot.indexer.common.utils.Constants.UNIPROTKB_INDEX_JOB;
import static uk.ac.ebi.uniprot.indexer.common.utils.Constants.UNIPROTKB_INDEX_STEP;

/**
 * Created 11/04/19
 *
 * @author Edd
 */
@ActiveProfiles(profiles = {"job", "offline"})
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {FakeIndexerSpringBootApplication.class, TestConfig.class, UniProtKBJob.class,
                           UniProtKBStep.class, ListenerConfig.class})
class UniProtKBJobIT {
    @Autowired
    private JobLauncherTestUtils jobLauncher;
    @Autowired
    private SolrTemplate template;
    @Autowired
    private SolrClient solrClient;
    

    @Test
    void testUniProtKBIndexingJob() throws Exception {
        JobExecution jobExecution = jobLauncher.launchJob();
        assertThat(jobExecution.getJobInstance().getJobName(), CoreMatchers.is(UNIPROTKB_INDEX_JOB));

        BatchStatus status = jobExecution.getStatus();
        assertThat(status, is(BatchStatus.COMPLETED));

        Page<UniProtDocument> response = template
                .query(SolrCollection.uniprot.name(), new SimpleQuery("*:*"), UniProtDocument.class);
        assertThat(response, is(notNullValue()));
        assertThat(response.getTotalElements(), is(5L));
        List<UniProtDocument>  results= response.getContent();
        results.stream().forEach(val -> System.out.println(val.accession));
        
        SolrQuery solrQuery = new SolrQuery("keyword:complete proteome");
        QueryResponse solrResponse= solrClient.query(SolrCollection.uniprot.name(), solrQuery);
        SolrDocumentList docList =
        		solrResponse.getResults();
        System.out.println("Number of entries=" +docList.size());
        assertThat(docList.size(), is(4));    
        
        response = template
                .query(SolrCollection.uniprot.name(), new SimpleQuery("keyword:complete proteome"), UniProtDocument.class);
         results= response.getContent();
       results.stream().forEach(val -> System.out.println(val.accession));
        assertThat(response, is(notNullValue()));
        assertThat(response.getTotalElements(), is(4L));    
        
        response = template
                .query(SolrCollection.uniprot.name(), new SimpleQuery("name:*"), UniProtDocument.class);
         results= response.getContent();
       results.stream().forEach(val -> System.out.println(val.accession));
        assertThat(response, is(notNullValue()));
        assertThat(response.getTotalElements(), is(5L));    
        
        response = template
                .query(SolrCollection.uniprot.name(), new SimpleQuery("gene:*"), UniProtDocument.class);
         results= response.getContent();
       results.stream().forEach(val -> System.out.println(val.accession));
        assertThat(response, is(notNullValue()));
        assertThat(response.getTotalElements(), is(5L));    
        
        response = template
                .query(SolrCollection.uniprot.name(), new SimpleQuery("content:*"), UniProtDocument.class);
         results= response.getContent();
       results.stream().forEach(val -> System.out.println(val.accession));
        assertThat(response, is(notNullValue()));
        assertThat(response.getTotalElements(), is(5L));    

        response = template
                .query(SolrCollection.uniprot.name(), new SimpleQuery("keyword_id:KW-0181"), UniProtDocument.class);
   results= response.getContent();
   System.out.println("keyword:*");
   results.stream().forEach(val -> System.out.println(val.accession));
    assertThat(response, is(notNullValue()));
    assertThat(response.getTotalElements(), is(4L));    
    
    response = template
            .query(SolrCollection.uniprot.name(), new SimpleQuery("existence:predicted"), UniProtDocument.class);
results= response.getContent();
System.out.println("keyword:*");
results.stream().forEach(val -> System.out.println(val.accession));
assertThat(response, is(notNullValue()));
assertThat(response.getTotalElements(), is(3L));   

//        StepExecution indexingStep = jobExecution.getStepExecutions().stream()
//                .filter(step -> step.getStepName().equals(UNIPROTKB_INDEX_STEP))
//                .collect(Collectors.toList()).get(0);
//
//        assertThat(indexingStep.getReadCount(), is(5));
//        checkWriteCount(jobExecution, Constants.INDEX_FAILED_ENTRIES_COUNT_KEY, 0);
//        checkWriteCount(jobExecution, Constants.INDEX_WRITTEN_ENTRIES_COUNT_KEY, 5);
    }

    private void checkWriteCount(JobExecution jobExecution, String uniprotkbIndexFailedEntriesCountKey, int i) {
        AtomicInteger failedCountAI = (AtomicInteger) jobExecution.getExecutionContext()
                .get(uniprotkbIndexFailedEntriesCountKey);
        assertThat(failedCountAI, CoreMatchers.is(CoreMatchers.notNullValue()));
        assertThat(failedCountAI.get(), CoreMatchers.is(i));
    }
}