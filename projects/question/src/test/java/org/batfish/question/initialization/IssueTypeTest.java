package org.batfish.question.initialization;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

public class IssueTypeTest {
  @Test
  public void testSerialization() throws IOException {
    String serialized = BatfishObjectMapper.writeString(IssueType.ConvertError);
    assertThat(
        BatfishObjectMapper.mapper().readValue(serialized, IssueType.class),
        equalTo(IssueType.ConvertError));
  }
}
