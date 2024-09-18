from openai import OpenAI
from g4f.client import Client
from g4f.Provider import RetryProvider, FreeGpt, Liaobots, You
import asyncio

# Set the event loop policy to WindowsSelectorEventLoopPolicy
asyncio.set_event_loop_policy(asyncio.WindowsSelectorEventLoopPolicy())


initial_prompt = ("""
    "You are ChatGPT, a specialized assistant for generating specific JSON files for PolyglIoT application. "
    "You will receive requests in natural language asking for generating those JSON files. These JSON files structure "
    " are demonstrated in the following example:

"{
  "Producers": [{
    "internal.id": "string", "protocol" : "string",
    "additional.props": { "topic": "string",
      "connection.timeout": int,
      "qos": int,
      "client.id": "string"
    }
  }],
  "Consumers": [{
      "internal.id": "string", "protocol" : "string",
      "additional.props": {
        "topic": "string",
        "client.id" : "string",
        "qos": int
      }
    }
    ],

  "Streams": [{
    "from.producers" : "string",
    "to.consumers" : "string"
    }]
}
It is noteworthy to say that if the "protocol" is rabbitmq, instead of "topic" field from "additional.props" it must be "queue".

Producers, Consumers and Streams might have multiple objects, since they are lists. "internal.id" must be a unique
string between producers and consumers. "protocol" must be either "mqtt", "kafka", "dds" or "rabbitmq". "qos" must be
in range of zero to two (including both). "client.id" and "connection.timeout" must be always "middleware-producer" (
for producers) or "middleware-consumer" (for consumers) and twenty, respectively. "from.producers" and "to.consumers"
are "internal.id", both producers and consumers, specified above. """)

initial_prompt2 = ("""
    "You are ChatGPT, a specialized assistant for generating JSON files for the PolyglIoT application. "
    "You will receive natural language requests for JSON file generation, following this structure:

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

client = Client(provider=RetryProvider([Liaobots, FreeGpt, You], shuffle=True))

print("\n\nChatBot > Hello, I'm ChatBot and I'm here to help you generating a configuration file! Specify your "
      "requirements.")

while True:
    print("\n\nMe (Enter 'exit' to quit) > ")
    req = str(input())
    if req.lower() == "exit":
        break

    response = client.chat.completions.create(
        model="",
        messages=[{"role": "system", "content": initial_prompt2}, {"role": "user", "content": req}]
    )

    full_response = ""

    if hasattr(response, 'choices'):
        for choice in response.choices:  # Iterate over the choices
            if hasattr(choice, 'message'):
                full_response += getattr(choice.message, 'content', '')

    # for choice in response.choices:  # Iterate over the choices
    #     full_response += choice.message.content

    print("\nChatBot > " + full_response) # response.choices[0].message.content
