package uk.ac.ebi.uniprot.indexer.crossref;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.ac.ebi.uniprot.indexer.crossref.readers.CrossRefReader;
import uk.ac.ebi.uniprot.search.document.dbxref.CrossRefDocument;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CrossRefReaderTest {
    private static final String DBXREF_PATH = "target/test-classes/crossref/test-dbxref.txt";
    private static CrossRefReader reader;

    @BeforeAll
    static void setReader() throws IOException {
        reader = new CrossRefReader(DBXREF_PATH);
    }

    @Test
    void testReadFile() {
        CrossRefDocument dbxRef = reader.read();
        assertNotNull(dbxRef, "Unable to read the dbxref file");
        verifyDBXRef(dbxRef);
        int count = 1;
        while (reader.read() != null) {
            count++;
        }

        assertEquals(count, 5);
    }

    private void verifyDBXRef(CrossRefDocument dbxRef) {
        assertNotNull(dbxRef.getAccession(), "Accession is null");
        assertNotNull(dbxRef.getAbbrev(), "Abbrev is null");
        assertNotNull(dbxRef.getName(), "Name is null");
        assertNotNull(dbxRef.getPubMedId(), "PUBMED ID is null");
        assertNotNull(dbxRef.getDoiId(), "DOI Id is null");
        assertNotNull(dbxRef.getLinkType(), "Link Type is null");
        assertNotNull(dbxRef.getServer(), "Server is null");
        assertNotNull(dbxRef.getDbUrl(), "DB URL is null");
        assertNotNull(dbxRef.getCategoryStr(), "Category is null");
    }
}