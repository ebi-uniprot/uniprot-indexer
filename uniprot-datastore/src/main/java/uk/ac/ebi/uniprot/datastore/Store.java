package uk.ac.ebi.uniprot.datastore;

import java.util.Collection;

/**
 * Created 24/07/19
 *
 * @author Edd
 */
@FunctionalInterface
public interface Store {
    void save(Collection<?> items);
}