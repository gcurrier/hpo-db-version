package com.database.sqlite;

import hpo.HpoDataProvider;
import ontologizer.ontology.Term;
import ontologizer.ontology.TermID;
import ontologizer.ontology.TermXref;
import ontologizer.util.OntologyConstants;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;

//
//import java.io.*;
//import java.nio.file.Path;
//import java.nio.file.Paths;

public class HpoToSQLiteDBUtil {

    public HpoToSQLiteDBUtil(Connection conn) {
        this.connection = conn;
    }

    private Connection connection;

    private PreparedStatement insertHpoTerm;
    private PreparedStatement selectExternalObjectId;

    private String databaseName;
    private String mySQLPath;

    public void prepLoadStatements() {
        try {
            PreparedStatement ps =  this.connection.prepareStatement("PRAGMA foreign_keys = OFF");
            ps.executeUpdate();
            this.insertHpoTerm = this.connection.prepareStatement("INSERT INTO term ( id, acc, name, is_obsolete, is_root, subontology) VALUES (?,?,?,?,?,?)");
            this.selectExternalObjectId = this.connection.prepareStatement("SELECT id FROM external_object WHERE external_id = ? AND  external_db = ?");
        } catch (SQLException se) {
            se.getMessage();
            se.printStackTrace();
            System.exit(1);
        }
    }

//    /**
//     * Method to register the driver for connection to Mysql-Database.
//     *
//     * @author Sebastian Koehler
//     */
//    private void registerDriver() {
//        try {
//            // EDIT: (MGZ) Glen Currier 2019.03.18: "com.mysql.jdbc.Driver" is deprecated
//            // Class.forName("com.mysql.jdbc.Driver");
//            Class.forName("com.mysql.cj.jdbc.Driver");
//        } catch (ClassNotFoundException cnfe) {
//            System.out.println("Couldn't find the driver!");
//            System.out.println("stack trace:");
//            cnfe.printStackTrace();
//            System.exit(1);
//        }
//    }

//    private void restoreDatabaseToFile(String mysqlHost, String mysqlPort, String mysqlUser, String mysqlUserPW,
//                                       String databaseName, String outFilePath, String mysqlPath) {
//        String mysqlDumpCommand = "mysqldump";
//
//        if (mysqlPath == null)
//            mysqlDumpCommand = mySQLPath + mysqlDumpCommand;
//        else
//            mysqlDumpCommand = mysqlPath + mysqlDumpCommand;
//
//        String[] command = new String[]{
//                mysqlDumpCommand,
//                "--user=" + mysqlUser,
//                "--password=" + mysqlUserPW,
//                "--host=" + mysqlHost,
//                "--port=" + mysqlPort,
//                "--routines",
//                "--no-data",
//                databaseName
//        };
//        System.out.println("Calling: " + Arrays.toString(command));
//        executeDumpToFile(outFilePath, command);
//    }

//    /**
//     * Method that dumps a given database to the given outfile, including routines. NOTE: If tables are empty, no dump file is created.
//     *
//     * @param mysqlHost    hostname
//     * @param mysqlPort    server port
//     * @param mysqlUser    user name
//     * @param mysqlUserPW  password
//     * @param databaseName db name
//     * @param outFilePath  dump file path
//     */
//    public void dumpDatabaseToFile(String mysqlHost, String mysqlPort, String mysqlUser, String mysqlUserPW,
//                            String databaseName, String outFilePath, String mysqlPath) {
//
//        String mysqlDumpCommand = "mysqldump";
//        if (mysqlPath == null)
//            mysqlDumpCommand = mySQLPath + mysqlDumpCommand;
//        else
//            mysqlDumpCommand = mysqlPath + mysqlDumpCommand;
//
//        String[] command = new String[]{
//                mysqlDumpCommand,
//                "--user=" + mysqlUser,
//                "--password=" + mysqlUserPW,
//                "--host=" + mysqlHost,
//                "--port=" + mysqlPort,
//                "--routines",
//                databaseName
//        };
//        if (!tablesHaveRows(connection)) {
//            restoreDatabaseToFile(mysqlHost, mysqlPort, mysqlUser, mysqlUserPW, databaseName, outFilePath, mysqlPath);
//        } else {
//            System.out.println("Calling: " + Arrays.toString(command));
//            executeDumpToFile(outFilePath, command);
//        }
//
//    }

//    /**
//     * Executes a mySQL dump to a file storage location
//     *
//     * @param outFilePath dump file path
//     * @param command     String array containing cli commands
//     */
//    private void executeDumpToFile(String outFilePath, String[] command) {
//        Path path = Paths.get(outFilePath);
//        File dir = new File(path.getParent().toString());
//        if (!dir.exists()) {
//            dir.mkdir();
//        }
//
//        try {
//
//            Process restoreProcess = Runtime.getRuntime().exec(command);
//
//            BufferedReader in = new BufferedReader(new InputStreamReader(restoreProcess.getInputStream()));
//            BufferedReader inErr = new BufferedReader(new InputStreamReader(restoreProcess.getErrorStream()));
//            String text = null;
//            BufferedWriter out = new BufferedWriter(new FileWriter(new File(outFilePath)));
//            while ((text = in.readLine()) != null) {
//                // System.out.println(" OutputStream: "+text);
//                out.write(text);
//                out.write("\n");
//            }
//
//            while ((text = inErr.readLine()) != null) {
//                System.out.println("    " + text);
//            }
//
//            try {
//                restoreProcess.waitFor();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//
//            System.out.println("Done dumping to file.");
//
//            restoreProcess.getInputStream().close();
//            restoreProcess.getOutputStream().close();
//            restoreProcess.getErrorStream().close();
//            out.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

//    private Boolean tablesHaveRows(Connection conn) {
//        boolean hasRows = false;
//        PreparedStatement checkRowCounts = null;
//        ResultSet rs;
//        String stmt = "SELECT TABLE_NAME\n" +
//                "FROM information_schema.tables\n" +
//                "WHERE TABLE_ROWS >= 1 AND TABLE_SCHEMA=?";
//
//        try {
//            checkRowCounts = conn.prepareStatement(stmt);
//            checkRowCounts.setString(1, databaseName);
//            rs = checkRowCounts.executeQuery();
//            if (rs.next()) {
//                hasRows = true;
//            }
//        } catch (SQLException se) {
//            System.out.println();
//            throw new RuntimeException();
//        }
//        return hasRows;
//    }

//    /***
//     * Creates new schema (db) in mySQL server instance using command line interface
//     * @param mysqlHost server hostname
//     * @param mysqlPort server port
//     * @param mysqlUser login username
//     * @param mysqlUserPW login password
//     * @param databaseName server instance database name
//     * @param dbSchemaPath location of schema dump file
//     * @param mysqlPath absolute path to mySQL executable
//     */
//    public void insertSchemaIntoDatabase(String mysqlHost, String mysqlPort, String mysqlUser, String mysqlUserPW,
//                                  String databaseName, String dbSchemaPath, String mysqlPath) {
//        dropDB();
//        createDB();
//        String mysqlCommand = "mysql";
//        if (mysqlPath == null)
//            mysqlCommand = mySQLPath + mysqlCommand;
//        else
//            mysqlCommand = mysqlPath + mysqlCommand;
//
//        String[] command = new String[]{mysqlCommand, "--user=" + mysqlUser, "--password=" + mysqlUserPW,
//                "--host=" + mysqlHost, "--port=" + mysqlPort, databaseName, "-e", "source " + dbSchemaPath};
//
//        try {
//            System.out.println("Execute: " + Arrays.toString(command));
//            Process restoreProcess = Runtime.getRuntime().exec(command);
//            BufferedReader in = new BufferedReader(new InputStreamReader(restoreProcess.getInputStream()));
//            BufferedReader inErr = new BufferedReader(new InputStreamReader(restoreProcess.getErrorStream()));
//            String text;
//
//            while ((text = in.readLine()) != null) {
//                System.out.println("   OutputStream: " + text);
//            }
//            while ((text = inErr.readLine()) != null) {
//                System.out.println("    " + text);
//            }
//
//            try {
//                restoreProcess.waitFor();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//
//            System.out.println(databaseName + " database created.");
//
//            restoreProcess.getInputStream().close();
//            restoreProcess.getOutputStream().close();
//            restoreProcess.getErrorStream().close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }

//    /***
//     * Retrieves the OS Path to the mySQL executable file by querying the database.
//     * @param conn Connection object
//     */
//    private void setMySQLPath(Connection conn) {
//        String OS = System.getProperty("os.name").toLowerCase();
//        String mysqlPath = "";
//        try {
//            Statement stmt = conn.createStatement();
//            ResultSet rs = stmt.executeQuery("select @@basedir");
//            while (rs.next()) {
//                mysqlPath = rs.getString(1);
//            }
//
//            if (OS.contains("win")) {
//                this.mySQLPath = mysqlPath + "\\bin\\";
//            } else {
//                this.mySQLPath = mysqlPath + "/bin/";
//            }
//
//        } catch (SQLException se) {
//            System.out.println("Unable to connect to database");
//            System.out.println("Unable to retrieve MySQL system PATH");
//            se.printStackTrace();
//            System.exit(1);
//        }
////        return this.mySQLPath;
//    }

//    public String getMySQLPath() {
//        return this.mySQLPath;
//    }
//
//    /**
//     * Method to create to mysql-server and create a new database with the given
//     * name. Does not check for existing databases.
//     *
//     * @param hostName     server instance hostname
//     * @param port         server instance port
//     * @param username     login username
//     * @param password     login password
//     * @param databaseName server instance database name
//     */
//    public void makeConnection(String hostName, String port, String username, String password, String databaseName) {
//        registerDriver();
//        this.databaseName = databaseName;
//        try {
//            // connect to "root" mysql-db
//            String connectionString = "jdbc:mysql://" + hostName + ":" + port + "/mysql";
//            System.out.println("Connection: " + connectionString);
//            connection = DriverManager.getConnection(connectionString, username, password);
//            connection.setAutoCommit(true);
//            setMySQLPath(connection);
//        } catch (SQLException se) {
//            System.out.println("Unable to create database " + databaseName);
//            se.printStackTrace();
//            System.exit(1);
//        }
//
//        try {
//            setInsertHpoTerm(connection.prepareStatement(
//                    "INSERT INTO " + databaseName
//                            + ".term ( id, acc, name, is_obsolete, is_root, subontology) " + "VALUES (?,?,?,?,?,?)"));
//            setSelectExternalObjectId(connection.prepareStatement(
//                    "SELECT id FROM " + databaseName + ".external_object WHERE external_id = ? AND  external_db = ?"));
//        } catch (SQLException se) {
//            se.getMessage();
//            se.printStackTrace();
//            System.exit(1);
//        }
//
//    }

//    public void doCommit() {
//        try {
//            connection.commit();
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    public void doRollback() {
//        try {
//            connection.rollback();
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    public void dropDB() {
//        PreparedStatement dropDBstmt;
//        System.out.format("Dropping database: %s (if exists). %n", databaseName);
//        String dropDbSql = String.format("DROP DATABASE %s", databaseName);
//        try {
//            dropDBstmt = connection.prepareStatement(dropDbSql);
//            dropDBstmt.executeUpdate();
//        } catch (SQLException e) {
//            System.out.format("DB %s does not exist. Skipping DROP action. %n", databaseName);
//        }
//    }
//
//    public void createDB() {
//        PreparedStatement createDBstmt;
//        System.out.format("Creating database: %s. %n", this.databaseName);
//        String createDbSQL = String.format("CREATE DATABASE IF NOT EXISTS %s", this.databaseName);
//        try {
//            createDBstmt = connection.prepareStatement(createDbSQL);
//            createDBstmt.executeUpdate();
//        } catch (SQLException se) {
//            System.out.println("Unable to create database " + databaseName);
//            se.printStackTrace();
//            System.exit(1);
//        }
//    }

    /**
     * Insert the term into the corresponding table. Also, the alternative ID's the
     * term's definitions and synonyms are inserted into the corresponding tables.
     *
     * @param term         The Term object
     * @param dataProvider Instance of HPODataProvider
     */
    public void insertHpoTerm(Term term, HpoDataProvider dataProvider) {
        /* HP:00000123 --> 123 */
        int termId = term.getID().id;
        /* true for obsolete terms */
        boolean obsolete = term.isObsolete();
        /* true for the four root terms (all, organ abn., inheritance, onset) */
        boolean isRoot = (termId == 1 || termId == 5 || termId == 4 || termId == 118);

        String subontology;
        if (dataProvider.isOrganAbnormalityTerm(term)
            || OntologyConstants.organAbnormalityRootId.equals(term.getIDAsString()))
            subontology = "P";
        else if (dataProvider.isInheritanceTerm(term)
            || OntologyConstants.inheritanceRootId.equals(term.getIDAsString()))
            subontology = "I";
        else if (dataProvider.isModifierTerm(term) || OntologyConstants.modifierRootId.equals(term.getIDAsString()))
            subontology = "M";
        else if (dataProvider.isClinicalCourseTerm(term)
            || OntologyConstants.clinicalCourseRoot.equals(term.getIDAsString()))
            subontology = "C";
        else if (dataProvider.isFrequencyTerm(term) || OntologyConstants.frequencyRootId.equals(term.getIDAsString()))
            subontology = "F";
        else
            subontology = "A"; // 'A'll
        try {
            int idx = 1;
            insertHpoTerm.setInt(idx++, termId);
            insertHpoTerm.setString(idx++, term.getIDAsString());
            insertHpoTerm.setString(idx++, term.getName());
            insertHpoTerm.setBoolean(idx++, obsolete);
            insertHpoTerm.setBoolean(idx++, isRoot);
            insertHpoTerm.setString(idx++, subontology);
            insertHpoTerm.executeUpdate();
            insertHpoTerm.clearParameters();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        /*
         * insert synonyms
         */
        for (String synonym : term.getSynonymsArrayList()) {
            insertSynonym(termId, synonym.trim());
        }
        /*
         * insert definition
         */
        String definition = term.getDefinition();
        if (definition != null && (!definition.equals(""))) {
            definition = makeSqlCompatible(definition);
            insertDefinition(termId, definition);
        }

        /*
         * insert alternative id's
         */
        // for (String alternativeIdString : term.getAlternativIDs()){
        for (TermID alternativeId : term.getAlternatives()) {
            String alternativeIdString = alternativeId.toString();
            int altIdInt = Integer.parseInt(alternativeIdString.replaceAll("HP:", ""));
            insertAlternativeId(termId, altIdInt);
        }
        /*
         * insert external reference
         */
        if (term.getXrefs() != null) {
            for (TermXref xref : term.getXrefs()) {

                String externalDatabseId = xref.getXrefId();
                String externalDatabase = xref.getDatabase();

                int xrefIdInHpoDb = selectOrInsertExternalObject(externalDatabseId, externalDatabase);

                insertTerm2XrefRelationship(term, xrefIdInHpoDb);
            }
        }
    }

    /**
     * removes escaped double quotes and replaces with non-escaped double quote
     *
     * @param textToInsert text string value to be inserted
     * @return text to insert
     */
    private String makeSqlCompatible(String textToInsert) {

        // remove "hasenfuesse"
        if (textToInsert.contains("\"")) {
            textToInsert = textToInsert.replaceAll("\"", "\\\\\"");
        }

        return textToInsert;
    }

    /**
     * Tries to select an object of the table external-object with the given id and
     * from the specified database. If no object is found the data is inserted into
     * the database.
     *
     * @param externalDatabaseId E.g. C012345
     * @param externalDatabase   E.g. Database.UMLS
     * @return the internal object-id in the HPO database
     */
    private int selectOrInsertExternalObject(String externalDatabaseId, String externalDatabase) {

        // try to select the object
        int xrefIdInHpoDb = getExternalObjectIdInHpoDb(externalDatabaseId, externalDatabase);
        // a -1 indicates that no object could be found
        if (xrefIdInHpoDb < 1) {
            // insert the object into the 'general' external object table
            xrefIdInHpoDb = insertExternalObject(externalDatabaseId, externalDatabase);
            // if this database corresponds to a disease database we insert the entry into external_object_disease
            if (isDiseaseDatabase(externalDatabase)) {
                insertExternalObjectDisease(xrefIdInHpoDb, externalDatabaseId, externalDatabase);
            }
        }
        return xrefIdInHpoDb;
    }

    /***
     * Insert rows into external_object_disease table
     * @param xrefIdInHpoDb external_object_disease.external_object_id
     * @param externalDatabaseId external_object_disease.disease_id
     * @param externalDatabase external_object_disease.db_name
     */
    private void insertExternalObjectDisease(int xrefIdInHpoDb, String externalDatabaseId, String externalDatabase) {
        PreparedStatement ps;
        String stmt = String.format("INSERT INTO %s.external_object_disease ( external_object_id, disease_id, db_name ) " + "VALUES (?, ?, ?)", this.databaseName);
        try {
            ps = this.connection.prepareStatement(stmt);
            ps.setInt(1, xrefIdInHpoDb);
            ps.setString(2, externalDatabaseId);
            ps.setString(3, externalDatabase.toString());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException se) {
            System.err.format("error in insertExternalObjectDisease(" + xrefIdInHpoDb + ", " + externalDatabaseId + ", "
                + externalDatabase + ")");
            throw new RuntimeException(se);
        }

    }

    /**
     * Adds the disease name and longtitle to the corresponding entry in the table
     * external_object_disease
     *
     * @param xrefIdInHpoDb   the internal id of this external-object
     * @param diseaseName     the main title of this disease
     * @param diseaseLongName a longer version of the name
     */
    private void updateDiseaseEntry(int xrefIdInHpoDb, String diseaseName, String diseaseLongName) {
        PreparedStatement ps;
        String stmt = String.format("UPDATE %s.external_object_disease SET disease_title = ?, disease_longtitle = ? WHERE external_object_id = ?", this.databaseName);
        try {
            ps = this.connection.prepareStatement(stmt);
            ps.setString(1, diseaseName);
            ps.setString(2, diseaseLongName);
            ps.setInt(3, xrefIdInHpoDb);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException(String.format("Update Failed: " + stmt.replace("?", "%s") + "%n", diseaseName, diseaseLongName, String.valueOf(xrefIdInHpoDb)));
        }

    }

    /**
     * Insert rows into term2external_object table
     *
     * @param term   term2external_object.term_id
     * @param xrefId term2external_object.external_object_id
     */
    private void insertTerm2XrefRelationship(Term term, int xrefId) {
        int termId = term.getID().id;
        PreparedStatement ps;
        String stmt = String.format("INSERT INTO %s.term2external_object ( term_id, external_object_id ) VALUES (?,?)", this.databaseName);
        try {
            ps = this.connection.prepareStatement(stmt);
            ps.setInt(1, termId);
            ps.setInt(2, xrefId);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException se) {
            se.getMessage();
            se.printStackTrace();
            throw new RuntimeException(String.format("Insert Failed: " + stmt.replace("?", "%s") + "%n", String.valueOf(termId), String.valueOf(xrefId)));
        }

//        String statement = "INSERT INTO " + databaseName + ".term2external_object ( term_id, external_object_id ) "
//                + "VALUES (" + term.getID().id + "," + xrefId + ")";
//        // insert into main table
//        try {
//            Statement stmt = this.connection.createStatement();
//            stmt.executeUpdate(statement);
//            stmt.close();
//        } catch (SQLException e) {
//            throw new RuntimeException("Tried to excecute: " + statement + " --> " + e);
//        }
    }

    /**
     * Insert an external db object into the table external_object. Note that for
     * diseases and/or pubmed entries you should call insertExternalDiseaseObject
     * and/or insertExternalPubmedObject afterwards.
     *
     * @param externalObjectId external_object.external_id
     * @param externalDatabase external_object.external_db
     * @return int
     */
    private int insertExternalObject(String externalObjectId, String externalDatabase) {
        int externalObjectIdInHpoDb = -1;
        PreparedStatement ps;
        String stmt = String.format("INSERT INTO %s.external_object ( external_id, external_db ) " + "VALUES (?, ?)", this.databaseName);
        try {
            ps = this.connection.prepareStatement(stmt, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, externalObjectId);
            ps.setString(2, externalDatabase.toString());
            ps.executeUpdate();
            // get the new key of annotation (auto-incremented by db)
            ResultSet res = ps.getGeneratedKeys();
            if (res.next()) {
                externalObjectIdInHpoDb = res.getInt(1);
            }
            ps.close();
        } catch (SQLException se) {
            System.err.println("error in insertXref(" + externalObjectId + ", " + externalDatabase + ")");
            se.getMessage();
            se.printStackTrace();
            throw new RuntimeException(se);
        }
        // returned id must be positive
        if (externalObjectIdInHpoDb < 1) {
            System.err.format("Something went wrong in insertXref(%s, %s)! Will return -1!", externalObjectId, externalDatabase);
        }
        return externalObjectIdInHpoDb;
    }

    /***
     * Does a select statement on the table 'external_object'. If the object with
     * the specified database and database-id is found, the function returns the ID
     * of this object in HPO-database. Otherwise -1 is returned.
     *
     * @param externDatabaseId external_object.database_id
     * @param externalDatabase external_object.db_name
     * @return int
     */
    private int getExternalObjectIdInHpoDb(String externDatabaseId, String externalDatabase) {
        try {
            this.selectExternalObjectId.setString(1, externDatabaseId);
            this.selectExternalObjectId.setString(2, externalDatabase.toString());

            ResultSet result = selectExternalObjectId.executeQuery();
            if (result.next()) {
                return result.getInt("id");
            }
        } catch (SQLException se) {
            se.getMessage();
            se.printStackTrace();
            System.exit(1);
        }
        return -1;
    }

    private void insertSynonym(int termId, String synonym) {
        PreparedStatement ps;
        String stmt = String.format("INSERT INTO %s.term_synonym ( term_id, term_synonym ) " + "VALUES (?,?)", this.databaseName);
        try {
            ps = this.connection.prepareStatement(stmt);
            ps.setInt(1, termId);
            ps.setString(2, String.format("\"%s\"", synonym));
            ps.executeUpdate();
            ps.close();
        } catch (SQLException se) {
            System.out.println("tried to execute: " + "INSERT INTO " + databaseName
                + ".term_synonym ( term_id, term_synonym ) " + "VALUES (" + termId + ",\"" + synonym + "\")");
            se.getMessage();
            se.printStackTrace();
            throw new RuntimeException(se);
        }

    }

    private void insertDefinition(int termId, String definition) {
        PreparedStatement ps;
        String stmt = String.format("INSERT INTO %s.term_definition ( term_id, term_definition ) VALUES (?,?)", this.databaseName);
        try {
            ps = this.connection.prepareStatement(stmt);
            ps.setInt(1, termId);
            ps.setString(2, String.format("\"%s\"", definition));
            ps.executeUpdate();
            ps.close();
        } catch (SQLException se) {
            se.getMessage();
            se.printStackTrace();
            throw new RuntimeException(se);
        }

    }

    private void insertAlternativeId(int termId, int alternativeId) {
        PreparedStatement ps = null;
        String stmt = String.format("INSERT INTO %s.term_alternative_id ( term_id, alternative_id ) VALUES (?,?)", this.databaseName);
        try {
            ps = this.connection.prepareStatement(stmt);
            ps.setInt(1, termId);
            ps.setInt(2, alternativeId);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException se) {
            se.getMessage();
            se.printStackTrace();
            throw new RuntimeException(se);
        }

    }

    public void insertAllPaths(HashSet<GraphPath> allPaths) {
        PreparedStatement ps;
        String stmt = String.format("INSERT INTO %s.graph_path ( term1_id, term2_id, distance ) VALUES (?,?,?)", this.databaseName);
        for (GraphPath path : allPaths) {
            try {
                ps = this.connection.prepareStatement(stmt);
                ps.setInt(1, path.term1);
                ps.setInt(2, path.term2);
                ps.setInt(3, path.distance);
                ps.executeUpdate();
                ps.close();
            } catch (SQLException se) {
                se.getMessage();
                se.printStackTrace();
                throw new RuntimeException(se);
            }
        }
    }

    public void insertAllTerm2TermRelationships(HpoDataProvider dataProvider) {
        ArrayList<Term> allTerms = new ArrayList<Term>();
        PreparedStatement ps;
        String stmt = String.format("INSERT INTO %s.term2term ( term1_id, term2_id, relationship_type ) VALUES (?,?,?)", this.databaseName);
        for (Term t : dataProvider.getHpo())
            allTerms.add(t);

        for (Term term : allTerms) {
            for (Term child : dataProvider.getHpoSlim().getChildren(term)) {
                if (child.equals(term))
                    continue;
                try {
                    ps = this.connection.prepareStatement(stmt);
                    ps.setInt(1, term.getID().id);
                    ps.setInt(2, child.getID().id);
                    ps.setString(3, "\"is_a\"");
                    ps.executeUpdate();
                    ps.close();
                } catch (SQLException se) {
                    se.getMessage();
                    se.printStackTrace();
                    throw new RuntimeException(se);
                }
            }
        }

    }

    public int insertDisease(String diseaseId, String database, String diseaseName, String diseaseLongName) {
        int xrefIdInHpoDb = selectOrInsertExternalObject(diseaseId, database);
        // update the disease entry (specific class)
        updateDiseaseEntry(xrefIdInHpoDb, diseaseName, diseaseLongName);
        return xrefIdInHpoDb;
    }

    public int insertAnnotation(int diseaseIdInDatabase, int termid, String evidenceCode, boolean doesNotApply, String frequencyModifier, String annotatedBy, Date annotatedDate) {
        int annotationId = -1;
        PreparedStatement ps;
        String stmt = String.format(
            "INSERT INTO %s.annotation ( term_id, external_object_disease_id, evidence_code, is_negative, frequency_modifier, annotated_by, annotated_date ) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)", this.databaseName);
        try {
            ps = this.connection.prepareStatement(stmt, Statement.RETURN_GENERATED_KEYS);
            int idx = 1;
            ps.setInt(idx++, termid);
            ps.setInt(idx++, diseaseIdInDatabase);
            ps.setString(idx++, evidenceCode);
            ps.setBoolean(idx++, doesNotApply);
            ps.setString(idx++, frequencyModifier);
            ps.setString(idx++, annotatedBy);
            ps.setDate(idx++, annotatedDate);
            ps.executeUpdate();
            // get the new key of annotation (auto-incremented by db)
            ResultSet res = ps.getGeneratedKeys();
            res.next();
            annotationId = res.getInt(1);
            ps.close();
        } catch (SQLException se) {
            se.getMessage();
            se.printStackTrace();
            System.out.println("current stmt: " + stmt.toString());
            throw new RuntimeException(se);
        }
        return annotationId;
    }

    public void insertAnnotationOnsetModifier(int annotationId, int termid, String comment) {
        PreparedStatement ps;
        String stmt = String.format("INSERT INTO %s.annotation_onset_modifier (annotation_id, onset_term_id, comment) VALUES (?,?,?)", this.databaseName);
        try {
            ps = this.connection.prepareStatement(stmt);
            ps.setInt(1, annotationId);
            ps.setInt(2, termid);
            ps.setString(3, comment);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException se) {
            se.getMessage();
            se.printStackTrace();
            throw new RuntimeException(se);
        }
    }

    private boolean isDiseaseDatabase(String database) {

        return database.equals("OMIM") || database.equals("DECIPHER") || database.startsWith("ORPHA")
            || database.equals("UMLS") || database.equals("MSH") || database.equals("SNOMEDCT_US")
            || database.equals("MEDDRA") || database.equals("ICD-9") || database.equals("ICD-10")
            || database.equals("ICD-O") || database.equals("MPATH") || database.equals("EPCC")
            || database.equals("DOID") || database.equals("NCIT") || database.equals("Fyler")
            || database.equals("MP");
    }

    private boolean isPubmedDatabase(String database) {
        return database.equals("PUBMED") || database.equalsIgnoreCase("PMID");
    }

    public PreparedStatement getInsertHpoTerm() {
        return insertHpoTerm;
    }

    private void setInsertHpoTerm(PreparedStatement insertHpoTerm) {
        this.insertHpoTerm = insertHpoTerm;
    }

    public PreparedStatement getSelectExternalObjectId() {
        return selectExternalObjectId;
    }

    private void setSelectExternalObjectId(PreparedStatement selectExternalObjectId) {
        this.selectExternalObjectId = selectExternalObjectId;
    }
}
