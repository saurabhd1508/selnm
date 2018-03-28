package com.netshoes.regression.pom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class PDPObjects 
{
	WebDriver driver;
	public PDPObjects(WebDriver driver)
	{
		this.driver = driver;
		PageFactory.initElements(driver, this);
	}
	
	@FindBy(css="ul[data-type='color']")
	public WebElement colorList;
	
	@FindBy(css="ul[data-type='size']")
	public WebElement sizeList;
	
	@FindBy(css="a[qa-automation='product-color']") 
	public List<WebElement> color;
	
	@FindBy(className="tell-me-button-wrapper")
	public WebElement divOutOfStock;
	
	public boolean isSizesAvailable()
	{
		if(sizeList.isDisplayed())
			return true;
		else
			return false;
	}
	
	public boolean isColorsAvailable()
	{
		if(colorList.isDisplayed())
			return true;
		else
			return false;
	}
	
	public void getInStockColor()
	{
		int cnt = 0;
		//Collections.sort(color);
		Iterator<WebElement> itr =color.iterator();
		WebElement col = null;
		while(itr.hasNext())
		{
			col = itr.next();
			col.click();
			if(isOutOfStoc())
			{
				System.out.println("Product is Out of stock");
			}
			else
			{
				System.out.println("Product is In of stock");
			}
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			/*if(isInStock(col))
			{
				System.out.println("Yes, color is avaliable");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if(cnt==0)
				{
					cnt=1;
					continue;
				}
				col.click();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				break;
			}
			else
			{
				System.out.println("Out of Stock");
				col.click();
			}*/
				
		}
	}
	
	public boolean isOutOfStoc()
	{
		if(divOutOfStock.isDisplayed())
			return true;
		else 
			return false;
	}
	public boolean isInStock(WebElement col)
	{
		System.out.println("Attribute value is = "+col.getAttribute("qa-option"));
		if(col.getAttribute("qa-option").equals("available"))
			return true;
		else
			return false;
	}
	
}
