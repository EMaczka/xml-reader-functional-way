package org.example;

import org.example.common.Result;

public class ElementName {

    public final Result<String> value;

    private ElementName(Result<String> value) {
        this.value = value;
    }

    public static ElementName apply(String value) {
        return new ElementName(Result.of(ElementName::isValidName, value, "Incorrect element name: " + value));
    }

    private static boolean isValidName(String path) {
        return true;
    }
}