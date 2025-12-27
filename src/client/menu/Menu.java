package client.menu;

import client.Client;
import client.InputHandler;
import comms.Packet;
import comms.common.PacketType;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Menu implements MenuItem{
    private static Menu instance = null;

    private String title;
    private List<MenuItem> items = new ArrayList<>();
    private boolean exit = false;
    private boolean isLogged = false;
    public DataInputStream clientIn;
    private Client client;

    public Menu(String title, DataInputStream clientIn, Client client){
        if (instance != null) return;
        this.title = title;
        this.clientIn = clientIn;
        this.client = client;
        instance = this;

        items.add(new MenuAction("Quantidade Vendida", PacketType.QUERY_QTD));
        items.add(new MenuAction("Receita Total", PacketType.QUERY_TOTAL));
        items.add(new MenuAction("Preço Máximo", PacketType.QUERY_MAX));
        items.add(new MenuAction("Preço Médio", PacketType.QUERY_AVG));
        items.add(new MenuAction("Criar Produto", PacketType.CREATE_PRODUCT));
        items.add(new MenuAction("Adicionar Venda", PacketType.ADD_SALE));
        items.add(new MenuAction("Adicionar Notificação Seq", PacketType.NOTIFY_SEQ));
        items.add(new MenuAction("Adicionar Notificação Conq", PacketType.NOTIFY_CONC));
    }

    public String getTitle(){ return title; }
    public static Menu getInstance(){ return instance; }
    public int getClientId(){ return client.getID(); }
    public void setLogged(){ isLogged = true; }

    public Packet execute(){
        while (!isLogged){
            Packet p = doLogin();
            if (p == null) continue;
            return p;
        }

        while(!exit){
            printMenu();
            int option = readInt();

            System.out.println("Option: " + items.get(option).getTitle());
            if (option >= 0 && option < items.size()){
                return items.get(option).execute();
            }
        }
        return null;
    }


    private void printMenu(){
        System.out.println("------- " + title + "---------");
        for (int i = 0; i < items.size(); i++){
            System.out.println(i + ": " + items.get(i).getTitle());
        }
    }

    private Packet doLogin(){
        if (isLogged) return null;
        System.out.print("Username: ");
        String username = readInput(255);
        if (username == null) return null;
        System.out.println(username);

        System.out.print("Password: ");
        String password = readInput(255);
        if (password == null) return null;
        System.out.println(password);

        byte[] data = InputHandler.inputToBytes(new String[]{"LOGIN",username, password});
        return InputHandler.handle(client.getID(), PacketType.LOGIN, data);
    }

    private String readInput(int maxSize){
        byte[] buf = new byte[maxSize];
        try {
            int bytesRead = clientIn.read(buf);
            if (bytesRead < 0) return null;
            return new String(buf).trim();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private int readInt(){
        String input = null;
        while(input == null) input = readInput(4);
        try{
            return Integer.parseInt(input);
        } catch (NumberFormatException e){
            System.out.println("Is that an Integer?");
            return readInt();
        }
    }

}
