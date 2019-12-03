package org.uniprot.store.search.domain.impl;

import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.uniprot.store.search.domain.Field;
import org.uniprot.store.search.domain.FieldGroup;
import org.uniprot.store.search.field.ReturnField;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public enum UniProtResultFields implements ReturnField {
    INSTANCE;
    private static final String FILENAME = "uniprot/result_field.json";
    private List<FieldGroup> resultFields = new ArrayList<>();
    private Map<String, Field> fieldMap = new HashMap<>();

    UniProtResultFields() {
        init();
    }

    void init() {
        ObjectMapper objectMapper = JsonConfig.getJsonMapper();
        try (InputStream is =
                UniProtResultFields.class.getClassLoader().getResourceAsStream(FILENAME); ) {
            List<FieldGroupImpl> fields =
                    objectMapper.readValue(is, new TypeReference<List<FieldGroupImpl>>() {});
            this.resultFields.addAll(fields);
            this.fieldMap =
                    this.resultFields.stream()
                            .flatMap(val -> val.getFields().stream())
                            .collect(Collectors.toMap(Field::getName, Function.identity()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<FieldGroup> getDatabaseFields() {
        return Databases.INSTANCE.getDatabaseFields();
    }

    public List<FieldGroup> getResultFields() {
        return resultFields;
    }

    public Optional<Field> getField(String name) {
        Field field = this.fieldMap.get(name);
        if (field == null) return Databases.INSTANCE.getField(name);
        else return Optional.of(field);
    }

    @Override
    public String toString() {
        return resultFields.stream().map(val -> val.toString()).collect(Collectors.joining(",\n"));
    }

    @Override
    public boolean hasReturnField(String fieldName) {
        return INSTANCE.getField(fieldName).isPresent();
    }
}
