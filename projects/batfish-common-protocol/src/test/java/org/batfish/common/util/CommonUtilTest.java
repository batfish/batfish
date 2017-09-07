package org.batfish.common.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;
import org.junit.Test;

/** Tests for {@link org.batfish.common.util.CommonUtil}. */
public class CommonUtilTest {

  @Test public void testWriteReadQuestionsFromStream() {
    String input = "{\"question\" : {\"question\" : \"answer\"}}";
    String expectedQuestion = "{\n  \"question\" : \"answer\"\n}";
    InputStream stream = new ByteArrayInputStream(input.getBytes());
    Map<String, String> actual = CommonUtil.readQuestionsFromStream(stream);
    assertThat(actual.get("question"), equalTo(expectedQuestion));
  }

}