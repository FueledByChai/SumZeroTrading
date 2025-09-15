/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sumzerotrading.util;

import java.time.LocalTime;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author RobTerpilowski
 */
public class PropertyUtil {

    protected Logger logger = LoggerFactory.getLogger(PropertyUtil.class);
    protected Properties props;

    public PropertyUtil(Properties props) {
        this.props = props;
    }

    public int readInt(String propName) {
        return Integer.parseInt(props.getProperty(propName));
    }

    public int readInt(String propName, int defaultValue) {
        String propValue = "";
        try {
            propValue = props.getProperty(propName);
            return Integer.parseInt(propValue);
        } catch (Exception ex) {
            logger.error(
                    "Unable to read property: " + propName + " with value: " + propValue + ", using default value");
            return defaultValue;
        }
    }

    public double readDouble(String propName) {
        return Double.parseDouble(props.getProperty(propName));
    }

    public double readDouble(String propName, double defaultValue) {
        String propValue = "";
        try {
            propValue = props.getProperty(propName);
            return Double.parseDouble(propValue);
        } catch (Exception ex) {
            logger.error(
                    "Unable to read property: " + propName + " with value: " + propValue + ", using default value");
            return defaultValue;
        }
    }

    public boolean readBoolean(String propName) {
        return Boolean.parseBoolean(props.getProperty(propName));
    }

    public boolean readBoolean(String propName, boolean defaultValue) {
        String propValue = props.getProperty(propName);
        if (propValue == null || propValue.length() == 0) {
            logger.error(
                    "Unable to read property: " + propName + " with value: " + propValue + ", using default value");
            return defaultValue;
        } else {
            return Boolean.parseBoolean(propValue);
        }
    }

    public LocalTime readLocalTime(String propName) {
        return LocalTime.parse(props.getProperty(propName));
    }

    public LocalTime readLocalTime(String propName, LocalTime defaultValue) {
        String propValue = "";
        try {
            propValue = props.getProperty(propName);
            return LocalTime.parse(propValue);
        } catch (Exception ex) {
            logger.error(
                    "Unable to read property: " + propName + " with value: " + propValue + ", using default value");
            return defaultValue;
        }
    }

}
