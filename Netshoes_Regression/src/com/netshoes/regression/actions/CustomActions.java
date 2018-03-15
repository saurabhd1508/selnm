package com.netshoes.regression.actions;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

public class CustomActions 
{
	WebDriver driver;
	public CustomActions(WebDriver driver)
	{
		this.driver = driver;
	}
	public void mouseHover(WebElement element)
	{
		Actions act = new Actions(driver);
		act.moveToElement(element).perform();
	}
}
