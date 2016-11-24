
package io;

import java.io.*;

public class Stream_Input implements Input {

   private BufferedReader p_in;

   public Stream_Input(InputStream is) {
      InputStreamReader isr = new InputStreamReader(is);
      p_in = new BufferedReader(isr);
   }

   public String get_line() {

      String line = null;

      try {
         line = p_in.readLine();
      } catch (IOException e) {
         e.printStackTrace();
      }

      return line;
   }

   public boolean ready() {

      boolean ready = false;

      try {
         ready = p_in.ready();
      } catch (IOException e) {
         System.err.println(e);
      }

      return ready;
   }
}

