package org.uniprot.store.search.domain;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.uniprot.store.search.domain.EvidenceGroup;
import org.uniprot.store.search.domain.EvidenceItem;
import org.uniprot.store.search.domain.impl.EvidenceGroupImpl;
import org.uniprot.store.search.domain.impl.EvidenceItemImpl;
import org.uniprot.store.search.domain.impl.GoEvidences;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class GoEvidencesTest {
	private static GoEvidences instance;

	@BeforeAll
	static void initAll() {
		instance = GoEvidences.INSTANCE;
	}

	@Test
	void testSize() {
		List<EvidenceGroup> groups = instance.getEvidences();
		assertEquals(3, groups.size());
		int size =
				groups.stream().mapToInt(val ->val.getItems().size())
				.sum();
		assertEquals(24, size);
	}
	
	@Test
	void testEvidenceGroup() {
		List<EvidenceGroup> groups = instance.getEvidences();
		assertEquals(3, groups.size());
		assertTrue(groups.stream().anyMatch(val -> val.getGroupName().equals("Any")));
		assertFalse(groups.stream().anyMatch(val -> val.getGroupName().equals("Manual experimental assertions")));

	}

	@Test
	void testEvidence() {
		List<EvidenceGroup> groups = instance.getEvidences();
		Optional<EvidenceGroup> aaGroup = groups.stream()
				.filter(val -> val.getGroupName().equals("Manual high-throughput assertions")).findFirst();

		assertFalse(aaGroup.isPresent());
		
		aaGroup = groups.stream()
				.filter(val -> val.getGroupName().equals("Manual assertions")).findFirst();
		assertTrue(aaGroup.isPresent());
		assertEquals(20, aaGroup.orElse(new EvidenceGroupImpl()).getItems().size());
		Optional<EvidenceItem> item = aaGroup.orElse(new EvidenceGroupImpl()).getItems().stream()
				.filter(val -> val.getName().equals("Inferred from high throughput genetic interaction [HGI]")).findFirst();
		assertTrue(item.isPresent());
		assertEquals("HGI", item.orElse(new EvidenceItemImpl()).getCode());
	}
}
