package com;

import com.database.sqlite.HpoToSQLiteDBUtil;
import com.database.sqlite.SQLiteUtility;
import com.database.sqlite.Tables;
import hpo.HpoDataProvider;
import ontologizer.ontology.Term;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotNull;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.Enumeration;
import java.util.Properties;

public class Main {
    public static void main(String[] args) throws ParseException {

        Instant startTime = Instant.now();

        // configure command line parser
        final CommandLineParser parser = new DefaultParser();
        HelpFormatter fmtr = new HelpFormatter();
        final Options opts = new Options();
        final CommandLine line;

        Option o_filespath = new Option("i", "inputs", true, "The absolute path to ddl, in and out folders");
        Option o_debug = new Option("d", "debug", false, "Turn on debug output.");
        Option o_help = new Option("h", "help", false, "Display help and exit.");

        opts.addOption(o_filespath);
        opts.addOption(o_debug);
        opts.addOption(o_help);

        line = parser.parse(opts, args);

        String className = Main.class.getSimpleName();
        String metaDir = getOption(o_filespath, line);
        boolean debug = line.hasOption("d");

        String optErr = null;
        if (line.hasOption("h")) {
            fmtr.printHelp(className, opts);
            System.exit(0);
        }
        if (line.hasOption("i")) {
            if (metaDir == null) {
                optErr = "The absolute path to your resource files is required to continue.";
            }
        }
        if (optErr != null) {
            fmtr.printHelp(className, opts);
            throw new IllegalArgumentException(optErr);
        }

        Properties props = getAppProperties(metaDir);
        String dbInDir = props.getProperty("db.inDir");
        String dbOutDir = props.getProperty("db.outDir");
        String dbDdlDir = props.getProperty("db.ddlDir");
        String dbName = props.getProperty("db.name");
        String fileAnno = getFilepath(metaDir,props.getProperty("file.anno"),dbInDir);
        String fileObo = getFilepath(metaDir,props.getProperty("file.obo"),dbInDir);
        String fileGenes = getFilepath(metaDir,props.getProperty("file.hgnc"),dbInDir);


        String dbFullName = String.format("%s.db", dbName);
        String dbPath = getFilepath(metaDir, dbFullName,dbOutDir);
        SQLiteUtility sqliteUtil = new SQLiteUtility(dbPath, dbFullName);
        sqliteUtil.connect();
        Connection sqliteConn = sqliteUtil.getConnection();
        buildSQLiteDb(sqliteConn);

        HpoDataProvider dataProvider = new HpoDataProvider();
        dataProvider.setAnnotationFile(fileAnno);
        dataProvider.setOboFile(fileObo);
        dataProvider.parseOntologyAndAssociations();

        HpoToSQLiteDBUtil hposqliteutil = new HpoToSQLiteDBUtil(sqliteConn);
        hposqliteutil.prepLoadStatements();
        // insert all terms
        System.out.println("Insert terms");
        for (Term t : dataProvider.getHpo()) {
            hposqliteutil.insertHpoTerm(t, dataProvider);
        }

        try {
            sqliteConn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (debug) {
            Enumeration<?> e = props.propertyNames();
            while (e.hasMoreElements()) {
                String key = (String) e.nextElement();
                String value = props.getProperty(key);
                String padKey = StringUtils.rightPad(key + ":", 10);
                String pathVal = String.format("%s/%s", metaDir, value);
                System.out.format("%s\t%s%n", padKey, pathVal);
            }
        }

        Instant endTime = Instant.now();
        Duration runTime = Duration.between(startTime, endTime);
        System.out.format("Done.%nTotal runtime: %s. %n", runTime.toString().replace("PT", "").toLowerCase());
    }

    private static String getFilepath(String rootPath, String filename,String subFoldername) {
        String fullPath;
        if (!(rootPath.equals(""))) {
            fullPath = String.format("%s/%s/%s", rootPath, subFoldername,filename);
        } else {
            throw new NullPointerException("Base directory path is null.");
        }
        return fullPath;
    }

    private static void buildSQLiteDb(Connection conn) {
        Tables t = new Tables(conn);
        t.createAllTables();
        t.createAllIndexes();
    }

    private static Properties getAppProperties(String metaDir) {
        String configFile = String.format("%s/%s", metaDir, "app.properties");
        Properties p = new Properties();
        try {
            p.load(new FileInputStream(configFile));
        } catch (IOException | NullPointerException ex) {
            ex.printStackTrace();
        }
        return p;
    }

    /**
     * Converts the current passed option to String and returns it
     *
     * @param opt         The parsed Option
     * @param commandLine The commandLine arguments
     * @return String
     */
    private static String getOption(Option opt, final CommandLine commandLine) {

        String optionValue = "";
        if (!(commandLine.getOptionValue(opt.getOpt())).isEmpty()) {
            optionValue = commandLine.getOptionValue(opt.getOpt());
        }
        if (!(commandLine.getOptionValue(opt.getLongOpt())).isEmpty()) {
            optionValue = commandLine.getOptionValue(opt.getLongOpt());
        }
        if (optionValue.isEmpty())
            throw new NullPointerException("Input parameter (-i/--input is null or invalid.");
        else
            return optionValue;
    }
}
