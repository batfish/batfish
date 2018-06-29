package org.batfish.allinone;

import static org.batfish.datamodel.matchers.RowsMatchers.hasSize;
import static org.batfish.question.aaaauthenticationlogin.AaaAuthenticationLoginQuestionPlugin.AaaAuthenticationAnswerer.COLUMN_LINE_NAMES;
import static org.batfish.question.aaaauthenticationlogin.AaaAuthenticationLoginQuestionPlugin.AaaAuthenticationAnswerer.COLUMN_NODE;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.question.aaaauthenticationlogin.AaaAuthenticationLoginQuestionPlugin.AaaAuthenticationAnswerer;
import org.batfish.question.aaaauthenticationlogin.AaaAuthenticationLoginQuestionPlugin.AaaAuthenticationQuestion;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class AaaAuthenticationTest {

  private static String TESTCONFIGS_PREFIX = "org/batfish/allinone/testconfigs/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private Batfish getBatfishForConfigurationNames(String... configurationNames) throws IOException {
    String[] names =
        Arrays.stream(configurationNames).map(s -> TESTCONFIGS_PREFIX + s).toArray(String[]::new);
    return BatfishTestUtils.getBatfishForTextConfigs(_folder, names);
  }

  @Test
  public void testAnswer() throws IOException {
    String hostname1 = "asaNode";
    String hostname2 = "iosNoAuthentication";
    String hostname3 = "iosRequiresAuthentication";

    Batfish batfish = getBatfishForConfigurationNames(hostname1, hostname2, hostname3);

    AaaAuthenticationQuestion question = new AaaAuthenticationQuestion();
    question.setNodeRegex(new NodesSpecifier("ios.*", true));
    AaaAuthenticationAnswerer answerer = new AaaAuthenticationAnswerer(question, batfish);
    TableAnswerElement answer = answerer.answer();

    assertThat(answer.getRows(), hasSize(1));

    for (Row row : answer.getRows().getData()) {
      // there should only be one row
      assertThat(
          row,
          equalTo(
              Row.of(
                  COLUMN_NODE,
                  new Node(hostname2),
                  COLUMN_LINE_NAMES,
                  Collections.singletonList("aux0"))));
    }
  }
}
