package org.batfish.question.aclreachability2;

import static org.batfish.datamodel.answers.AclLines2Rows.createMetadata;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.function.Function;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.answers.AclLines2Rows;
import org.batfish.datamodel.answers.AclSpecs;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.specifier.FilterSpecifier;
import org.batfish.specifier.SpecifierContext;

@ParametersAreNonnullByDefault
public class AclReachability2Answerer extends Answerer {

  public AclReachability2Answerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  @Override
  public TableAnswerElement answer() {
    AclReachability2Question question = (AclReachability2Question) _question;
    AclLines2Rows answerRows = new AclLines2Rows();

    SpecifierContext ctxt = _batfish.specifierContext();
    Set<String> specifiedNodes = question.nodeSpecifier().resolve(ctxt);
    FilterSpecifier filterSpecifier = question.filterSpecifier();

    Map<String, Set<IpAccessList>> specifiedAcls =
        CommonUtil.toImmutableMap(
            specifiedNodes, Function.identity(), node -> filterSpecifier.resolve(node, ctxt));

    SortedMap<String, Configuration> configurations = _batfish.loadConfigurations();
    List<AclSpecs> aclSpecs =
        AclReachabilityAnswererUtils.getAclSpecs(configurations, specifiedAcls, answerRows);
    _batfish.answerAclReachability(aclSpecs, answerRows);
    TableAnswerElement answer = new TableAnswerElement(createMetadata(question));
    answer.postProcessAnswer(question, answerRows.getRows());
    return answer;
  }
}
