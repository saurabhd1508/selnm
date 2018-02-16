package org.pom;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class GoogleSearchTest 
{
	public static void main(String[] args)
	{
		System.setProperty("webdriver.chrome.driver", "./resources/browseDrivers/chromeDrivers/chromedriver.exe");
		WebDriver driver = new ChromeDriver();
		driver.navigate().to("https://www.google.co.in/");
		GoogleHomePageObjects page =  new GoogleHomePageObjects(driver);
		page.txtSearch.sendKeys("100rabh");
		page.btnSearch.click();
	}
}