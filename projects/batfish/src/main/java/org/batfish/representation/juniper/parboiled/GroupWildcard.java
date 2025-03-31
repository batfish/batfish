package org.batfish.representation.juniper.parboiled;

import static com.google.common.base.Preconditions.checkArgument;

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
 * A class that converts Juniper group wildcards to a Java Regex.
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
                push(String.format("%s%s", pop(1), pop())))),
        EOI);
  }

  Rule Element() {
    return FirstOf(
        Op_Asterisk(),
        Op_QuestionMark(),
        CharacterClass(),
        AllLiterals(),
        OpenBracket(),
        CloseBracket());
  }

  Rule CharacterClass() {
    return Sequence(
        Ch('['),
        Sequence(
            push(""),
            Optional(FirstOf(Op_Bang(), Op_Carat()), push(String.format("%s%s", pop(1), pop())))),
        ClassLiterals(),
        Ch(']'),
        push(String.format("[%s%s]", pop(1), pop())));
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
                Ch(':'),
                Ch('/'))),
        push(match()));
  }

  /**
   * Characters that can be used outside of a character class. Superset of {@link #ClassLiterals()}
   */
  @SuppressSubnodes
  Rule AllLiterals() {
    // Skipping special characters until proven otherwise
    Rule base = FirstOf(ClassLiterals(), Dot());
    return Sequence(
        base, // pop()
        ZeroOrMore(base, push(String.format("%s%s", pop(1), pop()))));
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

  /**
   * This is the same as {@link #Op_Bang()}. It is not documented as supported by Juniper
   * (https://www.juniper.net/documentation/en_US/junos/topics/topic-map/configuration-groups-usage.html),
   * but it is reported to work in open-source user configs
   * (https://github.com/batfish/batfish/issues/6059).
   */
  @SuppressSubnodes
  Rule Op_Carat() {
    return Sequence(Ch('^'), push("^"));
  }

  public static String toJavaRegex(String wildcard) {
    if (wildcard.isEmpty()) {
      return "";
    }
    GroupWildcard parser = Parboiled.createParser(GroupWildcard.class);
    BasicParseRunner<String> runner = new BasicParseRunner<>(parser.TopLevel());
    ParsingResult<String> result = runner.run(wildcard);
    checkArgument(result.matched, "Unhandled input: <%s>", wildcard);
    return result.resultValue;
  }

  /** Like {@link #toJavaRegex(String)}, but for debugging. */
  @SuppressWarnings("unused") // leaving here for future debugging.
  static @Nonnull String debugToJavaRegex(String regex) {
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
