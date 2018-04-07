package org.batfish.question.jsonpathtotable;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.questions.Exclusion;
import org.batfish.question.jsonpathtotable.JsonPathToTableExtraction.Method;
import org.junit.Test;

public class JsonPathToTableAnswererTest {

  @Test
  public void computeAnswerTable() throws IOException {
    String innerAnswer = "{ 'excludeKey' : 'excludeVal', 'includeKey' : 'includeVal'}";
    String pathQuery = "$.*";

    // build an extraction to recover *Val
    JsonPathToTableExtraction extraction =
        new JsonPathToTableExtraction(new Schema("String"), Method.SUFFIXOFSUFFIX, "$", null);
    Map<String, JsonPathToTableExtraction> extractions = new HashMap<>();
    extractions.put("val", extraction);

    // build a Node composition that use "val" as name
    Map<String, String> dict = new HashMap<>();
    dict.put("name", "val");
    JsonPathToTableComposition composition =
        new JsonPathToTableComposition(new Schema("Node"), dict);
    Map<String, JsonPathToTableComposition> compositions = new HashMap<>();
    compositions.put("node", composition);

    // exclude excludeVal
    Exclusion exclusion =
        new Exclusion(
            null,
            (ObjectNode)
                BatfishObjectMapper.mapper()
                    .createObjectNode()
                    .set("val", new TextNode("excludeVal")));

    JsonPathToTableQuery query = new JsonPathToTableQuery(pathQuery, extractions, compositions);
    JsonPathToTableQuestion question = new JsonPathToTableQuestion(null, query, null);
    question.setExclusions(Collections.singletonList(exclusion));

    JsonPathToTableAnswerElement answer =
        JsonPathToTableAnswerer.computeAnswerTable(innerAnswer, question);

    // there should be one row and one excludedRow
    assertThat(answer.getRows().size(), equalTo(1));
    assertThat(answer.getExcludedRows().size(), equalTo(1));

    // the one row should have includeVal
    ObjectNode row = BatfishObjectMapper.mapper().createObjectNode();
    row.set("val", new TextNode("includeVal"));
    row.set(
        "node",
        BatfishObjectMapper.mapper().createObjectNode().set("name", new TextNode("includeVal")));
    assertThat(answer.getRows().contains(row), equalTo(true));

    // the summary should have the right count
    assertThat(answer.getSummary().getNumResults(), equalTo(1));
  }
}
