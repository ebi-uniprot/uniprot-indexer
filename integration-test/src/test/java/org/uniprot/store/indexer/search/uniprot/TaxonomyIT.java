package org.uniprot.store.indexer.search.uniprot;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.Is.is;
import static org.uniprot.store.indexer.search.uniprot.IdentifierSearchIT.ACC_LINE;
import static org.uniprot.store.indexer.search.uniprot.OrganismIT.OX_LINE;
import static org.uniprot.store.indexer.search.uniprot.TestUtils.convertToUniProtEntry;
import static org.uniprot.store.indexer.search.uniprot.TestUtils.query;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.uniprot.core.flatfile.writer.LineType;
import org.uniprot.store.search.field.QueryBuilder;

/**
 * Tests whether the taxonomy lineages have been indexed correctly taxonomy lineages Index is based
 * on taxonomy.dat file. See how we load file content at: FileNodeIterable.createNode and how we
 * index at UniprotEntryConverter.setLineageTaxons
 */
class TaxonomyIT {
    private static final String UNIPROT_FLAT_FILE_ENTRY_PATH = "/it/uniprot/P0A377.43.dat";
    // Entry 1
    private static final String ACCESSION1 = "Q197F4";
    private static final int ORGANISM_TAX_ID1 =
            7787; // Lineage: 7787 --> 7711 --> 33208 --> 2759 --> 1

    // Entry 2
    private static final String ACCESSION2 = "Q197F5";
    private static final int ORGANISM_TAX_ID2 =
            100673; // Lineage: 100673 --> 11552 --> 197913 --> 11308 --> 1

    // Entry 3
    private static final String ACCESSION3 = "Q197F6";
    private static final int ORGANISM_TAX_ID3 =
            93838; // Lineage: 93838 --> 11320 -->  197911 --> 11308 --> 1

    @RegisterExtension static UniProtSearchEngine searchEngine = new UniProtSearchEngine();

    @BeforeAll
    static void populateIndexWithTestData() throws IOException {
        // a test entry object that can be modified and added to index
        InputStream resourceAsStream = TestUtils.getResourceAsStream(UNIPROT_FLAT_FILE_ENTRY_PATH);
        UniProtEntryObjectProxy entryProxy =
                UniProtEntryObjectProxy.createEntryFromInputStream(resourceAsStream);

        // Entry 1
        entryProxy.updateEntryObject(LineType.AC, String.format(ACC_LINE, ACCESSION1));
        entryProxy.updateEntryObject(LineType.OX, String.format(OX_LINE, ORGANISM_TAX_ID1));
        searchEngine.indexEntry(convertToUniProtEntry(entryProxy));

        // Entry 2
        entryProxy.updateEntryObject(LineType.AC, String.format(ACC_LINE, ACCESSION2));
        entryProxy.updateEntryObject(LineType.OX, String.format(OX_LINE, ORGANISM_TAX_ID2));
        searchEngine.indexEntry(convertToUniProtEntry(entryProxy));

        // Entry 3
        entryProxy.updateEntryObject(LineType.AC, String.format(ACC_LINE, ACCESSION3));
        entryProxy.updateEntryObject(LineType.OX, String.format(OX_LINE, ORGANISM_TAX_ID3));
        searchEngine.indexEntry(convertToUniProtEntry(entryProxy));

        searchEngine.printIndexContents();
    }

    @Test
    void noMatchesForNonExistentName() {
        String query = taxonName("Unknown");

        QueryResponse response = searchEngine.getQueryResponse(query);

        List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
        assertThat(retrievedAccessions, is(empty()));
    }

    @Test
    void lineageNameMatchesEntry1() {
        String query = taxonName("Tetronarce californica");
        query = QueryBuilder.and(query, taxonName("Pacific electric ray"));
        query = QueryBuilder.and(query, taxonName("Torpedo californica")); // 7787
        query = QueryBuilder.and(query, taxonName("Chordata")); // 7711
        query = QueryBuilder.and(query, taxonName("Metazoa")); // 33208
        query = QueryBuilder.and(query, taxonName("Eukaryota")); // 2759

        QueryResponse response = searchEngine.getQueryResponse(query);

        List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
        assertThat(retrievedAccessions, containsInAnyOrder(ACCESSION1));
    }

    @Test
    void lineageNameMatchesEntry2() {
        String query = taxonName("Influenza C virus (strain C/Johannesburg/1/1966)\n"); // 100673
        query = QueryBuilder.and(query, taxonName("Influenza C virus")); // 11552
        query = QueryBuilder.and(query, taxonName("Gammainfluenzavirus")); // 197913
        query = QueryBuilder.and(query, taxonName("Orthomyxoviridae")); // 11308

        QueryResponse response = searchEngine.getQueryResponse(query);

        List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
        assertThat(retrievedAccessions, containsInAnyOrder(ACCESSION2));
    }

    @Test
    void lineageNameMatchesEntry3() {
        String query =
                taxonName(
                        "Influenza A virus (strain A/Goose/Guangdong/1/1996 H5N1 genotype Gs/Gd)");
        query = QueryBuilder.and(query, taxonName("Influenza A virus"));
        query = QueryBuilder.and(query, taxonName("Alphainfluenzavirus"));
        query = QueryBuilder.and(query, taxonName("Orthomyxoviridae"));

        QueryResponse response = searchEngine.getQueryResponse(query);

        List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
        assertThat(retrievedAccessions, containsInAnyOrder(ACCESSION3));
    }

    @Test
    void commonLineageNameMatchesEntry2And3() {
        String query = taxonName("Orthomyxoviridae");
        //        query = QueryBuilder.and(query, taxonName("9ORTO")); // ID: 11308

        QueryResponse response = searchEngine.getQueryResponse(query);

        List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
        assertThat(retrievedAccessions, containsInAnyOrder(ACCESSION2, ACCESSION3));
    }

    @Test
    void partialCommonLineageNameMatchesEntry2And3() {
        String query = taxonName("influenza");

        QueryResponse response = searchEngine.getQueryResponse(query);

        List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
        assertThat(retrievedAccessions, containsInAnyOrder(ACCESSION2, ACCESSION3));
    }

    @Test
    void lineageIdMatchesEntry1() {
        String query = taxonID(7787);
        query = QueryBuilder.and(query, taxonID(7711));
        query = QueryBuilder.and(query, taxonID(33208));
        query = QueryBuilder.and(query, taxonID(2759));
        query = QueryBuilder.and(query, taxonID(1));

        QueryResponse response = searchEngine.getQueryResponse(query);

        List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
        assertThat(retrievedAccessions, containsInAnyOrder(ACCESSION1));
    }

    @Test
    void lineageIdMatchesEntry2() {
        String query = taxonID(100673);
        query = QueryBuilder.and(query, taxonID(11552));
        query = QueryBuilder.and(query, taxonID(197913));
        query = QueryBuilder.and(query, taxonID(11308));
        query = QueryBuilder.and(query, taxonID(1));

        QueryResponse response = searchEngine.getQueryResponse(query);

        List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
        assertThat(retrievedAccessions, containsInAnyOrder(ACCESSION2));
    }

    @Test
    void lineageIdMatchesEntry3() {
        String query = taxonID(93838);
        query = QueryBuilder.and(query, taxonID(11320));
        query = QueryBuilder.and(query, taxonID(197911));
        query = QueryBuilder.and(query, taxonID(11308));
        query = QueryBuilder.and(query, taxonID(1));

        QueryResponse response = searchEngine.getQueryResponse(query);

        List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
        assertThat(retrievedAccessions, containsInAnyOrder(ACCESSION3));
    }

    @Test
    void commonLineageIDMatchesEntry2And3() {
        String query = taxonID(11308);

        QueryResponse response = searchEngine.getQueryResponse(query);

        List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
        assertThat(retrievedAccessions, containsInAnyOrder(ACCESSION2, ACCESSION3));
    }

    private String taxonName(String value) {
        return query(
                searchEngine.getSearchFieldConfig().getSearchFieldItemByName("taxonomy_name"),
                value);
    }

    private static String taxonID(int taxonomy) {
        return query(
                searchEngine.getSearchFieldConfig().getSearchFieldItemByName("taxonomy_id"),
                String.valueOf(taxonomy));
    }
}
