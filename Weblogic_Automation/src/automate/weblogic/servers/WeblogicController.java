package automate.weblogic.servers;

import java.awt.Dimension;
import java.awt.Image;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class WeblogicController 
{
	private Properties prop = new Properties();
	private WebDriver driver;
	private String process = null;
	private int numOfInstancesToProcess = 0;
	private int totalInstances = 0;
	private int elementId = 1;
	private int failedToStartInstancesCount=0;
	Deployment deploy = null;
	@BeforeMethod
	public void setProperties() throws FileNotFoundException 
	{
		InputStream inputPropFile = new FileInputStream("./resources/properties/weblogicConfigs.properties");
		try {
			prop.load(inputPropFile);
			totalInstances = Integer.parseInt(prop.getProperty("totalInstances"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setWebdriver()
	{
		System.setProperty("webdriver.chrome.driver",prop.getProperty("webDriverPath"));
		driver = new ChromeDriver();
	}
	@Test
	public void loginToWebLogic()
	{
		setWebdriver();
		WebElement txtUser= null, txtPass=null;
		boolean isTxtUseravailable=false;
		String uName=null;
		String password = null;
		String baseUrl = prop.getProperty("weblogicBaseUrl"); 
		if(baseUrl.equals(null) || baseUrl.equals("") || baseUrl.equals(" "))
		{
			System.out.println("Please enter value for weblogic's base url in weblogicConfigs.properties file");
			driver.close();
			driver.quit();
		}
		else
			driver.get(baseUrl);
			
		isTxtUseravailable = driver.findElements(By.id("j_username")).size()>=1;
		if(isTxtUseravailable)
		{
			uName = prop.getProperty("userName");
			password = prop.getProperty("password");
			
			if(uName.equals(null) || uName.equals("") || uName.equals(" "))
			{
				System.out.println("Please enter correct value for userName in weblogicConfigs.properties file");
				driver.close();
				driver.quit();
			}
			if(password.equals(null) || password.equals("") || password.equals(" ") || password.equals("XXXXX"))
			{
				System.out.println("Please enter correct value for password in weblogicConfigs.properties file");
				driver.close();
				driver.quit();
			}
			
			txtUser = driver.findElement(By.id("j_username"));
			txtPass = driver.findElement(By.id("j_password"));
			
			highLightElement(driver,txtUser);
			txtUser.sendKeys(uName);
			
			highLightElement(driver,txtPass);
			txtPass.sendKeys(password);
			
			WebElement btnLogin = driver.findElement(By.className("formButton"));
			highLightElement(driver,btnLogin);
			btnLogin.click();
			waitForTwoSeconds();
			String pageSource = driver.getPageSource();
			if (pageSource.contains("welcome"))
			{
				System.out.println("Successfully logged in to Weblogic Admin Console");
				navigateToServers();
			}
			else if(driver.getPageSource().contains("Authentication Denied"))
			{
				System.out.println("Please enter correct values for userName or password in weblogicConfigs.properties file");
				driver.close();
				driver.quit();
			}
			else
				System.out.println("Something is wrong with Weblogic Admin Console");
		}
		else
		{
			System.out.println("Something is wrong with Weblogic Admin Console");
		}
	}

	public void navigateToEnvironmentPage()
	{
		boolean isLnkEnvironmentAvailable = driver.findElements(By.id("linkEnvironmentsSummaryPage")).size()>=1;
		if(isLnkEnvironmentAvailable)
		{
			WebElement lnkEnvironmentAvailable = driver.findElement(By.id("linkEnvironmentsSummaryPage"));
			lnkEnvironmentAvailable.click();
		}
	}
	
	public void navigateToServers()
	{
		boolean isLnkServersAvailable = driver.findElements(By.linkText("Servers")).size()>=1;
		if(isLnkServersAvailable)
		{
			WebElement lnkServers = driver.findElement(By.linkText("Servers"));
			highLightElement(driver,lnkServers);
			lnkServers.click();
			waitForTwoSeconds();
			navigateToControl();
		}
		else
		{
			navigateToEnvironmentPage();
			WebElement lnkServers = driver.findElement(By.linkText("Servers"));
			highLightElement(driver,lnkServers);
			lnkServers.click();
			waitForTwoSeconds();
			navigateToControl();
		}
	}
	
	public boolean isElementPresent(By element)
	{
		boolean isElement = driver.findElements(element).size()>=1;
		if(isElement)
			return true;
		else
			return false;
	}
	
	public void navigateToControl() 
	{
		WebElement tabControl = null;
		if(isElementPresent(By.cssSelector("a[title*='Control- Tab']")))
		{
			try
			{
				tabControl = driver.findElement(By.cssSelector("a[title*='Control- Tab']"));
				highLightElement(driver,tabControl);
				tabControl.click();
				askUserForNumberOfInstancesToProcess();
			}
			catch(WebDriverException e)
			{
				if(e.getMessage().contains("Element is not clickable at point")||e.getMessage().contains("stale element reference"))
				{	
					JavascriptExecutor jse = (JavascriptExecutor)driver;
					jse.executeScript("arguments[0].scrollIntoView()", tabControl); 
					highLightElement(driver,tabControl);
					tabControl.click();
					askUserForNumberOfInstancesToProcess();
				}
			}
		}
	}
	
	public void askUserForNumberOfInstancesToProcess()
	{
		// Get Input from config file.
		String inputFromUser = prop.getProperty("numberOfInstancesToProcessInLoop");
		
		// Get input from user at runtime.
		System.out.println("How many instances to be shutdown in a group?");
		String questionForUser = "How many instances to be shutdown in a group?";
		if (inputFromUser.equals(""))
			inputFromUser = getUserInputFromPanel(questionForUser);
		if (inputFromUser.equals(""))
		{
			System.out.println("Please enter your input");
			askUserForNumberOfInstancesToProcess();
		}
		numOfInstancesToProcess = Integer.parseInt(inputFromUser);
		System.out.println(numOfInstancesToProcess + " instances to process");
		
		//Starting the actual Process...
		
		selectProcess();
	}
	
	@AfterMethod
	public void quitBrowserWindow()
	{
		System.out.println("Will close browser window after five minutes");
		waitForFiveMinutes();
		driver.close();
		driver.quit();
	}
	
	public String getUserInputFromPanel(String questionForUser)
	{
		String input = null;
		Image image = new ImageIcon(prop.getProperty("iconImage")).getImage();
		JOptionPane pane = new JOptionPane("");
		JPanel innerPanel = new JPanel();
		JDialog dialog = pane.createDialog(questionForUser);
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
	public HashMap<String, Integer> generateMap()
	{
		boolean isInstancePresent= false;
		String instanceName= null;
		HashMap<String,Integer> instanceNameAndNumber = null;
		instanceNameAndNumber = new HashMap<String, Integer>();
		for (int j = 1; j <= numOfInstancesToProcess; j++) 
		{
			isInstancePresent = driver.findElements(By.id("name" + elementId)).size() >= 1;
			if (isInstancePresent) 
			{
				instanceName = driver.findElement(By.id("name" + elementId)).getText();
				if (instanceName.contains("admin")) 
				{
					elementId++;
					j--;
					continue;
				}
				instanceNameAndNumber.put(instanceName, elementId);
				if (numOfInstancesToProcess != instanceNameAndNumber.size())
					elementId++;
			}
		}
		elementId++;
		return instanceNameAndNumber;
	}
	public void suspendProcess()
	{
		String instanceName=null;
		String instanceState=null;
		HashMap<String,Integer> instanceNameAndNumber = null;
		instanceNameAndNumber = new HashMap<String, Integer>();
		boolean isInstancePresent= false;
		for(int i=0;elementId<=totalInstances;i++)
		{
			for (int j = 1; j <= numOfInstancesToProcess; j++) 
			{
				isInstancePresent = driver.findElements(By.id("name" + elementId)).size() >= 1;
				if(isInstancePresent)
				{
					instanceName = driver.findElement(By.id("name" + elementId)).getText();
					if(instanceName.contains("admin"))
					{
						elementId++;
						j--;
						continue;
					}
					instanceState = checkInstanceState(elementId);
					if(instanceState.equals("RUNNING"))
						instanceNameAndNumber.put(instanceName, elementId);
					if(numOfInstancesToProcess != instanceNameAndNumber.size())
						elementId++;
				}
			}
			startSuspendProcess(instanceNameAndNumber);
			waitForTwoSeconds();
			elementId++;
			instanceNameAndNumber.clear();
			if(elementId>totalInstances)
				System.out.println("Suspend Process for RELEASE is completed...");
		}
	}
	
	public void startSuspendProcess(HashMap<String, Integer> instanceNameAndNumber)
	{
		selectInstances(instanceNameAndNumber);
		if(isSelectedInstancesAreRunning(instanceNameAndNumber))
			suspendInstances(instanceNameAndNumber);
		else if(isSelectedInstancesAreShutdown(instanceNameAndNumber))
		{
			deSelectInstances(instanceNameAndNumber);
			System.out.println("Selected Instances are already Shutdown");
		}
	}
	
	public void shutDownProcess()
	{
		String instanceName= null;
		String instanceState=null;
		HashMap<String,Integer> instanceNameAndNumber = null;
		instanceNameAndNumber = new HashMap<String, Integer>();
		boolean isInstancePresent= false;
		for(int i=0;elementId<=totalInstances;i++)
		{
			for (int j = 1; j <= numOfInstancesToProcess; j++) 
			{
				isInstancePresent = driver.findElements(By.id("name" + elementId)).size() >= 1;
				if(isInstancePresent)
				{
					instanceName = driver.findElement(By.id("name" + elementId)).getText();
					if(instanceName.contains("admin"))
					{
						elementId++;
						j--;
						continue;
					}
					instanceState = checkInstanceState(elementId);
					//if(!instanceState.equals("SHUTDOWN"))
					if(instanceState.equals("RUNNING"))
						instanceNameAndNumber.put(instanceName, elementId);
					if(numOfInstancesToProcess != instanceNameAndNumber.size())
						elementId++;
				}
			}
			
			if(isSelectedInstancesAreRunning(instanceNameAndNumber)&&!instanceNameAndNumber.isEmpty())
				startSuspendProcess(instanceNameAndNumber);
			waitForFiveSeconds();
				startShutdownProcess(instanceNameAndNumber);
			elementId++;
			instanceNameAndNumber.clear();
			//System.out.println("Waiting to normalize instances");
			if(elementId>totalInstances)
				System.out.println("Shutdown Process for RELEASE is completed...");
		}
	}
	public String checkInstanceState(int elementId2)
	{
		String instanceState = null;
		instanceState = driver.findElement(By.id("state" + elementId2)).getText();
		return instanceState;
	}
	public void startShutdownProcess(HashMap<String, Integer> instanceNameAndNumber)
	{
		selectInstances(instanceNameAndNumber);
		if(isSelectedInstancesAreSuspended(instanceNameAndNumber)&&!instanceNameAndNumber.isEmpty())
			shutDownInstances(instanceNameAndNumber);
		else if(isSelectedInstancesAreShutdown(instanceNameAndNumber))
		{
			System.out.println("Selected Instances are already shutdown");
			deSelectInstances(instanceNameAndNumber);
		}
		else
		{
			System.out.println("Selected Instances are not yet Suspened");
			deSelectInstances(instanceNameAndNumber);
		}
	}

	private void startDeployProcess() 
	{
		if(!isAnyInstanceRunning())
		{	
			System.out.println("Starting deploy process");
			navigateToDeployments();
			deploy = new Deployment(driver, prop);
			deploy.selectAndDeploy();
		}
		else
			System.out.println("All instances are not SHUTDOWN yet, Can NOT proceed with Deployment process");
	}
	
	public void startAllInstancesProcess()
	{
		String instanceName= null;
		String instanceState=null;
		HashMap<String,Integer> instanceNameAndNumber = null;
		instanceNameAndNumber = new HashMap<String, Integer>();
		boolean isInstancePresent= false;
		for(int i=0;elementId<=totalInstances;i++)
		{
			for (int j = 1; j <= numOfInstancesToProcess; j++) 
			{
				isInstancePresent = driver.findElements(By.id("name" + elementId)).size() >= 1;
				if(isInstancePresent)
				{
					instanceName = driver.findElement(By.id("name" + elementId)).getText();
					if(instanceName.contains("admin"))
					{
						elementId++;
						j--;
						continue;
					}
					instanceState = checkInstanceState(elementId);
					if(!instanceState.equals("RUNNING"))
						instanceNameAndNumber.put(instanceName, elementId);
					if(numOfInstancesToProcess != instanceNameAndNumber.size()&&j!=numOfInstancesToProcess)
						elementId++;
				}
			}
			selectInstances(instanceNameAndNumber);
			if(!isSelectedInstancesAreRunning(instanceNameAndNumber))
			{
				startInstances(instanceNameAndNumber);
			}
			else
			{
				System.out.println("Selected instances are already running");
				deSelectInstances(instanceNameAndNumber);
			}
			waitForTwoSeconds();
			elementId++;
			instanceNameAndNumber.clear();
			if(elementId>totalInstances)
				System.out.println("Start Instances Process for RELEASE is completed...");
		}
	}
		
	public void navigateToDeployments()
	{
		WebElement lnkDeployments =null;
		waitForTwoSeconds();
		boolean isLnkDeploymentsAvailable = driver.findElements(By.id("linkAppDeploymentsControlPage")).size()>=1;
		
		if(isLnkDeploymentsAvailable)
		{
			lnkDeployments = driver.findElement(By.id("linkAppDeploymentsControlPage"));
			lnkDeployments.click();
		}
	}
	
	public void selectInstances(HashMap<String, Integer> instanceNameAndNumber)
	{
		Set<String> keySet = instanceNameAndNumber.keySet();
		Iterator<String> itr = keySet.iterator();
		WebElement selectEle=null;
		String instanceName = null;
		while (itr.hasNext()) 
		{
			try
			{
				instanceName = itr.next().toString();
				selectEle = driver.findElement(By.cssSelector("input[title='Select " + instanceName + "']"));
				if(!selectEle.isSelected())
				{	
					selectEle.click();
					waitForSec();
				}
			}
			catch(StaleElementReferenceException e)
			{
				if(e.getMessage().contains("stale element reference"))
				{
					JavascriptExecutor jse = (JavascriptExecutor)driver;
					jse.executeScript("arguments[0].scrollIntoView()", selectEle); 
					if(!selectEle.isSelected())
					{	
						selectEle.click();
						waitForSec();
					}
				}
			}
			catch(WebDriverException e)
			{
				if(e.getMessage().contains("Element is not clickable at point"))
				{
					JavascriptExecutor jse = (JavascriptExecutor)driver;
					jse.executeScript("arguments[0].scrollIntoView()", selectEle); 
					if(!selectEle.isSelected())
					{	
						selectEle.click();
						waitForSec();
					}
				}
			}
		}
	}
	
	public void deSelectInstances(HashMap<String, Integer> instanceNameAndNumber)
	{
		Set<String> keySet = instanceNameAndNumber.keySet();
		Iterator<String> itr = keySet.iterator();
		WebElement selectEle=null;
		String instanceName = null;
		while (itr.hasNext()) 
		{
			try
			{
				instanceName = itr.next().toString();
				selectEle = driver.findElement(By.cssSelector("input[title='Select " + instanceName + "']"));
				if(selectEle.isSelected())
				{	
					selectEle.click();
					waitForSec();
				}
			}
			catch(StaleElementReferenceException e)
			{
				if(e.getMessage().contains("stale element reference"))
				{
					JavascriptExecutor jse = (JavascriptExecutor)driver;
					jse.executeScript("arguments[0].scrollIntoView()", selectEle); 
					if(selectEle.isSelected())
					{	
						selectEle.click();
						waitForSec();
					}
				}
			}
			catch(WebDriverException e)
			{
				if(e.getMessage().contains("Element is not clickable at point"))
				{
					JavascriptExecutor jse = (JavascriptExecutor)driver;
					jse.executeScript("arguments[0].scrollIntoView()", selectEle); 
					if(selectEle.isSelected())
					{	
						selectEle.click();
						waitForSec();
					}
				}
			}
		}
	}
	
	public void refreshPage()
	{
		waitForFiveSeconds();
		driver.navigate().refresh();
	}
	public void rollingRestartProcess()
	{
		String instanceName= null;
		HashMap<String,Integer> instanceNameAndNumber = null;
		instanceNameAndNumber = new HashMap<String, Integer>();
		boolean isInstancePresent= false;
		for(int i=0;elementId<=totalInstances;i++)
		{
			for (int j = 1; j <= numOfInstancesToProcess; j++) 
			{
				isInstancePresent = driver.findElements(By.id("name" + elementId)).size() >= 1;
				if(isInstancePresent)
				{
					instanceName = driver.findElement(By.id("name" + elementId)).getText();
					if(instanceName.contains("admin"))
					{
						elementId++;
						j--;
						continue;
					}
					instanceNameAndNumber.put(instanceName, elementId);
					if(numOfInstancesToProcess != instanceNameAndNumber.size())
						elementId++;
				}
			}
			
			if(isSelectedInstancesAreRunning(instanceNameAndNumber))
				startSuspendProcess(instanceNameAndNumber);
			waitForFiveSeconds();
				startShutdownProcess(instanceNameAndNumber);
			if(isSelectedInstancesAreShutdown(instanceNameAndNumber))
			{
				selectInstances(instanceNameAndNumber);
				startInstances(instanceNameAndNumber);
			}
			elementId++;
			instanceNameAndNumber.clear();
			//System.out.println("Waiting to normalize instances");
			//waitForTenSeconds();
			waitForFiveMinutes();
			if(elementId>totalInstances)
				System.out.println("Shutdown Process for RELEASE is completed...");
		}
	}
	public void startInstances(HashMap<String, Integer> instanceNameAndNumber) 
	{
		System.out.println("Starting Instances");
		
		List<WebElement> startButtons = new ArrayList<WebElement>(); 
		startButtons= driver.findElements(By.cssSelector("button[name='Start']"));
		Iterator<WebElement> eleItr = startButtons.iterator();
		while (eleItr.hasNext()) 
		{
			WebElement btn = (WebElement) eleItr.next();
			highLightElement(driver,btn);
			JavascriptExecutor jse = (JavascriptExecutor)driver;
			jse.executeScript("arguments[0].scrollIntoView()", btn); 
			try{
				btn.click();
				break;
			}
			catch(Exception e)
			{
				if(e.getMessage().contains("Element is not clickable at point")||e.getMessage().contains("stale element reference"))
				{
					System.out.println("Stale element exception... Element is not clickable at point");
					continue;
				}
			}
		}
		waitForTwoSeconds();
		selectServerLifeCycleAssistant();
		try {
			Set<String> keySet = instanceNameAndNumber.keySet();
			Iterator<String> itr = keySet.iterator();
			String instanceName = null;
			String instanceState = null;
			Integer instanceNumber = null;
			WebElement eleInstanceState = null;
			while (itr.hasNext()) 
			{
				instanceName = (String) itr.next();
				instanceNumber = instanceNameAndNumber.get(instanceName);
				while (true) 
				{
					eleInstanceState = driver.findElement(By.id("state" + instanceNumber));
					JavascriptExecutor jse = (JavascriptExecutor)driver;
					jse.executeScript("arguments[0].scrollIntoView()", eleInstanceState); 
					highLightElement(driver, eleInstanceState);
					//instanceNumber = instanceNameAndNumber.get(instanceName);
					instanceState = driver.findElement(By.id("state" + instanceNumber)).getText();
					if (instanceState.equals("RUNNING")) 
					{
						System.out.println("'"+instanceName + "' is started and running now");
						break;
					}
					else if(instanceState.equals("STARTING") || instanceState.equals("RESUMING")||driver.getPageSource().contains("TASK IN PROGRESS"))
						refreshPage();
					else if(instanceState.equalsIgnoreCase("FAILED_NOT_RESTARTABLE")||instanceState.equalsIgnoreCase("FORCE_SHUTTING_DOWN"))
					{
						failedToStartInstancesCount++;
						System.out.println("'"+instanceName + "' is in 'FAILED_NOT_RESTARTABLE' state, needs to check this manually");
						break;
					}
					else if(instanceState.equalsIgnoreCase("FORCE_SHUTTING_DOWN"))
					{
						failedToStartInstancesCount++;
						System.out.println("'"+instanceName + "' is in 'FORCE_SHUTTING_DOWN' state, needs to check this manually");
						break;
					}
				}
			}
		}
		catch(StaleElementReferenceException e)
		{
			System.out.println("In Start Instances, Stale Element Exception caught.");

			Set<String> keySet = instanceNameAndNumber.keySet();
			Iterator<String> itr = keySet.iterator();
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
					if (instanceState.equals("RUNNING")) 
					{
						System.out.println("'"+instanceName + "' is started and running now");
						break;
					}
					else if(instanceState.equals("STARTING") || instanceState.equals("RESUMING")||driver.getPageSource().contains("TASK IN PROGRESS"))
						refreshPage();
					else if(instanceState.equalsIgnoreCase("FAILED_NOT_RESTARTABLE")||instanceState.equalsIgnoreCase("FORCE_SHUTTING_DOWN"))
					{
						failedToStartInstancesCount++;
						System.out.println("'"+instanceName + "' is in 'FAILED_NOT_RESTARTABLE' state, needs to check this manually");
						break;
					}
					else if(instanceState.equalsIgnoreCase("FORCE_SHUTTING_DOWN"))
					{
						failedToStartInstancesCount++;
						System.out.println("'"+instanceName + "' is in 'FORCE_SHUTTING_DOWN' state, needs to check this manually");
						break;
					}
				}
			}
		}
		if(failedToStartInstancesCount>=1)
		{
			System.out.println("Total '"+failedToStartInstancesCount + "' instances are failed to Start, they needs to check manually");
		}
	}
	
	public void suspendInstances(HashMap<String, Integer> instanceNameAndNumber) 
	{
		int suspendedInstances=0;
		System.out.println("Suspending Instances");
		List<WebElement> suspendButtons = new ArrayList<WebElement>(); 
		suspendButtons= driver.findElements(By.cssSelector("button[name='Suspend']"));
		Iterator<WebElement> eleItr = suspendButtons.iterator();
		while (eleItr.hasNext()) 
		{
			WebElement btn = (WebElement) eleItr.next();
			highLightElement(driver,btn);
			try{
				btn.click();
			}
			catch(WebDriverException e)
			{
				if(e.getMessage().contains("Element is not clickable at point")||e.getMessage().contains("stale element reference"))
				{
					JavascriptExecutor jse = (JavascriptExecutor)driver;
					jse.executeScript("arguments[0].scrollIntoView()", btn); 
					highLightElement(driver, btn);
					btn.click();
					break;
				}
			}
		}
		
		waitForTwoSeconds();
		
		List<WebElement> lnkForceSuspend = new ArrayList<WebElement>(); 
		lnkForceSuspend= driver.findElements(By.linkText("Force Suspend Now"));
		Iterator<WebElement> eleLnkItr = lnkForceSuspend.iterator();
		while (eleLnkItr.hasNext()) 
		{
			WebElement btn = (WebElement) eleLnkItr.next();
			highLightElement(driver,btn);
			try{
				btn.click();
			}
			catch(WebDriverException e)
			{
				if(e.getMessage().contains("Element is not clickable at point"))
				{
					JavascriptExecutor jse = (JavascriptExecutor)driver;
					jse.executeScript("arguments[0].scrollIntoView()", btn); 
					highLightElement(driver, btn);
					btn.click();
					break;
				}
			}
		}
		selectServerLifeCycleAssistant();
		try {
			Set<String> keySet = instanceNameAndNumber.keySet();
			Iterator<String> itr = keySet.iterator();
			String instanceName = null;
			String instanceState = null;
			Integer instanceNumber = null;
			int refreshCount=0;
			WebElement eleInstanceState = null;
			while (itr.hasNext()) 
			{
				instanceName = (String) itr.next();
				instanceNumber = instanceNameAndNumber.get(instanceName);
				while(true)
				{
					eleInstanceState = driver.findElement(By.id("state" + instanceNumber));
					JavascriptExecutor jse = (JavascriptExecutor)driver;
					jse.executeScript("arguments[0].scrollIntoView()", eleInstanceState); 
					highLightElement(driver, eleInstanceState);
					instanceState = driver.findElement(By.id("state" + instanceNumber)).getText();
					if (instanceState.equals("ADMIN"))
					{
						System.out.println("'"+instanceName + "' got suspended");
						suspendedInstances++;
						break;
					}
					else if(instanceState.equals("FORCE_SUSPENDING")||driver.getPageSource().contains("TASK IN PROGRESS")||!instanceState.equals("ADMIN"))
						refreshPage();
				}
			}
			if(suspendedInstances==instanceNameAndNumber.size())
				System.out.println("Total '"+suspendedInstances+"' instances got suspended");
			else
				System.out.println("something is wrong and some instances are not yet suspended");
		}
		catch(StaleElementReferenceException e)
		{
			Set<String> keySet = instanceNameAndNumber.keySet();
			Iterator<String> itr = keySet.iterator();
			String instanceName = null;
			String instanceState = null;
			Integer instanceNumber = null;
			int refreshCount=0;
			while (itr.hasNext()) 
			{
				instanceName = (String) itr.next();
				instanceNumber = instanceNameAndNumber.get(instanceName);
				while(true)
				{
					instanceState = driver.findElement(By.id("state" + instanceNumber)).getText();
					if (instanceState.equals("ADMIN"))
					{
						System.out.println("'"+instanceName + "' got suspended");
						suspendedInstances++;
						break;
					}
					else if(instanceState.equals("FORCE_SUSPENDING")||driver.getPageSource().contains("TASK IN PROGRESS")||!instanceState.equals("ADMIN"))
						refreshPage();
				}
			}
			if(suspendedInstances==instanceNameAndNumber.size())
				System.out.println("Total '"+suspendedInstances+"' instances got suspended");
			else
				System.out.println("something is wrong and some instances are not yet suspended");
		}
	}
	
	public boolean isSelectedInstancesAreRunning(HashMap<String,Integer> instanceNameAndNumber)
	{
		Set<String> keySet = instanceNameAndNumber.keySet();
		Iterator<String> itr = keySet.iterator();
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
			
			if(instanceState.equals("ADMIN"))
			{
				System.out.println("'"+instanceName+"' is already Suspended." );
			}
			if(instanceState.equals("RUNNING"))
			{
				running++;
			}
		}
		if(running==mapSize)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public boolean isSelectedInstancesAreSuspended(HashMap<String,Integer> instanceNameAndNumber)
	{
		Set<String> keySet = instanceNameAndNumber.keySet();
		Iterator<String> itr = keySet.iterator();
		int suspended=0,mapSize=0;
		
		mapSize = instanceNameAndNumber.size();
		while (itr.hasNext())
		{
			String instanceName = null;
			String instanceState = null;
			Integer instanceNumber = null;

			instanceName = (String) itr.next();
			instanceNumber = instanceNameAndNumber.get(instanceName);
			instanceState = driver.findElement(By.id("state" + instanceNumber)).getText();
			if(instanceState.equals("ADMIN"))
			{
				suspended++;
			}
		}
		if(suspended==mapSize)
		{
			return true;
		}
		else
		{
			System.out.println("Selected instances are NOT suspended");
			return false;
		}
	}
	
	public boolean isAnyInstanceRunning()
	{
		String instanceName= null;
		HashMap<String,Integer> instanceNameAndNumber = null;
		instanceNameAndNumber = new HashMap<String, Integer>();
		String instanceState =null;
		boolean isInstancePresent= false;
		int eleId=1;
		for (int i = 0; eleId <= totalInstances; i++) 
		{
			isInstancePresent = driver.findElements(By.id("name" + eleId)).size() >= 1;
			if (isInstancePresent) 
			{
				instanceName = driver.findElement(By.id("name" + eleId)).getText();
				if (instanceName.contains("admin")) 
				{
					eleId++;
					i--;
					continue;
				}
				instanceState = driver.findElement(By.id("state" + eleId)).getText();
				System.out.println("Instance '" + instanceName + "' is in '" + instanceState + "' state");
				if(instanceState.equals("RUNNING"))
				{
					return true;
				}
				eleId++;
			}
		}
		return false;
	}
	
	/*public boolean isAllInstancesAreShutdown()
	{
		String instanceName= null;
		HashMap<String,Integer> instanceNameAndNumber = null;
		instanceNameAndNumber = new HashMap<String, Integer>();
		boolean isInstancePresent= false;
		int eleId=1;
		for (int i = 0; eleId <= totalInstances; i++) 
		{
			isInstancePresent = driver.findElements(By.id("name" + eleId)).size() >= 1;
			if (isInstancePresent) 
			{
				instanceName = driver.findElement(By.id("name" + eleId)).getText();
				if (instanceName.contains("admin")) 
				{
					eleId++;
					i--;
					continue;
				}
				instanceNameAndNumber.put(instanceName, eleId);
				eleId++;
			}
		}
		return isSelectedInstancesAreShutdown(instanceNameAndNumber);
	}*/
	
	public boolean isSelectedInstancesAreShutdown(HashMap<String,Integer> instanceNameAndNumber)
	{
		Set<String> keySet = instanceNameAndNumber.keySet();
		Iterator<String> itr = keySet.iterator();
		int shutdown=0,mapSize=0;
		
		mapSize = instanceNameAndNumber.size();
		while (itr.hasNext())
		{
			String instanceName = null;
			String instanceState = null;
			Integer instanceNumber = null;

			instanceName = (String) itr.next();
			instanceNumber = instanceNameAndNumber.get(instanceName);
			instanceState = driver.findElement(By.id("state" + instanceNumber)).getText();
			System.out.println("Instance '" + instanceName + "' is in '" + instanceState + "' state");
			if(instanceState.equals("SHUTDOWN"))
			{
				shutdown++;
			}
		}
		if(shutdown==mapSize)
		{
			return true;
		}
		else
		{
			System.out.println("Selected instances are NOT in SHUTDOWN state");
			return false;
		}
	}
	
	public void selectServerLifeCycleAssistant()
	{
		WebElement btnYes =driver.findElement(By.cssSelector("button[name='Yes']"));
		highLightElement(driver,btnYes);
		btnYes.click();
	}
	
	public void shutDownInstances(HashMap<String, Integer> instanceNameAndNumber) 
	{
		System.out.println("Shutting down Instances");
		
		List<WebElement> shutDownButtons = new ArrayList<WebElement>(); 
		shutDownButtons= driver.findElements(By.cssSelector("button[name='Shutdown']"));
		Iterator<WebElement> eleItr = shutDownButtons.iterator();
		while (eleItr.hasNext()) 
		{
			WebElement btn = (WebElement) eleItr.next();
			highLightElement(driver,btn);
			try{
				btn.click();
			}
			catch(WebDriverException e)
			{
				if(e.getMessage().contains("Element is not clickable at point"))
				{
					JavascriptExecutor jse = (JavascriptExecutor)driver;
					jse.executeScript("arguments[0].scrollIntoView()", btn); 
					highLightElement(driver, btn);
					btn.click();
					break;
				}
			}
		}
		
		waitForTwoSeconds();
		
		List<WebElement> lnkForceShutdown = new ArrayList<WebElement>(); 
		lnkForceShutdown= driver.findElements(By.linkText("Force Shutdown Now"));
		Iterator<WebElement> eleLnkItr = lnkForceShutdown.iterator();
		while (eleLnkItr.hasNext()) 
		{
			WebElement btn = (WebElement) eleLnkItr.next();
			highLightElement(driver,btn);
			try{
				btn.click();
			}
			catch(WebDriverException e)
			{
				if(e.getMessage().contains("Element is not clickable at point"))
				{
					JavascriptExecutor jse = (JavascriptExecutor)driver;
					jse.executeScript("arguments[0].scrollIntoView()", btn);
					highLightElement(driver, btn);
					btn.click();
					break;
				}
			}
		}
		
		selectServerLifeCycleAssistant();
		try {
			Set<String> keySet = instanceNameAndNumber.keySet();
			Iterator<String> itr = keySet.iterator();
			String instanceName = null;
			String instanceState = null;
			Integer instanceNumber = null;
			WebElement eleInstanceState = null;
			while (itr.hasNext()) 
			{
				instanceName = (String) itr.next();
				instanceNumber = instanceNameAndNumber.get(instanceName);
				
				while(true) 
				{
					eleInstanceState = driver.findElement(By.id("state" + instanceNumber));
					JavascriptExecutor jse = (JavascriptExecutor)driver;
					jse.executeScript("arguments[0].scrollIntoView()", eleInstanceState); 
					highLightElement(driver, eleInstanceState);
					instanceState = driver.findElement(By.id("state" + instanceNumber)).getText();
					if (instanceState.equals("SHUTDOWN")) 
					{
						System.out.println("'"+instanceName + "' got shutdown");
						break;
					}
					else if(instanceState.equals("FORCE_SHUTTING_DOWN")||driver.getPageSource().contains("TASK IN PROGRESS"))
						refreshPage();
				}
			}
		}
		catch(StaleElementReferenceException e)
		{
			Set<String> keySet = instanceNameAndNumber.keySet();
			Iterator<String> itr = keySet.iterator();
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
					if (instanceState.equals("SHUTDOWN")) 
					{
						System.out.println("'"+instanceName + "' got shutdown");
						break;
					}
					else if(instanceState.equals("FORCE_SHUTTING_DOWN")||driver.getPageSource().contains("TASK IN PROGRESS"))
						refreshPage();
				}
			}
		}
	}
	
	public void waitForSec()
	{
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
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
	
	public void waitForFiveSeconds()
	{
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void waitForFiveMinutes()
	{
		try {
			Thread.sleep(300000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void highLightElement(WebDriver driver, WebElement webElement)
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
	
	public void selectProcess()
	{
		process = prop.getProperty("process");
		if(process.equalsIgnoreCase("suspend"))
		{
			suspendProcess();
		}
		else if(process.equalsIgnoreCase("shutdown"))
		{
			shutDownProcess();
		}
		else if(process.equalsIgnoreCase("deployment"))
		{
			startDeployProcess();
		}
		else if(process.equalsIgnoreCase("start"))
		{
			startAllInstancesProcess();
		}
		else if(process.equalsIgnoreCase("rolling"))
		{
			rollingRestartProcess();
		}
	}
	/*public void main() throws FileNotFoundException
	{
		System.out.println("in main");
		WeblogicController wl = new WeblogicController();
		wl.setProperties();
		wl.setWebdriver();
		wl.loginToWebLogic();
	}*/
}