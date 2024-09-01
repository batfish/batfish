# Question development

The high-level architecture of questions is as follows.

What is called via pybtafish (bf.q.<foo>) is a JSON template. See example templates inside the subfolders of [questions in the Batfish repository](https://github.com/batfish/batfish/tree/master/questions).

The template are wrappers around Java-level questions. They contain information on the underlying Java question and on parameters and types. Clients embed parameter values supplied by the user when they send the template to the service. The service creates a Java-level question with the relevant parameters and invokes it to get the answer.

### Adding a new Java question

See an existing question (e.g., `CompareFiltersQuestionPlugin`) for the various classes that are needed, as well as the structure of each class and their relationships among one another.

Small but important note:  the getName() method in your subclass of Question must return a String that is all lowercase. Otherwise clients will not be able to find your question when looking for it on the classpath.

### Adding a new template

Follow examples in the existing templates.

## Information about specific questions

* [`FilterLineReachability`](https://pybatfish.readthedocs.io/en/latest/notebooks/filters.html#Filter-Line-Reachability) - the latest algorithmic rewrite is documented in the source tree for `org.batfish.question.filterlinereachability`.
