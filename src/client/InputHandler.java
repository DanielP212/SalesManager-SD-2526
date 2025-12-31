package client;

import comms.Packet;
import comms.common.Encodable;
import comms.common.PacketType;

import java.util.Arrays;

public class InputHandler {

    // TODO Adicionar o Filter request e testar
    // TODO interface para o cliente
    private static PacketType findType(String msg){
        msg = msg.toUpperCase().trim();
        //System.out.println(msg);
        return switch (msg){
            case "LOGIN" -> PacketType.LOGIN;
            case "REGISTER" -> PacketType.REGISTER;
            case "QUERY_QTD" -> PacketType.QUERY_QTD;
            case "ADD_SALE" -> PacketType.ADD_SALE;
            case "QUERY_TOTAL" -> PacketType.QUERY_TOTAL;
            case "QUERY_MAX" -> PacketType.QUERY_MAX;
            case "QUERY_AVG" -> PacketType.QUERY_AVG;
            case "CREATE" -> PacketType.CREATE_PRODUCT;
            case "NOTIFY_SEQUENTIAL" -> PacketType.NOTIFY_SEQ;
            case "NOTIFY_CONCURRENT" -> PacketType.NOTIFY_CONC;
            case null, default -> null;
        };
    }

    public static byte[] inputToBytes(String[] inputs){
        int totalInputsSize = 0;
        for(int i = 1; i < inputs.length; i++) totalInputsSize += inputs[i].length();

        byte[] bytes = new byte[(inputs.length - 1) + totalInputsSize]; // -1 porque o tipo não conta

        int counter = 1; // 1 porque o 0 e o tipo
        for(int i = 0; i < bytes.length;){
            String currInput = inputs[counter++];
            int inputSize = currInput.length();
            //if(counter == inputs.length) inputSize--; // Tirar o \n
            //System.out.println(inputSize);

            bytes[i++] = (byte)inputSize; // 1 byte para o tamanho da string

            for (int c = 0; c < inputSize; c++)
                bytes[i++] = (byte)currInput.charAt(c);
        }
        //System.out.println(Arrays.toString(bytes));
        return bytes;
    }

    // so para testes
    public static Packet handle(int clientID, String input){
        String[] split = input.split(" ");
        PacketType type = findType(split[0]);
        if (type == null){
            System.out.println("Invalid operation type!");
            return null;
        }
        return new Packet(clientID, type, serializeByType(type, split));
        //return new Packet(clientID, type, inputToBytes(split));
    }

    public static Packet handle(int clientID, PacketType type, byte[] data){
        return new Packet(clientID, type, data);
    }


    private static byte[] serializeByType(PacketType type, String[] inputs){
        if (type == PacketType.ADD_SALE){
            String nome = inputs[1];
            byte[] array = new byte[1 + nome.trim().length() + 4 + 4];
            int offset = 0;
            offset = Encodable.writeString(array, offset, nome);
            Encodable.writeIntBytes(array, offset, Integer.parseInt(inputs[2].trim()));
            Encodable.writeIntBytes(array, offset + 4, Integer.parseInt(inputs[3].trim()));
            return array;
        } else {
            String nome = inputs[1];
            byte[] array = new byte[1 + nome.trim().length() + 4];
            int newOffset = Encodable.writeString(array, 0, nome);
            Encodable.writeIntBytes(array, newOffset, Integer.parseInt(inputs[2].trim()));

            return array;
        }
    }
}
