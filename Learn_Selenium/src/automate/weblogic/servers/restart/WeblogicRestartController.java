package automate.weblogic.servers.restart;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

public class WeblogicRestartController 
{
	Properties pr = new Properties();
	
	private WebDriver driver;
	
	public void setProperties() throws FileNotFoundException
	{
		InputStream inputPropFile = new FileInputStream("./resources/properties/weblogicConfigs.properties");
		try {
			pr.load(inputPropFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		setWebdriver();
	}
	
	public void setWebdriver()
	{
		System.setProperty("webdriver.chrome.driver", pr.getProperty("webDriverPath"));
		driver =  new ChromeDriver();
		loginToWebLogic();
	}
	
	public void loginToWebLogic()
	{
		driver.get(pr.getProperty("HMG02WebLogicBaseUrl"));
		
		WebElement txtUser = driver.findElement(By.id("j_username"));
		WebElement txtPass = driver.findElement(By.id("j_password"));
		txtUser.sendKeys(pr.getProperty("HMGWLUserName"));
		txtPass.sendKeys(pr.getProperty("HMGWLPassowrd"));
		WebElement btnLogin = driver.findElement(By.className("formButton"));
		btnLogin.click();
	}
	public static void main(String[] args) throws FileNotFoundException 
	{
		WeblogicRestartController wl = new WeblogicRestartController();
		wl.setProperties();
	}
}
