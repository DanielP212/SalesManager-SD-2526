package comms.common;

public enum PacketType {
    LOGIN,
    REGISTER,
    QUERY_QTD,
    QUERY_TOTAL,
    QUERY_MEDIAN,
    QUERY_MAX;

    public static PacketType fromByte(byte b){
        return switch (b){
            case 0x00 -> LOGIN;
            case 0x01 -> REGISTER;
            case 0x02 -> QUERY_QTD;
            case 0x03 -> QUERY_TOTAL;
            case 0x04 -> QUERY_MEDIAN;
            case 0x05 -> QUERY_MAX;
            default -> null;
        };
    }

    public byte toByte(){
        return switch (this){
            case LOGIN -> 0x00;
            case REGISTER -> 0x01;
            case QUERY_QTD -> 0x02;
            case QUERY_TOTAL -> 0x03;
            case QUERY_MEDIAN -> 0x04;
            case QUERY_MAX -> 0x05;
        };
    }
}
