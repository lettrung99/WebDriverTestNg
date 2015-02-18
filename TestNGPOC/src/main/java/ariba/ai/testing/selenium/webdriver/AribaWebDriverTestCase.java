package ariba.ai.testing.selenium.webdriver;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;

import ariba.ai.testing.selenium.rc.SeleniumCSVLine;
import ariba.ai.testing.selenium.rc.SeleniumCSVTestCase;
import ariba.util.core.Constants;
import ariba.util.core.Fmt;
import ariba.util.core.ListUtil;

public class AribaWebDriverTestCase extends SeleniumBase {

	private String baseUrl;
	private String _csvFileName;
	private SeleniumCSVTestCase _seleniumCSVTestCase = null;
	protected final Logger logger = Logger.getLogger(getClass());;
	private int _currentCommandLineNumber;

	public AribaWebDriverTestCase(WebDriver driver, String baseUrl,
			String _csvFileName) {
		super(driver);
		this.baseUrl = baseUrl;
		this._csvFileName = _csvFileName;

	}

	public SeleniumCSVTestCase getSeleniumCSVTestCase() {
		if (_seleniumCSVTestCase == null) {
			_seleniumCSVTestCase = new SeleniumCSVTestCase(_csvFileName);
		}
		return _seleniumCSVTestCase;
	}

	// TODO
	// This is a map of strings that we will replace (if present) in either the
	// target string or the value string.
	// Used primarily to replace a canonical realm with a different realm name.
	// Ex: "S4All" substring
	// will be replaced by "S4All-1". For parallelization, multiple realms with
	// the same canonical form have
	// been created for some of the most commonly used realms.
	private Map<String, String> _replaceStringMap = null;
	

	public void runSeleniumCsv() throws Exception {

		try {

			_seleniumCSVTestCase = getSeleniumCSVTestCase();

			for (int i = 0; i < _seleniumCSVTestCase.getNumberOfCommands(); i++) {
				SeleniumCSVLine commandLine = _seleniumCSVTestCase.getCommandLine(i);

				int lineNumber = commandLine.getLineNumber();
				_currentCommandLineNumber = lineNumber;
				String command = commandLine.getCommand();

				System.out.println("What is the command line:" + command);
				String target = commandLine.getTarget();
				String value = commandLine.getValue();

				// Performs the replacements
				commandLine.processCommandReplacements(_replaceStringMap);
				if (!target.equals(commandLine.getTarget())) {
					log(Fmt.S("Target String: [%s] replaced by [%s]", target,
							commandLine.getTarget()));
				}
				if (!value.equals(commandLine.getValue())) {
					log(Fmt.S("Value String: [%s] replaced by [%s]", value,
							commandLine.getValue()));
				}
				// Update the local variables with the replaced values
				target = commandLine.getTarget();
				value = commandLine.getValue();

				log("Current Line #" + _currentCommandLineNumber + ":  " + Fmt.S(commandLine.getFormattedString()));
				if (command.equals("OPEN")) {
	        System.out.println("Url: " + baseUrl+target);
					open(baseUrl+target);
				}else if (command.equals("CLICK")) {
          click(AwBy.awname(target));
				}else if (command.equals("CLICKANDWAIT")) {          
        	click(AwBy.awname(target)); 
        	waitForPageToLoad(10);
				}
				else if (command.equals("WAIT")) {          
					implicitWaits(Integer.valueOf(target));
				}

			}

		}
		catch (Exception e) {
			logger.error(e.toString());
			throw e;
		}

	}

	public void log(String logMessage) {
		logger.info(Fmt.S("[TID:%s] %s", Thread.currentThread().getName(),
				logMessage));
	}
	
	


}
