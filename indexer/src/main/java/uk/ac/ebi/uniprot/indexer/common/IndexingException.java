package uk.ac.ebi.uniprot.indexer.common;

/**
 * A generic exception which can be thrown during the process of indexing data into one of the UniProt data sources.
 * <p/>
 * Use this exception if the situation that requires it is unrecoverable.
 * <p/>
 * Subclass from this class when the error is recoverable.
 *
 * @author Ricardo Antunes
 */
public class IndexingException extends RuntimeException {
    public IndexingException(String message) {
        super(message);
    }

    public IndexingException(String message, Throwable cause) {
        super(message, cause);
    }
}