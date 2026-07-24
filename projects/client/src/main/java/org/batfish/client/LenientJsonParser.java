package org.batfish.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.math.BigDecimal;

/**
 * Parses the lenient, {@code org.json}-flavored JSON that the Batfish client CLI historically
 * accepted for question parameters. This is <b>not</b> strict JSON: it tolerates the same
 * non-standard forms the CLI relied on when it used Jettison to parse parameter lines, namely
 *
 * <ul>
 *   <li>unquoted object keys (e.g. {@code {nodes: "as1.*"}}),
 *   <li>{@code =} and {@code =>} as key/value separators in addition to {@code :},
 *   <li>{@code ;} as a pair separator in addition to {@code ,},
 *   <li>single-quoted strings,
 *   <li>case-insensitive {@code true}/{@code false}/{@code null},
 *   <li>bare unquoted scalar values, and
 *   <li>slash-slash, slash-star, and {@code #} comments.
 * </ul>
 *
 * <p>The grammar is ported from Jettison's {@code JSONTokener}/{@code JSONObject}/{@code JSONArray}
 * so that CLI parameter parsing behaves identically after removing that dependency. The result is
 * emitted as Jackson {@link JsonNode}s.
 */
final class LenientJsonParser {

  /** Thrown on malformed lenient-JSON input. */
  static final class LenientJsonException extends Exception {
    LenientJsonException(String message) {
      super(message);
    }
  }

  private static final JsonNodeFactory NF = JsonNodeFactory.instance;

  private final String _source;
  private int _index;

  private LenientJsonParser(String source) {
    _source = source.trim();
    _index = 0;
  }

  /** Parses {@code text} as a single lenient-JSON value. */
  static JsonNode parse(String text) throws LenientJsonException {
    return new LenientJsonParser(text).nextValue();
  }

  private boolean more() {
    return _index < _source.length();
  }

  private char next() {
    if (more()) {
      return _source.charAt(_index++);
    }
    return 0;
  }

  private void back() {
    if (_index > 0) {
      _index -= 1;
    }
  }

  private String next(int n) throws LenientJsonException {
    int i = _index;
    int j = i + n;
    if (j >= _source.length()) {
      throw error("Substring bounds error");
    }
    _index += n;
    return _source.substring(i, j);
  }

  /** Get the next char, skipping whitespace and comments (slash-slash, slash-star, and hash). */
  private char nextClean() throws LenientJsonException {
    for (; ; ) {
      char c = next();
      if (c == '/') {
        switch (next()) {
          case '/':
            do {
              c = next();
            } while (c != '\n' && c != '\r' && c != 0);
            break;
          case '*':
            for (; ; ) {
              c = next();
              if (c == 0) {
                throw error("Unclosed comment.");
              }
              if (c == '*') {
                if (next() == '/') {
                  break;
                }
              }
            }
            break;
          default:
            if (!more()) {
              throw error("The JSON text is malformed");
            }
            back();
            return '/';
        }
      } else if (c == '#') {
        do {
          c = next();
        } while (c != '\n' && c != '\r' && c != 0);
      } else if (c == 0 || c > ' ') {
        return c;
      }
    }
  }

  /** Return the characters up to the next close quote character, with backslash processing. */
  private String nextString(char quote) throws LenientJsonException {
    char c;
    StringBuilder sb = new StringBuilder();
    for (; ; ) {
      c = next();
      switch (c) {
        case 0:
        case '\n':
        case '\r':
          throw error("Unterminated string");
        case '\\':
          c = next();
          switch (c) {
            case 'b':
              sb.append('\b');
              break;
            case 't':
              sb.append('\t');
              break;
            case 'n':
              sb.append('\n');
              break;
            case 'f':
              sb.append('\f');
              break;
            case 'r':
              sb.append('\r');
              break;
            case 'u':
              try {
                sb.append((char) Integer.parseInt(next(4), 16));
              } catch (NumberFormatException e) {
                throw error("Illegal escape.");
              }
              break;
            default:
              sb.append(c);
          }
          break;
        default:
          if (c == quote) {
            return sb.toString();
          }
          sb.append(c);
      }
    }
  }

  /** Get the next value: an object, array, string, boolean, null, number, or bare string. */
  private JsonNode nextValue() throws LenientJsonException {
    char c = nextClean();
    switch (c) {
      case '"':
      case '\'':
        return NF.textNode(nextString(c));
      case '{':
        back();
        return parseObject();
      case '[':
        back();
        return parseArray();
      default:
        break;
    }

    // Handle unquoted text: true/false/null, a number, or a bare string. Accumulate characters
    // until we reach the end of the text or a formatting character.
    StringBuilder sb = new StringBuilder();
    char b = c;
    while (c >= ' ' && ",:]}/\\\"[{;=#".indexOf(c) < 0) {
      sb.append(c);
      c = next();
    }
    back();

    String s = sb.toString().trim();
    if (s.isEmpty()) {
      throw error("Missing value.");
    }
    return toValueNode(s, b);
  }

  /** Convert an unquoted token to the appropriate {@link JsonNode}, mirroring Jettison. */
  private static JsonNode toValueNode(String s, char firstChar) {
    if (s.equalsIgnoreCase("true")) {
      return NF.booleanNode(true);
    }
    if (s.equalsIgnoreCase("false")) {
      return NF.booleanNode(false);
    }
    if (s.equalsIgnoreCase("null")) {
      return NF.nullNode();
    }

    // If it might be a number, try converting it. We support the 0- (octal) and 0x- (hex)
    // conventions, matching Jettison. If a number cannot be produced, the value is a string.
    if ((firstChar >= '0' && firstChar <= '9')
        || firstChar == '.'
        || firstChar == '-'
        || firstChar == '+') {
      if (firstChar == '0') {
        if (s.length() > 2 && (s.charAt(1) == 'x' || s.charAt(1) == 'X')) {
          try {
            return NF.numberNode(Integer.parseInt(s.substring(2), 16));
          } catch (NumberFormatException e) {
            // fall through
          }
        } else {
          try {
            return NF.numberNode(Integer.parseInt(s, 8));
          } catch (NumberFormatException e) {
            // fall through
          }
        }
      }
      try {
        return NF.numberNode(Integer.parseInt(s));
      } catch (NumberFormatException e) {
        try {
          return NF.numberNode(Long.parseLong(s));
        } catch (NumberFormatException f) {
          try {
            return NF.numberNode(new BigDecimal(s));
          } catch (NumberFormatException g) {
            return NF.textNode(s);
          }
        }
      }
    }
    return NF.textNode(s);
  }

  /**
   * Parse a lenient object, tolerating {@code =}/{@code =>} and {@code :}, and {@code ;}/{@code ,}.
   */
  private ObjectNode parseObject() throws LenientJsonException {
    ObjectNode object = NF.objectNode();
    char c;
    String key;

    if (nextClean() != '{') {
      throw error("A JSONObject text must begin with '{'");
    }
    for (; ; ) {
      c = nextClean();
      switch (c) {
        case 0:
          throw error("A JSONObject text must end with '}'");
        case '}':
          return object;
        case '{':
        case '[':
          throw error("Expected a key");
        default:
          back();
          key = nextValueAsKey();
      }

      // The key is followed by ':'. We also tolerate '=' or '=>'.
      c = nextClean();
      if (c == '=') {
        if (next() != '>') {
          back();
        }
      } else if (c != ':') {
        throw error("Expected a ':' after a key");
      }
      object.set(key, nextValue());

      // Pairs are separated by ','. We also tolerate ';'.
      switch (nextClean()) {
        case ';':
        case ',':
          if (nextClean() == '}') {
            return object;
          }
          back();
          break;
        case '}':
          return object;
        default:
          throw error("Expected a ',' or '}'");
      }
    }
  }

  /**
   * Reads the next value as an object key. Jettison stringifies whatever {@code nextValue} returns;
   * for a key that is a bare token or quoted string that is the token's text.
   */
  private String nextValueAsKey() throws LenientJsonException {
    JsonNode node = nextValue();
    return node.isValueNode() ? node.asText() : node.toString();
  }

  /** Parse a lenient array, tolerating {@code ;}/{@code ,} separators and holes. */
  private ArrayNode parseArray() throws LenientJsonException {
    ArrayNode array = NF.arrayNode();
    if (nextClean() != '[') {
      throw error("A JSONArray text must start with '['");
    }
    char c = nextClean();
    if (c == 0) {
      throw error("JSONArray text must end with ']'");
    } else if (c == ',') {
      throw error("JSONArray text has a trailing ','");
    }
    if (c == ']') {
      return array;
    }
    back();
    for (; ; ) {
      if (nextClean() == ',') {
        back();
        array.add(NF.nullNode());
      } else {
        back();
        array.add(nextValue());
      }
      switch (nextClean()) {
        case ';':
        case ',':
          char nextClean = nextClean();
          if (nextClean == 0) {
            throw error("JSONArray text has a trailing ','");
          } else if (nextClean == ']') {
            return array;
          }
          back();
          break;
        case ']':
          return array;
        default:
          throw error("Expected a ',' or ']'");
      }
    }
  }

  private LenientJsonException error(String message) {
    return new LenientJsonException(message + " at character " + _index + " of " + _source);
  }
}
