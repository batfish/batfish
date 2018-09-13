package org.batfish.question.jsonpathtotable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.io.IOException;
import java.util.Iterator;
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
import org.batfish.datamodel.answers.Schema.Type;
import org.batfish.datamodel.questions.Exclusion;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.Row.RowBuilder;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;
import org.batfish.question.jsonpathtotable.JsonPathToTableExtraction.Method;

public class JsonPathToTableAnswerer extends Answerer {

  public JsonPathToTableAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  @SuppressWarnings("deprecation")
  public static TableMetadata create(JsonPathToTableQuestion question) {
    List<ColumnMetadata> columnMetadata = new LinkedList<>();
    for (Entry<String, JsonPathToTableExtraction> entry :
        question.getPathQuery().getExtractions().entrySet()) {
      JsonPathToTableExtraction extraction = entry.getValue();
      if (extraction.getInclude()) {
        columnMetadata.add(
            new ColumnMetadata(
                entry.getKey(),
                extraction.getSchema(),
                extraction.getDescription(),
                extraction.getIsKey(),
                extraction.getIsValue()));
      }
    }
    for (Entry<String, JsonPathToTableComposition> entry :
        question.getPathQuery().getCompositions().entrySet()) {
      JsonPathToTableComposition composition = entry.getValue();
      if (composition.getInclude()) {
        columnMetadata.add(
            new ColumnMetadata(
                entry.getKey(),
                composition.getSchema(),
                composition.getDescription(),
                composition.getIsKey(),
                composition.getIsValue()));
      }
    }
    return new TableMetadata(columnMetadata, question.getDisplayHints());
  }

  /**
   * This procedure proceeds as follows:
   *
   * <ul>
   *   <li>First, compute the inner answer
   *   <li>Then, run {@link #computeAnswerTable(String, JsonPathToTableQuestion)}, which produces a
   *       set of result minus exclusions
   * </ul>
   */
  @Override
  public TableAnswerElement answer() {
    JsonPathToTableQuestion question = (JsonPathToTableQuestion) _question;

    Question innerQuestion = question.getInnerQuestion();
    Answerer innerAnswerer = _batfish.createAnswerer(innerQuestion);
    AnswerElement innerAnswer =
        (innerQuestion.getDifferential()) ? innerAnswerer.answerDiff() : innerAnswerer.answer();

    String innerAnswerStr = null;
    try {
      innerAnswerStr = BatfishObjectMapper.writeStringWithNulls(innerAnswer);
    } catch (IOException e) {
      throw new BatfishException("Could not get JSON string from inner answer", e);
    }

    TableAnswerElement answer = computeAnswerTable(innerAnswerStr, question);

    return answer;
  }

  /**
   * Computes the answer table from the inner answer and the question, excludes rows covered by
   * exclusions, and computes the answer summary
   *
   * @param innerAnswer The string that represents the JSON value of the inner answer
   * @param question The JsonPathToTableQuestion object
   * @return The resulting answer table
   */
  public static TableAnswerElement computeAnswerTable(
      String innerAnswer, JsonPathToTableQuestion question) {

    JsonPathToTableQuery query = question.getPathQuery();
    TableMetadata tableMetadata = create(question);
    TableAnswerElement answer = new TableAnswerElement(tableMetadata);

    // 1. get all the results
    List<JsonPathResult> jsonPathResults =
        JsonPathUtils.getJsonPathResults(query.getPath(), innerAnswer);

    // 2. Put them in the answer element based on whether they are covered by an exclusion
    for (JsonPathResult result : jsonPathResults) {
      Row answerValues =
          computeRowValues(query.getExtractions(), query.getCompositions(), result, tableMetadata);
      Exclusion exclusion = Exclusion.covered(answerValues, question.getExclusions());
      if (exclusion != null) {
        answer.addExcludedRow(answerValues, exclusion.getName());
      } else {
        answer.addRow(answerValues);
      }
    }

    // 3. hydrate the summary
    answer.setSummary(answer.computeSummary(question.getAssertion()));

    return answer;
  }

  private static Row computeRowValues(
      Map<String, JsonPathToTableExtraction> extractions,
      Map<String, JsonPathToTableComposition> compositions,
      JsonPathResult jpResult,
      TableMetadata tableMetadata) {
    ObjectNode answerValues = BatfishObjectMapper.mapper().createObjectNode();
    computeExtractions(extractions, jpResult, answerValues);
    doCompositions(compositions, extractions, answerValues);

    RowBuilder row = Row.builder();
    Iterator<String> iterator = answerValues.fieldNames();
    while (iterator.hasNext()) {
      String columnName = iterator.next();
      if (tableMetadata.containsColumn(columnName)) {
        row.put(columnName, answerValues.get(columnName));
      }
    }
    return row.build();
  }

  private static void computeExtractions(
      Map<String, JsonPathToTableExtraction> extractions,
      JsonPathResult jpResult,
      ObjectNode answerValues) {
    for (Entry<String, JsonPathToTableExtraction> entry : extractions.entrySet()) {
      String varName = entry.getKey();
      JsonPathToTableExtraction extraction = entry.getValue();
      switch (extraction.getMethod()) {
        case PREFIX:
          extractValuesFromPrefix(varName, extraction, jpResult, answerValues);
          break;
        case FUNCOFSUFFIX:
        case PREFIXOFSUFFIX:
        case SUFFIXOFSUFFIX:
          extractValuesFromSuffix(varName, extraction, jpResult, answerValues);
          break;
        default:
          throw new BatfishException("Unknown extraction method " + extraction.getMethod());
      }
    }
  }

  private static void doCompositions(
      Map<String, JsonPathToTableComposition> compositions,
      Map<String, JsonPathToTableExtraction> extractions,
      ObjectNode answerValues) {
    for (Entry<String, JsonPathToTableComposition> cEntry : compositions.entrySet()) {
      String varName = cEntry.getKey();
      JsonPathToTableComposition composition = cEntry.getValue();
      if (composition.getSchema().getType() == Type.LIST
          || composition.getSchema().getType() == Type.SET) {
        doCompositionList(varName, composition, extractions, answerValues);
      } else {
        doCompositionSingleton(varName, composition, answerValues);
      }
    }
  }

  private static void doCompositionList(
      String compositionName,
      JsonPathToTableComposition composition,
      Map<String, JsonPathToTableExtraction> extractions,
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
      if (extractions.get(varName).getSchema().getType() == Type.LIST
          || extractions.get(varName).getSchema().getType() == Type.SET) {
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
        if (extractions.get(varName).getSchema().getType() == Type.LIST
            || extractions.get(varName).getSchema().getType() == Type.SET) {
          object.set(propertyName, varNode.get(index));
        } else {
          object.set(propertyName, varNode);
        }
      }
      confirmValueType(object, composition.getSchema().getBaseType());
      arrayNode.add(object);
    }
    answerValues.set(compositionName, arrayNode);
  }

  private static void doCompositionSingleton(
      String compositionName, JsonPathToTableComposition composition, ObjectNode answerValues) {
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
    confirmValueType(object, composition.getSchema().getBaseType());
    answerValues.set(compositionName, object);
  }

  private static void extractValuesFromPrefix(
      String varName,
      JsonPathToTableExtraction extraction,
      JsonPathResult jpResult,
      ObjectNode answerValues) {
    if (extraction.getSchema().getType() == Type.LIST
        || extraction.getSchema().getType() == Type.SET) {
      throw new BatfishException("Prefix-based hints are incompatible with list or set types");
    }
    answerValues.set(varName, new TextNode(jpResult.getPrefixPart(extraction.getIndex())));
  }

  private static void extractValuesFromSuffix(
      String varName,
      JsonPathToTableExtraction extraction,
      JsonPathResult jpResult,
      ObjectNode answerValues) {
    List<JsonNode> extractedList = new LinkedList<>();
    if (!jpResult.isNullOrEmptySuffix()) {
      switch (extraction.getMethod()) {
        case FUNCOFSUFFIX:
          {
            if (!extraction.getSchema().isIntBased()) {
              throw new BatfishException(
                  "schema must be INT(LIST) with funcofsuffix-based extraction hint");
            }
            Object result =
                JsonPathUtils.computePathFunction(extraction.getFilter(), jpResult.getSuffix());
            if (result != null) {
              if (result instanceof Integer) {
                extractedList.add(new IntNode((Integer) result));
              } else if (result instanceof ArrayNode) {
                for (JsonNode node : (ArrayNode) result) {
                  if (!(node instanceof IntNode)) {
                    throw new BatfishException(
                        "Got non-integer result from path function after filter "
                            + extraction.getFilter());
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
                JsonPathUtils.getJsonPathResults(extraction.getFilter(), jpResult.getSuffix());
            for (JsonPathResult result : filterResults) {
              JsonNode value =
                  (extraction.getMethod() == Method.PREFIXOFSUFFIX)
                      ? new TextNode(result.getPrefixPart(extraction.getIndex()))
                      : result.getSuffix();
              confirmValueType(value, extraction.getSchema().getBaseType());
              extractedList.add(value);
            }
          }
          break;
        default:
          throw new BatfishException("Unknown extraction method " + extraction.getMethod());
      }
      if (extractedList.isEmpty()) {
        throw new BatfishException(
            String.format(
                "Got no results after filtering suffix values of the answer.%nFilter: %s%nJson: %s",
                extraction.getFilter(), jpResult.getSuffix()));
      }
    }

    if (extraction.getSchema().getType() == Type.LIST
        || extraction.getSchema().getType() == Type.SET) {
      answerValues.set(varName, BatfishObjectMapper.mapper().valueToTree(extractedList));
    } else {
      if (extractedList.isEmpty()) {
        answerValues.set(varName, null);
      } else if (extractedList.size() > 1) {
        throw new BatfishException(
            "Got multiple results after filtering suffix values "
                + " of the answer, but the display type is non-list");
      } else {
        answerValues.set(varName, extractedList.get(0));
      }
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
