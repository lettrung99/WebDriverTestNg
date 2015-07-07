package ariba.ai.testing.selenium.rc;

import ariba.ui.aribaweb.test.TestLinkHolder;
import ariba.util.core.Fmt;
import ariba.util.core.ListUtil;
import ariba.util.i18n.I18NUtil;
import ariba.util.io.CSVConsumer;
import ariba.util.io.CSVErrorHandler;
import ariba.util.io.CSVReader;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;


public class SeleniumCSVTestCase
{
    public static final String ANNOTATION_KEY = "@";
    private static final String WAITFORPAGETOLOAD = "WAITFORPAGETOLOAD";
    private static final String DESTAGER = ".*'" + TestLinkHolder.LinkType.Destager + "_.*";
    private static final String DESTAGER_PARAM = ".*'" + TestLinkHolder.LinkType.Param_Destager + "_.*";
    private static final String STAGER_FORM_RUN = ".*TestStagerForm:runStager.*";
    private static final String STAGER_FORM_CANCEL = ".*TestStagerForm:cancel.*";
    private static final String TA_PANE = ".*TestPane:PopupMenuLink.*";
    private static final String END_TEST = ".*TestPane:endTest.*";

    private List<SeleniumCSVLine> _lines;
    private List<SeleniumCSVLine> _commandLines;
    private List<SeleniumCSVLine> _annotationLines;
    private int _tearDownStartLineNumber = -1;
    String _csvErrorMessage = null;
    String _fileName = null;
    private static final String[] SELENIUM_SCRIPT_DIRECTORIES =
            new String[]{"internal","internal/selenium"};


    public SeleniumCSVTestCase (String filename)
    {
    	 System.out.println("what is my filename: " + filename);
        _fileName = filename;
        try {
            File resolvedFile = resolveScriptPath(filename);
            loadDataFromFile(resolvedFile);
        }
        catch (IOException ioxcp) {
            throw new SeleniumCSVException("Problem loading CSV", ioxcp);
        }
    }

    public SeleniumCSVTestCase (File file)
    {
        loadDataFromFile(file);
    }

    private void loadDataFromFile (File file)
    {
        _lines = ListUtil.list();
        _commandLines = ListUtil.list();
        _annotationLines = ListUtil.list();
        CSVReader reader = new CSVReader(new SeleniumTestCSVConsumer(this),
                new SeleniumCSVErrorHandler());
        try {
            reader.read(file, I18NUtil.EncodingUTF_8);
            System.out.println("what error: " + _csvErrorMessage);
            if (_csvErrorMessage != null) {
                throw new SeleniumCSVException(_csvErrorMessage);
            }
        }
        catch (IOException ioxcp) {
            throw new SeleniumCSVException("Problem loading CSV", ioxcp);
        }

        validate();
    }

    public Iterator<SeleniumCSVLine> getLines ()
    {
        return _lines.iterator();
    }

    public Iterator<SeleniumCSVLine> getCommandLines ()
    {
        return _commandLines.iterator();
    }

    public List<SeleniumCSVLine> getAllLines ()
    {
        return _lines;
    }

    public int getNumberOfLines ()
    {
        return _lines.size();
    }

    public SeleniumCSVLine getLine (int lineNumber)
    {
        return _lines.get(lineNumber);
    }

    public int getNumberOfCommands ()
    {
        return _commandLines.size();
    }

    public SeleniumCSVLine getCommandLine (int commandNumber)
    {
        return _commandLines.get(commandNumber);
    }

    public SeleniumCSVLine getCommandBeforeLine (SeleniumCSVLine commandLine)
    {
        int i = _commandLines.indexOf(commandLine);
        if (i > 0) {
            return _commandLines.get(i-1);
        }
        return null;
    }

    public SeleniumCSVLine getCommandAfterLine (SeleniumCSVLine commandLine)
    {
        int i = _commandLines.indexOf(commandLine);
        if (i > 0 && i < _commandLines.size() - 1) {
            return _commandLines.get(i+1);
        }
        return null;
    }

    public List<SeleniumCSVLine> getAnnotationLines()
    {
        return _annotationLines;
    }

    public boolean hasAnnotation (String name)
    {
        boolean value = false;
        for (SeleniumCSVLine line : _annotationLines) {
            if (name.equalsIgnoreCase(line.getAnnotationName())) {
                value = true;
                break;
            }
        }
        return value;
    }

    public String getAnnotation (String name)
    {
        String value = null;
        for (SeleniumCSVLine line : _annotationLines) {
            if (name.equalsIgnoreCase(line.getAnnotationName())) {
                value = line.getAnnotationValue();
                break;
            }
        }
        return value;
    }

    public String getFileName ()
    {
        return _fileName;
    }

    public String getContainingDir ()
    {
        String location = "";
        if (_fileName != null && _fileName.length() > 0) {
            int index = _fileName.lastIndexOf("/");
            if (index > 0) {
                location = _fileName.substring(0, index);
            }
        }

        return location;
    }

    public int getTearDownStartLineNumber()
    {
        return _tearDownStartLineNumber;
    }

    protected File resolveScriptPath (String csvFilename) throws IOException
    {
    	  System.out.println("I am in resolve: " + csvFilename);
        int i = 0;
        File file = new File(csvFilename);
        
        //comment this out for now.  Doesn't apply to the new stuff.  Need to revisit
        /*while ((!file.exists() || !file.isFile()) &&
            i<SELENIUM_SCRIPT_DIRECTORIES.length) {
            file = new File(SELENIUM_SCRIPT_DIRECTORIES[i++], csvFilename);
        }*/
        if (file.exists() && file.isFile()) {
            return file;
        }
        else {
      	  System.out.println("I am in resolve and throwing");

            throw new IOException("Cannot find script " + csvFilename +
                                  " under following locations: " +
                                  SELENIUM_SCRIPT_DIRECTORIES);
        }
    }

    private void validate() throws ValidationException
    {
        boolean onDestagerFormPage = false;
        for (int i = 0; i < _commandLines.size(); i ++) {
            SeleniumCSVLine commandLine = _commandLines.get(i);
            String command = commandLine.getCommand();
            String target = commandLine.getTarget();
            if (command.equalsIgnoreCase(WAITFORPAGETOLOAD)) {
                // ignore any WAITFORPAGETOLOAD command
                continue;
            }

            if (_tearDownStartLineNumber > -1) {
                if (onDestagerFormPage) {
                    // on the destager form page
                    if (target.matches(TA_PANE))
                    {
                        // clicking on the TA pane is not allowed
                        throw new ValidationException(commandLine,
                            "Clicking on the TA pane is not allowed");
                    }
                    else if (target.matches(STAGER_FORM_CANCEL))
                    {
                        // clicking on the Cancel button is not allowed
                        throw new ValidationException(commandLine,
                            "Clicking on the Cancel button is not allowed");
                    }
                    else if (target.matches(STAGER_FORM_RUN))
                    {
                        // clicking on the Run Stager button is OK and will go
                        // back to the Test Central page
                        onDestagerFormPage = false;
                    }

                    // else the command should be stager form related so continue
                }
                else {
                    // on the Test Central page
                    if (target.matches(TA_PANE))
                    {
                        // click on TA pane
                        if (i + 1 < _commandLines.size() - 1) {
                            // there should be only one last "end test" command
                            throw new ValidationException(commandLine,
                                "Only end test command is expected");
                        }
                        else if (i + 1 == _commandLines.size() - 1) {
                            SeleniumCSVLine nextCommandLine = _commandLines.get(i + 1);
                            String nextTarget = nextCommandLine.getTarget();
                            if (!nextTarget.matches(END_TEST))
                            {
                                // next command should be "end test"
                                throw new ValidationException(nextCommandLine,
                                    "End test command is expected");
                            }
                            else {
                                break;
                            }
                        }
                    }
                    else if (target.matches(DESTAGER_PARAM))
                    {
                        // click on parameterized destager
                        onDestagerFormPage = true;
                    }
                    else if (!target.matches(DESTAGER))
                    {
                        // only destager or parameterized destager clicks are allowed at this point
                        throw new ValidationException(commandLine,
                            "Only destager or parameterized destager clicks are allowed at this point");
                    }
                }
            }
            else {
                if (target.matches(DESTAGER))
                {
                    // found the start of the tear down section (destager)
                    _tearDownStartLineNumber = i;
                }
                else if (target.matches(DESTAGER_PARAM))
                {
                    // found the start of the tear down section (parameterized destager)
                    _tearDownStartLineNumber = i;
                    onDestagerFormPage = true;
                }
            }
        }
    }

    private class SeleniumTestCSVConsumer implements CSVConsumer
    {
        private SeleniumCSVTestCase testCase;

        public SeleniumTestCSVConsumer (SeleniumCSVTestCase testCase) {
            this.testCase = testCase;
        }
        
        public void consumeLineOfTokens (String path,
                                         int lineNumber,
                                         List line)
        {
            if (_csvErrorMessage != null) {
                throw new SeleniumCSVException(_csvErrorMessage);
            }

            if (line.isEmpty()) {
                return;
            }

            SeleniumCSVLine csvLine = new SeleniumCSVLine(testCase, line,  lineNumber);
            _lines.add(csvLine);
            if (!csvLine.isComment()) {
                _commandLines.add(csvLine);
            }
            if (csvLine.isAnnotation()) {
                _annotationLines.add(csvLine);
            }
        }
    }

    private class SeleniumCSVErrorHandler implements CSVErrorHandler
    {
        /*

          Due to the preposterous design of error handling in
          CSVReader, we can't throw exceptions here for two reasons:

          1. The CSVErrorHandler interface states that exceptions
             should not be thrown.

          2. In some cases the error is due to an exception that
             already occurred (e.g. encoding exception), but the
             exception is not passed in to this interface (instead
             CSVReader rethrows it AFTER invoking this handleError()).
             If we throw our own exception here, the underlying
             exception will be lost.

          So instead, we set the error message.  Next time through the
          loop, the SeleniumCSVConsumer will check to see if the error
          message is set and and throw the exception for us.  This
          also gives a chance for any underlying exception to be
          propagated by CSVReader.

        */

        public void handleError (int errorCode, String location, int lineNumber)
        {
            String detail;
            switch (errorCode) {
            case CSVReader.ErrorMissingComma:
                detail = "End of field not followed by newline or comma in {0}, line {1}";
                break;
            case CSVReader.ErrorUnbalancedQuotes:
                detail = "Unbalanced quotes in {0}, line {1}";
                break;
            case CSVReader.ErrorIllegalCharacterOrByteSequence:
                detail = "Illegal character or byte sequence in {0}, line {1}";
                break;
            default:
                detail = "Unknown CSV format error code {2} in {0}, line {1}";
                break;
            }
            _csvErrorMessage = Fmt.Si(detail, location, lineNumber, errorCode);
        }
    }

    public static class ValidationException extends RuntimeException
    {
        public ValidationException (SeleniumCSVLine commandLine, String message)
        {
            super(message);
            System.out.format("Validation error: %s", commandLine.getFormattedString());
        }
    }
}
