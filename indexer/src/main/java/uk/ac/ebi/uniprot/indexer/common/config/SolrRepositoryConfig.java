package uk.ac.ebi.uniprot.indexer.common.config;

import org.apache.http.client.HttpClient;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.HttpClientUtil;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.data.solr.core.SolrTemplate;

import java.util.Optional;

import static java.util.Arrays.asList;

/**
 *
 * @author lgonzales
 *
 * //TODO: REUSE COMMON SOLR CONFIG..... (DUPLICATED CODE FOR PoC ONLY)
 */
@Configuration
@Profile("online")
@Import(RepositoryConfigProperties.class)
public class SolrRepositoryConfig{
    @Bean
    public HttpClient httpClient(RepositoryConfigProperties config) {
        // I am creating HttpClient exactly in the same way it is created inside CloudSolrClient.Builder,
        // but here I am just adding Credentials
        ModifiableSolrParams param = null;
        if (config.getUsername() != null && !config.getUsername().isEmpty() && config.getPassword() != null && !config.getPassword().isEmpty()) {
            param = new ModifiableSolrParams();
            param.add(HttpClientUtil.PROP_BASIC_AUTH_USER, config.getUsername());
            param.add(HttpClientUtil.PROP_BASIC_AUTH_PASS, config.getPassword());
        }
        return HttpClientUtil.createClient(param);
    }

    @Bean
    public SolrClient uniProtSolrClient(HttpClient httpClient, RepositoryConfigProperties config) {
        String zookeeperhost = config.getZkHost();
        if (zookeeperhost != null && !zookeeperhost.isEmpty()) {
            String[] zookeeperHosts = zookeeperhost.split(",");
            return new CloudSolrClient.Builder(asList(zookeeperHosts), Optional.empty())
                    .withHttpClient(httpClient)
                    .withConnectionTimeout(config.getConnectionTimeout())
                    .withSocketTimeout(config.getSocketTimeout())
                    .build();
        } else if (!config.getHttphost().isEmpty()) {
            return new HttpSolrClient.Builder().withHttpClient(httpClient).withBaseSolrUrl(config.getHttphost())
                    .build();
        } else {
            throw new BeanCreationException("make sure your application.properties has eight solr zookeeperhost or httphost properties");
        }
    }

    @Bean
    public SolrTemplate solrTemplate(SolrClient uniProtSolrClient) {
        return new SolrTemplate(uniProtSolrClient);
    }

}
