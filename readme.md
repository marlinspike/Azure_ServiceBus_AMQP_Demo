# Azure_ServiceBus_AMQP_Demo
Demo of using the Advanced Message Queue Protocol with Azure Service Bus Message Queues

To configure:  
All configuration settings are in the servicebus.properties file -  
- [SERVICE_BUS_NAMESPACE] : The name of the Azure Service Bus
- [Queue_Name] : The name of the Service Bus Queue
- [SAS_Policy_Name] : The name of a SAS policy queue. NOTE - The SAS policy must be on the Queue itself, and MUST have BOTH send and receive claims
- [SAS_POLICY_KEY] : Either the Primary or Secondary key of the SAS Policy above