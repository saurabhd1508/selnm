package selenium_practice;

import java.util.List;

import org.openqa.selenium.*;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.chrome.ChromeDriver;

import com.gargoylesoftware.htmlunit.javascript.host.media.rtc.webkitRTCPeerConnection;

public class NS_Login 
{
	WebDriver driver;
	public void setting()
	{
		System.setProperty("webdriver.chrome.driver", "D://100rabh//Selenium//New Chrome//chromedriver.exe");
		driver = new ChromeDriver();
	}
	public void execute()
	{
		
		driver.get("https://www.netshoes.com.br/account/login.jsp");
		List<WebElement> inputs = (List<WebElement>)driver.findElements(By.tagName("input"));
		
		WebElement element;
		for(int i=0;i<=inputs.size()-1;i++)
		{	
			if(inputs.get(i).getAttribute("type").toString().equals("text") || inputs.get(i).getAttribute("type").toString().equals("password"))
			{
				highLightElement(driver,inputs.get(i));
				System.out.println("Input "+ i + " is = "+inputs.get(i).getAttribute("type").toString());
			}
			else
			{
				continue;
			}
		}
	}
	
	public void highLightElement(WebDriver driver, WebElement webElement)
	{
		String DELAY = "delay";
		JavascriptExecutor js = (JavascriptExecutor) driver;
		//js.executeScript("arguments[0].setAttribute('style,'border: solid 2px red'');", webElement);
		js.executeScript("arguments[0].setAttribute('style', arguments[1]);",webElement, "color: green; border: 4px solid green;");
		//js.executeScript("arguments[0].setAttribute('style', arguments[1]), color: yellow; border: 4px solid yellow;",webElement);
		try {
			int time = 500;
			if(System.getProperty(DELAY) != null){
			 time = Integer.parseInt(System.getProperty(DELAY));
			}
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		js.executeScript("arguments[0].setAttribute('style', arguments[1]);", webElement, ""); 
	}
	
	public void login()
	{
		WebElement cpf;
		cpf = driver.findElement(By.id("email-cpf"));
		cpf.sendKeys(TestDataInt.uName);
		
		WebElement pass;
		pass = driver.findElement(By.id("password"));
		pass.sendKeys(TestDataInt.password);
		
		WebElement submit = driver.findElement(By.id("login-button"));
		submit.click();
	}
	
	public void register()
	{
		WebElement reg = driver.findElement(By.id("email"));
		reg.sendKeys("xyz.1@gmail.com");
		WebElement continueBtn = driver.findElement(By.id("continue-button"));
		continueBtn.click();
		
		if(driver.getTitle().equals("Para fazer parte da Netshoes, cadastre algumas informaçóes sobre você."))
		{
			WebElement selDate = driver.findElement(By.id("dayBrithday"));
			selDate.click();
		    selDate.sendKeys("7");
			selDate.click();
			
			WebElement selMonth = driver.findElement(By.id("monthBrithday"));
			selMonth.click();
			selMonth.sendKeys("Jun");
			selMonth.click();
			
			WebElement selYear = driver.findElement(By.id("yearBrithday"));
			selYear.click();
			selYear.sendKeys("1990");
			selYear.click();
			WebElement cpf = driver.findElement(By.id("cpf"));
			cpf.sendKeys("123456789");
		}
		else
		{
			System.out.println("wrong page");
		}
	}
	
	public static void main(String[] args) 
	{
		NS_Login n = new NS_Login();
		n.setting();
		n.execute();
		//n.login();
		n.register();
	}
}
