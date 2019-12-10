package org.batfish.question.aaaauthenticationlogin;

import static org.batfish.datamodel.matchers.RowMatchers.hasColumn;
import static org.batfish.datamodel.matchers.RowsMatchers.hasSize;
import static org.batfish.datamodel.matchers.TableAnswerElementMatchers.hasRows;
import static org.batfish.question.aaaauthenticationlogin.AaaAuthenticationLoginAnswerer.COLUMN_LINE_NAMES;
import static org.batfish.question.aaaauthenticationlogin.AaaAuthenticationLoginAnswerer.COLUMN_NODE;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** End-to-end tests of {@link AaaAuthenticationLoginQuestion}. */
public class AaaAuthenticationLoginTest {

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
    String hostname4 = "juniperAuthenticationOrder";

    Batfish batfish = getBatfishForConfigurationNames(hostname1, hostname2, hostname3, hostname4);

    AaaAuthenticationLoginQuestion question =
        new AaaAuthenticationLoginQuestion("/((ios)|(juniper)).*/");
    AaaAuthenticationLoginAnswerer answerer = new AaaAuthenticationLoginAnswerer(question, batfish);
    TableAnswerElement answer = answerer.answer(batfish.getSnapshot());

    // answer should have exactly one row
    assertThat(answer.getRows(), hasSize(1));

    assertThat(
        answer,
        hasRows(
            contains(
                allOf(
                    hasColumn(COLUMN_NODE, equalTo(new Node(hostname2)), Schema.NODE),
                    hasColumn(
                        COLUMN_LINE_NAMES,
                        equalTo(Collections.singletonList("aux0")),
                        Schema.list(Schema.STRING))))));

    assertThat(
        answer,
        hasRows(not(contains(hasColumn(COLUMN_NODE, equalTo(new Node(hostname1)), Schema.NODE)))));

    assertThat(
        answer,
        hasRows(not(contains(hasColumn(COLUMN_NODE, equalTo(new Node(hostname3)), Schema.NODE)))));
  }
}
