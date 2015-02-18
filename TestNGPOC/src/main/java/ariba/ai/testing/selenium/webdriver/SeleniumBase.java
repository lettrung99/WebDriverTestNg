package ariba.ai.testing.selenium.webdriver;


import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.openqa.selenium.Alert;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.JavascriptExecutor;

import com.google.common.base.Predicate;

/**
 * TODO:  trying something quick.
 * @author i845198
 *
 */
public abstract class SeleniumBase {

	private WebDriver driver;

	public final static int GLOBAL_SHORT_SLEEP = 1;
	public final static int GLOBAL_MED_SLEEP = 3;
	public final static int GLOBAL_LONG_SLEEP = 10;

	// Look for this string on a page to identify if a build error has occurred.
	public final static String BUILD_ERROR_STRING = "Show Raw Stack Trace";

	public static int defaultExplicitWaitTimeout = 10;
	protected final Logger logger = Logger.getLogger(getClass());;
	protected String _logTag;

	public SeleniumBase (WebDriver driver)
	{
		this.driver = driver;
	}

	protected void open(String url){
		driver.get(url);
	}
	/**
	 * Click based on AwBy
	 * 
	 * @param webElement
	 * @throws Exception 
	 */
	protected void click (AwBy by) throws Exception
	{
		clickNoCapture(by);
	}

	/**
	 * Clicks an element without capturing a screen.
	 * 
	 * @param by
	 * @throws Exception 
	 */
	protected void clickNoCapture (AwBy by) throws Exception
	{
		click(findElement(by));
	}

	/**
	 * Moves the moves pointer to the center of the element with the locator.
	 * 
	 * @param by
	 * @throws Exception 
	 */
	protected void hoverOverElement (AwBy by) throws Exception
	{
		// TODO fix method so that it actually captures the specified element,
		// it does not do this currently
		Actions actions = new Actions(getDriver());
		WebElement element = findElement(by);
		actions.moveToElement(element);
		actions.perform();
		this.waitForPageToLoad(20);
	}

	/**
	 * First tries to find and click by1. If that's not present then tries to
	 * click by2
	 * 
	 * @param webElement
	 * @param webElementB
	 * @throws Exception 
	 */
	protected void click (AwBy by1, AwBy by2) throws Exception
	{
		if (isElementPresent(by1)) {
			click(by1);
		}
		else if (isElementPresent(by2)) {
			click(by2);
		}
		else {
			logger.error(_logTag + "None of the elements exist!");
		}
	}

	/**
	 * Click based on WebElement
	 * 
	 * @param webElement
	 * @throws Exception 
	 */
	protected boolean click (WebElement webElement) throws Exception
	{
		try {
			String awName = webElement.getAttribute("awname");
			webElement.click();
			logger.debug(_logTag + " Webelement: " + awName + " clicked.");
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * Enter text into fields
	 * 
	 * @param by
	 * @param text
	 * @throws Exception 
	 */
	protected void enter (AwBy by, String text) throws Exception
	{
		enter(findElement(by), text);
	}

	/**
	 * Enter text into fields without clearing the existing contents first.
	 * 
	 * @param by
	 * @param text
	 * @throws Exception 
	 */
	protected void enterNoClear (AwBy by, String text) throws Exception
	{
		enterNoClear(findElement(by), text);
	}

	/**
	 * Enter text into fields without clearing the existing contents first.
	 * 
	 * @param by
	 * @param text
	 * @throws Exception 
	 */
	private void enterNoClear (WebElement webElement, String text) throws Exception
	{
		enterKeys(webElement, text);
	}

	private void enter (WebElement webElement, String text) throws Exception
	{
		webElement.clear();
		enterKeys(webElement, text);
	}

	/**
	 * Send keys to the element
	 * 
	 * @param by
	 * @param keysToSend
	 * @throws Exception 
	 */
	protected void enterKeys (AwBy by, Keys keysToSend) throws Exception
	{
		enterKeys(findElement(by), keysToSend);
	}

	private void enterKeys (WebElement webElement, String text) throws Exception
	{
		try {
			webElement.sendKeys(text);
			logger.debug(_logTag + " Entered: \"" + text + "\" into Webelement: "
					+ webElement.getAttribute("awname"));
		}
		catch (Exception e) {
			throw e;
		}
	}

	private void enterKeys (WebElement webElement, Keys keysToSend) throws Exception
	{
		try {
			String awName = webElement.getAttribute("awname");
			webElement.sendKeys(keysToSend);
			logger.debug(_logTag + "Entered: \"" + keysToSend + "\" into Webelement: "
					+ awName);
		}
		catch (Exception e) {
			throw e;
		}
	}

	/**
	 * Finds an element.
	 * 
	 * @param by
	 * @return the WebElement or null if the WebElement was not found.
	 * @throws Exception 
	 */
	protected WebElement findElement (AwBy by) throws Exception
	{
		WebElement element = null;
		try {
			element = by.findElement(driver);

		}
		catch (Exception e) {

			e.printStackTrace();
			throw e;
		}
		return element;
	}

	/**
	 * Finds all elements matching the AwBy.
	 * 
	 * @param by
	 * @return a list of elements that match the AwBy.
	 */
	protected String findElementText (AwBy by, String pattern)
	{
		List<WebElement> elements = driver.findElements(by);
		for (WebElement element : elements) {
			String text = element.getText();
			if (text.matches(pattern))
				return text;
		}
		// TODO throw exception maybe
		return null;
	}

	/**
	 * Scrolls up a page element such as a list box given the element and the
	 * number of times to send a page up
	 * 
	 * @param element
	 * @param pages
	 * @throws Exception 
	 */
	protected void scrollUpPage (AwBy by, int pages) throws Exception
	{
		try {
			WebElement element = this.findElement(by);
			for (int i = 0; i < pages; i++) {
				element.sendKeys(Keys.PAGE_UP);
				this.waitForAjax(1, 30);

				logger.debug(_logTag + "Webelement: PageUp Sent.");
			}

		}
		catch (Exception e) {
			throw e;
		}

	}

	/**
	 * Scrolls down a page element such as a list box given the element and the
	 * number of times to send a page down
	 * 
	 * @author aoesterholm
	 * @param element
	 * @param pages
	 * @throws Exception 
	 */
	protected void scrollDownPage (AwBy by, int pages) throws Exception
	{
		scrollDownPage(by, pages, true);
	}

	/**
	 * Same as scrollDownPage but doesn't capture screen.
	 * 
	 * @author aoesterholm
	 * @param element
	 * @param pages
	 * @throws Exception 
	 */
	protected void scrollDownPageNoCapture (AwBy by, int pages) throws Exception
	{
		scrollDownPage(by, pages, false);
	}

	/**
	 * @author aoesterholm
	 * @param by
	 * @param pages
	 * @param capture
	 * @throws Exception 
	 */
	private void scrollDownPage (AwBy by, int pages, Boolean capture) throws Exception
	{
		try {
			WebElement element = this.findElement(by);
			for (int i = 0; i < pages; i++) {
				element.sendKeys(Keys.PAGE_DOWN);
				this.waitForAjax(1, 30);
				logger.debug(_logTag + "Webelement: PageDown Sent.");
			}

		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * Selects an element from a list given the list name, the number of options
	 * in the list and the value of the item you are searching for
	 * 
	 * @param byName
	 * @param options
	 * @param searchText
	 * @throws Exception 
	 * @deprecated use BasePage.click(String awNameSubstring, String
	 *             visibleText) instead.
	 */
	@Deprecated
	protected void clickElementByValue (String byName, int options, String searchText) throws Exception
	{

		String name = byName;
		boolean found = false;
		WebElement element = null;

		while (found != true) {
			for (int i = 1; i <= options; i++) {
				element = findElement(AwBy.awname(name));
				String elementText = element.getText();
				if (searchText.equals(elementText)) {
					click(element);
					found = true;
					break;
				}

				name = byName + "_" + Integer.toString(i);
			}
		}
	}

	protected WebDriver getDriver ()
	{
		return driver;
	}

	protected boolean isElementPresent (AwBy by)
	{
		try {
			by.findElement(driver);
			return true;
		}
		catch (Exception e) {

			return false;
		}

	}

	/**
	 * 
	 * @author aoesterholm
	 * @param by
	 * @throws Exception 
	 */
	protected boolean isElementVisible (AwBy by) throws Exception
	{
		return findElement(by).isDisplayed();
	}

	/**
	 * In aw5 selects an item from a <select> dropdown menu matching the visible
	 * text of the item. In aw6 changes the awname to the aw6 equivalent if
	 * needed and clicks the <div> tag that opens the other <div> tags that is
	 * the drop down menu and then clicks the item.
	 * 
	 * @param by
	 * @param visibleText
	 * @throws Exception 
	 */
	protected void selectByVisibleText (String awname, String visibleText) throws Exception
	{

		WebElement listElement = findElement(AwBy.awname(awname));
		Select droplist = new Select(listElement);
		droplist.selectByVisibleText(visibleText);

		waitForPageToLoad(30);
	}

	protected void selectByVisibleText (AwBy by, String visibleText) throws Exception
	{
		WebElement listElement = findElement(by);

		Select droplist = new Select(listElement);
		droplist.selectByVisibleText(visibleText);

		waitForPageToLoad(30);
	}

	/**
	 * Selects an item from a <select> dropdown menu matching the visible text
	 * of the item.
	 * 
	 * @param by
	 * @param partOfVisibleText
	 * @throws Exception 
	 */
	protected void selectDropdownItemByVisibleTextSubstring (AwBy by,
			String partOfVisibleText) throws Exception
	{
		WebElement listElement = findElement(by);
		Select droplist = new Select(listElement);

		List<WebElement> dropListOptions = droplist.getOptions();
		for (WebElement webElement : dropListOptions) {
			if (webElement.getText().contains(partOfVisibleText)) {
				logger.info(_logTag
						+ "selectByVisibleTextSubstring searched for substring "
						+ partOfVisibleText + ", found :  " + webElement.getText());
				droplist.selectByValue(webElement.getAttribute("value"));

				return;
			}
		}
		waitForPageToLoad(10);
	}

	/**
	 * Selects an item from a <select> dropdown menu matching the index in the
	 * list of the item.
	 * 
	 * @author aoesterholm, rbrady
	 * @param by
	 * @param visibleText
	 * @throws Exception 
	 */
	protected void selectByIndex (AwBy by, int index) throws Exception
	{
		WebElement listElement = findElement(by);
		Select droplist = new Select(listElement);

		droplist.selectByIndex(index);

		logger.info(_logTag + listElement.getAttribute("awname") + " -> Index " + index
				+ " selected.");
		waitForPageToLoad(10);
	}

	/**
	 * wait maxWaitSeconds for a page to load. A load here is either a complete
	 * page load or an AJAX update to part of the page. This method uses a
	 * JavaScript call to window.ariba.Request.isRequestInProgress() to find
	 * ongoing AJAX updates and document.readyState to identify complete page
	 * loads. NOTE! This method only works for AribaWeb pages.
	 * 
	 * @param maxWaitSeconds
	 */
	protected void waitForPageToLoad (int maxWaitSeconds)
	{
		try {
			WebDriverWait waitForPageLoadToFinish = (WebDriverWait)new WebDriverWait(
					driver, maxWaitSeconds).pollingEvery(50, TimeUnit.MILLISECONDS);
			waitForPageLoadToFinish.until(new Predicate<WebDriver>() {
				public boolean apply (WebDriver arg0)
				{
					return (Boolean)((JavascriptExecutor)driver)
							.executeScript("return document.readyState == 'complete' && window.ariba != undefined && window.ariba.Request != undefined && !window.ariba.Request.isRequestInProgress()");
				}
			});
		}
		catch (TimeoutException e) {
			logger.info(_logTag
					+ " Timed out after "
					+ maxWaitSeconds
					+ " seconds while waiting for page to load. If the page has loaded then maybe it isn't an AribaWeb page");
			throw e;
		}
	}
	
	protected void implicitWaits (int maxWaitSeconds) throws InterruptedException
	{
		Thread.sleep(maxWaitSeconds*1000);
	}

	/**
	 * wait maxWaitSeconds for a non-AribaWeb page to load. A load here is a
	 * complete page load. This is determined by when the following javascript
	 * expression returns true: document.readyState == 'complete'.
	 * 
	 * @param maxWaitSeconds
	 */
	protected void waitForNonAribaWebPageToLoad (int maxWaitSeconds)
	{
		try {
			WebDriverWait waitForPageLoadToFinish = (WebDriverWait)new WebDriverWait(
					driver, maxWaitSeconds).pollingEvery(50, TimeUnit.MILLISECONDS);
			waitForPageLoadToFinish.until(new Predicate<WebDriver>() {
				public boolean apply (WebDriver arg0)
				{
					return (Boolean)((JavascriptExecutor)driver)
							.executeScript("return document.readyState == 'complete'");
				}
			});
		}
		catch (TimeoutException e) {
			logger.info(_logTag + " Timed out after " + maxWaitSeconds
					+ " seconds while waiting for non-AribaWeb page to load.");
			throw e;
		}
	}

	/**
	 * Method to wait for a page element to be active or visible User passes
	 * their element they wish to wait for and value for timing out. If a build
	 * exception happens the method will return.
	 * 
	 * @author barobrien
	 * @param waitForElement
	 * @param timeout
	 * @return
	 */
	protected void waitForElementPresent (final String waitForElement, int timeout)
	{
		try {
			WebDriverWait waitForElementPresentOrError = (WebDriverWait)new WebDriverWait(
					driver, timeout).pollingEvery(2, TimeUnit.SECONDS);
			waitForElementPresentOrError.until(new Predicate<WebDriver>() {
				public boolean apply (WebDriver arg0)
				{
					if (isElementPresent(AwBy.awname(waitForElement))) {
						return true;
					}
					// Using string matching against
					// BUILD_ERROR_STRING which appear on the
					// Exception page.
					else if (driver.getPageSource().contains(BUILD_ERROR_STRING)) {
						logger.debug(_logTag + " Build error");
						return true;
					}
					return false;
				}
			});
		}
		catch (TimeoutException e) {
			logger.info(_logTag + " timed out after " + timeout
					+ " seconds while waiting for element to be present.");
			throw e;
		}
	}

	/**
	 * Return the first element that is visible and enables that matches the
	 * AwBy. This is useful in lists with hidden menus for the list items.
	 * 
	 * @author aoesterholm
	 * @param awNameSubstring
	 * @return
	 */
	protected WebElement findFirstVisible (String awNameSubstring)
	{
		List<WebElement> webElements = this
				.findElementsByAwnameSubstring(awNameSubstring);
		for (WebElement e : webElements) {
			if (e.isDisplayed()) {
				return e;
			}
		}
		return null;
	}

	/**
	 * wait maxWaitSeconds for a text field with a chooser drop down to load. A
	 * load here is an AJAX update. This method uses a JavaScript call to
	 * window.ariba.Request.isRequestInProgress() to find ongoing AJAX updates
	 * and document.readyState to identify complete page loads. NOTE! This
	 * method only works for AribaWeb pages.
	 * 
	 * @param maxWaitToStartSeconds
	 * @param maxWaitToFinishSeconds
	 */
	public void waitForAjax (long maxWaitToStartSeconds, long maxWaitToFinishSeconds)
	{
		// Wait for Ajax requests to start
		try {
			WebDriverWait waitForPageLoadToStart = (WebDriverWait)new WebDriverWait(
					driver, maxWaitToStartSeconds)
			.pollingEvery(10, TimeUnit.MILLISECONDS);
			waitForPageLoadToStart.until(new Predicate<WebDriver>() {
				public boolean apply (WebDriver arg0)
				{
					boolean requestInProgress = (Boolean)((JavascriptExecutor)driver)
							.executeScript("return window.ariba.Request.isRequestInProgress()");
					return requestInProgress;
				}
			});
		}
		catch (TimeoutException e) {

			logger.info(_logTag + " Timed out after " + maxWaitToStartSeconds
					+ " seconds while waiting for the ajax requests to start loading.");
			// Not throwing this exception since the worst case is a sleep here.
			return;
		}
		// Then do another wait for Ajax requests to finish
		try {
			WebDriverWait waitForPageLoadToFinish = (WebDriverWait)new WebDriverWait(
					driver, maxWaitToFinishSeconds).pollingEvery(50,
							TimeUnit.MILLISECONDS);
			waitForPageLoadToFinish.until(new Predicate<WebDriver>() {
				public boolean apply (WebDriver arg0)
				{
					boolean requestInProgress = (Boolean)((JavascriptExecutor)driver)
							.executeScript("return window.ariba.Request.isRequestInProgress()");
					return !requestInProgress;
				}
			});
		}
		catch (TimeoutException e) {
			logger.info(_logTag + " Timed out after " + maxWaitToFinishSeconds
					+ " seconds while waiting for the ajax requests to finish.");
			throw e;
		}
	}

	/**
	 * Check to see if a dialog window has appeared. This can be used to check
	 * if build qualification is happening and the browser prompts for the
	 * master password.
	 */
	protected void checkAlert ()
	{
		try {
			WebDriverWait wait = new WebDriverWait(driver, 2);
			wait.until(ExpectedConditions.alertIsPresent());
			Alert alert = driver.switchTo().alert();
			alert.accept();
			logger.debug(_logTag + " Clicked accept in modal dialog");
		}
		catch (Exception e) {
			// e.printStackTrace();
		}
	}

	/**
	 * Check if a checkbox is ticked
	 * 
	 * @author rbrady, aoesterholm
	 * @param by
	 * @return
	 * @throws Exception 
	 */
	public boolean isCheckBoxTicked (AwBy by) throws Exception
	{
		return findElement(by).isSelected();
	}

	/**
	 * Sets the string parameter as the content in all Xinha Rich Text Editors
	 * on the page. The method waits for all the Xinha editors to load before it
	 * sets the content.
	 * 
	 * @param string
	 *            the content to add to all the editors on the page.
	 */
	protected void setXinhaContent (String string)
	{
		JavascriptExecutor js = (JavascriptExecutor)getDriver();
		WebDriverWait waitForXinhaLoadToFinish = new WebDriverWait(getDriver(), 20);
		waitForXinhaLoadToFinish.until(new Predicate<WebDriver>() {
			public boolean apply (WebDriver arg0)
			{
				logger.info(_logTag + "waiting for Xinha Rich Text editor load to start");
				boolean xinhaLoadStarted = (Boolean)(((JavascriptExecutor)getDriver())
						.executeScript("return window.RichTextEditors != undefined"));
				if (xinhaLoadStarted) {
					logger.info(_logTag + "Xinha Rich Text editor has started");
					return (Boolean)(((JavascriptExecutor)getDriver())
							.executeScript("for (var editorId in RichTextEditors)"
									+ "{if(!RichTextEditors[editorId]._iframeLoadDone){"
									+ "return false;}}return true;"));
				}
				return false;
			}
		});
		js.executeScript("for (var editorId in RichTextEditors){RichTextEditors[editorId].setHTML('"
				+ string + "');}");
		return;
	}

	/**
	 * Sleeps the thread for the numberof seconds.
	 * 
	 * @param sec
	 */
	protected static void sleep (int sec)
	{
		try {
			Thread.sleep(sec * 1000);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Uploads a file given the awname of the html <input> element that includes
	 * the Browse button, the title of the window that it opens and the path to
	 * the file to upload
	 * 
	 * @param by
	 * @param window
	 * @param file
	 */

	protected void uploadFile (AwBy by, String window, String file)
	{
		try {
			JavascriptExecutor executor = (JavascriptExecutor)getDriver();

			WebElement element = driver.findElement(by);
			executor.executeScript("arguments[0].click();", element);

			logger.info(_logTag
					+ "Running Command resource/AutoIt/Bin/UploadFile.exe  \"" + window
					+ "\" \"" + file + "\"");

			Runtime.getRuntime().exec(
					"resource/AutoIt/Bin/UploadFile.exe  \"" + window + "\" \"" + file
					+ "\" /c start dir ");

		}
		catch (Exception exc) {/* handle exception */
		}
	}

	/**
	 * When called it will search for all of the i icons a page, Click on them
	 * and take a screenshot of the popup message
	 * 
	 * @author chinchey
	 * @throws Exception 
	 * @deprecated Use clickAll method since the aw names for I icons are
	 *             different on each page
	 */
	protected void captureIIcons () throws Exception
	{
		List<WebElement> listImg = driver.findElements(AwBy
				.xpath("//img[contains(@class,'cueTipIcon w-cueTip-icon')]"));
		logger.info(_logTag + listImg.size() + " i Icons found on page");

		for (int x = 0; x < listImg.size(); x++) {
			logger.info("Element " + x + ": ");
			click(listImg.get(x));
			waitForPageToLoad(40);

		}
	}


	// TODO: Need to think about popup windows etc.
	public Set<String> listAllBrowserWindows ()
	{
		Set<String> set = driver.getWindowHandles();
		Iterator<String> itr = set.iterator();
		while (itr.hasNext()) {
			logger.debug(_logTag + "Browser window:" + itr.next());
		}
		return set;
	}

	public void switchToWindow (String WindowHandle)
	{
		logger.debug(_logTag + "Switching to window:" + WindowHandle);
		driver.switchTo().window(WindowHandle);
	}

	/**
	 * Closes the current window but leaves the browser instance open if it is
	 * not the only window
	 * 
	 */
	public void closeWindow ()
	{
		driver.close();
	}

	/**
	 * Given the AwBy of the link that will open the new pop up window will
	 * click on the link, go to the new page, capture the screen of the new
	 * window and return to the main page
	 * 
	 * @param by
	 * @throws Exception 
	 */

	protected void capturePopUpWindow (AwBy by) throws Exception
	{
		click(by);
		waitForPageToLoad(30);
		Set<String> set = listAllBrowserWindows();
		Iterator<String> itr = set.iterator();
		String originalHandle = itr.next();
		String popUpHandle = "";
		while (itr.hasNext()) {
			popUpHandle = itr.next();
		}
		logger.info(_logTag + "Switching to popup");
		switchToWindow(popUpHandle);
		waitForNonAribaWebPageToLoad(30);
		logger.info(_logTag + "Closing popup");
		closeWindow();
		logger.info(_logTag + "Switching to main page");
		switchToWindow(originalHandle);
		waitForPageToLoad(30);
	}

	/**
	 * Method to click All elements within a page by passing in a common part of
	 * the element name. For Example: this method could be used to click all
	 * tabs at the top of a page. Calls wait for page to load between clicks
	 * 
	 * @author barobrien
	 * @param awNameSubstring
	 * @throws Exception 
	 */
	public void clickAll (String awNameSubstring) throws Exception
	{
		clickAll(awNameSubstring, true, "");
	}

	/**
	 * Method to click all elements in a page. If incrementScreenCount is set to
	 * false the total screenscount used for naming will not increase with each
	 * click, but a different counter will increase that is part of the suffix.
	 * Example: screen1-iIcon1, screen1-iIcon2 The advantage is that a
	 * difference in the number of clicks will not misalign the screens the
	 * follow after clickAll()
	 * 
	 * @author aoesterholm
	 * @param awNameSubstring
	 * @param incrementScreenCount
	 * @param screenNameSuffix
	 * @throws Exception 
	 */
	public void clickAll (String awNameSubstring, boolean incrementScreenCount,
			String screenNameSuffix) throws Exception
	{
		// The secondary screencount is added to the suffix of the screen with
		// the main screencount in the name
		int secondaryScreenCount = 1;
		String clickAllSuffix = "clickAll";
		@SuppressWarnings("deprecation")
		List<WebElement> elements = this.driver.findElements(AwBy
				.xpath("//*[contains(@awname, '" + awNameSubstring + "')]"));
		int elementCount = elements.size();
		while (elementCount != 0) {
			@SuppressWarnings("deprecation")
			List<WebElement> updatedElements = this.driver.findElements(AwBy
					.xpath("//*[contains(@awname, '" + awNameSubstring + "')]"));
			WebElement i = updatedElements.get(elementCount - 1);
			click(i);
			this.waitForPageToLoad(30);
			elementCount--;
		}
	}

	/**
	 * click method that combines awName substring with visible text on page
	 * 
	 * @author rbrady,aoesterholm
	 * @param awNameSubstring
	 * @return WebElement the web element that was clicked or null if no
	 *         matching element was found. This element will most likely be
	 *         stale if you try and interact with it.
	 * @throws Exception 
	 */
	public WebElement click (String awNameSubstring, String visibleText) throws Exception
	{
		@SuppressWarnings("deprecation")
		List<WebElement> elements = this.driver.findElements(AwBy
				.xpath("//*[contains(@awname, '" + awNameSubstring + "')]"));
		for (WebElement element : elements) {
			logger.debug(_logTag + "element text : " + element.getText());
			if (element.getText().equals(visibleText)) {
				click(element);
				return element;
			}
		}
		return null;
	}

	public void clickNoCapture (String awNameSubstring, String visibleText) throws Exception
	{
		click(awNameSubstring, visibleText);
	}

	/**
	 * Return a list with all the elements on the page that match the AwBy
	 * parameter
	 * 
	 * @author aoesterholm
	 * @param by
	 * @return
	 */
	protected List<WebElement> findElements (AwBy by)
	{
		return driver.findElements(by);
	}

	/**
	 * Return a list with all the elements on the page that match the awname
	 * substring.
	 * 
	 * @author aoesterholm
	 * @param awNameSubstring
	 * @return
	 */
	@SuppressWarnings("deprecation")
	protected List<WebElement> findElementsByAwnameSubstring (String awNameSubstring)
	{
		return findElements(AwBy.xpath("//*[contains(@awname, '" + awNameSubstring
				+ "')]"));
	}


	/**
	 * To check the presence of an element with visible text.
	 * 
	 * @author gavijay
	 */
	public boolean isElementPresent (String awNameSubstring, String visibleText)
	{
		@SuppressWarnings("deprecation")
		List<WebElement> elements = this.driver.findElements(AwBy
				.xpath("//*[contains(@awname, '" + awNameSubstring + "')]"));
		for (WebElement element : elements) {
			logger.debug(_logTag + "element text : " + element.getText());
			if (element.getText().equals(visibleText)) {
				return true;
			}

		}
		return false;
	}

	/**
	 * Method to: test whether or not specified text is present in a <Select>
	 * dropdown
	 * 
	 * @author karthur, aoesterholm
	 * @param by
	 * @param visibleText
	 * @return
	 * @throws Exception 
	 */
	public boolean isMenuOptionPresent (AwBy by, String visibleText) throws Exception
	{
		boolean result = false;
		WebElement listElement = findElement(by);
		SelectExtra droplist = new SelectExtra(listElement);

		result = droplist.isVisibleTextPresent(visibleText);
		waitForPageToLoad(10);
		return result;
	}

	/**
	 * Expands the '...' element to achieve the text hidden behind it
	 * 
	 * @author rbrady
	 * @throws Exception 
	 */
	protected void expandPageHintText () throws Exception
	{

		@SuppressWarnings("deprecation")
		List<WebElement> listSpan = driver.findElements(AwBy.tagName("span"));
		logger.info(_logTag + listSpan.size() + " span elements found on page");
		for (WebElement e : listSpan) {
			if (e.getText().equals("...")) {
				click(e);
				this.waitForPageToLoad(30);
				// TODO add a recursive call if there are two link texts on the
				// same page
				return;
			}
		}
	}

	/**
	 * Override this method on any page that needs to filter out sample data,
	 * dates, times etc. that will be different in every run.
	 * 
	 * @author aoesterholm
	 * @param htmlWithStyles
	 * @return htmlWithStyles the input unchanged
	 */
	public String filterHtmlSource (String htmlWithStyles)
	{
		return htmlWithStyles;
	}

}
