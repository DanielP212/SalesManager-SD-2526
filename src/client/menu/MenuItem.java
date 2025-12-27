package client.menu;

import comms.Packet;

public interface MenuItem {
    String getTitle();
    Packet execute();
}
