# Questions and Templates

Questions are used to query various information about a network using Batfish.

To ask a question to Batfish service(coordinator), the client should have the template(JSON file) for that question, unless the service is also running along with the client. The question template is populated by the client with the user supplied parameters and then sent over to Batfish service in the payload for the REST API request.

So if we are running Batfish Java client(or any other client) separate from the Batfish service, we need the template files for the questions that we want to ask Batfish.

This directory contains the templates for two categories of questions
1. Stable: These questions are permanent and the current plan is to support them for the future.
2. Experimental: These questions are supported currently but in the future they may be replaced/deprecated or moved to the stable directory.

To use a question template in the client, it needs to be loaded first using the `load-questions` command.
`load-questions` can load the question templates from the Batfish service or from a local directory.

To load question templates from the remote Batfish service run `load-questions` without any parameters:
```java
load-questions
```
In this case the Batfish service(coordinator) should have been started with path of question templates directory passed in the `templatedirs` parameter(for details, see [here](https://github.com/batfish/batfish/wiki/Building-and-running-Batfish-service#installation-steps))

To load question templates from a local directory, run `load-questions` with a local directory path:
```java
load-questions ../batfish/questions/stable
```

To run a question using the loaded templates, Batfish Java client provides the `answer` command. For example:
```java
answer ipowners
```
This command will run the `ipOwners` question on the current testrig and will fetch and print the answer on the Batfish Java client console. Please note that `answer <question-name>` command treats the question-name as case-insensitive.