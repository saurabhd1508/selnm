package automate.weblogic.servers.restart;

import java.awt.Dimension;
import java.awt.Image;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.print.attribute.HashAttributeSet;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import junit.framework.Assert;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

public class WeblogicRestartController 
{
	Properties prop = new Properties();
	
	private WebDriver driver;
	String environment;
	public void setProperties() throws FileNotFoundException
	{
		InputStream inputPropFile = new FileInputStream("./resources/properties/weblogicConfigs.properties");
		try {
			prop.load(inputPropFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		setWebdriver();
	}
	
	public void setWebdriver()
	{
		System.setProperty("webdriver.chrome.driver", prop.getProperty("webDriverPath"));
		driver =  new ChromeDriver();
		loginToWebLogic();
	}
	
	
	public void loginToWebLogic()
	{
		driver.get(prop.getProperty("WebLogicBaseUrl"));
		setEnvironment();
		//askUser();
		WebElement txtUser = driver.findElement(By.id("j_username"));
		WebElement txtPass = driver.findElement(By.id("j_password"));
		txtUser.sendKeys(prop.getProperty("HMGWLUserName"));
		txtPass.sendKeys(prop.getProperty("HMGWLPassowrd"));
		WebElement btnLogin = driver.findElement(By.className("formButton"));
		btnLogin.click();
		
		String pageSource = driver.getPageSource();
		//System.out.println(pageSource);
		/*boolean isLoggedIn = false;
		try {
			Thread.sleep(3000);
			isLoggedIn = driver.findElements(By.id("welcome")).size()>1;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		*/
		//if(isLoggedIn)
		if(pageSource.contains("welcome"))
		{
			System.out.println("Successfully logged in to Weblogic Admin Console");
			navigateToServers();
		}
		else
		{
			System.out.println("Something is wrong with Weblogic Admin Console");
			System.exit(0);
		}
	}
	
	public void setEnvironment()
	{
		if(driver.getCurrentUrl().contains("hmg03"))
		{
			environment = "HMG03"; 
		}
		if(driver.getCurrentUrl().contains("hmg05"))
		{
			environment = "HMG05"; 
		}
		else if(driver.getCurrentUrl().contains(prop.getProperty("Dom01ConsoleBR")))
		{
			environment = "BR_DOM01";
		}
		else if(driver.getCurrentUrl().contains(prop.getProperty("Dom02ConsoleBR")))
		{
			environment = "BR_DOM02";
		}
		else if(driver.getCurrentUrl().contains(prop.getProperty("Dom03ConsoleBR")))
		{
			environment = "BR_DOM03";
		}
		else if(driver.getCurrentUrl().contains(prop.getProperty("Dom04ConsoleBR")))
		{
			environment = "BR_DOM04";
		}
		else if(driver.getCurrentUrl().contains(prop.getProperty("DomServicesBR")))
		{
			environment = "BR_Services";
		}
	}
	public void navigateToServers() 
	{
		WebElement lnkServers = driver.findElement(By.linkText("Servers"));
		highLightElement(lnkServers);
		lnkServers.click();
		navigateToControl();
	}
	public void highLightElement(WebElement webElement)
	{
		String DELAY = "delay";
		JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeScript("arguments[0].setAttribute('style', arguments[1]);",webElement, "color: green; border: 4px solid green;");
		try {
			int time = 500;
			if (System.getProperty(DELAY) != null) {
				time = Integer.parseInt(System.getProperty(DELAY));
			}
			Thread.sleep(time);
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		} catch (NumberFormatException numForE) {
			numForE.printStackTrace();
		}
		js.executeScript("arguments[0].setAttribute('style', arguments[1]);",
				webElement, "");
	}
	private void navigateToControl() 
	{
		WebElement tabControl = driver.findElement(By.cssSelector("a[title*='Control- Tab']"));
		tabControl.click();
		askUser();
	}

    public String getUserInputFromPanel()
	{
    	String input = null;
    	Image image = new ImageIcon(prop.getProperty("iconImage")).getImage();
    	JOptionPane pane = new JOptionPane("");
        JPanel innerPanel = new JPanel();
        JDialog dialog = pane.createDialog("How many instances to be shutdown?");
        JTextField text = new JTextField(10);
       // JTextField text = new JTextField() { public void addNotify() { super.addNotify();requestFocus();}};
        innerPanel.add(text);
        dialog.setIconImage(image);
        dialog.add(innerPanel);
        dialog.pack();
        dialog.setSize(new Dimension(300, 150));
        dialog.setLocationRelativeTo(text);
        dialog.setAlwaysOnTop(true);
        dialog.setVisible(true);
        text.setVisible(true);
        text.setFocusable(true);
        text.requestFocus();
        input = text.getText();
		return input;
	}
    String restartProces =null;
    int numOfInstancesToRestart = 0;
	public void askUser() 
	{
		System.out.println("How many instances to be shutdown?");
		
		String inputFromUser = prop.getProperty("instancesToRestart");
		if(inputFromUser.equals(""))
			inputFromUser =	getUserInputFromPanel();
		if(inputFromUser.equals(""))
		{
			System.out.println("Please enter your input");
			askUser();
		}
		numOfInstancesToRestart = Integer.parseInt(inputFromUser);
		System.out.println(numOfInstancesToRestart+" instances to restart");

		
		if(numOfInstancesToRestart==1)
		{
			System.out.println("First is Admin Server itself, you can't restart it. Please enter again");
			askUser();
		}
		else
		{
			restartInstances();
		}
		//WebElement bccInstance = driver.findElement(By.tagName("input")).getAttribute("title").contains("Select atg_bcc");
		
	}
	public void restartInstances()
	{
		restartProces = prop.getProperty("restartProces");
		if(restartProces.equals("rolling"))
		{
			System.out.println("Restarting '"+numOfInstancesToRestart+"' in a group");
			selectInstances();
		}
		else if(restartProces.equals("release"))
		{
			
		}
		else
			System.out.println("Defined Restart process '"+restartProces+"' is not correct");
		
		/*WebElement bccInstance = driver.findElement(By.cssSelector("input[title='Select atg_bcc']"));
		bccInstance.click();
		highLightElement(bccInstance);*/
	}
	int totalInstances =0;
	public void selectInstances()
	{
		String instanceName;
		String instanceState;
		WebElement eleSelectInstance;
		int eleId=3;
		if(environment.equals("HMG03"))
			totalInstances = Integer.parseInt(prop.getProperty("TotalHMG03Instances"));
		else if(environment.equals("HMG05"))
			totalInstances = Integer.parseInt(prop.getProperty("TotalHMG05Instances"));
		
		instanceName = driver.findElement(By.id("name"+eleId)).getText();
		instanceState = driver.findElement(By.id("state"+eleId)).getText();
		eleSelectInstance = driver.findElement(By.cssSelector("input[title='Select "+instanceName+"']"));
		eleSelectInstance.click();
		suspendInstances();
		/*HashMap<String,String> instancesWithState;
		HashMap<WebElement, HashMap<String,String>> selectWithInstancesAndState = new HashMap<WebElement,HashMap<String,String>>();
		
		for(int i=0;i<(totalInstances/numOfInstancesToRestart);i++)
		{
			for(int j=0;j<numOfInstancesToRestart;j++)
			{
				instancesWithState = new HashMap<String, String>();
				instanceName = driver.findElement(By.id("name"+eleId)).getText();
				instanceState = driver.findElement(By.id("state"+eleId)).getText();
				
				eleSelectInstance = driver.findElement(By.cssSelector("input[title='Select "+instanceName+"']"));
				//System.out.println("Name of instance to be selected is '"+instanceName+"' and its state is '"+ instanceState+"'\n");
				instancesWithState.put(instanceName, instanceState);
				//System.out.println("Putting data in instancesWithState '"+instancesWithState.keySet().iterator().next()+"'\n"); 
				selectWithInstancesAndState.put(eleSelectInstance, instancesWithState);
				//System.out.println("Data in selectWithInstancesAndState "+selectWithInstancesAndState+"\n"); 
				//System.out.println("Current State of '"+name+"' is '"+instanceState+"'");
				//totalInstances = totalInstances - numOfInstancesToRestart;
				eleId++;
			}	
		
			Set keySet = selectWithInstancesAndState.keySet();
			Iterator itr = keySet.iterator();
			while (itr.hasNext()) 
			{
				WebElement selectEle = (WebElement) itr.next();
				selectEle.click();
				
				//System.out.println(type.getText());
				Map innerInstancesWithState = selectWithInstancesAndState.get(selectEle);
				Set innerSet = innerInstancesWithState.keySet();
				Iterator innerItr = innerSet.iterator();
				while (innerItr.hasNext()) 
				{
					String nameNstate = (String) innerItr.next();
					//System.out.println(selectEle.toString()+"\t" + nameNstate+"\t - "+innerInstancesWithState.get(nameNstate));
				}
			}
			suspendInstances();
			try {
				System.out.println("Sleeping for 10 Sec");
				Thread.sleep(10000);
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}*/
	}
	
	public void startAutoRefresh()
	{
		WebElement imgRefresh = driver.findElement(By.cssSelector("img[id='refreshIcondefaultRegion']"));
		imgRefresh.click();
	}
	public void suspendInstances()
	{
		WebElement btnSuspend = driver.findElement(By.cssSelector("button[name='Suspend']"));
		highLightElement(btnSuspend);
		btnSuspend.click();
		WebElement lnkForceSuspend = driver.findElement(By.linkText("Force Suspend Now"));
		highLightElement(lnkForceSuspend);
		lnkForceSuspend.click();
		WebElement btnYes = driver.findElement(By.cssSelector("button[name='Yes']"));
		btnYes.click();
		startAutoRefresh();
	}
	
	public void shutDownInstances()
	{
		
	}
	public static void main(String[] args) throws FileNotFoundException 
	{
		WeblogicRestartController wl = new WeblogicRestartController();
		wl.setProperties();
		System.exit(0);
	}
}