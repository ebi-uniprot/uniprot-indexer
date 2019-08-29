package org.uniprot.store.indexer.uniprotkb.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.uniprot.core.cv.xdb.UniProtXDbTypes;
import org.uniprot.core.uniprot.comment.CommentType;
import org.uniprot.core.uniprot.feature.FeatureCategory;
import org.uniprot.store.search.document.suggest.SuggestDictionary;
import org.uniprot.store.search.document.suggest.SuggestDocument;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created 16/05/19
 *
 * @author Edd
 */
@Configuration
public class SuggestionConfig {
    private static final String DEFAULT_TAXON_SYNONYMS_FILE = "default-taxon-synonyms.txt";
    private static final String COMMENT_LINE_PREFIX = "#";
    static final String DATABASE_PREFIX = "Database: ";
    static final String FEATURE_CATEGORY_PREFIX = "Feature Category: ";
    static final String COMMENT_TYPE_PREFIX = "Comment type: ";

    @Bean
    public Map<String, SuggestDocument> suggestDocuments() {
        Map<String, SuggestDocument> suggestionMap = new HashMap<>();

        loadDefaultMainSuggestions().forEach(suggestion -> suggestionMap.put(suggestion.value, suggestion));
        loadDefaultTaxonSynonymSuggestions().forEach(suggestion -> suggestionMap
                .put(SuggestDictionary.TAXONOMY.name() + ":" + suggestion.id, suggestion));

        return suggestionMap;
    }

    private List<SuggestDocument> loadDefaultMainSuggestions() {
        List<SuggestDocument> defaultSuggestions = new ArrayList<>();

        defaultSuggestions.addAll(enumToSuggestions(new FeatureCategoryToSuggestion()));
        defaultSuggestions.addAll(enumToSuggestions(new CommentTypeToSuggestion()));
        defaultSuggestions.addAll(databaseSuggestions());

        return defaultSuggestions;
    }

    private List<SuggestDocument> loadDefaultTaxonSynonymSuggestions() {
        List<SuggestDocument> taxonSuggestions = new ArrayList<>();
        InputStream inputStream = SuggestionConfig.class.getClassLoader()
                .getResourceAsStream(DEFAULT_TAXON_SYNONYMS_FILE);
        if (inputStream != null) {
            try (Stream<String> lines = new BufferedReader(new InputStreamReader(inputStream)).lines()) {
                lines.map(this::createDefaultTaxonomySuggestion)
                        .filter(Objects::nonNull)
                        .forEach(taxonSuggestions::add);
            }
        }

        return taxonSuggestions;
    }

    private SuggestDocument createDefaultTaxonomySuggestion(String csvLine) {
        String[] lineParts = csvLine.split("\t");
        if (!csvLine.startsWith(COMMENT_LINE_PREFIX) && lineParts.length == 4) {
            return SuggestDocument.builder()
                    .value(lineParts[0])
                    .altValues(Stream.of(lineParts[2].split(",")).collect(Collectors.toList()))
                    .id(lineParts[1])
                    .importance(lineParts[3])
                    .dictionary(SuggestDictionary.TAXONOMY.name())
                    .build();
        } else {
            return null;
        }
    }

    private static List<SuggestDocument> databaseSuggestions() {
        return UniProtXDbTypes.INSTANCE.getAllDBXRefTypes().stream()
                .map(type -> {
                    String name = removeTerminalSemiColon(type.getDisplayName());
                    return SuggestDocument.builder()
                            .value(DATABASE_PREFIX + name)
                            .dictionary(SuggestDictionary.MAIN.name())
                            .build();
                })
                .collect(Collectors.toList());
    }

    static String removeTerminalSemiColon(String displayName) {
        int charIndex = displayName.indexOf(';');
        if (charIndex < 0) {
            return displayName;
        } else {
            return displayName.substring(0, charIndex);
        }
    }

    private <T extends Enum<T>> List<SuggestDocument> enumToSuggestions(EnumSuggestionFunction<T> typeToSuggestion) {
        return Stream.of(typeToSuggestion.getEnumType().getEnumConstants())
                .map(typeToSuggestion)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    interface EnumSuggestionFunction<T> extends Function<T, Optional<SuggestDocument>> {
        Class<T> getEnumType();
    }

    static class FeatureCategoryToSuggestion implements EnumSuggestionFunction<FeatureCategory> {
        @Override
        public Optional<SuggestDocument> apply(FeatureCategory value) {
            String name = value.name();
            return Optional.of(SuggestDocument.builder()
                                       .value(FEATURE_CATEGORY_PREFIX + name)
                                       .dictionary(SuggestDictionary.MAIN.name())
                                       .build());
        }

        @Override
        public Class<FeatureCategory> getEnumType() {
            return FeatureCategory.class;
        }
    }

    static class CommentTypeToSuggestion implements EnumSuggestionFunction<CommentType> {
        @Override
        public Optional<SuggestDocument> apply(CommentType value) {
            String name = value.toXmlDisplayName();
            return value == CommentType.UNKNOWN ?
                    Optional.empty() :
                    Optional.of(SuggestDocument.builder()
                                        .value(COMMENT_TYPE_PREFIX + name)
                                        .dictionary(SuggestDictionary.MAIN.name())
                                        .build());
        }

        @Override
        public Class<CommentType> getEnumType() {
            return CommentType.class;
        }
    }
}