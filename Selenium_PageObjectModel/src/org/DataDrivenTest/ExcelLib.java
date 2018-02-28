package org.DataDrivenTest;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class ExcelLib 
{
	Workbook wrkBook = null;
	Sheet wrkSheet = null;
	Hashtable dict =  new Hashtable(); 
	
	// create a constructor 
	public ExcelLib (String path) throws BiffException, IOException
	{
			wrkBook = Workbook.getWorkbook(new File(path));
			wrkSheet = wrkBook.getSheet("Sheet1");
	}
	
	// Returns number of rows
	public int rowCount()
	{
		return wrkSheet.getRows(); 
	}

	//Returns the Cell value by taking row and Column values as argument
	public String readCell(int col, int row)
	{
		return wrkSheet.getCell(col, row).getContents();
	}
	
	//Create Column Dictionary to hold all the Column Names
	public void columnDisctionary()
	{
		//Iterate through all the columns in the Excel sheet and store the value in Hashtable
		for(int col=0;col<wrkSheet.getColumns();col++)
		{
			dict.put(readCell(col, 0), col);
		}
	}
	
	//Read Column Names
	public int getCell(String colName)
	{
		try
		{
			int value;
			value = ((Integer)dict.get(colName)).intValue();
			return value;
		}
		catch(NullPointerException e)
		{
			return (0);
		}
	}
	
	/*public String getCellValue(int colNumber, int rowNumber)
	{
		return wrkBook.getSheet(0).getCell(colNumber, rowNumber).getContents();
	}*/
}
