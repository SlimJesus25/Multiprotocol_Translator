# Multiprotocol Translator

This repository contains a Multiprotocol Middleware Translator, capable of redirecting messages between the MQTT, Kafka and AMQP protocols, allowing the User to customize the connection as they please, as well as offering an implementation of Message Delivery Semantics for every protocol (that is not MQTT, since it's already implemented).

The "Multiprotocol Middleware Translator for IoT" document goes over the work done to get to this project, including study, analysis, and a deeper dive over the code that makes it.

## How to run

1. Clone the repository
2. Travel to the "arrowhead skeleton/application-skeleton-consumer" directory and run "mvn install -DskipTests"
3. Once the build is complete, place the resulting .jar (found in resources) in a directory like this:
    - application.jar
    - properties (folder)
      - properties.json
      - general_properties.json

An example of both of these files can be found in the resources folder. If you do not correctly create the folder like this, the application will default to them.

The general_properties file simply needs you to specify if you are using arrowhead (and if so, to fill out the application.properties file beforehand), and to input the default addresses for each broker you are using.

In properties.json, you can list the Producers and Consumers you are using, and between which you wish to create the translation for. The following quote from the accompannying .pdf might help you fill it out:

> **Internal ID** – This field defines a unique identifier for the device so the Middleware can know
which devices are supposed to be connected. The Internal ID is only used inside the
Middleware, it has no connection to the ID of the Producer or Consumer itself.
>
> **Protocol** - Refers to the messaging protocol this device is producing/consuming to/from.
>
> **Additional Props** - These concern the properties of the equivalent consumer/producer the
application will create to communicate with the user’s device, and not necessarily the device
itself (the user’s device may, for example, produce messages with a QoS of 2, but the user may
declare for its Middleware consumer equivalent to receive messages with a QoS of 0) (...)
> 
> **Streams** - With all the equivalent Consumers/Producers created, the application will assign to
each internal Consumer a list of Producers, as specified in this entry.

5. Now run the jar, and start publishing some messages!

If the translations do not seem to be working, you may run the tests to try to identify what's wrong. These use the broker information available at the general_properties file to make sure the translation is happening as it's supposed to. If they don't correctly run, it might just be your brokers that's wrong. (Note that these tests expect an easy broker without authentication. If you do have it, stick to testing with the application itself)

Pay attention to the logs outputted by the application. All the Producers and Consumers will let you know once they succesfully connect. As well as any errors while trying to do so.

## How to add a new protocol (or a new Producer and Consumer)

1. Clone the repository
2. Paste the Classes for your Producer and Consumer of the protocol anywhere in the project. They must contain code to Consume a message and obtain its contents, and to produce a new message.
3. Go to the "MiddlewareSetup" class, under the "eu.arrowhead.application.skeleton.consumer" package.
4. Go to the "createMaps" method, and add or replace your classes. The "key" value is whatever the application should use to identify your protocol. The actual value is the path to your class.
5. Extend the IProducer and IConsumer classes in yours, respectively

    The .pdf has more information about this, but basically:

    - The Map<String,String> that comes in the constructor contains the list of properties defined in the "additional.props" field of the properties.json file.
    - Producer's "produce(topic,message)" method must contain the code to produce a new message to that particular topic (or exchange or whatever) with that particular message.
    - Consumer's "OnMessageReceived(topic,message)" method must be called whenever a new message is to be processed. It will make a call to all the IProducer's linked to this IConsumer to produce a new message.
    - The ConnectionDetails class contains the address and port you are to connect to.
6. Congratulations. You can now translate messages between the MQTT, Kafka, AMQP, and whatever protocol you just added! Or replaced.