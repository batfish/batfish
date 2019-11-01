package org.batfish.grammar.flatjuniper;

import static java.util.stream.Collectors.toList;
import static org.glassfish.jersey.internal.guava.Predicates.not;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Deactivate_lineContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Deactivate_line_tailContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Delete_lineContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Delete_line_tailContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Flat_juniper_configurationContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Interface_idContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Set_lineContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Set_line_tailContext;

/**
 * Flat Juniper pre-processor that removes parse tree nodes corresponding to deleted lines, as well
 * as delete statements themselves.
 */
public class Deleter extends FlatJuniperParserBaseListener {

  private boolean _enablePathRecording;
  private boolean _reenablePathRecording;
  private final @Nonnull StatementTree _deactivateStatementTree;
  private final @Nonnull StatementTree _setStatementTree;
  private List<String> _words;
  private Set<ParseTree> _deletedStatements;
  private Multimap<StatementTree, ParseTree> _statementsByTree;

  public Deleter() {
    _deactivateStatementTree = new StatementTree();
    _deletedStatements = new HashSet<>();
    _setStatementTree = new StatementTree();
    _statementsByTree = HashMultimap.create();
  }

  @Override
  public void exitDeactivate_line(Deactivate_lineContext ctx) {
    addStatementToTree(_deactivateStatementTree, ctx);
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
    deleteSubtree(_deactivateStatementTree);
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
  public void enterInterface_id(Interface_idContext ctx) {
    if (_enablePathRecording && (ctx.unit != null || ctx.chnl != null || ctx.node != null)) {
      _enablePathRecording = false;
      _reenablePathRecording = true;
      String text = ctx.getText();
      _words.add(text);
    }
  }

  @Override
  public void exitInterface_id(Interface_idContext ctx) {
    if (_reenablePathRecording) {
      _enablePathRecording = true;
      _reenablePathRecording = false;
    }
  }

  @Override
  public void exitFlat_juniper_configuration(Flat_juniper_configurationContext ctx) {
    ctx.children =
        ctx.children.stream().filter(not(_deletedStatements::contains)).collect(toList());
  }

  @Override
  public void visitTerminal(TerminalNode node) {
    if (_enablePathRecording) {
      _words.add(node.getText());
    }
  }

  private void addStatementToTree(StatementTree tree, ParseTree ctx) {
    StatementTree subtree = tree;
    for (String word : _words) {
      subtree = subtree.getOrAddSubtree(word);
    }
    _statementsByTree.put(subtree, ctx);
  }

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
}
