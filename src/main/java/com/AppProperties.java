package com;

import org.apache.commons.lang.ObjectUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class AppProperties {

    Properties properties;

    AppProperties(String filename){
        try {
            this.properties.load(new FileInputStream(filename));
        } catch (IOException | NullPointerException ex) {
            ex.printStackTrace();
        }
    }
}
