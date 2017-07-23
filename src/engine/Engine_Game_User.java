
package engine;

public interface Engine_Game_User {

   void engine_ready(int sd, String name);
   void engine_quit(int sd);

   void engine_search_info(int sd, String info);
   void engine_move(int sd, long mv);
}

