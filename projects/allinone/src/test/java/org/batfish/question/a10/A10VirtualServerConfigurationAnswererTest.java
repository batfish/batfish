package org.batfish.question.a10;

import static org.batfish.datamodel.matchers.RowMatchers.hasColumn;
import static org.batfish.datamodel.matchers.RowsMatchers.hasSize;
import static org.batfish.datamodel.matchers.TableAnswerElementMatchers.hasRows;
import static org.batfish.question.a10.A10VirtualServerConfigurationAnswerer.COL_NODE;
import static org.batfish.question.a10.A10VirtualServerConfigurationAnswerer.COL_SERVERS;
import static org.batfish.question.a10.A10VirtualServerConfigurationAnswerer.COL_SERVICE_GROUP_NAME;
import static org.batfish.question.a10.A10VirtualServerConfigurationAnswerer.COL_SERVICE_GROUP_TYPE;
import static org.batfish.question.a10.A10VirtualServerConfigurationAnswerer.COL_VIRTUAL_SERVER_IP;
import static org.batfish.question.a10.A10VirtualServerConfigurationAnswerer.COL_VIRTUAL_SERVER_NAME;
import static org.batfish.question.a10.A10VirtualServerConfigurationAnswerer.COL_VIRTUAL_SERVER_PORT;
import static org.batfish.question.a10.A10VirtualServerConfigurationAnswerer.COL_VIRTUAL_SERVER_PORT_TYPE_NAME;
import static org.batfish.question.a10.A10VirtualServerConfigurationAnswerer.COL_VIRTUAL_SERVER_TYPE;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.Arrays;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** E2e test for {@link org.batfish.question.a10.A10VirtualServerConfigurationAnswerer} */
public class A10VirtualServerConfigurationAnswererTest {

  private static final String TESTCONFIGS_PREFIX = "org/batfish/allinone/testconfigs/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private Batfish getBatfishForConfigurationNames(String... configurationNames) throws IOException {
    String[] names =
        Arrays.stream(configurationNames).map(s -> TESTCONFIGS_PREFIX + s).toArray(String[]::new);
    return BatfishTestUtils.getBatfishForTextConfigs(_folder, names);
  }

  @Test
  public void testAnswer() throws IOException {
    String hostname1 = "a10_virtual_server_configuration";

    Batfish batfish = getBatfishForConfigurationNames(hostname1);

    A10VirtualServerConfigurationQuestion question =
        new A10VirtualServerConfigurationQuestion(".*");
    A10VirtualServerConfigurationAnswerer answerer =
        new A10VirtualServerConfigurationAnswerer(question, batfish);
    TableAnswerElement answer = answerer.answer(batfish.getSnapshot());

    // answer should have one row per virtual
    assertThat(answer.getRows(), hasSize(1));

    assertThat(
        answer,
        hasRows(
            containsInAnyOrder(
                allOf(
                    hasColumn(COL_NODE, equalTo(new Node(hostname1)), Schema.NODE),
                    hasColumn(COL_VIRTUAL_SERVER_NAME, equalTo("vs1"), Schema.STRING),
                    hasColumn(COL_VIRTUAL_SERVER_IP, equalTo(Ip.parse("10.10.10.1")), Schema.IP),
                    hasColumn(COL_VIRTUAL_SERVER_PORT, equalTo(443), Schema.INTEGER),
                    hasColumn(COL_VIRTUAL_SERVER_TYPE, equalTo("TCP"), Schema.STRING),
                    hasColumn(
                        COL_VIRTUAL_SERVER_PORT_TYPE_NAME, equalTo("vs1.10000"), Schema.STRING),
                    hasColumn(COL_SERVICE_GROUP_NAME, equalTo("vs1.10000"), Schema.STRING),
                    hasColumn(COL_SERVICE_GROUP_TYPE, equalTo("TCP"), Schema.STRING),
                    hasColumn(
                        COL_SERVERS,
                        equalTo(ImmutableSet.of("s1:10000:1.1.1.1", "s2:9999:2.2.2.2")),
                        Schema.set(Schema.STRING))))));
  }
}
