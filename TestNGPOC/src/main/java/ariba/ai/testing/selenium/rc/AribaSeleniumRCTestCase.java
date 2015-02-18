/*
    Copyright (c) 1996-2010 Ariba, Inc.
    All rights reserved. Patents pending.

    $Id: //ariba/ond/platform/R1_rel/test/test-framework/test/ariba/framework/AribaSeleniumRCTestCase.java#1 $
    
    Responsible: ssri
 */

package ariba.ai.testing.selenium.rc;

import ariba.util.core.ArrayUtil;
import ariba.util.core.Constants;
import ariba.util.core.Fmt;
import ariba.util.core.ListUtil;
import ariba.util.core.MapUtil;
import ariba.util.core.StringUtil;


import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.SeleniumException;

import ariba.ai.testing.util.*;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * The Selenium RC test. The test can be configured with the parameters as
 * described in {@link AribaSeleniumRCTestParameters}
 *
 * @aribaapi private
 */
public class AribaSeleniumRCTestCase 
{

    String _args[] = null;
    String _browser = "*chrome";
    String _baseURL = null;
    String _csvFilename = null;

    // default time out value
    String _timeOut = "60000";
    String _logLevel = "info";

    String _suite = null;
    String _test = null;
    String _package = null;

    private boolean _rerunningFailedTest = false;
    private SeleniumCSVTestCase _seleniumCSVTestCase = null;

    String _csvErrorMessage = null;

    private static final String LOCATOR_TA_PANE = "[@awname='TestPane:PopupMenuLink']";
    private static final String LOCATOR_TEST_CENTRAL = "[@awname='TestPane:TestCentral']";


    private static final String _timeoutErrMsg =
            "ERROR: Command should already time out. Forcing to quit now.";
    private static final String _communicationErrMsg =
            "ERROR: Problem communicating with the selenium server or browser.";
    private int _currentCommandLineNumber;

    private Selenium _selenium;
    private String baseUrl = "";

    // use to track the start time and expected end time of a command
    private final CommandTime _commandTime = new CommandTime();

    // indicate if we're done
    private boolean _finish = false;

    // This is a map of strings that we will replace (if present) in either the
    // target string or the value string.
    // Used primarily to replace a canonical realm with a different realm name.
    // Ex: "S4All" substring
    // will be replaced by "S4All-1". For parallelization, multiple realms with
    // the same canonical form have
    // been created for some of the most commonly used realms.
    private Map<String, String> _replaceStringMap = null;

    // Rewriting URLs before sending them to the browser

    private static List<RewriteRule> _rewriteRules = ListUtil.list();


    public AribaSeleniumRCTestCase (  Selenium _selenium, String  _csvFilename, String baseUrl )
    {
    	 this._selenium    = _selenium;
    	 this._csvFilename =  _csvFilename;
    	 this.baseUrl      = baseUrl;
    	 this._seleniumCSVTestCase = this.getSeleniumCSVTestCase();
    	 
    }

    private Selenium selenium ()
    {
        return _selenium;
    }

    public void setReplaceStringMap (Map aMap)
    {
        _replaceStringMap = aMap;
    }

    public void setSuiteName (String suite)
    {
        _suite = suite;
    }

    public void setTestName (String test)
    {
        _test = test;
    }

    public void setPackageName (String packageName)
    {
        _package = packageName;
    }

    public Map<String, String> getReplaceStringMap ()
    {
        return _replaceStringMap;
    }

    public String getSuiteName ()
    {
        return _suite;
    }

    public String getTestName ()
    {
        return _test;
    }

    public String getPackageName ()
    {
        return _package;
    }

    public String getTestId ()
    {
        String testId = null;
        if (!StringUtil.nullOrEmptyOrBlankString(_csvFilename)) {
            testId = _csvFilename.replaceAll("\\\\", "/");
            //strip out .csv extension
            if (testId.endsWith(".csv")) {
                testId = testId.substring(0, testId.length() - 4);
            }
            int index = testId.lastIndexOf('/');
            if (index > 0) {
                String filePath = testId.substring(0, index);
                String fileName = testId.substring(index+1, testId.length());
                // Replaces path separator by suite separtor . in the suite name
                filePath = filePath.replaceAll("/", ".");
                // replaces all . in the filename name by _
                // so that there is no confusion withsuite separator
                testId = Fmt.S("%s#%s", filePath, fileName); 
            }
        }
        return testId;
    }


    public SeleniumCSVTestCase getSeleniumCSVTestCase ()
    {
        if (_seleniumCSVTestCase == null) {
            _seleniumCSVTestCase = new SeleniumCSVTestCase(_csvFilename);
        }
        return _seleniumCSVTestCase;
    }

    private String getShortTestId ()
    {
        return FrameworkUtil.getTestShortIdFromId(getTestId());
    }

    public boolean isRerunningFailedTest ()
    {
        return _rerunningFailedTest;
    }
    
    public String toString ()
    {
        return Fmt.S("%s(Package: %s, Suite: %s)", getTestName(),
                     getPackageName(), getSuiteName());
    }

    public Throwable runSeleniumTest ()
    {
      System.out.println("I Got Here.");

        try {
            boolean skipTearDown = false;
            ThrowableWrapper thWrapper = null;

            for (int i = 0; i < _seleniumCSVTestCase.getNumberOfCommands(); i++)
            {
                System.out.println("I Got Here: " + i);

                SeleniumCSVLine commandLine = _seleniumCSVTestCase
                    .getCommandLine(i);

                int lineNumber = commandLine.getLineNumber();
                _currentCommandLineNumber = lineNumber;
                String command = commandLine.getCommand();

                System.out.println("What is the command line:" + command);
                String target = commandLine.getTarget();
                String value = commandLine.getValue();
                
                // Performs the replacements
                commandLine.processCommandReplacements(_replaceStringMap);

                if (!target.equals(commandLine.getTarget())) {
                    log(Fmt.S("Target String: [%s] replaced by [%s]",
                            target, commandLine.getTarget()));
                }
                if (!value.equals(commandLine.getValue())) {
                    log(Fmt.S("Value String: [%s] replaced by [%s]",
                                value, commandLine.getValue()));
                }

                // Update the local variables with the replaced values
                target = commandLine.getTarget();
                value = commandLine.getValue();
                 
                // set the command start time and expected end time
                synchronized(_commandTime) {
                    _commandTime.setStartTime(System.currentTimeMillis());
                    _commandTime.setEndTime(_timeOut);
                }
                Throwable th = null;

                //We verifying if we should get the session information
                //We can't get the session info on a sequence commandX + waitForPageToLoad.
                //Because the getCookie resets the pageLoaded flag in the awpage we
                //can't call it in case the nextCommand is waitForPageToLoad because in
                //case the page loads fast (before getCoookie executes), waitForPageToLoad will
                //hang,timeout and fail the test thinking that the page is not yet loaded
                //(flag pageLoaded being reset by getCookie).
                if (i+1 < _seleniumCSVTestCase.getNumberOfCommands()) {
                    SeleniumCSVLine nextCommandLine =
                        _seleniumCSVTestCase.getCommandLine(i+1);
                    String nextCommand = nextCommandLine.getCommand();
                    if ("WAITFORPAGETOLOAD".equals(nextCommand)) {
                        commandLine.setPageLocalCommand(false);
                    }
                }
                
                try {
                  
                    // Update the local variables in case the callback modified the realm
                    target = commandLine.getTarget();
                    value = commandLine.getValue();

                    log(Fmt.S(commandLine.getFormattedString()));

                    if (command.equals("OPEN")) {
                        String rewrittenUrl = rewriteUrl(getShortTestId(),
                                                         target);
                        rewrittenUrl = addTestInfoToUrl(rewrittenUrl);
                        selenium().open(rewrittenUrl);
                    }
                    else if (command.equals("TYPE")) {
                        selenium().type(target, value);
                    }
                    else if (command.equals("CLICK")) {
                    
                        selenium().click("//*"+target);
                    }
                    else if (command.equals("CLICKANDWAIT")) {
                
                        selenium().click(target);
                        // Special hack
                        if (!target.matches(".*TestPane:PopupMenuLink.*")) {
                            waitForPageToLoad(_timeOut, false);
                        }
                        else {
                            log(Fmt.S("Skip waitForPage to load on TA link"));
                        }
                    }
                    else if (command.equals("KEYPRESS")) {
                        selenium().keyPress(target, value);
                    }
                    else if (command.equals("KEYPRESSANDWAIT")) {
                        selenium().keyPress(target, value);
                        waitForPageToLoad(_timeOut, false);
                    }
                    else if (command.equals("SELECT")) {
                        selenium().select(target, value);
                    }
                    else if (command.equals("SELECTANDWAIT")) {
                        selenium().select(target, value);
                        waitForPageToLoad(_timeOut, false);
                    }
                    else if (command.equals("CHECK")) {
                        selenium().check(target);
                    }
                    else if (command.equals("CHECKANDWAIT")) {
                        if (!selenium().isChecked(target)) {
                            selenium().check(target);
                            waitForPageToLoad(_timeOut, false);
                        }
                    }
                    else if (command.equals("UNCHECK")) {
                        selenium().uncheck(target);
                    }
                    else if (command.equals("UNCHECKANDWAIT")) {
                        if (selenium().isChecked(target)) {
                            selenium().uncheck(target);
                            waitForPageToLoad(_timeOut, false);
                        }
                    }
                    else if (command.equals("WAITFORPAGETOLOAD")) {
                        // Use i here, not lineNumber. i counts command lines
                        // which is what we want;
                        // lineNumber counts the number of lines in the file
                        // including comments.
                        if (i > 0
                            && "CLICKANDWAIT".equals(_seleniumCSVTestCase
                                .getCommandLine(i - 1).getCommand()))
                        {
                            log(Fmt.S("Skip redundant waitForPageToLoad after " +
                                    "clickAndWait: line: %s", i));
                        }
                        else {
                            waitForPageToLoad(target);
                        }
                    }
                    else if (command.equals("TYPEFILENAME")) {
                        String fileURL = createFileURL(value);
                        //selenium().typefilename(target, fileURL);
                        selenium().type(target, fileURL);
                    }
                    else if (command.equals("SETTIMEOUT")) {
                        _timeOut = getTimeout(target, true);
                        selenium().setTimeout(_timeOut);
                    }
                    else if (command.equals("ASSERTTEXT")) {
                        String text = selenium().getText(target);
                        if (text == null || !text.trim().equals(value)) {
                            throw new SeleniumException(
                                Fmt.Si("assertText Failed in {0}, line {1}",
                                        _csvFilename, lineNumber));
                        }
                    }
                    else if (command.equals("ASSERTVALUE")) {
                        String text = selenium().getValue(target);
                        if (text == null || !text.trim().equals(value)) {
                            throw new SeleniumException(
                                Fmt.Si("assertValue Failed in {0}, line {1} : " +
                                        "Expected value - {2} and Found value - {3}",
                                        _csvFilename, lineNumber, text, value));
                        }
                    }
                    else if (command.equals("ASSERTTEXTPRESENT")) {
                        if (!selenium().isTextPresent(target)) {
                            // throw exception upon command failure
                            throw new SeleniumException(
                                Fmt.Si("assertTextPresent Failed in {0}, line {1}",
                                        _csvFilename, lineNumber));
                        }
                    }
                    else if (command.equals("ASSERTTEXTNOTPRESENT")) {
                        if (selenium().isTextPresent(target)) {
                            // throw exception upon command failure
                            throw new SeleniumException(
                                Fmt.Si("assertTextNotPresent Failed in {0}, line {1}",
                                        _csvFilename, lineNumber));
                        }
                    }
                    else if (command.equals("ASSERTELEMENTPRESENT")) {
                        if (!selenium().isElementPresent(target)) {
                            // throw exception upon command failure
                            throw new SeleniumException(
                                Fmt.Si("assertElementPresent Failed in {0}, line {1}",
                                        _csvFilename, lineNumber));
                        }
                    }
                    else if (command.equals("ASSERTELEMENTNOTPRESENT")) {
                        if (selenium().isElementPresent(target)) {
                            // throw exception upon command failure
                            throw new SeleniumException(
                                Fmt.Si("assertElementNotPresent Failed in {0}, line {1}",
                                        _csvFilename, lineNumber));
                        }
                    }
                    else if (command.equals("ASSERTCHECKED")) {
                        if (!"on".equals(selenium().getValue(target))) {
                            // throw exception upon command failure
                            throw new SeleniumException(
                                Fmt.Si("assertChecked Failed in {0}, line {1}",
                                        _csvFilename, lineNumber));
                        }
                    }
                    else if (command.equals("ASSERTNOTCHECKED")) {
                        if (!"off".equals(selenium().getValue(target))) {
                            // throw exception upon command failure
                            throw new SeleniumException(
                                Fmt.Si("assertNotChecked Failed in {0}, line {1}",
                                        _csvFilename, lineNumber));
                        }
                    }
                    else if (command.equals("WAITFORELEMENTPRESENT")) {
                        selenium().waitForCondition(
                            Fmt.S("selenium.isElementPresent(\"%s\");",
                                  target), _timeOut);
                    }
                    else if (command.equals("ASSERTSELECTOPTIONS")) {
                        String[] actualList = selenium().getSelectOptions(target);
                        String[] expectedList = StringUtil.delimitedStringToArray(value,
                                                                                  ',');
                        if (!ArrayUtil.arrayEquals(expectedList, actualList)) {
                            // throw exception upon command failure
                            throw new SeleniumException(
                                Fmt.Si("assertSelectOptions Failed in {0}, " +
                                        "line {1}. {2}. {3}", _csvFilename,
                                        lineNumber, ArrayUtil.formatArray("Expected:",
                                                                          expectedList),
                                        ArrayUtil.formatArray("Actual:",
                                                              actualList)));
                        }
                    }
                    else if (command.equals("ADDSELECTION")) {
                        selenium().addSelection(target, value);
                    }
                    else if (command.equals("REMOVESELECTION")) {
                        selenium().removeSelection(target, value);
                    }
                    else if (command.equals("WAITFORPOPUP")) {
                        String timeout = getTimeout(value, true);
                        if (!_timeOut.equals(timeout)) {
                            // update the end time
                            synchronized(_commandTime) {
                                _commandTime.setEndTime(timeout);
                            }
                        }
                        selenium().waitForPopUp(target, value);
                    }
                    else if (command.equals("SELECTWINDOW")) {
                        selenium().selectWindow(target);
                    }
                    else if (command.equals("SELECTFRAME")) {
                        selenium().selectFrame(target);
                    }
                    else {
                        throw new SeleniumCSVException(
                            Fmt.Si("Unknown Selenium command ''{0}'' in {1}, line {2}",
                                   command, _csvFilename, lineNumber));
                    }
                }
                catch (UnsupportedOperationException e) {
                    // These are known to be infrastructure errors (remote
                    // control connection failed, etc.); let them pass
                    // through. There are probably others we need to
                    // handle too.
                    th = e;
                    commandLine.setPageLocalCommand(false);
                }
                catch (SkipTestException e) {
                    // encounter SkipTestException, just re-throw it
                    th = e;
                    commandLine.setPageLocalCommand(false);
                }
                catch (Exception e) { // OK
                	  System.out.println("error is: " + e.toString());
                	  
                    commandLine.setPageLocalCommand(false);
                    // if permission denied, ignore the exception and continue
                    // the execution.
                    if (e.getMessage() != null
                        && e.getMessage().equalsIgnoreCase("Permission denied"))
                    {
                        log(Fmt.S("%s Permission denied Exception: Ignore",
                                  e.getMessage()));
                    }
                    else {
                        // Here we handle different types of exceptions
                        // as we do not have special exceptions for failures considered Infrastructure
                        // we have to look at the messages...
                        if (_finish) {
                            // this is the case that the command should time out now
                            // but Selenium RC server is still waiting and the
                            // browser
                            // may crash already, so we've set the flag in
                            // testSelenium() to quit
                            skipTearDown = true;
                            Exception exp =
                                new SeleniumInternalException(_timeoutErrMsg);
                            logError(Fmt.S("%s: line: %s ;command: %s ;target: %s ;" +
                                    "value: %s.",_timeoutErrMsg, lineNumber, command,
                                    target, value), exp);
                            th = exp;
                        }
                        else if (e.getMessage() != null  &&
                                 e.getMessage().indexOf(
                                        "Connection refused: connect") >= 0) {
                            // Here we have a communication issue with selenium server
                            skipTearDown = true;
                            Exception exp =
                                new SeleniumInternalException(_communicationErrMsg);
                            logError(Fmt.S("%s: line: %s ;command: %s ;target: %s ;" +
                                           "value: %s.",_communicationErrMsg,
                                           lineNumber, command, target, value), exp);
                            th = exp;
                        }
                        else {
                            // Otherwise, assume it's a test failure, not an
                            // infrastructure error.
                            log(Fmt.S("Selenium command failed***: %s", e.getMessage()));

                            th = new SeleniumTestError("Test case failure: ", e.getMessage());
                        }
                    }
                }
                finally {
                    // clear out the start time and end time
                    synchronized(_commandTime) {
                        _commandTime.clear();
                    }
                }

                if (th != null) {
                    if (thWrapper != null) {
                        // error in the tear down section, so we can't continue
                        thWrapper.addThrowable(th);
                        break;
                    }

                    if (!skipTearDown) {
                        int tearDownStartLineNumber =
                                _seleniumCSVTestCase.getTearDownStartLineNumber();
                        if (tearDownStartLineNumber > -1 && tearDownStartLineNumber > i) {
                            log(
                            Fmt.S("Executing tear down steps starting at line %s ...",
                                _seleniumCSVTestCase.getCommandLine(
                                            tearDownStartLineNumber).getLineNumber()));
                            thWrapper = new ThrowableWrapper(th);
                            Exception ex = goToTestCentralPage();
                            if (ex != null) {
                                // error while going to the Test Central page, so we can't continue
                                thWrapper.addThrowable(ex);
                                break;
                            }
                            else {
                                // jump to the start of the tear down section
                                i = tearDownStartLineNumber - 1;
                                continue;
                            }
                        }
                    }

                    return th;
                }
            }

            if (thWrapper != null) {
                return thWrapper;
            }

        }
        catch (Exception e) {
            log(Fmt.S("WARNING! An exception occurs in runSeleniumTest(): %s", e));
            return e;
        }

        return null;
    }

    private Exception goToTestCentralPage ()
    {
        log("Try going to the Test Central page");
        try {
            selenium().click(LOCATOR_TA_PANE);
            selenium().click(LOCATOR_TEST_CENTRAL);
            waitForPageToLoad(_timeOut, false);
        }
        catch (Exception e) {
            log(Fmt.S("An exception occurs while going to the Test Central page: %s", e));
            return e;
        }

        return null;
    }

    private String addTestInfoToUrl (String url)
    {
        String separator = "?";
        String pattern = "%s%stestId=%s&testShortId=%s&testLineNb=%s";
        if (url.lastIndexOf('?') > 0) {
            if (!url.endsWith("&")) {
                separator = "&";
            }
            else {
                separator = "";
            }
        }
        try {
            url = Fmt.S(pattern, url, separator,
                    URLEncoder.encode(getTestId(), "UTF-8"),
                    URLEncoder.encode(getShortTestId(), "UTF-8"),
                    _currentCommandLineNumber);
        }
        catch (UnsupportedEncodingException e) {
            logError("Cannot add test information to URL.",e);
        }
        return url;
    }

  
    private String getTestCSVName ()
    {
        String testCaseString = _csvFilename;
        // strip off the .csv
        int indexOfLastDot = _csvFilename.lastIndexOf(".");
        if (indexOfLastDot != -1) {
            testCaseString = _csvFilename.substring(0, indexOfLastDot);
        }

        // strip off the leading directories - we're being defensive that could
        // be slash or backslash.
        int indexOfLastSlash = testCaseString.lastIndexOf("/");
        int indexOfLastBackSlash = testCaseString.lastIndexOf("\\");
        int endOfDirectories = Math.max(indexOfLastSlash, indexOfLastBackSlash);
        if (endOfDirectories > -1) {
            testCaseString = testCaseString.substring(endOfDirectories + 1,
                                                      testCaseString.length());
        }
        return testCaseString;
    }

  

    private static final String OBJECT_NOT_FOUND_STRING = 
            "ERROR: Element [@awname=";
    private static final String TEXT_PRESENT_FAILURE_STRING =
            "assertTextPresent Failed";
    private static final String TEXT_NOT_PRESENT_FAILURE_STRING =
            "assertTextNotPresent Failed";

   

    /**
     * Checks to see if the currently displayed page is a system error page. If
     * it is it extracts the stack information from the page and returns it.
     *
     * @return null if no error page, error information if the page was the
     *         error page.
     */
    private void checkForErrorPage () throws SkipTestException,
        ErrorPageException
    {
        String locator = Fmt.S("[@awname='%s']",
                               TestExceptionInfo.AWNAME_LOCATOR);
        if (selenium().isElementPresent(locator)) {
            // this is the error page (see AWExceptionBodyRenderer and
            // TestExceptionInfo)
            Map<String, String> infoMap = MapUtil.map();
            for (int i = 0; i < TestExceptionInfo.TABLE_ROWS; i++) {
                String key = selenium().getTable(Fmt.S("%s.%s.0", locator, i));
                String value = selenium()
                    .getTable(Fmt.S("%s.%s.1", locator, i));
                if (!StringUtil.nullOrEmptyOrBlankString(key)) {
                    infoMap.put(key, value);
                }
            }

            // throw SkipTestException if it exists
            if (SkipTestException.ClassName.equals(infoMap
                .get(TestExceptionInfo.CLASS_NAME_KEY)))
            {
                log("Encounter SkipTestException: skip the test");
                throw new SkipTestException(infoMap
                    .get(TestExceptionInfo.MESSAGE_KEY));
            }
            else if (SkipTestException.ClassName.equals(infoMap
                .get(TestExceptionInfo.ROOT_CAUSE_CLASS_NAME_KEY)))
            {
                log("Encounter SkipTestException: skip the test");
                throw new SkipTestException(infoMap
                    .get(TestExceptionInfo.ROOT_CAUSE_MESSAGE_KEY));
            }

            // otherwise throw ErrorPageException with the stack trace
            String stackTrace = infoMap.get(TestExceptionInfo.STACK_TRACE_KEY);
            ErrorPageException xcp = new ErrorPageException("Application Error");
            xcp.setPageStackTrace(stackTrace);
            throw xcp;
        }
    }

    private String createFileURL (String pathFromScript)
    {
        return baseUrl + pathFromScript;
    }


    public void waitForPageToLoad (String timeout) throws SkipTestException,
        ErrorPageException
    {
        waitForPageToLoad(timeout, true);
    }
    /**
     * ON IE browser, most of the permission denied exceptions are caused during
     * selenium call for waitForPageToLoad. As a solution to handle this
     * situation and continuing the script execution ignoring this message, we
     * are using selenium's waitForCondition to check whether the page is loaded
     * completely of not in case we get Permission denied error during page
     * load. watiForCondition guarantees that page Load is complete in case
     * waitForPageToLoad fails with Permission denied error.
     *
     * @param timeout max time to wait before a SeleniumException is thrown
     * @param applyTimeoutRatio when using timeout, should apply timeoutRatio.
     *                          In most cases where the waitForPageToLoad has a defined timeout,
     *                          it should be true. But if you are using the default from a
     *                          SetTimeOut command or the default timeout value,
     *                          the timeoutRatio has already been applied and
     *                          you don't want to apply more than once.
     * @throws SkipTestException if the new page contains an Ariba stack trace
     *             caused by a SkipTestException
     * @throws ErrorPageException if the new page contains an Ariba stack trace
     */
    public void waitForPageToLoad (String timeout, boolean applyTimeoutRatio)
            throws SkipTestException, ErrorPageException
    {
        try {
            timeout = getTimeout(timeout, !_timeOut.equals(timeout), applyTimeoutRatio);
            if (!_timeOut.equals(timeout)) {
                // update the end time
                synchronized(_commandTime) {
                    _commandTime.setEndTime(timeout);
                }
            }
            selenium().waitForPageToLoad(timeout);
        }
        catch (SeleniumException e) {
            if (e.getMessage().equalsIgnoreCase("Permission denied")) {
                log(Fmt.S("%s Permission denied Exception at " +
                        "waitForPageLoadToCommand:Ignore", e.getMessage()));

                selenium().waitForCondition("selenium.doGetAribaPageLoadedStatus() && "
                                          + "selenium.doGetAribaPageLoadedStatus()",
                                          timeout);
            }
            else {
                throw e;
            }
        }
        checkForErrorPage();

    }

    private String getTimeout (String timeoutStr,
                               boolean checkTimeout) throws NumberFormatException
    {
        return getTimeout(timeoutStr, checkTimeout, true);
    }

    private String getTimeout (String timeoutStr, boolean checkTimeout,
                               boolean applyTimeoutRatio) throws NumberFormatException
    {
        try {
            long timeout = Long.parseLong(timeoutStr);
            if (checkTimeout) {
                if (timeout > 1800000) {
                    log(Fmt.S("Found long timeout > 30m: %s: %s", timeoutStr,
                              getTestId()));
                }
                else if (timeout > 3600000) {
                    log(Fmt.S("Found long timeout > 1h: %s: %s", timeoutStr,
                              getTestId()));
                }
            }
           /* if (applyTimeoutRatio) {
                double ratio = SeleniumTestEnvUtil.getSeleniumTimeoutRatio();
                if (ratio != 1) {
                    long newTimeout = Math.round(timeout * ratio);
                    String newTimeoutStr = Long.toString(newTimeout);
                    log(Fmt.S("Replacing timeout %s ms by %s ms", timeout,
                              newTimeoutStr));
                    timeoutStr = newTimeoutStr;
                }
            }*/
            return timeoutStr;
        }
        catch (NumberFormatException e) {
            log(Fmt.S("Invalid timeout %s.", timeoutStr));
            throw e;
        }
    }

    private class ErrorPageException extends Exception
    {
        public ErrorPageException (String msg)
        {
            super(msg);
        }

        private String _pageStackTrace = null;

        public void setPageStackTrace (String trace)
        {
            // need to replace the \n with <br/>
            _pageStackTrace = trace;
        }

        public String getPageStackTrace ()
        {
            return _pageStackTrace;
        }
    }

    public String escapeJSValue (String value) {
        if (value == null) {
            return null;
        }
        return value.replaceAll("\"", "\\\"");
    }

  
    private static String rewriteUrl (String shortTestId, String target)
    {
        String url = target;
        for (RewriteRule r : _rewriteRules) {

            url = r.rewrite(shortTestId, url);

            if (r.isLast()) {
                break;
            }
            if (r.isNext()) {
                url = rewriteUrl(shortTestId, url);
                break;
            }

        }
        return url;
    }

    public void log (String logMessage)
    {
        System.out.println(Fmt.S("[TID:%s] %s", getShortTestId(), logMessage));
    }

    public void logError (String logMessage, Exception e)
    {
        System.out.println(Fmt.S("[TID:%s] %s", getShortTestId(), logMessage) +":\n"+  e);
    }

    private static class RewriteRule
    {
        private Pattern _pattern;
        private String _replacement;

        // Mostly so we can print it for debugging
        private String _flagsString = Constants.EmptyString;

        private boolean _flagLast;
        private boolean _flagNoCase;
        private boolean _flagNext;

        public RewriteRule (List line)
        {
            if (line.size() < 2 || line.size() > 3) {
                throw new RuntimeException("Invalid line");// OK
            }
            String pattern = (String)line.get(0);
            String replacement = (String)line.get(1);

            if (line.size() > 2) {
                _flagsString = (String)line.get(2);
                String[] flags = _flagsString.split("\\|");
                for (String flag : flags) {
                    if (flag.equals("L")) {
                        _flagLast = true;
                    }
                    else if (flag.equals("NC")) {
                        _flagNoCase = true;
                    }
                    else if (flag.equals("N")) {
                        _flagNext = true;
                    }
                    else {
                        throw new RuntimeException(Fmt.S("Invalid flag %s",
                                                         flag));// OK
                    }
                }
            }

            int flags = 0;
            if (_flagNoCase) {
                flags = Pattern.CASE_INSENSITIVE;
            }

            _pattern = Pattern.compile(pattern, flags);
            _replacement = replacement;
            System.out.format("Added Selenium URL rewrite rule %s -> %s (%s)",
                              _pattern, _replacement, _flagsString);
        }

        public boolean isLast ()
        {
            return _flagLast;
        }

        public boolean isNext ()
        {
            return _flagNext;
        }

        public String rewrite (String shortTestId, String url)
        {

            // log(Fmt.S("Checking pattern %s against string %s", _pattern,
            // url));
            Matcher m = _pattern.matcher(url);
            if (m.find()) {
                String result = m.replaceAll(_replacement);
                System.out.format(Fmt.S("[TID:%s] Rewrote %s to %s using %s -> %s (%s)",
                                        shortTestId, url, result, _pattern,
                                        _replacement, _flagsString));
                return result;
            }
            return url;
        }
    }

    private class CommandTime
    {
        private static final long _defaultTimeout = 60000;

        // give the timeout a 5 secs buffer
        private static final long _bufferTimeout = 5000;

        private Long _startTime;
        private Long _endTime;

        public void setStartTime (Long startTime)
        {
            _startTime = startTime;
        }

        public void setEndTime (Long endTime)
        {
            _endTime = endTime;
        }

        public void setEndTime (String timeout)
        {
            try {
                _endTime = _startTime + Long.parseLong(timeout)
                    + _bufferTimeout;
            }
            catch (NumberFormatException e) {
                log(Fmt.S("Cannot parse timeout string: %s, using default timeout " +
                        "instead: %s", timeout, _defaultTimeout));
                _endTime = _startTime + _defaultTimeout + _bufferTimeout;
            }
        }

        public Long getStartTime ()
        {
            return _startTime;
        }

        public Long getEndTime ()
        {
            return _endTime;
        }

        public void clear ()
        {
            _startTime = null;
            _endTime = null;
        }
    }
}
