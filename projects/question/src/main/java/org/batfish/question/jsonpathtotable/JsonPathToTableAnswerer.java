package org.batfish.question.jsonpathtotable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.batfish.common.Answerer;
import org.batfish.common.BatfishException;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.JsonPathResult;
import org.batfish.common.util.JsonPathUtils;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.AnswerSummary;
import org.batfish.datamodel.questions.DisplayHints;
import org.batfish.datamodel.questions.DisplayHints.Composition;
import org.batfish.datamodel.questions.DisplayHints.Extraction;
import org.batfish.datamodel.questions.Exclusions;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.jsonpath.JsonPathExtractionHint;
import org.batfish.question.jsonpath.JsonPathExtractionHint.UseType;

public class JsonPathToTableAnswerer extends Answerer {

  public JsonPathToTableAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  /**
   * This procedure proceeds as follows:
   *
   * <ul>
   *   <li>First, compute the inner answer
   *   <li>Then, run computeResult, which produces a set of result minus exclusions
   *   <li>Finally, evaluate any assertions and compute summary
   * </ul>
   */
  @Override
  public JsonPathToTableAnswerElement answer() {
    JsonPathToTableQuestion question = (JsonPathToTableQuestion) _question;

    Question innerQuestion = question.getInnerQuestion();
    String innerQuestionName = innerQuestion.getName();
    Answerer innerAnswerer =
        _batfish.getAnswererCreators().get(innerQuestionName).apply(innerQuestion, _batfish);
    AnswerElement innerAnswer =
        (innerQuestion.getDifferential()) ? innerAnswerer.answerDiff() : innerAnswerer.answer();

    String innerAnswerStr = null;
    try {
      innerAnswerStr = BatfishObjectMapper.writeString(innerAnswer);
    } catch (IOException e) {
      throw new BatfishException("Could not get JSON string from inner answer", e);
    }

    JsonPathToTableQuery query = question.getPathQuery();
    JsonPathToTableAnswerElement answerElement =
        new JsonPathToTableAnswerElement(query.computeTableMetadata());

    if (question.getDebug()) {
      answerElement.addDebugInfo("innerAnswer", innerAnswer);
    }

    computeResults(innerAnswerStr, query, question.getExclusions(), answerElement);

    AnswerSummary summary = new AnswerSummary(null, 0, 0, answerElement.getRows().size());
    if (question.getAssertion() != null) {
      if (question.getAssertion().evaluate(answerElement.getRows())) {
        summary.setNumPassed(1);
      } else {
        summary.setNumFailed(1);
      }
    }

    answerElement.setSummary(summary);
    return answerElement;
  }

  public static void computeResults(
      String jsonStr,
      JsonPathToTableQuery query,
      Exclusions exclusions,
      JsonPathToTableAnswerElement answerElement) {

    List<JsonPathResult> jsonPathResults =
        JsonPathUtils.getJsonPathResults(query.getPath(), jsonStr);

    for (JsonPathResult result : jsonPathResults) {
      ObjectNode answerValues = computeAnswerValues(query.getDisplayHints(), result);
      boolean excluded = false;
      if (exclusions != null) {
        for (ObjectNode exclusion : exclusions) {
          if (Exclusions.firstCoversSecond(exclusion, answerValues)) {
            answerElement.addExcludedRow(answerValues, exclusion);
            excluded = true;
            break;
          }
        }
      }
      if (!excluded) {
        answerElement.addRow(answerValues);
      }
    }
  }

  public static ObjectNode computeAnswerValues(DisplayHints displayHints, JsonPathResult jpResult) {
    ObjectNode answerValues = BatfishObjectMapper.mapper().createObjectNode();
    computeExtractions(displayHints.getExtractions(), jpResult, answerValues);
    if (displayHints.getCompositions() != null) {
      doCompositions(displayHints.getCompositions(), displayHints.getExtractions(), answerValues);
    }
    return answerValues;
  }

  private static void computeExtractions(
      Map<String, Extraction> extractions, JsonPathResult jpResult, ObjectNode answerValues) {
    for (Entry<String, Extraction> entry : extractions.entrySet()) {
      String varName = entry.getKey();
      Extraction extraction = entry.getValue();
      JsonPathExtractionHint jpeHint = null;
      try {
        jpeHint = JsonPathExtractionHint.fromExtractionHint(extraction);
      } catch (IOException e) {
        throw new BatfishException(
            "Could not extract JsonPathExtractionHint from ExtractionHint", e);
      }
      switch (jpeHint.getUse()) {
        case PREFIX:
          extractValuesFromPrefix(varName, extraction, jpeHint, jpResult, answerValues);
          break;
        case FUNCOFSUFFIX:
        case PREFIXOFSUFFIX:
        case SUFFIXOFSUFFIX:
          extractValuesFromSuffix(varName, extraction, jpeHint, jpResult, answerValues);
          break;
        default:
          throw new BatfishException("Unknown use type " + jpeHint.getUse());
      }
    }
  }

  private static void doCompositions(
      Map<String, Composition> compositions,
      Map<String, Extraction> extractions,
      ObjectNode answerValues) {
    for (Entry<String, Composition> cEntry : compositions.entrySet()) {
      String varName = cEntry.getKey();
      Composition composition = cEntry.getValue();
      if (composition.getSchemaAsObject().isList()) {
        doCompositionList(varName, composition, extractions, answerValues);
      } else {
        doCompositionSingleton(varName, composition, answerValues);
      }
    }
  }

  private static void doCompositionList(
      String compositionName,
      Composition composition,
      Map<String, Extraction> extractions,
      ObjectNode answerValues) {
    // check if we have any list type extraction variables and listLengths agree
    int listLen = 0;
    for (Entry<String, String> pEntry : composition.getDictionary().entrySet()) {
      String propertyName = pEntry.getKey();
      String varName = pEntry.getValue();
      if (!extractions.containsKey(varName)) {
        throw new BatfishException(
            String.format(
                "varName '%s' for '%s' of '%s' is not in extractions",
                varName, composition.getDictionary().get(varName), compositionName));
      }
      if (extractions.get(varName).getSchemaAsObject().isList()) {
        if (answerValues.get(varName) == null) {
          throw new BatfishException(
              String.format(
                  "varName '%s' for '%s' of '%s' is not in answer values",
                  varName, propertyName, compositionName));
        }
        ArrayNode varNode = (ArrayNode) answerValues.get(varName);
        if (listLen != 0 && listLen != varNode.size()) {
          throw new BatfishException(
              "Found lists of different lengths in values: " + listLen + " " + varNode.size());
        }
        listLen = varNode.size();
      }
    }
    if (listLen == 0) {
      throw new BatfishException("None of the extraction values is a list for " + compositionName);
    }
    ObjectMapper mapper = BatfishObjectMapper.mapper();
    ArrayNode arrayNode = mapper.createArrayNode();
    for (int index = 0; index < listLen; index++) {
      ObjectNode object = mapper.createObjectNode();
      for (Entry<String, String> pEntry : composition.getDictionary().entrySet()) {
        String propertyName = pEntry.getKey();
        String varName = pEntry.getValue();
        JsonNode varNode = answerValues.get(varName);
        if (extractions.get(varName).getSchemaAsObject().isList()) {
          object.set(propertyName, ((ArrayNode) varNode).get(index));
        } else {
          object.set(propertyName, varNode);
        }
      }
      confirmValueType(object, composition.getSchemaAsObject().getBaseType());
      arrayNode.add(object);
    }
    answerValues.set(compositionName, arrayNode);
  }

  private static void doCompositionSingleton(
      String compositionName, Composition composition, ObjectNode answerValues) {
    ObjectMapper mapper = BatfishObjectMapper.mapper();
    ObjectNode object = mapper.createObjectNode();
    for (Entry<String, String> pEntry : composition.getDictionary().entrySet()) {
      String propertyName = pEntry.getKey();
      String varName = pEntry.getValue();
      if (answerValues.get(varName) == null) {
        throw new BatfishException(
            String.format(
                "varName '%s' for property '%s' of composition '%s' is not in display values",
                varName, propertyName, compositionName));
      }
      object.set(propertyName, answerValues.get(varName));
    }
    confirmValueType(object, composition.getSchemaAsObject().getBaseType());
    answerValues.set(compositionName, object);
  }

  private static void extractValuesFromPrefix(
      String varName,
      Extraction extraction,
      JsonPathExtractionHint jpeHint,
      JsonPathResult jpResult,
      ObjectNode answerValues) {
    if (extraction.getSchemaAsObject().isList()) {
      throw new BatfishException("Prefix-based hints are incompatible with list types");
    }
    answerValues.set(varName, new TextNode(jpResult.getPrefixPart(jpeHint.getIndex())));
  }

  private static void extractValuesFromSuffix(
      String varName,
      Extraction extraction,
      JsonPathExtractionHint jpeHint,
      JsonPathResult jpResult,
      ObjectNode answerValues) {
    List<JsonNode> extractedList = new LinkedList<>();
    switch (jpeHint.getUse()) {
      case FUNCOFSUFFIX:
        {
          if (!extraction.getSchemaAsObject().isIntOrIntList()) {
            throw new BatfishException(
                "schema must be INT(LIST) with funcofsuffix-based extraction hint");
          }
          Object result =
              JsonPathUtils.computePathFunction(jpeHint.getFilter(), jpResult.getSuffix());
          if (result != null) {
            if (result instanceof Integer) {
              extractedList.add(new IntNode((Integer) result));
            } else if (result instanceof ArrayNode) {
              for (JsonNode node : (ArrayNode) result) {
                if (!(node instanceof IntNode)) {
                  throw new BatfishException(
                      "Got non-integer result from path function after filter "
                          + jpeHint.getFilter());
                }
                extractedList.add(node);
              }
            } else {
              throw new BatfishException("Unknown result type from computePathFunction");
            }
          }
        }
        break;
      case PREFIXOFSUFFIX:
      case SUFFIXOFSUFFIX:
        {
          List<JsonPathResult> filterResults =
              JsonPathUtils.getJsonPathResults(jpeHint.getFilter(), jpResult.getSuffix());
          for (JsonPathResult result : filterResults) {
            JsonNode value =
                (jpeHint.getUse() == UseType.PREFIXOFSUFFIX)
                    ? new TextNode(result.getPrefixPart(jpeHint.getIndex()))
                    : result.getSuffix();
            confirmValueType(value, extraction.getSchemaAsObject().getBaseType());
            extractedList.add(value);
          }
        }
        break;
      default:
        throw new BatfishException("Unknown UseType " + jpeHint.getUse());
    }
    if (extractedList.size() == 0) {
      throw new BatfishException(
          "Got no results after filtering suffix values of the answer"
              + "\nFilter: "
              + jpeHint.getFilter()
              + "\nJson: "
              + jpResult.getSuffix());
    }

    if (extraction.getSchemaAsObject().isList()) {
      answerValues.set(varName, BatfishObjectMapper.mapper().valueToTree(extractedList));
    } else {
      if (extractedList.size() > 1) {
        throw new BatfishException(
            "Got multiple results after filtering suffix values "
                + " of the answer, but the display type is non-list");
      }
      answerValues.set(varName, extractedList.get(0));
    }
  }

  private static void confirmValueType(JsonNode value, Class<?> baseClass) {
    try {
      BatfishObjectMapper.mapper().readValue(value.toString(), baseClass);
    } catch (IOException e) {
      throw new BatfishException(
          "Could not map extracted value to expected type " + baseClass + "\nValue: " + value, e);
    }
  }
}
