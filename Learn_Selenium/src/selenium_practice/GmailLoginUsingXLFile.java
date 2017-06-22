package selenium_practice;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class GmailLoginUsingXLFile {

	public static void main(String[] args) throws IOException 
	{
		//WebDriver driver = null;
		//driver = new FirefoxDriver();
		//D:\\100rabh\\Others\\Development\\GitWorkSpaces\\selnm\\Learn_Selenium\\resources\\TestData\\TestInputData.xlsx
		//\\resources\\TestData\\TestInputData.xlsx
		String inputFilePath="D:\\100rabh\\Others\\Development\\GitWorkSpaces\\selnm\\Learn_Selenium\\resources\\TestData\\TestInputData.xlsx";
		FileInputStream inputStream = new FileInputStream(new File(inputFilePath));
		
		Workbook workBook = new XSSFWorkbook(inputStream);
 		
		//Sheet firstSheet = (Sheet) workBook.getSheetAt(0);
		Sheet firstSheet = workBook.getSheetAt(0);
		Iterator<Row> rowIterator = firstSheet.iterator();
		
		while(rowIterator.hasNext())
		{
			Row nextRow = rowIterator.next();
			Iterator<Cell> cellIterator = nextRow.cellIterator();
			
			while(cellIterator.hasNext())
			{
				Cell cell = cellIterator.next();
				
				switch(cell.getCellType())
				{
				case Cell.CELL_TYPE_STRING:
				//case CellType.STRING:
					//System.out.println("column index is "+cell.getColumnIndex());
					if(cell.getColumnIndex()==0)
						System.out.print(cell.getStringCellValue()+" : ");
					else
						System.out.print(cell.getStringCellValue());
					break;
				case Cell.CELL_TYPE_BOOLEAN:
				//case CellType.BOOLEAN:
					System.out.println(cell.getBooleanCellValue());
					break;
				case Cell.CELL_TYPE_NUMERIC:
					System.out.println(cell.getBooleanCellValue());
					break;
				}
				//System.out.println(" - ");
			}
			System.out.println();
		}
		workBook.close();
		inputStream.close();
	}

}
