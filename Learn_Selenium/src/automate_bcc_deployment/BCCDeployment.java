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

public class BCCDeployment
{
	private WebDriver driver;
	private String strProjectAbacosName;
	private String strOldPromoProject;
	private String environment=null;
	private boolean firstVisitToProdOverview = false; 
	private boolean isAbacosInToDo = false;
	private boolean isOldPromoInToDo = false;
	
	
	Properties prop = new Properties();
	ProjectActions actions;
	
	public void startDeploymentProcess() throws FileNotFoundException 
	{
		initializeProperties();
	}

	public void initializeProperties() throws FileNotFoundException 
	{
		InputStream input = null;
		input = new FileInputStream("./resources/properties/config.properties");
		//input = new FileInputStream("D://100rabh//Others//Development//GitWorkSpaces//selnm//Learn_Selenium//resources//properties//config.properties");
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

	public void openBCCUrl() 
	{
		//BCC Url
		driver.get(prop.getProperty("BaseUrlAR"));
		
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
		
		if (isBccOK) 
		{
			System.out.println("'"+isBccOK+"' BCC is working fine");
			loginToBCC();
		} else {
			System.out.println("'"+isBccOK+"' BCC is down or something is wrong with BCC, please check it... Closing Program");
			System.exit(0);
		}
	}

	public void loginToBCC() 
	{
		//System.out.println("on Login Page");
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
			
			//monitorDeployment();
			
			//Temp
			/*TimerTask timerTask = new BCCDeployment();
			Timer timer = new Timer(true);
			timer.schedule(timerTask, 0, 1000);*/
			
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
		navigateTo(lnkCAConsole);
		checkProdOverView();
	}
	
	public void checkProdOverView() 
	{
		//Set Project's names
		strProjectAbacosName = prop.getProperty("strProjectAbacosName");
		strOldPromoProject = prop.getProperty("strProjectOldPromoName");
		
		if(!firstVisitToProdOverview)
		{
			firstVisitToProdOverview=true;
			WebElement lnkProdOverview;
			String prodOverviewStr=null;
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
		btnDeploy.click();
		navigateToDetaisTab();
		
		//Scheduling Deployment monitor
		/*TimerTask timerTask = new BCCDeployment();
		Timer timer = new Timer(true);
		timer.scheduleAtFixedRate(timerTask, 0, 10*1000);*/
		
		try {
			Thread.sleep(1000);
			monitorDeployment();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/*public void run() 
	{
		//monitorDeployment();
		WebElement txtPass = driver.findElement(By.id("loginPassword"));
		String pass = prop.getProperty("HMGpassword");
		txtPass.clear();
	}*/
	
	public void monitorDeployment()
	{
		System.out.println("Monitoring Deployment...");
		WebElement progressBar = driver.findElement(By.id("progressBar"));
		System.out.println(progressBar.getText());
		while(true)
		{
			String pageSource = driver.getPageSource();
			//if(progressBar.getText().equals("Deployment Progress"))
			if(pageSource.contains("Deployment Failed"))
			{
				System.out.println("Deployment Failed...!!! Please stop it...");
				stopDeployment();
				haltDeployment();
				break;
			}
			else if(pageSource.contains("Please refresh the page"))
			{
				
				break;
			}
			else
			{	
				try {
					Thread.sleep(5000);
					System.out.println("\nDeployment is going on");
					System.out.println((driver.findElement(By.id("progressBar")).getText()));
					highLightElementProgressBar();
				} catch (InterruptedException e) 
				{
					e.printStackTrace();
				}
			}
		}
		deploymentCompleted();
	}
	
	public void stopDeployment()
	{
		System.out.println("Stopping Deployment");
	}

	public void haltDeployment()
	{
		System.out.println("Halting Deployment");
	}
	
	public void deploymentCompleted()
	{
		System.out.println("Deployment is completed...Please accept projects.");
	}
	
	public void acceptProjects()
	{
		
	}
	public void highLightElementProgressBar() 
	{
		WebElement progressBar = driver.findElement(By.id("progressBar"));
		String DELAY = "delay";
		JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeScript("arguments[0].setAttribute('style', arguments[1]);",progressBar, "color: green; border: 4px solid green;");
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
		//progressBar = driver.findElement(By.id("progressBar"));
		js.executeScript("arguments[0].setAttribute('style', arguments[1]);",driver.findElement(By.id("progressBar")), "");
	}
	
	public void navigateToDetaisTab()
	{
		WebElement lnkDetails = driver.findElement(By.linkText("Details"));
		lnkDetails.click();
	}
	
	public void isDeploymentResumed() {
		WebElement btnResumeDeployment;
		boolean isResumed = driver.findElements(By.partialLinkText("Resume ")).size() < 1;
		//System.out.println(isResumed);
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
			System.out.println("Deployments Resumed.");
			navigateToAgents();
			navigateToPlanTab();
			navigateToDoTab();
		}
	}

	public void navigateToDoTab() {
		WebElement lnkToDoTab;
		lnkToDoTab = driver.findElement(By.linkText("To Do"));
		lnkToDoTab.click();
		
		isAbacosInToDo = checkProjectsInToDo(driver, strProjectAbacosName);
		isOldPromoInToDo = checkProjectsInToDo(driver, strOldPromoProject);

		if (isAbacosInToDo && isOldPromoInToDo) 
		{
			WebElement foundAbacosProject = driver.findElement(By.partialLinkText(strProjectAbacosName));
			highLightElement(driver, foundAbacosProject);
						
			WebElement foundOldPromoProject = driver.findElement(By.partialLinkText(strOldPromoProject));
			highLightElement(driver, foundOldPromoProject);
			
			System.out.println("Both Projects '" + foundAbacosProject.getText()+"' And '"+foundOldPromoProject.getText()+ "' available in ToDo");
			startDeployment();
		}
		/*else if (isAbacosInToDo) 
		{
			WebElement foundAbacosProject = driver.findElement(By.partialLinkText(strProjectAbacosName));
			highLightElement(driver, foundAbacosProject);
			System.out.println("Project " + foundAbacosProject.getText()+ " available in ToDo");
		}
		else if (isOldPromoInToDo) {
			WebElement foundOldPromoProject = driver.findElement(By
					.partialLinkText(strOldPromoProject));
			highLightElement(driver, foundOldPromoProject);
			System.out.println("Project " + foundOldPromoProject.getText()
					+ " available in ToDo");
		}*/ else if (!isAbacosInToDo && isOldPromoInToDo) {
			System.out.println("'"+strProjectAbacosName +"' is not available in ToDo, Going to Search it");
			navigateToHome();
			WebElement lnkCAProjects = driver.findElement(By.linkText("CA Projects"));
			// searchProject();
			navigateTo(lnkCAProjects);
			searchProject(strProjectAbacosName);
		} else if (isAbacosInToDo && !isOldPromoInToDo) {
			System.out.println("'"+strOldPromoProject + "' is not available in ToDo, Going to Search it");
			navigateToHome();
			WebElement lnkCAProjects = driver.findElement(By.linkText("CA Projects"));
			// searchProject();
			navigateTo(lnkCAProjects);
			searchProject(strOldPromoProject);
		} else {
			System.out.println("Going to Search Projects  '" + strProjectAbacosName + "'  And  '" + strOldPromoProject+"'");
			navigateToHome();
			WebElement lnkCAProjects = driver.findElement(By.linkText("CA Projects"));
			// searchProject();
			navigateTo(lnkCAProjects);
			searchProject(strProjectAbacosName, strOldPromoProject);
		}
	}

	public void searchProject(String strProjectAbacosName,String strOldPromoProject) 
	{
		//System.out.println("Ready to Search Projects  '" + strProjectAbacosName	+ "'  And  '" + strOldPromoProject+"'");
		searchProject(strProjectAbacosName);
		
		navigateToAvailableProjects();
		
		searchProject(strOldPromoProject);
		navigateToHome();
		navigateToCA_Console();
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
			cancelProjectsFromPlan();
		}
	}
	
	public void cancelProjectsFromPlan()
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
	
	public void navigateToAvailableProjects()
	{
		//« Available projects
		WebElement availableProjects = driver.findElement(By.partialLinkText("Available projects"));
		navigateTo(availableProjects);
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
			System.out.println("Current Snapshot is - '"+currentSnapshot+"'");
			StringBuilder sb = new StringBuilder(currentSnapshot);
			sb.insert(0, "<td class=\"rightAligned\"><span class=\"tableInfo\">");
			sb.append("</span></td>");
			//System.out.println("Uppended string is "+snapshotString);
			String snapshotString = sb.toString();
			//System.out.println("Got String from builder "+snapshotString);
			
			String pageSource = driver.getPageSource();
			//System.out.println(pageSource);

	        int ind1,ind2,snapshotCount=0,agentStatusCount=0;
	        for(int i=0,j=0; i+snapshotString.length()<=pageSource.length(); i++,j++)    //i+sub.length() is used to reduce comparisons
	        {
	            ind1 = pageSource.indexOf(snapshotString, i);
	            ind2 = pageSource.indexOf("Idle", j); 
	            if(ind1>=0)
	            {
	                snapshotCount++;
	                i=ind1;
	                ind1=-1;
	            }
	            if(ind2>=0)
	            {
	            	agentStatusCount++;
	            	j=ind2;
	                ind2=-1;
	            }
	        }
	        System.out.println("Total number of Snapshot '"+currentSnapshot+"'  in Agents is = '"+snapshotCount+"'");
	        System.out.println("Total number of Idle Agents is = '"+agentStatusCount+"'");
	        
	        if(environment.equals("HMG02"))
	        {
	        	if(snapshotCount==1 && agentStatusCount==1)
	        	{
	        		System.out.println("Agents health is OK and we are good to start deployment");
	        	}
	        	else
	        	{
	        		System.out.println("Something is wrong with Agents. Please check");
	        		System.exit(0);
	        	}
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
	
	public void searchProject(String searchProjectName) 
	{
		WebElement searchBox = driver.findElement(By
				.name("/atg/epub/servlet/ProcessSearchFormHandler.textInput"));
		searchBox.clear();
		searchBox.sendKeys(searchProjectName);
		WebElement lnkGo = driver.findElement(By.className("goButton")); 
		JavascriptExecutor jse = (JavascriptExecutor)driver;
		jse.executeScript("arguments[0].scrollIntoView()", lnkGo);
		//System.out.println(lnkGo.getText());
		try {
			Thread.sleep(3000);
			lnkGo.click();
		} catch (InterruptedException interuptE) {
			interuptE.printStackTrace();
		}
		
		boolean isProjectNotFound = driver.findElements(By.tagName("img")).size() < 1;
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
			WebElement projectFound = driver.findElement(By.tagName("img"));
			boolean isCurrentTaskAvailable = driver.findElements(
					By.className("current")).size() < 1;
			if (!isCurrentTaskAvailable) {
				WebElement currentTask = driver.findElement(By
						.className("current"));
				highLightElement(driver, currentTask);
				String strCurrentTask = currentTask.getText();
				System.out.println("Current Task is = " + strCurrentTask);
				projectFound.click();
				selectProjectAction(driver, strCurrentTask);
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
			navigateToHome();
			navigateToCA_Console();
			navigateToDeploymentOverview();
		} else if (strCurrentTask.equals("Content Review")) {
			//actions.approveContent(driver2, drpActions, btnGo);
		} else if (strCurrentTask.equals("Approve for Deployment")) {
			//actions.approveForStagingDeployment(driver2, drpActions, btnGo);
			if(environment.equals("BR_Production") || environment.equals("AR_Production"))
				actions.approveForStagingDeployment(driver2);
			else if(environment.equals("HMG02"))
				actions.approveForProductionDeployment(driver2);
		}else if(strCurrentTask.equals("Verify Staging Deployment"))
		{
			actions.acceptStagingDeployment(driver2);;
		}
	}
	
	public void navigateToDeploymentOverview()
	{
		WebElement lnkDeploymentOverview = driver.findElement(By.partialLinkText("back to deployment overview"));
		highLightElement(driver, lnkDeploymentOverview);
		lnkDeploymentOverview.click();
		navigateToStage();
	}
	
	public void navigateToStage()
	{
		//prodOverviewStr = prop.getProperty("prodOverviewXpathAR");
		//prodOverview = driver.findElement(By.xpath(prodOverviewStr)).getAttribute("href").contains(prop.getProperty("ARProdTar"));
		WebElement lnkStageOverview = driver.findElement(By.linkText("Staging"));
		highLightElement(driver, lnkStageOverview);
		lnkStageOverview.click();
		isDeploymentResumed();
	}
	
	public void switchToIFrame(WebDriver driver2, WebElement frmID) {
		driver2.switchTo().frame(frmID);
		WebElement btnOK = driver2.findElement(By.id("okActionButton"));
		btnOK.click();
	}

	public void navigateTo(WebElement navElement) {
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
		js.executeScript("arguments[0].setAttribute('style', arguments[1]);",
				webElement, "");
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		BCCDeployment deploy = new BCCDeployment();
		deploy.startDeploymentProcess();
	}
}
