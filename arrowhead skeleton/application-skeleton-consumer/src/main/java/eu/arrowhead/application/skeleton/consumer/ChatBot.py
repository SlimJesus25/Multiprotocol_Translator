from g4f.client import Client
from g4f.Provider import RetryProvider, FreeGpt, Liaobots, You
import asyncio

# Set the event loop policy to WindowsSelectorEventLoopPolicy
asyncio.set_event_loop_policy(asyncio.WindowsSelectorEventLoopPolicy())

initial_prompt2 = (""""Before anything, answer in english please, I don't understand Chinese.You are ChatGPT, 
a specialized assistant for generating JSON files for the PolyglIoT application. " "You will receive natural language 
requests for JSON file generation, following this structure:

    {
      "Producers": [{"internal.id": "string", "protocol": "string", "additional.props": {"topic": "string", "connection.
      timeout": int, "qos": int, "client.id": "string"}}],
      "Consumers": [{"internal.id": "string", "protocol": "string", "additional.props": {"topic": "string", "client.id":
       "string", "qos": int}}],
      "Streams": [{"from.producers": "string", "to.consumers": "string"}]
    }

    Note: If "protocol" is "rabbitmq", use "queue" instead of "topic" in "additional.props". "Producers" and 
    "Consumers" can have multiple entries. Ensure unique "internal.id" and valid values for "protocol" and "qos". 
    "internal.id" must be a unique string between producers and consumers. "protocol" must be either "mqtt", "kafka", 
    "dds" or "rabbitmq". "qos" must be in range of zero to two (including both). "client.id" and "connection.timeout" 
    must be always "middleware-producer" (for producers) or "middleware-consumer" (for consumers) and twenty, 
    respectively. "from.producers" and "to.consumers" are "internal.id", both producers and consumers, 
    specified above." """)

initial_prompt3 = (""""Before anything, answer in english please, I don't understand Chinese. You are ChatGPT, 
a specialized assistant for helping with pre-requirements to use PolyglIoT application. You will receive requests to 
explain how to configure and install pre-requirements and you are going to base your responses in the following 
text:" "In order to be able to execute PolyglIoT successfully, there are several software components that need to be 
installed and configured, as such as:

OpenDDS: it is recommended to follow strictly the official OpenDDS's GitHub repository documentation () because it 
might be challenging to set OpenDDS with JNI; Arrowhead: it is recommended to install the three core services (
Orchestrator, Authorization and Service Registry); Edge4CPS: it is recommended to follow () to install and configure 
this software properly. OpenDDS Tips The following commands represent a simple example on how to install and 
configure OpenDDS properly. The example is using Linux Mint:

Download OpenDDS 3.28, save it on "Downloads" directory and change your directory to the downloaded file.

Install C++ compiler with "$ sudo apt-get install g++".

Install git with "$ sudo apt-get install git".

Start configuring and transpiling the files with "$ ./configure --java --compiler /path/to/g++". Use "$ which g++" to 
check the path.

"$ make".

Retrieve the content from the generated file "setenv.sh" and paste it on the end of "~/.bashrc".

Use "$ source ~/.bashrc" to update the current terminal.

Change directory to "DDS_ROOT/ACE_Wrappers/bin" and run "$ ./mwc.pl -type gnuace"

Run "$ make"

Finally, it is also needed to add these jar to the IDE. There are two simple alternatives, add it to the local maven 
repository or add manually the jar files in the project structure. In IntelIJ, go to "Project Structure" > 
"Dependencies" > "Add new JARs or directories". There are 5 JAR files that should exist and their paths should be:

OpenDDS-3.28/lib/tao_java.jar
OpenDDS-3.28/lib/OpenDDS_DCPS.jar
OpenDDS-3.28/lib/i2jrt_corba.jar
OpenDDS-3.28/lib/i2jrt.jar
OpenDDS-3.28/java/tests/messenger/messenger_idl/messenger_idl_test.jar

To check if everything is properly configured, compilation process won't bother about DDS related classes. It might 
be needed to add the JAR files to classpath, however it is just a click, since IDE is capable of handle that 
automatically. """)

initial_prompt4 = ("""Before anything, answer in english please, I don't understand Chinese. This operation mode is 
defined as dynamic, because it allows modifications in runtime. For instance, let us assume the there are already two 
producers and one consumer. If a system is interested in produce to the same topic as these, a new (internal) 
consumer is needed. This REST API avoids unnecessary restarts due its dynamic nature. When this operation mode is set 
up, PolyglIoT starts listening to HTTP/S requests on port 8080. It is allowed to create producers/consumers, 
alter producers associated to a consumer, get information about a specific (or all) producer(s)/consumer(s) and 
delete a specific producer/consumer. It is also prepared to handle with input errors with suggestive exceptions being 
threw. This API is prepared for multiple requests accessing to shared resources using fined-grained synchronization. 
The available endpoints are: - PUT -> /api/updateConsumerAssociations - POST -> /api/addProducer - POST -> 
/api/addConsumer - GET -> /api/producersInfo - GET -> /api/producerInfo/{id} - GET -> /api/consumersInfo - GET -> 
/api/consumerInfo/{id} - DELETE -> /api/deleteProducer/{id} - DELETE -> /api/deleteConsumer/{id}

Note: Endpoint documentation is located on “<IP_Address>:8080/swagger-ui/index.html#/”""")

client = Client(provider=RetryProvider([Liaobots, FreeGpt, You], shuffle=True))

while True:
    print("Welcome to PolyglIoT ChatBot assistant.\n"
          "1 - Generate JSON files and comprehend the structure.\n"
          "2 - General help with pre-requirements.\n"
          "3 - Understand how flexible API works.\n"
          "4 - Exit\n\n")

    choice = input("Choice: ")
    context = ""
    first_message = "I'm here to help you generating a configuration file! Specify your requirements."

    if choice == "1":
        context = initial_prompt2
    elif choice == "2":
        context = initial_prompt3
    elif choice == "3":
        context = initial_prompt4
    elif choice == "4":
        exit(0)
    else:
        print("Invalid choice")
        continue

    print("\n\nChatBot > Hello, I'm ChatBot and " + first_message)

    while True:
        print("\n\nMe (Enter 'exit' to quit) > ")
        req = str(input())
        if req.lower() == "exit":
            break

        response = client.chat.completions.create(
            model="",
            messages=[{"role": "system", "content": context}, {"role": "user", "content": req}]
        )

        full_response = ""

        if hasattr(response, 'choices'):
            for choice in response.choices:  # Iterate over the choices
                if hasattr(choice, 'message'):
                    full_response += getattr(choice.message, 'content', '')

        # for choice in response.choices:  # Iterate over the choices
        #     full_response += choice.message.content

        print("\nChatBot > " + full_response)  # response.choices[0].message.content
