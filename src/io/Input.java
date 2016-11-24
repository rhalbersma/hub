
package io;

public interface Input {
   String get_line(); // null for EOF
   boolean ready(); // full line available?
}

