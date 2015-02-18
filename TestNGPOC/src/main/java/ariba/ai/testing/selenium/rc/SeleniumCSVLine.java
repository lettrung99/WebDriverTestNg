/*
    Copyright (c) 1996-2009 Ariba, Inc.
    All rights reserved. Patents pending.

    $Id: //ariba/ond/platform/R1_rel/test/test-framework/test/ariba/framework/SeleniumCSVLine.java#1 $

    Responsible: jimh
 */
package ariba.ai.testing.selenium.rc;

import ariba.util.core.StringUtil;
import ariba.util.core.Fmt;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SeleniumCSVLine {

    private SeleniumCSVTestCase testCase;
    private String _command;
    private String _target;
    private String _value;
    private int _lineNumber;
    private boolean _isComment;
    private String _commentContents;
    private boolean _isAnnotation;
    private String _annotationName;
    private String _annotationValue;
    private boolean _isReplacementDone = false;
    // False if there is a round trip to the server
    // ie all xxxAndWait commands and commands followed 
    // by waitForPageToLoad
    private boolean _pageLocalCommand = true;

    private static final String[] COMMANDS_THAT_DONT_HIGHLIGHT =
        {"OPEN", "WAITFORPAGETOLOAD", "SETTIMEOUT", "ASSERTTEXTPRESENT",
         "ASSERTTEXTNOTPRESENT", "ASSERTELEMENTNOTPRESENT"};

    private static final Pattern CommentPattern =
                Pattern.compile("^\\s*\\#("+
                        SeleniumCSVTestCase.ANNOTATION_KEY +
                        "([^=]+)(=(.+))?)?(.*)");


    public SeleniumCSVLine (SeleniumCSVTestCase testCase, List lineFromCSV, int lineNumber)
    {
        this.testCase = testCase;
        
        if (lineFromCSV.isEmpty()) {
            return;
        }
        Matcher commentMatcher = CommentPattern.matcher((String)lineFromCSV.get(0));
        if (commentMatcher.matches()) {
            // It's a comment.
            _isComment = true;
            _commentContents = StringUtil.fastJoin(lineFromCSV, ",");
            
            // Checks if it's a processing instruction
            if (!StringUtil.nullOrEmptyOrBlankString(commentMatcher.group(1))) {
                _isAnnotation = true;
                _annotationName = (commentMatcher.group(2) != null)?commentMatcher.group(2).trim():null;
                _annotationValue = commentMatcher.group(4);
            }
        } 
        else if (lineFromCSV.size() < 3) {
            throw new SeleniumCSVException(
                    "Invalid format: each line must have three values");
        }
        else {
            String originalCommand = (String)lineFromCSV.get(0);
            _command = originalCommand.toUpperCase();
            _target = (String)lineFromCSV.get(1);
            _value = (String)lineFromCSV.get(2);
        }
        _lineNumber = lineNumber;        
    }


    public SeleniumCSVTestCase getTestCase () {
        return testCase;
    }

    public String getCommand ()
    {
        return _command;
    }

    public void setCommand (String command)
    {
        _command = command;
    }

    public String getTarget ()
    {
        return _target;
    }

    public void setTarget (String target)
    {
        _target = target;
    }

    public String getValue ()
    {
        return _value;
    }

    public void setValue (String value)
    {
        _value = value;
    }
    
    public int getLineNumber ()
    {
        return _lineNumber;
    }

    public boolean isComment ()
    {
        return _isComment;
    }

    public String getComment ()
    {
        return _commentContents;
    }

    public boolean isAnnotation() {
        return _isAnnotation;
    }

    public String getAnnotationName() {
        return _annotationName;
    }

    public String getAnnotationValue() {
        return _annotationValue;
    }

    public boolean isPageLocalCommand() {
        return _pageLocalCommand;
    }

    public void setPageLocalCommand(boolean pageLocalCommand) {
        _pageLocalCommand = pageLocalCommand;
    }

    public String getFormattedString ()
    {
        String formattedString;
        if (isComment()) {
            formattedString = _commentContents;
        }
        else {
            formattedString =
                    Fmt.S("[Line %s] { command: '%s', target: '%s', value: '%s' }",
                          _lineNumber, _command, _target, _value);
        }
        return formattedString;
    }

    public String getTruncatedTarget ()
    {
        String target = getTarget();
        if (target.length() > 100) {
            String truncatedString = StringUtil.strcat(
                    target.substring(0,96), "..."
            );
            return truncatedString;
        }
        return target;
    }

    public String getTruncatedValue ()
    {
        String value = getValue();
        if (value.length() > 100) {
            String truncatedString = StringUtil.strcat(
                    value.substring(0,96), "..."
            );
            return truncatedString;
        }
        return value;
    }

        /**
     * Used to get the target only for commands which use a valid ui target.
     * Several of our commands such as asserttextpresent and waitforpagetoload
     * really have their values in the target field.  For these command types
     * this method will return null.
     * @return null if the command has a target which does not represent a real ui
     * element, the target otherwise.
     */
    public String getHighlightableTarget ()
    {
        for (int i = 0; i < COMMANDS_THAT_DONT_HIGHLIGHT.length; i++) {
            if (_command.equalsIgnoreCase(COMMANDS_THAT_DONT_HIGHLIGHT[i])) {
                return null;
            }
        }
        return _target;
    }

    public void processCommandReplacements (Map<String, String> replaceStringMap)
    {
        if (!_isReplacementDone && replaceStringMap != null) {
            String target = replaceSubStringMaybe(replaceStringMap, getTarget());
            String value = replaceSubStringMaybe(replaceStringMap, getValue());
            setTarget(target);
            setValue(value);
            _isReplacementDone = true;
        }
    }

    

    // Does an exact match on original string and replace with
    // new value.
    private String replaceSubStringMaybe (Map<String, String> replaceStringMap,
                                          String originalString)
    {
        Iterator fromIterator = replaceStringMap.keySet().iterator();
        String newString = originalString;
        while (fromIterator.hasNext()) {
            String origVal = (String)fromIterator.next();
            String newVal = (String)replaceStringMap.get(origVal);
            Pattern myPattern = Pattern.compile(origVal,
                                                Pattern.CASE_INSENSITIVE);
            newString = myPattern.matcher(newString).replaceAll(newVal);
        }
        return newString;
    }
}
