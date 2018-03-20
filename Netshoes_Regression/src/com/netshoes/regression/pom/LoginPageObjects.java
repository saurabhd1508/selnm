package com.netshoes.regression.pom;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import org.openqa.selenium.support.PageFactory;

public class LoginPageObjects 
{
	WebDriver driver;
	public LoginPageObjects(WebDriver driver)
	{
		this.driver = driver;
		PageFactory.initElements(driver, this);
	}
	
	//@FindBy(id="username")

	@FindBy(how = How.XPATH, using = "//*[@id=\"username\"]")
	//@FindBy(css="input[id='username']")
	public WebElement txtUserName; 
	//= driver.findElement(By.id("username"));
	
	@FindBy(id="password")
	public WebElement txtPassword;
	
	@FindBy(id="login-button")
	public WebElement btnLogin;
	
	public void enterCredentials()
	{
		txtUserName.sendKeys("oesaurabh-55@yahoo.com");
		txtPassword.sendKeys("123456");
		btnLogin.click();
	}
}
