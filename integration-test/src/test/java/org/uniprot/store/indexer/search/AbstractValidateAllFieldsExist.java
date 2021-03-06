package org.uniprot.store.indexer.search;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.uniprot.store.config.searchfield.common.SearchFieldConfig;
import org.uniprot.store.config.searchfield.model.SearchFieldItem;

/**
 * This class tests that all search fields known about via, {@link SearchFieldConfig}, can be
 * queried against. No special test is performed on the response other than checking that the
 * request worked. If the field did not exist, then a test will fail and developers will be alerted,
 * and the file that acts as source for {@link SearchFieldConfig}, {@code
 * <UniProtDataType>-search-fields.json} will need correcting.
 *
 * <p>For example if {@code uniprotkb-search-fields.json} contains field 'xxx', but the Solr schema
 * defines a field called, 'xx', then a test will fail that reports the Solr error stating that
 * field 'xxx' does not exist.
 *
 * <p>Note that dynamic fields in Solr have slightly different behaviour. Dynamic fields in a Solr
 * schema, e.g., ft_*, allow queries upon any field starting with ft_. Created 18/11/2019
 *
 * @author Edd
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractValidateAllFieldsExist<T> {

    protected abstract AbstractSearchEngine<T> getSearchEngine();

    protected SearchFieldConfig getSearchFieldConfig() {
        return getSearchEngine().getSearchFieldConfig();
    }

    private Stream<Arguments> provideSearchFields() {
        return Stream.concat(
                        getSearchFieldConfig().getSortFieldItems().stream(),
                        getSearchFieldConfig().getSearchFieldItems().stream())
                .map(SearchFieldItem::getFieldName)
                .map(Arguments::of);
    }

    @ParameterizedTest(name = "{0}:*")
    @MethodSource("provideSearchFields")
    void searchFieldIsKnownToSearchEngine(String searchField) {
        QueryResponse queryResponse =
                getSearchEngine().getQueryResponse("select", searchField + ":*");
        assertThat(queryResponse, is(notNullValue()));
    }

    @Test
    void unknownFieldCausesException() {
        assertThrows(
                SolrException.class,
                () ->
                        getSearchEngine()
                                .getQueryResponse(
                                        "select", "DELIBERATE-RUBBISH-FIELD-NO-NO-NO-NO:*"));
    }
}
