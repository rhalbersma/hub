
package hub;

import draughts.*;
import engine.*;
import gui.*;
import util.*;

public class Model_Game implements Engine_Game_User {

   private static final boolean Auto_Save = false;

   private final Engine_Game[] p_engine;
   private String[] p_name;
   private boolean[] p_ready;

   private Game p_game;
   private boolean p_ponder;
   private Timer p_timer;

   private int p_score; // REMOVE ME
   private int p_size; // REMOVE ME

   public Model_Game(Engine_Game engine_1, Engine_Game engine_2) {

      engine_1.set_user(this, Side.White);
      engine_2.set_user(this, Side.Black);

      p_engine = new Engine_Game[] { engine_1, engine_2 };
      p_name = new String[] { "White", "Black" };
      p_ready = new boolean[] { false, false };

      p_game = null;
      p_ponder = Hub.vars.get_bool("game-ponder");
      p_timer = new Timer();

      p_score = 0;
      p_size = 0;

      // start

      Hub.log("start");
      Hub.gui.set_state("Init");

      // the game will actually start when the engines are ready
   }

   private void first_game() {
      new_game();
   }

   private void next_game() {
      new_game(); // HACK: infinite match #
   }

   private void new_game() {
      Hub.log("new-game #");
      set_game(new Game());
   }

   private void set_pos(String s) {

      Hub.log("set-pos " + s + " #");

      try {
         set_game(new Game(FEN.from_fen(s)));
      } catch (Bad_Input e) {
         e.printStackTrace();
         return;
      }
   }

   private synchronized void set_game(Game game) {

      assert !game.is_end();

      Hub.gui.set_state("Play");

      p_game = game;

      int    moves = Hub.vars.get_int("game-moves");
      double time  = Hub.vars.get_real("game-time") * 60.0;
      double inc   = Hub.vars.get_real("game-inc");
      p_game.set_time_control(moves, time, inc);

      // update GUI

      String game_name = p_name[Side.White] + " (white) vs. " + p_name[Side.Black] + " (black)";
      Hub.gui.set_title(game_name);

      Hub.gui.set_info(" "); // to set window size properly

      update_gui();

      // update engines

      p_timer.restart();

      int atk = p_game.turn();
      int def = Side.opp(atk);

      p_engine[def].game_start(p_game, def, p_ponder);
      p_engine[atk].game_start(p_game, atk, p_ponder);
   }

   private synchronized void save_game() {
      String file_name = "game/" + Hub.vars.get("file") + ".pdn";
      PDN_Output.save_game(file_name, p_game);
   }

   private void update_gui() {

      Hub.gui.set_board(p_game.pos(), Move.bit(p_game.last_move()));

      // Hub.gui.set_move(p_game.i() / 2 + 1);
      Hub.gui.set_move(p_score);
      // Hub.gui.set_move(p_size);

      Hub.gui.set_clocks((int) p_game.time(Side.White),
                         (int) p_game.time(Side.Black));
   }

   public synchronized void engine_ready(int sd, String name) {

      if (name != null) p_name[sd] = name;
      p_ready[sd] = true;

      if (p_ready[Side.White] && p_ready[Side.Black]) first_game();
   }

   public void engine_quit(int sd) {
      assert false;
   }

   public void engine_search_info(int sd, String info) {
      if (my_turn(sd)) Hub.gui.set_info(info);
   }

   public synchronized void engine_move(int sd, long mv) {

      assert my_turn(sd);
      if (!my_turn(sd)) return;

      double time = p_timer.elapsed();

      Hub.log(String.format("move side=%d move=%s time=%.1f", sd, Move.to_string(mv, p_game.pos()), time));

      p_game.add_move(mv, time);
      p_timer.restart();
      if (Auto_Save) save_game();

      update_gui();

      // update engines

      p_timer.restart();

      int atk = p_game.turn();
      int def = Side.opp(atk);

      if (p_game.is_end()) { // game end

         Hub.log("game-end #");
         Hub.gui.set_state("End");

         p_engine[atk].game_end();
         p_engine[def].game_end();

         if (p_game.pos().is_end()) { // loss
            p_score += (p_game.turn() == Side.White) ? -1 : +1;
         }

         p_size++;

         next_game();

      } else { // game move

         p_engine[atk].game_move(mv, time);
         p_engine[def].game_move(mv, time);
      }
   }

   private boolean my_turn(int sd) {
      return !p_game.is_end() && p_game.turn() == sd;
   }
}

