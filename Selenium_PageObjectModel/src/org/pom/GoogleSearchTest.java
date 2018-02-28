package org.pom;

import java.io.IOException;

import jxl.read.biff.BiffException;

import org.DataDrivenTest.ExcelLib;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class GoogleSearchTest 
{
	public static void main(String[] args) throws InterruptedException, IOException, BiffException
	{
		ExcelLib excel = new ExcelLib("./resources/dataFiles/TestData.xls");
		excel.columnDisctionary();
		
		System.setProperty("webdriver.chrome.driver", "./resources/browseDrivers/chromeDrivers/chromedriver.exe");
		WebDriver driver = new ChromeDriver();
		
		driver.navigate().to("https://www.google.co.in/");
		//Object for the page
		GoogleHomePageObjects page =  new GoogleHomePageObjects(driver);
		
		//search for the keyword 'Selenium'
		//page.searchGoogle(excel.getCellValue(0, 1));
		System.out.println("Terms is - "+excel.readCell(excel.getCell("SearchTerms"), 1));
		page.searchGoogle(excel.readCell(excel.getCell("SearchTerms"), 1));
		
		//clicking the selenium website link, will return a selenium web site
		SeleniumPageObjects seleniumPage =	page.clickSelenium();
		//wait for page load
		Thread.sleep(1000);
		//click download tab
		seleniumPage = new SeleniumPageObjects(driver);
		Thread.sleep(1000);
		seleniumPage.clickDownload();
		Thread.sleep(1000);
		
		//navigate to selenium home page
		seleniumPage.navigateHome();
	}
}