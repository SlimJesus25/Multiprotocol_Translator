# Multiprotocol Translator

This repository contains a Multiprotocol Middleware Translator as known as PolyglIoT, capable of **redirecting messages
between the MQTT, Kafka, AMQP and DDS communication protocols.**
Allows the User to customize the connection as they please, as well as offering an implementation of **Message Delivery 
Semantics for every protocol.**

## Set up the project

First things first, it is recommended to read PolyglIoT's report to have a better understanding of this project, 
otherwise it is going to be challenging and complex to relate things.

### Software Versions

 | Software                     | Version |
 |------------------------------|---------|
 | Java                         | 11.0.16 |
 | Eclipse Paho MQTT            | 1.2.5   |
 | Apache Kafka                 | 3.2.3   |
 | RabbitMQ                     | 5.9.0   |
 | OpenDDS                      | 3.28    |
 | Orchestrator (Arrowhead)     | 4.6.0   |
 | Authorization (Arrowhead)    | 4.6.0   |
 | Service Registry (Arrowhead) | 4.6.0   |

### Prerequisites

 In order to be able to execute PolyglIoT successfully, there are several software components that need to be installed 
 and configured, as such as:
 - OpenDDS: it is recommended to follow strictly the official OpenDDS's GitHub repository documentation () because it 
might be challenging to set OpenDDS with JNI;
 - Arrowhead: it is recommended to install the three core services (Orchestrator, Authorization and Service Registry);
 - Edge4CPS: it is recommended to follow () to install and configure this software properly.

#### OpenDDS Tips

The following commands represent a simple example on how to install and configure OpenDDS properly. The example is using Linux 
Mint:
1. Download OpenDDS 3.28, save it on "Downloads" directory and change your directory to the downloaded file.
2. Install C++ compiler with "$ sudo apt-get install g++".
3. Install git with "$ sudo apt-get install git".
4. Start configuring and transpiling the files with "$ ./configure --java --compiler /path/to/g++". Use "$ which g++" to 
check the path.
5. "$ make".
6. Retrieve the content from the generated file "setenv.sh" and paste it on the end of "~/.bashrc".
7. Use "$ source ~/.bashrc" to update the current terminal.
8. Change directory to "DDS_ROOT/ACE_Wrappers/bin" and run "$ ./mwc.pl -type gnuace"
9. Run "$ make"
10. Finally, it is also needed to add these jar to the IDE. There are two simple alternatives, add it to the local
 maven repository or add manually the jar files in the project structure. In IntelIJ, go to "Project Structure" > 
 "Dependencies" > "Add new JARs or directories". There are 5 JAR files that should exist and their paths should be:
    - OpenDDS-3.28/lib/tao_java.jar
    - OpenDDS-3.28/lib/OpenDDS_DCPS.jar
    - OpenDDS-3.28/lib/i2jrt_corba.jar
    - OpenDDS-3.28/lib/i2jrt.jar
    - OpenDDS-3.28/java/tests/messenger/messenger_idl/messenger_idl_test.jar 

    <br> To check if everything is properly configured, compilation process won't bother about DDS related classes. It might be
    needed to add the JAR files to classpath, however it is just a click, since IDE is capable of handle that automatically.


## How to run

1. Clone the repository.
2. Follow "Prerequisites" topic's instruction to be able to run PolyglIoT. Even to execute in it most basic form, 
OpenDDS must be installed and configured.
3. There are two alternatives of startup (configurable on "src/main/resources/general_properties.json", 
on "flexible_api" field) and it must be set to false for static execution, or true for dynamic execution:
   1. Run based on a configuration file (static): When PolyglIoT runs statically, it retrieves information 
from "src/main/resources/properties.json" file. Information related to producers, consumers and linkage between 
consumers and producers. Static run is useful when is already known in advance all the relevant information. This allows
PolyglIoT to be more efficient. If there is some kind of update necessity, static alternative is not scalable enough.
   2. Run REST API (dynamic): There is a REST API listening to requests on port 8080 (documentation about end points on
"localhost:8080/swagger-ui/index.html"). This allows Create, Read, Update and Delete (CRUD) in real-time, allowing 
scalability. This option must be used when PolyglIoT is working with Arrowhead and/or Edge4CPS.
4. Still on "general_properties" file, it is relevant to say that information related to "default_brokers" is only 
necessary to the static execution. Here, is indicated, for each communication protocol, the broker IP address and port.
This allows PolyglIoT to create client instances to establish connection with the respective broker(s).
5. There is an important DDS file named "properties" and the "DCPSConfigFile" field must be replaced by the actual path.
6. Related to "DCPSConfigFile", the .ini file (use "tcp.ini" because the used protocol for communication is TCP) must
be properly configured with the machine's IP address and port. 
7. Everything is configured and ready so execute. Start ConsumerMain.

Extra: You might use PubTester or SubTester (both located on "dds" directory) to act as external producer or external 
consumer. Configure "pubSubConf.json" and "arguments.json" to set up topics, amount of messages (to produce), etc.