package batfish.grammar.topology;

import org.antlr.v4.runtime.tree.ParseTreeListener;

import batfish.representation.Topology;

public interface TopologyExtractor extends ParseTreeListener {

   Topology getTopology();

}
