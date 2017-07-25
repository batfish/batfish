package org.batfish.client;

import static jline.internal.Preconditions.checkNotNull;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import jline.console.completer.Completer;

public class CommandCompleter implements Completer {

  private final SortedSet<String> _commandStrs;

  public CommandCompleter() {
    _commandStrs = new TreeSet<>(Command.getNameMap().keySet());
  }

  @Override
  public int complete(String buffer, int cursor, List<CharSequence> candidates) {
    checkNotNull(candidates);

    if (buffer == null) {
      candidates.addAll(_commandStrs);
    } else {
      String trimmedBuffer = buffer.trim();

      for (String match : _commandStrs.tailSet(buffer)) {
        if (!match.startsWith(trimmedBuffer)) {
          break;
        }

        candidates.add(match);
      }
    }

    // if the match was unique and the complete command was specified, print
    // the command usage
    if (candidates.size() == 1 && candidates.get(0).equals(buffer)) {
      candidates.clear();
      candidates.add(
          buffer + " " + Command.getUsageMap().get(Command.getNameMap().get(buffer)).getFirst());
    }

    return candidates.isEmpty() ? -1 : 0;
  }
}
