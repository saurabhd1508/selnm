package automate.weblogic.servers.restart;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.openqa.selenium.Alert;
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
		driver.get(prop.getProperty("HMG05WebLogicBaseUrl"));
		setEnvironment();
		askUser();
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
		shutDownInstances();
	}

	public void shutDownInstances() 
	{
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
        innerPanel.add(text);
        dialog.setIconImage(image);
        dialog.add(innerPanel);
        dialog.pack();
        dialog.setSize(new Dimension(300, 150));
        dialog.setLocationRelativeTo(text);
        dialog.setAlwaysOnTop(true);
        dialog.setVisible(true);
        input = text.getText();
		return input;
	}
	
	public void askUser() 
	{
		System.out.println("How many instances to be shutdown?");
		
		int num = Integer.parseInt(getUserInputFromPanel());
		System.out.println(num+" instances to restart");
		System.out.println(num+1);
		//WebElement bccInstance = driver.findElement(By.tagName("input")).getAttribute("title").contains("Select atg_bcc");
		WebElement bccInstance = driver.findElement(By.cssSelector("input[title='Select atg_bcc']"));
		highLightElement(bccInstance);
	}

	public static void main(String[] args) throws FileNotFoundException 
	{
		WeblogicRestartController wl = new WeblogicRestartController();
		wl.setProperties();
		System.exit(0);
	}
}
