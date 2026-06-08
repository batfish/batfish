package org.batfish.vendor.sros.grammar;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.batfish.common.Warnings;
import org.batfish.common.Warnings.ParseWarning;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.SilentSyntaxListener;
import org.batfish.grammar.UnrecognizedLineToken;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.vendor.sros.grammar.SrosParser.Bracketed_clauseContext;
import org.batfish.vendor.sros.grammar.SrosParser.StatementContext;
import org.batfish.vendor.sros.representation.SrosConfiguration;

/**
 * Walks an SR-OS parse tree and records the configuration's canonical absolute-path statements into
 * a {@link SrosConfiguration}.
 *
 * <p>The grammar accepts the brace/hierarchical form, the flat {@code /configure ...} form, and a
 * mix of the two. This builder normalizes all of them to one canonical form: for every configured
 * leaf (or empty block) it emits a single space-joined path string from the implicit root, e.g.
 * {@code configure router "Base" bgp router-id 1.1.1.1}. A leading {@code /configure} from the flat
 * form is normalized to {@code configure} so that the three input forms produce identical output.
 */
@ParametersAreNonnullByDefault
public final class SrosConfigurationBuilder extends SrosParserBaseListener
    implements SilentSyntaxListener {

  public SrosConfigurationBuilder(
      SrosCombinedParser parser,
      String text,
      Warnings warnings,
      SilentSyntaxCollection silentSyntax) {
    _parser = parser;
    _text = text;
    _c = new SrosConfiguration();
    _w = warnings;
    _silentSyntax = silentSyntax;
    _stack = new ArrayDeque<>();
  }

  @Override
  public void enterStatement(StatementContext ctx) {
    _stack.addLast(joinWords(ctx));
  }

  @Override
  public void exitStatement(StatementContext ctx) {
    // Emit a canonical line for a leaf statement (no block) or an empty block. A non-empty block's
    // presence is implied by its children, so it gets no line of its own (mirrors Juniper
    // flattening).
    boolean isLeaf = ctx.block() == null;
    boolean isEmptyBlock = ctx.block() != null && ctx.block().statement().isEmpty();
    if (isLeaf || isEmptyBlock) {
      _c.getStatements().add(canonicalLine(ctx));
    }
    _stack.removeLast();
  }

  /** Build the full canonical path string for a leaf/empty-block statement {@code ctx}. */
  private @Nonnull String canonicalLine(StatementContext ctx) {
    String line = String.join(" ", _stack);
    Bracketed_clauseContext bracket = ctx.bracketed_clause();
    if (bracket != null && !bracket.word().isEmpty()) {
      line =
          line
              + " [ "
              + bracket.word().stream()
                  .map(ParserRuleContext::getText)
                  .collect(Collectors.joining(" "))
              + " ]";
    }
    // Normalize the flat form's leading "/configure" to the brace form's "configure".
    if (line.startsWith("/")) {
      line = line.substring(1);
    }
    maybeSetHostname(line);
    return line;
  }

  /** The SR-OS system name maps to the Batfish hostname: {@code configure system name "<name>"}. */
  private void maybeSetHostname(String line) {
    String prefix = "configure system name ";
    if (line.startsWith(prefix)) {
      _c.setHostname(unquote(line.substring(prefix.length()).trim()));
    }
  }

  private static @Nonnull String joinWords(StatementContext ctx) {
    return ctx.word().stream().map(ParserRuleContext::getText).collect(Collectors.joining(" "));
  }

  private static @Nonnull String unquote(String text) {
    if (text.length() >= 2 && text.charAt(0) == '"' && text.charAt(text.length() - 1) == '"') {
      return text.substring(1, text.length() - 1);
    }
    return text;
  }

  @Override
  public void visitErrorNode(ErrorNode errorNode) {
    Token token = errorNode.getSymbol();
    int line = token.getLine();
    String lineText = errorNode.getText().replace("\n", "").replace("\r", "").trim();
    _c.setUnrecognized(true);

    if (token instanceof UnrecognizedLineToken) {
      UnrecognizedLineToken unrecToken = (UnrecognizedLineToken) token;
      _w.getParseWarnings()
          .add(
              new ParseWarning(
                  line, lineText, unrecToken.getParserContext(), "This syntax is unrecognized"));
    } else {
      _w.redFlagf(
          "Unrecognized Line: %d: %s SUBSEQUENT LINES MAY NOT BE PROCESSED CORRECTLY",
          line, lineText);
    }
  }

  @Override
  public void exitEveryRule(ParserRuleContext ctx) {
    tryProcessSilentSyntax(ctx);
  }

  @Override
  public @Nonnull SilentSyntaxCollection getSilentSyntax() {
    return _silentSyntax;
  }

  @Override
  public @Nonnull String getInputText() {
    return _text;
  }

  @Override
  public @Nonnull BatfishCombinedParser<?, ?> getParser() {
    return _parser;
  }

  @Override
  public @Nonnull Warnings getWarnings() {
    return _w;
  }

  public @Nonnull SrosConfiguration getConfiguration() {
    return _c;
  }

  private final @Nonnull SrosCombinedParser _parser;
  private final @Nonnull String _text;
  private final @Nonnull SrosConfiguration _c;
  private final @Nonnull Warnings _w;
  private final @Nonnull SilentSyntaxCollection _silentSyntax;

  /** Stack of space-joined word segments for the currently-open statements, root first. */
  private final @Nonnull Deque<String> _stack;
}
