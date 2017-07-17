package automate_bcc_deployment;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

public class BCCDeployment {

	Properties prop = new Properties();
	WebDriver driver;
	String strProjectAbacosName;
	String strOldPromoProject;
	ProjectActions actions;
	String environment=null;
	public void startDeploymentProcess() throws FileNotFoundException 
	{
		initializeProperties();
	}

	public void initializeProperties() throws FileNotFoundException 
	{
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
		actions= new ProjectActions(driver);
		openBCCUrl();
	}

	public void highLightElement(WebDriver driver, WebElement webElement) 
	{
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
		driver.get(prop.getProperty("BaseUrlHMG"));
		if(driver.getCurrentUrl().contains("hmg02"))
		{
			environment = "HMG02"; 
		}
		else if(driver.getCurrentUrl().contains(prop.getProperty("curBCCurlBR")))
		{
			environment = "BR_Production";
		}
		else if(driver.getCurrentUrl().contains(prop.getProperty("curBCCurlAR")))
		{
			environment = "AR_Production";
		}
		boolean isBccOK = driver.findElements(By.tagName("input")).size() > 1;
		System.out.println(isBccOK+" BCC is working fine");
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
		String chkHomeUrl, uName, pass;
		
		if(environment.equals("HMG02"))
		{
			txtuser = driver.findElement(By.id("loginName"));
			uName = prop.getProperty("userName");
			txtuser.sendKeys(uName);

			txtPass = driver.findElement(By.id("loginPassword"));
			pass = prop.getProperty("HMGpassword");
			txtPass.sendKeys(pass);
		}
		if(environment.equals("BR_Production"))
		{
			txtuser = driver.findElement(By.id("loginName"));
			uName = prop.getProperty("userName");
			txtuser.sendKeys(uName);

			txtPass = driver.findElement(By.id("loginPassword"));
			pass = prop.getProperty("prodPassword");
			txtPass.sendKeys(pass);
		}
		if(environment.equals("AR_Production"))
		{
			txtuser = driver.findElement(By.id("loginName"));
			uName = prop.getProperty("userName");
			txtuser.sendKeys(uName);

			txtPass = driver.findElement(By.id("loginPassword"));
			pass = prop.getProperty("prodPassword");
			txtPass.sendKeys(pass);
		}
		btnLogin = driver.findElement(By.name(prop.getProperty("btnLogin")));
		btnLogin.click();

		chkHomeUrl = driver.getCurrentUrl();

		if (chkHomeUrl.contains(prop.getProperty("afterLoggedInHomeUrl")))
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
	
	boolean firstVisitToProdOverview = false; 
	
	public void checkProdOverView() 
	{
		strProjectAbacosName = prop.getProperty("strProjectAbacosName");
		strOldPromoProject = prop.getProperty("strProjectOldPromoName");
		if(!firstVisitToProdOverview)
		{
			firstVisitToProdOverview=true;
			WebElement lnkProdOverview;
			String prodOverviewStr=null;
			//String currentUrl = driver.getCurrentUrl();
			boolean prodOverview = false;
			//if(currentUrl.contains("hmg"))
			if(environment.equals("HMG02"))
			{
				prodOverviewStr = prop.getProperty("prodOverviewXpathHMG");
				prodOverview = driver.findElement(By.xpath(prodOverviewStr)).getAttribute("href").contains(prop.getProperty("hmg02ProdTar"));
			}
			//else if(currentUrl.contains(prop.getProperty("curBCCurlBR")))
			else if(environment.equals("BR_Production"))
			{
				prodOverviewStr = prop.getProperty("prodOverviewXpathBR");
				prodOverview = driver.findElement(By.xpath(prodOverviewStr)).getAttribute("href").contains(prop.getProperty("BRProdTar"));
			}
			//else if(currentUrl.contains(prop.getProperty("curBCCurlAR")))
			else if(environment.equals("AR_Production"))
			{
				prodOverviewStr = prop.getProperty("prodOverviewXpathAR");
				prodOverview = driver.findElement(By.xpath(prodOverviewStr)).getAttribute("href").contains(prop.getProperty("ARProdTar"));
			}
			
		// boolean stageOverview =
		// driver.findElement(By.xpath(prodOverviewStr)).getAttribute("href").contains(prop.getProperty("ProductionProdTar"));
		if (prodOverview) {
			System.out.println("Production overview found");
			lnkProdOverview = driver.findElement(By.linkText("Production"));
			highLightElement(driver, lnkProdOverview);
			lnkProdOverview.click();
			isDeploymentResumed();
		} 
		else
			System.out.println("Prod link NOT found");
		}
		else
		{
			System.out.println("In Prod overview more than once...");
			isAbacosInToDo = checkProjectsInToDo(driver, strProjectAbacosName);
			if(isAbacosInToDo)
			System.out.println(driver.findElement(By.partialLinkText(strProjectAbacosName)).getText());
			startDeployment();
		}
	}
	
	public void startDeployment() 
	{
		System.out.println("Starting Deployment");
		WebElement selectAllProjects = driver.findElement(By.id("checkAllField"));
		selectAllProjects.click();
		WebElement btnDeploySelected = driver.findElement(By.id("deployButton"));
		btnDeploySelected.click();
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		WebElement optNow = driver.findElement(By.xpath("//*[@id=\"adminToDoRight\"]/table/tbody/tr[1]/td[2]/p/input[1]"));
		highLightElement(driver, optNow);
		optNow.click();
		WebElement btnDeploy = driver.findElement(By.linkText("Deploy"));
		highLightElement(driver, btnDeploy);
		//btnDeploy.click();
		navigateToDetaisTab();
		monitorDeployment();
	}
	
	public void navigateToDetaisTab()
	{
		WebElement lnkDetails = driver.findElement(By.linkText("Details"));
		lnkDetails.click();
	}
	
	public void monitorDeployment()
	{
		
	}
	
	public void isDeploymentResumed() {
		WebElement btnResumeDeployment;
		boolean isResumed = driver.findElements(By.partialLinkText("Resume ")).size() < 1;
		System.out.println(isResumed);
		if (isResumed) {
			System.out.println("Deployments Already Resumed");
			navigateToAgents();
			navigateToPlanTab();
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
			navigateToAgents();
			navigateToPlanTab();
			navigateToDoTab();
		}
	}

	boolean isAbacosInToDo = false;
	boolean isOldPromoInToDo = false;

	public void navigateToDoTab() {
		WebElement lnkToDoTab;
		lnkToDoTab = driver.findElement(By.linkText("To Do"));
		lnkToDoTab.click();
		
		isAbacosInToDo = checkProjectsInToDo(driver, strProjectAbacosName);
		isOldPromoInToDo = checkProjectsInToDo(driver, strOldPromoProject);

		if (isAbacosInToDo) {
			WebElement foundAbacosProject = driver.findElement(By
					.partialLinkText(strProjectAbacosName));
			highLightElement(driver, foundAbacosProject);
			System.out.println("Project " + foundAbacosProject.getText()
					+ " available in ToDo");
		}
		if (isOldPromoInToDo) {
			WebElement foundOldPromoProject = driver.findElement(By
					.partialLinkText(strOldPromoProject));
			highLightElement(driver, foundOldPromoProject);
			System.out.println("Project " + foundOldPromoProject.getText()
					+ " available in ToDo");
		} else if (!isAbacosInToDo && isOldPromoInToDo) {
			System.out.println(strProjectAbacosName +" is not available in ToDo, Going to Search it");
			navigateToHome();
			WebElement lnkCAProjects = driver.findElement(By.linkText("CA Projects"));
			// searchProject();
			navigateTo(driver, lnkCAProjects);
			searchProject(driver, strProjectAbacosName);
		} else if (isAbacosInToDo && !isOldPromoInToDo) {
			System.out.println(strOldPromoProject + " is not available in ToDo, Going to Search it");
			navigateToHome();
			WebElement lnkCAProjects = driver.findElement(By.linkText("CA Projects"));
			// searchProject();
			navigateTo(driver, lnkCAProjects);
			searchProject(driver, strOldPromoProject);
		} else {
			System.out.println("Going to Search Projects  " + strProjectAbacosName + " and " + strOldPromoProject);
			navigateToHome();
			WebElement lnkCAProjects = driver.findElement(By.linkText("CA Projects"));
			// searchProject();
			navigateTo(driver, lnkCAProjects);
			searchProject(strProjectAbacosName, strOldPromoProject);
		}
	}

	public void searchProject(String strProjectAbacosName,String strOldPromoProject) 
	{
		System.out.println("Ready to Search Projects  " + strProjectAbacosName	+ " and " + strOldPromoProject);
		searchProject(driver, strProjectAbacosName);
		
		navigateToAvailableProjects();
		
		searchProject(driver, strOldPromoProject);
		//navigateToHome();
		//navigateToCA_Console();
	}
	
	public void navigateToPlanTab()
	{
		System.out.println("Checking Plan tab");
		WebElement planTab = driver.findElement(By.linkText("Plan"));
		planTab.click();
		boolean isPlanEmpty = driver.findElements(By.tagName("img")).size() < 1;
		if(isPlanEmpty)
			System.out.println("No projects in Plan tab");
		else
		{
			System.out.println("There are projects availabel in Plan tab... Please cancel them");
			WebElement selectAll = driver.findElement(By.id("checkAllField"));
			selectAll.click();
			WebElement btnCancelSelected = driver.findElement(By.linkText("Cancel selected"));
			btnCancelSelected.click();
			WebElement frmCancelDeployment = driver.findElement(By.id("cancelDeploymentsActionIframe"));

			driver.switchTo().frame(frmCancelDeployment);
			WebElement btnOK = driver.findElement(By.linkText("OK"));
			btnOK.click();
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if((driver.findElements(By.tagName("img")).size() < 1))
			{
				System.out.println("Projects cancelled...");
			}
		}
	}
	public void navigateToAvailableProjects()
	{
		//« Available projects
		WebElement availableProjects = driver.findElement(By.partialLinkText("Available projects"));
		navigateTo(driver,availableProjects);
	}
	public void navigateToHome() {
		WebElement lnkHome = driver.findElement(By.linkText("Home"));
		lnkHome.click();
	}
	
	public void navigateToAgents()
	{
		WebElement agentsTab = driver.findElement(By.linkText("Agents"));
		agentsTab.click();
		try {
			Thread.sleep(3000);
			WebElement lblSnapshot = driver.findElement(By.xpath("//*[@id=\"adminDeployment\"]/table/tbody/tr[2]/td[5]/span"));
			String currentSnapshot = lblSnapshot.getText();
			System.out.println("Current Snapshot is - "+currentSnapshot);
			StringBuilder snapshotString = new StringBuilder(currentSnapshot);
			snapshotString.insert(0, "<td class=\"rightAligned\"><span class=\"tableInfo\">");
			snapshotString.append("</span></td>");
			//System.out.println("Uppended string is "+snapshotString);
			String str = snapshotString.toString();
			System.out.println("Got String from builder "+str);
			
			String pageSource = driver.getPageSource();
			//System.out.println(pageSource);

	        int ind,snapshotCount=0,agentStatusCount=0;
	        for(int i=0; i+str.length()<=pageSource.length(); i++)    //i+sub.length() is used to reduce comparisons
	        {
	        	//int indexOf(substring,fromIndex);
	            //ind = pageSource.indexOf(snapshotString, i);
	        	
	            ind=pageSource.indexOf(str, i);
	            if(ind>=0)
	            {
	                snapshotCount++;
	                agentStatusCount++;
	                i=ind;
	                ind=-1;
	            }
	        }
	        System.out.println("Total number of snapshot '"+currentSnapshot+"'  in Agents is  "+snapshotCount);
	        System.out.println("Total number of Idle Agents is  "+agentStatusCount);
	        
	        if(environment.equals("HMG02"))
	        {
	        	if(snapshotCount==1 && agentStatusCount==1)
	        	{
	        		System.out.println("Agents health is OK and we are good to start deployment");
	        	}
	        	else
	        		System.out.println("Something is wrong with Agents. Please check");
	        }
	        //else if(driver.getCurrentUrl().contains(prop.getProperty("curBCCurlBR")))
	        else if(environment.equals("BR_Production"))
	        {
	        	if(snapshotCount==Integer.parseInt(prop.getProperty("totalBRAgents")) && agentStatusCount==Integer.parseInt(prop.getProperty("totalBRAgents")))
	        	{
	        		System.out.println("All '"+prop.getProperty("totalBRAgents")+"' Agent's health is OK and we can start deployment");
	        	}
	        	else
	        		System.out.println("Something is wrong with Agents. Please check");
	        }
	        //else if(driver.getCurrentUrl().contains(prop.getProperty("curBCCurlAR")))
	        else if(environment.equals("AR_Production"))
	        {
	        	if(snapshotCount==Integer.parseInt(prop.getProperty("totalARAgents")) && agentStatusCount==Integer.parseInt(prop.getProperty("totalARAgents")))
	        	{
	        		System.out.println("All '"+prop.getProperty("totalARAgents")+"' Agent's health is OK and we can start deployment");
	        	}
	        	else
	        		System.out.println("Something is wrong with Agents. Please check");
	        }
	        
		} catch (InterruptedException interuptE) {
			interuptE.printStackTrace();
		}
	}
	
	public void searchProject(WebDriver driver2, String searchProjectName) 
	{
		WebElement searchBox = driver2.findElement(By
				.name("/atg/epub/servlet/ProcessSearchFormHandler.textInput"));
		searchBox.clear();
		searchBox.sendKeys(searchProjectName);
		WebElement lnkGo = driver2.findElement(By.className("goButton")); 
		JavascriptExecutor jse = (JavascriptExecutor)driver2;
		jse.executeScript("arguments[0].scrollIntoView()", lnkGo);
		System.out.println(lnkGo.getText());
		try {
			Thread.sleep(3000);
			lnkGo.click();
		} catch (InterruptedException interuptE) {
			interuptE.printStackTrace();
		}
		
		boolean isProjectNotFound = driver2.findElements(By.tagName("img")).size() < 1;
		//boolean isProjectNotFound = driver2.findElements(By.tagName("img")).
		//boolean isProjectNotFound = driver2.findElements(By.cssSelector("input[class='centerAligned error']")).size() < 1;
		
		if (isProjectNotFound) 
		{
			System.out.println("Project "  + searchProjectName + " is NOT Found...");
			navigateToHome();
			navigateToCA_Console();
		}
		else 
		{
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
		//WebElement btnGo = driver2.findElement(By.partialLinkText("Go"));
		
		//ProjectActions actions = new ProjectActions(driver2,drpActions,strCurrentTask,btnGo);
		
		if (strCurrentTask.equals("Author")) {
			//actions.reviewProject(driver2, drpActions, btnGo);
			//actions.approveContent(driver2, drpActions, btnGo);
			//actions.approveForStagingDeployment(driver2, drpActions, btnGo);
			actions.reviewProject(driver2);
		} else if (strCurrentTask.equals("Content Review")) {
			//actions.approveContent(driver2, drpActions, btnGo);
		} else if (strCurrentTask.equals("Approve for Deployment")) {
			//actions.approveForStagingDeployment(driver2, drpActions, btnGo);
			//approveForStagingDeployment(driver2);
			actions.approveForProductionDeployment(driver2);
		}
	}
	
	public void switchToIFrame(WebDriver driver2, WebElement frmID) {
		driver2.switchTo().frame(frmID);
		WebElement btnOK = driver2.findElement(By.id("okActionButton"));
		btnOK.click();
	}

	public void navigateTo(WebDriver driver2, WebElement navElement) {
		System.out.println(navElement.getText() + " found, highlighting it");
		try {
			((JavascriptExecutor) driver).executeScript(
					"arguments[0].scrollIntoView(true);", navElement);
		} catch (Exception e) {
		}

		highLightElement(driver, navElement);
		navElement.click();
	}

	public boolean checkProjectsInToDo(WebDriver driver2, String strProjectName) {
		boolean chkProject;
		chkProject = driver2.getPageSource().contains(strProjectName);
		if (chkProject)
			return true;
		else
			return false;
	}

	public static void main(String[] args) throws FileNotFoundException {
		BCCDeployment deploy = new BCCDeployment();
		deploy.startDeploymentProcess();
	}
}
