# Anypoint Template: SFDC to SFDC Account Broadcast - Push Notification

+ [License Agreement](#licenseagreement)
+ [Use Case](#usecase)
+ [Run it!](#runit)
    * [Running on premise](#runonopremise)
    * [Running on CloudHub](#runoncloudhub)
    * [Properties to be configured](#propertiestobeconfigured)
+ [Customize It!](#customizeit)
    * [config.xml](#configxml)
    * [endpoints.xml](#endpointsxml)
    * [businessLogic.xml](#businesslogicxml)
    * [errorHandling.xml](#errorhandlingxml)
    

# License Agreement <a name="licenseagreement"/>
Note that using this template is subject to the conditions of this [License Agreement](AnypointTemplateLicense.pdf).
Please review the terms of the license before downloading and using this template. In short, you are allowed to use the template for free with Mule ESB Enterprise Edition, CloudHub, or as a trial in Anypoint Studio.

# Use Case <a name="usecase"/>
As a Salesforce admin I want to syncronize accounts between two Salesfoce orgs.

This Anypoint Template should serve as a foundation for setting an online sync of accounts from one SalesForce instance to another in manner of push notification. In this template you have option to decide whether to use the traditional Polling trigger flow which will repeatedly poll source Salesforce org for changes and updates or 
you can decide to use the Push Notification trigger flow which reflects changes in Salesforce source org instantly. Everytime there is a new account or a change in an already existing one, the integration template will recevive notification instantly from Salesforce source instance and it will be responsible for updating the account on the target org.

Requirements have been set not only to be used as examples, but also to establish a starting point to adapt your integration to your requirements.

As implemented, this Anypoint Template leverage the [Batch Module](http://www.mulesoft.org/documentation/display/current/Batch+Processing) and [Outbound messaging](https://www.salesforce.com/us/developer/docs/api/Content/sforce_api_om_outboundmessaging.htm)
The batch job is divided in Input, Process and On Complete stages. The integration is triggered by http inbound connector defined in the flow that is going to trigger the application and executing the batch job with received message from Salesforce source instance.
Outbound messaging in Salesforce allows you to specify that changes to fields within Salesforce can cause messages with field values to be sent to designated external servers.
Outbound messaging is part of the workflow rule functionality in Salesforce. Workflow rules watch for specific kinds of field changes and trigger automatic Salesforce actions in this case sending accounts as an outbound message to Mule Http inbound connector,
which will then further process this message and creates Account in target Salesforce org.

# Run it!

Steps to get SFDC to SFDC Account Broadcast - Push Notification running.

## Running on premise <a name="runonopremise"/>

Using Outbound messaging in Salesforce levarages on sending SOAP messages to a publicly accessible server. So unless you have public IP address you won't be able to receive Salesforce outbound message. 

## Running on CloudHub <a name="runoncloudhub"/>

+ Locate the properties file `mule.dev.properties`, in src/main/resources
+ Complete all the properties required as per the examples in the section [Properties to be configured](#propertiestobeconfigured)
+ Deploy the template on cloudhub [creating your application on CloudHub](http://www.mulesoft.org/documentation/display/current/Hello+World+on+CloudHub) 
+ Once your app is all set and started, you will need to define Salesforce outbound messaging and a simple workflow rule. [This article will show you how to accomplish this](https://www.salesforce.com/us/developer/docs/api/Content/sforce_api_om_outboundmessaging_setting_up.htm)
The most important setting here is the `Endpoint URL` which needs to point to your application running on Cloudbhub, eg. `http://yourapp.cloudhub.io:80`. Additionaly, try to add just few fields to the `Fields to Send` to keep it simple for begin.
Once this all is done every time when you will make a change on Account in source Salesforce org. This account will be sent as a SOAP message to the Http endpoint of running application in Cloudhub.

## Properties to be configured (With examples) <a name="propertiestobeconfigured"/>

In order to use this Anypoint Template you need to configure properties (Credentials, configurations, etc.) either in properties file or in CloudHub as Environment Variables. Detail list with examples:

### Application configuration
+ http.port `9090` 
+ poll.frequencyMillis `60000`
+ poll.startDelayMillis `0`
+ watermark.defaultExpression `YESTERDAY`

#### SalesForce Connector configuration for company A
+ sfdc.a.username `bob.dylan@orga`
+ sfdc.a.password `DylanPassword123`
+ sfdc.a.securityToken `avsfwCUl7apQs56Xq2AKi3X`
+ sfdc.a.url `https://login.salesforce.com/services/Soap/u/28.0`

#### SalesForce Connector configuration for company B
+ sfdc.b.username `joan.baez@orgb`
+ sfdc.b.password `JoanBaez456`
+ sfdc.b.securityToken `ces56arl7apQs56XTddf34X`
+ sfdc.b.url `https://login.salesforce.com/services/Soap/u/28.0`

# Customize It!<a name="customizeit"/>

This brief guide intends to give a high level idea of how this Anypoint Template is built and how you can change it according to your needs.
As mule applications are based on XML files, this page will be organized by describing all the XML that conform the Anypoint Template.
Of course more files will be found such as Test Classes and [Mule Application Files](http://www.mulesoft.org/documentation/display/current/Application+Format), but to keep it simple we will focus on the XMLs.

Here is a list of the main XML files you'll find in this application:

* [config.xml](#configxml)
* [inboundEndpoints.xml](#inboundendpointsxml)
* [businessLogic.xml](#businesslogicxml)
* [errorHandling.xml](#errorhandlingxml)


## config.xml<a name="configxml"/>
Configuration for Connectors and [Properties Place Holders](http://www.mulesoft.org/documentation/display/current/Configuring+Properties) are set in this file. **Even you can change the configuration here, all parameters that can be modified here are in properties file, and this is the recommended place to do it so.** Of course if you want to do core changes to the logic you will probably need to modify this file.

In the visual editor they can be found on the *Global Element* tab.


## businessLogic.xml<a name="businesslogicxml"/>
Functional aspect of the Anypoint Template is implemented on this XML, directed by one flow that will poll for SalesForce creations/updates. The severeal message processors constitute four high level actions that fully implement the logic of this Anypoint Template:

1. During the Input stage the Anypoint Template will go to the source SalesForce org and query all the existing users that match the filter criteria.
2. During the Process stage, each SFDC User will be filtered depending on, if it has an existing matching user in the target Salesforce org.
3. The last step of the Process stage will group the users and create/update them in target Salesforce org.
Finally during the On Complete stage the Anypoint Template will logoutput statistics data into the console.

## endpoints.xml<a name="endpointsxml"/>
This is file is not used in this particular Anypoint Template, but you'll oftenly find flows containing the inbound endpoints to start the integration. Here you can decide whether to use traditional way of using Polling inboud endpoint to repeatedly check source Salesforce org for updates or 
if you intend to deploy your template to Cloudhub you can use the Push Notification trigger flow. **Imporant!** Whether you decide to use the Polling trigger flow or the Push Notification trigger flow, keep in mind that you need manually mark the other trigger flow as **Stopped**.
Leaving both trigger flows as Started can lead to undesired effects.

## errorHandling.xml<a name="errorhandlingxml"/>
Contains a [Catch Exception Strategy](http://www.mulesoft.org/documentation/display/current/Catch+Exception+Strategy) that is only Logging the exception thrown (If so). As you imagine, this is the right place to handle how your integration will react depending on the different exceptions.

