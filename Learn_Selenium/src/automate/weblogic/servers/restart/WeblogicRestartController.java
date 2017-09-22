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
import org.openqa.selenium.support.ui.Sleeper;

import com.gargoylesoftware.htmlunit.javascript.background.JavaScriptExecutor;

public class WeblogicRestartController 
{
	private Properties prop = new Properties();
	private WebDriver driver;
	private String environment;
	private String restartProcess = null;
	private int totalShutdownInstances=0;
	private int numOfInstancesToRestart = 0;
	private int loop = 0;
	private int totalInstances = 0;
	private int remainingInstances = 0;
	private int elementId = 1;
	
	public void setProperties() throws FileNotFoundException {
		InputStream inputPropFile = new FileInputStream("./resources/properties/weblogicConfigs.properties");
		try {
			prop.load(inputPropFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setWebdriver()
	{
		System.setProperty("webdriver.chrome.driver",prop.getProperty("webDriverPath"));
		driver = new ChromeDriver();
	}

	public void loginToWebLogic()
	{
		WebElement txtUser= null, txtPass=null;
		boolean isTxtUseravailable=false;
		driver.get(prop.getProperty("WebLogicBaseUrl"));
		setEnvironment();
		// askUser();
		isTxtUseravailable = driver.findElements(By.id("j_username")).size()>=1;
		if(isTxtUseravailable)
		{
			txtUser = driver.findElement(By.id("j_username"));
			txtPass = driver.findElement(By.id("j_password"));
			if(environment.equals("LOCAL"))
			{
				txtUser.sendKeys(prop.getProperty("LocalWLUserName"));
				txtPass.sendKeys(prop.getProperty("LocalWLPassword"));
			}
			if(environment.equals("HMG03")||environment.equals("HMG05"))
			{
				txtUser.sendKeys(prop.getProperty("HMGWLUserName"));
				txtPass.sendKeys(prop.getProperty("HMGWLPassowrd"));
			}
			else if(environment.equals("DOM01")||environment.equals("DOM02")||environment.equals("DOM03")||environment.equals("DOM04")||environment.equals("Services"))
			{
				txtUser.sendKeys(prop.getProperty("ProductionWLUserName"));
				txtPass.sendKeys(prop.getProperty("ProductionWLPassword"));
			}
			WebElement btnLogin = driver.findElement(By.className("formButton"));
			btnLogin.click();
			waitForTwoSeconds();
			String pageSource = driver.getPageSource();
			if (pageSource.contains("welcome"))
			{
				System.out.println("Successfully logged in to Weblogic Admin Console");
				navigateToServers();
				//startDeployProcess();
			}
			else
			{
				System.out.println("Something is wrong with Weblogic Admin Console");
			}
		}
		else
		{
			System.out.println("Something is wrong with Weblogic Admin Console");
		}
	}

	public void setEnvironment()
	{
		if (driver.getCurrentUrl().contains("localhost"))
			environment = "LOCAL";
		else if (driver.getCurrentUrl().contains("hmg03"))
			environment = "HMG03";
		else if (driver.getCurrentUrl().contains("hmg05"))
			environment = "HMG05";
		else if (driver.getCurrentUrl().contains(prop.getProperty("Dom01ConsoleBR")))
			environment = "DOM01";
		else if (driver.getCurrentUrl().contains(prop.getProperty("Dom02ConsoleBR")))
			environment = "DOM02";
		else if (driver.getCurrentUrl().contains(prop.getProperty("Dom03ConsoleBR")))
			environment = "DOM03";
		else if (driver.getCurrentUrl().contains(prop.getProperty("Dom04ConsoleBR")))
			environment = "DOM04";
		else if (driver.getCurrentUrl().contains(prop.getProperty("DomServicesBR")))
			environment = "Services";
	}

	public void navigateToServers()
	{
		WebElement lnkServers = driver.findElement(By.linkText("Servers"));
		highLightElement(lnkServers);
		lnkServers.click();
		waitForTwoSeconds();
		navigateToControl();
	}

	private void navigateToControl() 
	{
		WebElement tabControl = driver.findElement(By.cssSelector("a[title*='Control- Tab']"));
		waitForTenSeconds();
		highLightElement(tabControl);
		/*JavaScriptExecutor ex = (JavaScriptExecutor)driver;
		ExecuteScript("arguments[0].click();", tabControl);*/
		tabControl.click();
		askUser();
	}
	
	public void askUser()
	{
		// Get Input from config file.
		String inputFromUser = prop.getProperty("numberOfInstancesToProcessInLoop");
		
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
		
		//Starting the actual Process...
		
		//startProcess();
		startReleaseProcess();
	}

	public String getUserInputFromPanel()
	{
		String input = null;
		Image image = new ImageIcon(prop.getProperty("iconImage")).getImage();
		JOptionPane pane = new JOptionPane("");
		JPanel innerPanel = new JPanel();
		//JDialog dialog = pane.createDialog("Are we ready to start deploy process?");
		JDialog dialog = pane.createDialog("Are we ready to start deploy process?");
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
	
	public void startReleaseProcess()
	{
		String instanceName= null;
		
		HashMap<String,Integer> instanceNameAndNumber = null;
		
		restartProcess = prop.getProperty("restartProcess");
		
		instanceNameAndNumber = new HashMap<String, Integer>();
		
		if (environment.equals("HMG03"))
			totalInstances = Integer.parseInt(prop.getProperty("TotalHMG03Instances"));
		else if (environment.equals("HMG05"))
			totalInstances = Integer.parseInt(prop.getProperty("TotalHMG05Instances"));
		else if (environment.equals("LOCAL"))
			totalInstances = Integer.parseInt(prop.getProperty("TotalLocalInstances"));
		else if (environment.equals("DOM01"))
			totalInstances = Integer.parseInt(prop.getProperty("TotalDom01Instances"));
		else if (environment.equals("DOM02"))
			totalInstances = Integer.parseInt(prop.getProperty("TotalDom02Instances"));
		else if (environment.equals("DOM03"))
			totalInstances = Integer.parseInt(prop.getProperty("TotalDom03Instances"));
		else if (environment.equals("DOM04"))
			totalInstances = Integer.parseInt(prop.getProperty("TotalDom04Instances"));
		else if (environment.equals("Services"))
			totalInstances = Integer.parseInt(prop.getProperty("TotalServiceDomInstances"));
		
		boolean isInstancePresent= false;
		if (restartProcess.equals("release"))
		{
			for(int i=0;elementId<=totalInstances;i++)
			 {
				for (int j = 1; j <= numOfInstancesToRestart; j++) 
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
						if(numOfInstancesToRestart != instanceNameAndNumber.size())
							elementId++;
					}
				}
				startSuspendProcess(instanceNameAndNumber);
				waitForTwoSeconds();
				startShutdownProcess(instanceNameAndNumber);
				elementId++;
				instanceNameAndNumber.clear();
				//System.out.println("Waiting to normalize instances");
				//waitForTenSeconds();
				if(elementId>totalInstances)
					 System.out.println("Shutdown Process for RELEASE is completed...");
			 }
		}
	}
	public void startSuspendProcess(HashMap<String, Integer> instanceNameAndNumber)
	{
		selectInstances(instanceNameAndNumber);
		//waitForTenSeconds();
		if(isSelectedInstancesAreRunning(instanceNameAndNumber))
			suspendInstances(instanceNameAndNumber);
		else
			System.out.println("Selected Instances are not Running");
	}
	
	public void startShutdownProcess(HashMap<String, Integer> instanceNameAndNumber)
	{
		selectInstances(instanceNameAndNumber);
		if(isSelectedInstancesAreSuspended(instanceNameAndNumber))
			shutDownInstances(instanceNameAndNumber);
		else
			System.out.println("Selected Instances are not yet Suspened");
	}
	public void startProcess() 
	{
		restartProcess = prop.getProperty("restartProcess");
		String instanceName, instanceState,inputFromUser;
		//WebElement eleSelectInstance;
		boolean isInstancePresent= false;
		//HashMap<String,String> instancesWithState = null;
		//HashMap<WebElement, HashMap<String,String>> selectWithInstancesAndState = new HashMap<WebElement,HashMap<String,String>>();
		HashMap<String,Integer> instanceNameAndNumber = null;
		instanceNameAndNumber = new HashMap<String, Integer>();
		
		if (environment.equals("HMG03"))
			totalInstances = Integer.parseInt(prop.getProperty("TotalHMG03Instances"));
		else if (environment.equals("HMG05"))
			totalInstances = Integer.parseInt(prop.getProperty("TotalHMG05Instances"));
		else if (environment.equals("LOCAL"))
			totalInstances = Integer.parseInt(prop.getProperty("TotalLocalInstances"));
		else if (environment.equals("DOM01"))
			totalInstances = Integer.parseInt(prop.getProperty("TotalDom01Instances"));
		else if (environment.equals("DOM02"))
			totalInstances = Integer.parseInt(prop.getProperty("TotalDom02Instances"));
		else if (environment.equals("DOM03"))
			totalInstances = Integer.parseInt(prop.getProperty("TotalDom03Instances"));
		else if (environment.equals("DOM04"))
			totalInstances = Integer.parseInt(prop.getProperty("TotalDom04Instances"));
		else if (environment.equals("Services"))
			totalInstances = Integer.parseInt(prop.getProperty("TotalServiceDomInstances"));
		
		if (restartProcess.equals("rolling"))
		{
			for(int i=0;elementId<=totalInstances;i++)
			 {
				for (int j = 1; j <= numOfInstancesToRestart; j++) 
				{
					//instancesWithState = new HashMap<String, String>();
					isInstancePresent = driver.findElements(By.id("name" + elementId)).size() > 1;
					if(isInstancePresent)
					{
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
						//eleSelectInstance = driver.findElement(By.cssSelector("input[title='Select " + instanceName + "']"));
						//instancesWithState.put(instanceName, instanceState);
				}
				//	selectWithInstancesAndState.put(eleSelectInstance,instancesWithState);
				}
				selectInstances(instanceNameAndNumber);
				if(isSelectedInstancesAreRunning(instanceNameAndNumber))
				{
					suspendInstances(instanceNameAndNumber);
					selectInstances(instanceNameAndNumber);
					if(isSelectedInstancesAreSuspended(instanceNameAndNumber))
						shutDownInstances(instanceNameAndNumber);
					else
						System.out.println("Selected Instances are not yet suspened");
					selectInstances(instanceNameAndNumber);
					if(isSelectedInstancesAreShutdown(instanceNameAndNumber))
						startInstances(instanceNameAndNumber);
					else
						System.out.println("Selected Instances are not in shutdown state");
				}
				elementId++;
				instanceNameAndNumber.clear();
				if(elementId>=totalInstances)
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
					//instancesWithState = new HashMap<String, String>();
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
					//eleSelectInstance = driver.findElement(By.cssSelector("input[title='Select " + instanceName + "']"));
					//instancesWithState.put(instanceName, instanceState);
					//selectWithInstancesAndState.put(eleSelectInstance,instancesWithState);
				}
				selectInstances(instanceNameAndNumber);
				 if(isSelectedInstancesAreRunning(instanceNameAndNumber))
				 {
					suspendInstances(instanceNameAndNumber);
					selectInstances(instanceNameAndNumber);
					if(isSelectedInstancesAreSuspended(instanceNameAndNumber))
						shutDownInstances(instanceNameAndNumber);
				 }
				 elementId++;
				 instanceNameAndNumber.clear();
				 //System.out.println("Waiting to normalize instances");
				// waitForTenSeconds();
				 
				/* if(elementId>totalInstances)
					 System.out.println("Restart Process for RELEASE is completed...");*/
			 }
			startDeployProcess();
			/*while(true)
			{
				inputFromUser = getUserInputFromPanel();
				if(inputFromUser.equalsIgnoreCase("wait"))
				{
					waitForTenSeconds();
				}
				else if(inputFromUser.equalsIgnoreCase("ok"))
				{
					startDeployProcess();
					break;
				}
				else if(inputFromUser.equalsIgnoreCase(""))
					System.out.println("Please enter your input");
				else if(inputFromUser.isEmpty())
					System.out.println("Please enter your input");
				else
					System.out.println("Please enter valid input");
			}*/
		}
		else
			System.out.println("Defined process '" + restartProcess+ "' is not correct");
	}
	
	private void startDeployProcess() 
	{
		System.out.println("Starting deploy process");
		navigateToDeployments();
		selectAndDeploy();
		//activateChanges();
	}
	
	public void selectAndDeploy() 
	{
		System.out.println("In Select and Deploy");
		int totalDeployments = 0; 
		int complatedDeployments =0;
		
		if (environment.equals("HMG03"))
		{
			totalDeployments = Integer.parseInt(prop.getProperty("TotalHMG03Deployments"));
			boolean isSelectEstoreAvailabel = driver.findElements(By.cssSelector("input[title='Select Estore']")).size()>=1;
			
			if(isSelectEstoreAvailabel)
			{
				takeLockAndEdit();
				WebElement selectEstore = driver.findElement(By.cssSelector("input[title='Select Estore']"));
				selectEstore.click();
				updateDeployments();
				complatedDeployments = verifyDeploymentSuccessMsg();
				activateChanges();
				System.out.println("Completed deployments are - "+complatedDeployments);
			}
			else
				System.out.println("Selected Deployment is not available");
			
			boolean isSelectAtg_BccAvailabel = driver.findElements(By.cssSelector("input[title='Select atg_bcc']")).size()>=1;
			if(isSelectAtg_BccAvailabel)
			{
				takeLockAndEdit();
				WebElement selectBCC = driver.findElement(By.cssSelector("input[title='Select atg_bcc']"));
				selectBCC.click();
				updateDeployments();
				complatedDeployments = verifyDeploymentSuccessMsg();
				activateChanges();
				System.out.println("Completed deployments are - "+complatedDeployments);
			}
			else
				System.out.println("Selected Deployment is not available");
			
			if(complatedDeployments==totalDeployments)
			{
				System.out.println("All '"+totalDeployments+"' deployments are completed.");
				releaseLockConfiguration();
			}
			else
				System.out.println("Deployments are going on");
		}
		else if (environment.equals("HMG05"))
		{
			totalDeployments = Integer.parseInt(prop.getProperty("TotalHMG05Deployments"));
			boolean isSelectEstoreAvailabel = driver.findElements(By.cssSelector("input[title='Select Estore']")).size()>=1;
			
			if(isSelectEstoreAvailabel)
			{
				takeLockAndEdit();
				WebElement selectEstore = driver.findElement(By.cssSelector("input[title='Select Estore']"));
				selectEstore.click();
				updateDeployments();
				complatedDeployments = verifyDeploymentSuccessMsg();
				activateChanges();
				System.out.println("Completed deployments are - "+complatedDeployments);
			}
			else
				System.out.println("Selected Deployment is not available");
			
			boolean isSelectAtg_BccAvailabel = driver.findElements(By.cssSelector("input[title='Select EstoreCA']")).size()>=1;
			if(isSelectAtg_BccAvailabel)
			{
				takeLockAndEdit();
				WebElement selectBCC = driver.findElement(By.cssSelector("input[title='Select EstoreCA']"));
				selectBCC.click();
				updateDeployments();
				complatedDeployments = verifyDeploymentSuccessMsg();
				activateChanges();
				System.out.println("Completed deployments are - "+complatedDeployments);
			}
			else
				System.out.println("Selected Deployment is not available");
			
			if(complatedDeployments==totalDeployments)
			{
				System.out.println("All '"+totalDeployments+"' deployments are completed.");
				releaseLockConfiguration();
			}
			else
				System.out.println("Deployments are going on");
		}
		else if (environment.equals("LOCAL"))
		{
		}
		else if (environment.equals("DOM01"))
		{
			totalDeployments = Integer.parseInt(prop.getProperty("TotalDOM01Deployments"));
			boolean isSelectDeploy1Availabel = driver.findElements(By.cssSelector("input[title='Select Estore']")).size()>=1;
			
			if(isSelectDeploy1Availabel)
			{
				takeLockAndEdit();
				WebElement selectDeploy1 = driver.findElement(By.cssSelector("input[title='Select Estore']"));
				selectDeploy1.click();
				updateDeployments();
				complatedDeployments = verifyDeploymentSuccessMsg();
				activateChanges();
				System.out.println("Completed deployments are - "+complatedDeployments);
			}
			else
				System.out.println("Selected Deployment is not available");
			
			boolean isSelectDeploy2Availabel = driver.findElements(By.cssSelector("input[title='Select EstoreSLM01']")).size()>=1;
			if(isSelectDeploy2Availabel)
			{
				takeLockAndEdit();
				WebElement selectDeploy2 = driver.findElement(By.cssSelector("input[title='Select EstoreSLM01']"));
				selectDeploy2.click();
				updateDeployments();
				complatedDeployments = verifyDeploymentSuccessMsg();
				activateChanges();
				System.out.println("Completed deployments are - "+complatedDeployments);
			}
			else
				System.out.println("Selected Deployment is not available");
			
			boolean isSelectDeploy3Availabel = driver.findElements(By.cssSelector("input[title='Select EstoreWS']")).size()>=1;
			if(isSelectDeploy3Availabel)
			{
				takeLockAndEdit();
				WebElement selectDeploy3 = driver.findElement(By.cssSelector("input[title='Select EstoreWS']"));
				selectDeploy3.click();
				updateDeployments();
				complatedDeployments = verifyDeploymentSuccessMsg();
				activateChanges();
				System.out.println("Completed deployments are - "+complatedDeployments);
			}
			else
				System.out.println("Selected Deployment is not available");
			
			if(complatedDeployments==totalDeployments)
			{
				System.out.println("All '"+totalDeployments+"' deployments are completed.");
				releaseLockConfiguration();
			}
			else
				System.out.println("Deployments are going on");
		}
		else if (environment.equals("DOM02"))
		{
			totalDeployments = Integer.parseInt(prop.getProperty("TotalDOM02Deployments"));
			boolean isSelectDeploy1Availabel = driver.findElements(By.cssSelector("input[title='Select Estore']")).size()>=1;
			
			if(isSelectDeploy1Availabel)
			{
				takeLockAndEdit();
				WebElement selectDeploy1 = driver.findElement(By.cssSelector("input[title='Select Estore']"));
				selectDeploy1.click();
				updateDeployments();
				complatedDeployments = verifyDeploymentSuccessMsg();
				activateChanges();
				System.out.println("Completed deployments are - "+complatedDeployments);
			}
			else
				System.out.println("Selected Deployment is not available");
			
			boolean isSelectDeploy2Availabel = driver.findElements(By.cssSelector("input[title='Select EstoreSLM01']")).size()>=1;
			if(isSelectDeploy2Availabel)
			{
				takeLockAndEdit();
				WebElement selectDeploy2 = driver.findElement(By.cssSelector("input[title='Select EstoreSLM01']"));
				selectDeploy2.click();
				updateDeployments();
				complatedDeployments = verifyDeploymentSuccessMsg();
				activateChanges();
				System.out.println("Completed deployments are - "+complatedDeployments);
			}
			else
				System.out.println("Selected Deployment is not available");
			
			boolean isSelectDeploy3Availabel = driver.findElements(By.cssSelector("input[title='Select EstoreWS']")).size()>=1;
			if(isSelectDeploy3Availabel)
			{
				takeLockAndEdit();
				WebElement selectDeploy3 = driver.findElement(By.cssSelector("input[title='Select EstoreWS']"));
				selectDeploy3.click();
				updateDeployments();
				complatedDeployments = verifyDeploymentSuccessMsg();
				activateChanges();
				System.out.println("Completed deployments are - "+complatedDeployments);
			}
			else
				System.out.println("Selected Deployment is not available");
			
			if(complatedDeployments==totalDeployments)
			{
				System.out.println("All '"+totalDeployments+"' deployments are completed.");
				releaseLockConfiguration();
			}
			else
				System.out.println("Deployments are going on");
		}
		else if (environment.equals("DOM03"))
		{
			totalDeployments = Integer.parseInt(prop.getProperty("TotalDOM03Deployments"));
			boolean isSelectDeploy1Availabel = driver.findElements(By.cssSelector("input[title='Select Estore']")).size()>=1;
			
			if(isSelectDeploy1Availabel)
			{
				takeLockAndEdit();
				WebElement selectDeploy1 = driver.findElement(By.cssSelector("input[title='Select Estore']"));
				selectDeploy1.click();
				updateDeployments();
				complatedDeployments = verifyDeploymentSuccessMsg();
				activateChanges();
				System.out.println("Completed deployments are - "+complatedDeployments);
			}
			else
				System.out.println("Selected Deployment is not available");
			
			if(complatedDeployments==totalDeployments)
			{
				System.out.println("All '"+totalDeployments+"' deployments are completed.");
				releaseLockConfiguration();
			}
			else
				System.out.println("Deployments are going on");
		}
		else if (environment.equals("DOM04"))
		{
			totalDeployments = Integer.parseInt(prop.getProperty("TotalDOM01Deployments"));
			boolean isSelectDeploy1Availabel = driver.findElements(By.cssSelector("input[title='Select Estore']")).size()>=1;
			
			if(isSelectDeploy1Availabel)
			{
				takeLockAndEdit();
				WebElement selectDeploy1 = driver.findElement(By.cssSelector("input[title='Select Estore']"));
				selectDeploy1.click();
				updateDeployments();
				complatedDeployments = verifyDeploymentSuccessMsg();
				activateChanges();
				System.out.println("Completed deployments are - "+complatedDeployments);
			}
			else
				System.out.println("Selected Deployment is not available");
			
			boolean isSelectDeploy2Availabel = driver.findElements(By.cssSelector("input[title='Select Estore-nsps30']")).size()>=1;
			if(isSelectDeploy2Availabel)
			{
				takeLockAndEdit();
				WebElement selectDeploy2 = driver.findElement(By.cssSelector("input[title='Select Estore-nsps30']"));
				selectDeploy2.click();
				updateDeployments();
				complatedDeployments = verifyDeploymentSuccessMsg();
				activateChanges();
				System.out.println("Completed deployments are - "+complatedDeployments);
			}
			else
				System.out.println("Selected Deployment is not available");
			
			if(complatedDeployments==totalDeployments)
			{
				System.out.println("All '"+totalDeployments+"' deployments are completed.");
				releaseLockConfiguration();
			}
			else
				System.out.println("Deployments are going on");
		}
		else if (environment.equals("Services"))
		{
			totalDeployments = Integer.parseInt(prop.getProperty("TotalDOM02Deployments"));
			boolean isSelectDeploy1Availabel = driver.findElements(By.cssSelector("input[title='Select EstoreCA']")).size()>=1;
			
			if(isSelectDeploy1Availabel)
			{
				takeLockAndEdit();
				WebElement selectDeploy1 = driver.findElement(By.cssSelector("input[title='Select EstoreCA']"));
				selectDeploy1.click();
				updateDeployments();
				complatedDeployments = verifyDeploymentSuccessMsg();
				activateChanges();
				System.out.println("Completed deployments are - "+complatedDeployments);
			}
			else
				System.out.println("Selected Deployment is not available");
			
			boolean isSelectDeploy2Availabel = driver.findElements(By.cssSelector("input[title='Select EstoreWS02']")).size()>=1;
			if(isSelectDeploy2Availabel)
			{
				takeLockAndEdit();
				WebElement selectDeploy2 = driver.findElement(By.cssSelector("input[title='Select EstoreWS02']"));
				selectDeploy2.click();
				updateDeployments();
				complatedDeployments = verifyDeploymentSuccessMsg();
				activateChanges();
				System.out.println("Completed deployments are - "+complatedDeployments);
			}
			else
				System.out.println("Selected Deployment is not available");
			
			if(complatedDeployments==totalDeployments)
			{
				System.out.println("All '"+totalDeployments+"' deployments are completed.");
				releaseLockConfiguration();
			}
			else
				System.out.println("Deployments are going on");
		}
	}
	
	public void takeLockAndEdit()
	{
		boolean isBtnLockAndEditEnabled = driver.findElement(By.cssSelector("button[name='save']")).isEnabled();
		if(isBtnLockAndEditEnabled)
		{
			System.out.println("Lock & Edit button is Enabled");
			WebElement btnLockAndEdit = driver.findElement(By.cssSelector("button[name='save']"));
			highLightElement(btnLockAndEdit);
			btnLockAndEdit.click();
		}
		else
			System.out.println("Lock & Edit button is Disabled");
	}
	
	public void activateChanges()
	{
		boolean isBtnActivateChangesEnabled = driver.findElement(By.cssSelector("button[name='save']")).isEnabled();
		boolean isLblSuccessMsgAvaialbe=false;
		if(isBtnActivateChangesEnabled)
		{
			System.out.println("Activate changes button is Enabled");
			WebElement btnActivateChanges = driver.findElement(By.cssSelector("button[name='save']"));
			//System.out.println("Text on Activate changes button is '"+btnActivateChanges.getText()+"'");
			highLightElement(btnActivateChanges);
			btnActivateChanges.click();
			waitForTwoSeconds();
			isLblSuccessMsgAvaialbe = driver.findElements(By.cssSelector("span[class='message_SUCCESS']")).size()>=1;
			
			System.out.println("Is success message availabel - "+isLblSuccessMsgAvaialbe);
			if(isLblSuccessMsgAvaialbe)
			{
				WebElement lblSuccessMsg = driver.findElement(By.cssSelector("span[class='message_SUCCESS']"));
				System.out.println("lbl Msg after chanegs activated is - "+lblSuccessMsg.getText());
				if(lblSuccessMsg.getText().contains("All changes have been activated"))
				{
					System.out.println("Changes have been activated.");
					//releaseLockConfiguration();
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

	public void releaseLockConfiguration()
	{
		boolean isBtnReleaseLockEnabled =  driver.findElement(By.cssSelector("button[name='cancel']")).isEnabled();
		if(isBtnReleaseLockEnabled)
		{
			System.out.println("Release button is Enabled");
			WebElement btnReleaseLock = driver.findElement(By.cssSelector("button[name='cancel']"));
			highLightElement(btnReleaseLock);
			btnReleaseLock.click();
		}
		else
			System.out.println("Lock is already released, button is Disabled");
	}
	public void updateDeployments()
	{
		WebElement btnFinish;
		boolean isBtnUpddateAvailable = driver.findElements(By.cssSelector("button[name='Update']")).size()>=1;
		if(isBtnUpddateAvailable)
		{	WebElement btnUpdate;
			btnUpdate = driver.findElement(By.cssSelector("button[name='Update']"));
			System.out.println("Updating changes");
			btnUpdate.click();
			waitForTwoSeconds();
			btnFinish = driver.findElement(By.cssSelector("button[name='Finish']"));
			btnFinish.click();
		}
		else
			System.out.println("Update Button is not available");
	}
	int complatedDeployments =0;
	public int verifyDeploymentSuccessMsg()
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
	
	

	public void navigateToDeployments()
	{
		WebElement lnkDeployments =null;
		waitForTwoSeconds();
		boolean isLnkDeploymentsAvailable = driver.findElements(By.id("linkAppDeploymentsControlPage")).size()>=1;
		
		if(isLnkDeploymentsAvailable)
		{
			System.out.println("Going to Deployments page");
			lnkDeployments = driver.findElement(By.id("linkAppDeploymentsControlPage"));
			lnkDeployments.click();
		}
	}
	int selectCount =0;
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
				selectCount++;
				if(selectCount>=18)
				{
					JavascriptExecutor jse = (JavascriptExecutor)driver;
					jse.executeScript("window.scrollBy(0,250)", "");
				}
				else
				{
					//System.out.println("is element selected? - "+selectEle.isSelected());
					if(!selectEle.isSelected())
						selectEle.click();
				}
			}
			catch(StaleElementReferenceException e)
			{
				System.out.println("Caught Stale Element Exception... continuing with execution");
				instanceName = itr.next().toString();
				selectEle = driver.findElement(By.cssSelector("input[title='Select " + instanceName + "']"));
				selectEle.click();
			}
		}
	}
	public void waitForSec()
	{
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	public void refreshPage()
	{
		waitForTenSeconds();
		driver.navigate().refresh();
	}
	
	public void startInstances(HashMap<String, Integer> instanceNameAndNumber) 
	{
		System.out.println("Starting Instances");
		WebElement btnStart = driver.findElement(By.cssSelector("button[name='Start']"));
		highLightElement(btnStart);
		btnStart.click();
		if(environment.equals("HMG03")||environment.equals("HMG05")||environment.equals("DOM01")||environment.equals("DOM02")||environment.equals("DOM03")||environment.equals("DOM04") ||environment.equals("Services"))
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
		int suspendedInstances=0;
		System.out.println("Suspending Instances");
		//WebElement btnSuspend = null;
		List<WebElement> suspendButtons = new ArrayList<WebElement>(); 
		suspendButtons= driver.findElements(By.cssSelector("button[name='Suspend']"));
		//waitForTwoSeconds();
		Iterator<WebElement> eleItr = suspendButtons.iterator();
		while (eleItr.hasNext()) 
		{
			WebElement btn = (WebElement) eleItr.next();
			highLightElement(btn);
			try{
				btn.click();
			}
			catch(WebDriverException e)
			{
				if(e.getMessage().contains("Element is not clickable at point"))
					continue;
			}
		}
		
		waitForTwoSeconds();
		//btnSuspend.click();
		
		List<WebElement> lnkForceSuspend = new ArrayList<WebElement>(); 
		lnkForceSuspend= driver.findElements(By.linkText("Force Suspend Now"));
		//waitForTwoSeconds();
		Iterator<WebElement> eleLnkItr = lnkForceSuspend.iterator();
		while (eleLnkItr.hasNext()) 
		{
			WebElement btn = (WebElement) eleLnkItr.next();
			highLightElement(btn);
			try{
				btn.click();
			}
			catch(WebDriverException e)
			{
				if(e.getMessage().contains("Element is not clickable at point"))
					continue;
			}
		}
		
		if(environment.equals("HMG03")||environment.equals("HMG05")||environment.equals("DOM01")||environment.equals("DOM02")||environment.equals("DOM03")||environment.equals("DOM04") ||environment.equals("Services"))
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
				while(true)
				{
					refreshPage();
					instanceState = driver.findElement(By.id("state" + instanceNumber)).getText();
					//System.out.println("Instance is '"+instanceName+"' and its state is '"+instanceState+"'");
					if (instanceState.equals("ADMIN"))
					{
						System.out.println("'"+instanceName + "' got suspended");
						suspendedInstances++;
						break;
					}
				}
				if(suspendedInstances==instanceNameAndNumber.size())
					System.out.println("Total '"+suspendedInstances+"' got suspended");
				else
					System.out.println("something is wrong and some instances are not yet suspended");
			}
			
			//stopAutoRefresh();
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
			//stopAutoRefresh();
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
			
			if(instanceState.equals("ADMIN"))
			{
				System.out.println("'"+instanceName+"' is already Suspended." );
				//instanceNameAndNumber.remove(instanceName);
				//continue;
			}
			if(instanceState.equals("RUNNING"))
			{
				running++;
			}
		}
		if(running==mapSize)
		{
			System.out.println("Total Running instances - '"+running+"' are equal to map's size '"+mapSize+"'");
			return true;
		}
		else
		{
			System.out.println("Total Running instances - '"+running+"' and map's size is '"+mapSize+"'");
			return false;
		}
	}
	
	public boolean isSelectedInstancesAreSuspended(HashMap<String,Integer> instanceNameAndNumber)
	{
		Set keySet = instanceNameAndNumber.keySet();
		Iterator itr = keySet.iterator();
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
			System.out.println("Instance '" + instanceName + "' in '" + instanceState + "' state");
			if(instanceState.equals("ADMIN"))
			{
				suspended++;
			}
		}
		if(suspended==mapSize)
		{
			System.out.println("All selected instances are suspended "+suspended+" "+mapSize);
			return true;
		}
		else
		{
			System.out.println("Selected instances are NOT suspended "+suspended+" "+mapSize);
			return false;
		}
	}
	
	public boolean isSelectedInstancesAreShutdown(HashMap<String,Integer> instanceNameAndNumber)
	{
		Set keySet = instanceNameAndNumber.keySet();
		Iterator itr = keySet.iterator();
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
			System.out.println("Instance '" + instanceName + "' in '" + instanceState + "' state");
			if(instanceState.equals("SHUTDOWN"))
			{
				shutdown++;
			}
		}
		if(shutdown==mapSize)
		{
			System.out.println("All selected instances are shutdown "+shutdown+" "+mapSize);
			return true;
		}
		else
		{
			System.out.println("Selected instances are NOT shutdown "+shutdown+" "+mapSize);
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
		
		List<WebElement> shutDownButtons = new ArrayList<WebElement>(); 
		shutDownButtons= driver.findElements(By.cssSelector("button[name='Shutdown']"));
		//waitForTwoSeconds();
		Iterator<WebElement> eleItr = shutDownButtons.iterator();
		while (eleItr.hasNext()) 
		{
			WebElement btn = (WebElement) eleItr.next();
			highLightElement(btn);
			try{
				btn.click();
			}
			catch(WebDriverException e)
			{
				if(e.getMessage().contains("Element is not clickable at point"))
					continue;
			}
		}
		
		waitForTwoSeconds();
		
		List<WebElement> lnkForceShutdown = new ArrayList<WebElement>(); 
		lnkForceShutdown= driver.findElements(By.linkText("Force Shutdown Now"));
		//waitForTwoSeconds();
		Iterator<WebElement> eleLnkItr = lnkForceShutdown.iterator();
		while (eleLnkItr.hasNext()) 
		{
			WebElement btn = (WebElement) eleLnkItr.next();
			highLightElement(btn);
			try{
				btn.click();
			}
			catch(WebDriverException e)
			{
				if(e.getMessage().contains("Element is not clickable at point"))
					continue;
			}
		}
		
		if(environment.equals("HMG03")||environment.equals("HMG05")||environment.equals("DOM01")||environment.equals("DOM02")||environment.equals("DOM03")||environment.equals("DOM04") ||environment.equals("Services"))
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
				
				while(true) 
				{
					refreshPage();
					instanceState = driver.findElement(By.id("state" + instanceNumber)).getText();
					//System.out.println("Instance is '" + instanceName + "' and its state is '" + instanceState + "'");
					if (instanceState.equals("SHUTDOWN")) 
					{
						System.out.println("'"+instanceName + "' got shutdown");
						totalShutdownInstances++;
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
					//instanceNumber = instanceNameAndNumber.get(instanceName);
					refreshPage();
					instanceState = driver.findElement(By.id("state" + instanceNumber)).getText();
					//System.out.println("Instance is '" + instanceName + "' and its state is '" + instanceState + "'");
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
	
	public void selectProcess()
	{
		
	}
	public static void main(String[] args) throws FileNotFoundException
	{
		WeblogicRestartController wl = new WeblogicRestartController();
		wl.setProperties();
		wl.setWebdriver();
		wl.loginToWebLogic();
	}
}