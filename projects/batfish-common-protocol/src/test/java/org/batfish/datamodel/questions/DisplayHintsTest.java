package org.batfish.datamodel.questions;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Map;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.questions.DisplayHints.ExtractionHint;
import org.batfish.datamodel.questions.DisplayHints.ValueType;
import org.junit.Test;

public class DisplayHintsTest {

  @Test
  public void displayHintsParsingTest() throws IOException {
    String text = CommonUtil.readResource("org/batfish/datamodel/questions/displayHintsTest.json");
    BatfishObjectMapper mapper = new BatfishObjectMapper();
    DisplayHints displayHints = mapper.readValue(text, DisplayHints.class);

    assertThat(displayHints.getTextDesc().equals("${node1} has no accounting command"),
        equalTo(true));

    Map<String, ExtractionHint> extractionHints = displayHints.getExtractionHints();
    assertThat(extractionHints.size(), equalTo(3));

    // here, we only test for ExtractionHint level concepts
    // tests that sit with jsonpath question validate if prefix/suffix filters are parsed correctly

    ExtractionHint hint0 = extractionHints.get("node1");
    assertThat(hint0.getIsList(), equalTo(false));
    assertThat(hint0.getValueType(), equalTo(ValueType.STRING));
    assertThat(hint0.getHints().containsKey("use"), equalTo(true));

    ExtractionHint hint1 = extractionHints.get("interfaces1");
    assertThat(hint1.getIsList(), equalTo(true));
    assertThat(hint1.getValueType(), equalTo(ValueType.STRING));
    assertThat(hint1.getHints().containsKey("use"), equalTo(true));

    ExtractionHint hint2 = extractionHints.get("nodes1");
    assertThat(hint2.getIsList(), equalTo(true));
    assertThat(hint2.getValueType(), equalTo(ValueType.INT));
    assertThat(hint2.getHints().containsKey("use"), equalTo(true));
  }
}
