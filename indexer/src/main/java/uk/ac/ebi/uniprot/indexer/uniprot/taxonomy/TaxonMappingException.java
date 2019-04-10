package uk.ac.ebi.uniprot.indexer.uniprot.taxonomy;

/**
 * Exception that will occur when there is a problem with the retrieval of a taxonomic node
 */
public class TaxonMappingException extends RuntimeException {
    public TaxonMappingException(String message) {
        super(message);
    }
    public TaxonMappingException(String message, Throwable cause) {
        super(message, cause);
    }
}