package org.uniprot.store.indexer.search.uniprot;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.Test;
import org.uniprot.core.flatfile.writer.LineType;
import org.uniprot.core.uniprot.feature.FeatureType;
import org.uniprot.store.search.field.QueryBuilder;
import org.uniprot.store.search.field.UniProtField;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.uniprot.store.indexer.search.uniprot.IdentifierSearchIT.ACC_LINE;
import static org.uniprot.store.indexer.search.uniprot.TestUtils.*;

/**
 * Tests showing the behaviour of searching FT fields
 */
public class FTSearchIT {
    public static final String Q6GZX4 = "Q6GZX4";
    public static final String Q197B1 = "Q197B1";
    private static final String UNIPROT_FLAT_FILE_ENTRY_PATH = "/it/uniprot/P0A377.43.dat";
    @RegisterExtension
    public static UniProtSearchEngine searchEngine = new UniProtSearchEngine();

    @BeforeAll
    public static void populateIndexWithTestData() throws IOException {
        // a test entry object that can be modified and added to index
        InputStream resourceAsStream = TestUtils.getResourceAsStream(UNIPROT_FLAT_FILE_ENTRY_PATH);
        UniProtEntryObjectProxy entryProxy = UniProtEntryObjectProxy.createEntryFromInputStream(resourceAsStream);

        // --------------
        entryProxy.updateEntryObject(LineType.AC, String.format(ACC_LINE, Q6GZX4));
        entryProxy.updateEntryObject(LineType.FT, "FT   CHAIN         1    256       Putative transcription factor 001R.\n" +
                "FT                                /FTId=PRO_0000410512.\n" +
                "FT   COMPBIAS     14     17       Poly-Arg.");
        searchEngine.indexEntry(convertToUniProtEntry(entryProxy));

        // --------------
        entryProxy.updateEntryObject(LineType.AC, String.format(ACC_LINE, Q197B1));
        entryProxy.updateEntryObject(LineType.FT, "FT   COILED       62    124       {ECO:0000255}.");
        searchEngine.indexEntry(convertToUniProtEntry(entryProxy));

        searchEngine.printIndexContents();
        

    }

    @Test
    public void coiledFindEntrysWithEcoExactFF() {
        String query = featureEvidence(FeatureType.COILED, "ECO_0000255");

        QueryResponse response = searchEngine.getQueryResponse(query);

        List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
        assertThat(retrievedAccessions, contains(Q197B1));
    }

    @Test
    public void coiledFindEntrysWithEcoInExact() {
        String query = featureEvidence(FeatureType.COILED, "ECO_0000255");

        QueryResponse response = searchEngine.getQueryResponse(query);

        List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
        assertThat(retrievedAccessions, contains(Q197B1));
    }

    @Test
    public void chainFindEntrysWithChain() {
    	String query = query(UniProtField.Search.accession, Q6GZX4);
         query = QueryBuilder.and(query, features(FeatureType.CHAIN, "*"));

        QueryResponse response = searchEngine.getQueryResponse(query);

        List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
        assertThat(retrievedAccessions, contains(Q6GZX4));
    }

    @Test
    public void chainFindEntryWithChain() {
        String query = features(FeatureType.CHAIN, "*");

        QueryResponse response = searchEngine.getQueryResponse(query);

        List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
        assertTrue(retrievedAccessions.contains(Q6GZX4));
    }

    @Test
    public void chainFindEntryContainingPutativeTranscription() {
        String query = features(FeatureType.CHAIN, "Putative transcription");

        QueryResponse response = searchEngine.getQueryResponse(query);

        List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
        assertThat(retrievedAccessions, contains(Q6GZX4));
    }

    @Test
    public void chainFindEntryContainingFtId() {
        String query = features(FeatureType.CHAIN, "/FTId=PRO_0000410512");

        QueryResponse response = searchEngine.getQueryResponse(query);

        List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
        assertThat(retrievedAccessions, contains(Q6GZX4));
    }

    @Test
    public void chainFindNoEntryContainingFtId() {
        String query = features(FeatureType.CHAIN, "/FTId=PRO_000041051");

        QueryResponse response = searchEngine.getQueryResponse(query);

        List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
        assertThat(retrievedAccessions, is(empty()));
    }

    @Test
    public void chainFindEntryContainingFtIdOnly() {
        String query = features(FeatureType.CHAIN, "PRO_0000410512");

        QueryResponse response = searchEngine.getQueryResponse(query);

        List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
        assertThat(retrievedAccessions, contains(Q6GZX4));
    }

    @Test
    public void chainFindEntryCopyPaste1FtLine() {
        String query = features(FeatureType.CHAIN, "Putative transcription factor 001R.");

        QueryResponse response = searchEngine.getQueryResponse(query);

        List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
        assertThat(retrievedAccessions, contains(Q6GZX4));
    }

}