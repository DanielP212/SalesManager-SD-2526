package client;

import comms.Packet;
import comms.common.Encodable;
import comms.common.RequestType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InputHandler {

    // TODO Adicionar mais coisas
    private static RequestType findType(String msg){
        msg = msg.toUpperCase().trim();
        System.out.println(msg);
        return switch (msg){
            case "AUTH" -> RequestType.AUTH;
            case "ADD" -> RequestType.ADD;
            case null, default -> null;
        };
    }



    // ISTO DEVE SER PARA ELIMINAR
    // Talvez funcione, mas provavelmente não
    private static byte[] inputToBytes(String[] inputs){
        int totalInputsSize = 0;
        for(int i = 1; i < inputs.length; i++) totalInputsSize += inputs[i].trim().length();

        byte[] bytes = new byte[((inputs.length - 1) * 4) + totalInputsSize]; // -1 porque o tipo nao conta

        int counter = 1;
        for(int i = 0; i < bytes.length;){
            String currInput = inputs[counter++];
            Encodable.writeIntBytes(bytes, i, currInput.length());
            i+=4;
            for (int c = 0; c < currInput.length(); c++)
                bytes[i] = (byte)currInput.charAt(c);
            i+=currInput.length();
        }
        System.out.println(Arrays.toString(bytes));
        return bytes;
    }

    public static Packet handle(int clientID, String input){
        String[] split = input.split(" ");

        RequestType type = findType(split[0]);
        if (type == null){
            System.out.println("Invalid operation type!");
            return null;
        }
        // TODO passar as inputs para o servidor
        // Ou seja, passar as inputs para array de bytes e mandar pelo packet
        return new Packet(clientID, type, new byte[2]);
    }
}
