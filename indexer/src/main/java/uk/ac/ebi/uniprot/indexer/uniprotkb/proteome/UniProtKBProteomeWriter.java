package uk.ac.ebi.uniprot.indexer.uniprotkb.proteome;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.batch.item.ItemWriter;
import org.springframework.data.solr.core.SolrTemplate;

import com.google.common.base.Strings;

import uk.ac.ebi.uniprot.indexer.uniprot.taxonomy.TaxonomicNode;
import uk.ac.ebi.uniprot.indexer.uniprot.taxonomy.TaxonomyRepo;
import uk.ac.ebi.uniprot.search.SolrCollection;
import uk.ac.ebi.uniprot.xml.jaxb.proteome.ComponentType;
import uk.ac.ebi.uniprot.xml.jaxb.proteome.ProteinType;
import uk.ac.ebi.uniprot.xml.jaxb.proteome.Proteome;

/**
 *
 * @author jluo
 * @date: 2 May 2019
 *
 */

public class UniProtKBProteomeWriter implements ItemWriter<Proteome> {
	private static final String GC_SET_ACC = "GCSetAcc";
	private final SolrClient solrClient;
	private final SolrCollection collection;
	private final TaxonomyRepo taxonomyRepo;

	public UniProtKBProteomeWriter(SolrClient solrClient, SolrCollection collection, TaxonomyRepo taxonomyRepo) {
		this.solrClient = solrClient;
		this.collection = collection;
		this.taxonomyRepo = taxonomyRepo;
	}

	@Override
	public void write(List<? extends Proteome> items) throws Exception {
		for (Proteome item : items) {
			write(item);
		}
		this.solrClient.commit(collection.name());
	}

	private void write(Proteome item) throws Exception {
		Optional<String> genomeAssemblyId = fetchGenomeAssemblyId(item);
		List<String> content =  fetchContent(item);
		List<ComponentType> components = item.getComponent();
		for (ComponentType component : components) {
			List<String> genomeAccessions = component.getGenomeAccession();
			List<ProteinType> proteins = component.getProtein();
		//	List<SolrInputDocument> documents =
			proteins.stream().forEach(protein -> addToSolr(protein, genomeAssemblyId, genomeAccessions, content));
		//	.forEach(action);
			//		.collect(Collectors.toList());
		//	this.solrClient.add(collection.name(), documents);
		}
	//	this.solrTemplate.softCommit(collection.name());
	}

	private  void addToSolr(ProteinType protein, Optional<String> genomeAssemblyId, List<String> genomeAccessions, List<String> content) {
		SolrInputDocument solrInputDocument = new SolrInputDocument();
		solrInputDocument.addField("accession_id", protein.getAccession());
	
		
		if (genomeAssemblyId.isPresent()) {
			Map<String, Object> fieldModifier = new HashMap<>(1);
			fieldModifier.put("set", genomeAssemblyId.get());
			solrInputDocument.addField("genome_assembly", fieldModifier);
		}
		if (!genomeAccessions.isEmpty()) {
			Map<String, Object> fieldModifier = new HashMap<>(1);
			fieldModifier.put("set", genomeAccessions);
			solrInputDocument.addField("genome_accession", fieldModifier);
		}
		if(!content.isEmpty()) {
			Map<String, Object> fieldModifier = new HashMap<>(1);
			fieldModifier.put("set", content);
			solrInputDocument.addField("proteome_content", fieldModifier);
		}
		try {
		this.solrClient.add(collection.name(), Arrays.asList(solrInputDocument));
		this.solrClient.commit(collection.name());
		}catch(Exception e) {
			
		}
	}

	
	private SolrInputDocument  convert(ProteinType protein, Optional<String> genomeAssemblyId, List<String> genomeAccessions, List<String> content) {
		SolrInputDocument solrInputDocument = new SolrInputDocument();
		solrInputDocument.addField("accession_id", protein.getAccession());
	
		
		if (genomeAssemblyId.isPresent()) {
			Map<String, Object> fieldModifier = new HashMap<>(1);
			fieldModifier.put("set", genomeAssemblyId.get());
			solrInputDocument.addField("genome_assembly", fieldModifier);
		}
//		if (!genomeAccessions.isEmpty()) {
//			Map<String, Object> fieldModifier = new HashMap<>(1);
//			fieldModifier.put("set", genomeAccessions);
//			solrInputDocument.addField("genome_accession", fieldModifier);
//		}
//		if(!content.isEmpty()) {
//			Map<String, Object> fieldModifier = new HashMap<>(1);
//			fieldModifier.put("set", content);
//			solrInputDocument.addField("proteome_content", fieldModifier);
//		}
		return solrInputDocument;
	//	this.solrClient.add(collection.name(), Arrays.asList(solrInputDocument));
	}

	private Optional<String> fetchGenomeAssemblyId(Proteome source) {
		return source.getDbReference().stream().filter(val -> val.getType().equals(GC_SET_ACC)).map(val -> val.getId())
				.findFirst();
	}
	
	private List<String> fetchContent(Proteome proteome){
		 List<String>  content = new ArrayList<>();
		 content.add(proteome.getUpid());
		 content.add(proteome.getDescription());
		 Optional<TaxonomicNode> taxonomicNode = taxonomyRepo.retrieveNodeUsingTaxID(proteome.getTaxonomy().intValue());
		if (taxonomicNode.isPresent()) {
			TaxonomicNode node = taxonomicNode.get();
			content.addAll(extractTaxonode(node));
			taxonomicNode = getParentTaxon(node.id());
			 while (taxonomicNode.isPresent()) {
	                TaxonomicNode parent = taxonomicNode.get();
	                content.addAll(extractTaxonode(parent));
	                taxonomicNode = getParentTaxon(parent.id());
	            }
		}
		
		 return content;
	}

	 private Optional<TaxonomicNode> getParentTaxon(int taxId) {
	        Optional<TaxonomicNode> optionalNode = taxonomyRepo.retrieveNodeUsingTaxID(taxId);
	        return optionalNode.filter(TaxonomicNode::hasParent).map(TaxonomicNode::parent);
	    }
	private List<String> extractTaxonode(TaxonomicNode node) {
		List<String> taxonmyItems = new ArrayList<>();
		if(!Strings.isNullOrEmpty(node.scientificName())) {
			taxonmyItems.add(node.scientificName());
		}
		if(!Strings.isNullOrEmpty(node.commonName())) {
			taxonmyItems.add(node.commonName());
		}
		if(!Strings.isNullOrEmpty(node.synonymName())) {
			taxonmyItems.add(node.synonymName());
		}
		if(!Strings.isNullOrEmpty(node.mnemonic())) {
			taxonmyItems.add(node.mnemonic());
		}
		return taxonmyItems;
	}
}
