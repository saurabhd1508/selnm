package com.netshoes.regression.pom;

import java.util.List;

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
	//@FindBy(xpath="//*[contains(@href,'#header-user-')]")
	//css="input[id='username']"
	//id="username-logged"
	@FindBy(css="a[qa-automation='home-account-button']")
	public WebElement mnuEntrar;
	
	public void mouseOverOnEntrar()
	{
		CustomActions act =  new CustomActions(driver);
		act.mouseHover(mnuEntrar);
	}
	
	@FindBy(linkText="Login")
	public WebElement lnkLogin;
	
	@FindBy(css="span[id='username-logged']")
	public List<WebElement> lblLoggedInUser;
	
	public void validateUser()
	{
		if(lblLoggedInUser.size()>=1)
			System.out.println("Logged in user is - "+lblLoggedInUser.iterator().next().getText());
		else
			System.out.println("Something is wrong in login, please try again");
	}
}
