package org.batfish.grammar.palo_alto;

import static com.google.common.base.Predicates.not;
import static java.util.stream.Collectors.toList;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.batfish.grammar.hierarchical.StatementTree;
import org.batfish.grammar.palo_alto.PaloAltoParser.Delete_lineContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Delete_line_tailContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Palo_alto_configurationContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Set_lineContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Set_line_tailContext;

/**
 * Flat PaloAlto pre-processor that removes parse tree nodes corresponding to deleted lines, as well
 * as delete statements themselves.
 */
@ParametersAreNonnullByDefault
public class Deleter extends PaloAltoParserBaseListener {

  /*
   * Implementation overview:
   *
   * Iterate through each child parse-tree of the configuration. Each corresponds to a set
   * or delete line.
   *
   * Each time a 'set' parse-tree is encountered:
   * - record the words following 'set'
   * - build out the deactivate (or set) StatementTree, using each word as a key.
   * - add the parse-tree to the set of parse-trees stored at the node corresponding to the last word
   *
   * Each time a 'delete' parse-tree is encountered:
   * - record the words following 'delete'
   * - for 'set' StatementTrees
   *   - find the node corresponding to the last word
   *   - collect all parse-trees stored there and in its subtrees
   *   - mark those parse-trees as deleted
   *   - remove the node (and therefore its subtrees) from the tree
   * - Mark the 'delete' parse-tree itself as deleted
   *
   * After visiting all child parse-trees of the configuration, replace its list of children with a
   * new list containing only those parse-trees not marked for deletion.
   */

  public Deleter() {
    _deletedStatements = new HashSet<>();
    _setStatementTree = new StatementTree();
    _statementsByTree = HashMultimap.create();
  }

  @Override
  public void exitSet_line(Set_lineContext ctx) {
    addStatementToTree(_setStatementTree, ctx);
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
    _deletedStatements.add(ctx);
    deleteSubtree(_setStatementTree);
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
  public void exitPalo_alto_configuration(Palo_alto_configurationContext ctx) {
    // Replace the list of children with a new list containing only those nodes not marked for
    // deletion.
    ctx.children =
        ctx.children.stream().filter(not(_deletedStatements::contains)).collect(toList());
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
   * - Mark for deletion all parse-trees stored there and in its subtrees
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
              _deletedStatements.addAll(_statementsByTree.get(t));
              _statementsByTree.removeAll(t);
            });
    subtree.getParent().deleteSubtree(lastWord);
  }

  private boolean _enablePathRecording;
  private final @Nonnull StatementTree _setStatementTree;
  private List<String> _words;
  private Set<ParseTree> _deletedStatements;
  private Multimap<StatementTree, ParseTree> _statementsByTree;
}
