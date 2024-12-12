package utils;

import java.util.ArrayList;
import java.util.List;

public class RawDataParser {
	// 각 데이터를 저장
    public static class ParsedLine {
        public String number;   // 번호
        public String hexData;  // 16진수 데이터
        public String asciiData; // ASCII 데이터

        public ParsedLine(String number, String hexData, String asciiData) {
            this.number = number;
            this.hexData = hexData;
            this.asciiData = asciiData;
        }
    }
    
    // Raw Data를 파싱하는 메서드
    public static List<ParsedLine> parseRawData(String rawData) {
        List<ParsedLine> parsedLines = new ArrayList<>();
        System.out.println(rawData);
        // 각 라인을 처리
        String[] lines = rawData.split("\n");
        for (String line : lines) {
            if (line.trim().isEmpty()) continue; // 빈 줄 무시
            // '*' 제거
            line = line.replace("*", " ");
            // ':'를 기준으로 번호와 나머지 데이터 분리
            String[] parts = line.split(":", 2);
            if (parts.length < 2) continue; // ':'가 없으면 무시

            String number = parts[0].trim(); // 번호 추출
            String remainingData = parts[1].trim(); // 나머지 데이터

            // 16진수 데이터와 ASCII 데이터 분리
            int asciiStartIndex = findAsciiStartIndex(remainingData); // ASCII 데이터 시작 인덱스 찾기
            String hexData = remainingData.substring(0, asciiStartIndex).trim(); // 16진수 데이터
            String asciiData = remainingData.substring(asciiStartIndex).trim(); // ASCII 데이터

            // ParsedLine 객체 생성 및 추가
            parsedLines.add(new ParsedLine(number, hexData, asciiData));
        }

        return parsedLines;
    }
    // ASCII 데이터 시작 인덱스를 찾는 메서드
    private static int findAsciiStartIndex(String data) {
        // 세 개 이상의 연속된 공백 이후를 ASCII 데이터의 시작으로 간주
        int index = data.indexOf("   "); // 최소 세 개의 연속된 공백
        return (index != -1) ? index + 2 : data.length(); // 연속 공백이 없으면 끝까지 16진수 데이터로 간주
    }

    public static List<String[]> parsePayloadToTableFormatted(List<ParsedLine> parsedLines) {
        List<String[]> rows = new ArrayList<>();

        StringBuilder rawDataBuilder = new StringBuilder();
        for (ParsedLine line : parsedLines) {
            rawDataBuilder.append(line.hexData.replace(" ", "").trim());
        }
        String rawData = rawDataBuilder.toString();

        // 이더넷 헤더 분석 (0번 ~ 13번 바이트)
        if (rawData.length() >= 28) {
            String destinationMac = formatMacAddress(rawData.substring(0, 12)); // 도착지 MAC
            String sourceMac = formatMacAddress(rawData.substring(12, 24)); // 출발지 MAC
            String etherType = rawData.substring(24, 28).toUpperCase(); // EtherType

            rows.add(new String[]{rawData.substring(0, 12), destinationMac, "도착지 MAC"});
            rows.add(new String[]{rawData.substring(12, 24),  sourceMac, "출발지 MAC"});
            rows.add(new String[]{rawData.substring(24, 28), etherType, "EtherType"});

            if ("0800".equalsIgnoreCase(etherType)) { // IPv4
                parseIPv4HeaderFormatted(rawData.substring(28), rows, 4);
            }
        } else {
            rows.add(new String[]{"1", rawData, "유효하지 않은 패킷: 길이가 너무 짧습니다."});
        }

        return rows;
    }

    private static void parseIPv4HeaderFormatted(String rawData, List<String[]> rows, int startNumber) {
        if (rawData.length() < 40) { // 최소 IPv4 헤더 크기 20 bytes (40 characters in Hex)
            rows.add(new String[]{"IPv4 헤더 길이가 너무 짧습니다.", "오류", "유효하지 않은 IPv4 패킷"});
            return;
        }

        String versionAndIhl = rawData.substring(0, 2);
        String tos = rawData.substring(2, 4);
        String totalLength = rawData.substring(4, 8);
        String identification = rawData.substring(8, 12);
        String flagsAndFragmentOffset = rawData.substring(12, 16);
        String ttl = rawData.substring(16, 18);
        String protocol = rawData.substring(18, 20);
        String headerChecksum = rawData.substring(20, 24);
        String sourceIp = rawData.substring(24, 32);
        String destinationIp = rawData.substring(32, 40);
        String sourceIpDetail = formatIpAddress(rawData.substring(24, 32));
        String destinationIpDetail = formatIpAddress(rawData.substring(32, 40));

        rows.add(new String[]{versionAndIhl, "버전 및 IHL", "IPv4, 헤더 길이 20 바이트"});
        rows.add(new String[]{tos, "Type of Service", "QoS 관련 정보"});
        rows.add(new String[]{totalLength, "Total Length", "패킷 전체 길이 (헤더 + 데이터)"});
        rows.add(new String[]{identification, "Identification", "패킷 식별자"});
        rows.add(new String[]{flagsAndFragmentOffset, "Flags & Fragment Offset", "플래그 및 프래그먼트 오프셋"});
        rows.add(new String[]{ttl, "TTL", "Time To Live: 패킷의 생존 시간"});
        rows.add(new String[]{String.valueOf(startNumber++), protocol, "Protocol: " + identifyProtocol(protocol)});
        rows.add(new String[]{headerChecksum, "Header Checksum", "IPv4 헤더 오류 검출 체크섬"});
        rows.add(new String[]{sourceIp, sourceIpDetail, "패킷의 출발지 IP 주소"});
        rows.add(new String[]{destinationIp, destinationIpDetail, "패킷의 목적지 IP 주소"});
    }

    private static String identifyProtocol(String protocol) {
        switch (protocol) {
            case "06":
                return "TCP (Transmission Control Protocol)";
            case "11":
                return "UDP (User Datagram Protocol)";
            case "01":
                return "ICMP (Internet Control Message Protocol)";
            default:
                return "알 수 없는 프로토콜";
        }
    }

    private static String formatMacAddress(String hex) {
        return hex.replaceAll("(.{2})", "$1:").replaceAll(":$", "").toUpperCase();
    }

    private static String formatIpAddress(String hex) {
        StringBuilder ip = new StringBuilder();
        for (int i = 0; i < hex.length(); i += 2) {
            ip.append(Integer.parseInt(hex.substring(i, i + 2), 16));
            if (i < hex.length() - 2) {
                ip.append(".");
            }
        }
        return ip.toString();
    }
}


