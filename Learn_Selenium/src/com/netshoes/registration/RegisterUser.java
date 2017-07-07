package com.netshoes.registration;

import java.io.FileInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import org.openqa.selenium.WebDriver;



public class RegisterUser 
{
	Workbook workbook = null;
	WebDriver driver = null;
	FileInputStream inputFile = null;
	public void settings() throws FileNotFoundException
	{
		System.setProperty("webdriver.chrome.driver", "./resources/browserDrivers/chromeDrivers/chromedriver.exe");
		//driver = new ChromeDriver();
		getFile();
	}
	
	public void getFile() throws FileNotFoundException
	{
		inputFile = new FileInputStream(new File("./resources/TestData/Steps_RegisterUser.xlsx"));
	}
	
	public void processFile() throws IOException
	{
		workbook = new HSSFWorkbook(inputFile);
		Sheet stepsSheet = workbook.getSheetAt(0);
		//System.out.println(stepsSheet.getSheetName());
		
		Iterator<Row> rowItr = stepsSheet.rowIterator();
		Row nextRow = rowItr.next();
		Iterator<Cell> celltr = nextRow.cellIterator();
		
		while(rowItr.hasNext())
		{
			while(celltr.hasNext())
			{
				System.out.println(celltr.next().toString());
			}
		}
	}
	
	public static void main(String[] args) throws IOException 
	{
		RegisterUser register = new RegisterUser();
		register.settings();
		register.processFile();
	}
}
