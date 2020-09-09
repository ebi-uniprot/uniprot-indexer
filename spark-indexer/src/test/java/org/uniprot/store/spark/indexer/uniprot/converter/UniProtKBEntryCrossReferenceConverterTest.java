package org.uniprot.store.spark.indexer.uniprot.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.uniprot.core.Property;
import org.uniprot.core.uniprotkb.evidence.Evidence;
import org.uniprot.core.uniprotkb.evidence.EvidenceCode;
import org.uniprot.core.uniprotkb.evidence.impl.EvidenceBuilder;
import org.uniprot.core.uniprotkb.xdb.UniProtKBCrossReference;
import org.uniprot.core.uniprotkb.xdb.UniProtKBDatabase;
import org.uniprot.core.uniprotkb.xdb.impl.UniProtCrossReferenceBuilder;
import org.uniprot.cv.xdb.UniProtKBDatabaseImpl;
import org.uniprot.store.search.document.uniprot.UniProtDocument;

/**
 * @author lgonzales
 * @since 2019-09-11
 */
class UniProtKBEntryCrossReferenceConverterTest {

    @Test
    void convertProteomeCrossReferences() {
        UniProtDocument document = new UniProtDocument();

        UniProtEntryCrossReferenceConverter converter = new UniProtEntryCrossReferenceConverter();

        UniProtKBCrossReference xref =
                getUniProtDBCrossReference(
                        new UniProtKBDatabaseImpl("Proteomes"),
                        "id value",
                        new Property("Component", "PC12345"));
        List<UniProtKBCrossReference> references = Collections.singletonList(xref);

        converter.convertCrossReferences(references, document);

        assertEquals(
                new HashSet<>(Arrays.asList("proteomes-id value", "id value")), document.crossRefs);

        assertEquals(Collections.singleton("proteomes"), document.databases);

        assertTrue(document.xrefCountMap.containsKey("xref_count_proteomes"));
        assertEquals(1L, document.xrefCountMap.get("xref_count_proteomes"));

        assertEquals(Collections.singleton("id value"), document.proteomes);
        assertEquals(Collections.singleton("PC12345"), document.proteomeComponents);
    }

    @Test
    void convertGoCrossReferences() {
        UniProtDocument document = new UniProtDocument();

        UniProtEntryCrossReferenceConverter converter = new UniProtEntryCrossReferenceConverter();

        UniProtKBCrossReference xref =
                getUniProtDBCrossReference(
                        new UniProtKBDatabaseImpl("GO"),
                        "GO:12345",
                        new Property("GoTerm", "C:apical dendrite"),
                        new Property("GoEvidenceType", "IDA:UniProtKB"));
        List<UniProtKBCrossReference> references = Collections.singletonList(xref);

        converter.convertCrossReferences(references, document);

        assertEquals(new HashSet<>(Arrays.asList("go-GO:12345", "GO:12345")), document.crossRefs);

        assertEquals(Collections.singleton("go"), document.databases);

        assertTrue(document.xrefCountMap.containsKey("xref_count_go"));
        assertEquals(1L, document.xrefCountMap.get("xref_count_go"));

        assertEquals(new HashSet<>(Arrays.asList("12345", "apical dendrite")), document.goes);
        assertEquals(new HashSet<>(Collections.singletonList("12345")), document.goIds);

        assertTrue(document.goWithEvidenceMaps.containsKey("go_ida"));
        assertEquals(
                new HashSet<>(Arrays.asList("12345", "apical dendrite")),
                document.goWithEvidenceMaps.get("go_ida"));
    }

    @Test
    void convertPDBCrossReferences() {
        UniProtDocument document = new UniProtDocument();

        UniProtEntryCrossReferenceConverter converter = new UniProtEntryCrossReferenceConverter();

        UniProtKBCrossReference xref =
                getUniProtDBCrossReference(
                        new UniProtKBDatabaseImpl("PDB"),
                        "id value",
                        new Property("id", "PDB12345"));
        List<UniProtKBCrossReference> references = Collections.singletonList(xref);

        converter.convertCrossReferences(references, document);

        assertEquals(new HashSet<>(Arrays.asList("pdb-id value", "id value")), document.crossRefs);

        assertEquals(Collections.singleton("pdb"), document.databases);

        assertTrue(document.xrefCountMap.containsKey("xref_count_pdb"));
        assertEquals(1L, document.xrefCountMap.get("xref_count_pdb"));

        assertTrue(document.d3structure);
    }

    @Test
    void convertEmblCrossReferences() {
        UniProtDocument document = new UniProtDocument();

        UniProtEntryCrossReferenceConverter converter = new UniProtEntryCrossReferenceConverter();

        UniProtKBCrossReference xref =
                getUniProtDBCrossReference(
                        new UniProtKBDatabaseImpl("EMBL"),
                        "id value",
                        new Property("ProteinId", "EMBL12345"),
                        new Property("NotProteinId", "notIndexed"));
        List<UniProtKBCrossReference> references = Collections.singletonList(xref);

        converter.convertCrossReferences(references, document);

        assertEquals(
                new HashSet<>(
                        Arrays.asList("embl-id value", "embl-EMBL12345", "EMBL12345", "id value")),
                document.crossRefs);

        assertEquals(Collections.singleton("embl"), document.databases);

        assertTrue(document.xrefCountMap.containsKey("xref_count_embl"));
        assertEquals(1L, document.xrefCountMap.get("xref_count_embl"));
    }

    @Test
    void convertCrossReferencesWithProperty() {
        UniProtDocument document = new UniProtDocument();

        UniProtEntryCrossReferenceConverter converter = new UniProtEntryCrossReferenceConverter();

        UniProtKBCrossReference xref =
                getUniProtDBCrossReference(
                        new UniProtKBDatabaseImpl("Ensembl"),
                        "id value",
                        new Property("ProteinId", "E12345"));
        List<UniProtKBCrossReference> references = Collections.singletonList(xref);

        converter.convertCrossReferences(references, document);

        assertEquals(
                new HashSet<>(
                        Arrays.asList("ensembl-id value", "ensembl-E12345", "E12345", "id value")),
                document.crossRefs);

        assertEquals(Collections.singleton("ensembl"), document.databases);

        assertTrue(document.xrefCountMap.containsKey("xref_count_ensembl"));
        assertEquals(1L, document.xrefCountMap.get("xref_count_ensembl"));
    }

    private static UniProtKBCrossReference getUniProtDBCrossReference(
            UniProtKBDatabase dbType, String id, Property... property) {
        return new UniProtCrossReferenceBuilder()
                .id(id)
                .isoformId("Q9NXB0-1")
                .propertiesSet(Arrays.asList(property))
                .database(dbType)
                .evidencesAdd(createEvidence())
                .build();
    }

    private static Evidence createEvidence() {
        return new EvidenceBuilder()
                .evidenceCode(EvidenceCode.ECO_0000269)
                .databaseName("PubMed")
                .databaseId("71234329")
                .build();
    }
}
