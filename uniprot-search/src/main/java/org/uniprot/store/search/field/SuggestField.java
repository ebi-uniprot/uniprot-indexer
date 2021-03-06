package org.uniprot.store.search.field;

/**
 * Created 13/05/19
 *
 * @author Edd
 */
public class SuggestField {
    public enum Search {
        content,
        id,
        dict
    }

    public enum Stored {
        id,
        value,
        altValue
    }

    public enum Importance {
        highest,
        high,
        medium,
        low
    }
}
