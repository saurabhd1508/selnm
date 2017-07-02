package selenium_practice;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

import com.selenium.tests.operations.ExcelFileOperations;
import com.selenium.tests.operations.TestWebDriverOperations;


public class GmailLoginUsingXLFile {

	Properties prop = new Properties();
	WebDriver driver;
	
	TestWebDriverOperations wd = new TestWebDriverOperations();
	ExcelFileOperations xl = new ExcelFileOperations();
	
	public void initializeProperties() throws FileNotFoundException 
	{
		InputStream input = null;
		input = new FileInputStream("./resources/properties/config.properties");
		try {
			prop.load(input);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		//setting(); added in TestWebDriverOperations class
		//wd.setChromeWDPath(driver);
	}
/*	public void setting()
	{
		System.out.println(prop.getProperty("webDriverPath"));
		System.setProperty("webdriver.chrome.driver", prop.getProperty("webDriverPath"));
	}
*/
	public void login() throws IOException
	{
		
		//driver.get("https://www.gmail.com");
		
		String inputFilePath = prop.getProperty("inputFilePath");
		//FileInputStream inputStream = new FileInputStream(new File(inputFilePath));
		System.out.println("Input File path is "+inputFilePath);
		FileInputStream inputStream = null;
		inputStream = xl.setFile(inputFilePath);
		
		Workbook workBook = new HSSFWorkbook(inputStream);
 		
		//Sheet firstSheet = (Sheet) workBook.getSheetAt(0);
		//Sheet firstSheet = workBook.getSheetAt(0);
		Sheet firstSheet = workBook.getSheet("Data");
		Iterator<Row> rowIterator = firstSheet.iterator();
		
		for(int i=0;rowIterator.hasNext();i++)
		{
			System.out.println("in rowItr "+rowIterator.next().cellIterator().next().toString());
		}
		
		int totalRows=0;
		String password, userName = null;
		
		while(rowIterator.hasNext())
		{
			Row nextRow = rowIterator.next();
			Iterator<Cell> cellIterator = nextRow.cellIterator();
			Cell cell = null;
			
			for(int j=0; cellIterator.hasNext(); j++)
			//for(Cell cell : row)
			{
				cell = cellIterator.next();
				CellType type = cell.getCellTypeEnum();
				if(type == CellType.STRING)
				{
					//userName = prop.getProperty("user");
					//password = prop.getProperty("pass");
					if(cell.getStringCellValue().equalsIgnoreCase("Col1"))
					{
						System.out.println("Username found...");
						//int nextRowIndex = cell.getRowIndex();
						int nextRowIndex = cell.getRowIndex()+1;
						//System.out.println(nextColumnIndex);
						userName = nextRow.getCell(nextRowIndex).toString();
						System.out.println(userName);
					}
					
					if(cell.getStringCellValue().equalsIgnoreCase("Col2"))
					{
						System.out.println("Password found...");
						int nextRowIndex = cell.getRowIndex()+1;
						//System.out.println(nextColumnIndex);
						password = nextRow.getCell(nextRowIndex).toString();
						System.out.println(password);
					}
					
					/*
					System.out.println("Username found...");
					System.out.println("Username is "+xl.getStringCellData(cell,nextRow,userName));
					System.out.println(nextRow.getRowNum());
					//xl.getStringCellData(cell, nextRow, password);
					System.out.println("Password found...");
					System.out.println("Password is "+xl.getStringCellData(cell,nextRow,password));*/
				}
				else
					System.out.println("UserName not found!!!");
			}
			
			/*while(cellIterator.hasNext())
			{
				cell = cellIterator.next();
				
				switch(cell.getCellTypeEnum())
				{
				//case Cell.CELL_TYPE_STRING:
				case STRING:
					//System.out.println("column index is "+cell.getColumnIndex());
					//if(cell.getColumnIndex()==0)
						//System.out.print(cell.getStringCellValue()+" : ");
					//else
						//System.out.print(cell.getStringCellValue());
					if(cell.getStringCellValue().equalsIgnoreCase("userName"))
					{
						System.out.println("Username found...");
						int nextColumnIndex = cell.getColumnIndex()+1;
						//System.out.println(nextColumnIndex);
						userName = nextRow.getCell(nextColumnIndex).toString();
						System.out.println(userName);
					}
					if(cell.getStringCellValue().equalsIgnoreCase("password"))
					{
						System.out.println("Password found...");
						int nextColIndex = cell.getColumnIndex()+1;
						password = nextRow.getCell(nextColIndex).toString();
						System.out.println(password);
					}
					break;
				case NUMERIC:
					System.out.println(cell.getNumericCellValue());
					break;
				default:
					System.out.println("UserName not found!!!");
					break;
				}
				//System.out.println(" - ");
			}*/
			//System.out.println(cell.getSheet().getRow(0).getCell(0).toString());
			System.out.println();
			totalRows++;
		}
		System.out.println("total rows = "+totalRows);
		workBook.close();
		inputStream.close();
		//driver.quit();
	}
	public static void main(String[] args) throws IOException 
	{
		GmailLoginUsingXLFile g =  new GmailLoginUsingXLFile();
		g.initializeProperties();
		g.login();
	}
}

