package org.batfish.question.specifiers;

import static org.batfish.datamodel.matchers.RowMatchers.hasColumn;
import static org.batfish.question.specifiers.SpecifiersAnswerer.COL_IP_SPACE;
import static org.batfish.question.specifiers.SpecifiersAnswerer.COL_LOCATIONS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import java.io.IOException;
import java.util.SortedMap;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.matchers.TableAnswerElementMatchers;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.question.specifiers.SpecifiersQuestion.QueryType;
import org.batfish.specifier.InterfaceLocation;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** End-to-end tests of {@link SpecifiersQuestion}. */
public class SpecifiersTest {
  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private Batfish _batfish;

  private SortedMap<String, Configuration> _configs;

  private InterfaceLocation _interfaceLocation;

  private static final Ip INTERFACE_IP = new Ip("10.0.0.0");

  @Before
  public void setup() throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Configuration c = cb.build();
    Vrf vrf = nf.vrfBuilder().setOwner(c).build();
    Interface iface =
        nf.interfaceBuilder()
            .setVrf(vrf)
            .setOwner(c)
            .setAddress(new InterfaceAddress(INTERFACE_IP, Prefix.MAX_PREFIX_LENGTH))
            .build();
    _interfaceLocation = new InterfaceLocation(iface.getOwner().getHostname(), iface.getName());
    _configs = ImmutableSortedMap.of(c.getHostname(), c);
    _batfish = BatfishTestUtils.getBatfish(_configs, _folder);
  }

  @Test
  public void testIpSpaceDefault() {
    SpecifiersQuestion question = new SpecifiersQuestion(QueryType.IP_SPACE);

    AnswerElement answer = new SpecifiersAnswerer(question, _batfish).answer();
    assertThat(answer, instanceOf(TableAnswerElement.class));

    TableAnswerElement table = (TableAnswerElement) answer;

    assertThat(
        table,
        TableAnswerElementMatchers.hasRows(
            Matchers.contains(
                allOf(
                    hasColumn(
                        COL_IP_SPACE, equalTo(INTERFACE_IP.toIpSpace().toString()), Schema.STRING),
                    hasColumn(
                        COL_LOCATIONS,
                        equalTo(ImmutableSet.of(_interfaceLocation).toString()),
                        Schema.STRING)))));
  }
}
