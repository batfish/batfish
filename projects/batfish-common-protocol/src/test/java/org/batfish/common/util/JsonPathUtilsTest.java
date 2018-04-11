package org.batfish.common.util;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.node.TextNode;
import java.util.List;
import org.junit.Test;

public class JsonPathUtilsTest {

  @Test
  public void computePathFunction() {

    String content = "{ 'a' : 'X', 'b' : 'Z' }";
    String query = "$.length()"; // $.a.length() does not work; need to understand

    Object result = JsonPathUtils.computePathFunction(query, content);

    assertThat(result, equalTo(2));
  }

  @Test
  public void getJsonPathResults() {

    String content = "{ 'a' : 'X', 'b' : 'Z' }";
    String query = "$.a";

    List<JsonPathResult> results = JsonPathUtils.getJsonPathResults(query, content);

    assertThat(results.size(), equalTo(1));
    assertThat(results.get(0).getSuffix(), equalTo(new TextNode("X")));
  }
}
