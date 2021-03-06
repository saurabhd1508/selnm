package com.netshoes.regression.begin;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.netshoes.regression.pom.HomePageObjects;
import com.netshoes.regression.pom.LoginPageObjects;
import com.netshoes.regression.pom.PDPObjects;
import com.netshoes.regression.pom.SearchPageObjects;

public class RegressionStarter 
{
  WebDriver driver;
  HomePageObjects home;
  LoginPageObjects log;
  SearchPageObjects search;
  PDPObjects pdp;
  public RegressionStarter()
  {
	  System.setProperty("webdriver.chrome.driver","./resources/browserDrivers/chromeDrivers/chromedriver.exe");
	  driver = new ChromeDriver();
	  home = new HomePageObjects(driver);
	  
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
	  log = home.goToLoginPage();
	  log = new LoginPageObjects(driver);
	  log.enterCredentials();
	  try {
		Thread.sleep(20000);
	} catch (InterruptedException e) {
		e.printStackTrace();
	}
	  home.validateUser();
	  home.searchProducts("chuteira feminino");
	  
	  search = new SearchPageObjects(driver);
	  String url = driver.getCurrentUrl();
	  System.out.println(url);
	  if(isThisSearchPage(url))
	  {
		  System.out.println("Yes we are on search page");
		  search.selectProduct();
		  pdp = new PDPObjects(driver);
		  if(pdp.isColorsAvailable())
		  {
			  pdp.getInStockColor();
		  }
			  
		  //WebElement product = driver.findElement(By.cssSelector("a[title^='Camisa ']"));
		  //product.click();
	  }
	  else
		  System.out.println("Something is worng... we are not on search page");
  }
  
  public boolean isThisSearchPage(String url) 
  {
	  if(url.contains("www.netshoes.com.br/busca?"))
		  return true;
	  else
		  return false;
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
