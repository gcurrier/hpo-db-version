# RebuildHpoDb

Uses _javautil_pheno_ and _phenologizer_ from maven [compbio maven repo](library at http://compbio.charite.de/tl_files/maven) to rebuild the HPO database.

## Concept
Requires 3 files: 
- the _phenotype_annotations.tab_ file (downloadable [here](http://compbio.charite.de/jenkins/job/hpo.annotations/lastStableBuild/)), 
- the latest _hp.obo_ file, found [here](http://purl.obolibrary.org/obo/hp.obo) 
- the latest schema dump file from the [db-schema](db-schema) folder included in this repo.

1. A database is created with the name "hpo_\<yyyyMMdd>" (e.g. hpo_20001231) in the given MySQL instance using the schema dump file. Data from the datafiles is then inserted to the appropriate tables.
2. If indicated (via command line options), a custom dataset is created (providing your own code is present).
3. Finally, a dump file is created of the hpo database.

Additionally, more files may be added as needed.

## Technical details
The program relies mainly upon a properties file:
````
#OS Properties
os.inDir=<Location of all files>
os.outDir=<location for all output>

#DB Properties
db.host=<IP or FQDN to DB host>
db.port=<DB port>
db.username=<db username>
db.password=<db password>
db.schemafile=<name of db schema file>

#Additional properties 
#propType.propName=<additional property value>

````

All datafiles for import, and the schema dump file must be placed (copied) to the directory indicated in __os.inDir__.

While the properties file contains information the program requires to run, there are also command line options necessary for the program to know _HOW_ to run:
````
usage: RebuildHpoDb
 -d,--db-type <arg>       The type of database (mysql/mssql) (defaults to mysql)
 -r,--rebuild-db          Rebuild hpo database
 -c,--rebuild-custom-db   Rebuild custom dataset
 -b,--debug               Debug output (show all parameters)
 -h,--help                Display this help message and exit

````
- -d: for the moment, only mysql systems are supported
- -r: will tear down and rebuild the mysql hpo database
- -c: if custom code is present, will run that portion of code to do whatever is indicated
- -b: display hpo "prep" info and input parameters/variables
- -h: help display and exit

Execution examples:
````
# An example of rebuilding the hpo db and running custom code WITH debug output:
java -jar RebuildHpoDb_0.2.jar -rcb

# Just the Hpo DB
java -jar RebuildHpoDb_0.2.jar -r

# Just the Custom code (db MUST be present)
java -jar RebuildHpoDb_0.2.jar -c

# Debug output (ONLY)
java -jar RebuildHpoDb_0.2.jar -b
````

## Installation

Requirements

Java jre 1.8

Maven >= 3

navigate to the project root
 ```
 mvn clean install
 ```
 
The output jar will be located under _$project_root/target_
 
#### Credits

[compbio](http://drseb.github.io/science/cv/) | Original Author | Dr. Sebastian Köhler  [Original hpo-db-version](https://github.com/drseb/hpo-db-version) 

[MGZ, München](https://www.mgz-muenchen.de/startseite.html) | Contributor | Glen Currier | [Contributor Fork](https://github.com/gcurrier/hpo-db-version/tree/mgz-customization)
 
 
