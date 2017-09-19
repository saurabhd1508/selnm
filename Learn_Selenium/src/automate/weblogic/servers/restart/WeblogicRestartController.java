package automate.weblogic.servers.restart;

import java.awt.Dimension;
import java.awt.Image;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

public class WeblogicRestartController 
{
	Properties prop = new Properties();
	private WebDriver driver;
	String environment;

	public void setProperties() throws FileNotFoundException {
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
		System.setProperty("webdriver.chrome.driver",prop.getProperty("webDriverPath"));
		driver = new ChromeDriver();
		loginToWebLogic();
	}

	public void loginToWebLogic()
	{
		driver.get(prop.getProperty("WebLogicBaseUrl"));
		setEnvironment();
		// askUser();
		WebElement txtUser = driver.findElement(By.id("j_username"));
		WebElement txtPass = driver.findElement(By.id("j_password"));
		txtUser.sendKeys(prop.getProperty("HMGWLUserName"));
		txtPass.sendKeys(prop.getProperty("HMGWLPassowrd"));
		WebElement btnLogin = driver.findElement(By.className("formButton"));
		btnLogin.click();
		waitForTwoSeconds();
		String pageSource = driver.getPageSource();
		if (pageSource.contains("welcome"))
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
		if (driver.getCurrentUrl().contains("7001"))
			environment = "LOCAL";
		if (driver.getCurrentUrl().contains("hmg03"))
			environment = "HMG03";
		if (driver.getCurrentUrl().contains("hmg05"))
			environment = "HMG05";
		else if (driver.getCurrentUrl().contains(prop.getProperty("Dom01ConsoleBR")))
			environment = "BR_DOM01";
		else if (driver.getCurrentUrl().contains(prop.getProperty("Dom02ConsoleBR")))
			environment = "BR_DOM02";
		else if (driver.getCurrentUrl().contains(prop.getProperty("Dom03ConsoleBR")))
			environment = "BR_DOM03";
		else if (driver.getCurrentUrl().contains(prop.getProperty("Dom04ConsoleBR")))
			environment = "BR_DOM04";
		else if (driver.getCurrentUrl().contains(prop.getProperty("DomServicesBR")))
			environment = "BR_Services";
	}

	public void navigateToServers()
	{
		WebElement lnkServers = driver.findElement(By.linkText("Servers"));
		highLightElement(lnkServers);
		lnkServers.click();
		navigateToControl();
	}

	private void navigateToControl() {
		waitForTwoSeconds();
		WebElement tabControl = driver.findElement(By.cssSelector("a[title*='Control- Tab']"));
		tabControl.click();
		askUser();
	}

	String restartProcess = null;
	int numOfInstancesToRestart = 0;
	public void askUser()
	{
		// Get Input from config file.
		String inputFromUser = prop.getProperty("instancesToRestart");
		
		// Get input from user at runtime.
		System.out.println("How many instances to be shutdown?");
		if (inputFromUser.equals(""))
			inputFromUser = getUserInputFromPanel();
		if (inputFromUser.equals(""))
		{
			System.out.println("Please enter your input");
			askUser();
		}
		numOfInstancesToRestart = Integer.parseInt(inputFromUser);
		System.out.println(numOfInstancesToRestart + " instances to restart");
		startProcess();
	}

	public String getUserInputFromPanel()
	{
		String input = null;
		Image image = new ImageIcon(prop.getProperty("iconImage")).getImage();
		JOptionPane pane = new JOptionPane("");
		JPanel innerPanel = new JPanel();
		JDialog dialog = pane.createDialog("How many instances to be shutdown?");
		JTextField text = new JTextField(10);
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
	
	int loop = 0, totalInstances = 0, remainingInstances = 0, elementId = 1;
	HashMap<String,Integer> instanceNameAndNumber = null;
	
	public void startProcess() 
	{
		restartProcess = prop.getProperty("restartProcess");
		String instanceName, instanceState;
		WebElement eleSelectInstance;
		
		HashMap<String,String> instancesWithState = null;
		//HashMap<WebElement, HashMap<String,String>> selectWithInstancesAndState = new HashMap<WebElement,HashMap<String,String>>();
		
		instanceNameAndNumber = new HashMap<String, Integer>();
		
		if (environment.equals("HMG03"))
			totalInstances = Integer.parseInt(prop.getProperty("TotalHMG03Instances"));
		else if (environment.equals("HMG05"))
			totalInstances = Integer.parseInt(prop.getProperty("TotalHMG05Instances"));
		else if (environment.equals("LOCAL"))
			totalInstances = Integer.parseInt(prop.getProperty("TotalLocalInstances"));
		
		loop = totalInstances / numOfInstancesToRestart;
		System.out.println("Loop is of "+loop);
		
		if (restartProcess.equals("rolling"))
		{
			for(int i=0;elementId<=totalInstances;i++)
			 {
				for (int j = 1; j <= numOfInstancesToRestart; j++) 
				{
					instancesWithState = new HashMap<String, String>();
					instanceName = driver.findElement(By.id("name" + elementId)).getText();
					instanceState = driver.findElement(By.id("state" + elementId)).getText();
					if(instanceName.equals("AdminServer(admin)"))
					{
						elementId++;
						j--;
						continue;
					}
					instanceNameAndNumber.put(instanceName, elementId);
					if(numOfInstancesToRestart != instanceNameAndNumber.size())
						elementId++;
					eleSelectInstance = driver.findElement(By.cssSelector("input[title='Select " + instanceName + "']"));
					instancesWithState.put(instanceName, instanceState);
				//	selectWithInstancesAndState.put(eleSelectInstance,instancesWithState);
				}
				selectInstances(instanceNameAndNumber);
				 if(isSelectedInstancesAreRunning(instanceNameAndNumber))
				 {
					suspendInstances(instanceNameAndNumber);
					selectInstances(instanceNameAndNumber);
					shutDownInstances(instanceNameAndNumber);
					selectInstances(instanceNameAndNumber);
					startInstances(instanceNameAndNumber);
				 }
				 elementId++;
				 instanceNameAndNumber.clear();
				 if(elementId>totalInstances)
					 System.out.println("Rolling restart Process is completed...");
				 System.out.println("Waiting to normalize instances");
				 waitForTenSeconds();
			 }
		}
		else if (restartProcess.equals("release"))
		{
			for(int i=0;elementId<=totalInstances;i++)
			 {
				for (int j = 1; j <= numOfInstancesToRestart; j++) 
				{
					instancesWithState = new HashMap<String, String>();
					instanceName = driver.findElement(By.id("name" + elementId)).getText();
					instanceState = driver.findElement(By.id("state" + elementId)).getText();
					if(instanceName.equals("AdminServer(admin)"))
					{
						elementId++;
						j--;
						continue;
					}
					instanceNameAndNumber.put(instanceName, elementId);
					if(numOfInstancesToRestart != instanceNameAndNumber.size())
						elementId++;
					eleSelectInstance = driver.findElement(By.cssSelector("input[title='Select " + instanceName + "']"));
					instancesWithState.put(instanceName, instanceState);
					//selectWithInstancesAndState.put(eleSelectInstance,instancesWithState);
				}
				selectInstances(instanceNameAndNumber);
				 if(isSelectedInstancesAreRunning(instanceNameAndNumber))
				 {
					suspendInstances(instanceNameAndNumber);
					selectInstances(instanceNameAndNumber);
					shutDownInstances(instanceNameAndNumber);
					
				 }
				 elementId++;
				 instanceNameAndNumber.clear();
				 System.out.println("Waiting to normalize instances");
				 waitForTenSeconds();
				 if(elementId>totalInstances)
					 System.out.println("Restart Process for RELEASE is completed...");
			 }
		}
		else
			System.out.println("Defined process '" + restartProcess+ "' is not correct");
	}
	
	public void selectInstances(HashMap<String, Integer> instanceNameAndNumber2)
	{
		Set keySet = instanceNameAndNumber2.keySet();
		Iterator itr = keySet.iterator();
		WebElement selectEle=null;
		String instanceName = null;
		while (itr.hasNext()) 
		{
			try
			{
				instanceName = itr.next().toString();
				selectEle = driver.findElement(By.cssSelector("input[title='Select " + instanceName + "']"));
				selectEle.click();
			}
			catch(StaleElementReferenceException e)
			{
				instanceName = itr.next().toString();
				selectEle = driver.findElement(By.cssSelector("input[title='Select " + instanceName + "']"));
				selectEle.click();
				/*System.out.println("Caught Stale Element Exception... continuing with execution");
				selectEle = (WebElement) itr.next();
				selectEle.click();*/
			}
		}
	}
	
	public void refreshPage()
	{
		waitForTenSeconds();
		driver.navigate().refresh();
	}
	public void startInstances(HashMap<String, Integer> instanceNameAndNumber2) 
	{
		System.out.println("Starting Instances");
		WebElement btnStart = driver.findElement(By.cssSelector("button[name='Start']"));
		highLightElement(btnStart);
		btnStart.click();
		if(environment.equals("HMG05"))
			selectServerLifeCycleAssistant();
		//startAutoRefresh();
		try {
			Set keySet = instanceNameAndNumber.keySet();
			Iterator itr = keySet.iterator();
			String instanceName = null;
			String instanceState = null;
			Integer instanceNumber = null;
			while (itr.hasNext()) 
			{
				instanceName = (String) itr.next();
				instanceNumber = instanceNameAndNumber.get(instanceName);
				while (true) 
				{
					instanceNumber = instanceNameAndNumber.get(instanceName);
					instanceState = driver.findElement(By.id("state" + instanceNumber)).getText();
					//System.out.println("Instance is '" + instanceName + "' and its state is '" + instanceState + "'");
					refreshPage();
					if (instanceState.equals("RUNNING")) 
					{
						System.out.println("'"+instanceName + "' is started and running now");
						break;
					}
				}
			}
			//stopAutoRefresh();
		}
		catch(StaleElementReferenceException e)
		{
			System.out.println("Caught Stale Element Exception... continuing with execution");
			Set keySet = instanceNameAndNumber.keySet();
			Iterator itr = keySet.iterator();
			String instanceName = null;
			String instanceState = null;
			Integer instanceNumber = null;
			while (itr.hasNext()) 
			{
				instanceName = (String) itr.next();
				instanceNumber = instanceNameAndNumber.get(instanceName);
				while (true) 
				{
					instanceNumber = instanceNameAndNumber.get(instanceName);
					instanceState = driver.findElement(By.id("state" + instanceNumber)).getText();
					refreshPage();
					if (instanceState.equals("RUNNING")) 
					{
						System.out.println("'"+instanceName + "' is started and running now");
						break;
					}
				}
			}
			//stopAutoRefresh();
		}
	}

	
	
	public void suspendInstances(HashMap<String, Integer> instanceNameAndNumber) 
	{
		System.out.println("Suspending Instances");
		WebElement btnSuspend = driver.findElement(By.cssSelector("button[name='Suspend']"));
		highLightElement(btnSuspend);
		btnSuspend.click();
		WebElement lnkForceSuspend = driver.findElement(By.linkText("Force Suspend Now"));
		highLightElement(lnkForceSuspend);
		lnkForceSuspend.click();
		if(environment.equals("HMG05"))
		{
			selectServerLifeCycleAssistant();
		}
		startAutoRefresh();
		try {
			Set keySet = instanceNameAndNumber.keySet();
			Iterator itr = keySet.iterator();
			while (itr.hasNext()) 
			{
				while(true)
				{
					String instanceName = null;
					String instanceState = null;
					Integer instanceNumber = null;
					instanceName = (String) itr.next();
					instanceNumber = instanceNameAndNumber.get(instanceName);
					instanceState = driver.findElement(By.id("state" + instanceNumber)).getText();
					System.out.println("Instance is '"+instanceName+"' and its state is '"+instanceState+"'");
					if (instanceState.equals("ADMIN"))
					{
						System.out.println("'"+instanceName + "' got suspended");
						break;
					}
				}
			}
			stopAutoRefresh();
		}
		catch(StaleElementReferenceException e)
		{
			Set keySet = instanceNameAndNumber.keySet();
			Iterator itr = keySet.iterator();
			while (itr.hasNext())
			{
				while(true)
				{
					String instanceName = null;
					String instanceState = null;
					Integer instanceNumber = null;
					instanceName = (String) itr.next();
					instanceNumber = instanceNameAndNumber.get(instanceName);
					instanceState = driver.findElement(By.id("state" + instanceNumber)).getText();
					System.out.println("Instance is '"+instanceName+"' and its state is '"+instanceState+"'");
					if (instanceState.equals("ADMIN"))
					{
						System.out.println("'"+instanceName + "' got suspended");
						break;
					}
				}
			}
			stopAutoRefresh();
		}
	}

	public boolean isSelectedInstancesAreRunning(HashMap<String,Integer> instanceNameAndNumber)
	{
		Set keySet = instanceNameAndNumber.keySet();
		Iterator itr = keySet.iterator();
		int running=0,mapSize=0;
		
		mapSize = instanceNameAndNumber.size();
		while (itr.hasNext())
		{
			String instanceName = null;
			String instanceState = null;
			Integer instanceNumber = null;

			instanceName = (String) itr.next();
			instanceNumber = instanceNameAndNumber.get(instanceName);
			instanceState = driver.findElement(By.id("state" + instanceNumber)).getText();
			System.out.println("Instance is '" + instanceName + "' and its state is '" + instanceState + "'");
			if(instanceState.equals("RUNNING"))
			{
				running++;
			}
		}
		if(running==mapSize)
		{
			System.out.println("Selected instances are running "+running+" "+mapSize);
			return true;
		}
		else
		{
			System.out.println("Selected instances are NOT Running "+running+" "+mapSize);
			return false;
		}
	}
	
	public void selectServerLifeCycleAssistant()
	{
		WebElement btnYes =driver.findElement(By.cssSelector("button[name='Yes']"));
		btnYes.click();
	}
	
	public void shutDownInstances(HashMap<String, Integer> instanceNameAndNumber) 
	{
		System.out.println("Shutting down Instances");
		WebElement btnShutdown = driver.findElement(By.cssSelector("button[name='Shutdown']"));
		highLightElement(btnShutdown);
		btnShutdown.click();
		waitForTwoSeconds();
		WebElement lnkForceShutdown = driver.findElement(By.linkText("Force Shutdown Now"));
		highLightElement(lnkForceShutdown);
		lnkForceShutdown.click();
		if(environment.equals("HMG05"))
			selectServerLifeCycleAssistant();
		//startAutoRefresh();
		try {
			Set keySet = instanceNameAndNumber.keySet();
			Iterator itr = keySet.iterator();
			String instanceName = null;
			String instanceState = null;
			Integer instanceNumber = null;
			while (itr.hasNext()) 
			{
				instanceName = (String) itr.next();
				instanceNumber = instanceNameAndNumber.get(instanceName);
				
				while (true) 
				{
					instanceState = driver.findElement(By.id("state" + instanceNumber)).getText();
					//System.out.println("Instance is '" + instanceName + "' and its state is '" + instanceState + "'");
					refreshPage();
					if (instanceState.equals("SHUTDOWN")) 
					{
						System.out.println("'"+instanceName + "' got shutdown");
						break;
					}
				}
			}
			//stopAutoRefresh();
		}
		catch(StaleElementReferenceException e)
		{
			Set keySet = instanceNameAndNumber.keySet();
			Iterator itr = keySet.iterator();
			String instanceName = null;
			String instanceState = null;
			Integer instanceNumber = null;
			while (itr.hasNext()) 
			{
				instanceName = (String) itr.next();
				instanceNumber = instanceNameAndNumber.get(instanceName);
				
				while (true) 
				{
					instanceNumber = instanceNameAndNumber.get(instanceName);
					instanceState = driver.findElement(By.id("state" + instanceNumber)).getText();
					//System.out.println("Instance is '" + instanceName + "' and its state is '" + instanceState + "'");
					refreshPage();
					if (instanceState.equals("SHUTDOWN")) 
					{
						System.out.println("'"+instanceName + "' got shutdown");
						break;
					}
				}
			}
			//stopAutoRefresh();
		}
	}
	
	public void waitForTwoSeconds()
	{
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void waitForTenSeconds()
	{
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
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
		js.executeScript("arguments[0].setAttribute('style', arguments[1]);",webElement, "");
	}
	
	public boolean isAutoRefreshRunning()
	{
		String refreshStatus = driver.findElement(By.id("refreshIcondefaultRegion")).getAttribute("alt");
		if(refreshStatus.contains("Start refresh"))
		{
			return false;
		}
		if(refreshStatus.contains("Stop refresh"))
		{
			return true;
		}
		return false;
	}
	
	public void startAutoRefresh()
	{
		if(isAutoRefreshRunning())
			System.out.println("AutoRefresh is already running");
		else
		{
			waitForTwoSeconds();
			WebElement imgRefresh = driver.findElement(By.id("refreshIcondefaultRegion"));
			imgRefresh.click();
			System.out.println("Auto Refresh Started.");
		}
	}
	public void stopAutoRefresh()
	{
		if(isAutoRefreshRunning())
		{
			waitForTwoSeconds();
			WebElement imgRefresh = driver.findElement(By.id("refreshIcondefaultRegion"));
			imgRefresh.click();
			System.out.println("Auto Refresh Stopped");
		}
		else
		{
			System.out.println("Auto Refresh is not running");
		}
	}

	public static void main(String[] args) throws FileNotFoundException
	{
		WeblogicRestartController wl = new WeblogicRestartController();
		wl.setProperties();
		System.exit(0);
	}
}