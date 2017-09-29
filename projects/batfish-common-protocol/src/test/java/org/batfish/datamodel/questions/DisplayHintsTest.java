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

    // here, we only test for ExtractionHint level concepts
    // tests that sit with jsonpath question validate if prefix/suffix filters are parsed correctly

    assertThat(displayHints.getTextDesc().equals("${mynode} has nothing on ${myinterface}"),
        equalTo(true));

    Map<String, ExtractionHint> extractionHints = displayHints.getExtractionHints();
    assertThat(extractionHints.size(), equalTo(3));

    ExtractionHint hint0 = extractionHints.get("node1");
    assertThat(hint0.getValueType().isListType(), equalTo(false));
    assertThat(hint0.getValueType(), equalTo(ValueType.STRING));
    assertThat(hint0.getHints().containsKey("use"), equalTo(true));

    ExtractionHint hint1 = extractionHints.get("interfaces1");
    assertThat(hint1.getValueType().isListType(), equalTo(true));
    assertThat(hint1.getValueType(), equalTo(ValueType.STRINGLIST));
    assertThat(hint1.getHints().containsKey("use"), equalTo(true));

    ExtractionHint hint2 = extractionHints.get("nodes1");
    assertThat(hint2.getValueType().isListType(), equalTo(true));
    assertThat(hint2.getValueType(), equalTo(ValueType.INTLIST));
    assertThat(hint2.getHints().containsKey("use"), equalTo(true));
  }
}
