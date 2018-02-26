package org.pom;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class GoogleHomePageObjects 
{
	//Initialize my objects in the page 
	public GoogleHomePageObjects(WebDriver driver)
	{
		PageFactory.initElements(driver,this);;
	}
	@FindBy (name="q")
	public WebElement txtSearch;
	
	@FindBy (name="btnK")
	public WebElement btnSearch;
	
	@FindBy(linkText="Selenium - Web Browser Automation")
	public WebElement lnkSelenium;
	
	// Search for the text given
	public void searchGoogle(String searchString)
	{
		txtSearch.sendKeys(searchString);
		btnSearch.click();
	}
	
	public SeleniumPageObjects clickSelenium()
	{ 
		lnkSelenium.click();
		return new SeleniumPageObjects();
	}
}