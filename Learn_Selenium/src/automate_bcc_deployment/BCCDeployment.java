package automate_bcc_deployment;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Select;

import com.gargoylesoftware.htmlunit.javascript.host.media.rtc.webkitRTCPeerConnection;
import com.sun.org.apache.regexp.internal.recompile;

public class BCCDeployment {

	Properties prop = new Properties();
	WebDriver driver;

	public void startDeploymentProcess() throws FileNotFoundException
	{
		initializeProperties();
	}
	
	public void initializeProperties() throws FileNotFoundException {
		InputStream input = null;
		input = new FileInputStream("./resources/properties/config.properties");
		try {
			prop.load(input);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		setting();
	}

	public void setting() {
		System.setProperty(prop.getProperty("baseDriver"),prop.getProperty("webDriverPath"));
		driver = new ChromeDriver();
		openBCCUrl();
	}

	public void highLightElement(WebDriver driver, WebElement webElement) {
		String DELAY = "delay";
		JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeScript("arguments[0].setAttribute('style', arguments[1]);",
				webElement, "color: green; border: 4px solid green;");
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

	public void openBCCUrl()
	{
			WebElement chkbtnLogin;
			System.out.println("In openBCCUrl");
			driver.get(prop.getProperty("baseUrl"));
			boolean isBccOK = driver.findElements(By.tagName("input")).size()>1;
			System.out.println(isBccOK);
			if(isBccOK)
			{
				//chkbtnLogin = driver.findElement(By.name(prop.getProperty("btnLogin")));
				loginToBCC();
			}
			else
			{
				System.out.println("BCC is down or something is wrong with BCC, please check it... Closing Program");
				System.exit(0);
			}
	}
	public void loginToBCC()
	{
		System.out.println("on Login Page");
		WebElement txtuser, txtPass, btnLogin;
		String chkHomeUrl1,uName,pass;
		
		txtuser = driver.findElement(By.id("loginName"));
		uName = prop.getProperty("userName");
		txtuser.sendKeys(uName);

		txtPass = driver.findElement(By.id("loginPassword"));
		pass = prop.getProperty("password");
		txtPass.sendKeys(pass);

		btnLogin = driver.findElement(By.name(prop.getProperty("btnLogin")));
		btnLogin.click();

		chkHomeUrl1 = driver.getCurrentUrl();

		if (chkHomeUrl1.startsWith("http://hmg02-atg11-app.ns2online.com.br:8180/atg/bcc/home")) 
			System.out.println("Logged in to BCC");
		else
			System.out.println("Not able to LogIn to BCC, Somthing is worng...");
		
		try {
			Thread.sleep(3000);
			searchCA_Console();
		} catch (InterruptedException interuptE) {
			interuptE.printStackTrace();
		}
	}
	
	public void searchCA_Console()
	{
		WebElement lnkCAConsole;
		System.out.println("Searching CA Console");
		lnkCAConsole = driver.findElement(By.linkText("CA Console"));
		navigateTo(driver, lnkCAConsole);
/*		System.out.println("CA Console found, highlighting it");

		try {
			((JavascriptExecutor) driver).executeScript(
					"arguments[0].scrollIntoView(true);", lnkCAConsole);
			System.out.println("JavaScript Executed...");
		} catch (Exception e) {

		}

		highLightElement(driver, lnkCAConsole);

		lnkCAConsole.click();*/
		checkProdOverView();
	}
	
	public void checkProdOverView()
	{
		WebElement lnkProdOverview;
		String prodOverviewStr = prop.getProperty("prodOverviewXpath");
		boolean prodOverview = driver.findElement(By.xpath(prodOverviewStr))
				.getAttribute("href")
				.contains(prop.getProperty("hmg02ProdTar"));
		// boolean stageOverview =
		// driver.findElement(By.xpath(prodOverviewStr)).getAttribute("href").contains(prop.getProperty("ProductionProdTar"));
		if (prodOverview) {
			System.out.println("Prod link found");
			lnkProdOverview = driver.findElement(By.linkText("Production"));
			highLightElement(driver, lnkProdOverview);
			lnkProdOverview.click();
			isDeploymentResumed();
		} else
			System.out.println("Prod link NOT found");
	}
	
	public void isDeploymentResumed()
	{
		WebElement btnResumeDeployment;
		boolean isResumed = driver.findElements(By.partialLinkText("Resume ")).size()<1;
		System.out.println(isResumed);
		if(isResumed)
		{
			System.out.println("Deployments Already Resumed");
			navigateToDoTab();
		}
		else
		{
			btnResumeDeployment = driver.findElement(By.partialLinkText("Resume "));
			System.out.println("Resuming Deploy");
			btnResumeDeployment.click();
			WebElement frmSiteAction = driver.findElement(By.id("resumeSiteActionIframe"));
			driver.switchTo().frame(frmSiteAction);
			WebElement btnOK = driver.findElement(By.partialLinkText("OK"));
			btnOK.click();
			System.out.println("Deployment Resumed.");
		}
	}
	
	public void navigateToDoTab()
	{
		WebElement lnkToDoTab;
		lnkToDoTab = driver.findElement(By.linkText("To Do"));
		lnkToDoTab.click();
		checkAbacosProjectInToDo();
	}
	
	public void checkAbacosProjectInToDo() 
	{
		String strProjectAbacosName = prop.getProperty("strProjectAbacosName");
		boolean isProjectAvailable = checkProjectsInToDo(driver,strProjectAbacosName);

		if (isProjectAvailable)
		{
			System.out.println("Ready With Project ");
			WebElement foundProject = driver.findElement(By.partialLinkText(strProjectAbacosName));
			highLightElement(driver, foundProject);
			System.out.println("Project " + strProjectAbacosName+ " available in ToDo");
		}
		else
		{
			System.out.println("Going to Search Project...");
			WebElement lnkHome = driver.findElement(By.linkText("Home"));
			lnkHome.click();
			WebElement lnkCAProjects = driver.findElement(By.linkText("CA Projects"));
			//searchProject();
			navigateTo(driver, lnkCAProjects);
			searchProject(driver,strProjectAbacosName);
		}
	}
	
	public void searchProject(WebDriver driver2, String searchProjectName) 
	{	
		WebElement searchBox = driver2.findElement(By.name("/atg/epub/servlet/ProcessSearchFormHandler.textInput"));
		searchBox.sendKeys(searchProjectName);
		WebElement lnkGo = driver2.findElement(By.linkText("Go"));
		lnkGo.click();
		//PubPortlets/html/ProjectsPortlet/images/icon_process.gif
		boolean isProjectNotFound = driver2.findElements(By.tagName("img")).size()<1;
		if(isProjectNotFound)
		{
			System.out.println("Project NOT Found...");
		}
		else
		{
			System.out.println("Project Found...");
			WebElement projectFound = driver2.findElement(By.tagName("img"));
			WebElement currentTask = driver2.findElement(By.className("current"));
			highLightElement(driver2, currentTask);
			String strCurrentTask = currentTask.getText();
			System.out.println("Current Task is = "+strCurrentTask);
			projectFound.click();
			//WebElement  optSelectAction = driver2.findElement(By.id("actionOption11"));
			//By.cssSelector("[id$=default-create-firstname]")
			WebElement  optSelectAction = driver2.findElement(By.cssSelector("[id^=actionOption]"));
			Select drpActions = new Select(optSelectAction);
			selectProjectAction(driver2,drpActions,strCurrentTask);
		}
	}
	
	public void selectProjectAction(WebDriver driver2, Select drpActions, String strCurrentTask)
	{
		WebElement btnGo = driver2.findElement(By.partialLinkText("Go"));
		if(strCurrentTask.equals("Author"))
		{
			System.out.println("Selecting ready for review");
			drpActions.selectByVisibleText("Ready for Review");
			btnGo.click();
			WebElement frmWorkFlow = driver2.findElement(By.id("workflowIframe"));
			switchToIFrame(driver2,frmWorkFlow);
		}
		else if(strCurrentTask.equals("Content Review"))
		{
			System.out.println("Approving Content");
			drpActions.selectByVisibleText("Approve Content");
			btnGo.click();
			WebElement frmWorkFlow = driver2.findElement(By.id("workflowIframe"));
			switchToIFrame(driver2,frmWorkFlow);
		}
		else if(strCurrentTask.equals("Approve for Deployment"))
		{
			System.out.println("Adding to Todo tab");
			drpActions.selectByVisibleText("Approve for Production Deployment");
			btnGo.click();
			WebElement frmWorkFlow = driver2.findElement(By.id("workflowIframe"));
			switchToIFrame(driver2, frmWorkFlow);
		}
	}
	
	public void switchToIFrame(WebDriver driver2, WebElement frmID)
	{
		driver2.switchTo().frame(frmID);
		WebElement btnOK = driver2.findElement(By.id("okActionButton"));
		btnOK.click();
	}
	
	
	public void navigateTo(WebDriver driver2, WebElement navElement)
	{
		System.out.println(navElement.toString() + " found, highlighting it");

		try {
			((JavascriptExecutor) driver).executeScript(
					"arguments[0].scrollIntoView(true);", navElement);
			System.out.println("JavaScript Executed...");
		} catch (Exception e) {
		}

		highLightElement(driver, navElement);
		navElement.click();
	}
	
	public boolean checkProjectsInToDo(WebDriver driver2,
			String strProjectAbacosName) {
		boolean chkProject;
		//chkProject = driver2.findElement(By.partialLinkText(strProjectAbacosName));
		chkProject = driver2.findElements(By.partialLinkText(strProjectAbacosName)).size()<1;
		if(chkProject)
			return false;
		else
			return true;
	}

	public static void main(String[] args) throws FileNotFoundException {
		BCCDeployment deploy = new BCCDeployment();
		deploy.startDeploymentProcess();
		/*deploy.setting();
		deploy.openBCCUrl();
		deploy.loginToBCC();
		deploy.navigateCA_Console();
		deploy.checkProdOverView();
		deploy.isDeploymentResumed();
		deploy.navigateToDoTab();
		deploy.checkAbacosProjectInToDo();*/
	}
}
