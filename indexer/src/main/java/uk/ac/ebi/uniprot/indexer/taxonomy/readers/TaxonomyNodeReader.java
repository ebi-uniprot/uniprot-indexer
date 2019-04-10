package uk.ac.ebi.uniprot.indexer.taxonomy.readers;

import org.springframework.jdbc.core.RowMapper;

import uk.ac.ebi.uniprot.indexer.document.taxonomy.TaxonomyDocument;
import uk.ac.ebi.uniprot.indexer.taxonomy.steps.TaxonomyNodeStep;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This class is mapping {@link ResultSet} returned by SQL executed in
 *  {@link TaxonomyNodeStep}
 *  to {@link TaxonomyDocument} object that will be used to save Taxonomy Nodes
 *
 *  @author lgonzales
 */
public class TaxonomyNodeReader implements RowMapper<TaxonomyDocument> {

    @Override
    public TaxonomyDocument mapRow(ResultSet resultSet, int i) throws SQLException {
        TaxonomyDocument.TaxonomyDocumentBuilder builder = TaxonomyDocument.builder();
        builder.id(""+resultSet.getLong("TAX_ID"));
        builder.taxId(resultSet.getLong("TAX_ID"));
        String common = resultSet.getString("SPTR_COMMON");
        if(common == null){
            common = resultSet.getString("NCBI_COMMON");
        }
        builder.common(common);
        String scientificName = resultSet.getString("SPTR_SCIENTIFIC");
        if(scientificName == null){
            scientificName = resultSet.getString("NCBI_SCIENTIFIC");
        }
        builder.scientific(scientificName);

        builder.mnemonic(resultSet.getString("TAX_CODE"));
        builder.ancestor(resultSet.getLong("PARENT_ID"));
        builder.rank(resultSet.getString("RANK"));
        builder.synonym(resultSet.getString("SPTR_SYNONYM"));
        //builder.(resultSet.getString("SUPERREGNUM"));
        builder.hidden(resultSet.getBoolean("HIDDEN"));
        return builder.build();
    }
}