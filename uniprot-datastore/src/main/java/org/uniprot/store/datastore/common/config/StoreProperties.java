package org.uniprot.store.datastore.common.config;

import lombok.Data;

/**
 * Created 27/07/19
 *
 * @author Edd
 */
@Data
public class StoreProperties {
    private String host;
    private int numberOfConnections;
    private String storeName;
}
