package com.netshoes.regression.begin;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.netshoes.regression.pom.HomePageObjects;
import com.netshoes.regression.pom.LoginPageObjects;

public class RegressionStarter 
{
  WebDriver driver;
  HomePageObjects home;
  LoginPageObjects log;
  
  public RegressionStarter()
  {
	  System.setProperty("webdriver.chrome.driver","./resources/browserDrivers/chromeDrivers/chromedriver.exe");
	  driver = new ChromeDriver();
	  home = new HomePageObjects(driver);
	  log = new LoginPageObjects(driver);
  }
  public static void main(String arg[])
  {
	  RegressionStarter start =  new RegressionStarter();
	  start.startExecution();
  }
  
  public void startExecution()
  {
	  navigateToSite(); 
  }
  /*@BeforeTest
  public void setWebdriver() 
  {
	  System.setProperty("webdriver.chrome.driver", "./resources/browserDrivers/chromeDrivers/chromedriver.exe");
	  driver = new ChromeDriver();
	  home = new HomePageObjects(driver);
	  log = new LoginPageObjects(driver);
  }*/
  //@Test
  public void navigateToSite()
  {
	  driver.get("http://www.netshoes.com.br");
	  try {
		Thread.sleep(1000);
	} catch (InterruptedException e) {
		e.printStackTrace();
	}
	  home.mouseOverOnEntrar();
	  navigateToLogin();
  }
  //@Test
  public void navigateToLogin()
  {
	  home.lnkLogin.click();
	  log.enterCredentials();
	  try {
		Thread.sleep(1000);
	} catch (InterruptedException e) {
		e.printStackTrace();
	}
	  home.validateUser();
  }
  //@Test
  /*public void enterCredentials()
  {
	  log.txtUserName.sendKeys("oesaurabh-55@yahoo.com");
	  log.txtPassword.sendKeys("123456");
	  log.btnLogin.click();
  }*/
  /*@AfterSuite
  public void closeBrowser()
  {
	  driver.quit();
  }*/
}
