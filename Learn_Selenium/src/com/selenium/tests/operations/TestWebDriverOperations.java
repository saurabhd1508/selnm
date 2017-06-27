package com.selenium.tests.operations;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class TestWebDriverOperations 
{
	public void setChromeWDPath(WebDriver driver)
	{
		System.setProperty("webdriver.chrome.driver", "./resources/browserDrivers/chromeDrivers/chromedriver.exe");
		driver = new ChromeDriver();
	}
}
