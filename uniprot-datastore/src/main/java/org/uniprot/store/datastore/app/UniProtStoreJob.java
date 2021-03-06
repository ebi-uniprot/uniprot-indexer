package org.uniprot.store.datastore.app;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * This annotation should be used on a Spring Boot app to denote that the application is the main
 * entry point to a UniProt store job.
 *
 * <p>This annotation subsequently scans required packages in this codebase to pick up required
 * beans used by the indexing job. Created 28/05/19
 *
 * @author Edd
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
// Add extra packages below if all beans in it are required for all jobs
@ComponentScan(
        basePackages = {
            // job/step listeners that are useful for all jobs
            "org.uniprot.store.job.common.listener",

            // this package defines all job configs, but only 1 is enabled
            // via @ConditionalOnProperty annotation
            "org.uniprot.store.datastore.app"
        })
@Configuration
public @interface UniProtStoreJob {}
