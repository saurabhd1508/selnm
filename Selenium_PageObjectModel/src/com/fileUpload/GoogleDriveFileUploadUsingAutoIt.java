package com.fileUpload;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import autoitx4java.AutoItX;

public class GoogleDriveFileUploadUsingAutoIt 
{
	WebDriver driver = null;
	public static void main(String[] args) throws InterruptedException, IOException
	{
		GoogleDriveFileUploadUsingAutoIt upload = new GoogleDriveFileUploadUsingAutoIt();
		upload.initilizeDriver();
		upload.loginToGoogleDrive();
		//upload.navigateToGoogleDrive();
		upload.startFileUpload();
		
	}
	
	private void initilizeDriver()
	{
		System.setProperty("webdriver.chrome.driver", "./resources/browseDrivers/chromeDrivers/chromedriver.exe");
		driver = new ChromeDriver();
	}
	
	private void loginToGoogleDrive() throws InterruptedException 
	{
		//driver.get("http://gmail.com/");
		driver.navigate().to("https://drive.google.com/drive/my-drive");
		Thread.sleep(500);
		driver.manage().window().maximize();
		WebElement txtLogin = driver.findElement(By.id("identifierId"));
		txtLogin.sendKeys("oetestgigya");
		WebElement btnNext = driver.findElement(By.id("identifierNext"));
		btnNext.click();
		Thread.sleep(2000);
		WebElement txtPass = driver.findElement(By.name("password"));
		txtPass.sendKeys("saurabh@123");
		WebElement btnPassNext = null;
		try{
			Thread.sleep(1000);
			btnPassNext = driver.findElement(By.id("passwordNext"));
			Thread.sleep(1000);
			btnPassNext.click();
		}
		catch(WebDriverException e)
		{
			if(e.getMessage().contains("Element is not clickable at point"))
			{
				driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
				JavascriptExecutor jse = (JavascriptExecutor)driver;
				jse.executeScript("arguments[0].scrollIntoView()", btnPassNext); 
				Thread.sleep(1000);
				btnPassNext.click();
			}
		}
		
		Thread.sleep(10000);		
	}
	private void startFileUpload() throws InterruptedException, IOException 
	{
		WebElement btnNew = driver.findElement(By.cssSelector("button[aria-label='New']"));
		btnNew.click();
		Thread.sleep(1500);
		WebElement fileUpload = null;
		try
		{
		fileUpload = driver.findElement(By.cssSelector("path[d='M12,0H4C2.896,0,2.01,0.896,2.01,2L2,18c0,1.104,0.886,2,1.99,2H16c1.104,0,2-0.896,2-2V6L12,0z M11,13v4H9v-4H6l4-4l4,4H11  z M11,7V1.5L16.5,7H11z']"));
		fileUpload.click();
		}
		catch(WebDriverException e)
		{
			/*String errMsg = e.getMessage();
			System.out.println(errMsg);
			
			String[] matches = new String[]{"(",")"};
			int id1=0, id2=0, flg=0, x=0, y=0;
			for(String s : matches)
			{
				if(errMsg.contains(s))
				{
					if(flg==0)
					{
						id1=errMsg.indexOf(s);
						flg=1;
					}
					else
					{
						id2=errMsg.indexOf(s);
						break;
					}
				}
			}
			String mainString = errMsg.substring((id1+1), id2);
			String splitAry[] = mainString.split("\\W+");
			System.out.println("Spllited strings are - "+splitAry[0]+" "+splitAry[1]);
			x = Integer.parseInt(splitAry[0]);
			y = Integer.parseInt(splitAry[1]);
			System.out.println("Final x and y are - "+x+" "+y);*/
			
			if(fileUpload.isDisplayed())
			{
				System.out.println("yes its visible.. clicking on it");
				Actions builder = new Actions(driver);
				builder.moveToElement(fileUpload, (0), (20)).click().build().perform();
			}
		}
		Thread.sleep(2000);
		Runtime.getRuntime().exec("./lib/FileUploadNew.exe");
		Thread.sleep(10000);
		System.out.println("Is file uploaded? - "+ isFileUploadSucceeded());
	}

	
	private void navigateToGoogleDrive() throws InterruptedException
	{
		driver.navigate().to("https://drive.google.com/drive/my-drive");
		Thread.sleep(5000);
	}
	
	private boolean isFileUploadSucceeded()
	{
		boolean isFileUploadedDialogBox =false;
		isFileUploadedDialogBox	= driver.findElements(By.cssSelector("div[aria-label='1 upload complete']")).size()>=1;
		if(isFileUploadedDialogBox)
			return true;
		else
			return false;
	}
}