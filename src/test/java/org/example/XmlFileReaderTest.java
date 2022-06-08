package org.example;

import org.example.common.Executable;
import org.example.common.Result;
import org.example.common.List;
import org.jdom2.Element;

/**
 * Unit test for simple App.
 */
public class XmlFileReaderTest
{

    private final static String format =
            "COMMON : %s\n" + "BOTANICAL : %s\n" +
                    "ZONE : %s\n" + "AVAILABILTY : %s\n" + "PRICE : %s\n" + "LIGHT : %s\n";
    private final static List<String> elementNames =
                List.list("COMMON", "BOTANICAL", "ZONE", "AVAILABILITY", "PRICE", "LIGHT");

    public static void main(String[] args) {
        Executable program = ReadXmlFile.readXmlFile(XmlFileReaderTest::getXmlFilePath,
                                                    XmlFileReaderTest::getRootElementName,
                                                    XmlFileReaderTest::processElement,
                                                    XmlFileReaderTest::processList);
        try {
            program.exec();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static Result<String> processElement(Element element) {
        try {
        return Result.of(String.format(format, elementNames.map(element::getChildText)
                                                    .toJavaList()
                                                    .toArray()));
        } catch (Exception e) {
            return Result.failure("Exception during element formatting. Probably cause is no element name on element list " + elementNames);
        }
    }

    private static String getChildText(Element element, String name) {
        String string = element.getChildText(name);
        return string != null ? string :  "No such element found  " + name;
    }
    private static ElementName getRootElementName() {
        return ElementName.apply("PLANT");
    }

    private static FilePath getXmlFilePath() {
        return FilePath.apply("plant_catalog.xml");
    }

    private static <T> void processList(List<T> list) {
        list.forEach(System.out::println);
    }
}