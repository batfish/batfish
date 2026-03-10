package org.batfish.grammar.flatjuniper;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.batfish.common.Warnings;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.BatfishListener;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Activate_lineContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Activate_line_tailContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Deactivate_lineContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Deactivate_line_tailContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Delete_lineContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Delete_line_tailContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Flat_juniper_configurationContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Insert_dstContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Insert_lineContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Insert_srcContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Replace_lineContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Set_lineContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Set_line_tailContext;
import org.batfish.grammar.hierarchical.StatementTree;

/**
 * Flat Juniper pre-processor that removes parse tree nodes corresponding to deleted lines, as well
 * as delete statements themselves.
 */
@ParametersAreNonnullByDefault
public class InsertDeleteApplicator extends FlatJuniperParserBaseListener
    implements BatfishListener {

  /*
   * Implementation overview:
   *
   * Iterate through each child parse-tree of the configuration. Each corresponds to a set,
   * activate, deactivate, or delete line.
   *
   * Each time a 'set' parse-tree is encountered:
   * - record the words following 'set'
   * - build out the set StatementTree, using each word as a key.
   * - add the parse-tree to the set of parse-trees stored at the node corresponding to the last word
   *
   * Each time an 'activate' or 'deactivate' parse-tree is encountered:
   * - record the words following 'activate'/'deactivate'
   * - find the StatementTree node corresponding to the recorded words
   * - add the parse-tree to the set of parse-trees stored at the node corresponding to the last word
   *
   * Each time an 'insert' parse-tree is encountered:
   * - reorder subtree at common parent by moving the named child before/after target child
   *
   * Each time a 'delete' parse-tree is encountered:
   * - record the words following 'delete'
   * - find the node corresponding to the last word
   * - remove the node (and therefore its subtrees) from the tree
   *   - note that this removes 'set', 'activate', and 'deactivate' lines
   *
   * Each of these statements takes affect when it is reached, rather than after a pass. That way you don't
   * (e.g.) delete set lines that occur later in the text.
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

  @Override
  public @Nonnull String getInputText() {
    return _parser.getInput();
  }

  @Override
  public @Nonnull BatfishCombinedParser<?, ?> getParser() {
    return _parser;
  }

  @Override
  public @Nonnull Warnings getWarnings() {
    return _warnings;
  }

  @Override
  public void enterDeactivate_line_tail(Deactivate_line_tailContext ctx) {
    _enablePathRecording = true;
    _words = new LinkedList<>();
  }

  @Override
  public void exitDeactivate_line_tail(Deactivate_line_tailContext ctx) {
    _enablePathRecording = false;
  }

  @Override
  public void exitDeactivate_line(Deactivate_lineContext ctx) {
    addStatementToTree(_statementTree, ctx);
  }

  @Override
  public void enterActivate_line_tail(Activate_line_tailContext ctx) {
    _enablePathRecording = true;
    _words = new LinkedList<>();
  }

  @Override
  public void exitActivate_line_tail(Activate_line_tailContext ctx) {
    _enablePathRecording = false;
  }

  @Override
  public void exitActivate_line(Activate_lineContext ctx) {
    addStatementToTree(_statementTree, ctx);
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
    deleteSubtree(_statementTree, false);
    _dirty = true;
  }

  @Override
  public void exitReplace_line(Replace_lineContext ctx) {
    // Replace is just delete, except we preserve the parent node so that the replacement lines
    // appear at the same spot in the output config.
    deleteSubtree(_statementTree, true);
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
  public void enterInsert_src(Insert_srcContext ctx) {
    _enablePathRecording = true;
    _words = new LinkedList<>();
  }

  @Override
  public void exitInsert_src(Insert_srcContext ctx) {
    _enablePathRecording = false;
    _insertSrcWords = _words;
  }

  @Override
  public void enterInsert_dst(Insert_dstContext ctx) {
    _enablePathRecording = true;
    _words = new LinkedList<>();
  }

  @Override
  public void exitInsert_dst(Insert_dstContext ctx) {
    _enablePathRecording = false;
    _insertDstWords = _words;
  }

  @Override
  public void enterInsert_line(Insert_lineContext ctx) {
    _currentInsert = ctx;
  }

  @Override
  public void exitInsert_line(Insert_lineContext ctx) {
    if (ctx.BEFORE() != null) {
      moveSubtree(true);
    } else {
      assert ctx.AFTER() != null;
      moveSubtree(false);
    }
    _dirty = true;
    _currentInsert = null;
    _insertSrcWords = null;
    _insertDstWords = null;
  }

  @Override
  public void exitFlat_juniper_configuration(Flat_juniper_configurationContext ctx) {
    if (!_dirty) {
      return;
    }
    // Replace the list of children by dumping statements from a pre-order traversal of the
    // StatementTree.
    //
    // Rather than clearing the list and adding all the statements, keep all the error nodes then
    // add all the statements. This makes sure errors make it to the final output.
    ctx.children =
        ctx.children.stream().filter(c -> c instanceof ErrorNode).collect(Collectors.toList());
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
   * - In delete mode, remove the node (and therefore its subtrees) from tree.
   * - In replace mode, remove only the subtree to preserve order of the node relative to its siblings.
   */
  private void deleteSubtree(StatementTree tree, boolean replace) {
    StatementTree subtree = tree;
    String lastWord = null;
    for (String word : _words) {
      subtree = subtree.getSubtree(word);
      if (subtree == null) {
        return;
      }
      lastWord = word;
    }
    assert lastWord != null;
    subtree
        .getSubtrees()
        .forEach(
            t -> {
              _statementsByTree.removeAll(t);
            });
    assert subtree.getParent() != null;
    if (!replace) {
      subtree.getParent().deleteSubtree(lastWord);
    } else {
      subtree.deleteAllSubtrees();
    }
  }

  /*
   * - Find the node of tree corresponding to _insertSrcWords
   * - Remove the node from the tree
   * - Re-insert the node after or before the node corresponding to _insertDstWords
   *   - note that _insertDstWords only contains a suffix
   */
  private void moveSubtree(boolean before) {
    StatementTree subtree = _statementTree;
    String lastWord = null;
    String lastSrcWord;
    String lastDstWord;
    for (String word : _insertSrcWords) {
      subtree = subtree.getSubtree(word);
      if (subtree == null) {
        warn(_currentInsert, "source does not exist");
        return;
      }
      lastWord = word;
    }
    assert lastWord != null;
    lastSrcWord = lastWord;
    StatementTree treeToMove = subtree;
    for (int i = 0; i < _insertDstWords.size(); i++) {
      subtree = subtree.getParent();
    }
    lastWord = null;
    // make sure dst node exists
    for (String word : _insertDstWords) {
      subtree = subtree.getSubtree(word);
      if (subtree == null) {
        warn(_currentInsert, "destination does not exist");
        return;
      }
      lastWord = word;
    }
    assert lastWord != null;
    lastDstWord = lastWord;
    // make sure there are at least two src words
    int numSrcWords = _insertSrcWords.size();
    int numDstWords = _insertDstWords.size();
    if (numSrcWords < 2) {
      warn(_currentInsert, "source must be at least 2 words");
      return;
    }
    // make sure dst is tail of src except last element
    if (!_insertSrcWords
        .subList(numSrcWords - numDstWords, numSrcWords - 1)
        .equals(_insertDstWords.subList(0, numDstWords - 1))) {
      warn(_currentInsert, "source and destination types do not match");
      return;
    }
    StatementTree parent = treeToMove.getParent();
    parent.deleteSubtree(lastSrcWord);
    if (before) {
      parent.insertBefore(lastDstWord, lastSrcWord, treeToMove);
    } else {
      parent.insertAfter(lastDstWord, lastSrcWord, treeToMove);
    }
  }

  private Insert_lineContext _currentInsert;
  private boolean _dirty;
  private boolean _enablePathRecording;
  private final @Nonnull StatementTree _statementTree;
  private List<String> _words;
  private List<String> _insertSrcWords;
  private List<String> _insertDstWords;
  private final Multimap<StatementTree, ParseTree> _statementsByTree;
  private final @Nonnull BatfishCombinedParser<?, ?> _parser;
  private final @Nonnull Warnings _warnings;
}
