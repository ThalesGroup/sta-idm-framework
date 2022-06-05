# sta-idm-connector
Identity Connector for STA over REST API(s)

STA Identity Connctor Framework is built using the connId Framework to develop, manage and run along side with other identity connectors for by direcectional syncronization with other target systems. 


<br><br><br><br>
<br>
<br>
<p align="center">
  <img src="https://github.com/ThalesGroup/sta-idm-framework/edit/master/STA-sync.drawio.png" width="850">
</p>
<br>
<br>
<br>
<br><br><br><br>


Capabilities and Features

| Schema                 | Supported?    |Notes                                        |
| ---------------------- | ------------- |-------------                                |
| Provisioning           | YES           |                                             |
| Live Synchronization   | YES           |Full                                         |
| Password               | NO            |Passwords are not supported directly         |
| Filtering changes      | YES           |limited attribute based                      |
| Paging support         | YES           |Simple Page Results                          |
| Password               | NO            |                                             |


The connector is ment to be compatible with the following IDM Services

1.ConnId
2.Evolveum Midpoint
3.CzechIdM Identity Manager
4.Open ICF


How to use?
<link to video>

How to Build ?
This project depends on the connID framework 

Dependency
Based on the IDM service version being used, use the right version into this connector pom.xml file:

    <dependency>
        <artifactId>connector-rest</artifactId>
        <groupId>com.evolveum.polygon</groupId>
        <version>1.4.2.14-SNAPSHOT</version>
    </dependency>
    
    <dependency>
     <groupId>net.tirasa.connid</groupId>
     <artifactId>connector-framework</artifactId>
     <version>${connId.version}</version>
</dependency>

(adjust the polygon version with the version that you use for connector parent)

Limitations
1. Password syncronization is not directly available.
2. Serach is based on userid and email id only. 
