package indexer.go.evidence;

import lombok.extern.slf4j.Slf4j;
import org.apache.spark.api.java.function.PairFunction;
import org.uniprot.core.uniprot.evidence.Evidence;
import org.uniprot.core.uniprot.evidence.impl.EvidenceHelper;
import scala.Tuple2;

/**
 * @author lgonzales
 * @since 2019-11-14
 */
@Slf4j
public class GoEvidencesFileMapper implements PairFunction<String, String, GoEvidence> {

    private static final long serialVersionUID = 7265825845507683822L;

    @Override
    public Tuple2<String, GoEvidence> call(String line) throws Exception {
        String[] splitedLine = line.split("\t");
        if (splitedLine.length >= 7) {
            String accession = splitedLine[0];
            String goId = splitedLine[1];
            String evidenceValue = splitedLine[6].replace("PMID", "ECO:0000269|PubMed");
            Evidence evidence = EvidenceHelper.parseEvidenceLine(evidenceValue);
            return new Tuple2<>(accession, new GoEvidence(goId, evidence));
        } else {
            log.warn("unable to parse line: '" + line + "' in go evidence file");
        }
        return null;
    }
}