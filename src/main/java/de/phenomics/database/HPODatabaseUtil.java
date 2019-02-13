package de.phenomics.database;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import hpo.HpoDataProvider;
import ontologizer.ontology.Term;
import ontologizer.ontology.TermID;
import ontologizer.ontology.TermXref;
import ontologizer.util.OntologyConstants;

public class HPODatabaseUtil {

	private Connection connection = null;

	private PreparedStatement insertHpoTerm;
	private PreparedStatement selectExternalObjectId;

	private String databaseName;

	/**
	 * Method to register the driver for connection to Mysql-Database.
	 * 
	 * @author Sebastian Koehler
	 */
	public void registerDriver() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException cnfe) {
			System.out.println("Couldn't find the driver!");
			System.out.println("stack trace:");
			cnfe.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Method that dumps a given database to the given outfile.
	 * 
	 * @param mysqlHost
	 * @param mysqlPort
	 * @param mysqlUser
	 * @param mysqlUserPW
	 * @param databaseName
	 * @param outFilePath
	 */
	public void dumpDatabaseToFile(String mysqlHost, String mysqlPort, String mysqlUser, String mysqlUserPW,
			String databaseName, String outFilePath, String mysqlPath) {

		String mysqlDumpCommand = "mysqldump";
		if (mysqlPath != null)
			mysqlDumpCommand = mysqlPath + mysqlDumpCommand;

		String[] command = new String[] { mysqlDumpCommand, "--user=" + mysqlUser, "--password=" + mysqlUserPW,
				"--host=" + mysqlHost, "--port=" + mysqlPort, databaseName };
		System.out.println("  calling: " + Arrays.toString(command));
		try {

			Process restoreProcess = Runtime.getRuntime().exec(command);

			BufferedReader in = new BufferedReader(new InputStreamReader(restoreProcess.getInputStream()));
			BufferedReader inErr = new BufferedReader(new InputStreamReader(restoreProcess.getErrorStream()));
			String text = null;
			BufferedWriter out = new BufferedWriter(new FileWriter(new File(outFilePath)));
			while ((text = in.readLine()) != null) {
				// System.out.println(" OutputStream: "+text);
				out.write(text);
				out.write("\n");
			}

			while ((text = inErr.readLine()) != null) {
				System.out.println("   !!!! ErrorStream: " + text);
			}

			try {
				restoreProcess.waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			System.out.println("Done dumping to file.");

			restoreProcess.getInputStream().close();
			restoreProcess.getOutputStream().close();
			restoreProcess.getErrorStream().close();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void insertSchemaIntoDatabase(String mysqlHost, String mysqlPort, String mysqlUser, String mysqlUserPW,
			String databaseName, String dbSchemaPath, String mysqlPath) {

		String mysqlCommand = "mysql";
		if (mysqlPath != null)
			mysqlCommand = mysqlPath + mysqlCommand;

		String[] command = new String[] { mysqlCommand, "--user=" + mysqlUser, "--password=" + mysqlUserPW,
				"--host=" + mysqlHost, "--port=" + mysqlPort, databaseName, "-e", "source " + dbSchemaPath };

		try {
			System.out.println("Execute: " + Arrays.toString(command));
			Process restoreProcess = Runtime.getRuntime().exec(command);
			BufferedReader in = new BufferedReader(new InputStreamReader(restoreProcess.getInputStream()));
			BufferedReader inErr = new BufferedReader(new InputStreamReader(restoreProcess.getErrorStream()));
			String text = null;

			while ((text = in.readLine()) != null) {
				System.out.println("   OutputStream: " + text);
			}
			while ((text = inErr.readLine()) != null) {
				System.out.println("   !!!! ErrorStream: " + text);
			}

			try {
				restoreProcess.waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			System.out.println("Done restore.");

			restoreProcess.getInputStream().close();
			restoreProcess.getOutputStream().close();
			restoreProcess.getErrorStream().close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Method to create to mysql-server and create a new database with the given
	 * name. No check for existing databases!!
	 * 
	 * @param databaseName
	 */
	public void makeConnection(String hostName, String port, String username, String password, String databaseName) {

		/*
		 * Connection
		 */
		registerDriver();
		this.databaseName = databaseName;

		try {

			// connect to "root" mysql-db
			String connectionString = "jdbc:mysql://" + hostName + ":" + port + "/mysql";
			System.out.println("connecting to " + connectionString);
			connection = DriverManager.getConnection(connectionString, username, password);
			connection.setAutoCommit(true);

			// dropping database if exists+
			System.out.println("dropping database: " + databaseName + " (if exists)");
			Statement dropDBstmt = connection.createStatement();
			String dropDBsql = "DROP DATABASE " + databaseName;
			try {
				dropDBstmt.executeUpdate(dropDBsql);
			} catch (SQLException e) {
				System.out.println(" !!! seems that db didn't exist... catched SQL exception... let's continue");
			}

			// create a new database with the provided name
			System.out.println("creating database: " + databaseName);
			Statement createDBstmt = connection.createStatement();
			String createDbSQL = "CREATE DATABASE IF NOT EXISTS " + databaseName;
			createDBstmt.executeUpdate(createDbSQL);
		} catch (SQLException se) {
			System.out.println("Couldn't connect: print out a stack trace and exit.");
			se.printStackTrace();
			System.exit(1);
		}

		/*
		 * Prepare some statements
		 */
		try {
			this.insertHpoTerm = connection.prepareStatement("INSERT INTO " + databaseName
					+ ".term ( id, acc, name, is_obsolete, is_root, subontology) " + "VALUES (?,?,?,?,?,?)");
			this.selectExternalObjectId = connection.prepareStatement(
					"SELECT id FROM " + databaseName + ".external_object WHERE external_id = ? AND  external_db = ?");
		} catch (SQLException se) {
			System.out.println("Couldn't connect: print out a stack trace and exit.");
			se.printStackTrace();
			System.exit(1);
		}

	}

	public void doCommit() {
		try {
			connection.commit();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public void doRollback() {
		try {
			connection.rollback();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Insert the term into the corresponding table. Also, the alternative ID's the
	 * term's definitions and synonyms are inserted into the corresponding tables.
	 * 
	 * @param term
	 * @param dataProvider
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
	 * @param textToInsert
	 * @return
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
	 * @param externalDatabaseId
	 *            E.g. C012345
	 * @param externalDatabase
	 *            E.g. Database.UMLS
	 * @return the internal object-id in the HPO database
	 * @see Database
	 */
	private int selectOrInsertExternalObject(String externalDatabaseId, String externalDatabase) {

		// try to select the object
		int xrefIdInHpoDb = getExternalObjectIdInHpoDb(externalDatabaseId, externalDatabase);

		// a -1 indicates that no object could be found
		if (xrefIdInHpoDb < 1) {

			// insert the object into the 'general' external object table
			xrefIdInHpoDb = insertExternalObject(externalDatabaseId, externalDatabase);

			// if this database corresponds to a disease database we insert the
			// entry into external_object_disease
			if (isDiseaseDatabase(externalDatabase)) {
				insertExternalObjectDisease(xrefIdInHpoDb, externalDatabaseId, externalDatabase);
			}
		}
		return xrefIdInHpoDb;
	}

	private void insertExternalObjectDisease(int xrefIdInHpoDb, String externalDatabseId, String externalDatabase) {
		// insert into main table
		try {

			PreparedStatement stmt = this.connection.prepareStatement("INSERT INTO " + databaseName
					+ ".external_object_disease ( external_object_id, disease_id, db_name ) " + "VALUES ( ? , ? , ? )");

			stmt.setInt(1, xrefIdInHpoDb);
			stmt.setString(2, externalDatabseId);
			stmt.setString(3, externalDatabase.toString());
			// insert
			stmt.executeUpdate();

			stmt.close();

		} catch (SQLException e) {
			System.err.println("error in insertExternalObjectDisease(" + xrefIdInHpoDb + ", " + externalDatabseId + ", "
					+ externalDatabase + ")");
			throw new RuntimeException(e);
		}

	}

	/**
	 * Adds the disease name and longtitle to the corresponding entry in the table
	 * external_object_disease
	 * 
	 * @param xrefIdInHpoDb
	 *            the internal id of this external-object
	 * @param diseaseName
	 *            the main title of this disease
	 * @param diseaseLongName
	 *            a longer version of the name
	 */
	private void updateDiseaseEntry(int xrefIdInHpoDb, String diseaseName, String diseaseLongName) {

		try {

			PreparedStatement stmt = this.connection
					.prepareStatement("UPDATE " + databaseName + ".external_object_disease SET " + "disease_title = ?, "
							+ "disease_longtitle=? " + " WHERE external_object_id = ?");

			stmt.setString(1, diseaseName);
			stmt.setString(2, diseaseLongName);
			stmt.setInt(3, xrefIdInHpoDb);
			// insert
			stmt.executeUpdate();

			stmt.close();

		} catch (SQLException e) {
			throw new RuntimeException("Error when trying to insert DiseaseEntry( " + xrefIdInHpoDb + ", " + diseaseName
					+ ", " + diseaseLongName + "\n\n" + e);
		}

	}

	/**
	 * @param term
	 * @param xrefId
	 */
	private void insertTerm2XrefRelationship(Term term, int xrefId) {
		String statement = "INSERT INTO " + databaseName + ".term2external_object ( term_id, external_object_id ) "
				+ "VALUES (" + term.getID().id + "," + xrefId + ")";
		// insert into main table
		try {
			Statement stmt = this.connection.createStatement();
			stmt.executeUpdate(statement);
			stmt.close();
		} catch (SQLException e) {
			throw new RuntimeException("Tried to excecute: " + statement + " --> " + e);
		}
	}

	/**
	 * Insert an external db object into the table external_object. Note that for
	 * diseases and/or pubmed entries you should call insertExternalDiseaseObject
	 * and/or insertExternalPubmedObject afterwards.
	 * 
	 * @param externalObjectId
	 * @param externalDatabase
	 * @return
	 */
	private int insertExternalObject(String externalObjectId, String externalDatabase) {

		int externalObjectIdInHpoDb = -1;

		// insert into main table
		try {

			PreparedStatement stmt = this.connection.prepareStatement("INSERT INTO " + databaseName
					+ ".external_object ( external_id, external_db ) " + "VALUES ( ? , ? )",
					Statement.RETURN_GENERATED_KEYS);

			stmt.setString(1, externalObjectId);
			stmt.setString(2, externalDatabase.toString());
			// insert
			stmt.executeUpdate();

			// get the new key of annotation (auto-incremented by db)
			ResultSet res = stmt.getGeneratedKeys();
			if (res.next()) {
				externalObjectIdInHpoDb = res.getInt(1);
			}

			stmt.close();

		} catch (SQLException e) {
			System.err.println("error in insertXref(" + externalObjectId + ", " + externalDatabase + ")");
			throw new RuntimeException(e);
		}

		// returned id must be positive
		if (externalObjectIdInHpoDb < 1) {
			System.err.println("something went wrong in insertXref(" + externalObjectId + ", " + externalDatabase
					+ ")! will return -1!");
		}

		return externalObjectIdInHpoDb;
	}

	/**
	 * Does a select statement on the table 'external_object'. If the object with
	 * the specified datbase and database-id is found, the function returns the ID
	 * of this object in HPO-database. Otherwise -1 is returned.
	 * 
	 * @param xref
	 * @return
	 */
	private int getExternalObjectIdInHpoDb(String externDatabaseId, String externalDatabase) {
		try {
			this.selectExternalObjectId.setString(1, externDatabaseId);
			this.selectExternalObjectId.setString(2, externalDatabase.toString());

			ResultSet result = selectExternalObjectId.executeQuery();
			if (result.next()) {
				return result.getInt("id");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return -1;
	}

	private void insertSynonym(int termId, String synonym) {

		try {
			Statement stmt = this.connection.createStatement();
			stmt.executeUpdate("INSERT INTO " + databaseName + ".term_synonym ( term_id, term_synonym ) " + "VALUES ("
					+ termId + ",\"" + synonym + "\")");
			stmt.close();
		} catch (SQLException e) {
			System.out.println("tried to execute: " + "INSERT INTO " + databaseName
					+ ".term_synonym ( term_id, term_synonym ) " + "VALUES (" + termId + ",\"" + synonym + "\")");
			throw new RuntimeException(e);
		}

	}

	private void insertDefinition(int termId, String definition) {

		try {
			Statement stmt = this.connection.createStatement();

			stmt.executeUpdate("INSERT INTO " + databaseName + ".term_definition ( term_id, term_definition ) "
					+ "VALUES (" + termId + ",\"" + definition + "\")");
			stmt.close();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

	}

	private void insertAlternativeId(int termId, int alternativeId) {

		try {
			Statement stmt = this.connection.createStatement();
			stmt.executeUpdate("INSERT INTO " + databaseName + ".term_alternative_id ( term_id, alternative_id ) "
					+ "VALUES (" + termId + "," + alternativeId + ")");
			stmt.close();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

	}

	public void insertAllPathes(HashSet<GraphPath> allPathes) {

		for (GraphPath path : allPathes) {
			try {
				Statement stmt = this.connection.createStatement();
				stmt.executeUpdate("INSERT INTO " + databaseName + ".graph_path ( term1_id, term2_id, distance ) "
						+ "VALUES (" + path.term1 + ", " + path.term2 + "," + path.distance + ")");
				stmt.close();
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}

	}

	public void insertAllTerm2TermRelationships(HpoDataProvider dataProvider) {

		ArrayList<Term> allTerms = new ArrayList<Term>();
		for (Term t : dataProvider.getHpo())
			allTerms.add(t);

		for (Term term : allTerms) {

			for (Term child : dataProvider.getHpoSlim().getChildren(term)) {

				if (child.equals(term))
					continue;

				try {
					Statement stmt = this.connection.createStatement();
					stmt.executeUpdate(
							"INSERT INTO " + databaseName + ".term2term ( term1_id, term2_id, relationship_type ) "
									+ "VALUES (" + term.getID().id + ", " + child.getID().id + ",\"is_a\")");
					stmt.close();
				} catch (SQLException e) {
					throw new RuntimeException(e);
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

	public int insertAnnotation(int diseaseIdInDatabase, int termid, String evidenceCode, boolean doesNotApply,
			String frequencyModifier, String annotatedBy, Date annotatedDate) {

		int annotationId = -1;
		PreparedStatement stmt = null;
		try {
			stmt = this.connection.prepareStatement("INSERT INTO " + databaseName
					+ ".annotation ( term_id, external_object_disease_id, evidence_code, is_negative, frequency_modifier, annotated_by, annotated_date ) "
					+ "VALUES ( ?, ?, ?, ?, ?, ?,?)", Statement.RETURN_GENERATED_KEYS);
			// set values
			int idx = 1;
			stmt.setInt(idx++, termid);
			stmt.setInt(idx++, diseaseIdInDatabase);
			stmt.setString(idx++, evidenceCode);
			stmt.setBoolean(idx++, doesNotApply);
			stmt.setString(idx++, frequencyModifier);
			stmt.setString(idx++, annotatedBy);
			stmt.setDate(idx++, annotatedDate);

			// insert
			stmt.executeUpdate();

			// get the new key of annotation (auto-incremented by db)
			ResultSet res = stmt.getGeneratedKeys();
			res.next();
			annotationId = res.getInt(1);
			stmt.close();
		} catch (SQLException e) {
			System.out.println("current stmt: " + stmt.toString());
			throw new RuntimeException(e);
		}

		return annotationId;
	}

	public void insertAnnotationOnsetModifier(int annotationId, int termid, String comment) {
		try {
			Statement stmt = this.connection.createStatement();
			if (comment == null || comment.equals("")) {
				stmt.executeUpdate(
						"INSERT INTO " + databaseName + ".annotation_onset_modifier ( annotation_id, onset_term_id ) "
								+ "VALUES (" + annotationId + ", " + termid + ")");
			}
			else {
				stmt.executeUpdate("INSERT INTO " + databaseName
						+ ".annotation_onset_modifier ( annotation_id, onset_term_id, comment ) " + "VALUES ("
						+ annotationId + ", " + termid + ", \"" + comment + "\")");
			}

			stmt.close();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private boolean isDiseaseDatabase(String database) {

		if (database.equals("OMIM") || database.equals("DECIPHER") || database.startsWith("ORPHA")
				|| database.equals("UMLS") || database.equals("MSH") || database.equals("SNOMEDCT_US")
				|| database.equals("MEDDRA") || database.equals("ICD-9") || database.equals("ICD-10")
				|| database.equals("ICD-O") || database.equals("MPATH") || database.equals("EPCC")
				|| database.equals("DOID") || database.equals("NCIT") || database.equals("Fyler")
				|| database.equals("MP")) {
			return true;
		}

		return false;

	}

	private boolean isPubmedDatabase(String database) {

		if (database.equals("PUBMED") || database.equalsIgnoreCase("PMID"))
			return true;

		return false;

	}

}
