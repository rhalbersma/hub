
package io;

public interface Output {
   void put_line(String line); // line must not be null (but can be empty)
   void close(); // puts EOF
}

