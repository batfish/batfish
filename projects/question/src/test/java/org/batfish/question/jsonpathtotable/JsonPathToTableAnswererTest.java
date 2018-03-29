package org.batfish.question.jsonpathtotable;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.questions.DisplayHints;
import org.batfish.datamodel.questions.DisplayHints.Composition;
import org.batfish.datamodel.questions.DisplayHints.Extraction;
import org.batfish.datamodel.questions.Exclusion;
import org.junit.Test;

public class JsonPathToTableAnswererTest {

  @Test
  public void computeAnswerTable() throws IOException {
    String innerAnswer = "{ 'excludeKey' : 'excludeVal', 'includeKey' : 'includeVal'}";
    String pathQuery = "$.*";

    // build an extraction to recover *Val
    Extraction extraction = new Extraction();
    extraction.setSchema("String");
    Map<String, JsonNode> map = new HashMap<>();
    map.put("use", new TextNode("suffixofsuffix"));
    map.put("filter", new TextNode("$"));
    extraction.setMethod(map);
    Map<String, Extraction> extractions = new HashMap<>();
    extractions.put("val", extraction);

    // build a Node composition that use "val" as name
    Composition composition = new Composition();
    composition.setSchema("Node");
    Map<String, String> dict = new HashMap<>();
    dict.put("name", "val");
    composition.setDictionary(dict);
    Map<String, Composition> compositions = new HashMap<>();
    compositions.put("node", composition);

    // exclude excludeVal
    Exclusion exclusion =
        new Exclusion(
            null,
            (ObjectNode)
                BatfishObjectMapper.mapper()
                    .createObjectNode()
                    .set("val", new TextNode("excludeVal")));

    DisplayHints dhints = new DisplayHints(compositions, extractions, null);
    JsonPathToTableQuery query = new JsonPathToTableQuery(pathQuery, dhints);
    JsonPathToTableQuestion question = new JsonPathToTableQuestion(null, query, null);
    question.setExclusions(Collections.singletonList(exclusion));

    JsonPathToTableAnswerElement answer =
        JsonPathToTableAnswerer.computeAnswerTable(innerAnswer, question);

    // there should be one row and one excludedRow
    assertThat(answer.getRows().size(), equalTo(1));
    assertThat(answer.getExcludedRows().size(), equalTo(1));

    // the one row should have includeVal
    assertThat(answer.getRows().get(0).get("val"), equalTo(new TextNode("includeVal")));

    // the summary should have the right count
    assertThat(answer.getSummary().getNumResults(), equalTo(1));
  }
}
