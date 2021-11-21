parser grammar SonicParser;

options {
  superClass = 'org.batfish.grammar.BatfishParser';
  tokenVocab = SonicLexer;
}

noop : ANYTHING;
