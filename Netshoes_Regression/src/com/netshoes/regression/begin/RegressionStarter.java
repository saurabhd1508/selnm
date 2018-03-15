package com.netshoes.regression.begin;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.netshoes.regression.pom.HomePageObjects;

public class RegressionStarter 
{
  WebDriver driver;
  HomePageObjects home;
  @BeforeTest
  public void setWebdriver() 
  {
	  System.setProperty("webdriver.chrome.driver", "./resources/browserDrivers/chromeDrivers/chromedriver.exe");
	  driver = new ChromeDriver();
	  home = new HomePageObjects(driver);
  }
  @Test
  public void naviagetToSite()
  {
	  driver.get("http://www.netshoes.com.br");
	  try {
		Thread.sleep(1000);
	} catch (InterruptedException e) {
		e.printStackTrace();
	}
	  home.mouseOverOnEntrar();
  }
  public void navigateToLogin()
  {
	  home.lnkLogin.click();
  }
  
  /*@AfterSuite
  public void closeBrowser()
  {
	  driver.quit();
  }*/
}
