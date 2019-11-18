package org.uniprot.store.search.domain2;

import java.util.Optional;

/**
 * Created 12/11/2019
 *
 * @author Edd
 */
public interface SearchField {
    String getTerm();

    SearchFieldType getType();

    default Optional<String> getSortTerm() {
        return Optional.empty();
    }

    default Optional<String> getValidRegex() {
        return Optional.empty();
    }
}