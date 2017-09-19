package org.batfish.datamodel.questions;

import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.questions.DisplayHints.ExtractionHint;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class DisplayHintsTest {

  @Test
  public void ExtractionHintTest() throws IOException {
    String text = CommonUtil.readResource("org/batfish/datamodel/questions/extractionHintTest.json");
    ExtractionHint extractionHint;
    BatfishObjectMapper mapper = new BatfishObjectMapper();
    extractionHint = mapper.readValue(text, ExtractionHint.class);
    assertThat(2, equalTo(2));
  }

  @Test
  public void DisplayHintsTest() {
    String text = CommonUtil.readResource("org/batfish/datamodel/questions/displayHintsTest.json");
    DisplayHints displayHints;
    try {
      BatfishObjectMapper mapper = new BatfishObjectMapper();
      displayHints = mapper.readValue(text, DisplayHints.class);
    } catch (IOException e) {
      e.printStackTrace();
    }
    assertThat(2, equalTo(2));
  }
}
