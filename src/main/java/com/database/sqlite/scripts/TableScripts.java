package com.database.sqlite.scripts;

public interface TableScripts {

    String cre_tab_external_object =
        "CREATE TABLE if not exists external_object (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "external_id varchar(100) NOT NULL," +
            "external_db varchar(100) NOT NULL" +
            ")";

    String cre_uq_idx_external_object =
        "create unique index if not exists uq_external_obj  on external_object (external_id, external_db)";

    String cre_tab_external_object_disease =
        "CREATE TABLE if not exists external_object_disease (" +
            "external_object_id integer PRIMARY KEY NOT NULL," +
            "disease_id varchar(20) NOT NULL," +
            "db_name varchar(20) NOT NULL," +
            "disease_title varchar(1000) DEFAULT NULL," +
            "disease_longtitle varchar(3000) DEFAULT NULL," +
            "CONSTRAINT fk_disease_ext_object FOREIGN KEY (external_object_id) REFERENCES external_object (id)" +
            ")";

    String cre_uq_idx_external_object_disease =
        "create unique index if not exists uq_disease  on external_object_disease (disease_id,db_name)";

    String cre_tab_term =
        "CREATE TABLE if not exists term (" +
            "id integer PRIMARY KEY NOT NULL," +
            "name varchar(1000) NOT NULL," +
            "is_obsolete integer NOT NULL," +
            "is_root integer NOT NULL," +
            "subontology varchar(1) NOT NULL," +
            "comment text," +
            "acc varchar(10) NOT NULL" +
            ")";

    String cre_tab_annotation =
        "CREATE TABLE if not exists annotation (" +
            "term_id int NOT NULL," +
            "external_object_disease_id int NOT NULL," +
            "evidence_code varchar(5) NOT NULL," +
            "is_negative int DEFAULT 0," +
            "frequency_modifier varchar(20) DEFAULT NULL," +
            "annotation_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "annotated_date date NOT NULL," +
            "annotated_by varchar(100) NOT NULL," +
            "comment text," +
            "CONSTRAINT fk_annotation_disease_id FOREIGN KEY (external_object_disease_id) REFERENCES external_object_disease (external_object_id)," +
            "CONSTRAINT fk_annotation_termid FOREIGN KEY (term_id) REFERENCES term (id)" +
            ")";

    String cre_tab_annotation_onset_modifier =
        "CREATE TABLE if not exists annotation_onset_modifier (" +
            "annotation_id integer NOT NULL," +
            "onset_term_id integer NOT NULL," +
            "comment text," +
            "PRIMARY KEY (annotation_id,onset_term_id)," +
            "CONSTRAINT fk_onset_modifier_annotationid FOREIGN KEY (annotation_id) REFERENCES annotation (annotation_id)," +
            "CONSTRAINT fk_onset_modifier_onsetterm FOREIGN KEY (onset_term_id) REFERENCES term (id)" +
            ")";
    
    String cre_tab_graph_path = 
        "CREATE TABLE if not exists term (" +
            "  id INTEGER PRIMARY KEY NOT NULL," +
            "  name varchar(1000) NOT NULL," +
            "  is_obsolete integer NOT NULL," +
            "  is_root integer NOT NULL," +
            "  subontology varchar(1) NOT NULL," +
            "  comment text" +
            "  acc varchar(10) NOT NULL" +
            ")";
    
    String cre_tab_term2external_object = 
        "CREATE TABLE if not exists term2external_object (" +
            "  term_id integer NOT NULL," +
            "  external_object_id integer NOT NULL," +
            "  PRIMARY KEY (term_id,external_object_id)," +
            "  CONSTRAINT fk_term2external_ext_id FOREIGN KEY (external_object_id) REFERENCES external_object (id)," +
            "  CONSTRAINT fk_term2external_termid FOREIGN KEY (term_id) REFERENCES term (id)" +
            ")";
    
    String cre_tab_term2term = 
        "CREATE TABLE if not exists term2term (" +
            "  term1_id integer NOT NULL," +
            "  term2_id integer NOT NULL," +
            "  relationship_type varchar(10) NOT NULL," +
            "  PRIMARY KEY (term1_id,term2_id)," +
            "  CONSTRAINT fk_term2term_term1 FOREIGN KEY (term1_id) REFERENCES term (id)," +
            "  CONSTRAINT fk_term2term_term2 FOREIGN KEY (term2_id) REFERENCES term (id)" +
            ")";
    
    String cre_tab_term_alternative_id = 
        "CREATE TABLE if not exists term_alternative_id (" +
            "  term_id integer NOT NULL," +
            "  alternative_id integer NOT NULL," +
            "  CONSTRAINT fk_alternativeids_termid FOREIGN KEY (term_id) REFERENCES term (id)" +
            ")";
    
    String cre_tab_term_definition = 
        "CREATE TABLE if not exists term_definition (" +
            "  term_id integer NOT NULL," +
            "  term_definition text NOT NULL," +
            "  CONSTRAINT fk_termdefinition_term_id FOREIGN KEY (term_id) REFERENCES term (id)" +
            ")";
    
    String cre_tab_term_synonym = 
        "CREATE TABLE if not exists term_synonym (" +
            "  term_id integer NOT NULL," +
            "  term_synonym varchar(1000) NOT NULL," +
            "  id integer PRIMARY KEY AUTOINCREMENT," +
            "  CONSTRAINT fk_termid_synonym FOREIGN KEY (term_id) REFERENCES term (id)" +
            ")";
}
