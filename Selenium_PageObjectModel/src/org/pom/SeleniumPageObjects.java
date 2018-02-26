package org.pom;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class SeleniumPageObjects 
{
	public SeleniumPageObjects(WebDriver driver)
	{
		PageFactory.initElements(driver, this);
	}
	
	public SeleniumPageObjects() 
	{
	}

	@FindBy(linkText="Download")
	WebElement lnkDownload; 
	
	@FindBy(linkText="Browser Automation")
	WebElement lnkHome;
	
	public void clickDownload()
	{
		lnkDownload.click();
	}
	
	public void navigateHome()
	{
		lnkHome.click();
	}
}
