package uk.ac.ebi.uniprot.datastore.voldemort.client.impl;

import uk.ac.ebi.uniprot.datastore.voldemort.VoldemortClient;
import uk.ac.ebi.uniprot.datastore.voldemort.client.UniProtClient;
import uk.ac.ebi.uniprot.domain.uniprot.UniProtEntry;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class UniProtClientImpl implements UniProtClient {
    private final VoldemortClient<UniProtEntry> client;

    UniProtClientImpl(VoldemortClient<UniProtEntry> client) {
        this.client = client;
 
    }

    @Override
    public Optional<UniProtEntry> getEntry(String accession) {
        return client.getEntry(accession);

    }

    @Override
    public List<UniProtEntry> getEntries(Iterable<String> accessions) {
        return client.getEntries(accessions);
    }

    @Override
    public Map<String, UniProtEntry> getEntryMap(Iterable<String> ids) {
        return client.getEntryMap(ids);
    }

    @Override
    public void saveEntry(UniProtEntry entry) {
        client.saveEntry(entry);
    }

    @Override
    public String getStoreName() {
        return client.getStoreName();
    }

}