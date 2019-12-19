package indexer.uniprot.converter;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.uniprot.core.uniprot.feature.Feature;
import org.uniprot.core.uniprot.feature.FeatureType;
import org.uniprot.store.search.document.uniprot.UniProtDocument;

/**
 * @author lgonzales
 * @since 2019-09-04
 */
class UniProtEntryFeatureConverter {

    private static final String FEATURE = "ft_";
    private static final String FT_EV = "ftev_";
    private static final String FT_LENGTH = "ftlen_";

    void convertFeature(List<Feature> features, UniProtDocument document) {
        for (Feature feature : features) {
            String field = getFeatureField(feature, FEATURE);
            String evField = getFeatureField(feature, FT_EV);
            String lengthField = getFeatureField(feature, FT_LENGTH);
            Collection<String> featuresOfTypeList =
                    document.featuresMap.computeIfAbsent(field, k -> new HashSet<>());

            featuresOfTypeList.add(feature.getType().getName());
            document.content.add(feature.getType().getName());

            if (feature.hasFeatureId()) {
                featuresOfTypeList.add(feature.getFeatureId().getValue());
                document.content.add(feature.getFeatureId().getValue());
            }
            if (feature.hasDescription()) {
                featuresOfTypeList.add(feature.getDescription().getValue());
                document.content.add(feature.getDescription().getValue());
            }
            if (feature.hasDbXref()) {
                String xrefId = feature.getDbXref().getId();
                String dbName = feature.getDbXref().getDatabaseType().getName();
                featuresOfTypeList.addAll(UniProtEntryConverterUtil.getXrefId(xrefId, dbName));
                document.content.addAll(UniProtEntryConverterUtil.getXrefId(xrefId, dbName));
            }
            document.proteinsWith.add(feature.getType().name().toLowerCase());

            // start and end of location
            int length =
                    feature.getLocation().getEnd().getValue()
                            - feature.getLocation().getStart().getValue()
                            + 1;
            Set<String> evidences =
                    UniProtEntryConverterUtil.extractEvidence(feature.getEvidences());
            Collection<Integer> lengthList =
                    document.featureLengthMap.computeIfAbsent(lengthField, k -> new HashSet<>());
            lengthList.add(length);

            Collection<String> evidenceList =
                    document.featureEvidenceMap.computeIfAbsent(evField, k -> new HashSet<>());
            evidenceList.addAll(evidences);
        }
        document.proteinsWith.removeIf(this::filterUnnecessaryProteinsWithFeatureTypes);
    }

    private String getFeatureField(Feature feature, String type) {
        String field = type + feature.getType().name().toLowerCase();
        return field.replaceAll(" ", "_");
    }

    private boolean filterUnnecessaryProteinsWithFeatureTypes(String featureType) {
        return featureType.equalsIgnoreCase(FeatureType.SITE.toString())
                || featureType.equalsIgnoreCase(FeatureType.UNSURE.toString())
                || featureType.equalsIgnoreCase(FeatureType.CONFLICT.toString())
                || featureType.equalsIgnoreCase(FeatureType.NON_CONS.toString())
                || featureType.equalsIgnoreCase(FeatureType.NON_TER.toString());
    }
}