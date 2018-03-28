package com.netshoes.regression.pom;

import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class SearchPageObjects 
{
	WebDriver driver;
	
	public SearchPageObjects(WebDriver driver)
	{
		this.driver = driver;
		PageFactory.initElements(driver, this);
	}
	
	public SearchPageObjects(){}
	
	//@FindBy(xpath="//*[contains(@href,'#header-user-')]")
	//@FindBy(xpath="//*[contains(@data-src,'static.netshoes.com.br/produtos')]")
	
	
	//@FindBy(xpath="//*[@id=\"item-list\"]/div[1]/div[1]/a[1]")
	//@FindBy(css="div[itemprop='itemListElement']")
	//@FindBy(css="a[title^='Camisa ']")
	//@FindBy(css="img[src$='static.netshoes.com.br/produtos/'])")
	@FindBy(className="card-link")
	public List<WebElement> imgProduct;
	
	public void selectProduct() 
	{
		System.out.println("Selecting product");
		imgProduct.iterator().next().click();
	}
}
