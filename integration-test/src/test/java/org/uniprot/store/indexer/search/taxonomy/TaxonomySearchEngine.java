package org.uniprot.store.indexer.search.taxonomy;

import org.uniprot.store.config.searchfield.common.SearchFieldConfig;
import org.uniprot.store.config.searchfield.factory.SearchFieldConfigFactory;
import org.uniprot.store.config.searchfield.factory.UniProtDataType;
import org.uniprot.store.indexer.search.AbstractSearchEngine;
import org.uniprot.store.search.document.taxonomy.TaxonomyDocument;

class TaxonomySearchEngine extends AbstractSearchEngine<TaxonomyDocument> {
    private static final String SEARCH_ENGINE_NAME = "taxonomy";

    TaxonomySearchEngine() {
        super(SEARCH_ENGINE_NAME, identityConverter -> identityConverter);
    }

    @Override
    protected SearchFieldConfig getSearchFieldConfig() {
        return SearchFieldConfigFactory.getSearchFieldConfig(UniProtDataType.taxonomy);
    }

    @Override
    protected String identifierField() {
        return getSearchFieldConfig().getSearchFieldItemByName("id").getFieldName();
    }

    @Override
    protected String identifierQuery(String entryId) {
        return "("
                + getSearchFieldConfig().getSearchFieldItemByName("id").getFieldName()
                + ":\""
                + entryId
                + "\")";
    }
}
