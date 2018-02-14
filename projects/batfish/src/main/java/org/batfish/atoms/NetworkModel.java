package org.batfish.atoms;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import net.sf.javabdd.BDD;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.collections.FibRow;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.main.Batfish;
import org.batfish.symbolic.bdd.BDDAcl;
import org.batfish.symbolic.bdd.BDDInteger;
import org.batfish.symbolic.bdd.BDDPacket;
import org.batfish.symbolic.utils.Tuple;

public class NetworkModel {

  private Map<NodeInterfacePair, IpAccessList> _outAcls;
  private Map<NodeInterfacePair, IpAccessList> _inAcls;
  private Map<NodeInterfacePair, BitSet> _aclInPredicates;
  private Map<NodeInterfacePair, BitSet> _aclOutPredicates;
  private Map<NodeInterfacePair, BitSet> _forwardingPredicates;
  private ArrayList<BDD> _aclBdds;
  private ArrayList<BDD> _forwardingBdds;
  private Topology _topology;
  private BDDPacket _bddPkt;

  public NetworkModel(Batfish batfish, DataPlane dataPlane) {
    _outAcls = new HashMap<>();
    _inAcls = new HashMap<>();
    _aclInPredicates = new HashMap<>();
    _aclOutPredicates = new HashMap<>();
    _forwardingPredicates = new HashMap<>();
    _aclBdds = new ArrayList<>();
    _forwardingBdds = new ArrayList<>();
    _topology = batfish.getEnvironmentTopology();
    _bddPkt = new BDDPacket();
    Map<String, Configuration> configs = batfish.loadConfigurations();
    long l = System.currentTimeMillis();
    computeAclPredicates(configs);
    computeForwardingPredicates(dataPlane);
    System.out.println("Computing predicates took: " + (System.currentTimeMillis() - l));
    System.out.println("Number ACL atoms: " + _aclBdds.size());
    System.out.println("Number Fwd atoms: " + _forwardingBdds.size());
  }

  // Right now this does 32 exists operations -- one for each srcIp variable
  // It may be more efficent to first create a bdd representing the AND of
  // all variabels and then call exists one time on this bdd.
  private BDD existensialQuantifySrcIp(BDD bdd) {
    BDDInteger srcip = _bddPkt.getSrcIp();
    for (int i = 0; i < srcip.getBitvec().length; i ++) {
      bdd = bdd.exist(_bddPkt.getSrcIp().getBitvec()[i]);
    }
    return bdd;
  }

  private void computeAclPredicates(Map<String, Configuration> configs) {
    long l = System.currentTimeMillis();

    // Collect all the acls
    List<IpAccessList> acls = new ArrayList<>();
    List<Boolean> isOutbound = new ArrayList<>();
    List<NodeInterfacePair> nips = new ArrayList<>();

    for (Configuration config : configs.values()) {
      for (Interface iface : config.getInterfaces().values()) {
        IpAccessList in = iface.getIncomingFilter();
        IpAccessList out = iface.getOutgoingFilter();
        NodeInterfacePair nip = new NodeInterfacePair(config.getName(), iface.getName());
        if (in != null) {
          _inAcls.put(nip, in);
          acls.add(in);
          isOutbound.add(false);
          nips.add(nip);
        }
        if (out != null) {
          _outAcls.put(nip, out);
          acls.add(out);
          isOutbound.add(true);
          nips.add(nip);
        }
      }
    }

    // Sort ACLs increasing by size
    acls.sort(Comparator.comparingInt(a -> a.getLines().size()));

    List<Atom> atoms = new ArrayList<>();
    for (int i = 0; i < acls.size(); i++) {
      IpAccessList acl = acls.get(i);
      BDDAcl aclBdd = BDDAcl.create(null, acl, false);
      atoms.add(new Atom(i, aclBdd.getBdd()));
    }

    Set<Atom> allPredicates = computeAtomicPredicates(atoms);

    // Create bitsets for each interface
    int i = 0;
    for (Atom pred : allPredicates) {
      _aclBdds.add(pred.getBdd());
      for (Integer label : pred.getLabels()) {
        Boolean out = isOutbound.get(label);
        NodeInterfacePair nip = nips.get(label);
        if (out) {
          BitSet b = _aclOutPredicates.computeIfAbsent(nip, k -> new BitSet());
          b.set(i);
          _aclOutPredicates.put(nip, b);
        } else {
          BitSet b = _aclInPredicates.computeIfAbsent(nip, k -> new BitSet());
          b.set(i);
          _aclInPredicates.put(nip, b);
        }
      }
      i++;
    }
  }

  private void computeForwardingPredicates(DataPlane dp) {
    // create a BDD for each port
    Map<NodeInterfacePair, Atom> forwarding = new HashMap<>();
    List<Atom> atoms = new ArrayList<>();
    List<NodeInterfacePair> nips = new ArrayList<>();

    for (Entry<String, Map<String, SortedSet<FibRow>>> entry : dp.getFibs().entrySet()) {
      String router = entry.getKey();
      // for each router, compute the
      List<Tuple<NodeInterfacePair, FibRow>> fibsForRouter = new ArrayList<>();

      Set<NodeInterfacePair> allNips = new HashSet<>();
      for (Entry<String, SortedSet<FibRow>> entry2 : entry.getValue().entrySet()) {
        SortedSet<FibRow> fibs = entry2.getValue();
        for (FibRow fib : fibs) {
          NodeInterfacePair nip = new NodeInterfacePair(router, fib.getInterface());
          fibsForRouter.add(new Tuple<>(nip, fib));
          allNips.add(nip);
        }
      }

      // sort by decreasing prefix length
      fibsForRouter.sort(
          (t1, t2) -> {
            int len1 = t1.getSecond().getPrefix().getPrefixLength();
            int len2 = t2.getSecond().getPrefix().getPrefixLength();
            return len2 - len1;
          });

      // Compute the BDDs
      Map<NodeInterfacePair, BDD> fwdPredicates = new HashMap<>(allNips.size());
      for (NodeInterfacePair nip : allNips) {
        fwdPredicates.put(nip, BDDPacket.factory.zero());
      }
      BDD fwd = BDDPacket.factory.zero();
      for (Tuple<NodeInterfacePair, FibRow> tup : fibsForRouter) {
        NodeInterfacePair nip = tup.getFirst();
        FibRow fib = tup.getSecond();
        BDD port = fwdPredicates.get(nip);
        BDD pfx = destinationIpInPrefix(fib.getPrefix());
        BDD newPort = port.or(pfx.and(fwd.not()));
        fwd = fwd.or(pfx);
        fwdPredicates.put(nip, newPort);
      }

      // create the atoms
      for (Entry<NodeInterfacePair, BDD> e : fwdPredicates.entrySet()) {
        NodeInterfacePair nip = e.getKey();
        BDD bdd = e.getValue();
        Atom a = new Atom(atoms.size(), bdd);
        atoms.add(a);
        nips.add(nip);
      }
    }

    Set<Atom> disjointAtoms = computeAtomicPredicates(atoms);

    // Create bitsets for each interface
    int i = 0;
    for (Atom pred : disjointAtoms) {
      _forwardingBdds.add(pred.getBdd());
      for (Integer label : pred.getLabels()) {
        NodeInterfacePair nip = nips.get(label);
        BitSet b = _forwardingPredicates.computeIfAbsent(nip, k -> new BitSet());
        b.set(i);
        _forwardingPredicates.put(nip, b);
      }
      i++;
    }
  }

  /*
   * Does the 32 bit integer match the prefix using lpm?
   * Here the 32 bits are all symbolic variables
   */
  private BDD destinationIpInPrefix(Prefix p) {
    BDD[] bits = _bddPkt.getDstIp().getBitvec();
    BitSet b = p.getStartIp().getAddressBits();
    BDD acc = BDDPacket.factory.one();
    for (int i1 = 0; i1 < p.getPrefixLength(); i1++) {
      boolean res = b.get(i1);
      if (res) {
        acc = acc.and(bits[i1]);
      } else {
        acc = acc.and(bits[i1].not());
      }
    }
    return acc;
  }

  // P<l>        and {R1<ls1>, ..., Rn<lsn>}  == {(P and R1)<l::ls1>, ..., (P and Rn)<l::lsn>}
  // (not p)<l>  and {R1<ls1>, ..., Rn<lsn>}  ==
  @Nonnull
  private Set<Atom> computeAtomicPredicates(List<Atom> atoms) {
    Set<Atom> allPredicates = new HashSet<>();
    if (!atoms.isEmpty()) {
      Atom initial = atoms.get(0);
      Atom initialNot = new Atom(initial.getBdd().not());
      allPredicates.add(initial);
      allPredicates.add(initialNot);
      for (int i = 1; i < atoms.size(); i++) {
        Atom current = atoms.get(i);
        Set<Atom> newBdds = new HashSet<>();
        for (Atom pred : allPredicates) {
          BDD x = pred.getBdd().and(current.getBdd());
          if (!x.isZero()) {
            Set<Integer> labels = new HashSet<>(pred.getLabels());
            labels.addAll(current.getLabels());
            Atom a = new Atom(labels, x);
            newBdds.add(a);
          }
        }
        if (!newBdds.isEmpty()) {
          for (Atom pred : allPredicates) {
            BDD x = pred.getBdd().and(current.getBdd().not());
            if (!x.isZero()) {
              Atom a = new Atom(pred.getLabels(), x);
              newBdds.add(a);
            }
          }
          allPredicates = newBdds;
        }
      }
    }
    return allPredicates;
  }
}
