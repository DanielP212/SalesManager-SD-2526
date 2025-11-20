package comms.common;

public enum PacketType {
    LOGIN,
    REGISTER;

    public static PacketType fromByte(byte b){
        return switch (b){
            case 0x00 -> LOGIN;
            case 0x01 -> REGISTER;
            default -> null;
        };
    }

    public byte toByte(){
        return switch (this){
            case LOGIN -> 0x00;
            case REGISTER -> 0x01;
        };
    }
}
