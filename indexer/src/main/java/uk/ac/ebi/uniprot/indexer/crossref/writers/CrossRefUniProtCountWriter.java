package uk.ac.ebi.uniprot.indexer.crossref.writers;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemWriter;
import uk.ac.ebi.uniprot.indexer.common.utils.Constants;
import uk.ac.ebi.uniprot.indexer.crossref.readers.CrossRefUniProtCountReader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author sahmad
 */
public class CrossRefUniProtCountWriter implements ItemWriter<CrossRefUniProtCountReader.CrossRefProteinCount> {
    private StepExecution stepExecution;
    private static final String UNDERSCORE = "_";

    @Override
    public void write(List<? extends CrossRefUniProtCountReader.CrossRefProteinCount> xrefProtCountList) throws Exception {

        if (this.stepExecution != null) { // null if being called from unit test

            JobExecution jobExecution = this.stepExecution.getJobExecution();
            ExecutionContext executionContext = jobExecution.getExecutionContext();

            Map<String, CrossRefUniProtCountReader.CrossRefProteinCount> xrefProteinCountMap;

            if (executionContext.get(Constants.CROSS_REF_PROTEIN_COUNT_KEY) == null) {
                // create a map where key will be of the form abbrev_0 or abbrev_1 ie suffixed with entry_type

                xrefProteinCountMap = xrefProtCountList.stream()
                        .collect(
                                Collectors.toMap(xrefProtCount -> xrefProtCount.getAbbreviation() + UNDERSCORE + xrefProtCount.getEntryType(),
                                                xrefProtCount -> xrefProtCount));

            } else { // update the existing map

                xrefProteinCountMap = (Map<String, CrossRefUniProtCountReader.CrossRefProteinCount>)
                        executionContext.get(Constants.DISEASE_PROTEIN_COUNT_KEY);

                xrefProteinCountMap.putAll(xrefProtCountList.stream()
                        .collect(Collectors.toMap(xrefProtCount -> xrefProtCount.getAbbreviation() + UNDERSCORE + xrefProtCount.getEntryType(),
                                                  xrefProtCount -> xrefProtCount)));
            }

            executionContext.put(Constants.CROSS_REF_PROTEIN_COUNT_KEY, xrefProteinCountMap);
        }

    }

    @BeforeStep
    // set the stepExecution to pass data from this step to another step. See above executionContext.put() call
    public void setStepExecution(final StepExecution stepExecution) {
        this.stepExecution = stepExecution;
    }
}
