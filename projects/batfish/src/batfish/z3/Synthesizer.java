package batfish.z3;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;

import batfish.representation.Configuration;
import batfish.representation.Edge;
import batfish.representation.Interface;
import batfish.representation.Ip;
import batfish.representation.IpAccessList;
import batfish.representation.IpAccessListLine;
import batfish.ucla.DeptGenerator;
import batfish.util.SubRange;
import batfish.util.Util;

public class Synthesizer {
   private static final String ACCEPT_RELNAME = "R_accept";
   private static final String DROP_RELNAME = "R_drop";
   public static final String DST_IP_VAR = "dst_ip";
   public static final String DST_PORT_VAR = "dst_port";
   public static final String IP_PROTOCOL_VAR = "ip_prot";
   private static final int PORT_BITS = 16;
   private static final int PORT_MAX = 65535;
   private static final int PORT_MIN = 0;
   public static final String SRC_IP_VAR = "src_ip";
   public static final String SRC_PORT_VAR = "src_port";

   private static List<String> bitvectorGE(String bv, long lb, int numBits) {
      List<String> lines = new ArrayList<String>();
      // these masks refer to nested conditions, not to bitwise and, or
      int ind = 0;
      int numBitsLeft = numBits;

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
            String zero = "#b" + Util.extractBits(0L, orStartPos, orEndPos);
            String orCondition = "(not (= ((_ extract " + orEndPos + " "
                  + orStartPos + ") " + bv + ") " + zero + ") )";
            lines.add(indent(ind) + "(or");
            ind++;
            lines.add(indent(ind) + orCondition + "");
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
            String andMaskStr = "#b"
                  + Util.extractBits(andMask, andStartPos, andEndPos);
            String andCondition = "(= ((_ extract " + andEndPos + " "
                  + andStartPos + ") " + bv + ") " + andMaskStr + ")";
            lines.add(indent(ind) + "(and");
            ind++;
            String parens = "";
            if (numBitsLeft == 0) {
               for (int i = 0; i < ind; i++) {
                  parens += " )";
               }
            }
            lines.add(indent(ind) + andCondition + parens);
         }
      }
      return lines;
   }

   private static List<String> bitvectorLE(String bv, long lb, int numBits) {
      List<String> lines = new ArrayList<String>();
      List<String> geLines = bitvectorGE(bv, lb, numBits);
      int ind = 0;
      lines.add("(or");
      ind++;
      lines.add(indent(ind) + "(= " + bv + " #b"
            + Util.extractBits(lb, 0, numBits - 1) + ")");
      for (int i = 0; i < geLines.size() - 1; i++) {
         String geLine = geLines.get(i);
         lines.add(indent(ind) + geLine);
      }
      String lastGeLine = geLines.get(geLines.size() - 1);
      String parens = "";
      for (int i = 0; i < ind; i++) {
         parens += " )";
      }
      lines.add(indent(ind) + lastGeLine + parens);
      return lines;
   }

   public static String[] getStdArgs() {
      return new String[] {
            SRC_IP_VAR,
            DST_IP_VAR,
            SRC_PORT_VAR,
            DST_PORT_VAR,
            IP_PROTOCOL_VAR };
   }

   private static String getStdArgsText() {
      String output = "";
      String[] stdArgs = getStdArgs();
      for (String arg : stdArgs) {
         output += arg + " ";
      }
      return output.trim();
   }

   public static String getVarDecls() {
      StringBuilder sbVarDecl = new StringBuilder(
            ";;; Variable Declarations\n\n");
      sbVarDecl.append("(declare-var " + SRC_IP_VAR + " (_ BitVec 32) )\n");
      sbVarDecl.append("(declare-var " + DST_IP_VAR + " (_ BitVec 32) )\n");
      sbVarDecl.append("(declare-var " + SRC_PORT_VAR + " (_ BitVec 16) )\n");
      sbVarDecl.append("(declare-var " + DST_PORT_VAR + " (_ BitVec 16) )\n");
      sbVarDecl
            .append("(declare-var " + IP_PROTOCOL_VAR + " (_ BitVec 16) )\n");
      sbVarDecl.append("\n");
      return sbVarDecl.toString();
   }

   public static String indent(int n) {
      String output = "";
      for (int i = 0; i < n; i++) {
         output += "   ";
      }
      return output;
   }

   private Map<String, Configuration> _configurations;
   private Map<String, TreeSet<FibRow>> _fibs;
   private Set<String> _packetRelations;
   private Set<Edge> _topologyEdges;

   private Map<String, Set<Interface>> _topologyInterfaces;

   public Synthesizer(Map<String, Configuration> configurations,
         Map<String, TreeSet<FibRow>> fibs, Set<Edge> topologyEdges) {
      _configurations = configurations;
      _fibs = fibs;
      _topologyEdges = topologyEdges;
      _packetRelations = new TreeSet<String>();
      computeTopologyInterfaces();
   }

   private void computeTopologyInterfaces() {
      _topologyInterfaces = new TreeMap<String, Set<Interface>>();
      for (Edge edge : _topologyEdges) {
         String hostname = edge.getNode1();
         if (!_topologyInterfaces.containsKey(hostname)) {
            _topologyInterfaces.put(hostname, new TreeSet<Interface>());
         }
         Set<Interface> interfaces = _topologyInterfaces.get(hostname);
         String interfaceName = edge.getInt1();
         if (interfaceName.startsWith(DeptGenerator.FAKE_INTERFACE_PREFIX)) {
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
            if (ifaceName.startsWith(DeptGenerator.FLOW_SINK_INTERFACE_PREFIX)) {
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

   private String getDrop() {
      StringBuilder sb = new StringBuilder();
      for (String hostname : _topologyInterfaces.keySet()) {
         String dropName = DROP_RELNAME + "_" + hostname;
         _packetRelations.add(dropName);
         int ind = 0;
         sb.append("(rule\n");
         ind++;
         sb.append(indent(ind) + "(=>\n");
         ind++;
         sb.append(indent(ind) + "(" + dropName + " " + getStdArgsText()
               + ")\n");
         sb.append(indent(ind) + "(" + DROP_RELNAME + " " + getStdArgsText()
               + ") ) )\n");
      }
      return sb.toString();
   }
   
   private String getAccept() {
      StringBuilder sb = new StringBuilder();
      for (String hostname : _configurations.keySet()) {
         String acceptName = ACCEPT_RELNAME + "_" + hostname;
         _packetRelations.add(acceptName);
         int ind = 0;
         sb.append("(rule\n");
         ind++;
         sb.append(indent(ind) + "(=>\n");
         ind++;
         sb.append(indent(ind) + "(" + acceptName + " " + getStdArgsText()
               + ")\n");
         sb.append(indent(ind) + "(" + ACCEPT_RELNAME + " " + getStdArgsText()
               + ") ) )\n");
      }
      return sb.toString();
   }

   private String getFlowSinkAccept() {
      StringBuilder sb = new StringBuilder();
      for (String hostname : _configurations.keySet()) {
         Configuration c = _configurations.get(hostname);
         for (String iface : c.getInterfaces().keySet()) {
            if (iface.startsWith(DeptGenerator.FLOW_SINK_INTERFACE_PREFIX)) {
               String postOutIfaceName = "R_postout_iface_" + hostname + "_"
                     + iface;
               int ind = 0;
               sb.append("(rule\n");
               ind++;
               sb.append(indent(ind) + "(=>\n");
               ind++;
               sb.append(indent(ind) + "(" + postOutIfaceName + " "
                     + getStdArgsText() + ")\n");
               sb.append(indent(ind) + "(" + ACCEPT_RELNAME + "_" + hostname + " "
                     + getStdArgsText() + ") ) )\n");
            }
         }
      }

      return sb.toString();
   }

   private String getMatchAcl() {
      StringBuilder sbMatchAcl = new StringBuilder(
            ";;; Rules for how packets can match acl lines\n\n");
      Map<String, IpAccessList> matchAcls = new TreeMap<String, IpAccessList>();
      // first we find out which acls we need to process
      for (String hostname : _topologyInterfaces.keySet()) {
         String prefix = hostname + "_acl_";
         Set<Interface> interfaces = _topologyInterfaces.get(hostname);
         for (Interface iface : interfaces) {
            if (iface.getIP() != null) {
               IpAccessList aclIn = iface.getIncomingFilter();
               IpAccessList aclOut = iface.getOutgoingFilter();
               if (aclIn != null) {
                  String name = prefix + aclIn.getName();
                  matchAcls.put(name, aclIn);
               }
               if (aclOut != null) {
                  String name = prefix + aclOut.getName();
                  matchAcls.put(name, aclOut);
               }
            }
         }
      }
      for (String name : matchAcls.keySet()) {
         IpAccessList acl = matchAcls.get(name);
         String passName = "P_" + name;
         String failName = "F_" + name;
         _packetRelations.add(passName);
         _packetRelations.add(failName);
         List<IpAccessListLine> lines = acl.getLines();
         for (int i = 0; i < lines.size(); i++) {
            String matchName = "M_" + name + "_" + i;
            String noMatchName = "N_" + name + "_" + i;
            _packetRelations.add(matchName);
            _packetRelations.add(noMatchName);
            IpAccessListLine line = lines.get(i);
            long dstIp = line.getDestinationIP().asLong();
            int dstIpWildcardBits = Util.numWildcardBits(line
                  .getDestinationWildcard().asLong());
            int dstIpStart = dstIpWildcardBits;
            int dstIpEnd = 31;
            String destIpMatchString = "#b"
                  + Util.extractBits(dstIp, dstIpStart, dstIpEnd);

            long srcIp = line.getSourceIP().asLong();
            int srcIpWildcardBits = Util.numWildcardBits(line
                  .getSourceWildcard().asLong());
            int srcIpStart = srcIpWildcardBits;
            int srcIpEnd = 31;
            String srcIpMatchString = "#b"
                  + Util.extractBits(srcIp, srcIpStart, srcIpEnd);

            long protocol = line.getProtocol();
            int maIndent;
            List<SubRange> srcPortRanges = line.getSrcPortRanges();
            List<SubRange> dstPortRanges = line.getDstPortRanges();

            // / match rule
            maIndent = 0;
            sbMatchAcl.append("(rule\n");
            maIndent++;
            sbMatchAcl.append(indent(maIndent) + "(=>\n");
            maIndent++;
            sbMatchAcl.append(indent(maIndent) + "(and\n");
            maIndent++;

            // must not match previous rule
            if (i > 0) {
               String prevNoMatchName = "N_" + name + "_" + (i - 1);
               sbMatchAcl.append(indent(maIndent) + "(" + prevNoMatchName + " "
                     + getStdArgsText() + ")\n");
            }
            // match protocol
            if (protocol != 0) {
               String protocolString = Util.longToZ3Hex16(protocol);
               sbMatchAcl.append(indent(maIndent) + "(= " + IP_PROTOCOL_VAR
                     + " " + protocolString + ")\n");
            }
            // match srcIp
            if (srcIpStart < 32) {
               sbMatchAcl.append(indent(maIndent) + "(= ((_ extract "
                     + srcIpEnd + " " + srcIpStart + ") " + SRC_IP_VAR + ") "
                     + srcIpMatchString + ")\n");
            }
            // match dstIp
            if (dstIpStart < 32) {
               sbMatchAcl.append(indent(maIndent) + "(= ((_ extract "
                     + dstIpEnd + " " + dstIpStart + ") " + DST_IP_VAR + ") "
                     + destIpMatchString + ")\n");
            }
            // match srcport
            getMatchAcl_portHelper(sbMatchAcl, maIndent, srcPortRanges,
                  SRC_PORT_VAR);
            // matchdstport
            getMatchAcl_portHelper(sbMatchAcl, maIndent, dstPortRanges,
                  DST_PORT_VAR);
            sbMatchAcl.append(indent(maIndent) + "true)\n");
            maIndent--;

            sbMatchAcl.append(indent(maIndent) + "(" + matchName + " "
                  + getStdArgsText() + ") ) )\n");

            // / no match rule
            maIndent = 0;
            sbMatchAcl.append("(rule\n");
            maIndent++;
            sbMatchAcl.append(indent(maIndent) + "(=>\n");
            maIndent++;
            sbMatchAcl.append(indent(maIndent) + "(and\n");
            maIndent++;
            if (i > 0) {
               String prevNoMatchName = "N_" + name + "_" + (i - 1);
               sbMatchAcl.append(indent(maIndent) + "(" + prevNoMatchName + " "
                     + getStdArgsText() + ")\n");
            }
            sbMatchAcl.append(indent(maIndent) + "(not\n");
            maIndent++;
            sbMatchAcl.append(indent(maIndent) + "(and\n");
            maIndent++;
            if (protocol != 0) {
               String protocolString = Util.longToZ3Hex16(protocol);
               sbMatchAcl.append(indent(maIndent) + "(= " + IP_PROTOCOL_VAR
                     + " " + protocolString + ")\n");
            }
            if (srcIpStart < 32) {
               sbMatchAcl.append(indent(maIndent) + "(= ((_ extract "
                     + srcIpEnd + " " + srcIpStart + ") " + SRC_IP_VAR + ") "
                     + srcIpMatchString + ")\n");
            }
            if (dstIpStart < 32) {
               sbMatchAcl.append(indent(maIndent) + "(= ((_ extract "
                     + dstIpEnd + " " + dstIpStart + ") " + DST_IP_VAR + ") "
                     + destIpMatchString + ")\n");
            }
            getMatchAcl_portHelper(sbMatchAcl, maIndent, srcPortRanges,
                  SRC_PORT_VAR);
            getMatchAcl_portHelper(sbMatchAcl, maIndent, dstPortRanges,
                  DST_PORT_VAR);
            sbMatchAcl.append(indent(maIndent) + "true) ) )\n");
            maIndent -= 3;

            sbMatchAcl.append(indent(maIndent) + "(" + noMatchName + " "
                  + getStdArgsText() + ") ) )\n");

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
            maIndent = 0;
            sbMatchAcl.append("(rule\n");
            maIndent++;
            sbMatchAcl.append(indent(maIndent) + "(=>\n");
            maIndent++;
            sbMatchAcl.append(indent(maIndent) + "(" + matchName + " "
                  + getStdArgsText() + ")\n");
            sbMatchAcl.append(indent(maIndent) + "(" + pfName + " "
                  + getStdArgsText() + ") ) )\n");

            // / fail rule for no matches
            if (i == lines.size() - 1) {
               maIndent = 0;
               sbMatchAcl.append("(rule\n");
               maIndent++;
               sbMatchAcl.append(indent(maIndent) + "(=>\n");
               maIndent++;
               sbMatchAcl.append(indent(maIndent) + "(" + noMatchName + " "
                     + getStdArgsText() + ")\n");
               sbMatchAcl.append(indent(maIndent) + "(" + failName + " "
                     + getStdArgsText() + ") ) )\n");
            }
         }
      }
      sbMatchAcl.append("\n");
      return sbMatchAcl.toString();
   }

   private void getMatchAcl_portHelper(StringBuilder sbMatchAcl, int maIndent,
         List<SubRange> ranges, String portVar) {
      if (ranges != null) {
         sbMatchAcl.append(indent(maIndent) + "(or\n");
         maIndent++;
         for (SubRange srcPortRange : ranges) {
            long low = srcPortRange.getStart();
            long high = srcPortRange.getEnd();
            if (low == high) {
               String portStr = Util.longToZ3Hex16(low);
               sbMatchAcl.append(indent(maIndent) + "(= " + portVar + " "
                     + portStr + ")\n");
            }
            else {
               boolean doLE = (high < PORT_MAX);
               boolean doGE = (low > PORT_MIN);
               sbMatchAcl.append(indent(maIndent) + "(and\n");
               maIndent++;
               if (doGE) {
                  List<String> geLines = bitvectorGE(portVar, low, PORT_BITS);
                  for (String geLine : geLines) {
                     sbMatchAcl.append(indent(maIndent) + geLine + "\n");
                  }
               }
               if (doLE) {
                  List<String> leLines = bitvectorLE(portVar, high, PORT_BITS);
                  for (String leLine : leLines) {
                     sbMatchAcl.append(indent(maIndent) + leLine + "\n");
                  }
               }
               sbMatchAcl.append(indent(maIndent) + "true)\n");
               maIndent--;
            }
         }
         sbMatchAcl.append(indent(maIndent) + "false)\n");
         maIndent--;
      }

   }

   private String getPostInAccept() {
      StringBuilder sbPostInAccept = new StringBuilder(
            ";;; Rules for when to accept\n\n");
      for (Configuration c : _configurations.values()) {
         String hostname = c.getHostname();
         /**
          * postin ==> preout for each ip address on an interface, accept if
          * destination ip matches
          */
         String postInName = "R_postin_" + hostname;
         String preOutName = "R_preout_" + hostname;
         _packetRelations.add(postInName);
         _packetRelations.add(preOutName);
         for (Interface i : c.getInterfaces().values()) {
            if (i.getName().startsWith(DeptGenerator.FAKE_INTERFACE_PREFIX)
                  || !i.getActive()) {
               continue;
            }
            Ip ip = i.getIP();
            if (ip != null) {
               int piaIndent = 0;
               String dstIp = Util.longToZ3Hex32(ip.asLong());
               sbPostInAccept.append("(rule\n");
               piaIndent++;
               sbPostInAccept.append(indent(piaIndent) + "(=>\n");
               piaIndent++;
               sbPostInAccept.append(indent(piaIndent) + "(and\n");
               piaIndent++;
               sbPostInAccept.append(indent(piaIndent) + "(" + postInName + " "
                     + getStdArgsText() + ")\n");
               sbPostInAccept.append(indent(piaIndent) + "(= " + DST_IP_VAR
                     + " " + dstIp + ") )\n");
               piaIndent--;
               sbPostInAccept.append(indent(piaIndent) + "(" + ACCEPT_RELNAME + "_" + hostname
                     + " " + getStdArgsText() + ") ) )\n");
            }
         }
      }
      sbPostInAccept.append("\n");
      return sbPostInAccept.toString();
   }

   private String getPostInFwd() {
      StringBuilder sbPostInFwd = new StringBuilder(
            ";;; Rules for when to attempt to forward\n\n");
      for (Configuration c : _configurations.values()) {
         String hostname = c.getHostname();

         /**
          * postin ==> preout for each ip address on an interface, accept if
          * destination ip matches
          */
         String postInName = "R_postin_" + hostname;
         String preOutName = "R_preout_" + hostname;
         _packetRelations.add(postInName);
         _packetRelations.add(preOutName);
         int pifIndent = 0;
         sbPostInFwd.append("(rule\n");
         pifIndent++;
         sbPostInFwd.append(indent(pifIndent) + "(=>\n");
         pifIndent++;
         sbPostInFwd.append(indent(pifIndent) + "(and\n");
         pifIndent++;
         for (Interface i : c.getInterfaces().values()) {
            if (i.getName().startsWith(DeptGenerator.FAKE_INTERFACE_PREFIX)) {
               continue;
            }
            Ip ip = i.getIP();
            if (ip != null) {
               String dstIp = Util.longToZ3Hex32(ip.asLong());
               sbPostInFwd.append(indent(pifIndent) + "(not (= " + DST_IP_VAR
                     + " " + dstIp + ") )\n");
            }
         }
         sbPostInFwd.append(indent(pifIndent) + "(" + postInName + " "
               + getStdArgsText() + ") )\n");
         pifIndent--;
         sbPostInFwd.append(indent(pifIndent) + "(" + preOutName + " "
               + getStdArgsText() + ") ) )\n");
      }
      sbPostInFwd.append("\n");
      return sbPostInFwd.toString();
   }

   private String getPostOut() {
      StringBuilder sb = new StringBuilder(
            ";;; Rules for when preout gets connected to postout, acls\n\n");
      for (String hostname : _topologyInterfaces.keySet()) {
         Set<Interface> interfaces = _topologyInterfaces.get(hostname);
         for (Interface iface : interfaces) {
            int ind = 0;
            String ifaceName = iface.getName();
            if (ifaceName.startsWith(DeptGenerator.FAKE_INTERFACE_PREFIX)) {
               continue;
            }
            String preOutIfaceName = "R_preout_iface_" + hostname + "_"
                  + ifaceName;
            String postOutIfaceName = "R_postout_iface_" + hostname + "_"
                  + ifaceName;
            _packetRelations.add(preOutIfaceName);
            _packetRelations.add(postOutIfaceName);
            IpAccessList acl = iface.getOutgoingFilter();

            sb.append("(rule\n");
            ind++;
            sb.append(indent(ind) + "(=>\n");
            ind++;
            if (acl == null) {
               sb.append(indent(ind) + "(" + preOutIfaceName + " "
                     + getStdArgsText() + ")\n");
            }
            else {
               String aclName = acl.getName();
               String aclAcceptName = "P_" + hostname + "_acl_" + aclName;
               // _packetRelations.add(aclAcceptName);
               sb.append(indent(ind) + "(and\n");
               ind++;
               sb.append(indent(ind) + "(" + preOutIfaceName + " "
                     + getStdArgsText() + ")\n");
               sb.append(indent(ind) + "(" + aclAcceptName + " "
                     + getStdArgsText() + ") )\n");
               ind--;
            }
            sb.append(indent(ind) + "(" + postOutIfaceName + " "
                  + getStdArgsText() + ") ) )\n");
            // failing case
            if (acl != null) {
               String dropName = DROP_RELNAME + "_" + hostname;
               ind = 0;
               String aclName = acl.getName();
               String aclFailName = "F_" + hostname + "_acl_" + aclName;
               sb.append("(rule\n");
               ind++;
               sb.append(indent(ind) + "(=>\n");
               ind++;
               sb.append(indent(ind) + "(and\n");
               ind++;
               sb.append(indent(ind) + "(" + preOutIfaceName + " "
                     + getStdArgsText() + ")\n");
               sb.append(indent(ind) + "(" + aclFailName + " "
                     + getStdArgsText() + ") )\n");
               ind--;
               sb.append(indent(ind) + "(" + dropName + " " + getStdArgsText()
                     + ") ) )\n");
            }
         }
      }
      return sb.toString();
   }

   private String getPreIn() {
      StringBuilder sb = new StringBuilder(
            ";;; Rules for when prein_iface gets connected to postin, acls\n\n");
      for (String hostname : _topologyInterfaces.keySet()) {
         Set<Interface> interfaces = _topologyInterfaces.get(hostname);
         for (Interface iface : interfaces) {
            int ind = 0;
            String ifaceName = iface.getName();
            if (ifaceName.startsWith(DeptGenerator.FAKE_INTERFACE_PREFIX)
                  || ifaceName
                        .startsWith(DeptGenerator.FLOW_SINK_INTERFACE_PREFIX)) {
               continue;
            }
            String preInIfaceName = "R_prein_iface_" + hostname + "_"
                  + ifaceName;
            String postInName = "R_postin_" + hostname;
            IpAccessList acl = iface.getIncomingFilter();

            sb.append("(rule\n");
            ind++;
            sb.append(indent(ind) + "(=>\n");
            ind++;
            // passing/null case
            if (acl == null) {
               sb.append(indent(ind) + "(" + preInIfaceName + " "
                     + getStdArgsText() + ")\n");
            }
            else {
               String aclName = acl.getName();
               String aclAcceptName = "P_" + hostname + "_acl_" + aclName;
               sb.append(indent(ind) + "(and\n");
               ind++;
               sb.append(indent(ind) + "(" + preInIfaceName + " "
                     + getStdArgsText() + ")\n");
               sb.append(indent(ind) + "(" + aclAcceptName + " "
                     + getStdArgsText() + ") )\n");
               ind--;
            }
            sb.append(indent(ind) + "(" + postInName + " " + getStdArgsText()
                  + ") ) )\n");
            // failing case
            if (acl != null) {
               ind = 0;
               String dropName = DROP_RELNAME + "_" + hostname;
               String aclName = acl.getName();
               String aclFailName = "F_" + hostname + "_acl_" + aclName;
               sb.append("(rule\n");
               ind++;
               sb.append(indent(ind) + "(=>\n");
               ind++;
               sb.append(indent(ind) + "(and\n");
               ind++;
               sb.append(indent(ind) + "(" + preInIfaceName + " "
                     + getStdArgsText() + ")\n");
               sb.append(indent(ind) + "(" + aclFailName + " "
                     + getStdArgsText() + ") )\n");
               ind--;
               sb.append(indent(ind) + "(" + dropName + " " + getStdArgsText()
                     + ") ) )\n");
            }
         }
      }
      return sb.toString();
   }

   private String getPreOutRoute() {
      StringBuilder sbPreOutRoute = new StringBuilder(
            ";;; Rules for where to forward packet (or drop if no route)\n\n");
      for (String hostname : _fibs.keySet()) {
         String preOutName = "R_preout_" + hostname;
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
            if (currentRow.getInterface().startsWith(
                  DeptGenerator.FAKE_INTERFACE_PREFIX)) {
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
            int porIndent = 0;
            String iface = currentRow.getInterface();
            String preOutIfaceName;
            if (iface.equals(FibRow.DROP_INTERFACE)) {
               String dropName = DROP_RELNAME + "_" + hostname;
               preOutIfaceName = dropName;
            }
            else {
               preOutIfaceName = "R_preout_iface_" + hostname + "_" + iface;
            }
            _packetRelations.add(preOutIfaceName);
            sbPreOutRoute.append("(rule\n");
            porIndent++;
            sbPreOutRoute.append(indent(porIndent) + "(=>\n");
            porIndent++;
            sbPreOutRoute.append(indent(porIndent) + "(and\n");
            porIndent++;
            // must not match more specific routes
            for (FibRow notRow : notRows) {
               int prefixLength = notRow.getPrefixLength();
               long prefix = notRow.getPrefix().asLong();
               int first = 32 - prefixLength;
               if (first >= 32) {
                  continue;
               }
               int last = 31;
               String prefixFragmentString = "#b"
                     + Util.extractBits(prefix, first, last);
               sbPreOutRoute.append(indent(porIndent) + "(not (= ((_ extract "
                     + last + " " + first + ") " + DST_IP_VAR + ") "
                     + prefixFragmentString + ") )\n");
            }

            // must match route
            int prefixLength = currentRow.getPrefixLength();
            long prefix = currentRow.getPrefix().asLong();
            int first = 32 - prefixLength;
            if (first < 32) {
               int last = 31;
               String prefixFragmentString = "#b"
                     + Util.extractBits(prefix, first, last);
               sbPreOutRoute.append(indent(porIndent) + "(= ((_ extract "
                     + last + " " + first + ") " + DST_IP_VAR + ") "
                     + prefixFragmentString + ")\n");
            }

            // must have reached preout
            sbPreOutRoute.append(indent(porIndent) + "(" + preOutName + " "
                  + getStdArgsText() + ") )\n");
            porIndent--;

            // then we forward out specified interface (or drop)
            sbPreOutRoute.append(indent(porIndent) + "(" + preOutIfaceName
                  + " " + getStdArgsText() + ") ) )\n");
         }
      }
      sbPreOutRoute.append("\n");
      return sbPreOutRoute.toString();
   }

   private String getRelDecls() {
      _packetRelations.add(DROP_RELNAME);
      _packetRelations.add(ACCEPT_RELNAME);
      StringBuilder sbDecl = new StringBuilder(";;; Relation declarations\n\n");
      String declTail = "( (_ BitVec 32) (_ BitVec 32) (_ BitVec 16) (_ BitVec 16) (_ BitVec 16) ) )\n";
      for (String packetRelation : _packetRelations) {
         sbDecl.append("(declare-rel " + packetRelation + declTail);
      }
      sbDecl.append("\n");
      return sbDecl.toString();
   }

   private String getToNeighbors() {
      StringBuilder sb = new StringBuilder(";;; Topology edge rules\n\n");
      for (Edge edge : _topologyEdges) {
         String hostnameOut = edge.getNode1();
         String hostnameIn = edge.getNode2();
         String intOut = edge.getInt1();
         String intIn = edge.getInt2();
         if (intIn.startsWith(DeptGenerator.FAKE_INTERFACE_PREFIX)
               || intIn.startsWith(DeptGenerator.FLOW_SINK_INTERFACE_PREFIX)
               || intOut.startsWith(DeptGenerator.FAKE_INTERFACE_PREFIX)
               || intOut.startsWith(DeptGenerator.FLOW_SINK_INTERFACE_PREFIX)) {
            continue;
         }

         String postOutName = "R_postout_iface_" + hostnameOut + "_" + intOut;
         String preInName = "R_prein_iface_" + hostnameIn + "_" + intIn;
         _packetRelations.add(preInName);
         int ind = 0;
         sb.append("(rule\n");
         ind++;
         sb.append(indent(ind) + "(=>\n");
         ind++;
         sb.append(indent(ind) + "(" + postOutName + " " + getStdArgsText()
               + ")\n");
         sb.append(indent(ind) + "(" + preInName + " " + getStdArgsText()
               + ") ) )\n");
      }
      sb.append("\n");
      return sb.toString();
   }

   public void synthesize(String outputFileStr) throws IOException {
      String varDecl = getVarDecls();
      String postInAccept = getPostInAccept();
      String postInFwd = getPostInFwd();
      String preOutRoute = getPreOutRoute();
      String matchAcl = getMatchAcl();
      String postOut = getPostOut();
      String toNeighbors = getToNeighbors();
      String preIn = getPreIn();
      String drop = getDrop();
      String accept = getAccept();
      String flowSinkAccept = getFlowSinkAccept();

      // must be last
      String relDecl = getRelDecls();

      File z3Out = new File(outputFileStr);
      z3Out.delete();
      FileUtils.write(z3Out, varDecl, true);
      FileUtils.write(z3Out, relDecl, true);
      FileUtils.write(z3Out, drop, true);
      FileUtils.write(z3Out, accept, true);
      FileUtils.write(z3Out, postInAccept, true);
      FileUtils.write(z3Out, postInFwd, true);
      FileUtils.write(z3Out, preOutRoute, true);
      FileUtils.write(z3Out, matchAcl, true);
      FileUtils.write(z3Out, toNeighbors, true);
      FileUtils.write(z3Out, preIn, true);
      FileUtils.write(z3Out, postOut, true);
      FileUtils.write(z3Out, flowSinkAccept, true);
   }

}
