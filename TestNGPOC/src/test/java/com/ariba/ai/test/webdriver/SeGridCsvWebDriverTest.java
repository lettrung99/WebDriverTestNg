package com.ariba.ai.test.webdriver;

import static org.testng.Assert.*;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.By;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
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

	private StringBuffer verificationErrors = new StringBuffer();

	@Parameters({ "platform", "browser", "version", "url", "hub_url", "csvFileName" })
	@BeforeTest(alwaysRun = true)
	public void setup(String platform, String browser, String version, String url, String hub_url, String csvFileName)
			throws MalformedURLException {
		
		DesiredCapabilities caps = new DesiredCapabilities();
		
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
			caps = DesiredCapabilities.internetExplorer();
		
		if (browser.equalsIgnoreCase("Firefox"))
			caps = DesiredCapabilities.firefox();
		
		if (browser.equalsIgnoreCase("iPad"))
			caps = DesiredCapabilities.ipad();
		
		if (browser.equalsIgnoreCase("Android"))
			caps = DesiredCapabilities.android();
		
		// Version
		if(!"".equals(version))
			caps.setVersion(version);
		
		try{
			System.out.println("MY HUB: " + hub_url);
			driver = new RemoteWebDriver(new URL(hub_url), caps);
		}catch(Exception e){
			System.out.println("MY HUB: " + e.getMessage());
			if (browser.equalsIgnoreCase("Firefox"))
				driver = new FirefoxDriver();
		  if (browser.equalsIgnoreCase("Safari"))
			  driver = new SafariDriver();
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
		driver.quit();
		
		String verificationErrorString = verificationErrors.toString();
		
		if (!"".equals(verificationErrorString)) {
			//testing take this out windows test should pass now
			fail(verificationErrorString);
		}
		
	}
}
