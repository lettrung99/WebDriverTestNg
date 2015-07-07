package com.ariba.ai.test.webdriver;

import static org.testng.Assert.*;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.By;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;
import org.openqa.selenium.safari.SafariDriver;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeTest;

import ariba.ai.testing.selenium.webdriver.AribaWebDriverTestCase;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class SeGridCsvWebDriverTest {
	
	WebDriver driver = null;
	AribaWebDriverTestCase wb = null;
	SessionId sessionId = null;
  protected ThreadLocal<RemoteWebDriver> threadDriver = null;
  protected ThreadLocal<TestCaseProperties> caseProp = null;

  
	private StringBuffer verificationErrors = new StringBuffer();

	@Parameters({ "platform", "browser", "version", "url", "hub_url", "csvFileName", "label" })
	@BeforeTest(alwaysRun = true)
	public void setup(String platform, String browser, String version, String url, String hub_url, String csvFileName, String label)
			throws MalformedURLException {
		
		threadDriver = new ThreadLocal<RemoteWebDriver>();
		caseProp = new ThreadLocal<TestCaseProperties>();
		DesiredCapabilities caps = new DesiredCapabilities();

		TestCaseProperties prop = new TestCaseProperties();
		prop.setBrowser(browser);
		prop.setPlatform(platform);
		prop.setVersion(version);
		
		caseProp.set(prop);
		
		caps.setCapability("jenkins.label", label);
		// Platforms
		if (platform.equalsIgnoreCase("Windows"))
			caps.setPlatform(org.openqa.selenium.Platform.WINDOWS);
		
		if (platform.equalsIgnoreCase("Vista"))
			caps.setPlatform(org.openqa.selenium.Platform.VISTA);
		
		if (platform.equalsIgnoreCase("MAC"))
			caps.setPlatform(org.openqa.selenium.Platform.MAC);
		
		if (platform.equalsIgnoreCase("Andorid"))
			caps.setPlatform(org.openqa.selenium.Platform.ANDROID);
		
		// Browsers
		if (browser.equalsIgnoreCase("Internet Explorer"))
			//caps = DesiredCapabilities.internetExplorer();
			caps.setBrowserName(BrowserType.IE);
		
		if(browser.equalsIgnoreCase("Chrome"))
			caps.setBrowserName(BrowserType.CHROME);

		if (browser.equalsIgnoreCase("Firefox"))
			//caps = DesiredCapabilities.firefox();
		  caps.setBrowserName(BrowserType.FIREFOX);
		
		if (browser.equalsIgnoreCase("iPad"))
			//caps = DesiredCapabilities.ipad();
			caps.setBrowserName(BrowserType.IPAD);
		
		if (browser.equalsIgnoreCase("Android"))
			//caps = DesiredCapabilities.android();
			caps.setBrowserName(BrowserType.ANDROID);
		
		// Version
		if(!"".equals(version))
			caps.setVersion(version);
		
		try{
			driver = new RemoteWebDriver(new URL(hub_url), caps);
			System.out.println("MY HUB: " + hub_url + " threading on - " + Thread.currentThread().getId());
			System.out.println("MY Driver Hashcode: " + driver.hashCode());

			threadDriver.set((RemoteWebDriver) driver);
			//to get the grid video url
			RemoteWebDriver dr = (RemoteWebDriver)driver;
			sessionId = dr.getSessionId();

		}catch(Exception e){
			System.out.println("MY HUB: " + e.getMessage());
		}
		
		System.out.println("My filePath: " + csvFileName);
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("com/ariba/ai/test/rc/"+csvFileName).getFile());
		wb = new AribaWebDriverTestCase(driver, url, file.getAbsolutePath());
	}

	@Test(description = "Test CSV file")
	public void testCsvFileName() throws InterruptedException {
		
		try {
			 wb.runSeleniumCsv();
		}
		catch (Exception e) {
			// Capture and append Exceptions/Errors
			verificationErrors.append(e.toString());
		}
		
	}

	@AfterTest
	public void afterTest() {
		// Close the browser
		//driver.quit();
		driver = threadDriver.get();
	
		URL remoteServer = ((HttpCommandExecutor)((RemoteWebDriver) driver).getCommandExecutor()).getAddressOfRemoteServer();
		System.out.println("MY Driver Hashcode: " + driver.hashCode());
		System.out.println("Platform: [" + caseProp.get().getPlatform() + "; Browser: ["+ caseProp.get().getBrowser() + "]; Version: [" + caseProp.get().getVersion()+"]");

		//System.out.println("MY video download url: " + "http://"+remoteServer.getHost()+":"+remoteServer.getPort()+"/grid/admin/HubVideoDownloadServlet/?sessionId=" + sessionId);
		System.out.println("MY video download url: " + "http://"+remoteServer.getHost()+":"+"3000"+"/download_video/" + sessionId+".mp4");

		driver.quit();
		
		String verificationErrorString = verificationErrors.toString();
		
		if (!"".equals(verificationErrorString)) {
			//testing take this out windows test should pass now
			fail(verificationErrorString);
		}
		
	}
}
