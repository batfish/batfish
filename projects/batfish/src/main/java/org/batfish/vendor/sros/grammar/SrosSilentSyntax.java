package org.batfish.vendor.sros.grammar;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection.SilentSyntaxElem;

/**
 * Sweeps a feature-extracted {@link SrosStatementTree} for statements that were parsed and accepted
 * but never read by {@link SrosFeatureExtractor}, recording each as a {@link SilentSyntaxElem} so
 * the {@code annotate} tool can flag it as "SILENTLY IGNORED".
 *
 * <p>SR-OS has no {@code _null} grammar rules (its grammar is generic — every statement is a word
 * sequence), so the parse-time {@link org.batfish.grammar.SilentSyntaxListener} mechanism used by
 * grammar-driven vendors finds nothing. Instead, the tree's accessors record which nodes the
 * extractor reads (see {@link SrosStatementTree} visit tracking); this sweep reports the rest.
 *
 * <p>Visited nodes form a connected subtree rooted at the (implicitly visited) root: a child is
 * only reachable by reading its parent, which marks the parent. So once a node is unvisited, its
 * whole subtree is unvisited. The sweep descends through visited nodes and, at each unvisited
 * child, emits one element for the maximal ignored statement without descending further — except
 * that a pure path-prefix node (one with no source context of its own, e.g. the {@code ospf}
 * interior word above the keyed instances) is descended into so the report lands on the
 * context-bearing statement(s) below it.
 */
@ParametersAreNonnullByDefault
public final class SrosSilentSyntax {

  /** Synthetic {@link SilentSyntaxElem} rule name; SR-OS has no ANTLR rule to name here. */
  private static final String RULE_NAME = "sros_unmodeled";

  /**
   * Walks {@code root} (assumed visited) and records every maximal unvisited statement into {@code
   * out}. {@code text} is the parsed (preprocessed) configuration text, used to recover a leaf
   * statement's source line.
   */
  public static void sweep(SrosStatementTree root, SilentSyntaxCollection out, String text) {
    new SrosSilentSyntax(out, text).sweepVisited(root, new ArrayDeque<>());
  }

  private SrosSilentSyntax(SilentSyntaxCollection out, String text) {
    _out = out;
    _text = text;
  }

  /** Descends through the children of a visited node, reporting unvisited subtrees. */
  private void sweepVisited(SrosStatementTree node, Deque<String> path) {
    for (Map.Entry<String, SrosStatementTree> e : node.getChildren().entrySet()) {
      SrosStatementTree child = e.getValue();
      path.addLast(e.getKey());
      if (child.wasVisited()) {
        sweepVisited(child, path);
      } else {
        reportIgnored(child, path);
      }
      path.removeLast();
    }
  }

  /**
   * Reports an unvisited subtree. If {@code node} carries a source context it is the ignored
   * statement and is reported as one element; otherwise it is a pure path prefix and the sweep
   * descends to the context-bearing statement(s) beneath it.
   */
  private void reportIgnored(SrosStatementTree node, Deque<String> path) {
    ParserRuleContext ctx = node.firstDefContext();
    if (ctx == null) {
      for (Map.Entry<String, SrosStatementTree> e : node.getChildren().entrySet()) {
        path.addLast(e.getKey());
        reportIgnored(e.getValue(), path);
        path.removeLast();
      }
      return;
    }
    int startLine = ctx.getStart().getLine();
    String elemText;
    if (ctx.getStart().getLine() == ctx.getStop().getLine()) {
      // A single-line leaf statement: report its own text (e.g. `persistent-index file ...`).
      int start = ctx.getStart().getStartIndex();
      int end = ctx.getStop().getStopIndex();
      elemText = _text.substring(start, end + 1);
    } else {
      // A multi-line brace block (e.g. `security { ... }`): report the path to it, not the whole
      // block body, so the comment stays one line.
      elemText = String.join(" ", path);
    }
    _out.addElement(new SilentSyntaxElem(RULE_NAME, startLine, elemText));
  }

  private final @Nonnull SilentSyntaxCollection _out;
  private final @Nonnull String _text;
}
