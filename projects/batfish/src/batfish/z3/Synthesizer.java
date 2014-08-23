package batfish.z3;

import java.io.File;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;

import batfish.collections.AclIndex;
import batfish.collections.EdgeSet;
import batfish.collections.FibMap;
import batfish.collections.FibRow;
import batfish.collections.HostnameAclNamePair;
import batfish.collections.HostnamePolicyNamePair;
import batfish.collections.InterfaceIndex;
import batfish.collections.NodeIndex;
import batfish.collections.PolicyIndex;
import batfish.collections.PolicyRouteFibIpMap;
import batfish.collections.PolicyRouteFibNodeMap;
import batfish.collections.VarSizeMap;
import batfish.representation.Configuration;
import batfish.representation.Edge;
import batfish.representation.Interface;
import batfish.representation.Ip;
import batfish.representation.IpAccessList;
import batfish.representation.IpAccessListLine;
import batfish.representation.PolicyMap;
import batfish.representation.PolicyMapAction;
import batfish.representation.PolicyMapClause;
import batfish.representation.PolicyMapMatchIpAccessListLine;
import batfish.representation.PolicyMapMatchLine;
import batfish.representation.PolicyMapMatchType;
import batfish.representation.PolicyMapSetLine;
import batfish.representation.PolicyMapSetNextHopLine;
import batfish.representation.PolicyMapSetType;
import batfish.util.SubRange;
import batfish.util.Util;
import batfish.z3.node.AcceptExpr;
import batfish.z3.node.AclDenyExpr;
import batfish.z3.node.IncomingAclInterfaceExpr;
import batfish.z3.node.NoIncomingAclInterfaceExpr;
import batfish.z3.node.NoOutgoingAclInterfaceExpr;
import batfish.z3.node.OutgoingAclInterfaceExpr;
import batfish.z3.node.PolicyDenyExpr;
import batfish.z3.node.PolicyExpr;
import batfish.z3.node.AclMatchExpr;
import batfish.z3.node.AclNoMatchExpr;
import batfish.z3.node.AclPermitExpr;
import batfish.z3.node.AndExpr;
import batfish.z3.node.BooleanExpr;
import batfish.z3.node.Comment;
import batfish.z3.node.DeclareRelExpr;
import batfish.z3.node.DeclareVarExpr;
import batfish.z3.node.DestinationRouteExpr;
import batfish.z3.node.DropExpr;
import batfish.z3.node.EqExpr;
import batfish.z3.node.ExtractExpr;
import batfish.z3.node.FalseExpr;
import batfish.z3.node.FlowSinkExpr;
import batfish.z3.node.IntExpr;
import batfish.z3.node.LitIntExpr;
import batfish.z3.node.NodeAcceptExpr;
import batfish.z3.node.NodeDropExpr;
import batfish.z3.node.NotExpr;
import batfish.z3.node.NotPolicyRoutedExpr;
import batfish.z3.node.OrExpr;
import batfish.z3.node.PacketRelExpr;
import batfish.z3.node.PolicyMatchExpr;
import batfish.z3.node.PolicyNoMatchExpr;
import batfish.z3.node.PolicyPermitExpr;
import batfish.z3.node.PolicyRoutedExpr;
import batfish.z3.node.PostInExpr;
import batfish.z3.node.PostInInterfaceExpr;
import batfish.z3.node.PostOutInterfaceExpr;
import batfish.z3.node.PreInInterfaceExpr;
import batfish.z3.node.PreOutExpr;
import batfish.z3.node.PreOutInterfaceExpr;
import batfish.z3.node.RuleExpr;
import batfish.z3.node.Statement;
import batfish.z3.node.TrueExpr;
import batfish.z3.node.VarIntExpr;

public class Synthesizer {
   private static final String ACL_VAR = "acl";
   public static final String CLAUSE_VAR = "clause";
   public static final String DST_IP_VAR = "dst_ip";
   public static final String DST_PORT_VAR = "dst_port";
   public static final String FAKE_INTERFACE_PREFIX = "TenGigabitEthernet200/";
   public static final String FLOW_SINK_INTERFACE_PREFIX = "TenGigabitEthernet100/";
   public static final String INTERFACE_IN_VAR = "interface_in";
   public static final String INTERFACE_OUT_VAR = "interface_out";
   public static final String INTERFACE_VAR = "interface";
   public static final String IP_PROTOCOL_VAR = "ip_prot";
   private static final String LAST_CLAUSE_VAR = "last_clause";
   private static final String LAST_LINE_VAR = "last_line";
   public static final String LINE_VAR = "line";
   public static final String NODE_ACCEPT_VAR = "node_accept";
   public static final String NODE_DROP_VAR = "node_drop";
   public static final String NODE_IN_VAR = "node_in";
   public static final String NODE_OUT_VAR = "node_out";
   public static final String NODE_VAR = "node";
   public static final List<String> PACKET_VARS = getPacketVars();
   private static final String POLICY_VAR = "policy";
   public static final List<String> POLICY_VARS = getPolicyVars();
   private static final int PORT_BITS = 16;
   private static final int PORT_MAX = 65535;
   private static final int PORT_MIN = 0;
   public static final String PREV_CLAUSE_VAR = "prev_clause";
   public static final String PREV_LINE_VAR = "prev_line";
   public static final String SRC_IP_VAR = "src_ip";
   public static final String SRC_NODE_VAR = "src_node";
   public static final String SRC_PORT_VAR = "src_port";

   public static BooleanExpr bitvectorGEExpr(String bv, long lb, int numBits) {
      // these masks refer to nested conditions, not to bitwise and, or
      int numBitsLeft = numBits;

      BooleanExpr finalExpr = null;
      BooleanExpr currentExpr = null;
      OrExpr currentOrExpr = null;
      AndExpr currentAndExpr = null;
      while (numBitsLeft > 0) {
         // find largest remaining 'subnet mask' not overlapping with lowerbound
         int orSpread = -1;
         long orMask = 0;
         int orStartPos;
         int orEndPos;
         orEndPos = numBitsLeft - 1;
         for (int i = orEndPos; i >= 0; i--) {
            orMask |= (1L << i);
            if ((lb & orMask) != 0) {
               orMask ^= (1L << i);
               break;
            }
            else {
               orSpread++;
               numBitsLeft--;
            }
         }
         if (orSpread >= 0) {
            orStartPos = orEndPos - orSpread;
            LitIntExpr zeroExpr = new LitIntExpr(0L, orStartPos, orEndPos);
            IntExpr extractExpr = newExtractExpr(bv, numBits, orStartPos,
                  orEndPos);
            EqExpr eqExpr = new EqExpr(extractExpr, zeroExpr);
            NotExpr notExpr = new NotExpr(eqExpr);
            OrExpr oldOrExpr = currentOrExpr;
            currentOrExpr = new OrExpr();
            currentOrExpr.addDisjunct(notExpr);
            if (currentExpr != null) {
               if (currentExpr == currentAndExpr) {
                  currentAndExpr.addConjunct(currentOrExpr);
               }
               else if (currentExpr == oldOrExpr) {
                  oldOrExpr.addDisjunct(currentOrExpr);
               }
            }
            else {
               finalExpr = currentOrExpr;
            }
            currentExpr = currentOrExpr;
         }

         // find largest remaining 'subnet mask' not overlapping with lowerbound
         int andSpread = -1;
         long andMask = 0;
         int andStartPos;
         int andEndPos;
         andEndPos = numBitsLeft - 1;
         for (int i = andEndPos; i >= 0; i--) {
            andMask |= (1L << i);
            if ((lb & andMask) != andMask) {
               andMask ^= (1L << i);
               break;
            }
            else {
               andSpread++;
               numBitsLeft--;
            }
         }
         if (andSpread >= 0) {
            andStartPos = andEndPos - andSpread;
            LitIntExpr andMaskExpr = new LitIntExpr(andMask, andStartPos,
                  andEndPos);
            IntExpr extractExpr = newExtractExpr(bv, numBits, andStartPos,
                  andEndPos);
            EqExpr eqExpr = new EqExpr(extractExpr, andMaskExpr);

            AndExpr oldAndExpr = currentAndExpr;
            currentAndExpr = new AndExpr();
            currentAndExpr.addConjunct(eqExpr);
            if (currentExpr != null) {
               if (currentExpr == currentOrExpr) {
                  currentOrExpr.addDisjunct(currentAndExpr);
               }
               else if (currentExpr == oldAndExpr) {
                  oldAndExpr.addConjunct(currentAndExpr);
               }
            }
            else {
               finalExpr = currentAndExpr;
            }
            currentExpr = currentAndExpr;
         }
      }
      return finalExpr;
   }

   public static BooleanExpr bitvectorLEExpr(String bv, long lb, int numBits) {
      OrExpr leExpr = new OrExpr();
      LitIntExpr upperBound = new LitIntExpr(lb, numBits);
      VarIntExpr var = new VarIntExpr(bv);
      EqExpr exactMatch = new EqExpr(var, upperBound);
      BooleanExpr ge = bitvectorGEExpr(bv, lb, numBits);
      NotExpr lessThan = new NotExpr(ge);
      leExpr.addDisjunct(exactMatch);
      leExpr.addDisjunct(lessThan);
      return leExpr;
   }

   private static List<String> getPacketVars() {
      List<String> vars = new ArrayList<String>();
      vars.add(SRC_NODE_VAR);
      vars.add(SRC_IP_VAR);
      vars.add(DST_IP_VAR);
      vars.add(SRC_PORT_VAR);
      vars.add(DST_PORT_VAR);
      vars.add(IP_PROTOCOL_VAR);
      return vars;
   }

   private static List<String> getPolicyVars() {
      List<String> vars = new ArrayList<String>();
      vars.add(SRC_IP_VAR);
      vars.add(DST_IP_VAR);
      vars.add(SRC_PORT_VAR);
      vars.add(DST_PORT_VAR);
      vars.add(IP_PROTOCOL_VAR);
      return vars;
   }

   public static String indent(int n) {
      String output = "";
      for (int i = 0; i < n; i++) {
         output += "   ";
      }
      return output;
   }

   private static boolean isLoopbackInterface(String ifaceName) {
      String lcIfaceName = ifaceName.toLowerCase();
      return lcIfaceName.startsWith("lo");
   }

   private static boolean isNullInterface(String ifaceName) {
      String lcIfaceName = ifaceName.toLowerCase();
      return lcIfaceName.startsWith("null");
   }

   private static IntExpr newExtractExpr(String var, int varSize, int low,
         int high) {
      if (low == 0 && high == varSize - 1) {
         return new VarIntExpr(var);
      }
      else {
         return new ExtractExpr(var, low, high);
      }
   }

   private final AclIndex _aclIndex;

   private final int _aclLineWidth;

   private final int _aclWidth;

   private final Map<String, Configuration> _configurations;

   private final FibMap _fibs;

   private final InterfaceIndex _interfaceIndex;

   private final int _interfaceWidth;

   private final int _maxAclLine;

   private final int _maxPolicyClause;

   private final NodeIndex _nodeIndex;

   private final int _nodeWidth;

   private final int _policyClauseWidth;

   private final PolicyIndex _policyIndex;

   private final int _policyWidth;

   private final PolicyRouteFibNodeMap _prFibs;

   private final boolean _simplify;

   private final EdgeSet _topologyEdges;

   private final Map<String, Set<Interface>> _topologyInterfaces;

   private final VarSizeMap _varSizes;

   public Synthesizer(Map<String, Configuration> configurations, FibMap fibs,
         PolicyRouteFibNodeMap prFibs, EdgeSet topologyEdges, boolean simplify) {
      _configurations = configurations;
      _fibs = fibs;
      _topologyEdges = topologyEdges;
      _prFibs = prFibs;
      _simplify = simplify;
      _topologyInterfaces = new TreeMap<String, Set<Interface>>();
      computeTopologyInterfaces();
      _varSizes = new VarSizeMap();
      _nodeIndex = new NodeIndex();
      initNodeIndex();
      _nodeWidth = Util.intWidth(_nodeIndex.size());
      _interfaceIndex = new InterfaceIndex();
      initInterfaceIndex();
      _interfaceWidth = Util.intWidth(_interfaceIndex.size());
      _maxAclLine = getMaxAclLine();
      _aclLineWidth = Util.intWidth(_maxAclLine);
      _maxPolicyClause = getMaxPolicyClause();
      _policyClauseWidth = Util.intWidth(_maxPolicyClause);
      _aclIndex = new AclIndex();
      initAclIndex();
      _aclWidth = Util.intWidth(_aclIndex.size());
      _policyIndex = new PolicyIndex();
      initPolicyIndex();
      _policyWidth = Util.intWidth(_policyIndex.size());
      initVarSizes();
   }

   private void computeTopologyInterfaces() {
      for (Edge edge : _topologyEdges) {
         String hostname = edge.getNode1();
         if (!_topologyInterfaces.containsKey(hostname)) {
            _topologyInterfaces.put(hostname, new TreeSet<Interface>());
         }
         Set<Interface> interfaces = _topologyInterfaces.get(hostname);
         String interfaceName = edge.getInt1();
         if (interfaceName.startsWith(FAKE_INTERFACE_PREFIX)) {
            continue;
         }
         Interface i = _configurations.get(hostname).getInterfaces()
               .get(interfaceName);
         interfaces.add(i);
      }
      for (String hostname : _configurations.keySet()) {
         Configuration c = _configurations.get(hostname);
         Map<String, Interface> nodeInterfaces = c.getInterfaces();
         for (String ifaceName : nodeInterfaces.keySet()) {
            if (ifaceName.startsWith(FLOW_SINK_INTERFACE_PREFIX)) {
               Interface iface = nodeInterfaces.get(ifaceName);
               if (!_topologyInterfaces.containsKey(hostname)) {
                  _topologyInterfaces.put(hostname, new TreeSet<Interface>());
               }
               Set<Interface> interfaces = _topologyInterfaces.get(hostname);
               interfaces.add(iface);
            }
         }
      }
   }

   private List<Statement> getAcceptRules() {
      List<Statement> statements = new ArrayList<Statement>();
      statements.add(new Comment("Node accept lead to universal accept"));
      NodeAcceptExpr nodeAccept = new NodeAcceptExpr(NODE_VAR);
      RuleExpr connectAccepts = new RuleExpr(nodeAccept, AcceptExpr.INSTANCE);
      statements.add(connectAccepts);
      return statements;
   }

   private IntExpr getAclNumber(String hostname, String aclName) {
      int aclNum = _aclIndex.get(new HostnameAclNamePair(hostname, aclName));
      return new LitIntExpr(aclNum, _aclWidth);
   }

   private List<Statement> getDestRouteToPreOutIfaceRules() {
      List<Statement> statements = new ArrayList<Statement>();
      statements
            .add(new Comment(
                  "Rules for sending destination routed packets to preoutIface stage"));
      for (String hostname : _fibs.keySet()) {
         TreeSet<FibRow> fibSet = _fibs.get(hostname);
         FibRow firstRow = fibSet.first();
         if (firstRow.getPrefix().asLong() != 0) {
            // no default route, so add one that drops traffic
            FibRow dropDefaultRow = new FibRow(new Ip(0), 0,
                  FibRow.DROP_INTERFACE);
            fibSet.add(dropDefaultRow);
         }
         FibRow[] fib = fibSet.toArray(new FibRow[] {});
         for (int i = 0; i < fib.length; i++) {
            FibRow currentRow = fib[i];
            if (currentRow.getInterface().startsWith(FAKE_INTERFACE_PREFIX)) {
               continue;
            }
            Set<FibRow> notRows = new TreeSet<FibRow>();
            for (int j = i + 1; j < fib.length; j++) {
               FibRow specificRow = fib[j];
               long currentStart = currentRow.getPrefix().asLong();
               long currentEnd = currentRow.getLastIp().asLong();
               long specificStart = specificRow.getPrefix().asLong();
               long specificEnd = specificRow.getLastIp().asLong();
               // check whether later prefix is contained in this one
               if (currentStart <= specificStart && specificEnd <= currentEnd) {
                  if (currentStart == specificStart
                        && currentEnd == specificEnd) {
                     // load balancing
                     continue;
                  }
                  if (currentRow.getInterface().equals(
                        specificRow.getInterface())) {
                     // no need to exclude packets matching the more specific
                     // prefix,
                     // since they would go out same interface
                     continue;
                  }
                  // exclude packets that match a more specific prefix that
                  // would go out a different interface
                  notRows.add(specificRow);
               }
               else {
                  break;
               }
            }
            AndExpr conditions = new AndExpr();
            DestinationRouteExpr destRoute = new DestinationRouteExpr(NODE_VAR);
            conditions.addConjunct(destRoute);
            String ifaceOutName = currentRow.getInterface();
            PacketRelExpr action;
            EqExpr nodeMatches = matchNodeVar(hostname);
            conditions.addConjunct(nodeMatches);
            if (ifaceOutName.equals(FibRow.DROP_INTERFACE)
                  || isLoopbackInterface(ifaceOutName)
                  || isNullInterface(ifaceOutName)) {
               action = new NodeDropExpr(NODE_VAR);
            }
            else {
               action = new PreOutInterfaceExpr(NODE_VAR, INTERFACE_VAR);
               EqExpr interfaceMatches = matchInterfaceVar(ifaceOutName);
               conditions.addConjunct(interfaceMatches);
            }

            // must not match more specific routes
            for (FibRow notRow : notRows) {
               int prefixLength = notRow.getPrefixLength();
               long prefix = notRow.getPrefix().asLong();
               int first = 32 - prefixLength;
               if (first >= 32) {
                  continue;
               }
               int last = 31;
               LitIntExpr prefixFragmentLit = new LitIntExpr(prefix, first,
                     last);
               IntExpr prefixFragmentExt = newExtractExpr(DST_IP_VAR, first,
                     last);
               NotExpr noPrefixMatch = new NotExpr();
               EqExpr prefixMatch = new EqExpr(prefixFragmentExt,
                     prefixFragmentLit);
               noPrefixMatch.SetArgument(prefixMatch);
               conditions.addConjunct(noPrefixMatch);
            }

            // must match route
            int prefixLength = currentRow.getPrefixLength();
            long prefix = currentRow.getPrefix().asLong();
            int first = 32 - prefixLength;
            if (first < 32) {
               int last = 31;
               LitIntExpr prefixFragmentLit = new LitIntExpr(prefix, first,
                     last);
               IntExpr prefixFragmentExt = newExtractExpr(DST_IP_VAR, first,
                     last);
               EqExpr prefixMatch = new EqExpr(prefixFragmentExt,
                     prefixFragmentLit);
               conditions.addConjunct(prefixMatch);
            }

            // then we forward out specified interface (or drop)
            RuleExpr rule = new RuleExpr(conditions, action);
            statements.add(rule);
         }
      }
      return statements;
   }

   private List<Statement> getDropRules() {
      List<Statement> statements = new ArrayList<Statement>();
      statements.add(new Comment("Node drop lead to universal drop"));
      NodeDropExpr nodeDrop = new NodeDropExpr(NODE_VAR);
      RuleExpr connectDrops = new RuleExpr(nodeDrop, DropExpr.INSTANCE);
      statements.add(connectDrops);
      return statements;
   }

   private List<Statement> getFlowSinkAcceptRules() {
      List<Statement> statements = new ArrayList<Statement>();
      statements.add(new Comment("Flow sink interfaces"));
      OrExpr flowSinkConditions = new OrExpr();
      for (String hostname : _configurations.keySet()) {
         Configuration c = _configurations.get(hostname);
         for (String ifaceName : c.getInterfaces().keySet()) {
            if (ifaceName.startsWith(FLOW_SINK_INTERFACE_PREFIX)) {
               AndExpr bothMatch = new AndExpr();
               EqExpr nodeMatches = matchNodeVar(hostname);
               EqExpr interfaceMatches = matchInterfaceVar(ifaceName);
               bothMatch.addConjunct(nodeMatches);
               bothMatch.addConjunct(interfaceMatches);
               flowSinkConditions.addDisjunct(bothMatch);
            }
         }
      }
      FlowSinkExpr f = new FlowSinkExpr(NODE_VAR, INTERFACE_VAR);
      RuleExpr flowSinks = new RuleExpr(flowSinkConditions, f);
      statements.add(flowSinks);

      statements.add(new Comment(
            "Post out flow sink interface leads to node accept"));
      AndExpr acceptConditions = new AndExpr();
      PostOutInterfaceExpr postOutIface = new PostOutInterfaceExpr(NODE_VAR,
            INTERFACE_VAR);
      acceptConditions.addConjunct(f);
      acceptConditions.addConjunct(postOutIface);
      NodeAcceptExpr nodeAccept = new NodeAcceptExpr(NODE_VAR);
      RuleExpr flowSinkAccept = new RuleExpr(acceptConditions, nodeAccept);
      statements.add(flowSinkAccept);
      return statements;
   }

   private IntExpr getInterfaceNumber(String iface) {
      int interfaceNumber = _interfaceIndex.get(iface);
      return new LitIntExpr(interfaceNumber, _interfaceWidth);
   }

   public InterfaceIndex getInterfaceNumbers() {
      return _interfaceIndex;
   }

   private List<Statement> getMatchAclRules() {
      List<Statement> statements = new ArrayList<Statement>();
      Comment comment = new Comment("Rules for how packets can match acl lines");
      statements.add(comment);
      Map<String, Map<String, IpAccessList>> matchAcls = new TreeMap<String, Map<String, IpAccessList>>();
      // first we find out which acls we need to process
      for (String hostname : _topologyInterfaces.keySet()) {
         Map<String, IpAccessList> aclMap = new TreeMap<String, IpAccessList>();
         Set<Interface> interfaces = _topologyInterfaces.get(hostname);
         for (Interface iface : interfaces) {
            if (iface.getIP() != null) {
               IpAccessList aclIn = iface.getIncomingFilter();
               IpAccessList aclOut = iface.getOutgoingFilter();
               PolicyMap routePolicy = iface.getRoutingPolicy();
               if (aclIn != null) {
                  String name = aclIn.getName();
                  aclMap.put(name, aclIn);
               }
               if (aclOut != null) {
                  String name = aclOut.getName();
                  aclMap.put(name, aclOut);
               }
               if (routePolicy != null) {
                  for (PolicyMapClause clause : routePolicy.getClauses()) {
                     for (PolicyMapMatchLine matchLine : clause.getMatchLines()) {
                        if (matchLine.getType() == PolicyMapMatchType.IP_ACCESS_LIST) {
                           PolicyMapMatchIpAccessListLine matchAclLine = (PolicyMapMatchIpAccessListLine) matchLine;
                           for (IpAccessList acl : matchAclLine.getLists()) {
                              String name = acl.getName();
                              aclMap.put(name, acl);
                           }
                        }
                     }
                  }
               }
            }
         }
         if (aclMap.size() > 0) {
            matchAcls.put(hostname, aclMap);
         }
      }
      for (Entry<String, Map<String, IpAccessList>> e : matchAcls.entrySet()) {
         String hostname = e.getKey();
         Map<String, IpAccessList> aclMap = e.getValue();
         for (Entry<String, IpAccessList> e2 : aclMap.entrySet()) {
            String aclName = e2.getKey();
            IpAccessList acl = e2.getValue();
            List<IpAccessListLine> lines = acl.getLines();
            for (int i = 0; i < lines.size(); i++) {
               IpAccessListLine line = lines.get(i);

               long dstIp = line.getDestinationIP().asLong();
               int dstIpWildcardBits = Util.numWildcardBits(line
                     .getDestinationWildcard().asLong());
               int dstIpStart = dstIpWildcardBits;
               int dstIpEnd = 31;

               long srcIp = line.getSourceIP().asLong();
               int srcIpWildcardBits = Util.numWildcardBits(line
                     .getSourceWildcard().asLong());
               int srcIpStart = srcIpWildcardBits;
               int srcIpEnd = 31;

               long protocol = line.getProtocol();
               List<SubRange> srcPortRanges = line.getSrcPortRanges();
               List<SubRange> dstPortRanges = line.getDstPortRanges();

               AndExpr matchConditions = new AndExpr();

               // ** must not match previous rule **
               AclNoMatchExpr prevNoMatch = null;
               EqExpr prevLineVarMatches = null;
               if (i > 0) {
                  prevNoMatch = new AclNoMatchExpr(ACL_VAR, PREV_LINE_VAR);
                  prevLineVarMatches = matchLineVar(PREV_LINE_VAR, i - 1);
               }

               // / match rule
               AndExpr matchLineCriteria = new AndExpr();
               matchConditions.addConjunct(matchLineCriteria);
               if (prevNoMatch != null) {
                  matchConditions.addConjunct(prevNoMatch);
                  matchConditions.addConjunct(prevLineVarMatches);
               }

               // match protocol
               if (protocol != 0) {
                  VarIntExpr protocolVar = new VarIntExpr(IP_PROTOCOL_VAR);
                  LitIntExpr protocolLit = new LitIntExpr(protocol, 16);
                  EqExpr matchProtocol = new EqExpr(protocolVar, protocolLit);
                  matchLineCriteria.addConjunct(matchProtocol);
               }
               // match srcIp
               if (srcIpStart < 32) {
                  IntExpr extractSrcIp = newExtractExpr(SRC_IP_VAR, srcIpStart,
                        srcIpEnd);
                  LitIntExpr srcIpMatchLit = new LitIntExpr(srcIp, srcIpStart,
                        srcIpEnd);
                  EqExpr matchSrcIp = new EqExpr(extractSrcIp, srcIpMatchLit);
                  matchLineCriteria.addConjunct(matchSrcIp);
               }
               // match dstIp
               if (dstIpStart < 32) {
                  IntExpr extractDstIp = newExtractExpr(DST_IP_VAR, dstIpStart,
                        dstIpEnd);
                  LitIntExpr dstIpMatchLit = new LitIntExpr(dstIp, dstIpStart,
                        dstIpEnd);
                  EqExpr matchDstIp = new EqExpr(extractDstIp, dstIpMatchLit);
                  matchLineCriteria.addConjunct(matchDstIp);
               }

               // match srcport
               if (srcPortRanges != null && srcPortRanges.size() > 0) {
                  BooleanExpr matchSrcPort = getMatchAclRules_portHelper(
                        srcPortRanges, SRC_PORT_VAR);
                  matchLineCriteria.addConjunct(matchSrcPort);
               }

               // matchdstport
               if (dstPortRanges != null && dstPortRanges.size() > 0) {
                  BooleanExpr matchDstPort = getMatchAclRules_portHelper(
                        dstPortRanges, DST_PORT_VAR);
                  matchLineCriteria.addConjunct(matchDstPort);
               }

               matchLineCriteria.addConjunct(TrueExpr.INSTANCE);

               EqExpr lineVarMatches = matchLineVar(i);
               EqExpr aclVarMatches = matchAclVar(hostname, aclName);
               matchConditions.addConjunct(aclVarMatches);
               matchConditions.addConjunct(lineVarMatches);
               AclMatchExpr matchLine = new AclMatchExpr(ACL_VAR, LINE_VAR);

               RuleExpr matchRule = new RuleExpr(matchConditions, matchLine);
               statements.add(matchRule);

               // / no match rule
               AndExpr noMatchConditions = new AndExpr();
               NotExpr noMatchLineCriteria = new NotExpr(matchLineCriteria);
               noMatchConditions.addConjunct(noMatchLineCriteria);
               noMatchConditions.addConjunct(aclVarMatches);
               noMatchConditions.addConjunct(lineVarMatches);
               if (prevNoMatch != null) {
                  noMatchConditions.addConjunct(prevNoMatch);
                  noMatchConditions.addConjunct(prevLineVarMatches);
               }
               AclNoMatchExpr noMatch = new AclNoMatchExpr(ACL_VAR, LINE_VAR);
               RuleExpr noMatchRule = new RuleExpr(noMatchConditions, noMatch);
               statements.add(noMatchRule);

               // / pass/fail rule for match
               PolicyExpr aclAction;
               switch (line.getAction()) {
               case ACCEPT:
                  aclAction = new AclPermitExpr(ACL_VAR);
                  break;

               case REJECT:
                  aclAction = new AclDenyExpr(ACL_VAR);
                  break;

               default:
                  throw new Error("invalid action");
               }
               AndExpr actionConditions = new AndExpr();
               actionConditions.addConjunct(aclVarMatches);
               actionConditions.addConjunct(lineVarMatches);
               actionConditions.addConjunct(matchLine);
               RuleExpr action = new RuleExpr(actionConditions, aclAction);
               statements.add(action);

               // / fail rule for no matches
               if (i == lines.size() - 1) {
                  int lastLineIndex = i;
                  AclDenyExpr aclDeny = new AclDenyExpr(ACL_VAR);
                  AclNoMatchExpr noMatchLast = new AclNoMatchExpr(ACL_VAR, LAST_LINE_VAR);
                  EqExpr lastLineVarMatches = matchLineVar(LAST_LINE_VAR, lastLineIndex);
                  AndExpr implicitDenyConditions = new AndExpr();
                  implicitDenyConditions.addConjunct(noMatchLast);
                  implicitDenyConditions.addConjunct(lastLineVarMatches);
                  implicitDenyConditions.addConjunct(aclVarMatches);
                  RuleExpr implicitDeny = new RuleExpr(implicitDenyConditions, aclDeny);
                  statements.add(implicitDeny);
               }
            }
         }
      }
      return statements;
   }

   private BooleanExpr getMatchAclRules_portHelper(List<SubRange> ranges,
         String portVar) {
      OrExpr or = new OrExpr();
      for (SubRange srcPortRange : ranges) {
         long low = srcPortRange.getStart();
         long high = srcPortRange.getEnd();
         if (low == high) {
            VarIntExpr portVarExpr = new VarIntExpr(portVar);
            LitIntExpr portLitExpr = new LitIntExpr(low, 16);
            EqExpr exactMatch = new EqExpr(portVarExpr, portLitExpr);
            or.addDisjunct(exactMatch);
         }
         else {
            boolean doLE = (high < PORT_MAX);
            boolean doGE = (low > PORT_MIN);
            AndExpr and = new AndExpr();
            if (doGE) {
               BooleanExpr geExpr = bitvectorGEExpr(portVar, low, PORT_BITS);
               and.addConjunct(geExpr);
            }
            if (doLE) {
               BooleanExpr leExpr = bitvectorLEExpr(portVar, high, PORT_BITS);
               and.addConjunct(leExpr);
            }
            if (!doGE && !doLE) {
               // all ports match
               return TrueExpr.INSTANCE;
            }
            and.addConjunct(TrueExpr.INSTANCE);
         }
      }
      or.addDisjunct(FalseExpr.INSTANCE);
      return or;
   }

   private int getMaxAclLine() {
      int max = -1;
      for (Configuration c : _configurations.values()) {
         for (IpAccessList acl : c.getIpAccessLists().values()) {
            int lastLine = acl.getLines().size() - 1;
            if (max < lastLine) {
               max = lastLine;
            }
         }
      }
      return max;
   }

   private int getMaxPolicyClause() {
      int max = -1;
      for (Configuration c : _configurations.values()) {
         for (PolicyMap p : c.getPolicyMaps().values()) {
            int lastClause = p.getClauses().size() - 1;
            if (max < lastClause) {
               max = lastClause;
            }
         }
      }
      return max;
   }

   private IntExpr getNodeNumber(String hostname) {
      int nodeNumber = _nodeIndex.get(hostname);
      return new LitIntExpr(nodeNumber, _nodeWidth);
   }

   public NodeIndex getNodeNumbers() {
      return _nodeIndex;
   }

   private IntExpr getPolicyNumber(String hostname, String policyName) {
      int policyNum = _policyIndex.get(new HostnamePolicyNamePair(hostname, policyName));
      return new LitIntExpr(policyNum, _policyWidth);
   }

   private List<Statement> getPolicyRouteRules() {
      List<Statement> statements = new ArrayList<Statement>();
      statements.add(new Comment("Policy-based routing rules"));
      PreOutInterfaceExpr preOutInterface = new PreOutInterfaceExpr(
            NODE_VAR, INTERFACE_OUT_VAR);      
      PostInInterfaceExpr postInIface = new PostInInterfaceExpr(NODE_VAR,
            INTERFACE_IN_VAR);
      for (Entry<String, Configuration> e : _configurations.entrySet()) {
         String hostname = e.getKey();
         EqExpr nodeMatches = matchNodeVar(hostname);
         PreOutExpr preOut = new PreOutExpr(NODE_VAR);
         PolicyRouteFibIpMap ipMap = _prFibs.get(hostname);
         Configuration c = e.getValue();
         for (Entry<String, Interface> e2 : c.getInterfaces().entrySet()) {
            String inIfaceName = e2.getKey();
            EqExpr inIntMatches = matchInterfaceVar(INTERFACE_IN_VAR,
                  inIfaceName);
            Interface iface = e2.getValue();
            PolicyMap p = iface.getRoutingPolicy();
            if (p != null) {
               String policyName = p.getMapName();
               EqExpr policyVarMatches = matchPolicyVar(hostname, policyName);
               AndExpr preOutIfacePolicyConditions = new AndExpr();
               preOutIfacePolicyConditions.addConjunct(policyVarMatches);
               preOutIfacePolicyConditions.addConjunct(inIntMatches);
               preOutIfacePolicyConditions.addConjunct(postInIface);
               preOutIfacePolicyConditions.addConjunct(preOut);
               preOutIfacePolicyConditions.addConjunct(nodeMatches);
               List<PolicyMapClause> clauses = p.getClauses();
               /**
                * For each clause, if we reach that clause, and it is a permit
                * clause, then for each acl matched in the clause, if the packet
                * is permitted by that acl, then the packet is permitted by that
                * clause. If there are no acls to match, then the packet is
                * permitted by that clause.
                */
               for (int i = 0; i < clauses.size(); i++) {
                  PolicyMapClause clause = clauses.get(i);
                  PolicyMapAction action = clause.getAction();
                  PolicyExpr policyAction;
                  switch (action) {
                  case PERMIT:
                     policyAction = new PolicyPermitExpr(POLICY_VAR);
                     break;
                  case DENY:
                     policyAction = new PolicyDenyExpr(POLICY_VAR);
                     break;
                  default:
                     throw new Error("bad action");
                  }
                  PolicyMatchExpr policyClauseMatch = new PolicyMatchExpr(
                        POLICY_VAR, CLAUSE_VAR);
                  EqExpr clauseVarMatches = matchClauseVar(i);
                  AndExpr preOutIfaceClauseConditions = new AndExpr();
                  preOutIfaceClauseConditions.addConjunct(preOutIfacePolicyConditions);
                  preOutIfaceClauseConditions.addConjunct(policyClauseMatch);
                  preOutIfaceClauseConditions.addConjunct(clauseVarMatches);
                  if (action == PolicyMapAction.PERMIT) {
                     /**
                      * For each clause, for every next hop interface
                      * corresponding to every next hop ip set in that clause,
                      * if the packet is permitted by that clause, then it
                      * reaches preOutInterface for that interface
                      */
                     for (PolicyMapSetLine setLine : clause.getSetLines()) {
                        if (setLine.getType() == PolicyMapSetType.NEXT_HOP) {
                           PolicyMapSetNextHopLine setNextHopLine = (PolicyMapSetNextHopLine) setLine;
                           Set<String> nextHopInterfaces = new TreeSet<String>();
                           for (Ip nextHopIp : setNextHopLine.getNextHops()) {
                              String nextHopInterface = ipMap.get(nextHopIp);
                              nextHopInterfaces.add(nextHopInterface);
                           }
                           for (String nextHopInterface : nextHopInterfaces) {
                              EqExpr nextHopIntMatches = matchInterfaceVar(
                                    INTERFACE_OUT_VAR, nextHopInterface);
                              AndExpr preOutIfaceConditions = new AndExpr();
                              preOutIfaceConditions.addConjunct(preOutIfaceClauseConditions);
                              preOutIfaceConditions
                                    .addConjunct(nextHopIntMatches);
                              RuleExpr preOutRule = new RuleExpr(
                                    preOutIfaceClauseConditions, preOutInterface);
                              statements.add(preOutRule);
                           }
                        }
                     }
                  }
                  /**
                   * if clausematch, then action
                   */
                  AndExpr actionConditions = new AndExpr();
                  actionConditions.addConjunct(policyClauseMatch);
                  actionConditions.addConjunct(policyVarMatches);
                  actionConditions.addConjunct(clauseVarMatches);
                  RuleExpr matchAction = new RuleExpr(actionConditions,
                        policyAction);
                  statements.add(matchAction);

                  boolean hasMatchIp = false;
                  AndExpr noAclMatchConditions = new AndExpr();
                  PolicyNoMatchExpr prevNoMatch = new PolicyNoMatchExpr(POLICY_VAR, PREV_CLAUSE_VAR);
                  EqExpr prevClauseVarMatches = matchClauseVar(PREV_CLAUSE_VAR, i-1);
                  for (PolicyMapMatchLine matchLine : clause.getMatchLines()) {
                     if (matchLine.getType() == PolicyMapMatchType.IP_ACCESS_LIST) {
                        hasMatchIp = true;
                        PolicyMapMatchIpAccessListLine matchIpLine = (PolicyMapMatchIpAccessListLine) matchLine;
                        for (IpAccessList acl : matchIpLine.getLists()) {
                           String aclName = acl.getName();
                           AclPermitExpr aclPermit = new AclPermitExpr(ACL_VAR);
                           EqExpr aclVarMatches = matchAclVar(hostname, aclName);
                           AclDenyExpr aclDeny = new AclDenyExpr(ACL_VAR);
                           noAclMatchConditions.addConjunct(aclDeny);
                           noAclMatchConditions.addConjunct(aclVarMatches);
                           RuleExpr matchRule;
                           if (i > 0) {
                              /**
                               * if prevnomatch and aclpass, then clausematch
                               */
                              AndExpr clauseMatchConditions = new AndExpr();
                              clauseMatchConditions.addConjunct(prevNoMatch);
                              clauseMatchConditions.addConjunct(prevClauseVarMatches);
                              clauseMatchConditions.addConjunct(policyVarMatches);
                              clauseMatchConditions.addConjunct(clauseVarMatches);
                              clauseMatchConditions.addConjunct(aclPermit);
                              clauseMatchConditions.addConjunct(aclVarMatches);
                              matchRule = new RuleExpr(clauseMatchConditions,
                                    policyClauseMatch);
                           }
                           else {
                              /**
                               * if aclpass, then clausematch
                               */
                              matchRule = new RuleExpr(aclPermit,
                                    policyClauseMatch);
                           }
                           statements.add(matchRule);
                        }
                     }
                  }
                  if (hasMatchIp) {
                     /**
                      * For each clause, if there is at least one acl to match,
                      * and the packet does not match any acls, then the packet
                      * is not matched by that clause
                      */
                     if (i > 0) {
                        noAclMatchConditions.addConjunct(prevNoMatch);
                     }
                     noAclMatchConditions.addConjunct(policyVarMatches);
                     noAclMatchConditions.addConjunct(clauseVarMatches);
                     PolicyNoMatchExpr policyClauseNoMatch = new PolicyNoMatchExpr(POLICY_VAR, CLAUSE_VAR);
                     RuleExpr noMatchRule = new RuleExpr(noAclMatchConditions,
                           policyClauseNoMatch);
                     statements.add(noMatchRule);
                  }
                  else {
                     /**
                      * Since there is nothing to match, this clause MUST match
                      * the packet if it is reached
                      */
                     RuleExpr trivialMatch;
                     if (i > 0) {
                        AndExpr prevNoMatchConditions = new AndExpr();
                        prevNoMatchConditions.addConjunct(prevNoMatch);
                        prevNoMatchConditions.addConjunct(prevClauseVarMatches);
                        prevNoMatchConditions.addConjunct(policyVarMatches);
                        trivialMatch = new RuleExpr(prevNoMatchConditions,
                              policyClauseMatch);
                     }
                     else {
                        trivialMatch = new RuleExpr(policyClauseMatch);
                     }
                     statements.add(trivialMatch);
                  }
               }
               /**
                * If the packet reaches the last clause, and is not matched by
                * that clause, then it is not permitted by the policy.
                */
               int lastIndex = p.getClauses().size() - 1;
               AndExpr noMatchLastClauseConditions = new AndExpr();
               PolicyNoMatchExpr noMatchLastClause = new PolicyNoMatchExpr(POLICY_VAR, LAST_CLAUSE_VAR);
               EqExpr lastClauseVarMatches = matchClauseVar(LAST_CLAUSE_VAR, lastIndex);
               noMatchLastClauseConditions.addConjunct(noMatchLastClause);
               noMatchLastClauseConditions.addConjunct(lastClauseVarMatches);
               noMatchLastClauseConditions.addConjunct(policyVarMatches);
               PolicyDenyExpr policyDeny = new PolicyDenyExpr(POLICY_VAR);
               RuleExpr noMatchDeny = new RuleExpr(noMatchLastClauseConditions,
                     policyDeny);
               statements.add(noMatchDeny);
            }
         }
      }
      return statements;
   }

   private List<Statement> getPostInInterfaceToPostInRules() {
      List<Statement> statements = new ArrayList<Statement>();
      statements.add(new Comment(
            "Rules for connecting postInInterface to postIn"));
      PostInInterfaceExpr postInIface = new PostInInterfaceExpr(NODE_VAR,
            INTERFACE_VAR);
      PostInExpr postIn = new PostInExpr(NODE_VAR);
      RuleExpr rule = new RuleExpr(postInIface, postIn);
      statements.add(rule);
      return statements;
   }

   private List<Statement> getPostInToNodeAcceptRules() {
      List<Statement> statements = new ArrayList<Statement>();
      statements
            .add(new Comment("postin ==> preout:",
                  "for each ip address on an interface, accept if destination ip matches"));
      for (Configuration c : _configurations.values()) {
         String hostname = c.getHostname();
         for (Interface i : c.getInterfaces().values()) {
            if (i.getName().startsWith(FAKE_INTERFACE_PREFIX) || !i.getActive()) {
               continue;
            }
            Ip ip = i.getIP();
            if (ip != null) {
               AndExpr conditions = new AndExpr();
               PostInExpr postIn = new PostInExpr(NODE_VAR);
               VarIntExpr dstIpVar = new VarIntExpr(DST_IP_VAR);
               LitIntExpr dstIpLit = new LitIntExpr(ip);
               EqExpr matchDstIp = new EqExpr(dstIpVar, dstIpLit);
               EqExpr nodeMatches = matchNodeVar(hostname);
               conditions.addConjunct(postIn);
               conditions.addConjunct(matchDstIp);
               conditions.addConjunct(nodeMatches);
               NodeAcceptExpr nodeAccept = new NodeAcceptExpr(NODE_VAR);
               RuleExpr rule = new RuleExpr(conditions, nodeAccept);
               statements.add(rule);
            }
         }
      }
      return statements;
   }

   private List<Statement> getPostInToPreOutRules() {
      List<Statement> statements = new ArrayList<Statement>();
      statements
            .add(new Comment(
                  "postin ==> preout:",
                  "forward to preOut if for each ip address on an interface, destination ip does not match"));
      for (Configuration c : _configurations.values()) {
         String hostname = c.getHostname();
         AndExpr conditions = new AndExpr();

         for (Interface i : c.getInterfaces().values()) {
            if (i.getName().startsWith(FAKE_INTERFACE_PREFIX)) {
               continue;
            }
            Ip ip = i.getIP();
            if (ip != null) {
               LitIntExpr dstIpLit = new LitIntExpr(ip);
               VarIntExpr dstIpVar = new VarIntExpr(DST_IP_VAR);
               NotExpr noMatchDstIp = new NotExpr();
               EqExpr matchDstIp = new EqExpr(dstIpVar, dstIpLit);
               noMatchDstIp.SetArgument(matchDstIp);
               conditions.addConjunct(noMatchDstIp);
            }
         }
         PostInExpr postIn = new PostInExpr(NODE_VAR);
         conditions.addConjunct(postIn);
         EqExpr nodeMatches = matchNodeVar(hostname);
         conditions.addConjunct(nodeMatches);
         PreOutExpr preOut = new PreOutExpr(NODE_VAR);
         RuleExpr rule = new RuleExpr(conditions, preOut);
         statements.add(rule);
      }
      return statements;
   }

   private List<Statement> getPreInInterfaceToPostInInterfaceRules() {
      List<Statement> statements = new ArrayList<Statement>();
      statements.add(new Comment(
            "Incoming ACL interfaces"));
      OrExpr inAclIntConditions = new OrExpr();
      
      for (String hostname : _topologyInterfaces.keySet()) {
         Set<Interface> interfaces = _topologyInterfaces.get(hostname);
         for (Interface iface : interfaces) {
            String ifaceName = iface.getName();
            if (ifaceName.startsWith(FAKE_INTERFACE_PREFIX)
                  || ifaceName.startsWith(FLOW_SINK_INTERFACE_PREFIX)) {
               continue;
            }
            IpAccessList inAcl = iface.getIncomingFilter();
            if (inAcl != null) {
               // add disjunct to conditions for having an incoming acl
               AndExpr both = new AndExpr();
               EqExpr nodeVarMatches = matchNodeVar(hostname);
               EqExpr interfaceVarMatches = matchInterfaceVar(ifaceName);
               both.addConjunct(nodeVarMatches);
               both.addConjunct(interfaceVarMatches);
               inAclIntConditions.addDisjunct(both);
               
               // add acl assignment
               String aclName = inAcl.getName();
               IncomingAclInterfaceExpr incomingAclInterface = new IncomingAclInterfaceExpr(NODE_VAR, INTERFACE_VAR, ACL_VAR);
               EqExpr nodeMatches = matchNodeVar(hostname);
               EqExpr interfaceMatches = matchInterfaceVar(ifaceName);
               EqExpr aclMatches = matchAclVar(hostname, aclName);
               AndExpr aclAssignmentConditions = new AndExpr();
               aclAssignmentConditions.addConjunct(nodeMatches);
               aclAssignmentConditions.addConjunct(interfaceMatches);
               aclAssignmentConditions.addConjunct(aclMatches);
               RuleExpr aclAssignment = new RuleExpr(aclAssignmentConditions, incomingAclInterface);
               statements.add(aclAssignment);
            }
         }
      }
      NotExpr noInAclIntConditions = new NotExpr(inAclIntConditions);
      NoIncomingAclInterfaceExpr noInAclInt = new NoIncomingAclInterfaceExpr(NODE_VAR, INTERFACE_VAR);
      RuleExpr noInAclIntRule = new RuleExpr(noInAclIntConditions, noInAclInt);
      statements.add(noInAclIntRule);
      
      statements.add(new Comment(
            "Interfaces with no incoming acl connect prein_iface directly to postin_iface"));
      PreInInterfaceExpr preInIface = new PreInInterfaceExpr(NODE_VAR, INTERFACE_VAR);
      PostInInterfaceExpr postInIface = new PostInInterfaceExpr(NODE_VAR, INTERFACE_VAR);
      AndExpr directConditions = new AndExpr();
      directConditions.addConjunct(preInIface);
      directConditions.addConjunct(noInAclInt);
      RuleExpr direct = new RuleExpr(directConditions, postInIface);
      statements.add(direct);
      
      statements.add(new Comment(
            "Interfaces with incoming acl connect prein_iface to postin_iface if acl permits"));
      IncomingAclInterfaceExpr inAclInt = new IncomingAclInterfaceExpr(NODE_VAR, INTERFACE_VAR, ACL_VAR);
      AclPermitExpr aclPermit = new AclPermitExpr(ACL_VAR);
      AndExpr throughAclConditions = new AndExpr();
      throughAclConditions.addConjunct(preInIface);
      throughAclConditions.addConjunct(inAclInt);
      throughAclConditions.addConjunct(aclPermit);
      RuleExpr throughAcl = new RuleExpr(throughAclConditions, postInIface);
      statements.add(throughAcl);
      
      statements.add(new Comment(
            "Interfaces with incoming acl connect prein_iface to node_drop if acl denies"));
      AclDenyExpr aclDeny = new AclDenyExpr(ACL_VAR);
      NodeDropExpr nodeDrop = new NodeDropExpr(NODE_VAR);
      AndExpr throughAclDenyConditions = new AndExpr();
      throughAclDenyConditions.addConjunct(preInIface);
      throughAclDenyConditions.addConjunct(inAclInt);
      throughAclDenyConditions.addConjunct(aclDeny);
      RuleExpr throughAclDeny = new RuleExpr(throughAclDenyConditions, nodeDrop);
      statements.add(throughAclDeny);
      
      return statements;
   }

   private List<Statement> getPreOutInterfaceToPostOutInterfaceRules() {
      List<Statement> statements = new ArrayList<Statement>();
      statements.add(new Comment(
            "Outgoing ACL interfaces"));
      OrExpr outAclIntConditions = new OrExpr();
      
      for (String hostname : _topologyInterfaces.keySet()) {
         Set<Interface> interfaces = _topologyInterfaces.get(hostname);
         for (Interface iface : interfaces) {
            String ifaceName = iface.getName();
            if (ifaceName.startsWith(FAKE_INTERFACE_PREFIX)) {
               continue;
            }
            IpAccessList outAcl = iface.getOutgoingFilter();
            if (outAcl != null) {
               // add disjunct to conditions for having an outgoing acl
               AndExpr both = new AndExpr();
               EqExpr nodeVarMatches = matchNodeVar(hostname);
               EqExpr interfaceVarMatches = matchInterfaceVar(ifaceName);
               both.addConjunct(nodeVarMatches);
               both.addConjunct(interfaceVarMatches);
               outAclIntConditions.addDisjunct(both);
               
               // add acl assignment
               String aclName = outAcl.getName();
               OutgoingAclInterfaceExpr outgoingAclInterface = new OutgoingAclInterfaceExpr(NODE_VAR, INTERFACE_VAR, ACL_VAR);
               EqExpr nodeMatches = matchNodeVar(hostname);
               EqExpr interfaceMatches = matchInterfaceVar(ifaceName);
               EqExpr aclMatches = matchAclVar(hostname, aclName);
               AndExpr aclAssignmentConditions = new AndExpr();
               aclAssignmentConditions.addConjunct(nodeMatches);
               aclAssignmentConditions.addConjunct(interfaceMatches);
               aclAssignmentConditions.addConjunct(aclMatches);
               RuleExpr aclAssignment = new RuleExpr(aclAssignmentConditions, outgoingAclInterface);
               statements.add(aclAssignment);
            }
         }
      }
      NotExpr noInAclIntConditions = new NotExpr(outAclIntConditions);
      NoOutgoingAclInterfaceExpr noInAclInt = new NoOutgoingAclInterfaceExpr(NODE_VAR, INTERFACE_VAR);
      RuleExpr noInAclIntRule = new RuleExpr(noInAclIntConditions, noInAclInt);
      statements.add(noInAclIntRule);
      
      statements.add(new Comment(
            "Interfaces with no outgoing acl connect preout_iface directly to postout_iface"));
      PreInInterfaceExpr preOutIface = new PreInInterfaceExpr(NODE_VAR, INTERFACE_VAR);
      PostInInterfaceExpr postOutIface = new PostInInterfaceExpr(NODE_VAR, INTERFACE_VAR);
      AndExpr directConditions = new AndExpr();
      directConditions.addConjunct(preOutIface);
      directConditions.addConjunct(noInAclInt);
      RuleExpr direct = new RuleExpr(directConditions, postOutIface);
      statements.add(direct);
      
      statements.add(new Comment(
            "Interfaces with outgoing acl connect preout_iface to postout_iface if acl permits"));
      OutgoingAclInterfaceExpr outAclInt = new OutgoingAclInterfaceExpr(NODE_VAR, INTERFACE_VAR, ACL_VAR);
      AclPermitExpr aclPermit = new AclPermitExpr(ACL_VAR);
      AndExpr throughAclConditions = new AndExpr();
      throughAclConditions.addConjunct(preOutIface);
      throughAclConditions.addConjunct(outAclInt);
      throughAclConditions.addConjunct(aclPermit);
      RuleExpr throughAcl = new RuleExpr(throughAclConditions, postOutIface);
      statements.add(throughAcl);
      
      statements.add(new Comment(
            "Interfaces with outgoing acl connect preout_iface to node_drop if acl denies"));
      AclDenyExpr aclDeny = new AclDenyExpr(ACL_VAR);
      NodeDropExpr nodeDrop = new NodeDropExpr(NODE_VAR);
      AndExpr throughAclDenyConditions = new AndExpr();
      throughAclDenyConditions.addConjunct(preOutIface);
      throughAclDenyConditions.addConjunct(outAclInt);
      throughAclDenyConditions.addConjunct(aclDeny);
      RuleExpr throughAclDeny = new RuleExpr(throughAclDenyConditions, nodeDrop);
      statements.add(throughAclDeny);
      
      return statements;
   }

   private List<Statement> getPreOutToDestRouteRules() {
      List<Statement> statements = new ArrayList<Statement>();
      statements.add(new Comment(
            "Rules for sending packets from preout to destroute stage"));
      /**
       * say which interfaces are (not) policy-routed
       */
      List<Entry<String, String>> policyRoutedInterfaces = new ArrayList<Entry<String, String>>();
      for (String hostname : _fibs.keySet()) {
         Configuration config = _configurations.get(hostname);
         for (Entry<String, Interface> ifaceEntry : config.getInterfaces()
               .entrySet()) {
            String ifaceName = ifaceEntry.getKey();
            Interface iface = ifaceEntry.getValue();
            PolicyMap p = iface.getRoutingPolicy();
            if (p != null) {
               Entry<String, String> policyRoutedEntry = new AbstractMap.SimpleEntry<String, String>(
                     hostname, ifaceName);
               policyRoutedInterfaces.add(policyRoutedEntry);
            }
         }
      }
      OrExpr policyRoutedConditions = new OrExpr();
      NotExpr notPolicyRoutedConditions = new NotExpr(policyRoutedConditions);
      for (Entry<String, String> policyRoutedEntry : policyRoutedInterfaces) {
         String hostname = policyRoutedEntry.getKey();
         String ifaceName = policyRoutedEntry.getValue();
         EqExpr nodeMatches = matchNodeVar(hostname);
         EqExpr interfaceMatches = matchInterfaceVar(ifaceName);
         AndExpr bothMatch = new AndExpr();
         bothMatch.addConjunct(nodeMatches);
         bothMatch.addConjunct(interfaceMatches);
         policyRoutedConditions.addDisjunct(bothMatch);
      }
      NotPolicyRoutedExpr notPolicyRoutedExpr = new NotPolicyRoutedExpr(
            NODE_VAR, INTERFACE_VAR);
      RuleExpr notPolicyRouted = new RuleExpr(notPolicyRoutedConditions,
            notPolicyRoutedExpr);
      statements.add(notPolicyRouted);
      PolicyRoutedExpr policyRoutedExpr = new PolicyRoutedExpr(NODE_VAR,
            INTERFACE_VAR);
      RuleExpr policyRouted = new RuleExpr(policyRoutedConditions,
            policyRoutedExpr);
      statements.add(policyRouted);

      /**
       * if a packet whose source node is a given node reaches preout on that
       * node, then it reaches destroute
       */
      AndExpr originConditions = new AndExpr();
      PreOutExpr preOutOrigin = new PreOutExpr(NODE_VAR);
      originConditions.addConjunct(preOutOrigin);
      DestinationRouteExpr destRouteOrigin = new DestinationRouteExpr(NODE_VAR);
      EqExpr nodeIsSourceNode = new EqExpr(new VarIntExpr(NODE_VAR),
            new VarIntExpr(SRC_NODE_VAR));
      originConditions.addConjunct(nodeIsSourceNode);
      RuleExpr originRule = new RuleExpr(originConditions, destRouteOrigin);
      statements.add(originRule);

      PreOutExpr preOut = new PreOutExpr(NODE_VAR);
      PostInInterfaceExpr postInInterface = new PostInInterfaceExpr(NODE_VAR,
            INTERFACE_VAR);
      DestinationRouteExpr destRoute = new DestinationRouteExpr(NODE_VAR);
      AndExpr notPolicyRoutedToDestRouteConditions = new AndExpr();
      NotPolicyRoutedExpr notPolicyRoutedInput = new NotPolicyRoutedExpr(
            NODE_VAR, INTERFACE_VAR);
      notPolicyRoutedToDestRouteConditions.addConjunct(notPolicyRoutedInput);
      notPolicyRoutedToDestRouteConditions.addConjunct(preOut);
      notPolicyRoutedToDestRouteConditions.addConjunct(postInInterface);
      RuleExpr policyRoutedToDestRoute = new RuleExpr(
            notPolicyRoutedToDestRouteConditions, destRoute);
      statements.add(policyRoutedToDestRoute);

      for (String hostname : _fibs.keySet()) {
         Configuration config = _configurations.get(hostname);
         /**
          * for each non-policy-routed interface, if something reaches postin on
          * that interface, and reaches preout, then it reaches destroute
          */
         /**
          * for each policy-routed interface, if something reaches postin on
          * that interface, and reaches preout, and fails its policy, then it
          * reaches destroute
          */

         for (Entry<String, Interface> ifaceEntry : config.getInterfaces()
               .entrySet()) {
            AndExpr perInterfaceConditions = new AndExpr();
            perInterfaceConditions.addConjunct(preOut);
            String ifaceName = ifaceEntry.getKey();
            Interface iface = ifaceEntry.getValue();
            PolicyMap p = iface.getRoutingPolicy();
            if (p != null) {
               String policyName = p.getMapName();
               PolicyDenyExpr policyDeny = new PolicyDenyExpr(POLICY_VAR);
               EqExpr policyMatches = matchPolicyVar(hostname, policyName);
               perInterfaceConditions.addConjunct(policyDeny);
               perInterfaceConditions.addConjunct(policyMatches);
               PostInInterfaceExpr postInIface = new PostInInterfaceExpr(
                     NODE_VAR, INTERFACE_VAR);
               perInterfaceConditions.addConjunct(postInIface);
               EqExpr nodeMatches = matchNodeVar(hostname);
               EqExpr interfaceMatches = matchInterfaceVar(ifaceName);
               perInterfaceConditions.addConjunct(nodeMatches);
               perInterfaceConditions.addConjunct(interfaceMatches);
               RuleExpr perInterfaceRule = new RuleExpr(perInterfaceConditions,
                     destRoute);
               statements.add(perInterfaceRule);
            }
         }
      }
      return statements;
   }

   private List<Statement> getRelDeclExprs() {
      List<Statement> statements = new ArrayList<Statement>();
      Comment header = new Comment("Relation declarations");
      statements.add(header);
      List<Integer> packetRelSizes = new ArrayList<Integer>();
      for (String packetVar : PACKET_VARS) {
         packetRelSizes.add(_varSizes.get(packetVar));
      }
      statements.add(new DeclareRelExpr(AcceptExpr.NAME, packetRelSizes));
      statements.add(new DeclareRelExpr(DropExpr.NAME, packetRelSizes));
      // packet relations with additional node argument
      List<Integer> nodeRelSizes = new ArrayList<Integer>();
      nodeRelSizes.addAll(packetRelSizes);
      nodeRelSizes.add(_nodeWidth);
      statements.add(new DeclareRelExpr(NodeAcceptExpr.NAME, nodeRelSizes));
      statements.add(new DeclareRelExpr(NodeDropExpr.NAME, nodeRelSizes));
      statements.add(new DeclareRelExpr(PreOutExpr.NAME, nodeRelSizes));
      statements.add(new DeclareRelExpr(PostInExpr.NAME, nodeRelSizes));
      statements
            .add(new DeclareRelExpr(DestinationRouteExpr.NAME, nodeRelSizes));
      // packet relations with additional node and interface arguments
      List<Integer> nodeIntRelSizes = new ArrayList<Integer>();
      nodeIntRelSizes.addAll(nodeRelSizes);
      nodeIntRelSizes.add(_interfaceWidth);
      statements.add(new DeclareRelExpr(PreOutInterfaceExpr.NAME,
            nodeIntRelSizes));
      statements.add(new DeclareRelExpr(PostOutInterfaceExpr.NAME,
            nodeIntRelSizes));
      statements.add(new DeclareRelExpr(PostInInterfaceExpr.NAME,
            nodeIntRelSizes));
      statements.add(new DeclareRelExpr(PreInInterfaceExpr.NAME,
            nodeIntRelSizes));

      // node-interface relations
      List<Integer> nodeInterfaceSizes = new ArrayList<Integer>();
      nodeInterfaceSizes.add(_nodeWidth);
      nodeInterfaceSizes.add(_interfaceWidth);
      statements.add(new DeclareRelExpr(FlowSinkExpr.NAME, nodeInterfaceSizes));
      statements.add(new DeclareRelExpr(PolicyRoutedExpr.NAME,
            nodeInterfaceSizes));
      statements.add(new DeclareRelExpr(NotPolicyRoutedExpr.NAME,
            nodeInterfaceSizes));
      statements.add(new DeclareRelExpr(NoIncomingAclInterfaceExpr.NAME,
            nodeInterfaceSizes));
      statements.add(new DeclareRelExpr(NoOutgoingAclInterfaceExpr.NAME,
            nodeInterfaceSizes));
      
      // acl assignment relations
      List<Integer> aclAssignmentSizes = new ArrayList<Integer>();
      aclAssignmentSizes.addAll(nodeInterfaceSizes);
      aclAssignmentSizes.add(_aclWidth);
      statements.add(new DeclareRelExpr(IncomingAclInterfaceExpr.NAME,
            aclAssignmentSizes));
      statements.add(new DeclareRelExpr(OutgoingAclInterfaceExpr.NAME,
            aclAssignmentSizes));
      
      // policy/acl relations common code
      List<Integer> policySizes = new ArrayList<Integer>();
      for (String policyVar : POLICY_VARS) {
         policySizes.add(_varSizes.get(policyVar));
      }
      
      // policy relations
      List<Integer> policyActionSizes = new ArrayList<Integer>();
      policyActionSizes.addAll(policySizes);
      policyActionSizes.add(_policyWidth);
      statements.add(new DeclareRelExpr(PolicyPermitExpr.NAME,
            policyActionSizes));
      statements.add(new DeclareRelExpr(PolicyDenyExpr.NAME,
            policyActionSizes));
      List<Integer> policyClauseSizes = new ArrayList<Integer>();
      policyClauseSizes.addAll(policyActionSizes);
      policyClauseSizes.add(_policyClauseWidth);
      statements.add(new DeclareRelExpr(PolicyMatchExpr.NAME,
            policyClauseSizes));
      statements.add(new DeclareRelExpr(PolicyNoMatchExpr.NAME,
            policyClauseSizes));
      
      // acl relations
      List<Integer> aclActionSizes = new ArrayList<Integer>();
      aclActionSizes.addAll(policySizes);
      aclActionSizes.add(_aclWidth);
      statements.add(new DeclareRelExpr(AclPermitExpr.NAME,
            aclActionSizes));
      statements.add(new DeclareRelExpr(AclDenyExpr.NAME,
            aclActionSizes));
      List<Integer> aclLineSizes = new ArrayList<Integer>();
      aclLineSizes.addAll(aclActionSizes);
      aclLineSizes.add(_aclLineWidth);
      statements.add(new DeclareRelExpr(AclMatchExpr.NAME,
            aclLineSizes));
      statements.add(new DeclareRelExpr(AclNoMatchExpr.NAME,
            aclLineSizes));

      return statements;
}

   private List<Statement> getToNeighborsRules() {
      List<Statement> statements = new ArrayList<Statement>();
      statements.add(new Comment("Topology edge rules"));
      for (Edge edge : _topologyEdges) {
         String hostnameOut = edge.getNode1();
         String hostnameIn = edge.getNode2();
         String intOut = edge.getInt1();
         String intIn = edge.getInt2();
         if (intIn.startsWith(FAKE_INTERFACE_PREFIX)
               || intIn.startsWith(FLOW_SINK_INTERFACE_PREFIX)
               || intOut.startsWith(FAKE_INTERFACE_PREFIX)
               || intOut.startsWith(FLOW_SINK_INTERFACE_PREFIX)) {
            continue;
         }

         PostOutInterfaceExpr postOutIface = new PostOutInterfaceExpr(
               NODE_OUT_VAR, INTERFACE_OUT_VAR);
         PreInInterfaceExpr preInIface = new PreInInterfaceExpr(NODE_IN_VAR,
               INTERFACE_IN_VAR);
         AndExpr conditions = new AndExpr();
         conditions.addConjunct(postOutIface);
         EqExpr outNodeExpr = matchNodeVar(NODE_OUT_VAR, hostnameOut);
         EqExpr inNodeExpr = matchNodeVar(NODE_IN_VAR, hostnameIn);
         EqExpr outIntExpr = matchInterfaceVar(INTERFACE_OUT_VAR, intOut);
         EqExpr inIntExpr = matchInterfaceVar(INTERFACE_IN_VAR, intIn);
         conditions.addConjunct(inNodeExpr);
         conditions.addConjunct(outNodeExpr);
         conditions.addConjunct(inIntExpr);
         conditions.addConjunct(outIntExpr);
         RuleExpr propagateToAdjacent = new RuleExpr(conditions, preInIface);
         statements.add(propagateToAdjacent);
      }
      return statements;
   }

   private List<Statement> getVarDeclExprs() {
      List<Statement> statements = new ArrayList<Statement>();
      statements.add(new Comment("Variable Declarations"));
      for (String var : _varSizes.keySet()) {
         int size = _varSizes.get(var);
         statements.add(new DeclareVarExpr(var, size));
      }
      return statements;
   }

   public VarSizeMap getVarSizes() {
      return _varSizes;
   }

   private void initAclIndex() {
      int count = 0;
      for (Entry<String, Configuration> ce : _configurations.entrySet()) {
         String hostname = ce.getKey();
         Configuration c = ce.getValue();
         for (String aclName : c.getIpAccessLists().keySet()) {
            HostnameAclNamePair p = new HostnameAclNamePair(hostname, aclName);
            _aclIndex.put(p, count);
            count++;
         }
      }
   }

   private void initInterfaceIndex() {
      Set<String> interfaceNames = new TreeSet<String>();
      for (Configuration config : _configurations.values()) {
         for (String ifaceName : config.getInterfaces().keySet()) {
            interfaceNames.add(ifaceName);
         }
      }
      int count = 0;
      for (String ifaceName : interfaceNames) {
         _interfaceIndex.put(ifaceName, count);
         count++;
      }
   }

   private void initNodeIndex() {
      int count = 0;
      for (String hostname : _configurations.keySet()) {
         _nodeIndex.put(hostname, count);
         count++;
      }
   }

   private void initPolicyIndex() {
      int count = 0;
      for (Entry<String, Configuration> ce : _configurations.entrySet()) {
         String hostname = ce.getKey();
         Configuration c = ce.getValue();
         for (String pName : c.getPolicyMaps().keySet()) {
            HostnamePolicyNamePair p = new HostnamePolicyNamePair(hostname,
                  pName);
            _policyIndex.put(p, count);
            count++;
         }
      }
   }

   private void initVarSizes() {
      _varSizes.put(SRC_NODE_VAR, _nodeWidth);
      _varSizes.put(SRC_IP_VAR, 32);
      _varSizes.put(DST_IP_VAR, 32);
      _varSizes.put(SRC_PORT_VAR, 16);
      _varSizes.put(DST_PORT_VAR, 16);
      _varSizes.put(IP_PROTOCOL_VAR, 16);
      _varSizes.put(NODE_VAR, _nodeWidth);
      _varSizes.put(INTERFACE_VAR, _interfaceWidth);
      _varSizes.put(NODE_ACCEPT_VAR, _nodeWidth);
      _varSizes.put(NODE_DROP_VAR, _nodeWidth);
      _varSizes.put(NODE_IN_VAR, _nodeWidth);
      _varSizes.put(NODE_OUT_VAR, _nodeWidth);
      _varSizes.put(INTERFACE_IN_VAR, _interfaceWidth);
      _varSizes.put(INTERFACE_OUT_VAR, _interfaceWidth);
      _varSizes.put(ACL_VAR, _aclWidth);
      _varSizes.put(POLICY_VAR, _policyWidth);
      _varSizes.put(LINE_VAR, _aclLineWidth);
      _varSizes.put(LAST_LINE_VAR, _aclLineWidth);
      _varSizes.put(PREV_LINE_VAR, _aclLineWidth);
      _varSizes.put(CLAUSE_VAR, _policyClauseWidth);
      _varSizes.put(LAST_CLAUSE_VAR, _policyClauseWidth);
      _varSizes.put(PREV_CLAUSE_VAR, _policyClauseWidth);
   }

   private EqExpr matchAclVar(String hostname, String aclName) {
      return matchAclVar(ACL_VAR, hostname, aclName);
   }

   private EqExpr matchAclVar(String aclVar, String hostname, String aclName) {
      return new EqExpr(new VarIntExpr(aclVar), getAclNumber(hostname, aclName));
   }

   private EqExpr matchClauseVar(int clause) {
      return matchLineVar(CLAUSE_VAR, clause);
   }

   private EqExpr matchClauseVar(String clauseVar, int clause) {
      return new EqExpr(new VarIntExpr(clauseVar), new LitIntExpr(clause,
            _policyClauseWidth));
   }

   private EqExpr matchInterfaceVar(String ifaceName) {
      return matchInterfaceVar(INTERFACE_VAR, ifaceName);
   }

   private EqExpr matchInterfaceVar(String interfaceVar, String ifaceName) {
      return new EqExpr(new VarIntExpr(interfaceVar),
            getInterfaceNumber(ifaceName));
   }

   private EqExpr matchLineVar(int line) {
      return matchLineVar(LINE_VAR, line);
   }

   private EqExpr matchLineVar(String lineVar, int line) {
      return new EqExpr(new VarIntExpr(lineVar), new LitIntExpr(line,
            _aclLineWidth));
   }

   private EqExpr matchNodeVar(String hostname) {
      return matchNodeVar(NODE_VAR, hostname);
   }

   private EqExpr matchNodeVar(String nodeVar, String hostname) {
      return new EqExpr(new VarIntExpr(nodeVar), getNodeNumber(hostname));
   }

   private EqExpr matchPolicyVar(String hostname, String policyName) {
      return matchPolicyVar(POLICY_VAR, hostname, policyName);
   }

   private EqExpr matchPolicyVar(String policyVar, String hostname,
         String policyName) {
      return new EqExpr(new VarIntExpr(policyVar), getPolicyNumber(hostname,
            policyName));
   }

   private IntExpr newExtractExpr(String var, int low, int high) {
      int varSize = _varSizes.get(var);
      return newExtractExpr(var, varSize, low, high);
   }

   public void synthesize(String outputFileStr) throws IOException {
      List<Statement> statements = new ArrayList<Statement>();
      List<Statement> varDecls = getVarDeclExprs();
      List<Statement> dropRules = getDropRules();
      List<Statement> acceptRules = getAcceptRules();
      List<Statement> flowSinkAcceptRules = getFlowSinkAcceptRules();
      List<Statement> postInRules = getPostInInterfaceToPostInRules();
      List<Statement> postInAcceptRules = getPostInToNodeAcceptRules();
      List<Statement> postInFwdRules = getPostInToPreOutRules();
      List<Statement> preOutToDestRouteRules = getPreOutToDestRouteRules();
      List<Statement> destRouteToPreOutIfaceRules = getDestRouteToPreOutIfaceRules();
      List<Statement> policyRouteRules = getPolicyRouteRules();
      List<Statement> matchAclRules = getMatchAclRules();
      List<Statement> toNeighborsRules = getToNeighborsRules();
      List<Statement> preInRules = getPreInInterfaceToPostInInterfaceRules();
      List<Statement> postOutRules = getPreOutInterfaceToPostOutInterfaceRules();

      /**
       * relation declarations MUST be generated last, but placed near the top
       */
      List<Statement> relDecls = getRelDeclExprs();

      statements.addAll(varDecls);
      statements.addAll(relDecls);
      statements.addAll(dropRules);
      statements.addAll(acceptRules);
      statements.addAll(flowSinkAcceptRules);
      statements.addAll(postInRules);
      statements.addAll(postInAcceptRules);
      statements.addAll(postInFwdRules);
      statements.addAll(preOutToDestRouteRules);
      statements.addAll(destRouteToPreOutIfaceRules);
      statements.addAll(policyRouteRules);
      statements.addAll(matchAclRules);
      statements.addAll(toNeighborsRules);
      statements.addAll(preInRules);
      statements.addAll(postOutRules);

      File z3Out = new File(outputFileStr);
      z3Out.delete();
      StringBuilder sb = new StringBuilder();
      for (Statement statement : statements) {
         if (_simplify) {
            Statement simplifiedStatement = statement.simplify();
            simplifiedStatement.print(sb, 0);
         }
         else {
            statement.print(sb, 0);
         }
         sb.append("\n");
      }
      FileUtils.write(z3Out, sb.toString());
   }

}
