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
import java.util.concurrent.TimeUnit;

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

public class WeblogicRestartController {
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
		System.out.println("How many instances to be shutdown?");
		String inputFromUser = prop.getProperty("instancesToRestart");
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
	
	public void startProcess()
	{
		restartProcess = prop.getProperty("restartProcess");
		
		if (environment.equals("HMG03"))
			totalInstances = Integer.parseInt(prop.getProperty("TotalHMG03Instances"));
		else if (environment.equals("HMG05"))
			totalInstances = Integer.parseInt(prop.getProperty("TotalHMG05Instances"));
		else if (environment.equals("LOCAL"))
			totalInstances = Integer.parseInt(prop.getProperty("TotalLocalInstances"));
		
		if (restartProcess.equals("rolling"))
		{
			 for(int i=0;eleId<=totalInstances;i++)
			 {
				 System.out.println("Restarting '" + numOfInstancesToRestart+ "' in a group");
				 selectInstances();
				 if(isSelectedInstancesAreRunning(instanceNameAndNumber))
				 {
					suspendInstances(instanceNameAndNumber);
					eleId=2;
					selectInstances();
					shutDownInstances(instanceNameAndNumber);
				 }
			 }
		}
		else if (restartProcess.equals("release"))
		{
		}
		else
			System.out.println("Defined process '" + restartProcess+ "' is not correct");
	}

	int totalInstances = 0;
	int remainingInstances = 0;
	HashMap<String,Integer> instanceNameAndNumber=null;
	int eleId = 2;
	public void selectInstances()
	{
		String instanceName;
		String instanceState;
		WebElement eleSelectInstance;

		waitForTwoSeconds();
		
		HashMap<String,String> instancesWithState = null;
		HashMap<WebElement, HashMap<String,String>> selectWithInstancesAndState = new HashMap<WebElement,HashMap<String,String>>();
		
		instanceNameAndNumber = new HashMap<String, Integer>();
		remainingInstances = totalInstances % numOfInstancesToRestart;
		System.out.println(remainingInstances);
		// for(int i=0;i<(totalInstances/numOfInstancesToRestart);i++)
		
		for (int j = 0; j < numOfInstancesToRestart; j++) 
		{
			instancesWithState = new HashMap<String, String>();
			instanceName = driver.findElement(By.id("name" + eleId)).getText();
			instanceState = driver.findElement(By.id("state" + eleId)).getText();
			instanceNameAndNumber.put(instanceName, eleId);
			eleSelectInstance = driver.findElement(By.cssSelector("input[title='Select " + instanceName + "']"));
			instancesWithState.put(instanceName, instanceState);
			selectWithInstancesAndState.put(eleSelectInstance,instancesWithState);
			eleId++;
		}
		Set keySet = selectWithInstancesAndState.keySet();
		Iterator itr = keySet.iterator();
		while (itr.hasNext()) {
			WebElement selectEle = (WebElement) itr.next();
			selectEle.click();
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
						System.out.println(instanceName + " got suspended");
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
						System.out.println(instanceName + " got suspended");
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
	
	public void suspendInstances(String instanceName, String instanceState,int eleId)
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
			while (true) {
				instanceName = driver.findElement(By.id("name" + eleId)).getText();
				instanceState = driver.findElement(By.id("state" + eleId)).getText();
				if (instanceState.equals("ADMIN")) {
					System.out.println(instanceName + " got suspended");
					break;
				}
			}
		}
		catch(StaleElementReferenceException e)
		{
			while (true)
			{
				instanceName = driver.findElement(By.id("name" + eleId)).getText();
				instanceState = driver.findElement(By.id("state" + eleId)).getText();
				if (instanceState.equals("ADMIN"))
				{
					System.out.println(instanceName+" got suspended");
					break;
				}
			}
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
		WebElement lnkForceShutdown = driver.findElement(By.linkText("Force Shutdown Now"));
		highLightElement(lnkForceShutdown);
		lnkForceShutdown.click();
		if(environment.equals("HMG05"))
			selectServerLifeCycleAssistant();
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
					if (instanceState.equals("SHUTDOWN"))
					{
						System.out.println(instanceName + " got shutdown");
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
					if (instanceState.equals("SHUTDOWN"))
					{
						System.out.println(instanceName + " got shutdown");
						break;
					}
				}
			}
			stopAutoRefresh();
		}
	}
	
	/*public void shutDownInstances(String instanceName, String instanceState,int eleId)
	{
		WebElement eleSelectInstance;
		eleSelectInstance = driver.findElement(By.cssSelector("input[title='Select " + instanceName + "']"));
		eleSelectInstance.click();
		WebElement btnShutdown = driver.findElement(By.cssSelector("button[name='Shutdown']"));
		highLightElement(btnShutdown);
		btnShutdown.click();
		WebElement lnkForceShutdown = driver.findElement(By.linkText("Force Shutdown Now"));
		highLightElement(lnkForceShutdown);
		lnkForceShutdown.click();
		if(environment.equals("HMG05"))
			selectServerLifeCycleAssistant();
		startAutoRefresh();
		try{
			while (true)
			{
				instanceName = driver.findElement(By.id("name" + eleId)).getText();
				instanceState = driver.findElement(By.id("state" + eleId)).getText();
				if (instanceState.equals("SHUTDOWN"))
				{
					System.out.println("Instance '"+instanceName+"' is shutdown");
					break;
				}
			}
		}
		catch(StaleElementReferenceException e)
		{
			System.out.println("Stale Exception caught");
			while (true)
			{
				instanceName = driver.findElement(By.id("name" + eleId)).getText();
				instanceState = driver.findElement(By.id("state" + eleId)).getText();
				if (instanceState.equals("SHUTDOWN"))
				{
					System.out.println("Instance '"+instanceName+"' is shutdown");
					stopAutoRefresh();
					break;
				}
			}
		}
	}*/
	
	public void waitForTwoSeconds()
	{
		try {
			Thread.sleep(2000);
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