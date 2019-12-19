package indexer.uniprot.mapper;

import java.util.Collection;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.apache.spark.api.java.Optional;
import org.apache.spark.api.java.function.Function;
import org.uniprot.store.search.document.uniprot.UniProtDocument;

import scala.Tuple2;
import indexer.go.relations.GoTerm;

/**
 * @author lgonzales
 * @since 2019-11-12
 */
@Slf4j
public class GoRelationsToUniProtDocument
        implements Function<Tuple2<UniProtDocument, Optional<Iterable<GoTerm>>>, UniProtDocument> {
    private static final long serialVersionUID = -5057000958468900711L;

    @Override
    public UniProtDocument call(Tuple2<UniProtDocument, Optional<Iterable<GoTerm>>> tuple)
            throws Exception {
        UniProtDocument doc = tuple._1;
        if (tuple._2.isPresent()) {
            tuple._2
                    .get()
                    .forEach(
                            goTerm -> {
                                String parentId = goTerm.getId().substring(3).trim();
                                String evidenceMapKey =
                                        getGoWithEvidenceMapsKey(parentId, doc.goWithEvidenceMaps);
                                Collection<String> goMapValues =
                                        doc.goWithEvidenceMaps.get(evidenceMapKey);
                                if (goTerm.getAncestors() != null) {
                                    goTerm.getAncestors()
                                            .forEach(
                                                    ancestor -> {
                                                        String idOnly =
                                                                ancestor.getId()
                                                                        .substring(3)
                                                                        .trim();
                                                        doc.goIds.add(idOnly);
                                                        doc.goes.add(idOnly);
                                                        doc.goes.add(ancestor.getName());

                                                        if (goMapValues != null) {
                                                            goMapValues.add(idOnly);
                                                            goMapValues.add(ancestor.getName());
                                                        } else {
                                                            log.warn(
                                                                    "Unable to find Go With Evidence Type Map Key"
                                                                            + " for accession"
                                                                            + doc.accession
                                                                            + " and go term "
                                                                            + parentId);
                                                        }
                                                    });
                                }
                            });
        }
        return doc;
    }

    private String getGoWithEvidenceMapsKey(
            String idOnly, Map<String, Collection<String>> goWithEvidenceMaps) {
        String result = "";
        for (Map.Entry<String, Collection<String>> mapEntry : goWithEvidenceMaps.entrySet()) {
            if (mapEntry.getValue().contains(idOnly)) {
                result = mapEntry.getKey();
                break;
            }
        }
        return result;
    }
}