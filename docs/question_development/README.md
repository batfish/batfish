# Question development

The high-level architecture of questions is as follows. What is called via pybatfish (`bf.q.<foo>`) is a JSON template. See example templates [here](https://github.com/batfish/batfish/tree/master/questions/stable). The templates are wrappers around Java-level questions. They contain information on the underlying Java question and on parameters and types. Clients embed parameter values supplied by the user when they send the template to the service. The service creates a Java-level question with the relevant parameters and invokes it to get the answer.

### Adding a new Java question

Adding a new Java question requires extending three bases classes: (1) [QuestionPlugin](https://github.com/batfish/batfish/blob/master/projects/question/src/main/java/org/batfish/question/QuestionPlugin.java) helps Batfish dynamically find and load the question; (2) [Question](https://github.com/batfish/batfish/blob/master/projects/batfish-common-protocol/src/main/java/org/batfish/datamodel/questions/Question.java) is the question object along with its parameters; and (3) [Answerer](https://github.com/batfish/batfish/blob/master/projects/batfish-common-protocol/src/main/java/org/batfish/common/Answerer.java) is the class that produces the answer. 

See existing questions (e.g., [CompareFilters](https://github.com/batfish/batfish/tree/master/projects/question/src/main/java/org/batfish/question/comparefilters)) for example of how to extend these classes. 

NB: the `getName()` method in your subclass of Question must return a String that is unique across all questions. Batfish finds questions using this name. The names are case-insensitive (so, `FooBar` is the same name as `fooBar` and `foobar`).

### Adding a new template

Follow examples of existing templates [here](https://github.com/batfish/batfish/tree/master/questions/stable) and [here](https://github.com/batfish/batfish/tree/master/questions/experimental). You can place your template in either folder or create a new folder. Batfish reads all sub-folders of the [questions folder](https://github.com/batfish/batfish/tree/master/questions/) when loading templates.

## Information about specific questions

* [`FilterLineReachability`](https://pybatfish.readthedocs.io/en/latest/notebooks/filters.html#Filter-Line-Reachability) - the latest algorithmic rewrite is documented in the source tree for `org.batfish.question.filterlinereachability`.
