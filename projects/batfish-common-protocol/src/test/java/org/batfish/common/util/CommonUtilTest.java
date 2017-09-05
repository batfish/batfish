package org.batfish.common.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

/** Tests for {@link org.batfish.common.util.CommonUtil}. */
public class CommonUtilTest {

  @Test
  public void testWriteStreamToMap() {
    String input = "{question : answer}";
    Map<String, String> expected = new HashMap<>();
    expected.put("question", "answer");
    InputStream stream = new ByteArrayInputStream(input.getBytes());
    Map<String, String> actual = CommonUtil.writeStreamToMap(stream);
    assertThat(actual, equalTo(expected));
  }

}