
package hub;

import java.io.*;

import draughts.*;
import engine.*;
import gui.*;
import util.*;

public class Hub {

   private static final boolean Disp_Log = true;

   private static final Mode mode = Mode.User;

   public static Var_List vars;

   public static Model_User model_user; // HACK for GUI
   public static GUI gui;

   private static PrintWriter p_log;

   public static void main(String args[]) {

      vars = new Var_List();
      vars.set("pdn-trace", "false");
      vars.set("gui-sound", "");
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

      if (mode == Mode.User) {

         Var_List params_engine = new Var_List("engine.ini");

         Engine_Hub engine = new Engine_Hub(1, vars.get("engine-dir"), vars.get("engine-command"), params_engine);
         gui = new GUI();
         model_user = new Model_User(engine);

         log("init done");

         util.Thread.launch(engine);

      } else if (mode == Mode.Game) {

         Var_List params_engine_1 = new Var_List("engine_1.ini");
         Var_List params_engine_2 = new Var_List("engine_2.ini");

         Engine_Hub engine_hub_1 = new Engine_Hub(1, vars.get("engine-1-dir"), vars.get("engine-1-command"), params_engine_1);
         Engine_Hub engine_hub_2 = new Engine_Hub(2, vars.get("engine-2-dir"), vars.get("engine-2-command"), params_engine_2);

         Engine_Game_Hub engine_game_1 = new Engine_Game_Hub(engine_hub_1);
         Engine_Game_Hub engine_game_2 = new Engine_Game_Hub(engine_hub_2);

         gui = new GUI();
         Model_Game model_game = new Model_Game(engine_game_1, engine_game_2);

         log("init done");

         util.Thread.launch(engine_hub_1);
         util.Thread.launch(engine_hub_2);
      }
   }

   public static void log(String s) {
      if (Disp_Log) System.out.println(s);
      if (p_log != null) p_log.println(s);
   }
}

enum Mode { User, Game };

