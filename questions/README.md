# Batfish Questions and Templates

## Introduction

Questions are used to query information about a network in Batfish.

To ask a question to the Batfish service (coordinator), the client should have the template (JSON file) for that question. The question template is updated by the client with the user supplied parameters and then sent to the Batfish service in the payload for the REST API request.

So if a client is running separately from the Batfish service, corresponding template files are needed for any questions intended to be run through the client.

## How to run questions in Batfish Java client

This directory contains the templates for two categories of questions:
1. Stable: These questions are permanent and the current plan is to support them in the future.
2. Experimental: These questions are supported currently but in the future they may be replaced/deprecated or moved to the stable directory.

### Loading questions

*Assumption*: Batfish service(coordinator) should have been started with the path of question templates directory passed in the `templatedirs` parameter (see [here](https://github.com/batfish/batfish/wiki/Building-and-running-Batfish-service#installation-steps) for details)

To use a question template in the client, it needs to be loaded first. Batfish Java client provides the `load-questions` command to load the question templates.

To load question templates in the Java client run:
```
batfish> load-questions
```

This will load the question templates in the client and will output information about the number of questions loaded, similar to:
```
Loaded 191 questions
Summary: 0 (non-assertion) results;
```

### Running a question

To run a question using the loaded templates, Batfish Java client provides the `answer` command. Run the answer command with the question name, for example:
```java
batfish> answer ipowners
```

This command will run the `ipOwners` question on the current testrig and will fetch and print the answer on the Batfish Java client console. 

Please note that `answer <question-name>` command treats the question-name as case-insensitive.

### Loading local questions from the client

If the client wants to load its own set of questions, `load-questions` command can be run with the directory path containing the question templates, for example:
```java
batfish> load-questions ../batfish/questions/experimental
```
Once the local questions are loaded in the client they can also be run similar to the questions loaded from the Batfish service (coordinator).
