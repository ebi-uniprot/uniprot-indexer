package org.uniprot.store.spark.indexer.uniprot.mapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.uniprot.core.flatfile.parser.UniprotKBLineParser;
import org.uniprot.core.flatfile.parser.impl.DefaultUniprotKBLineParserFactory;
import org.uniprot.core.flatfile.parser.impl.ac.AcLineObject;
import org.uniprot.core.flatfile.parser.impl.dr.DrLineObject;
import org.uniprot.core.util.Utils;

import scala.Tuple2;

/**
 * Map from entry in FF String format To Iterator of Tuple2{key=goId, value=accession} extracted
 * from DR lines
 *
 * @author lgonzales
 * @since 2019-11-12
 */
public class GoRelationsJoinMapper implements PairFlatMapFunction<String, String, String> {
    private static final long serialVersionUID = -2452907832200117358L;

    /**
     * @param entryStr flat file entry in String format
     * @return Iterator of Tuple2{key=goId, value=accession} extracted from DR lines
     */
    @Override
    public Iterator<Tuple2<String, String>> call(String entryStr) throws Exception {
        final UniprotKBLineParser<AcLineObject> acParser =
                new DefaultUniprotKBLineParserFactory().createAcLineParser();
        List<Tuple2<String, String>> goTuple = new ArrayList<>();

        List<String> goLines =
                Arrays.stream(entryStr.split("\n"))
                        .filter(line -> line.startsWith("DR   GO;") || line.startsWith("AC   "))
                        .collect(Collectors.toList());

        String acLine =
                goLines.stream()
                        .filter(line -> line.startsWith("AC  "))
                        .collect(Collectors.joining("\n"));
        String accession = acParser.parse(acLine + "\n").primaryAcc;
        String drLine =
                goLines.stream()
                        .filter(line -> line.startsWith("DR   GO;"))
                        .collect(Collectors.joining("\n"));
        if (Utils.notNullNotEmpty(drLine)) {
            final UniprotKBLineParser<DrLineObject> drParser =
                    new DefaultUniprotKBLineParserFactory().createDrLineParser();
            DrLineObject drLineObject = drParser.parse(drLine + "\n");
            drLineObject.drObjects.forEach(
                    drValue -> {
                        String goId = drValue.attributes.get(0).replace(" ", "");
                        goTuple.add(new Tuple2<String, String>(goId, accession));
                    });
        }

        return goTuple.iterator();
    }
}
