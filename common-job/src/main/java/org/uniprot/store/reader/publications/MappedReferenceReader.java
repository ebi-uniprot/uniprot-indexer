package org.uniprot.store.reader.publications;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.uniprot.core.publication.MappedReference;

/**
 * @author sahmad
 * @created 21/01/2021
 *     <p>This class is responsible for reading a list of entries with same accession and puibmed
 *     id.
 */
public class MappedReferenceReader<T extends MappedReference> {
    private final Iterator<String> lines;
    private T nextMappedRef;
    private MappedReferenceConverter<T> mappedReferenceConverter;

    public MappedReferenceReader(
            MappedReferenceConverter<T> mappedReferenceConverter, String filePath)
            throws IOException {
        this.lines = Files.lines(Paths.get(filePath)).iterator();
        this.mappedReferenceConverter = mappedReferenceConverter;
    }

    public List<T> readNext() {
        List<T> mappedReferences = null;
        T currentMappedRef = null;
        if (Objects.nonNull(this.nextMappedRef)) {
            currentMappedRef = this.nextMappedRef;
            mappedReferences = new ArrayList<>();
            mappedReferences.add(currentMappedRef);
            this.nextMappedRef = null;
        } else if (this.lines.hasNext()) {
            currentMappedRef = this.mappedReferenceConverter.convert(this.lines.next());
            mappedReferences = new ArrayList<>();
            mappedReferences.add(currentMappedRef);
        }

        while (this.lines.hasNext()) {
            this.nextMappedRef = this.mappedReferenceConverter.convert(this.lines.next());
            // keep adding to the list as long as accession-pubmed pair is same
            if (isAccessionPubMedIdPairEqual(currentMappedRef, this.nextMappedRef)) {
                currentMappedRef = this.nextMappedRef;
                mappedReferences.add(currentMappedRef);
            } else {
                break;
            }
        }
        return mappedReferences;
    }

    private boolean isAccessionPubMedIdPairEqual(T currentMappedRef, T nextMappedRef) {
        return currentMappedRef
                        .getUniProtKBAccession()
                        .getValue()
                        .equals(nextMappedRef.getUniProtKBAccession().getValue())
                && currentMappedRef.getPubMedId().equals(nextMappedRef.getPubMedId());
    }
}
