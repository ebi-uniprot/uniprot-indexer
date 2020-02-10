package org.uniprot.store.indexer.uniprotkb.converter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.*;

import org.junit.jupiter.api.Test;
import org.uniprot.core.uniprot.description.*;
import org.uniprot.core.uniprot.description.builder.*;
import org.uniprot.core.uniprot.evidence.Evidence;
import org.uniprot.core.uniprot.evidence.EvidenceCode;
import org.uniprot.core.uniprot.evidence.builder.EvidenceBuilder;
import org.uniprot.cv.ec.ECBuilder;
import org.uniprot.cv.ec.ECRepo;
import org.uniprot.store.search.document.suggest.SuggestDocument;
import org.uniprot.store.search.document.uniprot.UniProtDocument;

/**
 * @author lgonzales
 * @since 2019-09-11
 */
class UniprotEntryProteinDescriptionConverterTest {

    @Test
    void convertCompleteProteinDescription() {
        UniProtDocument document = new UniProtDocument();
        Map<String, SuggestDocument> suggestions = new HashMap<>();

        ECRepo ecRepo = mock(ECRepo.class);
        when(ecRepo.getEC("1.2.3.3"))
                .thenReturn(Optional.of(new ECBuilder().label("Label 3").id("1.2.3.3").build()));
        when(ecRepo.getEC("1.2.3.4"))
                .thenReturn(Optional.of(new ECBuilder().label("Label 4").id("1.2.3.4").build()));
        when(ecRepo.getEC("1.2.3.5"))
                .thenReturn(Optional.of(new ECBuilder().label("Label 5").id("1.2.3.5").build()));

        ProteinDescription proteinDescription = getProteinDescription();

        UniprotEntryProteinDescriptionConverter converter =
                new UniprotEntryProteinDescriptionConverter(ecRepo, suggestions);
        converter.convertProteinDescription(proteinDescription, document);

        List<String> indexedNames =
                Arrays.asList(
                        "rec full Name",
                        "recommended short name",
                        "sub full Name",
                        "short alt name1",
                        "a full alt Name",
                        "containsrec full Name",
                        "containsrecommended short name",
                        "containsshort alt name1",
                        "containsa full alt Name",
                        "contains cd antigen",
                        "contains allergen",
                        "contains inn antigen",
                        "contains biotech",
                        "includesrec full Name",
                        "includesrecommended short name",
                        "includesshort alt name1",
                        "includesa full alt Name",
                        "includes cd antigen",
                        "includes allergen",
                        "includes inn antigen",
                        "includes biotech",
                        "main allergen",
                        "main biotech",
                        "main cd antigen",
                        "main inn antigen");

        assertEquals(25, document.proteinNames.size());
        assertEquals(indexedNames, document.proteinNames);

        // Should we add EC numbers to the content field?
        assertEquals(25, document.content.size());
        assertEquals(new HashSet<>(indexedNames), document.content);

        assertEquals(30, document.proteinsNamesSort.length());
        assertEquals("rec full Name recommended shor", document.proteinsNamesSort);

        assertEquals(
                Arrays.asList(
                        "1.2.3.4", "1.2.3.5", "1.2.3.3", "1.2.3.4", "1.2.3.3", "1.2.3.4",
                        "1.2.3.3"),
                document.ecNumbers);
        assertEquals(document.ecNumbersExact, document.ecNumbers);

        assertTrue(document.fragment);
        assertFalse(document.precursor);

        assertEquals(3, suggestions.size());
        assertTrue(suggestions.containsKey("EC:1.2.3.3"));
        assertTrue(suggestions.containsKey("EC:1.2.3.4"));
        assertTrue(suggestions.containsKey("EC:1.2.3.5"));

        SuggestDocument suggestionDocument = suggestions.get("EC:1.2.3.3");
        assertEquals(suggestionDocument.id, "1.2.3.3");
        assertEquals(suggestionDocument.value, "Label 3");
        assertTrue(suggestionDocument.altValues.isEmpty());
        assertEquals(suggestionDocument.dictionary, "EC");
        assertEquals(suggestionDocument.importance, "medium");
    }

    @Test
    void convertPrecursorProteinDescription() {
        UniProtDocument document = new UniProtDocument();
        Map<String, SuggestDocument> suggestions = new HashMap<>();

        ECRepo ecRepo = mock(ECRepo.class);
        when(ecRepo.getEC("1.2.3.4"))
                .thenReturn(Optional.of(new ECBuilder().label("Label 4").id("1.2.3.4").build()));

        ProteinDescription description =
                new ProteinDescriptionBuilder()
                        .recommendedName(getRecommendedName(""))
                        .flag(FlagType.PRECURSOR)
                        .build();

        UniprotEntryProteinDescriptionConverter converter =
                new UniprotEntryProteinDescriptionConverter(ecRepo, suggestions);
        converter.convertProteinDescription(description, document);

        assertFalse(document.fragment);
        assertTrue(document.precursor);
    }

    @Test
    void convertPrecursorAndFragmentProteinDescription() {
        UniProtDocument document = new UniProtDocument();
        Map<String, SuggestDocument> suggestions = new HashMap<>();

        ECRepo ecRepo = mock(ECRepo.class);
        when(ecRepo.getEC("1.2.3.4"))
                .thenReturn(Optional.of(new ECBuilder().label("Label 4").id("1.2.3.4").build()));

        ProteinDescription description =
                new ProteinDescriptionBuilder()
                        .recommendedName(getRecommendedName(""))
                        .flag(FlagType.FRAGMENTS_PRECURSOR)
                        .build();

        UniprotEntryProteinDescriptionConverter converter =
                new UniprotEntryProteinDescriptionConverter(ecRepo, suggestions);
        converter.convertProteinDescription(description, document);

        assertTrue(document.fragment);
        assertTrue(document.precursor);
    }

    private static ProteinDescription getProteinDescription() {
        ProteinSection include =
                new ProteinSectionBuilder()
                        .recommendedName(getRecommendedName("includes"))
                        .alternativeNamesSet(createAltName("includes"))
                        .innNamesAdd(createName("includes inn antigen", "PRU100212"))
                        .allergenName(createName("includes allergen", "PRU10023"))
                        .biotechName(createName("includes biotech", "PRU10024"))
                        .cdAntigenNamesAdd(createName("includes cd antigen", "PRU10025"))
                        .build();

        ProteinSection contain =
                new ProteinSectionBuilder()
                        .recommendedName(getRecommendedName("contains"))
                        .alternativeNamesSet(createAltName("contains"))
                        .innNamesAdd(createName("contains inn antigen", "PRU100212"))
                        .allergenName(createName("contains allergen", "PRU10023"))
                        .biotechName(createName("contains biotech", "PRU10024"))
                        .cdAntigenNamesAdd(createName("contains cd antigen", "PRU10025"))
                        .build();

        ProteinRecName recommendedName = getRecommendedName("");
        List<ProteinAltName> proteinAltNames = createAltName("");
        List<ProteinSubName> subNames = getSubmissionName();

        return new ProteinDescriptionBuilder()
                .allergenName(createName("main allergen", "PRU10023"))
                .alternativeNamesSet(proteinAltNames)
                .biotechName(createName("main biotech", "PRU10024"))
                .cdAntigenNamesAdd(createName("main cd antigen", "PRU10025"))
                .flag(FlagType.FRAGMENT)
                .includesAdd(include)
                .containsAdd(contain)
                .innNamesAdd(createName("main inn antigen", "PRU100212"))
                .recommendedName(recommendedName)
                .submissionNamesSet(subNames)
                .build();
    }

    private static ProteinRecName getRecommendedName(String from) {
        Name fullName = createName(from + "rec full Name", "PRU10026");
        List<Name> shortNames = createNameList(from + "recommended short name", "PRU10020");
        List<EC> ecNumbers = createECNumbers("1.2.3.4", 10);

        return new ProteinRecNameBuilder()
                .fullName(fullName)
                .shortNamesSet(shortNames)
                .ecNumbersSet(ecNumbers)
                .build();
    }

    private static List<ProteinSubName> getSubmissionName() {
        Name fullName1 = createName("sub full Name", "PRU10027");
        List<EC> ecNumbers1 = createECNumbers("1.2.3.5", 11);

        ProteinSubName subName =
                new ProteinSubNameBuilder().fullName(fullName1).ecNumbersSet(ecNumbers1).build();
        return Collections.singletonList(subName);
    }

    private static List<ProteinAltName> createAltName(String from) {
        Name fullName = createName(from + "a full alt Name", "PRU10022");
        List<Name> shortNames = createNameList(from + "short alt name1", "PRU10028");
        List<EC> ecNumbers = createECNumbers("1.2.3.3", 9);

        ProteinAltName alternativeName =
                new ProteinAltNameBuilder()
                        .fullName(fullName)
                        .shortNamesSet(shortNames)
                        .ecNumbersSet(ecNumbers)
                        .build();
        return Collections.singletonList(alternativeName);
    }

    private static List<Name> createNameList(String value, String id) {
        return Collections.singletonList(createName(value, id));
    }

    private static Name createName(String value, String id) {
        return new NameBuilder().value(value).evidencesAdd(createEvidence(2)).build();
    }

    private static List<EC> createECNumbers(String ec, int index) {
        return Collections.singletonList(
                new org.uniprot.core.uniprot.description.builder.ECBuilder()
                        .value(ec)
                        .evidencesAdd(createEvidence(index))
                        .build());
    }

    private static Evidence createEvidence(int index) {
        return new EvidenceBuilder()
                .evidenceCode(EvidenceCode.ECO_0000255)
                .databaseName("PROSITE-ProRule")
                .databaseId("PRU1002" + index)
                .build();
    }
}
