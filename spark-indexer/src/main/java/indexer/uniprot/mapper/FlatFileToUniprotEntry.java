package indexer.uniprot.mapper;

import lombok.extern.slf4j.Slf4j;

import org.apache.spark.api.java.function.PairFunction;
import org.uniprot.core.flatfile.parser.SupportingDataMap;
import org.uniprot.core.flatfile.parser.UniprotLineParser;
import org.uniprot.core.flatfile.parser.impl.DefaultUniprotLineParserFactory;
import org.uniprot.core.flatfile.parser.impl.entry.EntryObject;
import org.uniprot.core.flatfile.parser.impl.entry.EntryObjectConverter;
import org.uniprot.core.uniprot.UniProtEntry;

import scala.Serializable;
import scala.Tuple2;

/**
 * @author lgonzales
 * @since 2019-11-12
 */
@Slf4j
public class FlatFileToUniprotEntry
        implements PairFunction<String, String, UniProtEntry>, Serializable {
    private static final long serialVersionUID = 8571366803867491177L;

    private final SupportingDataMap supportingDataMap;

    public FlatFileToUniprotEntry(SupportingDataMap supportingDataMap) {
        this.supportingDataMap = supportingDataMap;
    }

    @Override
    public Tuple2<String, UniProtEntry> call(String entryString) throws Exception {
        UniprotLineParser<EntryObject> entryParser =
                new DefaultUniprotLineParserFactory().createEntryParser();
        EntryObjectConverter entryObjectConverter =
                new EntryObjectConverter(supportingDataMap, true); // TODO: change to false

        EntryObject parsed = entryParser.parse(entryString);
        UniProtEntry uniProtEntry = entryObjectConverter.convert(parsed);
        return new Tuple2<>(uniProtEntry.getPrimaryAccession().getValue(), uniProtEntry);
    }
}