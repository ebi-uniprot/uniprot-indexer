package org.uniprot.store.indexer.publication.computational;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.batch.item.ItemProcessor;
import org.uniprot.core.json.parser.publication.CommunityMappedReferenceJsonConfig;
import org.uniprot.core.publication.CommunityMappedReference;
import org.uniprot.core.publication.ComputationallyMappedReference;
import org.uniprot.store.search.document.publication.PublicationDocument;

import java.nio.ByteBuffer;

import static org.uniprot.core.publication.MappedReferenceType.COMMUNITY;
import static org.uniprot.core.publication.MappedReferenceType.COMPUTATIONAL;

public class ComputationalPublicationProcessor
        implements ItemProcessor<ComputationallyMappedReference, PublicationDocument> {
    static final String ID_COMPONENT_SEPARATOR = "__";
    private final ObjectMapper objectMapper;

    public ComputationalPublicationProcessor() {
        this.objectMapper = CommunityMappedReferenceJsonConfig.getInstance().getFullObjectMapper();
    }

    @Override
    public PublicationDocument process(ComputationallyMappedReference reference) {
        PublicationDocument.PublicationDocumentBuilder builder = PublicationDocument.builder();

        builder.pubMedId(reference.getPubMedId())
                .accession(reference.getUniProtKBAccession().getValue())
                .id(computeId(reference))
                .type(COMPUTATIONAL.getIntValue())
                .publicationMappedReference(ByteBuffer.wrap(getObjectBinary(reference)));

        return builder.build();
    }

    String computeId(ComputationallyMappedReference reference) {
        return reference.getUniProtKBAccession().getValue()
                + ID_COMPONENT_SEPARATOR
                + reference.getPubMedId()
                + ID_COMPONENT_SEPARATOR
                + COMPUTATIONAL.getIntValue();
    }

    private byte[] getObjectBinary(ComputationallyMappedReference reference) {
        try {
            return this.objectMapper.writeValueAsBytes(reference);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(
                    "Unable to parse CommunityMappedReference to binary json: ", e);
        }
    }
}
