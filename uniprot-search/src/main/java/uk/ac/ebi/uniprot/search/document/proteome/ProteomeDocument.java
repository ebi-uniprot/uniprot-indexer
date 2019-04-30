package uk.ac.ebi.uniprot.search.document.proteome;



import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.solr.client.solrj.beans.Field;

import uk.ac.ebi.uniprot.search.document.Document;


public class ProteomeDocument  implements Document {
    
    @Field("upid")
    public String upid;

    @Field("organism_name")
    public List<String> organismName = new ArrayList<>();
    @Field("organism_sort")
    public String organismSort;
    
    @Field("organism_id")
    public int organismTaxId;
    
    @Field("taxonomy_name")
    public List<String> organismTaxon = new ArrayList<>();

    @Field("taxonomy_id")
    public List<Integer> taxLineageIds = new ArrayList<>();
    
    @Field("reference")
    public boolean isReferenceProteome;
    
    @Field("redundant")
    public boolean isRedundant;
    
    @Field("superkingdom")
    public String superkingdom;
    
    @Field("genome_accession")
    public List<String> genomeAccession = new ArrayList<>();
    
    @Field("genome_assembly")
    public List<String> genomeAssembly = new ArrayList<>();

    @Field("accession")
    public List<String> accession = new ArrayList<>();

    @Field("gene")
    public List<String> gene = new ArrayList<>();
    
    //DEFAULT SEARCH FIELD
    @Field("content")
    public Set<String> content = new HashSet<>();

    @Field("proteome_stored")
    public ByteBuffer proteomeStored;
    
    @Field("annotation_score")
    public int score;
    @Field("proteome_type")
    public int proteomeType;  //reference=1, representative =2, complete=3, redundant=4
    
}