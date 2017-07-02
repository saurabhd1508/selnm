package backup;

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
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class GmailLoginUsingXLFile {

	Properties prop = new Properties();
	WebDriver driver;
	
	public void initializeProperties() throws FileNotFoundException 
	{
		InputStream input = null;
		input = new FileInputStream("./resources/properties/config.properties");
		try {
			prop.load(input);
		//	System.out.println(prop.getProperty("webDriverPath"));
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		setting();
	}
	public void setting()
	{
		System.out.println(prop.getProperty("webDriverPath"));
		System.setProperty("webdriver.chrome.driver", prop.getProperty("webDriverPath"));
	}

	public void login() throws IOException
	{
		driver = new ChromeDriver();
		driver.get("https://www.gmail.com");
		
		//D:\\100rabh\\Others\\Development\\GitWorkSpaces\\selnm\\Learn_Selenium\\resources\\TestData\\TestInputData.xlsx
		//\\resources\\TestData\\TestInputData.xlsx
		//String inputFilePath="D:\\100rabh\\Others\\Development\\GitWorkSpaces\\selnm\\Learn_Selenium\\resources\\TestData\\TestInputData.xlsx";
		String inputFilePath="./resources/TestData/TestInputData.xlsx";
		FileInputStream inputStream = new FileInputStream(new File(inputFilePath));
		
		Workbook workBook = new XSSFWorkbook(inputStream);
		//Workbook workBook = new XSSFWorkbook(inputStream);
 		
		//Sheet firstSheet = (Sheet) workBook.getSheetAt(0);
		//Sheet firstSheet = workBook.getSheetAt(0);
		Sheet firstSheet = workBook.getSheet("Sheet1");
		Iterator<Row> rowIterator = firstSheet.iterator();
		
		String password, userName;
		
		while(rowIterator.hasNext())
		{
			Row nextRow = rowIterator.next();
			Iterator<Cell> cellIterator = nextRow.cellIterator();
			Cell cell = null;
			while(cellIterator.hasNext())
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
				case BOOLEAN:
				//case CellType.BOOLEAN:
					System.out.println(cell.getBooleanCellValue());
					break;
				case NUMERIC:
					System.out.println(cell.getBooleanCellValue());
					break;
				case BLANK:
					break;
				case ERROR:
					break;
				case FORMULA:
					break;
				case _NONE:
					break;
				default:
					System.out.println("UserName not found!!!");
					break;
				}
				//System.out.println(" - ");
			}
			//System.out.println(cell.getSheet().getRow(0).getCell(0).toString());
			System.out.println();
		}
		workBook.close();
		inputStream.close();
		//driver.quit();
	}
	public static void main(String[] args) throws IOException 
	{
		GmailLoginUsingXLFile g =  new GmailLoginUsingXLFile();
		g.initializeProperties();
		g.login();
		//driver = new FirefoxDriver();
		
		//System.setProperty("webdriver.chrome.driver", "D:\\100rabh\\Others\\Development\\GitWorkSpaces\\selnm\\Learn_Selenium\\lib\\chromedriver.exe");
	}
}

