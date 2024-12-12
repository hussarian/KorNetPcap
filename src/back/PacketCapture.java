package back;

import org.jnetpcap.Pcap;
import org.jnetpcap.PcapIf;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.packet.PcapPacketHandler;
import org.jnetpcap.protocol.network.Ip4;
import org.jnetpcap.protocol.tcpip.Tcp;
import org.jnetpcap.protocol.tcpip.Udp;
import java.util.ArrayList;
import java.util.List;
import utils.TimeStampConverter;
	

public class PacketCapture {
    private Pcap pcap; // Pcap 인스턴스 관리
    private boolean capturing = false; // 캡처 상태 플래그
    private PacketListener listener; // 패킷 이벤트 리스너
    
   
    // 네트워크 디바이스 목록 가져오기
    public List<PcapIf> getNetworkDevices() {
        List<PcapIf> alldevs = new ArrayList<>();
        StringBuilder errBuf = new StringBuilder();

        int result = Pcap.findAllDevs(alldevs, errBuf);
        if (result != Pcap.OK || alldevs.isEmpty()) {
            System.err.printf("네트워크 디바이스를 찾을 수 없습니다. 오류: %s\n", errBuf.toString());
            return null;
        }
        return alldevs;
    }

    // 패킷 캡처 시작
    public void startCapture(PcapIf selectedDevice) {
        if (capturing) {
            System.out.println("이미 패킷 캡처가 실행 중입니다.");
            return;
        }

        capturing = true;
        StringBuilder errBuf = new StringBuilder();
        pcap = Pcap.openLive(selectedDevice.getName(), 128 * 1024, Pcap.MODE_PROMISCUOUS, 10 * 1000, errBuf);
        if (pcap == null) {
            System.err.printf("Pcap을 열 수 없습니다. 오류: %s\n", errBuf.toString());
            capturing = false;
            return;
        }

        System.out.println("패킷 캡처를 시작합니다...");

        // 패킷 핸들러 정의
        PcapPacketHandler<String> packetHandler = new PcapPacketHandler<>() {
            @Override
            public void nextPacket(PcapPacket packet, String user) {
            	
                if (!capturing) {
                    pcap.breakloop();
                    return;
                }
                System.out.println("패킷 수신!");

                // 패킷 데이터 추출
                Ip4 ip = new Ip4();
                Tcp tcp = new Tcp();
                Udp udp = new Udp();
                
                String sourceIp = "알 수 없음";
                String destinationIp = "알 수 없음";
                String protocol = "알 수 없음";
                // 원시 데이터 추출
                String rawData = packet.toHexdump(); // 16진수 데이터
                String unixTime = String.valueOf(System.currentTimeMillis()); // unix time
                
                try {
                    // String -> long 변환
                    long timeStampAsLong = Long.parseLong(unixTime);

                    // 사람이 읽을 수 있는 형식으로 변환
                    String timeStamp = TimeStampConverter.convertTimeStampToReadableFormat(timeStampAsLong);

                    if (packet.hasHeader(ip)) {
                        sourceIp = org.jnetpcap.packet.format.FormatUtils.ip(ip.source());
                        destinationIp = org.jnetpcap.packet.format.FormatUtils.ip(ip.destination());
                        System.out.printf("패킷 수신 - 출발지: %s, 도착지: %s\n", sourceIp, destinationIp);
                    }
                    if (packet.hasHeader(tcp)) {
                        protocol = "TCP";
                    } else if (packet.hasHeader(udp)) {
                        protocol = "UDP";
                    }

                    // 리스너 호출
                    if (listener != null) {
                        listener.onPacketCaptured(protocol, sourceIp, destinationIp, packet.size(), timeStamp, rawData);
                    }

                    // 콘솔 출력
                    System.out.println("========================================");
                    System.out.printf("시간: %s\n출발지 IP: %s\n도착지 IP: %s\n프로토콜: %s\n",
                            timeStamp, sourceIp, destinationIp, protocol);
                    System.out.println("원시데이터:");
                    System.out.println(rawData);
                    System.out.println("========================================");

                } catch (NumberFormatException e) {
                    System.err.println("타임스탬프 변환 실패: " + unixTime);
                }
            }
        };

        // 패킷 캡처 루프 시작
        new Thread(() -> {
            pcap.loop(Pcap.LOOP_INFINITE, packetHandler, "캡처 중...");
            stopCapture(); // 캡처 종료 시 cleanup
        }).start();
    }

    // 패킷 캡처 중지
    public void stopCapture() {
        if (!capturing) {
            System.out.println("패킷 캡처가 실행 중이 아닙니다.");
            return;
        }
        capturing = false;
        if (pcap != null) {
            pcap.breakloop();
            pcap.close();
            pcap = null;
        }
        System.out.println("패킷 캡처가 중지되었습니다.");
    }

    // 리스너 설정
    public void setPacketListener(PacketListener listener) {
        this.listener = listener;
    }
}