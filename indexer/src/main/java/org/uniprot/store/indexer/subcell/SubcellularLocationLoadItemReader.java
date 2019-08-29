package org.uniprot.store.indexer.subcell;

import org.springframework.batch.item.ItemReader;
import org.uniprot.core.cv.impl.SubcellularLocationFileReader;
import org.uniprot.core.cv.subcell.SubcellularLocationEntry;

import java.io.IOException;
import java.util.Iterator;

/**
 * @author lgonzales
 * @since 2019-07-11
 */
public class SubcellularLocationLoadItemReader implements ItemReader<SubcellularLocationEntry> {
    private Iterator<SubcellularLocationEntry> subcellularLocationIterator;

    public SubcellularLocationLoadItemReader(String filePath) throws IOException {
        SubcellularLocationFileReader subcellularLocationFileReader = new SubcellularLocationFileReader();
        this.subcellularLocationIterator = subcellularLocationFileReader.parse(filePath).iterator();
    }

    @Override
    public SubcellularLocationEntry read() {
        if (this.subcellularLocationIterator.hasNext()) {
            return this.subcellularLocationIterator.next();
        }
        return null;
    }

}