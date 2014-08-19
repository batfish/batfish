package batfish.z3;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;

import batfish.dataplane.EdgeSet;
import batfish.dataplane.FibMap;
import batfish.dataplane.FibRow;
import batfish.dataplane.InterfaceMap;
import batfish.dataplane.NodeMap;
import batfish.dataplane.PolicyRouteFibIpMap;
import batfish.dataplane.PolicyRouteFibNodeMap;
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
import batfish.z3.node.IntExpr;
import batfish.z3.node.LitIntExpr;
import batfish.z3.node.NodeAcceptExpr;
import batfish.z3.node.NodeDropExpr;
import batfish.z3.node.NotExpr;
import batfish.z3.node.OrExpr;
import batfish.z3.node.PacketRelExpr;
import batfish.z3.node.PostInExpr;
import batfish.z3.node.PostInInterfaceExpr;
import batfish.z3.node.PostOutIfaceExpr;
import batfish.z3.node.PostOutInterfaceExpr;
import batfish.z3.node.PreInInterfaceExpr;
import batfish.z3.node.PreOutExpr;
import batfish.z3.node.PreOutInterfaceExpr;
import batfish.z3.node.RuleExpr;
import batfish.z3.node.Statement;
import batfish.z3.node.TrueExpr;
import batfish.z3.node.VarIntExpr;

public class Synthesizer {
   public static final String DST_IP_VAR = "dst_ip";
   public static final String DST_PORT_VAR = "dst_port";
   public static final String FAKE_INTERFACE_PREFIX = "TenGigabitEthernet200/";
   public static final String FLOW_SINK_INTERFACE_PREFIX = "TenGigabitEthernet100/";
   public static final String INTERFACE_IN_VAR = "interface_in";
   public static final String INTERFACE_OUT_VAR = "interface_out";
   public static final String INTERFACE_VAR = "interface";
   public static final String IP_PROTOCOL_VAR = "ip_prot";
   public static final String NODE_IN_VAR = "node_in";
   public static final String NODE_OUT_VAR = "node_out";
   public static final String NODE_VAR = "node";
   private static final int PORT_BITS = 16;
   private static final int PORT_MAX = 65535;
   private static final int PORT_MIN = 0;
   public static final String SRC_IP_VAR = "src_ip";
   private static final String SRC_NODE_VAR = "src_node";
   public static final String SRC_PORT_VAR = "src_port";

   private static String getAclDenyName(String hostname, String aclName) {
      return "D_acl_" + hostname + "_" + aclName;
   }

   private static String getAclMatchName(String hostname, String aclName,
         int line) {
      return "M_acl_" + hostname + "_" + aclName + "_" + line;
   }

   private static String getAclNoMatchName(String hostname, String aclName,
         int line) {
      return "N_acl_" + hostname + "_" + aclName + "_" + line;
   }

   private static String getAclPermitName(String hostname, String aclName) {
      return "P_acl_" + hostname + "_" + aclName;
   }

   public static String[] getPacketArgs() {
      return new String[] { SRC_NODE_VAR, SRC_IP_VAR, DST_IP_VAR, SRC_PORT_VAR,
            DST_PORT_VAR, IP_PROTOCOL_VAR };
   }

   public static String getPacketVarDecls() {
      StringBuilder sbVarDecl = new StringBuilder(
            ";;; Variable Declarations\n\n");
      sbVarDecl.append("(declare-var " + SRC_NODE_VAR + " (_ BitVec 32) )\n");
      sbVarDecl.append("(declare-var " + SRC_IP_VAR + " (_ BitVec 32) )\n");
      sbVarDecl.append("(declare-var " + DST_IP_VAR + " (_ BitVec 32) )\n");
      sbVarDecl.append("(declare-var " + SRC_PORT_VAR + " (_ BitVec 16) )\n");
      sbVarDecl.append("(declare-var " + DST_PORT_VAR + " (_ BitVec 16) )\n");
      sbVarDecl
            .append("(declare-var " + IP_PROTOCOL_VAR + " (_ BitVec 16) )\n");
      sbVarDecl.append("\n");
      return sbVarDecl.toString();
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

   private static String getPolicyClauseMatchName(String hostname,
         String policyName, int i) {
      return "M_policy_" + hostname + "_" + policyName + "_" + i;
   }

   private static String getPolicyClauseNoMatchName(String hostname,
         String policyName, int i) {
      return "N_policy_" + hostname + "_" + policyName + "_" + i;
   }

   private static String getPolicyDenyName(String hostname, String policyName) {
      return "D_policy_" + hostname + "_" + policyName;
   }

   private static String getPolicyPermitName(String hostname, String policyName) {
      return "P_policy_" + hostname + "_" + policyName;
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

   private final Map<String, Configuration> _configurations;

   private final FibMap _fibs;

   private final InterfaceMap _interfaceNumbers;

   private final int _interfaceWidth;

   private final NodeMap _nodeNumbers;

   private final int _nodeWidth;

   private final Set<String> _packetRelations;

   private final PolicyRouteFibNodeMap _prFibs;

   private final boolean _simplify;

   private final EdgeSet _topologyEdges;

   private final Map<String, Set<Interface>> _topologyInterfaces;

   private final Map<String, Integer> _varSizes;

   public Synthesizer(Map<String, Configuration> configurations, FibMap fibs,
         PolicyRouteFibNodeMap prFibs, EdgeSet topologyEdges, boolean simplify) {
      _configurations = configurations;
      _fibs = fibs;
      _topologyEdges = topologyEdges;
      _packetRelations = new TreeSet<String>();
      _prFibs = prFibs;
      _simplify = simplify;
      _topologyInterfaces = new TreeMap<String, Set<Interface>>();
      computeTopologyInterfaces();
      _varSizes = new LinkedHashMap<String, Integer>();
      _nodeNumbers = new NodeMap();
      _interfaceNumbers = new InterfaceMap();
      initNodeMap();
      _nodeWidth = Util.intWidth(_nodeNumbers.size());
      initInterfaceMap();
      _interfaceWidth = Util.intWidth(_interfaceNumbers.size());
      initVarSizes();
   }

   private BooleanExpr bitvectorGEExpr(String bv, long lb, int numBits) {
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
            IntExpr extractExpr = newExtractExpr(bv, orStartPos, orEndPos);
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
            IntExpr extractExpr = newExtractExpr(bv, andStartPos, andEndPos);
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

   private BooleanExpr bitvectorLEExpr(String bv, long lb, int numBits) {
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
            EqExpr nodeMatches = new EqExpr(new VarIntExpr(NODE_VAR),
                  getNodeNumber(hostname));
            conditions.addConjunct(nodeMatches);
            if (ifaceOutName.equals(FibRow.DROP_INTERFACE)
                  || isLoopbackInterface(ifaceOutName)
                  || isNullInterface(ifaceOutName)) {
               action = new NodeDropExpr(NODE_VAR);
            }
            else {
               action = new PreOutInterfaceExpr(NODE_VAR, INTERFACE_VAR);
               EqExpr interfaceMatches = new EqExpr(new VarIntExpr(
                     INTERFACE_VAR), getInterfaceNumber(ifaceOutName));
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
      statements.add(new Comment(
            "Post out flow sink interface leads to node accept"));
      for (String hostname : _configurations.keySet()) {
         Configuration c = _configurations.get(hostname);
         for (String iface : c.getInterfaces().keySet()) {
            if (iface.startsWith(FLOW_SINK_INTERFACE_PREFIX)) {
               AndExpr conditions = new AndExpr();
               PostOutIfaceExpr postOutIface = new PostOutIfaceExpr(NODE_VAR,
                     INTERFACE_VAR);
               conditions.addConjunct(postOutIface);
               EqExpr nodeMatches = new EqExpr(new VarIntExpr(NODE_VAR),
                     getNodeNumber(hostname));
               conditions.addConjunct(nodeMatches);
               EqExpr interfaceMatches = new EqExpr(new VarIntExpr(NODE_VAR),
                     getInterfaceNumber(iface));
               conditions.addConjunct(interfaceMatches);
               NodeAcceptExpr nodeAccept = new NodeAcceptExpr(NODE_VAR);
               RuleExpr flowSinkAccept = new RuleExpr(conditions, nodeAccept);
               statements.add(flowSinkAccept);
            }
         }
      }
      return statements;
   }

   private IntExpr getInterfaceNumber(String iface) {
      int interfaceNumber = _interfaceNumbers.get(iface);
      return new LitIntExpr(interfaceNumber, _interfaceWidth);
   }

   public InterfaceMap getInterfaceNumbers() {
      return _interfaceNumbers;
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
            String passName = getAclPermitName(hostname, aclName);
            String failName = getAclDenyName(hostname, aclName);
            _packetRelations.add(passName);
            _packetRelations.add(failName);
            List<IpAccessListLine> lines = acl.getLines();
            for (int i = 0; i < lines.size(); i++) {
               String matchName = getAclMatchName(hostname, aclName, i);
               String noMatchName = getAclNoMatchName(hostname, aclName, i);
               _packetRelations.add(matchName);
               _packetRelations.add(noMatchName);
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

               // ** must not match previous rule **
               PacketRelExpr prevNoMatch = null;
               if (i > 0) {
                  String prevNoMatchName = getAclNoMatchName(hostname, aclName,
                        i - 1);
                  prevNoMatch = new PacketRelExpr(prevNoMatchName);
               }

               // / match rule
               AndExpr matchConditions = new AndExpr();
               AndExpr matchLineCriteria = new AndExpr();
               matchConditions.addConjunct(matchLineCriteria);
               if (prevNoMatch != null) {
                  matchConditions.addConjunct(prevNoMatch);
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

               PacketRelExpr match = new PacketRelExpr(matchName);
               RuleExpr matchRule = new RuleExpr(matchConditions, match);
               statements.add(matchRule);

               // / no match rule
               AndExpr noMatchConditions = new AndExpr();
               NotExpr noMatchLineCriteria = new NotExpr(matchLineCriteria);
               noMatchConditions.addConjunct(noMatchLineCriteria);
               if (prevNoMatch != null) {
                  noMatchConditions.addConjunct(prevNoMatch);
               }
               PacketRelExpr noMatch = new PacketRelExpr(noMatchName);
               RuleExpr noMatchRule = new RuleExpr(noMatchConditions, noMatch);
               statements.add(noMatchRule);

               // / pass/fail rule for match
               String pfName;
               switch (line.getAction()) {
               case ACCEPT:
                  pfName = passName;
                  break;

               case REJECT:
                  pfName = failName;
                  break;

               default:
                  throw new Error("invalid action");
               }
               PacketRelExpr passOrFail = new PacketRelExpr(pfName);
               RuleExpr passFailRule = new RuleExpr(match, passOrFail);
               statements.add(passFailRule);

               // / fail rule for no matches
               if (i == lines.size() - 1) {
                  PacketRelExpr fail = new PacketRelExpr(failName);
                  RuleExpr failNoMatchesRule = new RuleExpr(noMatch, fail);
                  statements.add(failNoMatchesRule);
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

   private IntExpr getNodeNumber(String hostname) {
      int nodeNumber = _nodeNumbers.get(hostname);
      return new LitIntExpr(nodeNumber, _nodeWidth);
   }

   public NodeMap getNodeNumbers() {
      return _nodeNumbers;
   }

   private List<Statement> getPolicyRouteRules() {
      List<Statement> statements = new ArrayList<Statement>();
      statements.add(new Comment("Policy-based routing rules"));
      for (Entry<String, Configuration> e : _configurations.entrySet()) {
         String hostname = e.getKey();
         PreOutExpr preOut = new PreOutExpr(NODE_VAR);
         PolicyRouteFibIpMap ipMap = _prFibs.get(hostname);
         Configuration c = e.getValue();
         for (Entry<String, Interface> e2 : c.getInterfaces().entrySet()) {
            String inIfaceName = e2.getKey();
            Interface iface = e2.getValue();
            PostInInterfaceExpr postInIface = new PostInInterfaceExpr(NODE_VAR,
                  INTERFACE_IN_VAR);
            PolicyMap p = iface.getRoutingPolicy();
            if (p != null) {
               String policyName = p.getMapName();
               List<PolicyMapClause> clauses = p.getClauses();
               String policyPermitName = getPolicyPermitName(hostname,
                     policyName);
               _packetRelations.add(policyPermitName);
               String policyDenyName = getPolicyDenyName(hostname, policyName);
               _packetRelations.add(policyDenyName);

               /**
                * For each clause, if we reach that clause, and it is a permit
                * clause, then for each acl matched in the clause, if the packet
                * is permitted by that acl, then the packet is permitted by that
                * clause. If there are no acls to match, then the packet is
                * permitted by that clause.
                */
               for (int i = 0; i < clauses.size(); i++) {
                  PolicyMapClause clause = clauses.get(i);
                  String policyClauseMatchName = getPolicyClauseMatchName(
                        hostname, policyName, i);
                  _packetRelations.add(policyClauseMatchName);
                  String policyClauseNoMatchName = getPolicyClauseNoMatchName(
                        hostname, policyName, i);
                  _packetRelations.add(policyClauseNoMatchName);
                  String policyActionName;
                  PolicyMapAction action = clause.getAction();
                  switch (action) {
                  case PERMIT:
                     policyActionName = policyPermitName;
                     break;
                  case DENY:
                     policyActionName = policyDenyName;
                     break;
                  default:
                     throw new Error("bad action");
                  }
                  PacketRelExpr policyClauseMatch = new PacketRelExpr(
                        policyClauseMatchName);
                  AndExpr preOutIfaceConditions = new AndExpr();
                  preOutIfaceConditions.addConjunct(policyClauseMatch);
                  preOutIfaceConditions.addConjunct(postInIface);
                  preOutIfaceConditions.addConjunct(preOut);
                  EqExpr inIntMatches = new EqExpr(new VarIntExpr(
                        INTERFACE_IN_VAR), getInterfaceNumber(inIfaceName));
                  preOutIfaceConditions.addConjunct(inIntMatches);
                  EqExpr nodeMatches = new EqExpr(new VarIntExpr(NODE_VAR),
                        getNodeNumber(hostname));
                  preOutIfaceConditions.addConjunct(nodeMatches);
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
                              PreOutInterfaceExpr preOutInterface = new PreOutInterfaceExpr(
                                    NODE_VAR, INTERFACE_OUT_VAR);
                              EqExpr nextHopIntMatches = new EqExpr(
                                    new VarIntExpr(INTERFACE_OUT_VAR),
                                    getInterfaceNumber(nextHopInterface));
                              preOutIfaceConditions.addConjunct(nextHopIntMatches);
                              RuleExpr preOutRule = new RuleExpr(
                                    preOutIfaceConditions, preOutInterface);
                              statements.add(preOutRule);
                           }
                        }
                     }
                  }
                  /**
                   * if clausematch, then action
                   */
                  PacketRelExpr policyAction = new PacketRelExpr(
                        policyActionName);
                  RuleExpr matchAction = new RuleExpr(policyClauseMatch,
                        policyAction);
                  statements.add(matchAction);

                  boolean hasMatchIp = false;
                  AndExpr noAclMatchConditions = new AndExpr();
                  String policyPrevClauseNoMatchName = getPolicyClauseNoMatchName(
                        hostname, policyName, i - 1);
                  PacketRelExpr prevNoMatch = new PacketRelExpr(
                        policyPrevClauseNoMatchName);
                  for (PolicyMapMatchLine matchLine : clause.getMatchLines()) {
                     if (matchLine.getType() == PolicyMapMatchType.IP_ACCESS_LIST) {
                        hasMatchIp = true;
                        PolicyMapMatchIpAccessListLine matchIpLine = (PolicyMapMatchIpAccessListLine) matchLine;
                        for (IpAccessList acl : matchIpLine.getLists()) {
                           String aclName = acl.getName();
                           String aclPermitName = getAclPermitName(hostname,
                                 aclName);
                           PacketRelExpr aclPermit = new PacketRelExpr(
                                 aclPermitName);
                           String aclDenyName = getAclDenyName(hostname,
                                 aclName);
                           PacketRelExpr aclDeny = new PacketRelExpr(
                                 aclDenyName);
                           noAclMatchConditions.addConjunct(aclDeny);
                           RuleExpr matchRule;
                           if (i > 0) {
                              /**
                               * if prevnomatch and aclpass, then clausematch
                               */
                              AndExpr clauseMatchConditions = new AndExpr();
                              clauseMatchConditions.addConjunct(prevNoMatch);
                              clauseMatchConditions.addConjunct(aclPermit);
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
                     PacketRelExpr policyClauseNoMatch = new PacketRelExpr(
                           policyClauseNoMatchName);
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
                        trivialMatch = new RuleExpr(prevNoMatch,
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
               String noMatchLastName = getPolicyClauseNoMatchName(hostname,
                     policyName, lastIndex);
               PacketRelExpr noMatchLastClause = new PacketRelExpr(
                     noMatchLastName);
               PacketRelExpr policyDeny = new PacketRelExpr(policyDenyName);
               RuleExpr noMatchDeny = new RuleExpr(noMatchLastClause,
                     policyDeny);
               statements.add(noMatchDeny);
            }
         }
      }
      return statements;
   }

   private List<Statement> getPostInAcceptRules() {
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
               EqExpr nodeMatches = new EqExpr(new VarIntExpr(NODE_VAR),
                     getNodeNumber(hostname));
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

   private List<Statement> getPostInFwdRules() {
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
         EqExpr nodeMatches = new EqExpr(new VarIntExpr(NODE_VAR),
               getNodeNumber(hostname));
         conditions.addConjunct(nodeMatches);
         PreOutExpr preOut = new PreOutExpr(NODE_VAR);
         RuleExpr rule = new RuleExpr(conditions, preOut);
         statements.add(rule);
      }
      return statements;
   }

   private List<Statement> getPostInRules() {
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

   private List<Statement> getPostOutRules() {
      List<Statement> statements = new ArrayList<Statement>();
      statements.add(new Comment(
            "Rules for when preout gets connected to postout, acls"));
      for (String hostname : _topologyInterfaces.keySet()) {
         Set<Interface> interfaces = _topologyInterfaces.get(hostname);
         for (Interface iface : interfaces) {
            String ifaceName = iface.getName();
            if (ifaceName.startsWith(FAKE_INTERFACE_PREFIX)) {
               continue;
            }
            EqExpr nodeMatches = new EqExpr(new VarIntExpr(NODE_VAR),
                  getNodeNumber(hostname));
            EqExpr interfaceMatches = new EqExpr(new VarIntExpr(INTERFACE_VAR),
                  getInterfaceNumber(ifaceName));
            IpAccessList acl = iface.getOutgoingFilter();

            PreOutInterfaceExpr preOutIface = new PreOutInterfaceExpr(NODE_VAR,
                  INTERFACE_VAR);
            AndExpr conditions = new AndExpr();
            conditions.addConjunct(preOutIface);
            conditions.addConjunct(nodeMatches);
            conditions.addConjunct(interfaceMatches);
            if (acl != null) {
               String aclName = acl.getName();
               String aclAcceptName = getAclPermitName(hostname, aclName);
               PacketRelExpr aclAccept = new PacketRelExpr(aclAcceptName);
               conditions.addConjunct(aclAccept);
            }
            PostOutInterfaceExpr postOutIface = new PostOutInterfaceExpr(
                  NODE_VAR, INTERFACE_VAR);
            RuleExpr preOutToPostOut = new RuleExpr(conditions, postOutIface);
            statements.add(preOutToPostOut);
            // failing case
            if (acl != null) {
               String aclName = acl.getName();
               String aclFailName = getAclDenyName(hostname, aclName);
               PacketRelExpr aclFail = new PacketRelExpr(aclFailName);
               NodeDropExpr nodeDrop = new NodeDropExpr(NODE_VAR);
               AndExpr failConditions = new AndExpr();
               failConditions.addConjunct(preOutIface);
               failConditions.addConjunct(aclFail);
               failConditions.addConjunct(nodeMatches);
               failConditions.addConjunct(interfaceMatches);
               RuleExpr aclDrop = new RuleExpr(failConditions, nodeDrop);
               statements.add(aclDrop);
            }
         }
      }
      return statements;
   }

   private List<Statement> getPreInRules() {
      List<Statement> statements = new ArrayList<Statement>();
      statements.add(new Comment(
            "Rules for when prein_iface gets connected to postin, acls"));
      for (String hostname : _topologyInterfaces.keySet()) {
         Set<Interface> interfaces = _topologyInterfaces.get(hostname);
         for (Interface iface : interfaces) {
            String ifaceName = iface.getName();
            if (ifaceName.startsWith(FAKE_INTERFACE_PREFIX)
                  || ifaceName.startsWith(FLOW_SINK_INTERFACE_PREFIX)) {
               continue;
            }
            IpAccessList acl = iface.getIncomingFilter();

            AndExpr conditions = new AndExpr();
            PreInInterfaceExpr preInIface = new PreInInterfaceExpr(NODE_VAR,
                  INTERFACE_VAR);
            conditions.addConjunct(preInIface);
            EqExpr nodeMatches = new EqExpr(new VarIntExpr(NODE_VAR),
                  getNodeNumber(hostname));
            EqExpr interfaceMatches = new EqExpr(new VarIntExpr(INTERFACE_VAR),
                  getInterfaceNumber(ifaceName));
            conditions.addConjunct(nodeMatches);
            conditions.addConjunct(interfaceMatches);
            // passing/null case
            if (acl != null) {
               String aclName = acl.getName();
               String aclAcceptName = getAclPermitName(hostname, aclName);
               PacketRelExpr aclAccept = new PacketRelExpr(aclAcceptName);
               conditions.addConjunct(aclAccept);
            }
            PostInInterfaceExpr postInInterface = new PostInInterfaceExpr(
                  NODE_VAR, INTERFACE_VAR);
            RuleExpr preInToPostIn = new RuleExpr(conditions, postInInterface);
            statements.add(preInToPostIn);
            // failing case
            if (acl != null) {
               String aclName = acl.getName();
               String aclFailName = getAclDenyName(hostname, aclName);
               NodeDropExpr nodeDrop = new NodeDropExpr(NODE_VAR);
               PacketRelExpr aclFail = new PacketRelExpr(aclFailName);
               AndExpr failConditions = new AndExpr();
               failConditions.addConjunct(nodeMatches);
               failConditions.addConjunct(interfaceMatches);
               failConditions.addConjunct(preInIface);
               failConditions.addConjunct(aclFail);
               RuleExpr aclFailDrop = new RuleExpr(failConditions, nodeDrop);
               statements.add(aclFailDrop);
            }
         }
      }
      return statements;
   }

   private List<Statement> getPreOutToDestRouteRules() {
      List<Statement> statements = new ArrayList<Statement>();
      statements.add(new Comment(
            "Rules for sending packets from preout to destroute stage"));
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

      for (String hostname : _fibs.keySet()) {
         Configuration config = _configurations.get(hostname);
         PreOutExpr preOut = new PreOutExpr(NODE_VAR);
         DestinationRouteExpr destRoute = new DestinationRouteExpr(NODE_VAR);
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
               String policyDenyName = getPolicyDenyName(hostname, policyName);
               PacketRelExpr policyDeny = new PacketRelExpr(policyDenyName);
               perInterfaceConditions.addConjunct(policyDeny);
            }
            PostInInterfaceExpr postInIface = new PostInInterfaceExpr(NODE_VAR,
                  INTERFACE_VAR);
            perInterfaceConditions.addConjunct(postInIface);
            EqExpr nodeMatches = new EqExpr(new VarIntExpr(NODE_VAR),
                  getNodeNumber(hostname));
            EqExpr interfaceMatches = new EqExpr(new VarIntExpr(INTERFACE_VAR),
                  getInterfaceNumber(ifaceName));
            perInterfaceConditions.addConjunct(nodeMatches);
            perInterfaceConditions.addConjunct(interfaceMatches);
            RuleExpr perInterfaceRule = new RuleExpr(perInterfaceConditions,
                  destRoute);
            statements.add(perInterfaceRule);
         }
      }
      return statements;
   }

   private List<Statement> getRelDeclExprs() {
      List<Statement> statements = new ArrayList<Statement>();
      _packetRelations.add(AcceptExpr.NAME);
      _packetRelations.add(DropExpr.NAME);
      Comment header = new Comment("Relation declarations");
      statements.add(header);
      List<String> packetVars = getPacketVars();
      List<Integer> packetRelSizes = new ArrayList<Integer>();
      for (String packetVar : packetVars) {
         packetRelSizes.add(_varSizes.get(packetVar));
      }
      for (String packetRelation : _packetRelations) {
         DeclareRelExpr decl = new DeclareRelExpr(packetRelation,
               packetRelSizes);
         statements.add(decl);
      }
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
         EqExpr outNodeExpr = new EqExpr(new VarIntExpr(NODE_OUT_VAR),
               getNodeNumber(hostnameOut));
         EqExpr inNodeExpr = new EqExpr(new VarIntExpr(NODE_IN_VAR),
               getNodeNumber(hostnameIn));
         EqExpr outIntExpr = new EqExpr(new VarIntExpr(INTERFACE_OUT_VAR),
               getInterfaceNumber(intOut));
         EqExpr inIntExpr = new EqExpr(new VarIntExpr(INTERFACE_IN_VAR),
               getInterfaceNumber(intIn));
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

   private void initInterfaceMap() {
      Set<String> interfaceNames = new TreeSet<String>();
      for (Configuration config : _configurations.values()) {
         for (String ifaceName : config.getInterfaces().keySet()) {
            interfaceNames.add(ifaceName);
         }
      }
      int count = 0;
      for (String ifaceName : interfaceNames) {
         _interfaceNumbers.put(ifaceName, count);
         count++;
      }
   }

   private void initNodeMap() {
      int count = 0;
      for (String hostname : _configurations.keySet()) {
         _nodeNumbers.put(hostname, count);
         count++;
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
      _varSizes.put(NODE_IN_VAR, _nodeWidth);
      _varSizes.put(NODE_OUT_VAR, _nodeWidth);
      _varSizes.put(INTERFACE_IN_VAR, _interfaceWidth);
      _varSizes.put(INTERFACE_OUT_VAR, _interfaceWidth);
   }

   private IntExpr newExtractExpr(String var, int low, int high) {
      int varSize = _varSizes.get(var);
      if (low == 0 && high == varSize - 1) {
         return new VarIntExpr(var);
      }
      else {
         return new ExtractExpr(var, low, high);
      }
   }

   public void synthesize(String outputFileStr) throws IOException {
      List<Statement> statements = new ArrayList<Statement>();
      List<Statement> varDecls = getVarDeclExprs();
      List<Statement> dropRules = getDropRules();
      List<Statement> acceptRules = getAcceptRules();
      List<Statement> flowSinkAcceptRules = getFlowSinkAcceptRules();
      List<Statement> postInRules = getPostInRules();
      List<Statement> postInAcceptRules = getPostInAcceptRules();
      List<Statement> postInFwdRules = getPostInFwdRules();
      List<Statement> preOutToDestRouteRules = getPreOutToDestRouteRules();
      List<Statement> destRouteToPreOutIfaceRules = getDestRouteToPreOutIfaceRules();
      List<Statement> policyRouteRules = getPolicyRouteRules();
      List<Statement> matchAclRules = getMatchAclRules();
      List<Statement> toNeighborsRules = getToNeighborsRules();
      List<Statement> preInRules = getPreInRules();
      List<Statement> postOutRules = getPostOutRules();

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
