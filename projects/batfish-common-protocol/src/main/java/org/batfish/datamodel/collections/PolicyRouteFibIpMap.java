package org.batfish.datamodel.collections;

import java.util.HashMap;
import java.util.SortedSet;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.Ip;

public class PolicyRouteFibIpMap extends HashMap<Ip, SortedSet<Edge>> {

  private static final long serialVersionUID = 1L;
}
