-- MySQL dump 10.13  Distrib 5.1.45, for apple-darwin10.2.0 (i386)
--
-- Host: localhost    Database: myhpo
-- ------------------------------------------------------
-- Server version	5.1.45

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


--
-- Table structure for table `annotation`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `annotation` (
  `term_id` int(11) NOT NULL,
  `external_object_disease_id` int(11) NOT NULL,
  `evidence_code` varchar(5) NOT NULL,
  `is_negative` tinyint(1) DEFAULT '0',
  `frequency_modifier` varchar(20) DEFAULT NULL,
  `annotation_id` int(11) NOT NULL AUTO_INCREMENT,
  `annotated_date` date NOT NULL,
  `annotated_by` varchar(100) NOT NULL,
  `comment` mediumtext,
  PRIMARY KEY (`annotation_id`),
  KEY `fk_annotation_termid` (`term_id`),
  KEY `fk_annotation_disease_id` (`external_object_disease_id`),
  CONSTRAINT `fk_annotation_disease_id` FOREIGN KEY (`external_object_disease_id`) REFERENCES `external_object_disease` (`external_object_id`),
  CONSTRAINT `fk_annotation_termid` FOREIGN KEY (`term_id`) REFERENCES `term` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=411548 DEFAULT CHARSET=utf8 COMMENT='holds the assignment of an HPO-term to an external disease';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `annotation_onset_modifier`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `annotation_onset_modifier` (
  `annotation_id` int(11) NOT NULL,
  `onset_term_id` int(11) NOT NULL,
  `comment` mediumtext,
  PRIMARY KEY (`annotation_id`,`onset_term_id`) USING BTREE,
  KEY `fk_onset_modifier_onsetterm` (`onset_term_id`),
  CONSTRAINT `fk_onset_modifier_annotationid` FOREIGN KEY (`annotation_id`) REFERENCES `annotation` (`annotation_id`),
  CONSTRAINT `fk_onset_modifier_onsetterm` FOREIGN KEY (`onset_term_id`) REFERENCES `term` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;


--
-- Table structure for table `external_object`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `external_object` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `external_id` varchar(100) NOT NULL,
  `external_db` varchar(100) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uq_external_obj` (`external_id`,`external_db`),
  KEY `idx_external_obj_id` (`external_id`),
  KEY `idx_external_obj_db` (`external_db`)
) ENGINE=InnoDB AUTO_INCREMENT=268097 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `external_object_disease`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `external_object_disease` (
  `external_object_id` int(11) NOT NULL,
  `disease_id` varchar(20) NOT NULL,
  `db_name` varchar(20) NOT NULL,
  `disease_title` varchar(1000) DEFAULT NULL,
  `disease_longtitle` varchar(3000) DEFAULT NULL,
  PRIMARY KEY (`external_object_id`),
  UNIQUE KEY `uq_disease` (`disease_id`,`db_name`),
  KEY `idxdisease_id` (`disease_id`),
  KEY `idxdisease_db` (`db_name`),
  CONSTRAINT `fk_disease_ext_object` FOREIGN KEY (`external_object_id`) REFERENCES `external_object` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;


--
-- Table structure for table `graph_path`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `graph_path` (
  `term1_id` int(11) NOT NULL,
  `term2_id` int(11) NOT NULL,
  `distance` int(11) NOT NULL,
  PRIMARY KEY (`term1_id`,`term2_id`,`distance`) USING BTREE,
  KEY `fk_graph_path_term2` (`term2_id`),
  CONSTRAINT `fk_graph_path_term1` FOREIGN KEY (`term1_id`) REFERENCES `term` (`id`),
  CONSTRAINT `fk_graph_path_term2` FOREIGN KEY (`term2_id`) REFERENCES `term` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;



--
-- Table structure for table `term`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `term` (
  `id` int(11) NOT NULL COMMENT 'The identifier of the HPO-term as integer. E.g. the ID 123 corresponds to the term HP:0000123.',
  `name` varchar(1000) NOT NULL COMMENT 'A textual label for the term. Each term has a single such label (see *term_synonym* for alternate labels). The name should be unique within an ontology (in fact uniqueness is encourage across ontologies - the principle of univocity. However, this is not e',
  `is_obsolete` tinyint(1) NOT NULL COMMENT 'Equals 1 if this row corresponds to an obsoleted "ex-term". Note that obsoletes are not terms in the true sense, but as GO we house them in the same table as this is the most expedient for the kinds of queries people wish to perform. (OBO-Format: *is_obso',
  `is_root` tinyint(1) NOT NULL COMMENT 'Equals 1 if this term is one of the four root terms in the ontology graph. (OBO-Format: No correspoding tag)',
  `subontology` varchar(1) NOT NULL COMMENT 'This fields indicate to which subontology this term belongs. This can be one of ''O'', ''I'' or ''C'' (as defined in the HPO-annotation guide). For the Term "All" (HP:0000001) this field is set to ''A''.',
  `comment` mediumtext COMMENT 'A free-text comment with non-definitional information that may be useful for end-users or curators. (OBO-Format: *comment* tag - each term has max 1 comment) ',
  `acc` varchar(10) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='A term of the HPO.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `term2external_object`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `term2external_object` (
  `term_id` int(11) NOT NULL,
  `external_object_id` int(11) NOT NULL,
  PRIMARY KEY (`term_id`,`external_object_id`),
  KEY `fk_term2external_ext_id` (`external_object_id`),
  CONSTRAINT `fk_term2external_ext_id` FOREIGN KEY (`external_object_id`) REFERENCES `external_object` (`id`),
  CONSTRAINT `fk_term2external_termid` FOREIGN KEY (`term_id`) REFERENCES `term` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `term2term`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `term2term` (
  `term1_id` int(11) NOT NULL,
  `term2_id` int(11) NOT NULL,
  `relationship_type` varchar(10) NOT NULL,
  PRIMARY KEY (`term1_id`,`term2_id`),
  KEY `fk_term2term_term2` (`term2_id`),
  CONSTRAINT `fk_term2term_term1` FOREIGN KEY (`term1_id`) REFERENCES `term` (`id`),
  CONSTRAINT `fk_term2term_term2` FOREIGN KEY (`term2_id`) REFERENCES `term` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `term_alternative_id`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `term_alternative_id` (
  `term_id` int(11) NOT NULL,
  `alternative_id` int(11) NOT NULL,
  KEY `fk_alternativeids_termid` (`term_id`),
  CONSTRAINT `fk_alternativeids_termid` FOREIGN KEY (`term_id`) REFERENCES `term` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `term_definition`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `term_definition` (
  `term_id` int(11) NOT NULL,
  `term_definition` longtext NOT NULL,
  KEY `fk_termdefinition_term_id` (`term_id`),
  CONSTRAINT `fk_termdefinition_term_id` FOREIGN KEY (`term_id`) REFERENCES `term` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `term_synonym`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `term_synonym` (
  `term_id` int(11) NOT NULL,
  `term_synonym` varchar(1000) NOT NULL,
  `id` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`),
  KEY `fk_termid_synonym` (`term_id`),
  CONSTRAINT `fk_termid_synonym` FOREIGN KEY (`term_id`) REFERENCES `term` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2010-06-18 17:52:47
