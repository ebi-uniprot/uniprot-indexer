package uk.ac.ebi.uniprot.indexer.uniprotkb;

import uk.ac.ebi.uniprot.domain.uniprot.UniProtEntry;
import uk.ac.ebi.uniprot.indexer.document.uniprot.UniProtDocument;

import java.util.Objects;

/**
 * Created 12/04/19
 *
 * @author Edd
 */
public class ConvertableEntry {
    private final UniProtEntry entry;
    private UniProtDocument document;

    private ConvertableEntry(UniProtEntry entry) {
        this.entry = entry;
    }

    public static ConvertableEntry createConvertableEntry(UniProtEntry entry) {
        return new ConvertableEntry(entry);
    }

    public void convertsTo(UniProtDocument document) {
        this.document = document;
    }

    public UniProtEntry getEntry() {
        return entry;
    }

    public UniProtDocument getDocument() {
        return document;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConvertableEntry that = (ConvertableEntry) o;
        return Objects.equals(entry, that.entry) &&
                Objects.equals(document, that.document);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entry, document);
    }
}
