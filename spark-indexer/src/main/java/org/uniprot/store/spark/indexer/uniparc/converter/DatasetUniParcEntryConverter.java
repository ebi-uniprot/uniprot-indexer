package org.uniprot.store.spark.indexer.uniparc.converter;

import static org.uniprot.store.spark.indexer.util.RowUtils.hasFieldName;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;

import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.ArrayType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;
import org.uniprot.core.Location;
import org.uniprot.core.Property;
import org.uniprot.core.uniparc.*;
import org.uniprot.core.uniparc.builder.InterProGroupBuilder;
import org.uniprot.core.uniparc.builder.SequenceFeatureBuilder;
import org.uniprot.core.uniparc.builder.UniParcDBCrossReferenceBuilder;
import org.uniprot.core.uniparc.builder.UniParcEntryBuilder;
import org.uniprot.core.uniprot.taxonomy.builder.TaxonomyBuilder;
import org.uniprot.store.spark.indexer.util.RowUtils;

/**
 * This class convert XML Row result to a UniParcEntry
 *
 * @author lgonzales
 * @since 2020-02-13
 */
public class DatasetUniParcEntryConverter implements MapFunction<Row, UniParcEntry>, Serializable {

    private static final long serialVersionUID = -510915540437314415L;

    private static final String UNIPROTKB_EXCLUSION = "_UniProtKB_exclusion";
    private static final String ACCESSION = "accession";
    private static final String DB_REFERENCE = "dbReference";
    private static final String SIGNATURE_SEQUENCE_MATCH = "signatureSequenceMatch";
    private static final String SEQUENCE = "sequence";
    private static final String ID = "_id";
    private static final String DATABASE = "_database";
    private static final String IPR = "ipr";
    private static final String LCN = "lcn";
    private static final String NAME = "_name";
    private static final String START = "_start";
    private static final String END = "_end";
    private static final String TYPE = "_type";
    private static final String VERSION_I = "_version_i";
    private static final String ACTIVE = "_active";
    private static final String VERSION = "_version";
    private static final String CREATED = "_created";
    private static final String LAST = "_last";
    private static final String PROPERTY = "property";

    @Override
    public UniParcEntry call(Row rowValue) throws Exception {
        UniParcEntryBuilder builder = new UniParcEntryBuilder();
        if (hasFieldName(UNIPROTKB_EXCLUSION, rowValue)) {
            builder.uniprotExclusionReason(
                    rowValue.getString(rowValue.fieldIndex(UNIPROTKB_EXCLUSION)));
        }
        if (hasFieldName(ACCESSION, rowValue)) {
            builder.uniParcId(rowValue.getString(rowValue.fieldIndex(ACCESSION)));
        }
        if (hasFieldName(DB_REFERENCE, rowValue)) {
            List<Row> dbReferences = rowValue.getList(rowValue.fieldIndex(DB_REFERENCE));
            Set<Long> taxIds = new HashSet<>();
            for (Row dbReference : dbReferences) {
                UniParcDBCrossReference xref = convertDbReference(dbReference);
                builder.databaseCrossReferencesAdd(xref);
                xref.getProperties().stream()
                        .filter(property -> isTaxonomyProperty(property.getKey()))
                        .map(Property::getValue)
                        .map(Long::parseLong)
                        .forEach(taxIds::add);
            }
            taxIds.stream()
                    .map(
                            taxId -> {
                                return new TaxonomyBuilder().taxonId(taxId).build();
                            })
                    .forEach(builder::taxonomiesAdd);
        }
        if (hasFieldName(SIGNATURE_SEQUENCE_MATCH, rowValue)) {
            List<Row> sequenceFeatures =
                    rowValue.getList(rowValue.fieldIndex(SIGNATURE_SEQUENCE_MATCH));
            sequenceFeatures.stream()
                    .map(this::convertSequenceFeatures)
                    .forEach(builder::sequenceFeaturesAdd);
        }
        if (hasFieldName(SEQUENCE, rowValue)) {
            Row sequence = (Row) rowValue.get(rowValue.fieldIndex(SEQUENCE));
            builder.sequence(RowUtils.convertSequence(sequence));
        }
        return builder.build();
    }

    private boolean isTaxonomyProperty(String key) {
        return key.equalsIgnoreCase(UniParcDBCrossReference.PROPERTY_NCBI_TAXONOMY_ID);
    }

    public static StructType getUniParcXMLSchema() {
        StructType structType = new StructType();
        structType = structType.add("_dataset", DataTypes.StringType, true);
        structType = structType.add(UNIPROTKB_EXCLUSION, DataTypes.StringType, true);
        structType = structType.add(ACCESSION, DataTypes.StringType, true);
        ArrayType dbReferences = DataTypes.createArrayType(getDbReferenceSchema());
        structType = structType.add(DB_REFERENCE, dbReferences, true);
        structType = structType.add(SEQUENCE, RowUtils.getSequenceSchema(), true);
        ArrayType signature = DataTypes.createArrayType(getSignatureSchema());
        structType = structType.add(SIGNATURE_SEQUENCE_MATCH, signature, true);

        return structType;
    }

    private SequenceFeature convertSequenceFeatures(Row rowValue) {
        SequenceFeatureBuilder builder = new SequenceFeatureBuilder();
        if (hasFieldName(ID, rowValue)) {
            builder.signatureDbId(rowValue.getString(rowValue.fieldIndex(ID)));
        }
        if (hasFieldName(DATABASE, rowValue)) {
            String database = rowValue.getString(rowValue.fieldIndex(DATABASE));
            builder.signatureDbType(SignatureDbType.typeOf(database));
        }
        if (hasFieldName(IPR, rowValue)) {
            Row interproGroup = (Row) rowValue.get(rowValue.fieldIndex(IPR));
            builder.interproGroup(convertInterproGroup(interproGroup));
        }
        if (hasFieldName(LCN, rowValue)) {
            List<Row> locations = rowValue.getList(rowValue.fieldIndex(LCN));
            locations.stream().map(this::convertLocation).forEach(builder::locationsAdd);
        }
        return builder.build();
    }

    private InterProGroup convertInterproGroup(Row rowValue) {
        InterProGroupBuilder builder = new InterProGroupBuilder();
        if (hasFieldName(ID, rowValue)) {
            builder.id(rowValue.getString(rowValue.fieldIndex(ID)));
        }
        if (hasFieldName(NAME, rowValue)) {
            builder.name(rowValue.getString(rowValue.fieldIndex(NAME)));
        }
        return builder.build();
    }

    private Location convertLocation(Row rowValue) {
        Long start = 0L;
        if (hasFieldName(START, rowValue)) {
            start = rowValue.getLong(rowValue.fieldIndex(START));
        }
        Long end = 0L;
        if (hasFieldName(END, rowValue)) {
            end = rowValue.getLong(rowValue.fieldIndex(END));
        }
        return new Location(start.intValue(), end.intValue());
    }

    private UniParcDBCrossReference convertDbReference(Row rowValue) {
        UniParcDBCrossReferenceBuilder builder = new UniParcDBCrossReferenceBuilder();

        if (hasFieldName(ID, rowValue)) {
            builder.id(rowValue.getString(rowValue.fieldIndex(ID)));
        }
        if (hasFieldName(TYPE, rowValue)) {
            String databaseType = rowValue.getString(rowValue.fieldIndex(TYPE));
            builder.databaseType(UniParcDatabaseType.typeOf(databaseType));
        }
        if (hasFieldName(VERSION_I, rowValue)) {
            builder.versionI((int) rowValue.getLong(rowValue.fieldIndex(VERSION_I)));
        }
        if (hasFieldName(ACTIVE, rowValue)) {
            String active = rowValue.getString(rowValue.fieldIndex(ACTIVE));
            builder.active(active.equalsIgnoreCase("Y"));
        }
        if (hasFieldName(VERSION, rowValue)) {
            builder.version((int) rowValue.getLong(rowValue.fieldIndex(VERSION)));
        }
        DateTimeFormatter formatter =
                new DateTimeFormatterBuilder()
                        .parseCaseInsensitive()
                        .append(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                        .toFormatter();
        if (hasFieldName(CREATED, rowValue)) {
            String created = rowValue.getString(rowValue.fieldIndex(CREATED));
            builder.created(LocalDate.parse(created, formatter));
        }
        if (hasFieldName(LAST, rowValue)) {
            String last = rowValue.getString(rowValue.fieldIndex(LAST));
            builder.lastUpdated(LocalDate.parse(last, formatter));
        }
        if (hasFieldName(PROPERTY, rowValue)) {
            RowUtils.convertProperties(rowValue)
                    .forEach(
                            (key, value) -> {
                                value.forEach(valueItem -> builder.propertiesAdd(key, valueItem));
                            });
        }

        return builder.build();
    }

    static StructType getSignatureSchema() {
        StructType structType = new StructType();

        structType = structType.add(ID, DataTypes.StringType, true);
        structType = structType.add(DATABASE, DataTypes.StringType, true);

        structType = structType.add(IPR, getSeqFeatureGroupSchema(), true);
        ArrayType location = DataTypes.createArrayType(getLocationSchema());
        structType = structType.add(LCN, location, true);
        return structType;
    }

    static StructType getLocationSchema() {
        StructType structType = new StructType();
        structType = structType.add(START, DataTypes.LongType, true);
        structType = structType.add(END, DataTypes.LongType, true);
        return structType;
    }

    static StructType getSeqFeatureGroupSchema() {
        StructType structType = new StructType();
        structType = structType.add(ID, DataTypes.StringType, true);
        structType = structType.add(NAME, DataTypes.StringType, true);
        return structType;
    }

    static StructType getDbReferenceSchema() {
        StructType structType = new StructType();
        structType = structType.add(ID, DataTypes.StringType, true);
        structType = structType.add(TYPE, DataTypes.StringType, true);
        structType = structType.add(VERSION_I, DataTypes.LongType, true);
        structType = structType.add(ACTIVE, DataTypes.StringType, true);
        structType = structType.add(VERSION, DataTypes.LongType, true);
        structType = structType.add(CREATED, DataTypes.StringType, true);
        structType = structType.add(LAST, DataTypes.StringType, true);

        ArrayType property = DataTypes.createArrayType(RowUtils.getPropertySchema());
        structType = structType.add(PROPERTY, property, true);
        return structType;
    }
}
