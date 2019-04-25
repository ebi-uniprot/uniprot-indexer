package uk.ac.ebi.uniprot.search.document.disease;

import lombok.Builder;
import lombok.Getter;
import org.apache.solr.client.solrj.beans.Field;
import uk.ac.ebi.uniprot.search.document.Document;

import java.nio.ByteBuffer;
import java.util.List;
@Getter
@Builder
public class DiseaseDocument implements Document {
    @Field
    private String accession;
    @Field
    private List<String> name; // search by name
    @Field
    private List<String> content; // default search field
    @Field("disease_obj")
    private ByteBuffer diseaseObj;
}
