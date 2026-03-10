package org.batfish.datamodel.answers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

public class IssueTest {

  /** Test if Issues are (De)serialiazed properly */
  @Test
  public void testSerDeser() throws IOException {
    String issueStr =
        "{\"explanation\" : \"myex\", \"type\" : { \"major\" : \"maj\", \"minor\": \"min\" },"
            + " \"severity\" : 75, \"url\": \"www.cnn.com\"}";
    Issue issue = BatfishObjectMapper.mapper().readValue(issueStr, Issue.class);

    // check if the issue is properly deserialized
    assertThat(issue.getExplanation(), equalTo("myex"));
    assertThat(issue.getType().getMajor(), equalTo("maj"));
    assertThat(issue.getType().getMinor(), equalTo("min"));
    assertThat(issue.getSeverity(), equalTo(75));
    assertThat(issue.getUrl(), equalTo("www.cnn.com"));

    String issueSer = BatfishObjectMapper.mapper().writeValueAsString(issue);
    ObjectNode issueObj = (ObjectNode) BatfishObjectMapper.mapper().readTree(issueSer);

    // check serialization
    assertThat(issueObj.get("explanation").asText(), equalTo("myex"));
    assertThat(issueObj.get("severity").asInt(), equalTo(75));
    assertThat(issueObj.get("url").asText(), equalTo("www.cnn.com"));
    assertThat(issueObj.get("type").get("major").asText(), equalTo("maj"));
    assertThat(issueObj.get("type").get("minor").asText(), equalTo("min"));
  }
}
