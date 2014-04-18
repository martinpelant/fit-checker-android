package cz.mpelant.fitchecker.utils;

import android.support.annotation.NonNull;
import android.util.Xml;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that parses subjects from xml server response
 * Created by David Bilik[david.bilik@eman.cz] on 25.2.14.
 */
public class SubjectParser {
    @NonNull
    public static List<String> parseSubjects(String xmlResponse) throws XmlPullParserException, IOException {
        XmlPullParser parser = Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(new StringReader(xmlResponse));
        parser.nextTag();
        return readSubjects(parser);
    }

    @NonNull
    private static List<String> readSubjects(XmlPullParser parser) throws XmlPullParserException, IOException {
        ArrayList<String> subjects = new ArrayList<>();

        parser.require(XmlPullParser.START_TAG, null, "atom:feed");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("atom:entry")) {
                subjects.add(readSubject(parser));
            } else {
                skip(parser);
            }
        }
        return subjects;
    }

    // Parses the contents of an entry. If it encounters a title, summary, or link tag, hands them off
// to their respective "read" methods for processing. Otherwise, skips the tag.
    private static String readSubject(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "atom:entry");
        String title = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("atom:content")) {
                title = readTitle(parser);
            } else {
                skip(parser);
            }
        }
        return title;
    }

    // Processes title tags in the feed.
    private static String readTitle(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.nextTag();
        String value = parser.getAttributeValue(null, "xlink:href");
        value = value.substring("courses/".length(), value.length() - 1);
        parser.nextText();
        parser.next();
        return value;
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
}
