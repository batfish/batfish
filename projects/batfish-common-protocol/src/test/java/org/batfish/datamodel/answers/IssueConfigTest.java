package org.batfish.datamodel.answers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.answers.IssueConfig.MinorIssueConfig;
import org.junit.Test;

public class IssueConfigTest {

  /** Test if IssueConfig if properly serialized and deserialized */
  @Test
  public void testSerDeser() throws IOException {
    String config =
        "{\n"
            + " \"minorIssueConfigs\": [\n"
            + "     {\n"
            + "       \"major\": \"major\",\n"
            + "       \"minor\": \"minor1\",\n"
            + "       \"severity\": 23,\n"
            + "       \"url\" : \"www.cnn.com\""
            + "     },\n"
            + "     {\n"
            + "       \"major\": \"major\",\n"
            + "       \"minor\": \"minor2\",\n"
            + "       \"severity\": 24\n"
            + "     }\n"
            + "   ]\n"
            + "}";

    IssueConfig issueConfig = BatfishObjectMapper.mapper().readValue(config, IssueConfig.class);

    // check if the issue is properly deserialized
    assertThat(
        issueConfig,
        equalTo(
            new IssueConfig(
                ImmutableList.of(
                    new MinorIssueConfig("major", "minor1", 23, "www.cnn.com"),
                    new MinorIssueConfig("major", "minor2", 24, null)))));

    String configSer = BatfishObjectMapper.mapper().writeValueAsString(issueConfig);
    IssueConfig after = BatfishObjectMapper.mapper().readValue(configSer, IssueConfig.class);

    // check serialization
    assertThat(after, equalTo(issueConfig));
  }
}
