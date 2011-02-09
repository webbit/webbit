package chatroom2;

import webbit.magic.Client;

interface ChatClient extends Client {

    void say(String username, String message);

    void leave(String username);

    void join(String username);

}
