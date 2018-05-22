package org.batfish.question.ipowners;

import static org.batfish.question.ipowners.IpOwnersAnswerElement.COL_ACTIVE;
import static org.batfish.question.ipowners.IpOwnersAnswerElement.COL_INTERFACE_NAME;
import static org.batfish.question.ipowners.IpOwnersAnswerElement.COL_IP;
import static org.batfish.question.ipowners.IpOwnersAnswerElement.COL_NODE;
import static org.batfish.question.ipowners.IpOwnersAnswerElement.COL_VRFNAME;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multiset;
import java.util.Map;
import java.util.Set;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.Row;

class IpOwnersAnswerer extends Answerer {

  IpOwnersAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  @Override
  public AnswerElement answer() {
    IpOwnersQuestion question = (IpOwnersQuestion) _question;
    Map<String, Configuration> configurations = _batfish.loadConfigurations();
    Map<Ip, Set<String>> ipNodeOwners = CommonUtil.computeIpNodeOwners(configurations, false);
    Map<String, Set<Interface>> interfaces = CommonUtil.computeNodeInterfaces(configurations);

    IpOwnersAnswerElement answerElement = new IpOwnersAnswerElement();

    answerElement.postProcessAnswer(
        _question, generateRows(ipNodeOwners, interfaces, question.getDuplicatesOnly()));
    return answerElement;
  }

  @VisibleForTesting
  static Multiset<Row> generateRows(
      Map<Ip, Set<String>> ipNodeOwners,
      Map<String, Set<Interface>> interfaces,
      boolean duplicatesOnly) {
    Multiset<Row> rows = HashMultiset.create();

    interfaces.forEach(
        (hostname, interfaceSet) ->
            interfaceSet.forEach(
                iface -> {
                  if (iface.getAddress() == null) {
                    return;
                  }
                  if (ipNodeOwners
                              .getOrDefault(iface.getAddress().getIp(), ImmutableSet.of())
                              .size()
                          > 1
                      || !duplicatesOnly) {
                    rows.add(
                        Row.builder()
                            .put(COL_NODE, new Node(hostname))
                            .put(COL_VRFNAME, iface.getVrfName())
                            .put(COL_INTERFACE_NAME, iface.getName())
                            .put(COL_IP, iface.getAddress().getIp())
                            .put(COL_ACTIVE, iface.getActive())
                            .build());
                  }
                }));
    return rows;
  }
}
