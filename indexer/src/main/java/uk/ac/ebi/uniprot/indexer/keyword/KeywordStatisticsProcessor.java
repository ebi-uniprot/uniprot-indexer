package uk.ac.ebi.uniprot.indexer.keyword;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.batch.item.ItemProcessor;
import uk.ac.ebi.uniprot.cv.keyword.KeywordEntry;
import uk.ac.ebi.uniprot.cv.keyword.KeywordStatistics;
import uk.ac.ebi.uniprot.cv.keyword.impl.KeywordEntryImpl;
import uk.ac.ebi.uniprot.cv.keyword.impl.KeywordStatisticsImpl;
import uk.ac.ebi.uniprot.json.parser.keyword.KeywordJsonConfig;
import uk.ac.ebi.uniprot.search.document.keyword.KeywordDocument;

import java.nio.ByteBuffer;

/**
 * @author lgonzales
 */
public class KeywordStatisticsProcessor implements ItemProcessor<KeywordStatisticsReader.KeywordCount, KeywordDocument> {

    private final ObjectMapper keywordObjectMapper;

    public KeywordStatisticsProcessor() {
        this.keywordObjectMapper = KeywordJsonConfig.getInstance().getFullObjectMapper();
    }

    @Override
    public KeywordDocument process(KeywordStatisticsReader.KeywordCount keywordCount) throws Exception {
        KeywordStatistics statistics = new KeywordStatisticsImpl(keywordCount.getReviewedProteinCount(), keywordCount.getUnreviewedProteinCount());
        KeywordEntryImpl keywordEntry = new KeywordEntryImpl();
        keywordEntry.setStatistics(statistics);

        KeywordDocument.KeywordDocumentBuilder builder = KeywordDocument.builder();
        builder.id(keywordCount.getKeywordId());

        byte[] keywordByte = getKeywordObjectBinary(keywordEntry);
        builder.keywordObj(ByteBuffer.wrap(keywordByte));

        return builder.build();
    }

    private byte[] getKeywordObjectBinary(KeywordEntry keyword) {
        try {
            return this.keywordObjectMapper.writeValueAsBytes(keyword);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unable to parse Keyword to binary json: ", e);
        }
    }
}