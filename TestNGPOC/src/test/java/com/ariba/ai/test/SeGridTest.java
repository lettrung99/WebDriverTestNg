package com.ariba.ai.test;

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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class SeGridTest {
	
	WebDriver driver = null;
	private StringBuffer verificationErrors = new StringBuffer();

	@Parameters({ "platform", "browser", "version", "url", "hub_url" })
	@BeforeTest(alwaysRun = true)
	public void setup(String platform, String browser, String version, String url, String hub_url)
			throws MalformedURLException {
		
		DesiredCapabilities caps = new DesiredCapabilities();
		
		// Platforms
		if (platform.equalsIgnoreCase("Windows"))
			caps.setPlatform(org.openqa.selenium.Platform.WINDOWS);
		
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
		caps.setVersion(version);
		try{
			driver = new RemoteWebDriver(new URL(hub_url), caps);
		}catch(Exception e){
			if (browser.equalsIgnoreCase("Firefox"))
				driver = new FirefoxDriver();
		  if (browser.equalsIgnoreCase("Safari"))
			  driver = new SafariDriver();
		}
		
		// Open the BMI Calculator Application
		driver.get(url);
		/*try {
			driver.wait(1000);
		}
		catch (InterruptedException e) {
			verificationErrors.append(e.toString());
		}*/
		
	}

	@Test(description = "Test Bmi Calculator")
	public void testBmiCalculator() throws InterruptedException {
		//driver.manage().timeouts().implicitlyWait(300, TimeUnit.SECONDS);

		WebElement height = driver.findElement(By.name("heightCMS"));
		height.sendKeys("181");
		
		WebElement weight = driver.findElement(By.name("weightKg"));
		weight.sendKeys("80");
		
		//driver.manage().timeouts().implicitlyWait(31, TimeUnit.SECONDS);
		
		WebElement calculateButton = driver.findElement(By.id("Calculate"));
		calculateButton.click();
		
		try {
			
			WebElement bmi = driver.findElement(By.name("bmi"));
			assertEquals(bmi.getAttribute("value"), "24.4");
			
			WebElement bmi_category = driver.findElement(By.name("bmi_category"));
			assertEquals(bmi_category.getAttribute("value"), "Normal");
			
		}
		
		catch (Error e) {
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
			fail(verificationErrorString);
		}
		
	}
}
