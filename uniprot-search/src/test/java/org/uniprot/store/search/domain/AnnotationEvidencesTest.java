package org.uniprot.store.search.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.uniprot.store.search.domain.impl.AnnotationEvidences;
import org.uniprot.store.search.domain.impl.EvidenceGroupImpl;
import org.uniprot.store.search.domain.impl.EvidenceItemImpl;

class AnnotationEvidencesTest {

    private static AnnotationEvidences instance;

    @BeforeAll
    static void initAll() {
        instance = AnnotationEvidences.INSTANCE;
    }

    @Test
    void testSize() {
        List<EvidenceGroup> groups = instance.getEvidences();
        assertEquals(3, groups.size());
        int size = groups.stream().mapToInt(val -> val.getItems().size()).sum();
        assertEquals(15, size);
    }

    @Test
    void testEvidenceGroup() {
        List<EvidenceGroup> groups = instance.getEvidences();
        assertEquals(3, groups.size());
        assertTrue(groups.stream().anyMatch(val -> val.getGroupName().equals("Any")));
        assertTrue(groups.stream().anyMatch(val -> val.getGroupName().equals("Manual assertions")));
    }

    @Test
    void testEvidence() {
        List<EvidenceGroup> groups = instance.getEvidences();
        Optional<EvidenceGroup> aaGroup =
                groups.stream()
                        .filter(val -> val.getGroupName().equals("Automatic assertions"))
                        .findFirst();

        assertTrue(aaGroup.isPresent());
        assertEquals(4, aaGroup.orElse(new EvidenceGroupImpl()).getItems().size());
        Optional<EvidenceItem> item =
                aaGroup.orElse(new EvidenceGroupImpl()).getItems().stream()
                        .filter(val -> val.getName().equals("Sequence model"))
                        .findFirst();
        assertTrue(item.isPresent());
        assertEquals("ECO_0000256", item.orElse(new EvidenceItemImpl()).getCode());
    }
}
