package back;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PacketRepository {
	/**
     * 패킷 데이터를 데이터베이스에 저장하는 메서드.
     *
     * @param protocol      패킷의 프로토콜 (예: TCP, UDP)
     * @param sourceIP      출발지 IP 주소
     * @param destinationIP 목적지 IP 주소
     * @param length        패킷의 길이
     * @param timeStamp     패킷이 캡처된 시간
     * @param rawData       패킷의 원시 데이터 (16진수 형식)
     */
    public void savePacket(String protocol, String sourceIP, String destinationIP, int length, String timeStamp, String rawData) {
        String query = "INSERT INTO packets (protocol, sourceIP, destinationIP, length, timeStamp, rawData) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = ConnectionDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, protocol);
            statement.setString(2, sourceIP);
            statement.setString(3, destinationIP);
            statement.setInt(4, length);
            statement.setString(5, timeStamp);
            statement.setString(6, rawData);

            statement.executeUpdate();
            System.out.println("패킷 데이터가 성공적으로 저장되었습니다.");

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("패킷 데이터를 저장하는 중 오류 발생: " + e.getMessage());
        }
    }
    
    public List<String[]> getAllPackets() {
        List<String[]> packets = new ArrayList<>();
        String sql = "SELECT protocol, sourceIP, destinationIP, length, timeStamp, rawData FROM packets";

        try (Connection connection = ConnectionDB.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                String protocol = resultSet.getString("protocol");
                String sourceIP = resultSet.getString("sourceIP");
                String destinationIP = resultSet.getString("destinationIP");
                String length = String.valueOf(resultSet.getInt("length"));
                String timeStamp = resultSet.getString("timeStamp");
                String rawData = resultSet.getString("rawData");

                packets.add(new String[]{protocol, sourceIP, destinationIP, length, timeStamp, rawData});
            }

        } catch (SQLException e) {
            System.err.println("데이터베이스에서 패킷을 가져오는 중 오류 발생: " + e.getMessage());
        }

        return packets;
    }
}
