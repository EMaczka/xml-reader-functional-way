package org.example;

import org.example.common.*;
import org.jdom2.Element;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ReadXmlFile {

    public static <T> Executable readXmlFile(Supplier<FilePath> sPath,
                                             Supplier<ElementName> sRootName,
                                             Function<Element, Result<T>> f,
                                             Effect<List<T>> e) {
        final Result<String> path = sPath.get().value;
        final Result<String> rDoc = path.flatMap(ReadXmlFile::readFile2String);
        final Result<String> rRoot = sRootName.get().value;
        final Result<List<T>> result = rDoc.flatMap(doc -> rRoot
                .flatMap(rootElementName -> readDocument(rootElementName, doc))
                .flatMap(list -> List.sequence(list.map(f))));
        return () -> result.forEachOrThrow(e);
    }

    public static Result<String> readFile2String(String path) {
        try {
            return Result.success(new String(Files.readAllBytes(Paths.get(path))));
        } catch (IOException e) {
            return Result.failure(String.format("IO error while reading the file %s", path), e);
        } catch (Exception e) {
            return Result.failure(String.format("Unexpected error reading the file %s", path), e);
        }
    }

    public static Result<List<Element>> readDocument(String rootElementName, String stringDoc) {

        final SAXBuilder builder = new SAXBuilder();
        try {
            final Document document = builder.build(new StringReader(stringDoc));
            final Element rootElement = document.getRootElement();
            return Result.success(List.fromCollection(rootElement.getChildren(rootElementName)));
        } catch (IOException | JDOMException io) {
            return Result.failure(String.format("Incorrect root name '%s' or incorrect data XML %s",
                    rootElementName, stringDoc), io);
        } catch (Exception e) {
            return Result.failure(String.format("Unexpected error reading the XML data %s",
                    stringDoc), e);
        }
    }
}