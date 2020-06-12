CREATE TABLE TAXONOMY.V_PUBLIC_NODE
(
     TAX_ID INTEGER,
     PARENT_ID INTEGER,
     HIDDEN INTEGER,
     INTERNAL VARCHAR(64),
     RANK VARCHAR(64),
     GC_ID VARCHAR(64),
     MGC_ID VARCHAR(64),
     NCBI_SCIENTIFIC VARCHAR(64),
     NCBI_COMMON VARCHAR(64),
     SPTR_SCIENTIFIC VARCHAR(64),
     SPTR_COMMON VARCHAR(64),
     SPTR_SYNONYM VARCHAR(64),
     SPTR_CODE VARCHAR(64),
     TAX_CODE VARCHAR(64),
     SPTR_FF VARCHAR(64),
     SUPERREGNUM VARCHAR(64),
     SPTR_XML VARCHAR(256),
     TREE_LEFT INTEGER,
     TREE_RIGHT INTEGER
);
