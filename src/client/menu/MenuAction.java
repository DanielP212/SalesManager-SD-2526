package client.menu;

import client.InputHandler;
import comms.Packet;
import comms.common.Encodable;
import comms.common.PacketType;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class MenuAction implements MenuItem{
    private final String title;
    private final PacketType type;
    private static final Menu mainMenu = Menu.getInstance();

    public MenuAction(String title, PacketType type){
        this.title = title;
        this.type = type;
    }


    @Override
    public String getTitle() {
        return title;
    }

    public PacketType getType(){
        return type;
    }

    @Override
    public Packet execute() {
        return switch (type){
            case QUERY_QTD, QUERY_AVG, QUERY_TOTAL, QUERY_MAX -> QUERY();
            case ADD_SALE -> ADD_SALE();
            case CREATE_PRODUCT -> CREATE();
            case NOTIFY_SEQ -> NOTIFY_SEQ();
            case NOTIFY_CONC -> NOTIFY_CONQ();
            case FILTER -> FILTER();
            case ADVANCE_DAY -> ADVANCE_DAY();
            case null, default -> null;
        };
    }

    private String readInput(int maxSize){
        byte[] buf = new byte[maxSize];
        try {
            int bytesRead = mainMenu.clientIn.read(buf);
            if (bytesRead < 0) return null;
            return new String(buf, 0 ,bytesRead).trim();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private int readInt(){
        String input = null;
        while(input == null) input = readInput(8);
        try{
            return Integer.parseInt(input);
        } catch (NumberFormatException e){
            System.out.println("Is that an Integer?");
            return readInt();
        }
    }

    private float readFloat(){
        String input = null;
        while(input == null) input = readInput(8);
        try{
            return Float.parseFloat(input);
        } catch (NumberFormatException e){
            System.out.println("Is that a Float?");
            return readFloat();
        }
    }


    private Packet QUERY(){
        System.out.print("Nome do Produto: ");
        String nome = readInput(255);
        if (nome == null) return null;

        System.out.print("Número de dias: ");
        int numDias = readInt();

        byte[] array = new byte[1 + nome.trim().length() + 4];
        int newOffset = Encodable.writeString(array, 0, nome);
        Encodable.writeIntBytes(array, newOffset, numDias);


        return InputHandler.handle(mainMenu.getClientId(), type, array);
    }

    private Packet CREATE(){
        System.out.print("Nome do Produto: ");
        String nome = readInput(255);
        if (nome == null) return null;

        System.out.print("Preço base: ");
        float preco = readFloat();

        byte[] array = new byte[1 + nome.trim().length() + 4];
        int newOffset = Encodable.writeString(array, 0, nome);
        Encodable.writeIntBytes(array, newOffset, Float.floatToIntBits(preco));

        return InputHandler.handle(mainMenu.getClientId(), type, array);
    }

    private Packet ADD_SALE(){
        System.out.print("Nome do Produto: ");
        String nome = readInput(255);
        if (nome == null) return null;

        System.out.print("Quantidade vendida: ");
        int qtd = readInt();

        System.out.print("Preço: ");
        float preco = readFloat();

        byte[] array = new byte[1 + nome.trim().length() + 4 + 4];
        int offset = 0;
        offset = Encodable.writeString(array, offset, nome);
        Encodable.writeIntBytes(array, offset, qtd);
        offset += 4;
        Encodable.writeIntBytes(array, offset, Float.floatToIntBits(preco));

        return InputHandler.handle(mainMenu.getClientId(), type, array);

    }

    private Packet NOTIFY_SEQ(){
        System.out.print("Nome do Produto 1: ");
        String nome = readInput(255);
        if (nome == null) return null;

        System.out.print("Nome do Produto 2: ");
        String nome2 = readInput(255);
        if (nome2 == null) return null;

        byte[] array = new byte[1 + nome.trim().length() + 1 + nome.trim().length()];
        int offset = 0;
        offset = Encodable.writeString(array, offset, nome);
        Encodable.writeString(array, offset, nome2);

        return InputHandler.handle(mainMenu.getClientId(), type, array);
    }

    private Packet NOTIFY_CONQ(){
        System.out.print("Numero de Vendas: ");
        int num = readInt();


        byte[] array = new byte[4];
        Encodable.writeIntBytes(array, 0 , num);

        return InputHandler.handle(mainMenu.getClientId(), type, array);
    }


    private Packet FILTER(){
        System.out.println("Numero do dia: ");
        int day = readInt();

        List<String> produtos = new ArrayList<>();
        boolean stop = false;
        System.out.println("Produtos: ");
        do {
            String input = readInput(255);
            if (input.isEmpty()){
                stop = true;
                continue;
            }
            produtos.add(input);
        } while (!stop);

        String[] strings = new String[produtos.size()];
        int totalBytes = 0;
        totalBytes += 4; // int do dia
        totalBytes += 4; // tamanho do array
        for (int i = 0; i < produtos.size(); i++){
            totalBytes++; // tamanho da string
            totalBytes += produtos.get(i).length();
            strings[i] = produtos.get(i);
        }
        byte[] array = new byte[totalBytes];
        Encodable.writeIntBytes(array, 0, day);
        Encodable.writeStringArray(array, 4, strings);

        return InputHandler.handle(mainMenu.getClientId(), type, array);

    }

    private  Packet ADVANCE_DAY(){
        byte[] array = new byte[0];
        return InputHandler.handle(mainMenu.getClientId(), type, array);
    }
}
