package back;

public interface PacketListener {
	/**
     * 패킷이 캡처될 때 호출되는 메서드.
     *
     * @param protocol       캡처된 패킷의 프로토콜 
     * @param sourceIP       출발지 IP 주소
     * @param destinationIP  목적지 IP 주소
     * @param length         패킷의 길이
     * @param timeStamp      패킷이 캡처된 시간 
     * @param rawData        패킷의 원시 데이터 
     */
    void onPacketCaptured(String protocol, String sourceIP, String destinationIP, int length, String timeStamp, String rawData);
}
