package org.uniprot.store.datastore.voldemort.uniprot;

import org.uniprot.core.json.parser.uniprot.UniprotkbJsonConfig;
import org.uniprot.core.uniprotkb.UniProtkbEntry;
import org.uniprot.store.datastore.voldemort.VoldemortRemoteJsonBinaryStore;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * This class contains methods to save Uniprot voldemort entry remotely.
 *
 * <p>Created 05/10/2017
 *
 * @author lgonzales
 */
public class VoldemortRemoteUniProtKBEntryStore
        extends VoldemortRemoteJsonBinaryStore<UniProtkbEntry> {
    public static final String UNIPROT_VOLDEMORT_URL = "uniprotVoldemortUrl";
    public static final String UNIPROT_VOLDEMORT_STORE_NAME = "uniprotVoldemortStoreName";

    @Inject
    public VoldemortRemoteUniProtKBEntryStore(
            @Named(UNIPROT_VOLDEMORT_STORE_NAME) String storeName,
            @Named(UNIPROT_VOLDEMORT_URL) String voldemortUrl) {
        super(storeName, voldemortUrl);
    }

    public VoldemortRemoteUniProtKBEntryStore(
            int maxConnection, String storeName, String... voldemortUrl) {
        super(maxConnection, storeName, voldemortUrl);
    }

    @Override
    public String getStoreId(UniProtkbEntry entry) {
        return entry.getPrimaryAccession().getValue();
    }

    @Override
    public ObjectMapper getStoreObjectMapper() {
        return UniprotkbJsonConfig.getInstance().getFullObjectMapper();
    }

    @Override
    public Class<UniProtkbEntry> getEntryClass() {
        return UniProtkbEntry.class;
    }
}
