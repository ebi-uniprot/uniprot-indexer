package org.uniprot.store.indexer.search.uniref;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.uniprot.core.xml.jaxb.uniref.Entry;
import org.uniprot.core.xml.jaxb.uniref.PropertyType;
import org.uniprot.store.search.field.QueryBuilder;
import org.uniprot.store.search.field.UniRefField;

/**
 *
 * @author jluo
 * @date: 19 Aug 2019
 *
*/

public class OrganismSearchIT {
	 private static final String ID_1 = "UniRef100_A0A007";
	    private static final String ID_2 = "UniRef100_A0A009DWI3";
	
	    private static final String NAME_1 = "Cluster: MoeK5";
	    private static final String NAME_2 = "Cluster: Transposase DDE domain protein (Fragment)";
	
	    private static final String organism_name ="Homo sapiens";
	    private static final String taxId = "9606";
	    
	    @ClassRule
	    public static UniRefSearchEngine searchEngine = new UniRefSearchEngine();
	    
	    @BeforeClass
	    public static void populateIndexWithTestData() throws IOException {
	        //Entry 1
	        {
	            Entry entry = TestUtils.createSkeletonEntry(ID_1, NAME_1);
	            entry.getProperty().addAll(createEntryOrganismProperty(organism_name, taxId));
	            entry.getRepresentativeMember().getDbReference().getProperty().addAll(
	            		createMemberOrganismProperty(organism_name, taxId));
	            searchEngine.indexEntry(entry);
	            
	        }
	        //Entry 2
	        {
	            Entry entry = TestUtils.createSkeletonEntry(ID_2, NAME_2);
	            entry.getProperty().addAll(createEntryOrganismProperty(organism_name, taxId));
	            entry.getRepresentativeMember().getDbReference().getProperty().addAll(
	            		createMemberOrganismProperty(organism_name, taxId));
	            searchEngine.indexEntry(entry);
	        }
	      
	        searchEngine.printIndexContents();
	    }
	    
	    
	    @Test
	    public void testOrganism() {
	    	String  query =organismQuery(organism_name);
	    	  QueryResponse queryResponse =
	                  searchEngine.getQueryResponse(query);
	          List<String> retrievedAccessions = searchEngine.getIdentifiers(queryResponse);
	          
	          Assert.assertEquals(2, retrievedAccessions.size());
	          assertThat(retrievedAccessions, containsInAnyOrder(ID_1, ID_2));
	    }
	    @Test
	    public void testOTaxonId() {
	    	String  query =taxIdQuery(taxId);
	    	  QueryResponse queryResponse =
	                  searchEngine.getQueryResponse(query);
	          List<String> retrievedAccessions = searchEngine.getIdentifiers(queryResponse);
	          
	          Assert.assertEquals(2, retrievedAccessions.size());
	          assertThat(retrievedAccessions, containsInAnyOrder(ID_1, ID_2));
	    }
	    
	    private String organismQuery(String  organism) {
	    	return QueryBuilder.query(UniRefField.Search.taxonomy_name.name(),organism);
	    }
	    private String taxIdQuery(String  taxId) {
	    	return QueryBuilder.query(UniRefField.Search.taxonomy_id.name(),taxId);
	    }
	    static List<PropertyType> createEntryOrganismProperty(String organism, String taxId){
	    	return Arrays.asList(TestUtils.createProperty("common taxon", organism),
	    			TestUtils.createProperty("common taxon ID", taxId)
	    			);
	    }
	    static List<PropertyType> createMemberOrganismProperty(String organism, String taxId){
	    	return Arrays.asList(TestUtils.createProperty("source organism", organism),
	    			TestUtils.createProperty("NCBI taxonomy", taxId)
	    			);
	    }
}
