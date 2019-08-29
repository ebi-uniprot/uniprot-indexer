package org.uniprot.store.indexer.search.uniprot;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.uniprot.core.flatfile.writer.LineType;
import org.uniprot.core.uniprot.comment.CommentType;
import org.uniprot.store.search.field.QueryBuilder;
import org.uniprot.store.search.field.UniProtField;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.uniprot.store.indexer.search.uniprot.IdentifierSearchIT.ACC_LINE;
import static org.uniprot.store.indexer.search.uniprot.TestUtils.*;
import static org.hamcrest.Matchers.*;

/**
 * Tests showing the behaviour of searching CC fields
 */
public class CCSearchIT {
    public static final String Q6GZX4 = "Q6GZX4";
    public static final String Q6GZX3 = "Q6GZX3";
    public static final String Q6GZY3 = "Q6GZY3";
    public static final String Q197B6 = "Q197B6";
    private static final String UNIPROT_FLAT_FILE_ENTRY_PATH = "/it/uniprot/P0A377.43.dat";
    private static final String Q196W5 = "Q196W5";
    private static final String Q6GZN7 = "Q6GZN7";
    private static final String Q6V4H0 = "Q6V4H0";
    private static final String P48347 = "P48347";
    private static final String Q12345 = "Q12345";

    @ClassRule
    public static UniProtSearchEngine searchEngine = new UniProtSearchEngine();

    @BeforeClass
    public static void populateIndexWithTestData() throws IOException {
        // a test entry object that can be modified and added to index
        InputStream resourceAsStream = TestUtils.getResourceAsStream(UNIPROT_FLAT_FILE_ENTRY_PATH);
        UniProtEntryObjectProxy entryProxy = UniProtEntryObjectProxy.createEntryFromInputStream(resourceAsStream);

        // --------------
        entryProxy.updateEntryObject(LineType.AC, String.format(ACC_LINE, Q6GZX4));
        entryProxy.updateEntryObject(LineType.CC, "CC   -!- FUNCTION: Transcription activation. {ECO:0000305}.\n"+
        		"CC   -!- SEQUENCE CAUTION:\n"
				+ "CC       Sequence=CAA36850.1; Type=Frameshift; Positions=496;");
        searchEngine.indexEntry(convertToUniProtEntry(entryProxy));

        // --------------
        entryProxy.updateEntryObject(LineType.AC, String.format(ACC_LINE, Q6GZX3));
        entryProxy.updateEntryObject(LineType.CC,
                "CC   -!- SUBCELLULAR LOCATION: This-is-a-word Host membrane extraWord {ECO:0000305}; Single-pass\n" +
                        "CC       membrane protein {ECO:0000305}.\n"
                        + "CC   -!- BIOPHYSICOCHEMICAL PROPERTIES:\n" + 
                        "CC       Absorption:\n" + 
                        "CC         Abs(max)=~715 nm;\n" + 
                        "CC         Note=Emission maxima at 735 nm. {ECO:0000269|PubMed:11553743};");
        searchEngine.indexEntry(convertToUniProtEntry(entryProxy));

        // --------------
        entryProxy.updateEntryObject(LineType.AC, String.format(ACC_LINE, Q6GZY3));
        entryProxy.updateEntryObject(LineType.CC,
                "CC   -!- SUBCELLULAR LOCATION: This-is-a Host membrane; Single-pass\n" +
                        "CC       membrane protein. Note=Localizes at mid-cell.");
        searchEngine.indexEntry(convertToUniProtEntry(entryProxy));

        // --------------
        entryProxy.updateEntryObject(LineType.AC, String.format(ACC_LINE, Q197B6));
        entryProxy.updateEntryObject(LineType.CC,
                "CC   -!- SIMILARITY: Belongs to the protein kinase superfamily. Ser/Thr\n" +
                        "CC       protein kinase family. {ECO:0000255|PROSITE-ProRule:PRU00159}.\n" +
                        "CC   -!- SIMILARITY: Contains 1 protein kinase domain.\n" +
                        "CC       {ECO:0000255|PROSITE-ProRule:PRU00159}.");
        searchEngine.indexEntry(convertToUniProtEntry(entryProxy));

        // --------------
        entryProxy.updateEntryObject(LineType.AC, String.format(ACC_LINE, Q196W5));
        entryProxy.updateEntryObject(LineType.CC, "CC   -!- COFACTOR:\n" +
                "CC       Name=Zn(2+); Xref=ChEBI:CHEBI:29105; Evidence={ECO:0000250};\n" +
                "CC       Note=Binds 1 zinc ion per subunit. {ECO:0000250};\n" +
                "CC   -!- SUBCELLULAR LOCATION: Secreted {ECO:0000305}.\n" +
                "CC   -!- DOMAIN: The conserved cysteine present in the cysteine-switch\n" +
                "CC       motif binds the catalytic zinc ion, thus inhibiting the enzyme.\n" +
                "CC       The dissociation of the cysteine from the zinc ion upon the\n" +
                "CC       activation-peptide release activates the enzyme.\n" +
                "CC   -!- SIMILARITY: Belongs to the peptidase M10A family. {ECO:0000305}.");
        searchEngine.indexEntry(convertToUniProtEntry(entryProxy));

        // --------------
        entryProxy.updateEntryObject(LineType.AC, String.format(ACC_LINE, Q6GZN7));
        entryProxy
                .updateEntryObject(LineType.CC,
                        "CC   -!- CATALYTIC ACTIVITY:\n" +
                        "CC       Reaction=O2 + 2 R'C(R)SH = H2O2 + R'C(R)S-S(R)CR';\n" +
                        "CC         Xref=Rhea:RHEA:17357, ChEBI:CHEBI:15379, ChEBI:CHEBI:16240,\n" +
                        "CC         ChEBI:CHEBI:16520, ChEBI:CHEBI:17412; EC=1.8.3.2;\n" +
                        "CC   -!- COFACTOR:\n" +
                        "CC       Name=FAD; Xref=ChEBI:CHEBI:57692;\n" +
                        "CC         Evidence={ECO:0000255|PROSITE-ProRule:PRU00654};");
        searchEngine.indexEntry(convertToUniProtEntry(entryProxy));

        // --------------
        entryProxy.updateEntryObject(LineType.AC, String.format(ACC_LINE, Q6V4H0));
        entryProxy.updateEntryObject(LineType.CC,
                "CC   -!- CATALYTIC ACTIVITY:\n" +
                "CC       Reaction=(6E)-8-hydroxygeraniol + 2 NADP(+) = (6E)-8-oxogeranial +\n" +
                "CC         2 H(+) + 2 NADPH; Xref=Rhea:RHEA:32659, ChEBI:CHEBI:15378,\n" +
                "CC         ChEBI:CHEBI:57783, ChEBI:CHEBI:58349, ChEBI:CHEBI:64235,\n" +
                "CC         ChEBI:CHEBI:64239; EC=1.1.1.324; Evidence={ECO:0000269|Ref.1};\n"
                + "CC   -!- BIOPHYSICOCHEMICAL PROPERTIES:\n" +
                "CC       Kinetic parameters:\n" +
                "CC         KM=6.9 uM for Ins(1,3,4,5)P(4) {ECO:0000269|PubMed:9359836};\n" +
                "CC         Vmax=302 pmol/min/ug enzyme {ECO:0000269|PubMed:9359836};\n"
              +     "CC       Redox potential:\n" +
                    "CC         E(0) is about 178 mV. {ECO:0000269|PubMed:10433554};");
        searchEngine.indexEntry(convertToUniProtEntry(entryProxy));

        // --------------
        entryProxy.updateEntryObject(LineType.AC, String.format(ACC_LINE, P48347));
        entryProxy.updateEntryObject(LineType.CC, "CC   -!- ALTERNATIVE PRODUCTS:\n" +
                "CC       Event=Alternative splicing; Named isoforms=2;\n" +
                "CC       Name=1;\n" +
                "CC         IsoId=P48347-1; Sequence=Displayed;\n" +
                "CC       Name=2;\n" +
                "CC         IsoId=P48347-2; Sequence=VSP_008972;");
        searchEngine.indexEntry(convertToUniProtEntry(entryProxy));

        // --------------
        entryProxy.updateEntryObject(LineType.AC, String.format(ACC_LINE, Q12345));
        entryProxy.updateEntryObject(LineType.CC, "CC   -!- INTERACTION:\n" +
                "CC       Q41009:TOC34 (xeno); NbExp=2; IntAct=EBI-1803304, EBI-638506;\n"
                + "CC   -!- BIOPHYSICOCHEMICAL PROPERTIES:\n" + 
                "CC       Kinetic parameters:\n" + 
                "CC         KM=620 uM for O-phospho-L-serine (at 70 degrees Celsius and at\n" + 
                "CC         pH 7.5) {ECO:0000269|PubMed:12051918};\n" + 
                "CC       pH dependence:\n" + 
                "CC         Optimum pH is 7.5. {ECO:0000269|PubMed:12051918};\n" + 
                "CC       Temperature dependence:\n" + 
                "CC         Optimum temperature is 70 degrees Celsius.\n" + 
                "CC         {ECO:0000269|PubMed:12051918};");
        searchEngine.indexEntry(convertToUniProtEntry(entryProxy));

        searchEngine.printIndexContents();
    }

    @Test
    public void shouldFindTwoCofactorEntry() {
        String query = comments(CommentType.COFACTOR, "*");

        QueryResponse response = searchEngine.getQueryResponse(query);

        List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
        assertThat(retrievedAccessions, containsInAnyOrder(Q196W5, Q6GZN7));
    }

    @Test
    public void shouldFindOneFunctionEntry() {
        String query = comments(CommentType.FUNCTION, "*");

        QueryResponse response = searchEngine.getQueryResponse(query);

        List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
        assertThat(retrievedAccessions, containsInAnyOrder(Q6GZX4));
    }

    @Test
    public void domainFindLongCopyPastedSection() {
        String query = comments(CommentType.DOMAIN, "motif binds the catalytic zinc ion, thus inhibiting the enzyme.");

        QueryResponse response = searchEngine.getQueryResponse(query);

        List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
        assertThat(retrievedAccessions, contains(Q196W5));
    }

    @Test
    public void catalyticActivFindNADP() {
        String query = comments(CommentType.CATALYTIC_ACTIVITY, "NADP");

        QueryResponse response = searchEngine.getQueryResponse(query);

        List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
        assertThat(retrievedAccessions, contains(Q6V4H0));
    }

    @Test
    public void catalyticActivFindCopyAndPastedEquation() {
        String query = comments(CommentType.CATALYTIC_ACTIVITY, "2 R'C(R)SH + O(2) = R'C(R)S-S(R)CR'");

        QueryResponse response = searchEngine.getQueryResponse(query);

        List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
        assertThat(retrievedAccessions, contains(Q6GZN7));
    }

    @Test
    public void catalyticActivFindHydroxygeraniolCI() {
        String query = comments(CommentType.CATALYTIC_ACTIVITY, "HydrOxyGeraniol");

        QueryResponse response = searchEngine.getQueryResponse(query);

        List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
        assertThat(retrievedAccessions, contains(Q6V4H0));
    }

    @Test
    public void catalyticActivFindHydroxygeraniolExact() {
        String query = comments(CommentType.CATALYTIC_ACTIVITY, "hydroxygeraniol");

        QueryResponse response = searchEngine.getQueryResponse(query);

        List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
        assertThat(retrievedAccessions, contains(Q6V4H0));
    }

    @Test
    public void catalyticActivFindO2() {
        String query = comments(CommentType.CATALYTIC_ACTIVITY, "O(2)");

        QueryResponse response = searchEngine.getQueryResponse(query);

        List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
        assertThat(retrievedAccessions, contains(Q6GZN7));
    }

    @Test
    public void functionExactlyCorrect() {
    	String query = query(UniProtField.Search.accession, Q6GZX4);
         query = QueryBuilder.and(query, comments(CommentType.FUNCTION, "Transcription activation."));

        QueryResponse response = searchEngine.getQueryResponse(query);

        List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
        assertThat(retrievedAccessions, contains(Q6GZX4));
    }

    @Test
    public void functionWithoutTerminalStop() {
    	String query = query(UniProtField.Search.accession, Q6GZX4);
         query = QueryBuilder.and(query, comments(CommentType.FUNCTION, "Transcription activation"));

        QueryResponse response = searchEngine.getQueryResponse(query);

        List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
        assertThat(retrievedAccessions, contains(Q6GZX4));
    }

    @Test
    public void subcellularSpanning2Lines() {
    	String query = query(UniProtField.Search.accession, Q6GZX3);
         query = QueryBuilder.and(query, comments(CommentType.SUBCELLULAR_LOCATION, "Single-pass membrane protein"));
        QueryResponse response = searchEngine.getQueryResponse(query);

        List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
        assertThat(retrievedAccessions, contains(Q6GZX3));
    }

    @Test
    public void subcellularFindWithoutSemiColon() {
    	String query = query(UniProtField.Search.accession, Q6GZY3);
         query = QueryBuilder.and(query, comments(CommentType.SUBCELLULAR_LOCATION, "membrane"));

        QueryResponse response = searchEngine.getQueryResponse(query);

        List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
        assertThat(retrievedAccessions, contains(Q6GZY3));
    }

    @Test
    public void subcellularFindBothEntriesFromCommonTerm() {
        String query = comments(CommentType.SUBCELLULAR_LOCATION, "Host membrane");

        QueryResponse response = searchEngine.getQueryResponse(query);

        List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
        assertThat(retrievedAccessions, containsInAnyOrder(Q6GZY3, Q6GZX3));
    }

    @Test
    public void subcellularFindOneEntryFromMoreSpecificTerm() {
        String query = comments(CommentType.SUBCELLULAR_LOCATION, "Host membrane extraWord");

        QueryResponse response = searchEngine.getQueryResponse(query);

        List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
        assertThat(retrievedAccessions, contains(Q6GZX3));
    }

    @Test
    public void subcellularFindOneEntryFromSpecificHyphenatedTerm() {
        String query = comments(CommentType.SUBCELLULAR_LOCATION, "this-is-a-word");

        QueryResponse response = searchEngine.getQueryResponse(query);

        List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
        assertThat(retrievedAccessions, contains(Q6GZX3));
    }

    @Test
    public void subcellularFindBothEntriesFromHyphenatedTerm() {
        String query = comments(CommentType.SUBCELLULAR_LOCATION, "this-is");

        QueryResponse response = searchEngine.getQueryResponse(query);

        List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
        assertThat(retrievedAccessions, containsInAnyOrder(Q6GZY3, Q6GZX3));
    }

    @Test
    public void subcellularFindBothWithoutSemiColon() {
        String query = comments(CommentType.SUBCELLULAR_LOCATION, "membrane");

        QueryResponse response = searchEngine.getQueryResponse(query);

        List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
        assertThat(retrievedAccessions, containsInAnyOrder(Q6GZY3, Q6GZX3));
    }

    @Test
    public void subcellularPartialOrdered() {
    	String query =query(UniProtField.Search.accession, Q197B6);
         query = QueryBuilder.and(query,
                comments(CommentType.SIMILARITY, "Belongs to the protein kinase superfamily."));

        QueryResponse response = searchEngine.getQueryResponse(query);

        List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
        assertThat(retrievedAccessions, contains(Q197B6));
    }

    @Test
    public void subcellularPartialOrderedWithForwardSlash() {
    	String query =query(UniProtField.Search.accession, Q197B6);
         query = QueryBuilder.and(query, comments(CommentType.SIMILARITY, "Ser/Thr protein kinase family"));

        QueryResponse response = searchEngine.getQueryResponse(query);

        List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
        assertThat(retrievedAccessions, contains(Q197B6));
    }

    @Test
    public void subcellularPartialUnordered() {
    	String query =query(UniProtField.Search.accession, Q197B6);
         query = QueryBuilder.and(query, comments(CommentType.SIMILARITY, "protein kinase family Ser/Thr"));

        QueryResponse response = searchEngine.getQueryResponse(query);

        List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
        assertThat(retrievedAccessions, contains(Q197B6));
    }

    @Test
    public void findIsoformWithinAlternativeProductsComment() {
        String query = comments(CommentType.ALTERNATIVE_PRODUCTS, "P48347-1");

        QueryResponse response = searchEngine.getQueryResponse(query);

        List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
        assertThat(retrievedAccessions, contains(P48347));
    }

    @Test
    public void findFirstInteractionIdentifierWithinInteractionComment() {
        String query = comments(CommentType.INTERACTION, "EBI-1803304");

        QueryResponse response = searchEngine.getQueryResponse(query);

        List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
        assertThat(retrievedAccessions, contains(Q12345));
    }

    @Test
    public void findSecondInteractionIdentifierWithinInteractionComment() {
        String query = comments(CommentType.INTERACTION, "EBI-638506");

        QueryResponse response = searchEngine.getQueryResponse(query);

        List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
        assertThat(retrievedAccessions, contains(Q12345));
    }
    @Test
    public void findCofactorWithChebi() {
    		String query= query(UniProtField.Search.cc_cofactor_chebi, "57692");
    		QueryResponse response = searchEngine.getQueryResponse(query);

            List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
            assertThat(retrievedAccessions, contains(Q6GZN7));
    }
    @Test
    public void findCofactorWithNote() {
    		String query= query(UniProtField.Search.cc_cofactor_note, "zinc");
    		QueryResponse response = searchEngine.getQueryResponse(query);

            List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
            assertThat(retrievedAccessions, contains(Q196W5));
    }
    @Test
    public void findBPCPWithAbsorption() {
    		String query= query(UniProtField.Search.cc_bpcp_absorption, "emission");
    		QueryResponse response = searchEngine.getQueryResponse(query);
    		List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
    		assertThat(retrievedAccessions, contains(Q6GZX3));
    }
    @Test
    public void findBPCPWithKinetics() {
    		String query= query(UniProtField.Search.cc_bpcp_kinetics, "enzyme");
    		QueryResponse response = searchEngine.getQueryResponse(query);
    		List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
    		System.out.println(retrievedAccessions);
    		assertThat(retrievedAccessions, hasItem(Q6V4H0));
    		assertThat(retrievedAccessions, not(hasItem(Q12345)));
    }
    @Test
    public void findBPCPWithPhDependence() {
    		String query= query(UniProtField.Search.cc_bpcp_ph_dependence, "optimum");
    		QueryResponse response = searchEngine.getQueryResponse(query);
    		List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
    		assertThat(retrievedAccessions, hasItem(Q12345));
    		assertThat(retrievedAccessions, not(hasItem(Q6V4H0)));
    }
    @Test
    public void findBPCPWithTempDependence() {
    		String query= query(UniProtField.Search.cc_bpcp_temp_dependence, "70");
    		QueryResponse response = searchEngine.getQueryResponse(query);
    		List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
    		assertThat(retrievedAccessions, hasItem(Q12345));
    		assertThat(retrievedAccessions, not(hasItem(Q6V4H0)));
    }
    @Test
    public void findBPCPWithRedox() {
    		String query= query(UniProtField.Search.cc_bpcp_redox_potential, "178");
    		QueryResponse response = searchEngine.getQueryResponse(query);
    		List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
    		assertThat(retrievedAccessions, hasItem(Q6V4H0));
    		assertThat(retrievedAccessions, not(hasItem(Q12345)));
    }
    @Test
    public void findSubcellLocation() {
    		String query= query(UniProtField.Search.cc_scl_term, "membrane");
    		QueryResponse response = searchEngine.getQueryResponse(query);
    		List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
    		assertThat(retrievedAccessions, hasItem(Q6GZX3));
    		assertThat(retrievedAccessions, hasItem(Q6GZY3));
    }
    @Test
    public void findSubcellLocationNote() {
    		String query= query(UniProtField.Search.cc_scl_note, "Localizes");
    		QueryResponse response = searchEngine.getQueryResponse(query);
    		List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
    		assertThat(retrievedAccessions, hasItem(Q6GZY3));
    		assertThat(retrievedAccessions, not(hasItem(Q6GZX3)));
    }
    @Test
    public void findAPEvent() {
    		String query= query(UniProtField.Search.cc_ap_as, "splicing");
    		QueryResponse response = searchEngine.getQueryResponse(query);
    		List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
    		assertThat(retrievedAccessions, hasItem(P48347));
    		assertThat(retrievedAccessions, not(hasItem(Q6GZX3)));
    }
    @Test
    public void findSCType() {
    		String query= query(UniProtField.Search.cc_sc_framesh, "Frameshift");
    		QueryResponse response = searchEngine.getQueryResponse(query);
    		List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
    		assertThat(retrievedAccessions, hasItem(Q6GZX4));
    		assertThat(retrievedAccessions, not(hasItem(Q6GZX3)));
    }
    @Test
    public void findSCTypeNo() {
    		String query= query(UniProtField.Search.cc_sc_eterm, "Erroneous");
    		QueryResponse response = searchEngine.getQueryResponse(query);
    		List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
    		assertThat(retrievedAccessions, not(hasItem(Q6GZX4)));
    		assertThat(retrievedAccessions, not(hasItem(Q6GZX3)));
    }
}