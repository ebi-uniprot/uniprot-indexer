package org.uniprot.store.spark.indexer.uniparc;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.junit.jupiter.api.Test;
import org.uniprot.core.taxonomy.TaxonomyEntry;
import org.uniprot.core.taxonomy.TaxonomyLineage;
import org.uniprot.core.taxonomy.impl.TaxonomyEntryBuilder;
import org.uniprot.core.taxonomy.impl.TaxonomyLineageBuilder;
import org.uniprot.store.search.document.uniparc.UniParcDocument;
import org.uniprot.store.spark.indexer.common.JobParameter;
import org.uniprot.store.spark.indexer.common.util.SparkUtils;

import scala.Tuple2;

/**
 * @author lgonzales
 * @since 22/06/2020
 */
class UniParcDocumentsToHDFSWriterTest {

    @Test
    void writeIndexDocumentsToHDFS() {
        ResourceBundle application = SparkUtils.loadApplicationProperty();
        try (JavaSparkContext sparkContext = SparkUtils.loadSparkContext(application)) {
            JobParameter parameter =
                    JobParameter.builder()
                            .applicationConfig(application)
                            .releaseName("2020_02")
                            .sparkContext(sparkContext)
                            .build();

            UniParcDocumentsToHDFSWriterTest.UniParcDocumentsToHDFSWriterFake writer =
                    new UniParcDocumentsToHDFSWriterTest.UniParcDocumentsToHDFSWriterFake(
                            parameter);
            writer.writeIndexDocumentsToHDFS();
            List<UniParcDocument> savedDocuments = writer.getSavedDocuments();
            assertNotNull(savedDocuments);
            assertEquals(1, savedDocuments.size());
            UniParcDocument uniref50 = savedDocuments.get(0);
            assertEquals("UPI00000E8551", uniref50.getUpi());
            assertEquals("01AEF4B6A09EB753", uniref50.getSequenceChecksum());
            assertTrue(uniref50.getTaxLineageIds().contains(100));
            assertTrue(uniref50.getOrganismTaxons().contains("lineageSC"));
        }
    }

    private static class UniParcDocumentsToHDFSWriterFake extends UniParcDocumentsToHDFSWriter {

        private final JobParameter parameter;
        private List<UniParcDocument> documents;

        public UniParcDocumentsToHDFSWriterFake(JobParameter parameter) {
            super(parameter);
            this.parameter = parameter;
        }

        @Override
        JavaPairRDD<String, TaxonomyEntry> loadTaxonomyEntryJavaPairRDD() {
            List<Tuple2<String, TaxonomyEntry>> tuple2List = new ArrayList<>();
            TaxonomyEntry tax =
                    new TaxonomyEntryBuilder().taxonId(337687).scientificName("sn337687").build();
            tuple2List.add(new Tuple2<>("337687", tax));

            TaxonomyLineage lineage =
                    new TaxonomyLineageBuilder().taxonId(100).scientificName("lineageSC").build();
            tax =
                    new TaxonomyEntryBuilder()
                            .taxonId(10116)
                            .scientificName("sn10116")
                            .lineagesAdd(lineage)
                            .build();
            tuple2List.add(new Tuple2<>("10116", tax));

            return parameter.getSparkContext().parallelizePairs(tuple2List);
        }

        @Override
        void saveToHDFS(JavaRDD<UniParcDocument> uniParcDocumentRDD) {
            documents = uniParcDocumentRDD.collect();
        }

        List<UniParcDocument> getSavedDocuments() {
            return documents;
        }
    }
}
