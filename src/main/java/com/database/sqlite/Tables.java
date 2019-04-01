package com.database.sqlite;

import com.database.sqlite.scripts.TableScripts;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Tables implements TableScripts {
    private Connection conn;

    public Tables(Connection conn) {
        this.conn = conn;
    }

    public void createAllTables() {
        executeStatement(cre_tab_external_object);
        executeStatement(cre_tab_external_object_disease);
        executeStatement(cre_tab_term);
        executeStatement(cre_tab_annotation);
        executeStatement(cre_tab_annotation_onset_modifier);
        executeStatement(cre_tab_graph_path);
        executeStatement(cre_tab_term2external_object);
        executeStatement(cre_tab_term2term);
        executeStatement(cre_tab_term_alternative_id);
        executeStatement(cre_tab_term_definition);
        executeStatement(cre_tab_term_synonym);
        System.out.println("All tables created.");
    }

    public void createAllIndexes() {
        executeStatement(cre_uq_idx_external_object);
        executeStatement(cre_uq_idx_external_object_disease);
        System.out.println("All indexes created.");
    }

    private void executeStatement(String stmt) {
        try {
            PreparedStatement ps = this.conn.prepareStatement(stmt);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
