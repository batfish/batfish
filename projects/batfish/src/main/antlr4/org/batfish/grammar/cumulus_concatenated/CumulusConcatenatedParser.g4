parser grammar CumulusConcatenatedParser;

options {
  superClass = 'org.batfish.grammar.BatfishParser';
  tokenVocab = CumulusConcatenatedLexer;
}

noop : ANYTHING;
