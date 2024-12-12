package BACK;
/*
import java.util.ArrayList;
import java.util.Date;

import org.jnetpcap.Pcap;
import org.jnetpcap.PcapIf;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.packet.PcapPacketHandler;

public class JnetPCap_Launcher {

	public static void main(String[] args) {
		
		// 네트워크 장비를 저장하는 공간 할당
		ArrayList<PcapIf> allDevs = new ArrayList<PcapIf>();
		
		// 오류 메세지를 담는 버퍼
		StringBuilder errbuf = new StringBuilder();
		
		// 네트워크 장비를 찾아서 저장, 결과는 r에 담김
		int r = Pcap.findAllDevs(allDevs, errbuf);
		if( r == Pcap.NOT_OK || allDevs.isEmpty()) {
			System.out.println("네트워크 장치를 찾을 수 없습니다. 오류: " + errbuf.toString());
			return;
		}
		
		System.out.println(" [네트워크 장비 탐색 성공] ");
		
		// 찾은 장비를 하나씩 돌면서 정보 출력
		int i = 0;
		for(PcapIf device : allDevs) {
			String description = (device.getDescription() != null) ?
						device.getDescription() : "장비에 대한 설명이 없습니다.";
			System.out.printf("[%d번]: %s [%s]\n", i++, device.getName(), description);
		}
		
		// 네트워크 장치 중에서 하나를 선택
		PcapIf device = allDevs.get(1);
		System.out.printf("선택된 장치: %s\n", (device.getDescription() != null) ?
					device.getDescription() : device.getName());
		
		// 65536바이트 만큼 패킷을 캡쳐
		int snaplen = 64 * 1024;
		
		// 프라미스큐어스(promiscuous)모드 설정
		int flags = Pcap.MODE_NON_PROMISCUOUS;
		
		// 타임 아웃(Timeout)을 10초로 설정
		int timeout = 10 * 1000;
		
		// 장치의 패킷 캡쳐 활성화
		Pcap pcap = Pcap.openLive(device.getName(), snaplen, flags, timeout, errbuf);
		
		// Pcap 객체가 생성되지 않으면 오류 메세지를 발생
		if(pcap == null) {
			System.out.printf("패킷 캡쳐를 위해 네트워크 장치를 여는 데에 실패했습니다. 오류: " + errbuf.toString());
			return;
		}
		
		// 패킷을 처리하는 패킷 핸들러 정의
		PcapPacketHandler<String> jPacketHandler = new PcapPacketHandler<String>() {
			@Override
			public void nextPacket(PcapPacket packet, String user) {
				System.out.printf("캡쳐 시각: %s\n패킷의 길이: %-4d\n", new Date(packet.getCaptureHeader().timestampInMillis()),packet.getCaptureHeader().caplen());
			}
		};
		
		// 반복적으로 10번 패킷 캡쳐
		pcap.loop(10, jPacketHandler, "jNetPcap");
		pcap.close();
	}

}
*/
