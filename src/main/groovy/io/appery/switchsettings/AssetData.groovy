package io.appery.switchsettings;

import groovy.json.*

public class AssetData {

    def parsedData;

    static AssetData parse(String text) {
        return new AssetData(new JsonSlurper().parseText(text))
    }

    AssetData(parsedData) {
        this.parsedData = parsedData
    }

    List<String> getPropertyNames() {
        return parsedData.assets[0].assetData.servicesettings.properties.property.collect { it['@path'] }
    }

    Map getProperties() {
        //println JsonOutput.toJson(parsedData))
        return parsedData.assets[0].assetData.servicesettings.properties
    }

    void setProperties(Map props) {
        parsedData.assets[0].assetData.servicesettings.properties = props
    }

    String toJson() {
        return JsonOutput.toJson(parsedData)
    }

    String getProperty(String name) {
        List props = parsedData.assets[0].assetData.servicesettings.properties.property
        def result = props.find { it['@path'] == name }
        if (result == null) {
            return null
        } else {
            return result["@value"]
        }
    }

}