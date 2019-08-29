package org.uniprot.store.indexer.search.uniprot;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.uniprot.store.indexer.search.uniprot.IdentifierSearchIT.ACC_LINE;
import static org.uniprot.store.indexer.search.uniprot.TestUtils.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.uniprot.core.flatfile.writer.LineType;
import org.uniprot.core.uniprot.feature.FeatureType;
import org.uniprot.store.search.field.QueryBuilder;
import org.uniprot.store.search.field.UniProtField;

public class FTPtmProcessSearchIT {
	public static final String Q6GZX4 = "Q6GZX4";
	public static final String Q197B1 = "Q197B1";
	private static final String UNIPROT_FLAT_FILE_ENTRY_PATH = "/it/uniprot/P0A377.43.dat";
	private static final String Q12345 = "Q12345";
	private static final String Q6GZN7 = "Q6GZN7";
	private static final String Q6V4H0 = "Q6V4H0";
	private static final String P48347 = "P48347";
	@ClassRule
	public static UniProtSearchEngine searchEngine = new UniProtSearchEngine();

	@BeforeClass
	public static void populateIndexWithTestData() throws IOException {
		// a test entry object that can be modified and added to index
		InputStream resourceAsStream = TestUtils.getResourceAsStream(UNIPROT_FLAT_FILE_ENTRY_PATH);
		UniProtEntryObjectProxy entryProxy = UniProtEntryObjectProxy.createEntryFromInputStream(resourceAsStream);

		// --------------
		entryProxy.updateEntryObject(LineType.AC, String.format(ACC_LINE, Q6GZX4));
		entryProxy.updateEntryObject(LineType.FT,
				"FT   MOD_RES     853    853       Phosphoserine.\n" + 
				"FT                                {ECO:0000244|PubMed:19690332,\n" + 
				"FT                                ECO:0000244|PubMed:23186163}.\n" +
				"FT   CHAIN        41    387       Protein disulfide isomerase pTAC5,\n" + 
				"FT                                chloroplastic. {ECO:0000255}.\n" + 
				"FT                                /FTId=PRO_0000441697."
				);
		searchEngine.indexEntry(convertToUniProtEntry(entryProxy));

		// --------------
		entryProxy.updateEntryObject(LineType.AC, String.format(ACC_LINE, Q197B1));
		entryProxy.updateEntryObject(LineType.FT,
				"FT   CARBOHYD     55     55       S-linked (Hex...) cysteine.\n" + 
				"FT                                {ECO:0000250}.\n" + 
				"FT   CARBOHYD    583    583       N-linked (GlcNAc...) asparagine.\n" + 
				"FT                                {ECO:0000255}.");
		searchEngine.indexEntry(convertToUniProtEntry(entryProxy));

		entryProxy.updateEntryObject(LineType.AC, String.format(ACC_LINE, Q12345));
		entryProxy.updateEntryObject(LineType.FT,
				"FT   LIPID       200    200       S-geranylgeranyl cysteine. {ECO:0000250}.\n" +
				"FT   DISULFID     51    177       Reversible.\n" + 
				"FT                                {ECO:0000250|UniProtKB:Q84MC7}.");
		searchEngine.indexEntry(convertToUniProtEntry(entryProxy));

		
		entryProxy.updateEntryObject(LineType.AC, String.format(ACC_LINE, Q6GZN7));
		entryProxy.updateEntryObject(LineType.FT,
				"FT   CROSSLNK     13     13       Glycyl lysine isopeptide (Lys-Gly)\n" + 
				"FT                                (interchain with G-Cter in ubiquitin).\n" + 
				"FT                                {ECO:0000269|PubMed:18716620}.\n" + 
				"FT   CROSSLNK    289    289       Glycyl lysine isopeptide (Lys-Gly)\n" + 
				"FT                                (interchain with G-Cter in ubiquitin).\n" + 
				"FT                                {ECO:0000269|PubMed:18716620}.");
		searchEngine.indexEntry(convertToUniProtEntry(entryProxy));
		
		entryProxy.updateEntryObject(LineType.AC, String.format(ACC_LINE, Q6V4H0));
		entryProxy.updateEntryObject(LineType.FT,
				"FT   INIT_MET      1      1       Removed. {ECO:0000244|PubMed:22814378}.\n"+
				"FT   PEPTIDE     311    320       Linker peptide.\n" + 
				"FT                                {ECO:0000305|PubMed:10785398}.");
		searchEngine.indexEntry(convertToUniProtEntry(entryProxy));
		
		entryProxy.updateEntryObject(LineType.AC, String.format(ACC_LINE, P48347));
		entryProxy.updateEntryObject(LineType.FT,
				"FT   SIGNAL        1     19       {ECO:0000269|PubMed:2765556}.\n" +
				"FT   PROPEP       17     27       Activation peptide. {ECO:0000250}.\n" + 
				"FT                                /FTId=PRO_0000027671.\n" +
				"FT   TRANSIT       1     20       Chloroplast. {ECO:0000255}.");
		searchEngine.indexEntry(convertToUniProtEntry(entryProxy));
		
		
		searchEngine.printIndexContents();
	}
	@Test
	public void modResFindEntryWithEvidenceLength() {
		String query = features(FeatureType.MOD_RES, "phosphoserine");
			query = QueryBuilder.and(query, featureLength(FeatureType.MOD_RES, 1, 1));
			String evidence = "ECO_0000244";
			query = QueryBuilder.and(query, featureEvidence(FeatureType.MOD_RES, evidence));
			QueryResponse response = searchEngine.getQueryResponse(query);

			List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
			System.out.println(retrievedAccessions);
			assertThat(retrievedAccessions, hasItems( Q6GZX4 ));
			assertThat(retrievedAccessions, not(hasItem( Q197B1)));
	}
	@Test
	public void lipidFindEntryWithEvidenceLength() {
		String query = features(FeatureType.LIPID, "cysteine");
			query = QueryBuilder.and(query, featureLength(FeatureType.LIPID, 1, 1));
			String evidence = "ECO_0000250";
			query = QueryBuilder.and(query, featureEvidence(FeatureType.LIPID, evidence));
			QueryResponse response = searchEngine.getQueryResponse(query);

			List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
			System.out.println(retrievedAccessions);
			assertThat(retrievedAccessions, hasItems( Q12345 ));
			assertThat(retrievedAccessions, not(hasItem( Q6GZX4)));
	}
	@Test
	public void carbohydFindEntryWithEvidenceLength() {
		String query = features(FeatureType.CARBOHYD, "cysteine");
			query = QueryBuilder.and(query, featureLength(FeatureType.CARBOHYD, 1, 1));
			String evidence = "ECO_0000255";
			query = QueryBuilder.and(query, featureEvidence(FeatureType.CARBOHYD, evidence));
			QueryResponse response = searchEngine.getQueryResponse(query);

			List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
			System.out.println(retrievedAccessions);
			assertThat(retrievedAccessions, hasItems( Q197B1 ));
			assertThat(retrievedAccessions, not(hasItem( Q6GZX4)));
	}
	@Test
	public void disulfidFindEntryWithEvidenceLength() {
		String query = features(FeatureType.DISULFID, "reversible");
			query = QueryBuilder.and(query, featureLength(FeatureType.DISULFID, 100, 150));
			String evidence = "ECO_0000250";
			query = QueryBuilder.and(query, featureEvidence(FeatureType.DISULFID, evidence));
			QueryResponse response = searchEngine.getQueryResponse(query);

			List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
			System.out.println(retrievedAccessions);
			assertThat(retrievedAccessions, hasItems( Q12345 ));
			assertThat(retrievedAccessions, not(hasItem( Q6GZX4)));
	}
	
	@Test
	public void crosslinkFindEntryWithEvidenceLength() {
		String query = features(FeatureType.CROSSLNK, "lysine");
			query = QueryBuilder.and(query, featureLength(FeatureType.CROSSLNK, 1, 1));
			String evidence = "ECO_0000269";
			query = QueryBuilder.and(query, featureEvidence(FeatureType.CROSSLNK, evidence));
			QueryResponse response = searchEngine.getQueryResponse(query);

			List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
			System.out.println(retrievedAccessions);
			assertThat(retrievedAccessions, hasItems( Q6GZN7 ));
			assertThat(retrievedAccessions, not(hasItem( Q6GZX4)));
	}
	
	@Test
	public void chainFindEntryWithEvidenceLength() {
		String query = features(FeatureType.CHAIN, "disulfide");
			query = QueryBuilder.and(query, featureLength(FeatureType.CHAIN, 200, 400));
			String evidence = "ECO_0000255";
			query = QueryBuilder.and(query, featureEvidence(FeatureType.CHAIN, evidence));
			QueryResponse response = searchEngine.getQueryResponse(query);

			List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
			System.out.println(retrievedAccessions);
			assertThat(retrievedAccessions, hasItems( Q6GZX4 ));
			assertThat(retrievedAccessions, not(hasItem( Q197B1)));
	}
	
	@Test
	public void initMetFindEntryWithEvidenceLength() {
		String query = features(FeatureType.INIT_MET, "removed");
			query = QueryBuilder.and(query, featureLength(FeatureType.INIT_MET, 1, 1));
			String evidence = "ECO_0000244";
			query = QueryBuilder.and(query, featureEvidence(FeatureType.INIT_MET, evidence));

			QueryResponse response = searchEngine.getQueryResponse(query);

			List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
			System.out.println(retrievedAccessions);
			assertThat(retrievedAccessions, hasItems( Q6V4H0 ));
			assertThat(retrievedAccessions, not(hasItem( Q197B1)));
	}
	@Test
	public void peptideFindEntryWithEvidenceLength() {
		String query = features(FeatureType.PEPTIDE, "peptide");
			query = QueryBuilder.and(query, featureLength(FeatureType.PEPTIDE, 10, 20));
			String evidence = "ECO_0000305";
			query = QueryBuilder.and(query, featureEvidence(FeatureType.PEPTIDE, evidence));
			QueryResponse response = searchEngine.getQueryResponse(query);

			List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
			System.out.println(retrievedAccessions);
			assertThat(retrievedAccessions, hasItems( Q6V4H0 ));
			assertThat(retrievedAccessions, not(hasItem( Q197B1)));
	}
	@Test
	public void signalFindEntryWithEvidenceLength() {
		String query = features(FeatureType.SIGNAL, "*");
			query = QueryBuilder.and(query, featureLength(FeatureType.SIGNAL, 10, 20));
			String evidence = "ECO_0000269";
			query = QueryBuilder.and(query, featureEvidence(FeatureType.SIGNAL, evidence));
			QueryResponse response = searchEngine.getQueryResponse(query);

			List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
			System.out.println(retrievedAccessions);
			assertThat(retrievedAccessions, hasItems( P48347 ));
			assertThat(retrievedAccessions, not(hasItem( Q197B1)));
	}
	@Test
	public void propepFindEntryWithEvidenceLength() {
		String query = features(FeatureType.PROPEP, "peptide");
			query = QueryBuilder.and(query, featureLength(FeatureType.PROPEP, 5, 20));
			String evidence = "ECO_0000250";
			query = QueryBuilder.and(query, featureEvidence(FeatureType.PROPEP, evidence));
			QueryResponse response = searchEngine.getQueryResponse(query);

			List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
			System.out.println(retrievedAccessions);
			assertThat(retrievedAccessions, hasItems( P48347 ));
			assertThat(retrievedAccessions, not(hasItem( Q197B1)));
	}
	@Test
	public void transitFindEntryWithEvidenceLength() {
		String query = features(FeatureType.TRANSIT, "chloroplast");
			query = QueryBuilder.and(query, featureLength(FeatureType.TRANSIT, 5, 20));
			String evidence = "ECO_0000255";
			query = QueryBuilder.and(query, featureEvidence(FeatureType.TRANSIT, evidence));

			QueryResponse response = searchEngine.getQueryResponse(query);

			List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
			System.out.println(retrievedAccessions);
			assertThat(retrievedAccessions, hasItems( P48347 ));
			assertThat(retrievedAccessions, not(hasItem( Q197B1)));
	}
	
	@Test
	public void moleculeProcessFindTwoEntry() {
		String query = query(UniProtField.Search.ft_molecule_processing, "peptide");
		QueryResponse response = searchEngine.getQueryResponse(query);

		List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
		System.out.println(retrievedAccessions);
		assertThat(retrievedAccessions, hasItems(P48347, Q6V4H0));

	}

	@Test
	public void moleculeProcessFindTwoEntryWithLength() {
		String query = query(UniProtField.Search.ft_molecule_processing, "peptide");
		query = QueryBuilder.and(query, QueryBuilder.rangeQuery(UniProtField.Search.ftlen_molecule_processing.name(), 9, 10));

		QueryResponse response = searchEngine.getQueryResponse(query);

		List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
		System.out.println(retrievedAccessions);
		assertThat(retrievedAccessions, hasItems(Q6V4H0));
		assertThat(retrievedAccessions, not(hasItem( P48347)));
	}

	@Test
	public void moleculeProcessFindEntryWithLengthAndEvidence() {
		String query = query(UniProtField.Search.ft_molecule_processing, "peptide");
		query = QueryBuilder.and(query, QueryBuilder.rangeQuery(UniProtField.Search.ftlen_molecule_processing.name(), 9, 20));
		String evidence = "ECO_0000269";
		query = QueryBuilder.and(query, query(UniProtField.Search.ftev_molecule_processing, evidence));
		QueryResponse response = searchEngine.getQueryResponse(query);

		List<String> retrievedAccessions = searchEngine.getIdentifiers(response);
		System.out.println(retrievedAccessions);
		assertThat(retrievedAccessions, hasItems(P48347));
		assertThat(retrievedAccessions, not(hasItem(Q6V4H0)));
	}

}