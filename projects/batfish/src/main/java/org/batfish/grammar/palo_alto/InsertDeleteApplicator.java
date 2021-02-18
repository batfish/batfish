package org.batfish.grammar.palo_alto;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.batfish.common.Warnings;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.BatfishListener;
import org.batfish.grammar.hierarchical.StatementTree;
import org.batfish.grammar.palo_alto.PaloAltoParser.Delete_lineContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Delete_line_tailContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Move_actionContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Move_lineContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Move_srcContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Palo_alto_configurationContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Set_lineContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Set_line_tailContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.VariableContext;

/**
 * Flat PaloAlto pre-processor that removes parse tree nodes corresponding to deleted lines, as well
 * as delete statements themselves.
 */
@ParametersAreNonnullByDefault
public class InsertDeleteApplicator extends PaloAltoParserBaseListener implements BatfishListener {

  /*
   * Implementation overview:
   *
   * Iterate through each child parse-tree of the configuration. Each corresponds to a set
   * or delete line.
   *
   * Each time a 'set' parse-tree is encountered:
   * - record the words following 'set'
   * - build out the StatementTree, using each word as a key.
   * - add the parse-tree to the set of parse-trees stored at the node corresponding to the last word
   *
   * Each time a 'delete' parse-tree is encountered:
   * - record the words following 'delete'
   * - find the node corresponding to the last word
   * - collect all parse-trees stored there and in its subtrees
   * - remove the node (and therefore its subtrees) from the tree
   *
   * After visiting all child parse-trees of the configuration, replace its list of children with a
   * new list corresponding to a pre-order traversal of the statement tree.
   */

  public InsertDeleteApplicator(BatfishCombinedParser<?, ?> parser, Warnings warnings) {
    _parser = parser;
    _warnings = warnings;
    _statementTree = new StatementTree();
    _statementsByTree = HashMultimap.create();
  }

  @Nonnull
  @Override
  public String getInputText() {
    return _parser.getInput();
  }

  @Nonnull
  @Override
  public BatfishCombinedParser<?, ?> getParser() {
    return _parser;
  }

  @Nonnull
  @Override
  public Warnings getWarnings() {
    return _warnings;
  }

  @Override
  public void exitSet_line(Set_lineContext ctx) {
    addStatementToTree(_statementTree, ctx);
  }

  @Override
  public void enterSet_line_tail(Set_line_tailContext ctx) {
    _enablePathRecording = true;
    _words = new LinkedList<>();
  }

  @Override
  public void exitSet_line_tail(Set_line_tailContext ctx) {
    _enablePathRecording = false;
  }

  @Override
  public void exitDelete_line(Delete_lineContext ctx) {
    deleteSubtree(_statementTree);
    _dirty = true;
  }

  @Override
  public void enterDelete_line_tail(Delete_line_tailContext ctx) {
    _enablePathRecording = true;
    _words = new LinkedList<>();
  }

  @Override
  public void exitDelete_line_tail(Delete_line_tailContext ctx) {
    _enablePathRecording = false;
  }

  @Override
  public void enterMove_src(Move_srcContext ctx) {
    _enablePathRecording = true;
    _words = new LinkedList<>();
  }

  @Override
  public void exitMove_src(Move_srcContext ctx) {
    _enablePathRecording = false;
    _moveSrcWords = _words;
  }

  @Override
  public void enterMove_line(Move_lineContext ctx) {
    _currentMove = ctx;
  }

  @Override
  public void exitMove_line(Move_lineContext ctx) {
    Move_actionContext action = ctx.move_action();
    if (action.BEFORE() != null) {
      moveSubtreeBeforeAfter(true, action.name);
    } else if (action.AFTER() != null) {
      moveSubtreeBeforeAfter(false, action.name);
    } else if (action.TOP() != null) {
      moveSubtreeTopBottom(true);
    } else {
      assert action.BOTTOM() != null;
      moveSubtreeTopBottom(false);
    }
    _dirty = true;
    _currentMove = null;
    _moveSrcWords = null;
  }

  @Override
  public void exitPalo_alto_configuration(Palo_alto_configurationContext ctx) {
    if (!_dirty) {
      return;
    }
    // Replace the list of children by dumping statements from a pre-order traversal of the
    // StatementTree.
    ctx.children.clear();
    _statementTree.getSubtrees().forEach(tree -> ctx.children.addAll(_statementsByTree.get(tree)));
  }

  @Override
  public void visitTerminal(TerminalNode node) {
    if (_enablePathRecording) {
      _words.add(node.getText());
    }
  }

  /*
   * - Build out a path in tree, using each word as a key.
   * - Add ctx to the set of parse-trees stored at the node corresponding to the last word
   */
  private void addStatementToTree(StatementTree tree, ParseTree ctx) {
    StatementTree subtree = tree;
    for (String word : _words) {
      subtree = subtree.getOrAddSubtree(word);
    }
    _statementsByTree.put(subtree, ctx);
  }

  /*
   * - Find the node corresponding to the last word of tree
   * - Remove the node (and therefore its subtrees) from tree
   */
  private void deleteSubtree(StatementTree tree) {
    StatementTree subtree = tree;
    String lastWord = null;
    for (String word : _words) {
      subtree = subtree.getOrAddSubtree(word);
      lastWord = word;
    }
    assert lastWord != null;
    subtree
        .getSubtrees()
        .forEach(
            t -> {
              _statementsByTree.removeAll(t);
            });
    subtree.getParent().deleteSubtree(lastWord);
  }

  /*
   * - Find the node of tree corresponding to _moveSrcWords
   * - Remove the node from the tree
   * - Re-insert the node after or before the node corresponding to dstCtx
   */
  private void moveSubtreeBeforeAfter(boolean before, VariableContext dstCtx) {
    StatementTree subtree = _statementTree;
    String lastSrcWord = null;
    String dst = dstCtx.getText();
    for (String word : _moveSrcWords) {
      subtree = subtree.getSubtree(word);
      if (subtree == null) {
        warn(_currentMove, "Cannot execute move, source does not exist");
        return;
      }
      lastSrcWord = word;
    }
    assert lastSrcWord != null;
    StatementTree treeToMove = subtree;
    StatementTree parent = treeToMove.getParent();
    // make sure dst node exists
    if (parent.getSubtree(dst) == null) {
      warn(_currentMove, "Cannot execute move, destination does not exist");
      return;
    }
    // make sure there are at least two src words
    int numSrcWords = _moveSrcWords.size();
    if (numSrcWords < 2) {
      warn(_currentMove, "source must be at least 2 words");
      return;
    }
    parent.deleteSubtree(lastSrcWord);
    if (before) {
      parent.insertBefore(dst, lastSrcWord, treeToMove);
    } else {
      parent.insertAfter(dst, lastSrcWord, treeToMove);
    }
  }

  /*
   * - Find the node of tree corresponding to _moveSrcWords
   * - Remove the node from the tree
   * - Re-insert the node at the top or bottom of its original parent
   */
  private void moveSubtreeTopBottom(boolean top) {
    StatementTree subtree = _statementTree;
    String lastSrcWord = null;
    for (String word : _moveSrcWords) {
      subtree = subtree.getSubtree(word);
      if (subtree == null) {
        warn(_currentMove, "Cannot execute move, source does not exist");
        return;
      }
      lastSrcWord = word;
    }
    assert lastSrcWord != null;
    StatementTree treeToMove = subtree;
    StatementTree parent = treeToMove.getParent();
    // make sure there are at least two src words
    int numSrcWords = _moveSrcWords.size();
    if (numSrcWords < 2) {
      warn(_currentMove, "source must be at least 2 words");
      return;
    }
    parent.deleteSubtree(lastSrcWord);
    if (top) {
      parent.insertTop(lastSrcWord, treeToMove);
    } else {
      parent.insertBottom(lastSrcWord, treeToMove);
    }
  }

  private Move_lineContext _currentMove;
  private boolean _dirty;
  private boolean _enablePathRecording;
  private final @Nonnull StatementTree _statementTree;
  private List<String> _words;
  private List<String> _moveSrcWords;
  private Multimap<StatementTree, ParseTree> _statementsByTree;
  private final @Nonnull BatfishCombinedParser<?, ?> _parser;
  private final @Nonnull Warnings _warnings;
}
