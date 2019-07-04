package uk.ac.ebi.uniprot.indexer.keyword;

import org.springframework.batch.item.ItemReader;
import uk.ac.ebi.uniprot.cv.impl.KeywordFileReader;
import uk.ac.ebi.uniprot.cv.keyword.KeywordEntry;

import java.io.IOException;
import java.util.Iterator;

/**
 * @author lgonzales
 */
public class KeywordLoadItemReader implements ItemReader<KeywordEntry> {
    private Iterator<KeywordEntry> keywordIterator;

    public KeywordLoadItemReader(String filePath) throws IOException {
        KeywordFileReader keywordFileReader = new KeywordFileReader();
        this.keywordIterator = keywordFileReader.parse(filePath).iterator();
    }

    @Override
    public KeywordEntry read() {
        if (this.keywordIterator.hasNext()) {
            return this.keywordIterator.next();
        }
        return null;
    }

}