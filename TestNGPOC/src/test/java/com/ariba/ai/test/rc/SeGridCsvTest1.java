package com.ariba.ai.test.rc;

import java.net.MalformedURLException;
import java.net.URL;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeTest;

import static org.testng.Assert.*;
import ariba.ai.testing.selenium.rc.AribaSeleniumRCTestCase;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.webdriven.WebDriverBackedSelenium;


public class SeGridCsvTest1 {

	Selenium selenium = null;
	String csvFile = "";
	String baseUrl = "";
	
	@Parameters({ "browser","url", "hubhost", "hubport", "csvFile"})
	@BeforeTest(alwaysRun = true)
	public void setup(String browser,  String url, String hubhost, String hubport, String csvFile)
			throws MalformedURLException {

		try{
			
			this.csvFile = csvFile;
			this.baseUrl = url;
			
			
			WebDriver driver = new FirefoxDriver();


		// Create the Selenium implementation
		 selenium = new WebDriverBackedSelenium(driver, url);

			//selenium = new DefaultSelenium(hubhost, Integer.valueOf(hubport), "*"+browser, url);
		}catch(Exception e){
			System.out.println("Some Error: " + e.getMessage());
			//selenium = new DefaultSelenium("localhost", 8080, "*"+browser, url);
		}
    System.out.println("starting....");

		//selenium.start();
		
	}
	
	@Test(description = "Test Selenium RC CSV")
	public void testSeleniumCSV() throws InterruptedException {
		
		AribaSeleniumRCTestCase rc = null;
			
		try{
				rc = new AribaSeleniumRCTestCase( selenium, csvFile, baseUrl );
				rc.runSeleniumTest();
		}catch(Throwable e){
			fail(e.toString());
		}
		
	}
	
	@AfterTest
	public void afterTest() {
		
		// Close the browser
		selenium.stop();
		
	}
}
