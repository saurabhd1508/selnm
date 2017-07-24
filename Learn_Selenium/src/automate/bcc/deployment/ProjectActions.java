package automate.bcc.deployment;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

public class ProjectActions {
	WebDriver driver2;
	

	BCCDeployment deployment = new BCCDeployment();

	public ProjectActions(WebDriver driver2) {
		this.driver2 = driver2;
	}

	// Ready for Review
	public void reviewProject(WebDriver driver2) {
		System.out.println("Selecting ready for review");
		WebElement optSelectAction = driver2.findElement(By
				.cssSelector("[id^=actionOption]"));
		Select drpActions = new Select(optSelectAction);
		drpActions.selectByVisibleText("Ready for Review");
		WebElement btnGo = driver2.findElement(By.partialLinkText("Go"));
		btnGo.click();
		WebElement frmWorkFlow = driver2.findElement(By.id("workflowIframe"));
		deployment.switchToIFrame(driver2, frmWorkFlow);
		// approveContent(driver2, drpActions, btnGo);
		approveContent(driver2);
	}

	// Approve Content
	public void approveContent(WebDriver driver2) {
		System.out.println("Approving Content");
		WebElement optSelectAction = driver2.findElement(By
				.cssSelector("[id^=actionOption]"));
		Select drpActions = new Select(optSelectAction);
		drpActions.selectByVisibleText("Approve Content");
		WebElement btnGo = driver2.findElement(By.partialLinkText("Go"));
		btnGo.click();
		WebElement frmWorkFlow = driver2.findElement(By.id("workflowIframe"));
		deployment.switchToIFrame(driver2, frmWorkFlow);
		String currentUrl = driver2.getCurrentUrl();
		if (currentUrl.contains("hmg")) {
			approveForProductionDeployment(driver2);
		} else
			approveForStagingDeployment(driver2);
	}

	// Reject
	public void reject(WebDriver driver2) {
		System.out.println("Taking Back to Author");
		WebElement optSelectAction = driver2.findElement(By
				.cssSelector("[id^=actionOption]"));
		Select drpActions = new Select(optSelectAction);
		WebElement btnGo = driver2.findElement(By.partialLinkText("Go"));
		drpActions.selectByVisibleText("Reject");
		btnGo.click();
		WebElement frmWorkFlow = driver2.findElement(By.id("workflowIframe"));
		deployment.switchToIFrame(driver2, frmWorkFlow);
	}

	// Delete Project
	public void deleteProject(WebDriver driver2) {
		System.out.println("Deleting Project");
		WebElement optSelectAction = driver2.findElement(By
				.cssSelector("[id^=actionOption]"));
		Select drpActions = new Select(optSelectAction);
		WebElement btnGo = driver2.findElement(By.partialLinkText("Go"));
		drpActions.selectByVisibleText("Delete Project");
		btnGo.click();
		WebElement frmWorkFlow = driver2.findElement(By.id("workflowIframe"));
		deployment.switchToIFrame(driver2, frmWorkFlow);
	}

	// Approve and Deploy to Staging
	public void approveAndDeployToStaging(WebDriver driver2) {
		System.out.println("Approved and directly starting STAGE deployment");
		WebElement optSelectAction = driver2.findElement(By
				.cssSelector("[id^=actionOption]"));
		Select drpActions = new Select(optSelectAction);
		WebElement btnGo = driver2.findElement(By.partialLinkText("Go"));
		drpActions.selectByVisibleText("Approve and Deploy to Staging");
		btnGo.click();
		WebElement frmWorkFlow = driver2.findElement(By.id("workflowIframe"));
		deployment.switchToIFrame(driver2, frmWorkFlow);
	}

	// Approve for Staging Deployment
	public void approveForStagingDeployment(WebDriver driver2) {
		System.out.println("Adding to STAGE ToDo tab");
		WebElement optSelectAction = driver2.findElement(By
				.cssSelector("[id^=actionOption]"));
		Select drpActions = new Select(optSelectAction);
		drpActions.selectByVisibleText("Approve for Staging Deployment");
		WebElement btnGo = driver2.findElement(By.partialLinkText("Go"));
		btnGo.click();
		WebElement frmWorkFlow = driver2.findElement(By.id("workflowIframe"));
		deployment.switchToIFrame(driver2, frmWorkFlow);
	}

	// Reject Staging Deployment
	public void rejectStagingDeployment(WebDriver driver2) {
		System.out.println("Rejecting STAGE Deployment");
		WebElement optSelectAction = driver2.findElement(By
				.cssSelector("[id^=actionOption]"));
		Select drpActions = new Select(optSelectAction);
		WebElement btnGo = driver2.findElement(By.partialLinkText("Go"));
		drpActions.selectByVisibleText("Reject Staging Deployment");
		btnGo.click();
		WebElement frmWorkFlow = driver2.findElement(By.id("workflowIframe"));
		deployment.switchToIFrame(driver2, frmWorkFlow);
	}

	// Accept Staging Deployment
	public void acceptStagingDeployment(WebDriver driver2) {
		System.out.println("Accepting STAGE Deployment");
		WebElement optSelectAction = driver2.findElement(By
				.cssSelector("[id^=actionOption]"));
		Select drpActions = new Select(optSelectAction);
		WebElement btnGo = driver2.findElement(By.partialLinkText("Go"));
		drpActions.selectByVisibleText("Accept Staging Deployment");
		btnGo.click();
		WebElement frmWorkFlow = driver2.findElement(By.id("workflowIframe"));
		deployment.switchToIFrame(driver2, frmWorkFlow);
		approveForProductionDeployment(driver2);
	}

	// Revert Assets on Staging Immediately
	public void revertAssetsOnStagingImmediately(WebDriver driver2) {
		System.out.println("Reverting Assets on STAGE Immediately");
		WebElement optSelectAction = driver2.findElement(By
				.cssSelector("[id^=actionOption]"));
		Select drpActions = new Select(optSelectAction);
		WebElement btnGo = driver2.findElement(By.partialLinkText("Go"));
		drpActions.selectByVisibleText("Revert Assets on Staging Immediately");
		btnGo.click();
		WebElement frmWorkFlow = driver2.findElement(By.id("workflowIframe"));
		deployment.switchToIFrame(driver2, frmWorkFlow);
	}

	// Approve and Deploy to Production
	public void approveAndDeployToProduction(WebDriver driver2) {
		System.out
				.println("Approving and directly starting PRODUCTION deployment");
		WebElement optSelectAction = driver2.findElement(By
				.cssSelector("[id^=actionOption]"));
		Select drpActions = new Select(optSelectAction);
		WebElement btnGo = driver2.findElement(By.partialLinkText("Go"));
		drpActions.selectByVisibleText("Approve and Deploy to Production");
		btnGo.click();
		WebElement frmWorkFlow = driver2.findElement(By.id("workflowIframe"));
		deployment.switchToIFrame(driver2, frmWorkFlow);
	}

	// Approve for Production Deployment
	public void approveForProductionDeployment(WebDriver driver2) {
		System.out.println("Adding to PRODUCTION ToDo tab");
		WebElement optSelectAction = driver2.findElement(By
				.cssSelector("[id^=actionOption]"));
		Select drpActions = new Select(optSelectAction);
		WebElement btnGo = driver2.findElement(By.partialLinkText("Go"));
		drpActions.selectByVisibleText("Approve for Production Deployment");
		btnGo.click();
		WebElement frmWorkFlow = driver2.findElement(By.id("workflowIframe"));
		deployment.switchToIFrame(driver2, frmWorkFlow);
	}

	// Reject Production Deployment
	public void rejectProductionDeployment(WebDriver driver2) {
		System.out.println("Rejecting PRODUCTION Deployment");
		WebElement optSelectAction = driver2.findElement(By
				.cssSelector("[id^=actionOption]"));
		Select drpActions = new Select(optSelectAction);
		WebElement btnGo = driver2.findElement(By.partialLinkText("Go"));
		drpActions.selectByVisibleText("Reject Production Deployment");
		btnGo.click();
		WebElement frmWorkFlow = driver2.findElement(By.id("workflowIframe"));
		deployment.switchToIFrame(driver2, frmWorkFlow);
	}

	// Accept Production Deployment
	public void acceptProductionDeployment(WebDriver driver2) {
		System.out.println("Accepting PRODUCTION Deployment");
		WebElement optSelectAction = driver2.findElement(By
				.cssSelector("[id^=actionOption]"));
		Select drpActions = new Select(optSelectAction);
		WebElement btnGo = driver2.findElement(By.partialLinkText("Go"));
		drpActions.selectByVisibleText("Accept Production Deployment");
		btnGo.click();
		WebElement frmWorkFlow = driver2.findElement(By.id("workflowIframe"));
		deployment.switchToIFrame(driver2, frmWorkFlow);
	}

	// Revert Assets on Production Immediately
	public void revertAssetsOnProductionImmediately(WebDriver driver2) {
		System.out.println("Reverting assets on PRODUCTION Immediately");
		WebElement optSelectAction = driver2.findElement(By
				.cssSelector("[id^=actionOption]"));
		Select drpActions = new Select(optSelectAction);
		WebElement btnGo = driver2.findElement(By.partialLinkText("Go"));
		drpActions
				.selectByVisibleText("Revert Assets on Production Immediately");
		btnGo.click();
		WebElement frmWorkFlow = driver2.findElement(By.id("workflowIframe"));
		deployment.switchToIFrame(driver2, frmWorkFlow);
	}
}
