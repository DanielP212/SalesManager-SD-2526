package client;

import comms.Packet;
import comms.common.PacketType;

import java.util.Arrays;

public class InputHandler {

    // TODO Adicionar mais coisas
    // TODO interface para o cliente
    private static PacketType findType(String msg){
        msg = msg.toUpperCase().trim();
        //System.out.println(msg);
        return switch (msg){
            case "LOGIN" -> PacketType.LOGIN;
            case "REGISTER" -> PacketType.REGISTER;
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
