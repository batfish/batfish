package org.batfish.representation.juniper;

import javax.annotation.Nonnull;
import org.parboiled.BaseParser;
import org.parboiled.Parboiled;
import org.parboiled.Rule;
import org.parboiled.annotations.BuildParseTree;
import org.parboiled.annotations.SuppressSubnodes;
import org.parboiled.common.StringBuilderSink;
import org.parboiled.parserunners.BasicParseRunner;
import org.parboiled.parserunners.TracingParseRunner;
import org.parboiled.support.ParsingResult;

/**
 * A class that converts Juniper group wilcards to a Java Regex.
 *
 * @see <a
 *     href="https://www.juniper.net/documentation/en_US/junos/topics/topic-map/configuration-groups-usage.html#id-using-wildcards-with-configuration-groups"></a>
 */
@BuildParseTree
@SuppressWarnings({
  "checkstyle:methodname", // this class uses idiomatic names
  "WeakerAccess", // access of Rule methods is needed for parser auto-generation.
})
public class GroupWildcard extends BaseParser<String> {

  Rule TopLevel() {
    return Sequence(
        Sequence(
            Element(), // pop(1)
            ZeroOrMore(
                Element(), // pop()
                push(pop(1) + pop()))),
        EOI);
  }

  Rule Element() {
    return FirstOf(Op_Asterisk(), Op_QuestionMark(), CharacterClass(), AllLiterals());
  }

  Rule CharacterClass() {
    return Sequence(
        Ch('['),
        // TODO: properly handle negation
        Optional(Op_Bang()),
        ClassLiterals(),
        Ch(']'),
        push(String.format("[%s]", pop())));
  }

  /** Characters that can be used inside of a character class */
  Rule ClassLiterals() {
    return Sequence(
        OneOrMore(
            FirstOf(
                CharRange('a', 'z'),
                CharRange('A', 'Z'),
                CharRange('0', '9'),
                Ch('-'),
                Ch('_'),
                Ch(':'))),
        push(match()));
  }

  /**
   * Characters that can be used outside of a character class. Superset of {@link #ClassLiterals()}
   */
  @SuppressSubnodes
  Rule AllLiterals() {
    // Skip special characters until proven otherwise
    // TODO: handle singleton brackets
    Rule base = FirstOf(ClassLiterals(), Dot());
    return Sequence(
        base, // pop()
        ZeroOrMore(base, push(pop(1) + pop())));
  }

  Rule Dot() {
    return Sequence(Ch('.'), push("\\."));
  }

  @SuppressSubnodes
  Rule OpenBracket() {
    return Sequence(Ch('['), push("\\["));
  }

  @SuppressSubnodes
  Rule CloseBracket() {
    return Sequence(Ch(']'), push("\\]"));
  }

  @SuppressSubnodes
  Rule Op_Asterisk() {
    return Sequence(Ch('*'), push(".*"));
  }

  @SuppressSubnodes
  Rule Op_QuestionMark() {
    return Sequence(Ch('?'), push("\\w"));
  }

  @SuppressSubnodes
  Rule Op_Bang() {
    return Sequence(Ch('!'), push("^"));
  }

  public static String toJavaRegex(String wildcard) {
    GroupWildcard parser = Parboiled.createParser(GroupWildcard.class);
    BasicParseRunner<String> runner = new BasicParseRunner<>(parser.TopLevel());
    ParsingResult<String> result = runner.run(wildcard);
    if (!result.matched) {
      throw new IllegalArgumentException("Unhandled input: " + wildcard);
    }
    return result.resultValue;
  }

  /** Like {@link #toJavaRegex(String)}, but for debugging. */
  @SuppressWarnings("unused") // leaving here for future debugging.
  @Nonnull
  static String debugToJavaRegex(String regex) {
    GroupWildcard parser = Parboiled.createParser(GroupWildcard.class);
    TracingParseRunner<String> runner =
        new TracingParseRunner<String>(parser.TopLevel()).withLog(new StringBuilderSink());
    ParsingResult<String> result = runner.run(regex);
    if (!result.matched) {
      throw new IllegalArgumentException("Unhandled input: " + regex + "\n" + runner.getLog());
    }
    return result.resultValue;
  }
}
