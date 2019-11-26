package org.uniprot.store.indexer.search.uniref;

import org.uniprot.core.util.Crc64;
import org.uniprot.core.xml.jaxb.uniref.*;
import org.uniprot.core.xml.jaxb.uniref.MemberType.Sequence;
import org.uniprot.core.xml.uniprot.XmlConverterHelper;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * @author jluo
 * @date: 19 Aug 2019
 */
public class TestUtils {
    private static ObjectFactory xmlFactory = new ObjectFactory();

    private TestUtils() {}

    public static Entry createSkeletonEntry(String id, String name) {
        Entry entry = xmlFactory.createEntry();
        entry.setId(id);
        entry.setName(name);
        entry.setUpdated(XmlConverterHelper.dateToXml(LocalDate.now()));
        String sequence = "MPLIYMNIMLAFTISLLGMLVYRSHLMSSLLCLEGMMLSLFIMATLMTLNTHSLLANIVP";
        MemberType reMember =
                createRepresentativeMember(
                        "UniProtKB ID", "P12345_HUMAN", Collections.emptyList(), sequence);
        entry.setRepresentativeMember(reMember);
        return entry;
    }

    public static Sequence createSequence(String value) {
        Sequence sequence = xmlFactory.createMemberTypeSequence();
        sequence.setValue(value);
        sequence.setLength(value.length());
        sequence.setChecksum(Crc64.getCrc64(value));
        return sequence;
    }

    public static MemberType createMember(String type, String id, List<PropertyType> properties) {
        MemberType member = xmlFactory.createMemberType();
        DbReferenceType xref = xmlFactory.createDbReferenceType();
        xref.setType(type);
        xref.setId(id);
        member.setDbReference(xref);
        xref.getProperty().addAll(properties);
        return member;
    }

    public static MemberType createRepresentativeMember(
            String type, String id, List<PropertyType> properties, String sequence) {
        MemberType member = createMember(type, id, properties);
        member.setSequence(createSequence(sequence));
        return member;
    }

    public static PropertyType createProperty(String type, String value) {
        PropertyType property = xmlFactory.createPropertyType();
        property.setType(type);
        property.setValue(value);
        return property;
    }
}
