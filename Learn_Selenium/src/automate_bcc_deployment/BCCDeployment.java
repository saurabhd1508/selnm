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

	public void startDeploymentProcess() throws FileNotFoundException {
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
		System.out.println("In openBCCUrl");
		driver.get(prop.getProperty("baseUrl"));
		boolean isBccOK = driver.findElements(By.tagName("input")).size() > 1;
		System.out.println(isBccOK+" BCC is working");
		if (isBccOK) {
			loginToBCC();
		} else {
			System.out.println("BCC is down or something is wrong with BCC, please check it... Closing Program");
			System.exit(0);
		}
	}

	public void loginToBCC() {
		System.out.println("on Login Page");
		WebElement txtuser, txtPass, btnLogin;
		String chkHomeUrl1, uName, pass;

		txtuser = driver.findElement(By.id("loginName"));
		uName = prop.getProperty("userName");
		txtuser.sendKeys(uName);

		txtPass = driver.findElement(By.id("loginPassword"));
		pass = prop.getProperty("password");
		txtPass.sendKeys(pass);

		btnLogin = driver.findElement(By.name(prop.getProperty("btnLogin")));
		btnLogin.click();

		chkHomeUrl1 = driver.getCurrentUrl();

		if (chkHomeUrl1.startsWith(prop.getProperty("HMG02_BCCHome")))
			System.out.println("Logged in to BCC");
		else
			System.out.println("Not able to LogIn to BCC, Somthing is worng...");

		try {
			Thread.sleep(3000);
			navigateToCA_Console();
		} catch (InterruptedException interuptE) {
			interuptE.printStackTrace();
		}
	}

	public void navigateToCA_Console() {
		WebElement lnkCAConsole;
		System.out.println("Searching CA Console");
		lnkCAConsole = driver.findElement(By.linkText("CA Console"));
		navigateTo(driver, lnkCAConsole);
		checkProdOverView();
	}

	public void checkProdOverView() {
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

	public void isDeploymentResumed() {
		WebElement btnResumeDeployment;
		boolean isResumed = driver.findElements(By.partialLinkText("Resume ")).size() < 1;
		System.out.println(isResumed);
		if (isResumed) {
			System.out.println("Deployments Already Resumed");
			navigateToDoTab();
		} else {
			btnResumeDeployment = driver.findElement(By
					.partialLinkText("Resume "));
			System.out.println("Resuming Deploy");
			btnResumeDeployment.click();
			WebElement frmSiteAction = driver.findElement(By
					.id("resumeSiteActionIframe"));
			driver.switchTo().frame(frmSiteAction);
			WebElement btnOK = driver.findElement(By.partialLinkText("OK"));
			btnOK.click();
			System.out.println("Deployment Resumed.");
		}
	}

	boolean isAbacosInToDo = false;
	boolean isOldPromoInToDo = false;

	public void navigateToDoTab() {
		WebElement lnkToDoTab;
		lnkToDoTab = driver.findElement(By.linkText("To Do"));
		lnkToDoTab.click();
		String strProjectAbacosName = prop.getProperty("strProjectAbacosName");
		String strOldPromoProject = "Old Promotions Disabler";
		isAbacosInToDo = checkProjectsInToDo(driver, strProjectAbacosName);
		isOldPromoInToDo = checkProjectsInToDo(driver, strOldPromoProject);

		if (isAbacosInToDo) {
			WebElement foundAbacosProject = driver.findElement(By
					.partialLinkText(strProjectAbacosName));
			highLightElement(driver, foundAbacosProject);
			System.out.println("Project " + strProjectAbacosName
					+ " available in ToDo");
		}
		if (isOldPromoInToDo) {
			WebElement foundOldPromoProject = driver.findElement(By
					.partialLinkText(strOldPromoProject));
			highLightElement(driver, foundOldPromoProject);
			System.out.println("Project " + strOldPromoProject
					+ " available in ToDo");
		} else if (!isAbacosInToDo && isOldPromoInToDo) {
			System.out.println("Going to Search Project  "
					+ strProjectAbacosName);
			navigateToHome();
			WebElement lnkCAProjects = driver.findElement(By
					.linkText("CA Projects"));
			// searchProject();
			navigateTo(driver, lnkCAProjects);
			searchProject(driver, strProjectAbacosName);
		} else if (isAbacosInToDo && !isOldPromoInToDo) {
			System.out
					.println("Going to Search Project  " + strOldPromoProject);
			navigateToHome();
			WebElement lnkCAProjects = driver.findElement(By
					.linkText("CA Projects"));
			// searchProject();
			navigateTo(driver, lnkCAProjects);
			searchProject(driver, strOldPromoProject);
		} else {
			System.out.println("Going to Search Projects  "
					+ strProjectAbacosName + " and " + strOldPromoProject);
			navigateToHome();
			WebElement lnkCAProjects = driver.findElement(By
					.linkText("CA Projects"));
			// searchProject();
			navigateTo(driver, lnkCAProjects);
			searchProject(driver, strProjectAbacosName, strOldPromoProject);
		}
	}

	public void searchProject(WebDriver driver2, String strProjectAbacosName,
			String strOldPromoProject) {
		System.out.println("Ready to Search Projects  " + strProjectAbacosName
				+ " and " + strOldPromoProject);
		searchProject(driver, strProjectAbacosName);
		
		navigateToAvailableProjects(driver);
		
		searchProject(driver, strOldPromoProject);
		navigateToHome();
		navigateToCA_Console();

	}
	
	public void navigateToAvailableProjects(WebDriver driver2)
	{
		//� Available projects
		WebElement availableProjects = driver2.findElement(By.partialLinkText("Available projects"));
		navigateTo(driver2,availableProjects);
	}
	public void navigateToHome() {
		WebElement lnkHome = driver.findElement(By.linkText("Home"));
		lnkHome.click();
	}

	public void searchProject(WebDriver driver2, String searchProjectName) 
	{
		WebElement searchBox = driver2.findElement(By
				.name("/atg/epub/servlet/ProcessSearchFormHandler.textInput"));
		searchBox.clear();
		searchBox.sendKeys(searchProjectName);
		WebElement lnkGo = driver2.findElement(By.linkText("Go"));
		lnkGo.click();
		// PubPortlets/html/ProjectsPortlet/images/icon_process.gif
		boolean isProjectNotFound = driver2.findElements(By.tagName("img")).size() < 1;
		//boolean isProjectNotFound = driver2.findElements(By.cssSelector("input[class='centerAligned error']")).size() < 1;
		
		if (isProjectNotFound) {
			System.out.println("Project NOT Found...");
		} else {
			System.out.println("Project Found...");
			WebElement projectFound = driver2.findElement(By.tagName("img"));
			boolean isCurrentTaskAvailable = driver2.findElements(
					By.className("current")).size() < 1;
			if (!isCurrentTaskAvailable) {
				WebElement currentTask = driver2.findElement(By
						.className("current"));
				highLightElement(driver2, currentTask);
				String strCurrentTask = currentTask.getText();
				System.out.println("Current Task is = " + strCurrentTask);
				projectFound.click();
				selectProjectAction(driver2, strCurrentTask);
			}
		}
	}
	
	public void selectProjectAction(WebDriver driver2, String strCurrentTask) 
	{
		WebElement btnGo = driver2.findElement(By.partialLinkText("Go"));
		
		//ProjectActions actions = new ProjectActions(driver2,drpActions,strCurrentTask,btnGo);
		
		if (strCurrentTask.equals("Author")) {
			//actions.reviewProject(driver2, drpActions, btnGo);
			//actions.approveContent(driver2, drpActions, btnGo);
			//actions.approveForStagingDeployment(driver2, drpActions, btnGo);
			reviewProject(driver2);
		} else if (strCurrentTask.equals("Content Review")) {
			//actions.approveContent(driver2, drpActions, btnGo);
		} else if (strCurrentTask.equals("Approve for Deployment")) {
			//actions.approveForStagingDeployment(driver2, drpActions, btnGo);
			//approveForStagingDeployment(driver2);
			approveForProductionDeployment(driver2);
		}
	}

	// Ready for Review
			public void reviewProject(WebDriver driver2) {
				System.out.println("Selecting ready for review");
				WebElement optSelectAction = driver2.findElement(By.cssSelector("[id^=actionOption]"));
				Select drpActions = new Select(optSelectAction);
				drpActions.selectByVisibleText("Ready for Review");
				WebElement btnGo = driver2.findElement(By.partialLinkText("Go"));
				btnGo.click();
				WebElement frmWorkFlow = driver2.findElement(By.id("workflowIframe"));
				switchToIFrame(driver2, frmWorkFlow);
				//approveContent(driver2, drpActions, btnGo);
				approveContent(driver2);
			}

			// Approve Content
			//public void approveContent(WebDriver driver2, Select drpActions,WebElement btnGo)
			public void approveContent(WebDriver driver2)
			{
				System.out.println("Approving Content");
				WebElement optSelectAction = driver2.findElement(By.cssSelector("[id^=actionOption]"));
				Select drpActions = new Select(optSelectAction);
				drpActions.selectByVisibleText("Approve Content");
				WebElement btnGo = driver2.findElement(By.partialLinkText("Go"));
				btnGo.click();
				WebElement frmWorkFlow = driver2.findElement(By.id("workflowIframe"));
				switchToIFrame(driver2, frmWorkFlow);
				String currentUrl = driver2.getCurrentUrl();
				if(currentUrl.contains("hmg"))
				{
					approveForProductionDeployment(driver2);
				}
				else
					approveForStagingDeployment(driver2);
			}

			// Reject
			public void reject(WebDriver driver2) {
				System.out.println("Taking Back to Author");
				WebElement optSelectAction = driver2.findElement(By.cssSelector("[id^=actionOption]"));
				Select drpActions = new Select(optSelectAction);
				WebElement btnGo = driver2.findElement(By.partialLinkText("Go"));
				drpActions.selectByVisibleText("Reject");
				btnGo.click();
				WebElement frmWorkFlow = driver2.findElement(By.id("workflowIframe"));
				switchToIFrame(driver2, frmWorkFlow);
			}

			// Delete Project
			public void deleteProject(WebDriver driver2) 
			{
				System.out.println("Deleting Project");
				WebElement optSelectAction = driver2.findElement(By.cssSelector("[id^=actionOption]"));
				Select drpActions = new Select(optSelectAction);
				WebElement btnGo = driver2.findElement(By.partialLinkText("Go"));
				drpActions.selectByVisibleText("Delete Project");
				btnGo.click();
				WebElement frmWorkFlow = driver2.findElement(By.id("workflowIframe"));
				switchToIFrame(driver2, frmWorkFlow);
			}

			// Approve and Deploy to Staging
			public void approveAndDeployToStaging(WebDriver driver2) 
			{
				System.out.println("Approved and directly starting STAGE deployment");
				WebElement optSelectAction = driver2.findElement(By.cssSelector("[id^=actionOption]"));
				Select drpActions = new Select(optSelectAction);
				WebElement btnGo = driver2.findElement(By.partialLinkText("Go"));
				drpActions.selectByVisibleText("Approve and Deploy to Staging");
				btnGo.click();
				WebElement frmWorkFlow = driver2.findElement(By.id("workflowIframe"));
				switchToIFrame(driver2, frmWorkFlow);
			}

			// Approve for Staging Deployment
			public void approveForStagingDeployment(WebDriver driver2) {
				System.out.println("Adding to STAGE ToDo tab");
				WebElement optSelectAction = driver2.findElement(By.cssSelector("[id^=actionOption]"));
				Select drpActions = new Select(optSelectAction);
				drpActions.selectByVisibleText("Approve for Staging Deployment");
				WebElement btnGo = driver2.findElement(By.partialLinkText("Go"));
				btnGo.click();
				WebElement frmWorkFlow = driver2.findElement(By.id("workflowIframe"));
				switchToIFrame(driver2, frmWorkFlow);
			}

			// Reject Staging Deployment
			public void rejectStagingDeployment(WebDriver driver2) 
			{
				System.out.println("Rejecting STAGE Deployment");
				WebElement optSelectAction = driver2.findElement(By.cssSelector("[id^=actionOption]"));
				Select drpActions = new Select(optSelectAction);
				WebElement btnGo = driver2.findElement(By.partialLinkText("Go"));
				drpActions.selectByVisibleText("Reject Staging Deployment");
				btnGo.click();
				WebElement frmWorkFlow = driver2.findElement(By.id("workflowIframe"));
				switchToIFrame(driver2, frmWorkFlow);
			}

			// Accept Staging Deployment
			public void acceptStagingDeployment(WebDriver driver2) 
			{
				System.out.println("Accepting STAGE Deployment");
				WebElement optSelectAction = driver2.findElement(By.cssSelector("[id^=actionOption]"));
				Select drpActions = new Select(optSelectAction);
				WebElement btnGo = driver2.findElement(By.partialLinkText("Go"));
				drpActions.selectByVisibleText("Accept Staging Deployment");
				btnGo.click();
				WebElement frmWorkFlow = driver2.findElement(By.id("workflowIframe"));
				switchToIFrame(driver2, frmWorkFlow);
			}

			// Revert Assets on Staging Immediately
			public void revertAssetsOnStagingImmediately(WebDriver driver2) 
			{
				System.out.println("Reverting Assets on STAGE Immediately");
				WebElement optSelectAction = driver2.findElement(By.cssSelector("[id^=actionOption]"));
				Select drpActions = new Select(optSelectAction);
				WebElement btnGo = driver2.findElement(By.partialLinkText("Go"));
				drpActions.selectByVisibleText("Revert Assets on Staging Immediately");
				btnGo.click();
				WebElement frmWorkFlow = driver2.findElement(By.id("workflowIframe"));
				switchToIFrame(driver2, frmWorkFlow);
			}

			// Approve and Deploy to Production
			public void approveAndDeployToProduction(WebDriver driver2) 
			{
				System.out.println("Approving and directly starting PRODUCTION deployment");
				WebElement optSelectAction = driver2.findElement(By.cssSelector("[id^=actionOption]"));
				Select drpActions = new Select(optSelectAction);
				WebElement btnGo = driver2.findElement(By.partialLinkText("Go"));
				drpActions.selectByVisibleText("Approve and Deploy to Production");
				btnGo.click();
				WebElement frmWorkFlow = driver2.findElement(By.id("workflowIframe"));
				switchToIFrame(driver2, frmWorkFlow);
			}

			// Approve for Production Deployment
			public void approveForProductionDeployment(WebDriver driver2) 
			{
				System.out.println("Adding to PRODUCTION ToDo tab");
				WebElement optSelectAction = driver2.findElement(By.cssSelector("[id^=actionOption]"));
				Select drpActions = new Select(optSelectAction);
				WebElement btnGo = driver2.findElement(By.partialLinkText("Go"));
				drpActions.selectByVisibleText("Approve for Production Deployment");
				btnGo.click();
				WebElement frmWorkFlow = driver2.findElement(By.id("workflowIframe"));
				switchToIFrame(driver2, frmWorkFlow);
			}

			// Reject Production Deployment
			public void rejectProductionDeployment(WebDriver driver2) 
			{
				System.out.println("Rejecting PRODUCTION Deployment");
				WebElement optSelectAction = driver2.findElement(By.cssSelector("[id^=actionOption]"));
				Select drpActions = new Select(optSelectAction);
				WebElement btnGo = driver2.findElement(By.partialLinkText("Go"));
				drpActions.selectByVisibleText("Reject Production Deployment");
				btnGo.click();
				WebElement frmWorkFlow = driver2.findElement(By.id("workflowIframe"));
				switchToIFrame(driver2, frmWorkFlow);
			}

			// Accept Production Deployment
			public void acceptProductionDeployment(WebDriver driver2) 
			{
				System.out.println("Accepting PRODUCTION Deployment");
				WebElement optSelectAction = driver2.findElement(By.cssSelector("[id^=actionOption]"));
				Select drpActions = new Select(optSelectAction);
				WebElement btnGo = driver2.findElement(By.partialLinkText("Go"));
				drpActions.selectByVisibleText("Accept Production Deployment");
				btnGo.click();
				WebElement frmWorkFlow = driver2.findElement(By.id("workflowIframe"));
				switchToIFrame(driver2, frmWorkFlow);
			}

			// Revert Assets on Production Immediately
			public void revertAssetsOnProductionImmediately(WebDriver driver2) 
			{
				System.out.println("Reverting assets on PRODUCTION Immediately");
				WebElement optSelectAction = driver2.findElement(By.cssSelector("[id^=actionOption]"));
				Select drpActions = new Select(optSelectAction);
				WebElement btnGo = driver2.findElement(By.partialLinkText("Go"));
				drpActions.selectByVisibleText("Revert Assets on Production Immediately");
				btnGo.click();
				WebElement frmWorkFlow = driver2.findElement(By.id("workflowIframe"));
				switchToIFrame(driver2, frmWorkFlow);
			}
	
	public void switchToIFrame(WebDriver driver2, WebElement frmID) {
		driver2.switchTo().frame(frmID);
		WebElement btnOK = driver2.findElement(By.id("okActionButton"));
		btnOK.click();
	}

	public void navigateTo(WebDriver driver2, WebElement navElement) {
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

	public boolean checkProjectsInToDo(WebDriver driver2, String strProjectName) {
		boolean chkProject;
		chkProject = driver2.findElements(By.partialLinkText(strProjectName))
				.size() < 1;
		if (chkProject)
			return false;
		else
			return true;
	}

	public static void main(String[] args) throws FileNotFoundException {
		BCCDeployment deploy = new BCCDeployment();
		deploy.startDeploymentProcess();
	}
}
