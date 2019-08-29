package org.uniprot.store.indexer.taxonomy.readers;

import org.springframework.jdbc.core.RowMapper;
import org.uniprot.core.uniprot.taxonomy.Taxonomy;
import org.uniprot.core.uniprot.taxonomy.builder.TaxonomyBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author lgonzales
 */
public class TaxonomyVirusHostReader implements RowMapper<Taxonomy> {

    @Override
    public Taxonomy mapRow(ResultSet resultSet, int rowIndex) throws SQLException {
        TaxonomyBuilder builder = new TaxonomyBuilder();
        builder.taxonId(resultSet.getLong("TAX_ID"));
        String scientificName = resultSet.getString("SPTR_SCIENTIFIC");
        if (scientificName == null) {
            scientificName = resultSet.getString("NCBI_SCIENTIFIC");
        }
        String common = resultSet.getString("SPTR_COMMON");
        if (common == null) {
            common = resultSet.getString("NCBI_COMMON");
        }
        builder.scientificName(scientificName);
        builder.commonName(common);
        builder.mnemonic(resultSet.getString("TAX_CODE"));
        builder.addSynonyms(resultSet.getString("SPTR_SYNONYM"));
        return builder.build();
    }
}