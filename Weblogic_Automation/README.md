# Weblogic Automation


### Project Information:-
We always needs to release new code on web-logic instances and for this we are using manual process that is shutdown instnaces, 
deploy ear, start instances, etc. So I have developed this automated script for **Suspend Instances**, **Shutdown Instances**, 
**Deploy Code/ear**, **Start Instances**. Also we can do **Rolling Restart Instances** using this script.


### Pre-requisites:-

1) Java 7 needs be installed.
2) User need to have eclipse installed
3) Git should be installed.
4) Get clone this project on local in eclipse.


### Properties file introduction :-
For running script we need to do some config changes they should be done in **weblogicConfigs.properties** file located at 
/resources/properties/weblogicConfigs.properties.

1) weblogicBaseUrl :- This is the console url of weblogic portal on which you want to do release process.

2) userName :- This should be weblogic's authenticate user name. 

3) password :- This is password property and because of security reasons its value is "XXXXX", so user needs to update this property
   with web-logic user's correct password.   

4) process :- process should be one of this list 'suspend', 'shutdown', 'deployment', 'start' or 'rolling'
  a) suspend :- This process will suspend instances only if they are running. This can be use if you want get instances in 
  ADMIN state only.
  b) shutdown :- This process will shutdown instances only if they are suspended, if not suspended, then this will first suspend
  them and then will do shutdown.
  c) deployment :- This process will do ear deployment on clusters.
  d) start :- This process is used to start instances.
 
5) numberOfInstancesToProcessInLoop :- This is the number which will process instances in loop untill total number achived.
  e.g:- if numberOfInstancesToProcessInLoop is 5 and you have 30 instances then this will process 5 instances each time untill 
  total 30 instances got processed.
  
6) deployments :- This will have list of ear deployments, e.g. deployments will have list of ear deployments such as
   Estore,EstoreCA so in list this Estore and EstoreCA are two different deployments.
  
7) totalInstances :- This is integer property and it's value should be total number of instances available in the domain.


### How to run the script?

1) Set value of **weblogicBaseUrl** with valid weblogic's url

2) Set value of **userName** with valid user name.

3) Set value of **password** with valid password for user name set in step 2.

4) Set value of **process** which you want to execute.

5) How much instances need to be processed in 1 time is need to set as value of **numberOfInstancesToProcessInLoop** 

6) If you are going to do deployment process then you need to set list of **deployments**  

7) Set value of **totalInstances** with number total instances.

8) Run the WeblogicController.java as java application. 


### NOTE:-
Please don't do any manual intervention on web browser window while script(program) execution is going on, other wise program execution might be stopped with StaleElement exception. If this happens then you need to run script again.


### Author :-

Name :- Saurabh Dhumal

Date :- Oct 2017
