package org.batfish.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import org.batfish.common.BatfishException;
import org.batfish.common.BfConsts;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.questions.InstanceData;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.questions.Variable;

public class QuestionHelper {

  /**
   * Given the JSON representation of a question template and desired values of some parameters in
   * the template, this functions fills out the template with those values. It also fills the
   * template with the provided name.
   *
   * @param questionJson The question template to modify
   * @param parameters The parameters and values to fill
   * @param questionName The name to embed in the template
   * @return An {@link ObjectNode} representation of the filled out template
   */
  public static ObjectNode fillTemplate(
      ObjectNode questionJson, Map<String, JsonNode> parameters, String questionName)
      throws IOException {
    ObjectNode clonedQuestionJson = questionJson.deepCopy();

    ObjectNode instanceJson = (ObjectNode) clonedQuestionJson.get(BfConsts.PROP_INSTANCE);
    instanceJson.put(BfConsts.PROP_INSTANCE_NAME, questionName);
    String instanceDataStr = instanceJson.toString();
    InstanceData instanceData =
        BatfishObjectMapper.mapper()
            .readValue(instanceDataStr, new TypeReference<InstanceData>() {});
    Map<String, Variable> variables = instanceData.getVariables();

    Client.validateAndSet(parameters, variables);
    Client.checkVariableState(variables);

    JsonNode modifiedInstanceData =
        BatfishObjectMapper.mapper().readTree(BatfishObjectMapper.writePrettyString(instanceData));
    clonedQuestionJson.set(BfConsts.PROP_INSTANCE, modifiedInstanceData);

    return clonedQuestionJson;
  }

  public static Question getQuestion(
      String questionTypeStr, Map<String, Supplier<Question>> questions) {
    Supplier<Question> supplier = questions.get(questionTypeStr);
    if (supplier == null) {
      throw new BatfishException("No question found of type: " + questionTypeStr + '.');
    }
    Question question = supplier.get();
    return question;
  }

  public static String getQuestionString(
      String questionTypeStr, Map<String, Supplier<Question>> questions, boolean full) {
    Question question = getQuestion(questionTypeStr, questions);
    if (full) {
      return question.toFullJsonString();
    } else {
      return question.toJsonString();
    }
  }

  /**
   * Validates templates. It first checks if all the variables in template are being exercised by
   * validation (see reason below), and all variable in the templates are used somplace. Then, it
   * checks if the output of {@link #fillTemplate(ObjectNode, Map, String)} can be parsed into a
   * valid Question.
   *
   * @param questionJson The {@link ObjectNode} that represents the question
   * @param parsedParameters The map of parameter name to value
   * @return The {@link Question} after parameter filling
   * @throws IOException If instance data cannot be read from the template or {@link
   *     #fillTemplate(ObjectNode, Map, String)} throws an exception
   */
  static Question validateTemplate(ObjectNode questionJson, Map<String, JsonNode> parsedParameters)
      throws IOException {

    ObjectNode instanceJson = (ObjectNode) questionJson.get(BfConsts.PROP_INSTANCE);
    InstanceData instanceData =
        BatfishObjectMapper.mapper()
            .readValue(instanceJson.toString(), new TypeReference<InstanceData>() {});
    Map<String, Variable> variables = instanceData.getVariables();

    /*
     * We want all variables to be exercised (i.e, assigned values) as part of validation because
     * unexercised parameters and variables are removed as part of template preprocessing in {@link
     * Question#preprocessQuestion(String)} and then we cannot know if the parameter programmed in
     * the template belonged to that of the class.
     */
    Set<String> extraVariables = Sets.difference(variables.keySet(), parsedParameters.keySet());
    if (!extraVariables.isEmpty()) {
      throw new BatfishException(
          "Template validation should exercise all variables. Un-exercised variables: "
              + extraVariables);
    }

    String questionStr = questionJson.toString();
    for (String variable : variables.keySet()) {
      if (!questionStr.contains("${" + variable + "}")) {
        throw new BatfishException("Unused variable: " + variable);
      }
    }

    return Question.parseQuestion(fillTemplate(questionJson, parsedParameters, "qname").toString());
  }
}
