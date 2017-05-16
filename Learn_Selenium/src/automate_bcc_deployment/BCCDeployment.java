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

public class BCCDeployment {

	Properties prop = new Properties();
	WebDriver driver;

	public void startDeploymentProcess() throws FileNotFoundException
	{
		initializeProperties();
	}
	
	public void initializeProperties() throws FileNotFoundException {
		InputStream input = null;
		input = new FileInputStream("config.properties");
		try {
			prop.load(input);
		//	System.out.println(prop.getProperty("webDriverPath"));
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		setting();
	}

	public void setting() {
		System.setProperty(prop.getProperty("baseDriver"),
				prop.getProperty("webDriverPath"));
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
		//try {
			WebElement chkbtnLogin;
			System.out.println("In openBCCUrl");
			driver.get(prop.getProperty("baseUrl"));
			chkbtnLogin = driver.findElement(By.name(prop.getProperty("btnLogin")));
			loginToBCC();
		//} catch (NoSuchElementException exe) {
			//System.out.println("BCC is down or something is wrong with BCC, please check it... Closing Program");
			//System.exit(0);
		//}
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
			navigateCA_Console();
		} catch (InterruptedException interuptE) {
			interuptE.printStackTrace();
		}
	}
	
	public void navigateCA_Console()
	{
		WebElement lnkCAConsole;
		System.out.println("Searching CA Console");
		lnkCAConsole = driver.findElement(By.linkText("CA Console"));
		System.out.println("CA Console found, highlighting it");

		try {
			((JavascriptExecutor) driver).executeScript(
					"arguments[0].scrollIntoView(true);", lnkCAConsole);
			System.out.println("JavaScript Executed...");
		} catch (Exception e) {

		}

		highLightElement(driver, lnkCAConsole);

		lnkCAConsole.click();
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
		WebElement btnResumeDeployment,btnOk;
		try {
			btnResumeDeployment = driver.findElement(By
					.partialLinkText("Resume "));
			System.out.println("Resuming Deploy");

			List<WebElement> chkIsResumed = driver.findElements(By
					.partialLinkText("deployments"));

			System.out.println(chkIsResumed.size());

			if (btnResumeDeployment != null) {
				btnResumeDeployment.click();
				btnOk = driver.findElement(By.linkText("OK"));
				try {
					Thread.sleep(3000);
					navigateCA_Console();
				} catch (InterruptedException inturptE) {
					inturptE.printStackTrace();
				}
				highLightElement(driver, btnOk);
				btnOk.click();
				//navigateToDoTab();
				// chkIsResumed = driver.findElements(By.)
			}
		} catch (Exception e) {
			System.out.println("Deployments Already Resumed");
			navigateToDoTab();
			//e.printStackTrace();
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
		// String strCaConsole = driver.findElement(By.tagName("a")).toString();
		String strProjectAbacosName = prop.getProperty("strProjectAbacosName");
		boolean isProjectAvailable = checkProjectsInToDo(driver,
				strProjectAbacosName);

		if (isProjectAvailable)
			System.out.println("Ready With Project ");
		else
			searchProject();
	}
	
	public void searchProject()
	{
		System.out.println("Going to Search Project...");
	}
	
	public boolean checkProjectsInToDo(WebDriver driver2,
			String strProjectAbacosName) {
		WebElement chkProject;
		chkProject = driver2.findElement(By
				.partialLinkText(strProjectAbacosName));
		highLightElement(driver2, chkProject);
		System.out.println("Project " + strProjectAbacosName
				+ " available in ToDo");
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
