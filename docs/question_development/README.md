# Question development

The high-level architecture of questions is as follows. What is called via pybatfish (`bf.q.<foo>`) is a JSON template. See example templates [here](https://github.com/batfish/batfish/tree/master/questions/stable). The templates are wrappers around Java-level questions. They contain information on the underlying Java question and on parameters and types. Clients embed parameter values supplied by the user when they send the template to the service. The service creates a Java-level question with the relevant parameters and invokes it to get the answer.

### Adding a new Java question

Adding a new Java question requires extending three bases classes: (1) [QuestionPlugin](https://github.com/batfish/batfish/blob/master/projects/question/src/main/java/org/batfish/question/QuestionPlugin.java) helps Batfish dynamically find and load the question; (2) [Question](https://github.com/batfish/batfish/blob/master/projects/common/src/main/java/org/batfish/datamodel/questions/Question.java) is the question object along with its parameters; and (3) [Answerer](https://github.com/batfish/batfish/blob/master/projects/common/src/main/java/org/batfish/common/Answerer.java) is the class that produces the answer. 

See existing questions (e.g., [CompareFilters](https://github.com/batfish/batfish/tree/master/projects/question/src/main/java/org/batfish/question/comparefilters)) for example of how to extend these classes. 

NB: the `getName()` method in your subclass of Question must return a String that is unique across all questions.

### Adding a new template

See existing templates [here](https://github.com/batfish/batfish/tree/master/questions/stable) and [here](https://github.com/batfish/batfish/tree/master/questions/experimental). You can place your template in either folder or create a new folder. Batfish reads all sub-folders of the [questions folder](https://github.com/batfish/batfish/tree/master/questions/) when loading templates.

A question template has several components, which we explain using the [ipOWners](https://github.com/batfish/batfish/blob/master/questions/stable/ipOwners.json) template:

```
{
    "class": "org.batfish.question.ipowners.IpOwnersQuestion",
    "differential": false,
    "ips": "${ips}",
    "duplicatesOnly": "${duplicatesOnly}",
    "instance": {
        "description": "Returns where IP addresses are attached in the network.",
        "instanceName": "ipOwners",
        "longDescription": "For each device, lists the mapping from IPs to corresponding interface(s) and VRF(s).",
        "orderedVariableNames" : [
            "ips",
            "duplicatesOnly"
        ],
        "tags": [
            "configuration"
        ],
        "variables": {
            "ips": {
                "description": "Restrict output to only specified IP addresses",
                "type": "ipSpaceSpec",
                "displayName": "IP specifier",
                "optional": true
            },
            "duplicatesOnly": {
                "description": "Restrict output to only IP addresses that are duplicated (configured on a different node or VRF) in the snapshot",
                "type": "boolean",
                "value": false,
                "displayName": "Duplicates Only"
            }
        }
    }
}
```

The value of the key "class" points to the underlying Java question that will be invoked. The value of the key "differential" indicates if the question is [differential](https://pybatfish.readthedocs.io/en/latest/notebooks/differentialQuestions.html). The keys "ips" and "duplicatesOnly" are parameters of the Java question, and their values indicate how parameter values are assigned. For "ips", the string '"${ips}"' indicates that the parameter value should be assigned based on value of the "ips" template variable. The template variables, whose values are assigned via pybatfish commands such as `bf.q.ipOwners(ips="1.2.3.4")`, are listed under "instance" -> "variables". These variables names can be different from Java question parameters, though many templates maintain the correspondence. In addition to basing Java question parameter values based on template variable values, you can assign constant strings (see [bgpEdges.json](https://github.com/batfish/batfish/blob/master/questions/stable/bgpEdges.json)). 

Inside the "instance" key, "instanceName" corresponds to what you invoke in pybatfish (`ipOwners` in `bf.q.ipOwners`). This name need not be the same as what is returned via `getName()` in the Java question—in fact, you can define multiple templates for the same Java question (e.g., both [bgpEdges.json](https://github.com/batfish/batfish/blob/master/questions/stable/bgpEdges.json) and [ospfEdges](https://github.com/batfish/batfish/blob/master/questions/stable/bgpEdges.json) are templates for the [EdgesQuestion](https://github.com/batfish/batfish/tree/master/projects/question/src/main/java/org/batfish/question/edges)). The remaining top-level keys under "instance" are for documentation and presentation. 

For template variables, the "value" key is used to indicate the default value of the variable (which is used if not assigned by the user when invoking the template), the "optional" key indicates if it is OK for the user to not assign a value at all, the "type" field indicates the variable's type, and "description" and "displayName" contain information for documentation and presentation. 

## Information about specific questions

* [`FilterLineReachability`](https://pybatfish.readthedocs.io/en/latest/notebooks/filters.html#Filter-Line-Reachability) - the latest algorithmic rewrite is documented in the source tree for `org.batfish.question.filterlinereachability`.
