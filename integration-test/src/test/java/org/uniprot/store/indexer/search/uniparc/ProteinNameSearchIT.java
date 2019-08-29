package org.uniprot.store.indexer.search.uniparc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.Is.is;

import java.io.IOException;
import java.util.List;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.uniprot.core.uniparc.UniParcDatabaseType;
import org.uniprot.core.xml.jaxb.uniparc.DbReferenceType;
import org.uniprot.core.xml.jaxb.uniparc.Entry;
import org.uniprot.store.search.field.QueryBuilder;
import org.uniprot.store.search.field.UniParcField;

/**
 * Tests the search capabilities of the {@link UniParcQueryBuilder} when it comes to searching for UniParc entries
 * that reference protein names
 */
public class ProteinNameSearchIT {
    @ClassRule
    public static UniParcSearchEngine searchEngine = new UniParcSearchEngine();

    private static final String ID_1 = "UPI0000000001";
    private static final String ID_2 = "UPI0000000002";
    private static final String ID_3 = "UPI0000000003";
    private static final String ID_4 = "UPI0000000004";
    private static final String ID_5 = "UPI0000000005";
    private static final String ID_6 = "UPI0000000006";
    private static final String NAME_1 = "hypothetical protein";
    private static final String NAME_2 = "Protein kinase C inhibitor KCIP-1 isoform eta";
    private static final String NAME_3 = "14-3-3";
    private static final String NAME_4 = "hydrophobe/amphiphile efflux-1 (HAE1) family transporter";
    private static final String NAME_5 = "PP2A B subunit isoform B'-delta";
    private static final String NAME_6 = "Methylenetetrahydrofolate dehydrogenase (NADP(+))";

    @BeforeClass
    public static void populateIndexWithTestData() throws IOException {
        // a test entry object that can be modified and added to index

        
        //Entry 1
        {
            Entry entry = TestUtils.createDefaultUniParcEntry();
            entry.setAccession(ID_1);
            entry.getDbReference().clear();
            
            DbReferenceType xref= TestUtils.createXref(UniParcDatabaseType.TREMBL.getName(), "P47986", "Y");
            xref.getProperty().add(TestUtils.createProperty("protein_name", NAME_1));
            entry.getDbReference().add(xref);
            searchEngine.indexEntry(entry);
        }

        //Entry 2
        {
            Entry entry = TestUtils.createDefaultUniParcEntry();
            entry.setAccession(ID_2);
            entry.getDbReference().clear();
            
            DbReferenceType xref= TestUtils.createXref(UniParcDatabaseType.TREMBL.getName(), "P47986", "Y");
            xref.getProperty().add(TestUtils.createProperty("protein_name", NAME_2));
            entry.getDbReference().add(xref);
            searchEngine.indexEntry(entry);
        }



        //Entry 3
        {
            Entry entry = TestUtils.createDefaultUniParcEntry();
            entry.setAccession(ID_3);
            entry.getDbReference().clear();
            
            DbReferenceType xref= TestUtils.createXref(UniParcDatabaseType.TREMBL.getName(), "P47986", "Y");
            xref.getProperty().add(TestUtils.createProperty("protein_name", NAME_3));
            entry.getDbReference().add(xref);
            searchEngine.indexEntry(entry);
        }


        //Entry 4
        {
            Entry entry = TestUtils.createDefaultUniParcEntry();
            entry.setAccession(ID_4);
            entry.getDbReference().clear();
            
            DbReferenceType xref= TestUtils.createXref(UniParcDatabaseType.TREMBL.getName(), "P47986", "Y");
            xref.getProperty().add(TestUtils.createProperty("protein_name", NAME_4));
            entry.getDbReference().add(xref);
            searchEngine.indexEntry(entry);
        }


        //Entry 5
        {
            Entry entry = TestUtils.createDefaultUniParcEntry();
            entry.setAccession(ID_5);
            entry.getDbReference().clear();
            
            DbReferenceType xref= TestUtils.createXref(UniParcDatabaseType.TREMBL.getName(), "P47986", "Y");
            xref.getProperty().add(TestUtils.createProperty("protein_name", NAME_5));
            entry.getDbReference().add(xref);
            searchEngine.indexEntry(entry);
        }
        

        //Entry 6
        {
            Entry entry = TestUtils.createDefaultUniParcEntry();
            entry.setAccession(ID_6);
            entry.getDbReference().clear();
            
            DbReferenceType xref= TestUtils.createXref(UniParcDatabaseType.TREMBL.getName(), "P47986", "Y");
            xref.getProperty().add(TestUtils.createProperty("protein_name", NAME_6));
            entry.getDbReference().add(xref);
            searchEngine.indexEntry(entry);
        }
        


        searchEngine.printIndexContents();
    }

    @Test
    public void nonExistentProteinNameMatchesNoDocuments() throws Exception {
        String query = proteinName("Unknown");

        QueryResponse response = searchEngine.getQueryResponse(query);

        List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
        assertThat(retrievedAccessions, is(empty()));
    }

    @Test
    public void searchName1HitsEntry1() throws Exception {
        String query = proteinName(NAME_1);

        QueryResponse response = searchEngine.getQueryResponse(query);

        List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
        assertThat(retrievedAccessions, contains(ID_1));
    }

    @Test
    public void lowerCaseSearchName1HitsEntry1() throws Exception {
        String query = proteinName(NAME_1.toLowerCase());

        QueryResponse response = searchEngine.getQueryResponse(query);

        List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
        assertThat(retrievedAccessions, contains(ID_1));
    }

    @Test
    public void upperCaseSearchName1HitsEntry1() throws Exception {
        String query = proteinName(NAME_1.toUpperCase());

        QueryResponse response = searchEngine.getQueryResponse(query);

        List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
        assertThat(retrievedAccessions, contains(ID_1));
    }

    @Test
    public void partialSearchName1HitsEntry1() throws Exception {
        String query = proteinName("hypothetical");

        QueryResponse response = searchEngine.getQueryResponse(query);

        List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
        assertThat(retrievedAccessions, contains(ID_1));
    }

    @Test
    public void searchName2HitsEntry2() throws Exception {
        String query = proteinName("Protein kinase C inhibitor KCIP-1 isoform eta");

        QueryResponse response = searchEngine.getQueryResponse(query);

        List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
        assertThat(retrievedAccessions, contains(ID_2));
    }

    @Test
    public void partialSearchUsingHyphenatedNameInName2HitsEntry2() throws Exception {
        String query = proteinName("KCIP-1");

        QueryResponse response = searchEngine.getQueryResponse(query);

        List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
        assertThat(retrievedAccessions, contains(ID_2));
    }

    @Test
    public void noMatchForPartialSearchUsingHyphenatedNameWithExtraNonExistentCharacter() throws Exception {
        String query = proteinName("KCIPE-1");

        QueryResponse response = searchEngine.getQueryResponse(query);

        List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
        assertThat(retrievedAccessions, is(empty()));
    }

    @Test
    public void partialSearchUsingHyphenatedNameWithoutTheHyphenHitsEntry2() throws Exception {
        String query = proteinName("KCIP 1");

        QueryResponse response = searchEngine.getQueryResponse(query);

        List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
        assertThat(retrievedAccessions, contains(ID_2));
    }

    @Test
    public void partialSearchForHyphenatedNameWithoutLeftSideOfHyphenationHitsEntry2() throws Exception {
        String query = proteinName("KCIP");

        QueryResponse response = searchEngine.getQueryResponse(query);

        List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
        assertThat(retrievedAccessions, contains(ID_2));
    }

    @Test
    public void noMatchForPartialSubstringSearchOnHyphenatedName() throws Exception {
        String query = proteinName("KCI");

        QueryResponse response = searchEngine.getQueryResponse(query);

        List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
        assertThat(retrievedAccessions, is(empty()));
    }

    @Test
    public void searchForHyphenatedAndNumberedNameMatchesEntry3() throws Exception {
        String query = proteinName("14-3-3");

        QueryResponse response = searchEngine.getQueryResponse(query);

        List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
        assertThat(retrievedAccessions, contains(ID_3));
    }

    @Test
    public void searchForPartialHyphenatedAndNumberedNameMatchesEntry3() throws Exception {
        String query = proteinName("14-3");

        QueryResponse response = searchEngine.getQueryResponse(query);

        List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
        assertThat(retrievedAccessions, contains(ID_3));
    }

    @Test
    public void searchForPartialHyphenatedAndNumberedNameWithoutHyphenMatchesEntry3() throws Exception {
        String query = proteinName("14 3");

        QueryResponse response = searchEngine.getQueryResponse(query);

        List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
        assertThat(retrievedAccessions, contains(ID_3));
    }

    @Test
    public void noMatchForPartialIncorrectHyphenatedAndNumberedNameMatches0Entries() throws Exception {
        String query = proteinName("14-1");

        QueryResponse response = searchEngine.getQueryResponse(query);

        List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
        assertThat(retrievedAccessions, is(empty()));
    }

    @Test
    public void searchForNameWithParenthesisMatchesEntry4() throws Exception {
        String query = proteinName(NAME_4);

        QueryResponse response = searchEngine.getQueryResponse(query);

        List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
        assertThat(retrievedAccessions, contains(ID_4));
    }

    @Test
    public void partialSearchUsingNameInBetweenParenthesisWithMatchesEntry4() throws Exception {
        String query = proteinName("HAE1");

        QueryResponse response = searchEngine.getQueryResponse(query);

        List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
        assertThat(retrievedAccessions, contains(ID_4));
    }

    @Test
    public void searchForNameWithSingleQuoteMatchesEntry5() throws Exception {
        String query = proteinName(NAME_5);

        QueryResponse response = searchEngine.getQueryResponse(query);

        List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
        assertThat(retrievedAccessions, contains(ID_5));
    }

    @Test
    public void partialSearchForNameWithoutSingleQuoteMatchesEntry5() throws Exception {
        String query = proteinName("B-delta");

        QueryResponse response = searchEngine.getQueryResponse(query);

        List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
        assertThat(retrievedAccessions, contains(ID_5));
    }

    @Test
    public void searchForNameWithChemicalSymbolUsingMatchesEntry6() throws Exception {
        String query = proteinName(NAME_6);

        QueryResponse response = searchEngine.getQueryResponse(query);

        List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
        assertThat(retrievedAccessions, contains(ID_6));
    }

    @Test
    public void partialSearchForNameWithChemicalSymbolUsingSubstringOfChemicalSymbolMatchesEntry6() throws Exception {
        String query = proteinName("NADP");

        QueryResponse response = searchEngine.getQueryResponse(query);

        List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
        assertThat(retrievedAccessions, contains(ID_6));
    }
    private String proteinName(String value) {
    	return QueryBuilder.query(UniParcField.Search.protein.name(),value);
    }
    
}