package org.batfish.question;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.service.AutoService;
import com.google.common.base.Strings;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConnectedRoute;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.OspfExternalRoute;
import org.batfish.datamodel.OspfProcess;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.questions.InterfacesSpecifier;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.routing_policy.Environment.Direction;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.question.OspfStatusQuestionPlugin.OspfStatusAnswerElement.OspfStatus;

@AutoService(Plugin.class)
public class OspfStatusQuestionPlugin extends QuestionPlugin {

  public static class OspfStatusAnswerElement extends AnswerElement {

    public enum OspfStatus {
      ENABLED_ACTIVE,
      ENABLED_PASSIVE,
      DISABLED_EXPORTED,
      DISABLED_NOT_EXPORTED
    }

    public static class OspfInfo implements Comparable<OspfInfo> {

      private static final String PROP_INTERFACE = "interface";
      private static final String PROP_OSPF_STATUS = "ospfStatus";

      private NodeInterfacePair _iface;
      private OspfStatus _ospfStatus;

      @JsonCreator
      private OspfInfo(
          @JsonProperty(PROP_INTERFACE) NodeInterfacePair iface,
          @JsonProperty(PROP_OSPF_STATUS) OspfStatus status) {
        _iface = iface;
        _ospfStatus = status;
      }

      public OspfInfo(String hostname, String interfaceName, OspfStatus status) {
        this(new NodeInterfacePair(hostname, interfaceName), status);
      }

      @JsonProperty(PROP_INTERFACE)
      public NodeInterfacePair getInterface() {
        return _iface;
      }

      @JsonProperty(PROP_OSPF_STATUS)
      public OspfStatus getOspfStatus() {
        return _ospfStatus;
      }

      @Override
      public int compareTo(OspfInfo o) {
        return Comparator.comparing(OspfInfo::getInterface)
            .thenComparing(OspfInfo::getOspfStatus)
            .compare(this, o);
      }
    }

    private static final String PROP_OSPF_STATUSES = "ospfStatuses";

    private SortedSet<OspfInfo> _ospfStatuses;

    private OspfStatusAnswerElement() {
      this(null);
    }

    @JsonCreator
    private OspfStatusAnswerElement(
        @JsonProperty(PROP_OSPF_STATUSES) SortedSet<OspfInfo> ospfStatuses) {
      _ospfStatuses = (ospfStatuses == null) ? new TreeSet<>() : ospfStatuses;
    }

    public void add(String hostname, String ifaceName, OspfStatus status) {
      _ospfStatuses.add(new OspfInfo(hostname, ifaceName, status));
    }

    @JsonProperty(PROP_OSPF_STATUSES)
    public SortedSet<OspfInfo> getOspfStatus() {
      return _ospfStatuses;
    }

    @Override
    public String prettyPrint() {
      StringBuilder sb = new StringBuilder("Results for OSPF loopbacks check\n");
      _ospfStatuses.forEach(
          info -> sb.append("  " + info.getInterface() + " " + info.getOspfStatus() + "\n"));
      return sb.toString();
    }
  }

  public static class OspfStatusAnswerer extends Answerer {

    public OspfStatusAnswerer(Question question, IBatfish batfish) {
      super(question, batfish);
    }

    @Override
    public AnswerElement answer() {

      OspfStatusQuestion question = (OspfStatusQuestion) _question;

      OspfStatusAnswerElement answerElement = new OspfStatusAnswerElement();

      Map<String, Configuration> configurations = _batfish.loadConfigurations();
      Set<String> includeNodes = question.getNodeRegex().getMatchingNodes(configurations);

      for (Entry<String, Configuration> e : configurations.entrySet()) {
        String hostname = e.getKey();
        if (!includeNodes.contains(hostname)) {
          continue;
        }
        Configuration c = e.getValue();
        for (Vrf vrf : c.getVrfs().values()) {
          for (Entry<String, Interface> e2 : vrf.getInterfaces().entrySet()) {
            String interfaceName = e2.getKey();
            Interface iface = e2.getValue();
            if (question.getInterfacesSpecifier().matches(iface)) {
              if (iface.getOspfEnabled()) {
                // ospf is running either passively or actively
                if (iface.getOspfPassive()) {
                  if (question.matchesStatus(OspfStatus.ENABLED_PASSIVE)) {
                    answerElement.add(hostname, interfaceName, OspfStatus.ENABLED_PASSIVE);
                  }
                } else {
                  if (question.matchesStatus(OspfStatus.ENABLED_ACTIVE)) {
                    answerElement.add(hostname, interfaceName, OspfStatus.ENABLED_ACTIVE);
                  }
                }
              } else {
                // check if exported as external ospf route
                boolean exported = false;
                OspfProcess proc = vrf.getOspfProcess();
                if (proc != null) {
                  String exportPolicyName = proc.getExportPolicy();
                  if (exportPolicyName != null) {
                    RoutingPolicy exportPolicy = c.getRoutingPolicies().get(exportPolicyName);
                    if (exportPolicy != null) {
                      for (InterfaceAddress address : iface.getAllAddresses()) {
                        ConnectedRoute route =
                            new ConnectedRoute(
                                new Prefix(address.getIp(), address.getNetworkBits()),
                                interfaceName);
                        if (exportPolicy.process(
                            route,
                            new OspfExternalRoute.Builder(),
                            null,
                            vrf.getName(),
                            Direction.OUT)) {
                          exported = true;
                        }
                      }
                    }
                  }
                  if (exported) {
                    if (question.matchesStatus(OspfStatus.DISABLED_EXPORTED)) {
                      answerElement.add(hostname, interfaceName, OspfStatus.DISABLED_EXPORTED);
                    }
                  } else {
                    if (question.matchesStatus(OspfStatus.DISABLED_NOT_EXPORTED)) {
                      answerElement.add(hostname, interfaceName, OspfStatus.DISABLED_NOT_EXPORTED);
                    }
                  }
                }
              }
            }
          }
        }
      }

      return answerElement;
    }
  }

  // <question_page_comment>

  /**
   * Lists the OSPF status of interfaces.
   *
   * <p>When running OSPF, it is a good practice to announce loopbacks interface IPs into OSPF. This
   * question produces the list of nodes for which such announcements are happening.
   *
   * @type OspfStatus onefile
   * @param interfacesSpecifier Expression for interfaces to include. Default value is '.*' (all
   *     interfaces).
   * @param nodeRegex Regular expression for names of nodes to include. Default value is '.*' (all
   *     nodes).
   * @example bf_answer("OspfStatus", nodeRegex='as2.*') Answers the question only for nodes whose
   *     names start with 'as2'.
   */
  public static class OspfStatusQuestion extends Question {

    private static final String PROP_INTERFACES_SPECIFIER = "interfacesSpecifier";

    private static final String PROP_NODE_REGEX = "nodeRegex";

    private static final String PROP_STATUS = "status";

    @Nonnull private InterfacesSpecifier _interfacesSpecifier;

    @Nonnull private NodesSpecifier _nodeRegex;

    @Nonnull private Pattern _statusRegex;

    @JsonCreator
    private OspfStatusQuestion(
        @JsonProperty(PROP_INTERFACES_SPECIFIER) InterfacesSpecifier ifaceSpec,
        @JsonProperty(PROP_NODE_REGEX) NodesSpecifier nodeSpec,
        @JsonProperty(PROP_STATUS) String status) {
      _interfacesSpecifier = (ifaceSpec == null) ? InterfacesSpecifier.ALL : ifaceSpec;
      _nodeRegex = (nodeSpec == null) ? NodesSpecifier.ALL : nodeSpec;
      _statusRegex =
          Strings.isNullOrEmpty(status)
              ? Pattern.compile(".*")
              : Pattern.compile(status.toUpperCase());
    }

    @Override
    public boolean getDataPlane() {
      return false;
    }

    @Override
    public String getName() {
      return "ospfstatus";
    }

    @JsonProperty(PROP_INTERFACES_SPECIFIER)
    public InterfacesSpecifier getInterfacesSpecifier() {
      return _interfacesSpecifier;
    }

    @JsonProperty(PROP_NODE_REGEX)
    public NodesSpecifier getNodeRegex() {
      return _nodeRegex;
    }

    @JsonProperty(PROP_STATUS)
    private String getStatus() {
      return _statusRegex.toString();
    }

    public boolean matchesStatus(OspfStatus status) {
      return _statusRegex.matcher(status.toString()).matches();
    }

    @Override
    public String prettyPrint() {
      String retString =
          String.format(
              "ospfStatus %snodeRegex=\"%s\", interfaceSpecifier=\"%s\", statuses=\"%s\"",
              prettyPrintBase(), _nodeRegex, _interfacesSpecifier, _statusRegex);
      return retString;
    }
  }

  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new OspfStatusAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new OspfStatusQuestion(null, null, null);
  }
}
