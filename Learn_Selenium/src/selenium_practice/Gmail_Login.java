package selenium_practice;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
//import org.openqa.selenium.firefox.FirefoxDriver;
import java.util.List;
import java.util.Properties;

public class Gmail_Login implements TestDataInt 
{
	public void execute() throws InterruptedException
	{
		System.setProperty(TestDataInt.webDProperty, TestDataInt.driverPath);
		// objects and variables instantiation
		// WebDriver driver = new FirefoxDriver();

		WebDriver driver = new ChromeDriver();

		// launch the firefox browser and open the application url
		driver.get(TestDataInt.appUrl);

		// maximize the browser window
		//driver.manage().window().maximize();

		// declare and initialize the variable to store the expected title of the webpage.
		// fetch the title of the web page and save it into a string variable
		// compare the expected title of the page with the actual title of the
		// page and print the result
		
		//List<WebElement> inputs = (List<WebElement>) driver.findElement(By.tagName("input"));
		List<WebElement> labels = driver.findElements(By.tagName("label"));
		List<WebElement> inputs = (List<WebElement>) ((JavascriptExecutor)driver).executeScript(
			    "var labels = arguments[0], inputs = []; for (var i=0; i < labels.length; i++){" +
			    "inputs.push(document.getElementById(labels[i].getAttribute('for'))); } return inputs;", labels);
		System.out.println("Total Inputes "+inputs.size());
		
		for(int i=0;i<=inputs.size();i++)
		{	
			System.out.println("Input "+ i + " is = "+inputs.get(i).toString());
		}
		if ((TestDataInt.expectedTitle).equals(driver.getTitle()))
		{
			System.out.println("Verification Successful - The correct title is displayed on the web page.");
		} else {
			System.out.println("Verification Failed - An incorrect title is displayed on the web page.");
		}

		// enter a valid username in the email textbox

		WebElement username = driver.findElement(By.id("Email"));

		username.clear();
		username.sendKeys(TestDataInt.uName);

		// enter a valid password in the password textbox
		WebElement nextBtn = driver.findElement(By.id("next"));
		nextBtn.click();
		Thread.sleep(5000);
		WebElement password = driver.findElement(By.id("Passwd"));

		password.clear();

		password.sendKeys(TestDataInt.pass);

		// click on the Sign in button

		WebElement SignInButton = driver.findElement(By.id("signIn"));

		SignInButton.click();

		// close the web browser

		// driver.close();

		System.out.println("Test script executed successfully.");

		// terminate the program

		System.exit(0);
	}
	public static void main(String[] args) throws InterruptedException 
	{
		Gmail_Login g = new Gmail_Login();
		g.execute();
	}
}