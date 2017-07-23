
package engine;

import draughts.Game;

public interface Engine_Game {

   void set_user(Engine_Game_User user, int sd);

   void game_start(Game game, int sd, boolean ponder);
   void game_move(long mv, double time);
   void game_end();
}

