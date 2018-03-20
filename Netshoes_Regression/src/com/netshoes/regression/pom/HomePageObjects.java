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
	
	@FindBy(linkText="Login")
	public WebElement lnkLogin;
	
	@FindBy(css="span[id='username-logged']")
	public List<WebElement> lblLoggedInUser;
	
	@FindBy(id="search-input")
	public WebElement txtSearch; 
	
	@FindBy(css="button[qa-automation='home-search-button']")
	public WebElement btnSearch;
		
	public void mouseOverOnEntrar()
	{
		CustomActions act =  new CustomActions(driver);
		act.mouseHover(mnuEntrar);
	}
	
	public LoginPageObjects goToLoginPage()
	{
		lnkLogin.click();
		return new LoginPageObjects();
	}
	
	public void validateUser()
	{
		if(lblLoggedInUser.size()>=1)
			System.out.println("Logged in user is - "+lblLoggedInUser.iterator().next().getText());
		else
			System.out.println("Something is wrong in login, please try again");
	}
	
	public SearchPageObjects searchProducts(String searchString)
	{
		txtSearch.sendKeys(searchString);
		btnSearch.click();
		return new SearchPageObjects();
	}
}
