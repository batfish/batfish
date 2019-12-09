package org.batfish.question.f5_bigip;

import static org.batfish.datamodel.matchers.RowMatchers.hasColumn;
import static org.batfish.datamodel.matchers.RowsMatchers.hasSize;
import static org.batfish.datamodel.matchers.TableAnswerElementMatchers.hasRows;
import static org.batfish.question.f5_bigip.F5BigipVipConfigurationAnswerer.COL_DESCRIPTION;
import static org.batfish.question.f5_bigip.F5BigipVipConfigurationAnswerer.COL_NODE;
import static org.batfish.question.f5_bigip.F5BigipVipConfigurationAnswerer.COL_SERVERS;
import static org.batfish.question.f5_bigip.F5BigipVipConfigurationAnswerer.COL_VIRTUAL_ENDPOINT;
import static org.batfish.question.f5_bigip.F5BigipVipConfigurationAnswerer.COL_VIRTUAL_NAME;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.Arrays;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** End-to-end tests of {@link F5BigipVipConfigurationQuestion}. */
public final class F5BigipVipConfigurationTest {

  private static String TESTCONFIGS_PREFIX = "org/batfish/allinone/testconfigs/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private Batfish getBatfishForConfigurationNames(String... configurationNames) throws IOException {
    String[] names =
        Arrays.stream(configurationNames).map(s -> TESTCONFIGS_PREFIX + s).toArray(String[]::new);
    return BatfishTestUtils.getBatfishForTextConfigs(_folder, names);
  }

  @Test
  public void testAnswer() throws IOException {
    String hostname1 = "f5_bigip_vip_configuration";

    Batfish batfish = getBatfishForConfigurationNames(hostname1);

    F5BigipVipConfigurationQuestion question = new F5BigipVipConfigurationQuestion(".*");
    F5BigipVipConfigurationAnswerer answerer =
        new F5BigipVipConfigurationAnswerer(question, batfish);
    TableAnswerElement answer = answerer.answer(batfish.getSnapshot());

    // answer should have one row per virtual
    assertThat(answer.getRows(), hasSize(3));

    assertThat(
        answer,
        hasRows(
            containsInAnyOrder(
                allOf(
                    hasColumn(COL_NODE, equalTo(new Node(hostname1)), Schema.NODE),
                    hasColumn(COL_VIRTUAL_NAME, equalTo("/Common/virtual1"), Schema.STRING),
                    hasColumn(COL_VIRTUAL_ENDPOINT, equalTo("10.0.0.1:80 TCP"), Schema.STRING),
                    hasColumn(
                        COL_SERVERS,
                        equalTo(ImmutableSet.of("172.16.0.1:80", "172.16.0.2:8080")),
                        Schema.set(Schema.STRING)),
                    hasColumn(COL_DESCRIPTION, equalTo("virtual1 is cool"), Schema.STRING)),
                allOf(
                    hasColumn(COL_NODE, equalTo(new Node(hostname1)), Schema.NODE),
                    hasColumn(COL_VIRTUAL_NAME, equalTo("/Common/virtual2"), Schema.STRING),
                    hasColumn(COL_VIRTUAL_ENDPOINT, equalTo("192.0.2.2:80 TCP"), Schema.STRING),
                    hasColumn(
                        COL_SERVERS,
                        equalTo(ImmutableSet.of("10.0.0.2:80")),
                        Schema.set(Schema.STRING)),
                    hasColumn(COL_DESCRIPTION, equalTo("pool2 is lame"), Schema.STRING)),
                allOf(
                    hasColumn(COL_NODE, equalTo(new Node(hostname1)), Schema.NODE),
                    hasColumn(COL_VIRTUAL_NAME, equalTo("/Common/virtual3"), Schema.STRING),
                    hasColumn(COL_VIRTUAL_ENDPOINT, equalTo("192.0.2.3:80 TCP"), Schema.STRING),
                    hasColumn(
                        COL_SERVERS,
                        equalTo(ImmutableSet.of("10.0.0.3:80")),
                        Schema.set(Schema.STRING)),
                    hasColumn(COL_DESCRIPTION, equalTo(""), Schema.STRING)))));
  }
}
