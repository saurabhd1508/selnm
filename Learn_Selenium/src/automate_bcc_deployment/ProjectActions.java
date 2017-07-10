package automate_bcc_deployment;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

public class ProjectActions 
{
	WebDriver driver2;
	Select drpActions;
	String strCurrentTask;
	WebElement btnGo;
	
	BCCDeployment deploy = new BCCDeployment(); 
	
	public ProjectActions(WebDriver driver2, Select drpActions,String strCurrentTask, WebElement btnGo) 
	{
		this.driver2 = driver2;
		this.drpActions = drpActions;
		this.strCurrentTask = strCurrentTask;
		this.btnGo = btnGo;
	}

		// Ready for Review
		public void reviewProject(WebDriver driver2, Select drpActions,WebElement btnGo) {
			System.out.println("Selecting ready for review");
			drpActions.selectByVisibleText("Ready for Review");
			btnGo.click();
			WebElement frmWorkFlow = driver2.findElement(By.id("workflowIframe"));
			deploy.switchToIFrame(driver2, frmWorkFlow);
		}

		// Approve Content
		public void approveContent(WebDriver driver2, Select drpActions,WebElement btnGo) {
			System.out.println("Approving Content");
			drpActions.selectByVisibleText("Approve Content");
			btnGo.click();
			WebElement frmWorkFlow = driver2.findElement(By.id("workflowIframe"));
			deploy.switchToIFrame(driver2, frmWorkFlow);
		}

		// Reject
		public void reject(WebDriver driver2, Select drpActions, WebElement btnGo) {
			System.out.println("Taking Back to Author");
			drpActions.selectByVisibleText("Reject");
			btnGo.click();
			WebElement frmWorkFlow = driver2.findElement(By.id("workflowIframe"));
			deploy.switchToIFrame(driver2, frmWorkFlow);
		}

		// Delete Project
		public void deleteProject(WebDriver driver2, Select drpActions,WebElement btnGo) 
		{
			System.out.println("Deleting Project");
			drpActions.selectByVisibleText("Delete Project");
			btnGo.click();
			WebElement frmWorkFlow = driver2.findElement(By.id("workflowIframe"));
			deploy.switchToIFrame(driver2, frmWorkFlow);
		}

		// Approve and Deploy to Staging
		public void approveAndDeployToStaging(WebDriver driver2, Select drpActions,WebElement btnGo) 
		{
			System.out.println("Approved and directly starting STAGE deployment");
			drpActions.selectByVisibleText("Approve and Deploy to Staging");
			btnGo.click();
			WebElement frmWorkFlow = driver2.findElement(By.id("workflowIframe"));
			deploy.switchToIFrame(driver2, frmWorkFlow);
		}

		// Approve for Staging Deployment
		public void approveForStagingDeployment(WebDriver driver2,Select drpActions, WebElement btnGo) {
			System.out.println("Adding to STAGE ToDo tab");
			drpActions.selectByVisibleText("Approve for Staging Deployment");
			btnGo.click();
			WebElement frmWorkFlow = driver2.findElement(By.id("workflowIframe"));
			deploy.switchToIFrame(driver2, frmWorkFlow);
		}

		// Reject Staging Deployment
		public void rejectStagingDeployment(WebDriver driver2,Select drpActions, WebElement btnGo) 
		{
			System.out.println("Rejecting STAGE Deployment");
			drpActions.selectByVisibleText("Reject Staging Deployment");
			btnGo.click();
			WebElement frmWorkFlow = driver2.findElement(By.id("workflowIframe"));
			deploy.switchToIFrame(driver2, frmWorkFlow);
		}

		// Accept Staging Deployment
		public void acceptStagingDeployment(WebDriver driver2,Select drpActions, WebElement btnGo) 
		{
			System.out.println("Accepting STAGE Deployment");
			drpActions.selectByVisibleText("Accept Staging Deployment");
			btnGo.click();
			WebElement frmWorkFlow = driver2.findElement(By.id("workflowIframe"));
			deploy.switchToIFrame(driver2, frmWorkFlow);
		}

		// Revert Assets on Staging Immediately
		public void revertAssetsOnStagingImmediately(WebDriver driver2,Select drpActions, WebElement btnGo) 
		{
			System.out.println("Reverting Assets on STAGE Immediately");
			drpActions.selectByVisibleText("Revert Assets on Staging Immediately");
			btnGo.click();
			WebElement frmWorkFlow = driver2.findElement(By.id("workflowIframe"));
			deploy.switchToIFrame(driver2, frmWorkFlow);
		}

		// Approve and Deploy to Production
		public void approveAndDeployToProduction(WebDriver driver2,Select drpActions, WebElement btnGo) 
		{
			System.out.println("Approving and directly starting PRODUCTION deployment");
			drpActions.selectByVisibleText("Approve and Deploy to Production");
			btnGo.click();
			WebElement frmWorkFlow = driver2.findElement(By.id("workflowIframe"));
			deploy.switchToIFrame(driver2, frmWorkFlow);
		}

		// Approve for Production Deployment
		public void approveForProductionDeployment(WebDriver driver2,Select drpActions, WebElement btnGo) 
		{
			System.out.println("Adding to PRODUCTION ToDo tab");
			drpActions.selectByVisibleText("Approve for Production Deployment");
			btnGo.click();
			WebElement frmWorkFlow = driver2.findElement(By.id("workflowIframe"));
			deploy.switchToIFrame(driver2, frmWorkFlow);
		}

		// Reject Production Deployment
		public void rejectProductionDeployment(WebDriver driver2,Select drpActions, WebElement btnGo) 
		{
			System.out.println("Rejecting PRODUCTION Deployment");
			drpActions.selectByVisibleText("Reject Production Deployment");
			btnGo.click();
			WebElement frmWorkFlow = driver2.findElement(By.id("workflowIframe"));
			deploy.switchToIFrame(driver2, frmWorkFlow);
		}

		// Accept Production Deployment
		public void acceptProductionDeployment(WebDriver driver2,Select drpActions, WebElement btnGo) 
		{
			System.out.println("Accepting PRODUCTION Deployment");
			drpActions.selectByVisibleText("Accept Production Deployment");
			btnGo.click();
			WebElement frmWorkFlow = driver2.findElement(By.id("workflowIframe"));
			deploy.switchToIFrame(driver2, frmWorkFlow);
		}

		// Revert Assets on Production Immediately
		public void revertAssetsOnProductionImmediately(WebDriver driver2,Select drpActions, WebElement btnGo) 
		{
			System.out.println("Reverting assets on PRODUCTION Immediately");
			drpActions.selectByVisibleText("Revert Assets on Production Immediately");
			btnGo.click();
			WebElement frmWorkFlow = driver2.findElement(By.id("workflowIframe"));
			deploy.switchToIFrame(driver2, frmWorkFlow);
		}

		
}
