package org.uniprot.store.spark.indexer.publication.mapper;

import org.apache.spark.api.java.function.Function;
import org.uniprot.core.publication.MappedReference;
import org.uniprot.store.reader.publications.ComputationallyMappedReferenceConverter;

/**
 * Created 19/01/2021
 *
 * @author Edd
 */
public class SparkComputationallyMappedReferenceConverter
        implements Function<String, MappedReference> {
    @Override
    public MappedReference call(String rawReference) throws Exception {
        ComputationallyMappedReferenceConverter converter =
                new ComputationallyMappedReferenceConverter();
        return converter.convert(rawReference);
    }
}