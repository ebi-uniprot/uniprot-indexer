package org.uniprot.store.search.domain2;

import java.util.List;

/**
 * Created 12/11/2019
 *
 * @author Edd
 */
public interface SearchItem {
    String getId();

    String getLabel();

    String getTerm();

    String getTermValidRegex();

    String getNumberTerm();

    String getNumberValidRegex();

    String getSortTerm();

    String getAutoComplete();

    String getDataType();

    String getRangeTerm();

    String getEvTerm();

    String getDescription();

    String getSites();

    String getExample();

    String getItemType();

    List<SearchItem> getItems();
}