package org.batfish.grammar.topology;

import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.batfish.representation.Topology;

public interface TopologyExtractor extends ParseTreeListener {

   Topology getTopology();

}
