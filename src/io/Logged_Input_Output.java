
package io;

public class Logged_Input_Output implements Input_Output {

   private Input p_in;
   private Output p_out;

   private Output p_log;
   private String p_name;

   public Logged_Input_Output(Input in, Output out, Output log, String name) {

      p_in = in;
      p_out = out;

      p_log = log;
      p_name = name;
   }

   public String get_line() {

      String line = p_in.get_line();

      if (p_log != null) {
         if (line != null) {
            p_log.put_line(p_name + " < " + line);
         } else {
            p_log.put_line(p_name + " < EOF");
         }
      }

      return line;
   }

   public void put_line(String string) {
      if (p_log != null) p_log.put_line(p_name + " > " + string);
      p_out.put_line(string);
   }

   public boolean ready() {
      return p_in.ready();
   }

   public void close() {
      if (p_log != null) p_log.put_line(p_name + " > EOF");
      p_out.close();
   }
}

