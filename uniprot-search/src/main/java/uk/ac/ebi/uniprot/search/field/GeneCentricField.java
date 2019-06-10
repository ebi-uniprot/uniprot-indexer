package uk.ac.ebi.uniprot.search.field;

import java.util.function.Predicate;

import uk.ac.ebi.uniprot.search.field.validator.FieldValueValidator;

/**
 *
 * @author jluo
 * @date: 17 May 2019
 *
 */

public interface GeneCentricField {
	public enum Return {
		accession_id, genecentric_stored;
	};

	public enum Sort {
		accession_id("accession_id");

		private String solrFieldName;

		Sort(String solrFieldName) {
			this.solrFieldName = solrFieldName;
		}

		public String getSolrFieldName() {
			return solrFieldName;
		}

		@Override
		public String toString() {
			return this.solrFieldName;
		}
	}

	public static enum Search implements SearchField {
		accession_id(SearchFieldType.TERM, FieldValueValidator::isAccessionValid, null), // uniprot entry accession
		accession(SearchFieldType.TERM, FieldValueValidator::isAccessionValid, null), // uniprot entry accession
		upid(SearchFieldType.TERM, FieldValueValidator::isUpidValid, null), // proteome upid
		organism_id(SearchFieldType.TERM, FieldValueValidator::isNumberValue, 2.0f), gene(
				SearchFieldType.TERM), reviewed(SearchFieldType.TERM, FieldValueValidator::isBooleanValue, null); // reviewed
																													// or
																													// not
																													// reviewed

		private final Predicate<String> fieldValueValidator;
		private final SearchFieldType searchFieldType;
		private final Float boostValue;

		Search(SearchFieldType searchFieldType) {
			this.searchFieldType = searchFieldType;
			this.fieldValueValidator = null;
			this.boostValue = null;
		}

		Search(SearchFieldType searchFieldType, Predicate<String> fieldValueValidator, Float boostValue) {
			this.searchFieldType = searchFieldType;
			this.fieldValueValidator = fieldValueValidator;
			this.boostValue = boostValue;
		}

		public Predicate<String> getFieldValueValidator() {
			return this.fieldValueValidator;
		}

		public SearchFieldType getSearchFieldType() {
			return this.searchFieldType;
		}

		@Override
		public Float getBoostValue() {
			return this.boostValue;
		}

		@Override
		public boolean hasBoostValue() {
			return boostValue != null;
		}

		@Override
		public boolean hasValidValue(String value) {
			return this.fieldValueValidator == null || this.fieldValueValidator.test(value);
		}

		@Override
		public String getName() {
			return this.name();
		}

	}

}