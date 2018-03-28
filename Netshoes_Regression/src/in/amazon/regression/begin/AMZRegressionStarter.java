package in.amazon.regression.begin;

import in.amazon.regression.pom.AMZ_HomePageObjects;
import in.amazon.regression.pom.AMZ_LoginPageObjects;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeTest;

import com.netshoes.regression.pom.HomePageObjects;
import com.netshoes.regression.pom.LoginPageObjects;

public class AMZRegressionStarter 
{
	WebDriver driver;
	AMZ_HomePageObjects home;
	AMZ_LoginPageObjects log;
	
	
	public AMZRegressionStarter() 
	{
		super();
		System.setProperty("webdriver.chrome.driver","./resources/browserDrivers/chromeDrivers/chromedriver.exe");
		driver = new ChromeDriver();
		home = new AMZ_HomePageObjects(driver);
		log = new AMZ_LoginPageObjects(driver);
	}

	public static void main(String arg[]) 
	{
		AMZRegressionStarter start = new AMZRegressionStarter();
		start.startExecution();
	}
	
	private void startExecution() 
	{
		setWebdriver();
	}

//	@BeforeTest
	public void setWebdriver() 
	{
		navigateToSite();
	}

	//@Test
	public void navigateToSite() 
	{
		driver.get("http://www.amazon.com");
		navigateToLogin();
	}

	//@Test
	public void navigateToLogin() 
	{
		home.lnkAccountList.click();
		doLogin();
	}
	
	public void doLogin()
	{
		log.txtEmail.sendKeys("saurabhd1508@gmail.com");
	}
}