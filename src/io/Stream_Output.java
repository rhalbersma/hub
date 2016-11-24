
package io;

import java.io.*;

public class Stream_Output implements Output {

   private PrintWriter p_out;

   public Stream_Output(OutputStream os) {
      p_out = new PrintWriter(os, true);
   }

   public void put_line(String line) {
      p_out.println(line);
   }

   public void close() {
      p_out.close();
   }
}

