
package engine;

import draughts.*;
import hub.*;
import io.*;
import util.*;

public class Engine_Game_Hub implements Engine_Game, Engine_User {

   private static final boolean Copy = true;

   private final Engine_Hub p_engine;
   private Engine_Game_User p_user;

   private Game p_game;
   private int p_side;
   private boolean p_ponder;

   private boolean p_playing;
   private long p_expecting; // before pondering
   private long p_expected;  // during pondering

   public Engine_Game_Hub(Engine_Hub engine) {
      engine.set_user(this);
      p_engine = engine;
   }

   public void set_user(Engine_Game_User user, int sd) {
      p_user = user;
      p_side = sd;
   }

   public synchronized void game_start(Game game, int sd, boolean ponder) {

      assert p_user != null;
      assert p_engine.is_ready();

      p_engine.cancel();
      p_engine.new_game();

      p_game = Copy ? game.clone() : game;
      p_side = sd;
      p_ponder = ponder;

      p_playing = true;
      p_expecting = Move.None;
      p_expected = Move.None;

      update_engine(Move.None);
   }

   public synchronized void game_move(long mv, double time) {

      if (Copy) p_game.add_move(mv, time);

      if (my_turn() && p_ponder && p_expected != Move.None) {

         if (mv == p_expected) {
            Hub.log("ponder-hit");
            p_expected = Move.None;
            p_engine.ponder_hit(mv);
         } else {
            Hub.log("ponder-miss");
            update_engine(p_expecting);
         }

      } else {

         update_engine(p_expecting);
      }
   }

   public synchronized void game_end() {

      p_engine.cancel();

      p_game = null; // REMOVE ME?
      p_ponder = false;

      p_playing = false;
      p_expecting = Move.None;
      p_expected = Move.None;
   }

   private void update_engine(long expecting) {

      p_engine.cancel();

      p_expecting = Move.None;
      p_expected = Move.None;

      if (p_game.is_end()) {

         Hub.log("game-end #");

      } else if (my_turn()) {

         Hub.log(String.format("think time=%.1f", p_game.time(p_game.turn())));

         p_engine.think(p_game);

      } else if (opp_turn() && p_ponder && expecting != Move.None && p_engine.can_ponder(p_game, expecting)) {

         Hub.log(String.format("ponder move=%s time=%.1f", Move.to_string(expecting, p_game.pos()), p_game.time(Side.opp(p_game.turn()))));

         p_expected = expecting;
         p_engine.ponder(p_game, expecting);

      } else {

         Hub.log("wait");
      }
   }

   public void engine_ready(String name) {
      p_user.engine_ready(p_side, name);
   }

   public void engine_quit() {
      p_user.engine_quit(p_side);
   }

   public void engine_search_info(String info) {
      p_user.engine_search_info(p_side, info);
   }

   public synchronized void engine_move(long move, long answer) {

      assert my_turn();

      p_expecting = answer;
      p_user.engine_move(p_side, move);
   }

   public void engine_pong() {
      // ignore
   }

   private boolean my_turn() {
      assert !p_game.is_end();
      return p_playing && p_game.turn() == p_side;
   }

   private boolean opp_turn() {
      assert !p_game.is_end();
      return p_playing && p_game.turn() != p_side;
   }
}

