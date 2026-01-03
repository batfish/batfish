package org.batfish.datamodel.questions;

// a question that takes an optional nodeRegex parameter
// to specify the set of nodes to analyze
public interface INodeRegexQuestion extends IQuestion {

  NodesSpecifier getNodeRegex();

  void setNodeRegex(NodesSpecifier regex);
}
