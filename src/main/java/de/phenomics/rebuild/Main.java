package de.phenomics.rebuild;

import de.phenomics.database.GraphPath;
import de.phenomics.database.HPODatabaseUtil;
import hpo.Annotation;
import hpo.HpoDataProvider;
import hpo.Item;
import ontologizer.ontology.Term;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class Main {

    /**
     * A program to create a mysql version of the HPO and its annotations.
     *
     * @param args Command line inputs args
     */
    public static void main(String[] args) throws org.apache.commons.cli.ParseException {

        Instant startTime = Instant.now();

        // configure command line parser
        final CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        final Options options = new Options();
        HashMap<String, String> paramMap = new HashMap<>();

        Option o_dbType = new Option("d", "db-type", true, "The type of database (mysql/mssql)");
        Option o_dbHpoRebuild = new Option("r", "rebuild-db", false, "Rebuild hpo database");
        Option o_dbCustomRebuild = new Option("c", "rebuild-custom-db", false, "Rebuild custom dataset");
        Option o_debug = new Option("b", "debug", false, "Debug output (show all parameters)");
        Option o_help = new Option("h", "help", false, "Display this help message and exit");

        options.addOption(o_dbType);
        options.addOption(o_dbHpoRebuild);
        options.addOption(o_dbCustomRebuild);
        options.addOption(o_debug);
        options.addOption(o_help);

        //parse the command line
        final CommandLine line = parser.parse(options, args);

        // populate local variables with parameter values
//        String className = CreateMysqlHPODbVersion.class.getSimpleName();
        String className = Main.class.getSimpleName();
        String dbType = getOption(o_dbType, line);
        boolean dbHpoRebuild = line.hasOption("r");
        boolean dbCustomRebuild = line.hasOption("c");
        boolean debug = line.hasOption("b");
        String help = getOption(o_help, line);

        // check that required parameters are set
        ArrayList<String> arr_dbTypes = new ArrayList<>();
        arr_dbTypes.add("mssql");
        arr_dbTypes.add("mysql");

        String parameterError = null;
        if (line.hasOption("h")) {
            formatter.printHelp(className, options);
            System.exit(0);
        }
        if (line.hasOption("d")) {
            if (!arr_dbTypes.contains(dbType) && dbType != null) {
                parameterError = "The database type you have entered is not supported. Please use a value of either \"mysql\" or \"mssql\".";
            }
        } else {
            dbType = "mysql";
            System.out.format("Database type defaulting to %s", dbType);
        }
        //print parameter help on error and quit
        if (parameterError != null) {
            formatter.printHelp(className, options);
            throw new IllegalArgumentException(parameterError);
        }

        ResourceBundle rb = null;
        try {
            rb = ResourceBundle.getBundle("app");
        } catch (Exception e) {
            e.getMessage();
            e.printStackTrace();
            System.exit(1);
        }

        //get properties from file
        String dbHost = rb.getString("db.host");
        String dbPort = rb.getString("db.port");
        String dbUser = rb.getString("db.username");
        String dbPass = rb.getString("db.password");
        String dbSchemafile = rb.getString("db.schemafile");
        String osInDir = rb.getString("os.inDir");
        String osOutDir = rb.getString("os.outDir");

        //--------------- LOAD DATA -------------
        System.out.println("Load Terms, Annotations and supplementary data.");
        String oboFile = osInDir + "/hpo.obo";
        String annotationFile = osInDir + "/phenotype_annotation.tab";
        //Load and parse terms and annotations
        HpoDataProvider dataProvider = new HpoDataProvider();
        dataProvider.setOboFile(oboFile);
        dataProvider.setAnnotationFile(annotationFile);
        dataProvider.parseOntologyAndAssociations();
        //database import prep
        HPODatabaseUtil dbUtils = new HPODatabaseUtil();
        DateFormat df = new SimpleDateFormat("yyyyMMdd");
        java.util.Date today = Calendar.getInstance().getTime();
        String reportDate = df.format(today);
        String databaseName = "hpo_" + reportDate;
        String dbSchemaPath = osInDir + "/" + dbSchemafile;
        String outFilePath = osOutDir + "/" + reportDate + "/" + databaseName + ".sql";
        //make db connection
        dbUtils.makeConnection(dbHost, dbPort, dbUser, dbPass, databaseName);
        String mysqlPath = dbUtils.getMySQLPath();
        //load all variables into paramMap
        paramMap.put("dbHost", dbHost);
        paramMap.put("dbPort", dbPort);
        paramMap.put("dbUser", dbUser);
        paramMap.put("dbPass", dbPass);
        paramMap.put("dbType", dbType);
        paramMap.put("dbHpoRebuild", Boolean.toString(dbHpoRebuild));
        paramMap.put("dbCustomRebuild", Boolean.toString(dbHpoRebuild));
        paramMap.put("dbSchemafile", dbSchemafile);
        paramMap.put("osInDir", osInDir);
        paramMap.put("osOutDir", osOutDir);
        paramMap.put("oboFile", oboFile);
        paramMap.put("annotationFile", annotationFile);
        paramMap.put("databaseName", databaseName);
        paramMap.put("dbSchemaPath", dbSchemaPath);
        paramMap.put("outFilePath", outFilePath);
        paramMap.put("mysqlPath", mysqlPath);

        /*
         * Customization Variables: define and instantiate variables needed for support of customization code here.
         */
        // String keyName = "hpo/" + reportDate + "/" + databaseName + ".sql";

        /*
         * Customization Parameters: Put variables into HashMap "paramMap" here.
         */
        // paramMap.put("keyName", keyName);


        //if cli parameter for rebuilding hpo db is true...
        if (dbHpoRebuild) {
            System.out.println("Rebuilding HPO base dataset");
            System.out.format("Path to MySQL executable %s%n", mysqlPath);//
            System.out.format("Create schema (tables, relations) from file: %s%n", dbSchemaPath);
            dbUtils.insertSchemaIntoDatabase(dbHost, dbPort, dbUser, dbPass, databaseName, dbSchemaPath, mysqlPath);

            //-------------- H P O --------------
            // insert all terms
            System.out.println("Insert terms");
            for (Term t : dataProvider.getHpo()) {
                dbUtils.insertHpoTerm(t, dataProvider);
            }
            // fill graph_path table
            System.out.println("Insert graph-path");
            HashSet<GraphPath> allPaths = getAllUniqueDistPaths(dataProvider);
            dbUtils.insertAllPaths(allPaths);
            System.out.println("  allPaths.size() = " + allPaths.size());

            // fill term2term
            System.out.println("Insert term2term");
            dbUtils.insertAllTerm2TermRelationships(dataProvider);

            System.out.println("Insert annotations");

            //----------- ANNOTATIONS -----------
            // insert all diseases annotated with hpo-terms
            for (Item diseaseEntry : dataProvider.getDiseaseId2entry().values()) {
                int diseaseIdInDatabase =
                        dbUtils.insertDisease(
                                diseaseEntry.getDiseaseId().getDiseaseIdInDb(),
                                diseaseEntry.getDiseaseId().getDiseaseDb().toString(),
                                diseaseEntry.getName(),
                                diseaseEntry.getAlternativeNamesAsString()
                        );
                // insert the annotations of the disease
                ArrayList<Annotation> annotations = diseaseEntry.getAnnotations();
                for (Annotation annotation : annotations) {
                    int termid = annotation.getAnnotatedTerm().getID().id;
                    String evidenceCode = annotation.getEvidenceCode().toString();
                    boolean doesNotApply = annotation.getDoesNotApplyModifier();
                    String frequencyModifier = annotation.getFrequencyModifierHpoCode();
                    String annotatedBy = annotation.getAssignedBy();
                    java.sql.Date date = annotation.getDateCreated();
                    int annotationId =
                            dbUtils.insertAnnotation(
                                    diseaseIdInDatabase,
                                    termid,
                                    evidenceCode,
                                    doesNotApply,
                                    frequencyModifier,
                                    annotatedBy,
                                    date
                            );
                    if (annotation.getOnsetModifier() != null) {
                        Term onsetModifier = annotation.getOnsetModifier();
                        dbUtils.insertAnnotationOnsetModifier(annotationId, onsetModifier.getID().id, null);
                    }
                }
            }
        }
        //if cli parameter for rebuilding custom dataset/run custom code is true
        if (dbCustomRebuild) {
            System.out.println("Run Custom code");
            createCustomDataSet();
            loadCustomDataSet();
        }

        //debug mode output
        if (debug) {
            for (Object o : paramMap.entrySet()) {
                HashMap.Entry hme = (Map.Entry) o;
                String param = StringUtils.rightPad(hme.getKey().toString() + ":", 25);
                String val = hme.getValue().toString();
                System.out.format("Parameter %s\t%s%n", param, val);
            }
        }
        //regardless of cli params, dump completed db file
        if (dbHpoRebuild || dbCustomRebuild) {
            //Dump database to file
            System.out.println("Dumping " + databaseName + " to file at: " + outFilePath);
            dbUtils.dumpDatabaseToFile(dbHost, dbPort, dbUser, dbPass, databaseName, outFilePath, mysqlPath);
        }

        Instant endTime = Instant.now();
        Duration runTime = Duration.between(startTime, endTime);
        System.out.format("Done.%n Total runtime: %s. %n", runTime.toString().replace("PT", "").toLowerCase());
    }

    /**
     * @param dataProvider ?
     * @return HashSet<GraphPath>
     */
    private static HashSet<GraphPath> getAllUniqueDistPaths(HpoDataProvider dataProvider) {
        Term root = dataProvider.getHpo().getRootTerm();
        ArrayList<Term> currentPath = new ArrayList<Term>();
        currentPath.add(root);
        HashSet<GraphPath> allPaths = new HashSet<GraphPath>();
        recursiveGetAllPaths(root, currentPath, allPaths, dataProvider);

        // just a quick fix... we missed the term->same term with distance 0 stuff
        for (Term t : dataProvider.getHpo()) {
            GraphPath path = new GraphPath(t.getID().id, t.getID().id, 0);
            allPaths.add(path);
        }
        return allPaths;
    }

    /**
     * @param currentTerm  The current term
     * @param currentPath  The current path
     * @param allPaths     The hashset of all paths
     * @param dataProvider ?
     */
    private static void recursiveGetAllPaths(Term currentTerm, ArrayList<Term> currentPath, HashSet<GraphPath> allPaths, HpoDataProvider dataProvider) {
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

            if (!allPaths.contains(path)) {
                allPaths.add(path);
                if (verbose)
                    System.out.println("added as new path");
            } else {
                if (verbose)
                    System.out.println("path already contained");
            }

        }

        for (Term child : dataProvider.getHpoSlim().getChildren(currentTerm)) {
            if (child.equals(currentTerm))
                continue;
            currentPath.add(child);
            recursiveGetAllPaths(child, currentPath, allPaths, dataProvider);
            currentPath.remove(currentPath.size() - 1);
        }
    }

    /**
     * Converts the current passed option to String and returns it
     *
     * @param opt         The parsed Option
     * @param commandLine The commandLine arguments
     * @return String
     */
    private static String getOption(Option opt, final CommandLine commandLine) {

        if (commandLine.hasOption(opt.getOpt())) {
            return commandLine.getOptionValue(opt.getOpt());
        }
        if (commandLine.hasOption(opt.getLongOpt())) {
            return commandLine.getOptionValue(opt.getLongOpt());
        }
        return null;
    }

    /*
     * Customization Code (add what you need here)
     */
    /**
     * Method stub
     */
    private static void createCustomDataSet() {
        System.out.println("createCustomDataSet() called...");
    }

    /**
     * Method stub
     */
    private static void loadCustomDataSet() {
        System.out.println("loadCustomDataSet() called...");
    }
}
