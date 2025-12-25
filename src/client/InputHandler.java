package client;

import comms.Packet;
import comms.common.PacketType;

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
            case null, default -> null;
        };
    }

    private static byte[] inputToBytes(String[] inputs){
        int totalInputsSize = 0;
        for(int i = 1; i < inputs.length; i++) totalInputsSize += inputs[i].trim().length();

        byte[] bytes = new byte[(inputs.length - 1) + totalInputsSize]; // -1 porque o tipo não conta

        int counter = 1; // 1 porque o 0 e o tipo
        for(int i = 0; i < bytes.length;){
            String currInput = inputs[counter++];
            int inputSize = currInput.length();
            if(counter == inputs.length) inputSize--; // Tirar o \n
            //System.out.println(inputSize);

            bytes[i++] = (byte)inputSize; // 1 byte para o tamanho da string

            for (int c = 0; c < inputSize; c++)
                bytes[i++] = (byte)currInput.charAt(c);
        }
        //System.out.println(Arrays.toString(bytes));
        return bytes;
    }

    public static Packet handle(int clientID, String input){
        String[] split = input.split(" ");
        PacketType type = findType(split[0]);
        if (type == null){
            System.out.println("Invalid operation type!");
            return null;
        }
        return new Packet(clientID, type, inputToBytes(split));
    }
}
