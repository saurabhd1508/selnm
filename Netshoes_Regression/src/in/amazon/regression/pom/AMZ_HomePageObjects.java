package in.amazon.regression.pom;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.testng.annotations.Test;

public class AMZ_HomePageObjects 
{
	public AMZ_HomePageObjects(WebDriver driver)
	{
		PageFactory.initElements(driver, this);
	}
	
	@FindBy(id="nav-link-accountList")
	public WebElement lnkAccountList;
}
