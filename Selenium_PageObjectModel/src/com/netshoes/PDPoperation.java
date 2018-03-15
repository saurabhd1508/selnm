package com.netshoes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class PDPoperation 
{
	public void setWD()
	{
		System.setProperty("webdriver.chrome.driver", "./resources/browseDrivers/chromeDrivers/chromedriver.exe");
		driver = new ChromeDriver();
	}
	WebDriver driver;
	public void start()throws InterruptedException
	{
		driver.get("https://www.netshoes.com.br/chuteira-futsal-nike-beco-2-futsal-masculina-azul+branco-004-5830-058");
		
		/*List<WebElement> rdoSizes = new ArrayList<WebElement>();
		rdoSizes = driver.findElements(By.cssSelector("a[data-size='size-P']"));
		Iterator<WebElement> itrSize = rdoSizes.iterator();
		WebDriverWait wait = new WebDriverWait(driver,120);
		while(itrSize.hasNext())
		{
			
			WebElement size = null;
			size = wait.until(ExpectedConditions.presenceOfElementLocated((By.cssSelector("a[data-size='size-P']"))));
			//size = itrSize.next();
			
			size.click();
			Thread.sleep(5000);
			System.out.println("Is this size out of stock - "+isThisSizeOOS());
			
			wait.until(ExpectedConditions.stalenessOf(size));
			//size = findElement(size.g);
		}*/
	}
	public static void main(String[] args) throws InterruptedException
	{
		PDPoperation pdp = new PDPoperation();
		pdp.setWD();
		pdp.start();
	}
	public boolean isThisSizeOOS()
	{
		boolean isAviseMe = driver.findElements(By.cssSelector("button[data-ga-element='button_Avise-Me']")).size()>=1;
		if(isAviseMe)
			return true;
		else
			return false;
	}
}
