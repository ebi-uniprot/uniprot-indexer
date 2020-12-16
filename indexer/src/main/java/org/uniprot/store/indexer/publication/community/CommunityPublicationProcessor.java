package org.uniprot.store.indexer.publication.community;

import static org.uniprot.core.publication.MappedReferenceType.COMMUNITY;

import org.springframework.batch.item.ItemProcessor;
import org.uniprot.core.json.parser.publication.CommunityMappedReferenceJsonConfig;
import org.uniprot.core.publication.CommunityMappedReference;
import org.uniprot.store.search.document.publication.PublicationDocument;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CommunityPublicationProcessor
        implements ItemProcessor<CommunityMappedReference, PublicationDocument> {
    static final String ID_COMPONENT_SEPARATOR = "__";
    private final ObjectMapper objectMapper;

    public CommunityPublicationProcessor() {
        this.objectMapper = CommunityMappedReferenceJsonConfig.getInstance().getFullObjectMapper();
    }

    @Override
    public PublicationDocument process(CommunityMappedReference reference) {
        PublicationDocument.PublicationDocumentBuilder builder = PublicationDocument.builder();

        builder.pubMedId(reference.getPubMedId())
                .accession(reference.getUniProtKBAccession().getValue())
                .id(computeId(reference))
                .type(COMMUNITY.getIntValue())
                .publicationMappedReference(getObjectBinary(reference));

        return builder.build();
    }

    String computeId(CommunityMappedReference reference) {
        return reference.getUniProtKBAccession().getValue()
                + ID_COMPONENT_SEPARATOR
                + reference.getPubMedId()
                + ID_COMPONENT_SEPARATOR
                + reference.getSource().getId();
    }

    private byte[] getObjectBinary(CommunityMappedReference reference) {
        try {
            return this.objectMapper.writeValueAsBytes(reference);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(
                    "Unable to parse CommunityMappedReference to binary json: ", e);
        }
    }
}
