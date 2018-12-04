package org.batfish.question.filterchange;

import static org.batfish.datamodel.IpAccessListLine.accepting;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.datamodel.matchers.RowMatchers.hasColumn;
import static org.batfish.datamodel.matchers.RowsMatchers.hasSize;
import static org.batfish.datamodel.matchers.TableAnswerElementMatchers.hasRows;
import static org.batfish.question.testfilters.TestFiltersAnswerer.COL_ACTION;
import static org.batfish.question.testfilters.TestFiltersAnswerer.COL_FILTER_NAME;
import static org.batfish.question.testfilters.TestFiltersAnswerer.COL_LINE_CONTENT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import java.io.IOException;
import java.util.SortedMap;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.PacketHeaderConstraints;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.table.TableDiff;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.question.searchfilters.SearchFiltersAnswerer;
import org.batfish.question.searchfilters.SearchFiltersQuestion;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** End-to-end tests of {@link FilterChangeQuestion}. */
public class FilterChangeTest {

  private static final Prefix PRE1 = Prefix.parse("1.1.1.0/24");

  private static final Prefix PRE2 = Prefix.parse("1.1.2.0/24");

  private static final Prefix PRE3 = Prefix.parse("1.1.2.0/23");

  private static final Prefix PRE4 = Prefix.parse("1.1.2.0/25");

  private static final IpAccessList ACL =
      IpAccessList.builder()
          .setName("acl")
          .setLines(ImmutableList.of(accepting().setMatchCondition(matchDst(PRE1)).build()))
          .build();

  private static final IpAccessList ACL_NEW =
      IpAccessList.builder()
          .setName("acl")
          .setLines(
              ImmutableList.of(
                  accepting().setMatchCondition(matchDst(PRE1)).build(),
                  accepting().setMatchCondition(matchDst(PRE2)).build()))
          .build();

  @ClassRule public static TemporaryFolder _tmp = new TemporaryFolder();

  private static Batfish _batfish;

  @BeforeClass
  public static void setup() throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Configuration deltaConfig =
        nf.configurationBuilder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setHostname("A")
            .build();
    deltaConfig.getIpAccessLists().putAll(ImmutableMap.of(ACL.getName(), ACL));

    SortedMap<String, Configuration> deltaConfigurationMap =
        ImmutableSortedMap.of(deltaConfig.getHostname(), deltaConfig);

    Configuration baseConfig =
        nf.configurationBuilder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setHostname("A")
            .build();
    baseConfig.getIpAccessLists().putAll(ImmutableMap.of(ACL_NEW.getName(), ACL_NEW));

    SortedMap<String, Configuration> baseConfigurationMap =
        ImmutableSortedMap.of(baseConfig.getHostname(), baseConfig);

    _batfish = BatfishTestUtils.getBatfish(baseConfigurationMap, deltaConfigurationMap, _tmp);
    SearchFiltersQuestion sfq = SearchFiltersQuestion.builder().build();
    _batfish.registerAnswerer(
        sfq.getName(), sfq.getClass().getCanonicalName(), SearchFiltersAnswerer::new);
  }

  @Test
  public void testAllFlows() {
    FilterChangeQuestion question = new FilterChangeQuestion(ACL.getName(), "A", null, null, false);
    FilterChangeAnswerElement result = answer(question);
    assertThat(result.getRedundantFlows().getRows(), hasSize(1));
    assertThat(
        result.getRedundantFlows(),
        hasRows(
            containsInAnyOrder(
                ImmutableList.of(
                    allOf(
                        hasColumn(COL_ACTION, equalTo("PERMIT"), Schema.STRING),
                        hasColumn(COL_FILTER_NAME, equalTo(ACL.getName()), Schema.STRING),
                        hasColumn(COL_LINE_CONTENT, equalTo("line:0"), Schema.STRING))))));
    assertThat(result.getIncorrectFlows().getRows(), hasSize(1));
    assertThat(
        result.getIncorrectFlows(),
        hasRows(
            containsInAnyOrder(
                ImmutableList.of(
                    allOf(
                        hasColumn(COL_ACTION, equalTo("DENY"), Schema.STRING),
                        hasColumn(COL_FILTER_NAME, equalTo(ACL.getName()), Schema.STRING),
                        hasColumn(COL_LINE_CONTENT, equalTo("no-match"), Schema.STRING))))));
    assertThat(result.getCollateralDamage().getRows(), hasSize(0));
  }

  @Test
  public void testExact() {
    PacketHeaderConstraints headerConstraints =
        PacketHeaderConstraints.builder().setDstIp("" + PRE2).build();
    FilterChangeQuestion question =
        new FilterChangeQuestion(ACL.getName(), "A", headerConstraints, null, false);
    FilterChangeAnswerElement result = answer(question);
    assertThat(result.getRedundantFlows().getRows(), hasSize(0));
    assertThat(result.getIncorrectFlows().getRows(), hasSize(0));
    assertThat(result.getCollateralDamage().getRows(), hasSize(0));
  }

  @Test
  public void testDeny() {
    PacketHeaderConstraints headerConstraints =
        PacketHeaderConstraints.builder().setDstIp("1.1.3.3").build();
    FilterChangeQuestion question =
        new FilterChangeQuestion(ACL.getName(), "A", headerConstraints, LineAction.DENY, false);
    FilterChangeAnswerElement result = answer(question);
    assertThat(result.getRedundantFlows().getRows(), hasSize(1));
    assertThat(
        result.getRedundantFlows(),
        hasRows(
            containsInAnyOrder(
                ImmutableList.of(
                    allOf(
                        hasColumn(COL_ACTION, equalTo("DENY"), Schema.STRING),
                        hasColumn(COL_FILTER_NAME, equalTo(ACL.getName()), Schema.STRING),
                        hasColumn(COL_LINE_CONTENT, equalTo("no-match"), Schema.STRING))))));
    assertThat(result.getIncorrectFlows().getRows(), hasSize(0));
    assertThat(result.getCollateralDamage().getRows(), hasSize(1));
    assertThat(
        result.getCollateralDamage(),
        hasRows(
            containsInAnyOrder(
                ImmutableList.of(
                    allOf(
                        hasColumn(
                            TableDiff.deltaColumnName(COL_ACTION), equalTo("DENY"), Schema.STRING),
                        hasColumn(
                            TableDiff.baseColumnName(COL_ACTION), equalTo("PERMIT"), Schema.STRING),
                        hasColumn(COL_FILTER_NAME, equalTo(ACL.getName()), Schema.STRING),
                        hasColumn(
                            TableDiff.deltaColumnName(COL_LINE_CONTENT),
                            equalTo("no-match"),
                            Schema.STRING),
                        hasColumn(
                            TableDiff.baseColumnName(COL_LINE_CONTENT),
                            equalTo("line:1"),
                            Schema.STRING))))));
  }

  @Test
  public void testMissing() {
    PacketHeaderConstraints headerConstraints =
        PacketHeaderConstraints.builder().setDstIp("" + PRE3).build();
    FilterChangeQuestion question =
        new FilterChangeQuestion(ACL.getName(), "A", headerConstraints, null, false);
    FilterChangeAnswerElement result = answer(question);
    assertThat(result.getRedundantFlows().getRows(), hasSize(0));
    assertThat(result.getIncorrectFlows().getRows(), hasSize(1));
    assertThat(
        result.getIncorrectFlows(),
        hasRows(
            containsInAnyOrder(
                ImmutableList.of(
                    allOf(
                        hasColumn(COL_ACTION, equalTo("DENY"), Schema.STRING),
                        hasColumn(COL_FILTER_NAME, equalTo(ACL.getName()), Schema.STRING),
                        hasColumn(COL_LINE_CONTENT, equalTo("no-match"), Schema.STRING))))));
    assertThat(result.getCollateralDamage().getRows(), hasSize(0));
  }

  @Test
  public void testTooGeneral() {
    PacketHeaderConstraints headerConstraints =
        PacketHeaderConstraints.builder().setDstIp("" + PRE4).build();
    FilterChangeQuestion question =
        new FilterChangeQuestion(ACL.getName(), "A", headerConstraints, LineAction.PERMIT, false);
    FilterChangeAnswerElement result = answer(question);
    assertThat(result.getRedundantFlows().getRows(), hasSize(0));
    assertThat(result.getIncorrectFlows().getRows(), hasSize(0));
    assertThat(result.getCollateralDamage().getRows(), hasSize(1));
    assertThat(
        result.getCollateralDamage(),
        hasRows(
            containsInAnyOrder(
                ImmutableList.of(
                    allOf(
                        hasColumn(
                            TableDiff.deltaColumnName(COL_ACTION), equalTo("DENY"), Schema.STRING),
                        hasColumn(
                            TableDiff.baseColumnName(COL_ACTION), equalTo("PERMIT"), Schema.STRING),
                        hasColumn(COL_FILTER_NAME, equalTo(ACL.getName()), Schema.STRING),
                        hasColumn(
                            TableDiff.deltaColumnName(COL_LINE_CONTENT),
                            equalTo("no-match"),
                            Schema.STRING),
                        hasColumn(
                            TableDiff.baseColumnName(COL_LINE_CONTENT),
                            equalTo("line:1"),
                            Schema.STRING))))));
  }

  private FilterChangeAnswerElement answer(FilterChangeQuestion q) {
    FilterChangeAnswerer answerer = new FilterChangeAnswerer(q, _batfish);
    return answerer.answerDiff();
  }
}
