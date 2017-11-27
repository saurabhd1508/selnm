package automate.weblogic.servers;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class Deployment 
{
	WebDriver driver;
	WeblogicController wlController = new WeblogicController();
	Properties prop = new Properties();
	int complatedDeployments =0;
	public Deployment(WebDriver driver2, Properties prop2)
	{
		this.driver = driver2;
		this.prop=prop2;
	}
	
	public void selectAndDeploy() 
	{
		System.out.println("In Select and Deploy");
		int totalDeployments = 0; 
		String deploymentsString = prop.getProperty("deployments");
		List<String> deploymentsList = Arrays.asList(deploymentsString.split(",")); 
		totalDeployments = deploymentsList.size();
		Iterator<String> deployItr = deploymentsList.iterator();
		
		while(deployItr.hasNext())
		{
			String deployStr = deployItr.next().toString();
			System.out.println("Current Deployment is '"+deployStr+"'");
			boolean isSelectDeployAvailabel = driver.findElements(By.cssSelector("input[title='Select "+ deployStr+"']")).size()>=1;
			if(isSelectDeployAvailabel)
			{
				System.out.println("Deployment '"+deployStr+"' is available");
				takeLockAndEdit();
				WebElement selectDeploy = driver.findElement(By.cssSelector("input[title='Select "+ deployStr+"']"));
				selectDeploy.click();
				wlController.waitForTwoSeconds();
				updateDeployments();
				complatedDeployments = verifyDeploymentSuccessMsg();
				wlController.waitForSec();
				activateChanges();
				System.out.println("Completed deployments are - "+complatedDeployments);
			}
			else
				System.out.println("Selected Deployment is not available");
		}
			
		if(complatedDeployments==totalDeployments)
		{
			releaseLockConfiguration();
			System.out.println("All '"+totalDeployments+"' deployments are completed.");
		}
		else
			System.out.println("Something is wrong with deploymens, please check manually");
	}
	
	private void takeLockAndEdit()
	{
		boolean isBtnLockAndEditEnabled = driver.findElement(By.cssSelector("button[name='save']")).isEnabled();
		if(isBtnLockAndEditEnabled)
		{
			System.out.println("Lock & Edit button is Enabled");
			WebElement btnLockAndEdit = driver.findElement(By.cssSelector("button[name='save']"));
			wlController.highLightElement(driver, btnLockAndEdit);
			btnLockAndEdit.click();
		}
		else
		{
			System.out.println("Lock & Edit button is Disabled");
		}
	}
	
	private void activateChanges()
	{
		boolean isBtnActivateChangesEnabled = driver.findElement(By.cssSelector("button[name='save']")).isEnabled();
		boolean isLblSuccessMsgAvaialbe=false;
		if(isBtnActivateChangesEnabled)
		{
			System.out.println("Activate changes button is Enabled");
			WebElement btnActivateChanges = driver.findElement(By.cssSelector("button[name='save']"));
			wlController.highLightElement(driver, btnActivateChanges);
			btnActivateChanges.click();
			wlController.waitForTwoSeconds();
			isLblSuccessMsgAvaialbe = driver.findElements(By.cssSelector("span[class='message_SUCCESS']")).size()>=1;
			
			System.out.println("Is success message availabel - "+isLblSuccessMsgAvaialbe);
			if(isLblSuccessMsgAvaialbe)
			{
				WebElement lblSuccessMsg = driver.findElement(By.cssSelector("span[class='message_SUCCESS']"));
				System.out.println("'"+lblSuccessMsg.getText()+"'");
				if(lblSuccessMsg.getText().contains("All changes have been activated"))
				{
					System.out.println("Changes have been activated.");
					boolean isBtnReleaseLockEnabled =  driver.findElement(By.cssSelector("button[name='cancel']")).isEnabled();
					if(!isBtnReleaseLockEnabled)
						System.out.println("Lock is also released");
					else
						System.out.println("Lock is not yet released");
				}
				else
					System.out.println("Something is wrong with deployment");
			}
			else
				System.out.println("Something is wrong with deployment");
		}
		else
			System.out.println("Activate changes button is Disabled");
	}	

	private void releaseLockConfiguration()
	{
		boolean isBtnReleaseLockEnabled =  driver.findElement(By.cssSelector("button[name='cancel']")).isEnabled();
		if(isBtnReleaseLockEnabled)
		{
			System.out.println("Lock is not released, so releasing it.");
			WebElement btnReleaseLock = driver.findElement(By.cssSelector("button[name='cancel']"));
			wlController.highLightElement(driver, btnReleaseLock);
			btnReleaseLock.click();
		}
		else
			System.out.println("Lock is already released, button is Disabled");
	}
	
	private void updateDeployments()
	{
		WebElement btnFinish;
		boolean isBtnUpddateAvailable = driver.findElements(By.cssSelector("button[name='Update']")).size()>=1;
		if(isBtnUpddateAvailable)
		{	WebElement btnUpdate;
			btnUpdate = driver.findElement(By.cssSelector("button[name='Update']"));
			wlController.highLightElement(driver, btnUpdate);
			System.out.println("Updating changes");
			btnUpdate.click();
			wlController.waitForTwoSeconds();
			btnFinish = driver.findElement(By.cssSelector("button[name='Finish']"));
			wlController.highLightElement(driver, btnFinish);
			btnFinish.click();
		}
		else
			System.out.println("Update Button is not available");
	}
	
	private int verifyDeploymentSuccessMsg()
	{
		boolean isLblSuccessMsgAvaialbe =false;
		isLblSuccessMsgAvaialbe = driver.findElements(By.cssSelector("span[class='message_SUCCESS']")).size()>=1;
		if(isLblSuccessMsgAvaialbe)
		{
			WebElement lblSuccessMsg = driver.findElement(By.cssSelector("span[class='message_SUCCESS']"));
			if(lblSuccessMsg.getText().contains("Selected Deployments were updated."))
			{
				complatedDeployments++;
				System.out.println("Selected Deployments were updated. Please Activate the changes");
			}
		}
		else
			System.out.println("Success Message is not available");
		return complatedDeployments;
	}
}
