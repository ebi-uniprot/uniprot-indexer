package uk.ac.ebi.uniprot.indexer.test.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

@TestConfiguration
@Slf4j
public class FakeReadDatabaseConfig {

    @Bean(name = "readDataSource",destroyMethod = "shutdown")
    public EmbeddedDatabase readDataSource(){
        log.info("Initializing EmbeddedDatabase Database with necessary table and fake data for tests");
        return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2)
                .addScript("classpath:org/springframework/batch/core/schema-drop-h2.sql")
                .addScript("classpath:org/springframework/batch/core/schema-h2.sql")
                .addScript("classpath:database/createDatabaseSchema.sql")
                .addScript("classpath:database/loadDatabaseFakeData.sql")
                .build();
    }

}