package org.uniprot.store.search.domain2;

import java.util.Set;

/**
 * Created 14/11/19
 *
 * @author Edd
 */
public interface SearchFields {
    default boolean hasField(String field) {
        return getSearchFields().stream()
                .map(SearchField::getName)
                .anyMatch(searchField -> searchField.equals(field));
    }

    default boolean hasSortField(String field) {
        return getSearchFields().stream()
                .filter(searchField -> searchField.getSortName().isPresent())
                .map(SearchField::getName)
                .anyMatch(searchField -> searchField.equals(field));
    }

    default String getField(String field) {
        return getSearchFields().stream()
                .filter(searchField -> searchField.getName().equals(field))
                .map(SearchField::getName)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown field: " + field));
    }

    default String getSortFieldFor(String field) {
        IllegalArgumentException exception = new IllegalArgumentException(
                "Field '"
                        + field
                        + "' does not have an associated sort field.");
        for (SearchField searchField : getSearchFields()) {
            if (searchField.getName().equals(field) && searchField.getSortName().isPresent()) {
                return searchField
                        .getSortName()
                        .orElseThrow(() -> exception);
            }
        }
        throw exception;
    }

    default boolean fieldValueIsValid(String field, String value) {
        for (SearchField searchField : getSearchFields()) {
            if (searchField.getName().equals(field)) {
                return searchField.getValidRegex().map(value::matches).orElse(true);
            }
        }
        throw new IllegalArgumentException("Field does not exist: " + field);
    }

    Set<SearchField> getSearchFields();

    Set<SearchField> getGeneralFields();

    Set<SearchField> getRangeFields();

    Set<String> getSorts();
}