package uk.ac.ebi.uniprot.indexer.keyword;

/**
 * @author lgonzales
 */
public class KeywordSQLConstants {

    public static final String KEYWORD_STATISTICS_URL = " " +
            "SELECT COALESCE(r.accession,u.accession) as accession, r.reviewedProteinCount, u.unreviewedProteinCount " +
            "  FROM " +
            "    (SELECT kw.accession, count(1) as unreviewedProteinCount FROM sptr.dbentry db, sptr.dbentry_2_keyword dk, sptr.keyword kw " +
            "      WHERE dk.KEYWORD_ID = kw.KEYWORD_ID " +
            "        AND db.dbentry_id = dk.dbentry_id " +
            "        AND db.merge_status <>'R' " +
            "        AND db.deleted = 'N' " +
            "        AND db.entry_type = 1 " +
            "      GROUP BY kw.ACCESSION) u " +
            "    FULL JOIN (SELECT kw.accession, count(1) as reviewedProteinCount " +
            "      FROM sptr.dbentry db, sptr.dbentry_2_keyword dk, sptr.keyword kw " +
            "      WHERE dk.KEYWORD_ID = kw.KEYWORD_ID " +
            "        AND db.dbentry_id = dk.dbentry_id " +
            "        AND db.merge_status <>'R' " +
            "        AND db.deleted = 'N' " +
            "        AND db.entry_type = 0 " +
            "      GROUP BY kw.accession) r " +
            "    ON u.accession = r.accession " +
            "UNION ALL " + //UNION QUERY SELECTS KEYWORD CATEGORY COUNTS ....
            "SELECT COALESCE(r.accession,u.accession) as accession, r.reviewedProteinCount, u.unreviewedProteinCount " +
            "  FROM " +
            "    (SELECT category.accession, count( DISTINCT db.accession) as reviewedProteinCount " +
            "    FROM sptr.dbentry db " +
            "           INNER JOIN sptr.dbentry_2_keyword d2k on db.dbentry_id = d2k.DBENTRY_ID " +
            "           INNER JOIN ( " +
            "        SELECT DISTINCT keyword_id, " +
            "                        CASE UPPER(SUBSTR(HIERARCHY,0, INSTR(HIERARCHY,':')-1)) " +
            "                          WHEN 'CELLULAR COMPONENT' then 'KW-9998' " +
            "                          WHEN 'DEVELOPMENTAL STAGE' then 'KW-9996' " +
            "                          WHEN 'LIGAND' then 'KW-9993' " +
            "                          WHEN 'TECHNICAL TERM' then 'KW-9990' " +
            "                          WHEN 'CODING SEQUENCE DIVERSITY' then 'KW-9997' " +
            "                          WHEN 'BIOLOGICAL PROCESS' then 'KW-9999' " +
            "                          WHEN 'PTM' then 'KW-9991' " +
            "                          WHEN 'MOLECULAR FUNCTION' then 'KW-9992' " +
            "                          WHEN 'DISEASE' then 'KW-9995' " +
            "                          WHEN 'DOMAIN' then 'KW-9994' END accession " +
            "        FROM sptr.KEYWORD_HIERARCHY " +
            "      ) category ON d2k.KEYWORD_ID = category.keyword_id " +
            "    WHERE db.entry_type = 0 " +
            "      AND db.MERGE_STATUS <>'R' " +
            "      AND db.deleted ='N' " +
            "    GROUP BY category.accession) r " +
            "  FULL JOIN " +
            "    (SELECT category.accession, count( DISTINCT db.accession) as unreviewedProteinCount " +
            "     FROM sptr.dbentry db " +
            "            INNER JOIN sptr.dbentry_2_keyword d2k on db.dbentry_id = d2k.DBENTRY_ID " +
            "            INNER JOIN ( " +
            "         SELECT DISTINCT keyword_id, " +
            "                         CASE UPPER(SUBSTR(HIERARCHY,0, INSTR(HIERARCHY,':')-1)) " +
            "                           WHEN 'CELLULAR COMPONENT' then 'KW-9998' " +
            "                           WHEN 'DEVELOPMENTAL STAGE' then 'KW-9996' " +
            "                           WHEN 'LIGAND' then 'KW-9993' " +
            "                           WHEN 'TECHNICAL TERM' then 'KW-9990' " +
            "                           WHEN 'CODING SEQUENCE DIVERSITY' then 'KW-9997' " +
            "                           WHEN 'BIOLOGICAL PROCESS' then 'KW-9999' " +
            "                           WHEN 'PTM' then 'KW-9991' " +
            "                           WHEN 'MOLECULAR FUNCTION' then 'KW-9992' " +
            "                           WHEN 'DISEASE' then 'KW-9995' " +
            "                           WHEN 'DOMAIN' then 'KW-9994' END accession " +
            "         FROM sptr.KEYWORD_HIERARCHY " +
            "       ) category ON d2k.KEYWORD_ID = category.keyword_id " +
            "     WHERE db.entry_type = 1 " +
            "       AND db.MERGE_STATUS <>'R' " +
            "       AND db.deleted ='N' " +
            "     GROUP BY category.accession) u " +
            "  ON u.accession = r.accession";
}