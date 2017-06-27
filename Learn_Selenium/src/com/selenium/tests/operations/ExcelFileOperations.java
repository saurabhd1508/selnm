package com.selenium.tests.operations;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

public class ExcelFileOperations 
{
	public FileInputStream setFile(String filePath) throws FileNotFoundException
	{
		FileInputStream inputStream = new FileInputStream(new File(filePath));
		return inputStream;
	}
	
	public void createFile(String fileName)
	{
		
	}
	
	public String getStringCellData(Cell cell, Row nextRow, String str)
	{
		if(cell.getStringCellValue().equalsIgnoreCase(str))
		{
			int nextColumnIndex = cell.getColumnIndex()+1;
			str = nextRow.getCell(nextColumnIndex).toString();
			return str;
		}
		return str;
	}
	
	public void getNumericCellData()
	{
		
	}
}