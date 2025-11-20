package comms.common;

public enum RequestType {
    AUTH,
    ADD;

    public static RequestType fromByte(byte b){
        return switch (b){
            case 0x00 -> AUTH;
            case 0x01 -> ADD;
            default -> null;
        };
    }

    public byte toByte(){
        return switch (this){
            case AUTH -> 0x00;
            case ADD -> 0x01;
        };
    }
}
