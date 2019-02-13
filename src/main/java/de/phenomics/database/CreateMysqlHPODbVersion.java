package de.phenomics.database;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import hpo.Annotation;
import hpo.HpoDataProvider;
import hpo.Item;
import ontologizer.ontology.Term;

public class CreateMysqlHPODbVersion {

	/**
	 * A program to create a mysql version of the HPO and its annotations.
	 * 
	 * 
	 * @param args
	 * @throws ParseException
	 */
	public static void main(String[] args) throws ParseException {

		// configure command line parser
		final CommandLineParser commandLineParser = new BasicParser();
		HelpFormatter formatter = new HelpFormatter();
		final Options options = new Options();

		// the folder to the NCBI-data
		Option extDataOpt = new Option("d", "data", true, "Path to folder with the data.");
		options.addOption(extDataOpt);

		// the folder where the output is written to...
		Option outOpt = new Option("o", "out-folder", true, "The folder where the database-dump should be written to.");
		options.addOption(outOpt);

		// the file where the DB schema is located
		Option schemaOpt = new Option("b", "database-schema", true,
				"The file where the DB schema (sql-file) is located.");
		options.addOption(schemaOpt);

		// //// mysql options //////
		// the username that has access to the mysql server
		Option mysqlUserOpt = new Option("u", "mysql-user", true, "The username that has access to the mysql server.");
		options.addOption(mysqlUserOpt);

		// the password for the user of the mysql installation
		Option mysqlUserPwOpt = new Option("w", "mysql-password", true,
				"The password for the user of the mysql installation.");
		options.addOption(mysqlUserPwOpt);

		// the host of the mysql installation
		Option mysqlHostOpt = new Option("s", "mysql-host", true, "The hostname of your mysql server.");
		options.addOption(mysqlHostOpt);

		// the port of the mysql installation
		Option mysqlPortOpt = new Option("t", "mysql-port", true, "The port number of your mysql server.");
		options.addOption(mysqlPortOpt);

		// the path to mysql-commands if not in PATH variable
		Option mysqlPathOpt = new Option("a", "mysql-path", true,
				"The to your mysql-commands if they are not in the PATH variable.");
		options.addOption(mysqlPathOpt);

		/*
		 * parse the command line
		 */
		final CommandLine commandLine = commandLineParser.parse(options, args);

		// get the pathes to files
		String outfolder = getOption(outOpt, commandLine);
		String externalDataPath = getOption(extDataOpt, commandLine);
		// get the mysql parameters
		String mysqlHost = getOption(mysqlHostOpt, commandLine);
		String mysqlPort = getOption(mysqlPortOpt, commandLine);
		String mysqlUser = getOption(mysqlUserOpt, commandLine);
		String mysqlUserPW = getOption(mysqlUserPwOpt, commandLine);

		String mysqlPath = getOption(mysqlPathOpt, commandLine);
		String dbSchemaPath = getOption(schemaOpt, commandLine);

		// check that required parameters are set
		String parameterError = null;
		if (externalDataPath == null) {
			parameterError = "Please provide a folder where the data is located using the option " + extDataOpt.getOpt()
					+ " or " + extDataOpt.getLongOpt();
		}
		else if (outfolder == null) {
			parameterError = "Please provide an outfolder using the option " + outOpt.getOpt() + " or "
					+ outOpt.getLongOpt();
		}
		else if (dbSchemaPath == null) {
			parameterError = "Please provide a path to the sql file of the database schema using the option "
					+ schemaOpt.getOpt() + " or " + schemaOpt.getLongOpt();
		}
		else if (mysqlHost == null) {
			parameterError = "Please provide the mysql host name using the option " + mysqlHostOpt.getOpt() + " or "
					+ mysqlHostOpt.getLongOpt();
		}
		else if (mysqlPort == null) {
			parameterError = "Please provide the mysql host port using the option " + mysqlPortOpt.getOpt() + " or "
					+ mysqlPortOpt.getLongOpt();
		}
		else if (mysqlUser == null) {
			parameterError = "Please provide the mysql user name using the option " + mysqlUserOpt.getOpt() + " or "
					+ mysqlUserOpt.getLongOpt();
		}
		else if (mysqlUserPW == null) {
			parameterError = "Please provide the mysql user password using the option " + mysqlUserPwOpt.getOpt()
					+ " or " + mysqlUserPwOpt.getLongOpt();
		}

		/*
		 * Maybe something was wrong with the parameter. Print help for the user and die
		 * here...
		 */
		if (parameterError != null) {
			String className = CreateMysqlHPODbVersion.class.getSimpleName();

			formatter.printHelp(className, options);
			throw new IllegalArgumentException(parameterError);
		}

		/*
		 * -------------- LOAD DATA -----------
		 */
		System.out.println("loading ontology and annotations");

		String oboFile = externalDataPath + "/hp.obo";
		String annotationFile = externalDataPath + "/phenotype_annotation.tab";

		System.out.println("loading: " + oboFile);

		HpoDataProvider dataProvider = new HpoDataProvider();
		dataProvider.setOboFile(oboFile);
		dataProvider.setAnnotationFile(annotationFile);
		dataProvider.parseOntologyAndAssociations();

		HPODatabaseUtil dbUtils = new HPODatabaseUtil();

		// generate the month/year representation of this release
		DateFormat df = new SimpleDateFormat("MM_yyyy");
		java.util.Date today = Calendar.getInstance().getTime();
		String reportDate = df.format(today);
		System.out.println(reportDate);
		String databaseName = "MYHPO_" + reportDate;

		System.out.println("make connection / created db");
		dbUtils.makeConnection(mysqlHost, mysqlPort, mysqlUser, mysqlUserPW, databaseName);
		System.out.println("create schema (tables, relations)");
		dbUtils.insertSchemaIntoDatabase(mysqlHost, mysqlPort, mysqlUser, mysqlUserPW, databaseName, dbSchemaPath,
				mysqlPath);

		/*
		 * -------------- H P O -----------
		 */
		// insert all terms
		System.out.println("insert terms");
		for (Term t : dataProvider.getHpo()) {
			dbUtils.insertHpoTerm(t, dataProvider);
		}

		// fill graph_path table
		System.out.println("insert graph-path");
		HashSet<GraphPath> allPathes = getAllUniqueDistPathes(dataProvider);
		dbUtils.insertAllPathes(allPathes);
		System.out.println("  allpathes.size() = " + allPathes.size());

		// fill term2term
		System.out.println("insert term2term");
		dbUtils.insertAllTerm2TermRelationships(dataProvider);

		System.out.println("Insert annotations:");
		/*
		 * -------------- ANNOTATIONS -----------
		 */
		// insert all diseases annotated with hpo-terms
		for (Item diseaseEntry : dataProvider.getDiseaseId2entry().values()) {

			int diseaseIdInDatabase = dbUtils.insertDisease(diseaseEntry.getDiseaseId().getDiseaseIdInDb(),
					diseaseEntry.getDiseaseId().getDiseaseDb().toString(), diseaseEntry.getName(),
					diseaseEntry.getAlternativeNamesAsString());

			// insert the annotations of the disease
			ArrayList<Annotation> annotations = diseaseEntry.getAnnotations();
			for (Annotation annotation : annotations) {
				int termid = annotation.getAnnotatedTerm().getID().id;
				String evidenceCode = annotation.getEvidenceCode().toString();
				boolean doesNotApply = annotation.getDoesNotApplyModifier();
				String frequencyModifier = annotation.getFrequencyModifierHpoCode();
				String annotatedBy = annotation.getAssignedBy();

				java.sql.Date date = annotation.getDateCreated();

				int annotationId = dbUtils.insertAnnotation(diseaseIdInDatabase, termid, evidenceCode, doesNotApply,
						frequencyModifier, annotatedBy, date);

				if (annotation.getOnsetModifier() != null) {
					Term onsetModifier = annotation.getOnsetModifier();

					dbUtils.insertAnnotationOnsetModifier(annotationId, onsetModifier.getID().id, null);

				}
			}
		}

		String outFilePath = outfolder + "/" + databaseName + ".sql";
		System.out.println("dumping to file: " + outFilePath);
		dbUtils.dumpDatabaseToFile(mysqlHost, mysqlPort, mysqlUser, mysqlUserPW, databaseName, outFilePath, mysqlPath);

		System.out.println("exiting...");

	}

	private static HashSet<GraphPath> getAllUniqueDistPathes(HpoDataProvider dataProvider) {
		Term root = dataProvider.getHpo().getRootTerm();
		ArrayList<Term> currentPath = new ArrayList<Term>();
		currentPath.add(root);
		HashSet<GraphPath> allPathes = new HashSet<GraphPath>();
		recursiveGetAllPathes(root, currentPath, allPathes, dataProvider);

		// just a quick fix... we missed the term->same term with distance 0
		// stuff
		for (Term t : dataProvider.getHpo()) {
			GraphPath path = new GraphPath(t.getID().id, t.getID().id, 0);
			allPathes.add(path);
		}

		return allPathes;
	}

	private static void recursiveGetAllPathes(Term currentTerm, ArrayList<Term> currentPath,
			HashSet<GraphPath> allPathes, HpoDataProvider dataProvider) {

		boolean verbose = false;

		int maxPathSize = currentPath.size() - 1;
		Term term2 = currentPath.get(maxPathSize);

		if (verbose) {
			System.out.println("currentTerm : " + currentTerm);
			System.out.println("currentPath: " + currentPath);
			System.out.println("maxPathSize: " + maxPathSize);
		}

		for (int i = 0; i < maxPathSize; i++) {

			Term term1 = currentPath.get(i);
			int distance = maxPathSize - i;

			GraphPath path = new GraphPath(term1.getID().id, term2.getID().id, distance);

			if (verbose)
				System.out.println("path is : " + path);

			if (!allPathes.contains(path)) {
				allPathes.add(path);
				if (verbose)
					System.out.println("added as new path");
			}
			else {
				if (verbose)
					System.out.println("path already contained");
			}

		}

		for (Term child : dataProvider.getHpoSlim().getChildren(currentTerm)) {
			if (child.equals(currentTerm))
				continue;
			currentPath.add(child);
			recursiveGetAllPathes(child, currentPath, allPathes, dataProvider);
			currentPath.remove(currentPath.size() - 1);
		}

	}

	public static String getOption(Option opt, final CommandLine commandLine) {

		if (commandLine.hasOption(opt.getOpt())) {
			return commandLine.getOptionValue(opt.getOpt());
		}
		if (commandLine.hasOption(opt.getLongOpt())) {
			return commandLine.getOptionValue(opt.getLongOpt());
		}
		return null;
	}

}
