package org.uniprot.store.search.domain;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.uniprot.store.search.domain.SearchDataType;
import org.uniprot.store.search.domain.SearchItem;
import org.uniprot.store.search.domain.SearchItemType;
import org.uniprot.store.search.domain.Tuple;
import org.uniprot.store.search.domain.impl.TupleImpl;
import org.uniprot.store.search.domain.impl.UniProtSearchItem;
import org.uniprot.store.search.domain.impl.UniProtSearchItems;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class UniProtSearchItemsTest {
	private static UniProtSearchItems searchItems;

	@BeforeAll
	static void initAll() {
		searchItems = UniProtSearchItems.INSTANCE;
	}

	@Test
	void testSize() {
		List<SearchItem> items = searchItems.getSearchItems();
		assertEquals(27, items.size());
		AtomicInteger counter = new AtomicInteger();
		items.forEach(val -> numberOfItem(val, counter));
		assertTrue(counter.get()>127);
		
	}

	private void numberOfItem(SearchItem item, AtomicInteger counter) {
		if ((item.getItems() == null) || (item.getItems().isEmpty())) {
			counter.incrementAndGet();
		} else {
			item.getItems().forEach(val -> numberOfItem(val, counter));
		}
	}

	@Test
	void testSingleGeneNameItem() {
		Optional<SearchItem> item = searchItems.getSearchItems().stream()
				.filter(val -> val.getLabel().equals("Gene Name [GN]")).findFirst();
		assertTrue(item.isPresent());
		assertEquals("gene", item.orElse(new UniProtSearchItem()).getTerm());
		assertEquals(SearchDataType.STRING, item.orElse(new UniProtSearchItem()).getDataType());
	}

	@Test
	void testSingleOrganismItem() {
		Optional<SearchItem> item = searchItems.getSearchItems().stream()
				.filter(val -> val.getLabel().equals("Organism [OS]")).findFirst();
		assertTrue(item.isPresent());
		assertEquals("organism", item.orElse(new UniProtSearchItem()).getTerm());
		assertEquals(SearchDataType.STRING, item.orElse(new UniProtSearchItem()).getDataType());
		assertNotNull(item.orElse(new UniProtSearchItem()).getAutoComplete());
		assertEquals("/uniprot/api/suggester?dict=taxonomy&query=?", item.orElse(new UniProtSearchItem()).getAutoComplete());
	}

	@Test
	void testSingleProteinExistenceItem() {
		Optional<SearchItem> item = searchItems.getSearchItems().stream()
				.filter(val -> val.getLabel().equals("Protein Existence [PE]")).findFirst();
		assertTrue(item.isPresent());
		assertEquals("existence", item.orElse(new UniProtSearchItem()).getTerm());
		assertEquals(SearchDataType.ENUM, item.orElse(new UniProtSearchItem()).getDataType());
		assertNull(item.orElse(new UniProtSearchItem()).getAutoComplete());
		assertNotNull(item.orElse(new UniProtSearchItem()).getValues());
		assertEquals(5, item.orElse(new UniProtSearchItem()).getValues().size());
		Optional<Tuple> tuple = item.orElse(new UniProtSearchItem()).getValues().stream()
				.filter(val -> val.getName().equals("Inferred from homology")).findFirst();
		assertTrue(tuple.isPresent());
		assertEquals("homology", tuple.orElse(new TupleImpl()).getValue());
	}

	@Test
	void testFunctionCatalyticActivity() {
		Optional<SearchItem> item = searchItems.getSearchItems().stream()
				.filter(val -> val.getLabel().equals("Function")).findFirst();
		assertTrue(item.isPresent());
		assertEquals(SearchItemType.GROUP, item.orElse(new UniProtSearchItem()).getItemType());
		assertNotNull(item.orElse(new UniProtSearchItem()).getItems());
		Optional<SearchItem> subItem = item.orElse(new UniProtSearchItem()).getItems().stream()
				.filter(val -> val.getLabel().equals("Catalytic Activity")).findFirst();
		assertTrue(subItem.isPresent());
		assertEquals(SearchDataType.STRING, subItem.orElse(new UniProtSearchItem()).getDataType());
		assertEquals(SearchItemType.COMMENT, subItem.orElse(new UniProtSearchItem()).getItemType());
		assertEquals("catalytic_activity", subItem.orElse(new UniProtSearchItem()).getTerm());
		assertTrue(subItem.orElse(new UniProtSearchItem()).isHasEvidence());
	}

	@Test
	void testFunctionChebiTerm() {
		Optional<SearchItem> item = searchItems.getSearchItems().stream()
				.filter(val -> val.getLabel().equals("Function")).findFirst();
		assertTrue(item.isPresent());
		assertEquals(SearchItemType.GROUP, item.orElse(new UniProtSearchItem()).getItemType());
		assertNotNull(item.orElse(new UniProtSearchItem()).getItems());
		Optional<SearchItem> subItem = item.orElse(new UniProtSearchItem()).getItems().stream().filter(val -> val.getLabel().equals("Cofactors"))
				.findFirst();
		assertTrue(subItem.isPresent());
		assertEquals(SearchItemType.GROUP, subItem.orElse(new UniProtSearchItem()).getItemType());
		assertNotNull(subItem.orElse(new UniProtSearchItem()).getItems());

		Optional<SearchItem> subSubItem = subItem.orElse(new UniProtSearchItem()).getItems().stream()
				.filter(val -> val.getLabel().equals("ChEBI term")).findFirst();
		assertTrue(subSubItem.isPresent());
		assertEquals(SearchDataType.STRING, subSubItem.orElse(new UniProtSearchItem()).getDataType());
		assertEquals(SearchItemType.COMMENT, subSubItem.orElse(new UniProtSearchItem()).getItemType());
		assertEquals("/uniprot/api/suggester?dict=chebi&query=?", subSubItem.orElse(new UniProtSearchItem()).getAutoComplete());
		assertEquals("cofactor_chebi", subSubItem.orElse(new UniProtSearchItem()).getTerm());
		assertTrue(subSubItem.orElse(new UniProtSearchItem()).isHasEvidence());
	}

	@Test
	void testStructureTurn() {
		Optional<SearchItem> item = searchItems.getSearchItems().stream()
				.filter(val -> val.getLabel().equals("Structure")).findFirst();
		assertTrue(item.isPresent());
		assertEquals(SearchItemType.GROUP, item.orElse(new UniProtSearchItem()).getItemType());
		assertNotNull(item.orElse(new UniProtSearchItem()).getItems());
		Optional<SearchItem> subItem = item.orElse(new UniProtSearchItem()).getItems().stream()
				.filter(val -> val.getLabel().equals("Secondary structure")).findFirst();
		assertTrue(subItem.isPresent());
		assertNotNull(subItem.orElse(new UniProtSearchItem()).getItems());
		assertEquals(SearchItemType.GROUP, subItem.orElse(new UniProtSearchItem()).getItemType());

		Optional<SearchItem> subSubItem = subItem.orElse(new UniProtSearchItem()).getItems().stream().filter(val -> val.getLabel().equals("Turn"))
				.findFirst();
		assertTrue(subSubItem.isPresent());
		assertEquals(SearchItemType.FEATURE, subSubItem.orElse(new UniProtSearchItem()).getItemType());
		assertEquals(SearchDataType.STRING, subSubItem.orElse(new UniProtSearchItem()).getDataType());

		assertEquals("turn", subSubItem.orElse(new UniProtSearchItem()).getTerm());
		assertTrue(subSubItem.orElse(new UniProtSearchItem()).isHasRange());
		assertTrue(subSubItem.orElse(new UniProtSearchItem()).isHasEvidence());
	}

	@Test
	void testDatabase() {
		Optional<SearchItem> item = searchItems.getSearchItems().stream()
				.filter(val -> val.getLabel().equals("Cross-references")).findFirst();
		assertTrue(item.isPresent());
		assertEquals(SearchItemType.GROUP, item.orElse(new UniProtSearchItem()).getItemType());
	}

	@Test
	void testDateType() {
		Optional<SearchItem> item = searchItems.getSearchItems().stream()
				.filter(val -> val.getLabel().equals("Date Of")).findFirst();
		assertTrue(item.isPresent());
		assertEquals(SearchItemType.GROUP, item.orElse(new UniProtSearchItem()).getItemType());
		assertNotNull(item.orElse(new UniProtSearchItem()).getItems());
		Optional<SearchItem> subItem = item.orElse(new UniProtSearchItem()).getItems().stream()
				.filter(val -> val.getLabel().equals("Date of last entry modification")).findFirst();
		assertTrue(subItem.isPresent());
		assertEquals("modified", subItem.orElse(new UniProtSearchItem()).getTerm());
		assertEquals(SearchDataType.DATE, subItem.orElse(new UniProtSearchItem()).getDataType());
		assertEquals(SearchItemType.SINGLE, subItem.orElse(new UniProtSearchItem()).getItemType());
		assertTrue(subItem.orElse(new UniProtSearchItem()).isHasRange());
	}

	@Test
	void testGroupDisplay() {
		Optional<SearchItem> item = searchItems.getSearchItems().stream()
				.filter(val -> val.getLabel().equals("Literature Citation")).findFirst();
		assertTrue(item.isPresent());
		assertEquals(SearchItemType.GROUP_DISPLAY, item.orElse(new UniProtSearchItem()).getItemType());
		assertNotNull(item.orElse(new UniProtSearchItem()).getItems());
		Optional<SearchItem> subItem = item.orElse(new UniProtSearchItem()).getItems().stream().filter(val -> val.getLabel().equals("Published"))
				.findFirst();
		assertTrue(subItem.isPresent());
		assertEquals(SearchDataType.DATE, subItem.orElse(new UniProtSearchItem()).getDataType());
		assertEquals(SearchItemType.SINGLE, subItem.orElse(new UniProtSearchItem()).getItemType());
		assertEquals("lit_pubdate", subItem.orElse(new UniProtSearchItem()).getTerm());
		assertTrue(subItem.orElse(new UniProtSearchItem()).isHasRange());
	}

}
