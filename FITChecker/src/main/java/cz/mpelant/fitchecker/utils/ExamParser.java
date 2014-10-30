package cz.mpelant.fitchecker.utils;

import android.support.annotation.NonNull;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cz.mpelant.fitchecker.model.Exam;

/**
 * Class that parses exams from xml server response
 * Created by David Bilik[david.bilik@eman.cz] on 25.2.14.
 */
public class ExamParser {
    @NonNull
    public static List<Exam> parseExams(String xmlResponse) throws XmlPullParserException, IOException {
        XmlPullParser parser = Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(new StringReader(xmlResponse));
        parser.nextTag();
        return readExams(parser);
    }

    @NonNull
    private static List<Exam> readExams(XmlPullParser parser) throws XmlPullParserException, IOException {
        ArrayList<Exam> exams = new ArrayList<>();

        parser.require(XmlPullParser.START_TAG, null, "atom:feed");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("atom:entry")) {
                Exam e = readExam(parser);
                if(e.getTermType().equals(Exam.TERM_TYPE_EXAM)) {
                    exams.add(e);
                }
            } else {
                skip(parser);
            }
        }
        return exams;
    }

    private static Exam readExam(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "atom:entry");
        Exam e = new Exam();
        String id = "";
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("atom:content")) {
                e = readInnerExam(parser);
            } else if ((name.equals("atom:id"))) {
                id = readExamId(parser);
            } else {
                skip(parser);
            }
        }
        e.setExamId(id);
        return e;
    }

    private static Exam readInnerExam(XmlPullParser parser) throws IOException, XmlPullParserException {
        Exam e = new Exam();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("capacity")) {
                e.setCapacity(readCapacity(parser));
            } else if (name.equals("occupied")) {
                e.setOccupied(readOccupied(parser));
            } else if (name.equals("room")) {
                e.setRoom(readRoom(parser));
            } else if (name.equals("startDate")) {
                e.setDate(readStartDate(parser));
            } else if (name.equals("termType")) {
                e.setTermType(readTermType(parser));
            } else {
                skip(parser);
            }
        }
        return e;
    }

    private static String readTermType(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, "termType");
        String startDate = readText(parser);
        parser.require(XmlPullParser.END_TAG, null, "termType");
        return startDate;
    }

    private static String readStartDate(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, "startDate");
        String startDate = readText(parser);
        parser.require(XmlPullParser.END_TAG, null, "startDate");

        return startDate;
    }

    private static String readRoom(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, "room");
        String startDate = readText(parser);
        parser.require(XmlPullParser.END_TAG, null, "room");

        return startDate;
    }

    private static int readOccupied(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, "occupied");
        String startDate = readText(parser);
        parser.require(XmlPullParser.END_TAG, null, "occupied");

        return Integer.valueOf(startDate);
    }

    private static int readCapacity(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, "capacity");
        String startDate = readText(parser);
        parser.require(XmlPullParser.END_TAG, null, "capacity");

        return Integer.valueOf(startDate);
    }

    // For the tags title and summary, extracts their text values.
    private static String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }


    private static void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    public static Set<String> parseRegisteredExams(String xmlResponse) throws XmlPullParserException, IOException {
        XmlPullParser parser = Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(new StringReader(xmlResponse));
        parser.nextTag();
        return readRegisteredExams(parser);
    }

    private static Set<String> readRegisteredExams(XmlPullParser parser) throws IOException, XmlPullParserException {
        Set<String> exams = new HashSet<>();

        parser.require(XmlPullParser.START_TAG, null, "atom:feed");
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("atom:entry")) {
                exams.add(readRegisteredExam(parser));
            } else {
                skip(parser);
            }
        }
        return exams;
    }

    private static String readRegisteredExam(XmlPullParser parser) throws IOException, XmlPullParserException {
        String id = "";
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("atom:content")) {
                id = readRegisteredExamId(parser);
            } else {
                skip(parser);
            }
        }
        return id;
    }

    private static String readRegisteredExamId(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.nextTag();
        parser.require(XmlPullParser.START_TAG, null, "exam");
        String id = parser.getAttributeValue(null, "xlink:href");
//        parser.next();
        String[] parts = id.split("/");
        return parts[parts.length - 1];
    }


    private static String readExamId(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, "atom:id");
        String id = readText(parser);
        parser.require(XmlPullParser.END_TAG, null, "atom:id");
        String[] parts = id.split(":");
        return parts[parts.length - 1];
    }
}
