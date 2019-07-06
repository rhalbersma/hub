
package hub;

import java.io.*;

import draughts.*;
import engine.*;
import gui.*;
import util.*;

public class Hub {

   private static final boolean Disp_Log = true;

   public static Var_List vars;

   public static Model_User model_user; // HACK for GUI
   public static GUI gui;

   private static PrintWriter p_log;

   public static void main(String args[]) {

      vars = new Var_List();
      vars.set("game-variant", "normal");
      vars.set("gui-sound", "");
      vars.set("load-clock", "2");
      vars.load("hub.ini");

      if (vars.get_bool("log")) {
         try {
            OutputStream os = new FileOutputStream("log.txt", true);
            p_log = new PrintWriter(os, true);
         } catch (Exception e) {
            e.printStackTrace();
         }
      }

      log("---");
      log("init ###");

      Draughts.init();

      Var_List params_engine = new Var_List("engine.ini");

      Engine_Hub engine = new Engine_Hub(1, vars.get("engine-dir"), vars.get("engine-command"), params_engine);
      gui = new GUI();
      model_user = new Model_User(engine);

      log("init done");

      util.Thread.launch(engine);
   }

   public static void log(String s) {
      if (Disp_Log) System.out.println(s);
      if (p_log != null) p_log.println(s);
   }
}

