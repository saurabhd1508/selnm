package com.netshoes.regression.pom;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import com.netshoes.regression.actions.CustomActions;

public class HomePageObjects 
{
	WebDriver driver;
	public HomePageObjects(WebDriver driver)
	{
		this.driver =  driver;
		PageFactory.initElements(driver, this);
	}
	
	//@FindBy(xpath="//*[starts-with(@href,'#header-user')]")
	@FindBy(xpath="//*[contains(@href,'#header-user-')]")
	public WebElement mnuEntrar;
	
	public void mouseOverOnEntrar()
	{
		CustomActions act =  new CustomActions(driver);
		act.mouseHover(mnuEntrar);
	}
	
	@FindBy(linkText="Login")
	public WebElement lnkLogin;
	
	
}
