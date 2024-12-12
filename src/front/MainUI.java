package front;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import back.PacketCapture;
import back.PacketListener;
import back.PacketRepository;
import org.jnetpcap.PcapIf;
import utils.RawDataParser;

public class MainUI {
    private static Map<Integer, String> rawDataMap = new HashMap<>();// rawData 저장용 Map

    public static void main(String[] args) {
    	//메인프레임
        JFrame frame = new JFrame("패킷 분석기");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);
        frame.setLayout(new BorderLayout());

        // 메뉴바
        JMenuBar menuBar = new JMenuBar();
        JMenuItem startMenu = new JMenuItem("패킷분석 시작");
        JMenuItem stopMenu = new JMenuItem("정지");
        JMenuItem dbListMenu = new JMenuItem("DB 열람");
        JMenuItem dbSaveMenu = new JMenuItem("DB 저장");
        JMenuItem clearMenu = new JMenuItem("테이블 초기화");
        startMenu.setMaximumSize(new Dimension(60, 40));
        stopMenu.setMaximumSize(new Dimension(60, 40));
        dbListMenu.setMaximumSize(new Dimension(60, 40));
        dbSaveMenu.setMaximumSize(new Dimension(60, 40));
        clearMenu.setMaximumSize(new Dimension(100, 40));
        // 테두리 추가
        startMenu.setBorder(new LineBorder(Color.BLACK, 1));
        stopMenu.setBorder(new LineBorder(Color.BLACK, 1));
        dbListMenu.setBorder(new LineBorder(Color.BLACK, 1));
        dbSaveMenu.setBorder(new LineBorder(Color.BLACK, 1));
        clearMenu.setBorder(new LineBorder(Color.BLACK, 1));
        menuBar.add(startMenu);
        menuBar.add(stopMenu);
        menuBar.add(dbListMenu);
        menuBar.add(dbSaveMenu);
        menuBar.add(clearMenu);
        frame.setJMenuBar(menuBar);
        

        // 패킷 테이블 생성
        DefaultTableModel tableModel = new DefaultTableModel(
                new String[]{"번호", "프로토콜", "출발 IP", "도착 IP", "길이", "시간"}, 0);
        JTable packetTable = new JTable(tableModel);
        
        // 테이블 오름차 내림차 정렬
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        // 길이 열에 숫자 형식 적용
        sorter.setComparator(4, (o1, o2) -> {
            // o1과 o2를 Integer로 변환하여 비교
            Integer num1 = Integer.parseInt(o1.toString());
            Integer num2 = Integer.parseInt(o2.toString());
            return num1.compareTo(num2);
        });
        packetTable.setRowSorter(sorter);
        
        // 필터 패널
        JPanel filterPanel = new JPanel(new BorderLayout());
        JPanel filterInnerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterInnerPanel.add(new JLabel("필터:"));
        JTextField filterTextField = new JTextField(30);
        filterInnerPanel.add(filterTextField);

        JButton filterButton = new JButton("적용");
        filterInnerPanel.add(filterButton);
        JButton clearFilterButton = new JButton("초기화");
        filterInnerPanel.add(clearFilterButton);
        filterPanel.add(filterInnerPanel, BorderLayout.CENTER);
        filterPanel.setPreferredSize(new Dimension(frame.getWidth(), 35)); // 필터 패널 높이 증가
        frame.add(filterPanel, BorderLayout.NORTH);

        // 상세정보 및 데이터 영역
        JTextArea detailTextArea = new JTextArea();
        detailTextArea.setEditable(false);
        JScrollPane detailScrollPane = new JScrollPane(detailTextArea);
        detailScrollPane.setBorder(BorderFactory.createTitledBorder("패킷 상세 정보"));
        
        JButton analyzeButton = new JButton("분석");
        
        // 패킷 상세 정보 + 분석 버튼 패널
        JPanel detailPanelWithButton = new JPanel(new BorderLayout());
        detailPanelWithButton.add(detailScrollPane, BorderLayout.CENTER); // 상세 정보
        detailPanelWithButton.add(analyzeButton, BorderLayout.EAST); // 분석 버튼
        analyzeButton.setPreferredSize(new Dimension(100, detailScrollPane.getPreferredSize().height));
        
        // 원시 데이터 영역
        String[] rawColumns = {"번호", "16진수 데이터", "ASCII 데이터"};
        DefaultTableModel rawTableModel = new DefaultTableModel(rawColumns, 0);
        JTable rawTable = new JTable(rawTableModel);
        JScrollPane rawTableScrollPane = new JScrollPane(rawTable);
        rawTableScrollPane.setBorder(BorderFactory.createTitledBorder("원시 데이터 상세 정보"));

        // 페이로드 데이터 영역
        DefaultTableModel payloadTableModel = new DefaultTableModel(new String[]{"값", "내용", "설명"}, 0);
        JTable payloadTable = new JTable(payloadTableModel);
        JScrollPane payloadScrollPane = new JScrollPane(payloadTable);
        payloadScrollPane.setBorder(BorderFactory.createTitledBorder("분석 데이터"));

        // 좌측 패널 (상세 정보 + 분석 버튼 + 분석 영역)
        JSplitPane leftSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, detailPanelWithButton, payloadScrollPane);
        leftSplitPane.setResizeWeight(0.5); // 상세 정보와 원시 데이터 패널 비율 설정

        // 메인 패널 (좌측 + 원시)
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftSplitPane, rawTableScrollPane);
        mainSplitPane.setResizeWeight(0.5); // 좌측과 분석 영역 비율 설정

        // 패킷 테이블 스크롤
        JScrollPane scrollPane = new JScrollPane(packetTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("패킷 목록"));

        // 상단 패널 (패킷 테이블 + 메인 패널)
        JSplitPane topSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPane, mainSplitPane);
        topSplitPane.setResizeWeight(0.6); // 패킷 테이블과 하단 영역 비율 설정
        topSplitPane.setDividerLocation(400); // 초기 위치 설정

        frame.add(topSplitPane, BorderLayout.CENTER);
        
        // PacketCapture 객체 생성 및 리스너 설정
        PacketCapture packetCapture = new PacketCapture();
        packetCapture.setPacketListener(new PacketListener() {
            @Override
            public void onPacketCaptured(String protocol, String sourceIP, String destinationIP, int length, String timeStamp, String rawData) {
                SwingUtilities.invokeLater(() -> {
                    int rowIndex = tableModel.getRowCount();
                    tableModel.addRow(new Object[]{rowIndex + 1, protocol, sourceIP, destinationIP, length, timeStamp});
                    rawDataMap.put(rowIndex, rawData); // rawData 저장
                });
            }
        });
        
        // 필터링 버튼 동작 설정
        filterButton.addActionListener(e -> {
            String text = filterTextField.getText();
            if (text.trim().length() > 0) {
                sorter.setRowFilter(RowFilter.regexFilter(text));
            } else {
                sorter.setRowFilter(null); // 필터 제거
            }
        });

        // 필터 초기화 버튼
        clearFilterButton.addActionListener(e -> {
            filterTextField.setText("");
            sorter.setRowFilter(null); // 필터 제거
        });

        // 패킷 선택 시 rawData 표시
        packetTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && packetTable.getSelectedRow() != -1) {
                int selectedRow = packetTable.getSelectedRow();
                String protocol = (String) packetTable.getValueAt(selectedRow, 1);
                String sourceIP = (String) packetTable.getValueAt(selectedRow, 2);
                String destinationIP = (String) packetTable.getValueAt(selectedRow, 3);
                String length = packetTable.getValueAt(selectedRow, 4).toString();
                String timeStamp = (String) packetTable.getValueAt(selectedRow, 5);
                String rawData = rawDataMap.get(selectedRow);
                if (rawData == null) {
                    rawData = "원시 데이터 없음";
                }
                
                detailTextArea.setText(String.format(
                        "Protocol: %s\n출발 IP: %s\n도착 IP: %s\n길이: %s bytes\n시간: %s",
                        protocol, sourceIP, destinationIP, length, timeStamp
                ));
                
                // 원시 데이터 테이블 초기화 후 추가
                rawTableModel.setRowCount(0);
                if (rawData != null) {
                    List<RawDataParser.ParsedLine> parsedLines = RawDataParser.parseRawData(rawData);
                    for (RawDataParser.ParsedLine line : parsedLines) {
                        rawTableModel.addRow(new Object[]{line.number, line.hexData, line.asciiData});
                    }
                }

            }
        });
        // 분석 버튼 동작
        analyzeButton.addActionListener(e -> {
            int selectedRow = packetTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(null, "분석할 패킷을 선택하세요.", "오류", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String rawData = rawDataMap.get(selectedRow);
            if (rawData == null || rawData.isEmpty()) {
                JOptionPane.showMessageDialog(null, "원시 데이터가 없습니다.", "오류", JOptionPane.ERROR_MESSAGE);
                return;
            }

            List<RawDataParser.ParsedLine> parsedLines = RawDataParser.parseRawData(rawData);
            List<String[]> analysisResults = RawDataParser.parsePayloadToTableFormatted(parsedLines);
            payloadTableModel.setRowCount(0);
            for (String[] row : analysisResults) {
            	payloadTableModel.addRow(row);
            }
        });
        
        
        // 패킷 캡처 시작 버튼 동작
        startMenu.addActionListener(e -> {
            List<PcapIf> devices = packetCapture.getNetworkDevices();
            if (devices == null || devices.isEmpty()) {
                JOptionPane.showMessageDialog(null, "캡처 가능한 네트워크 장치를 찾을 수 없습니다.", "오류", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // 디바이스 이름 + 설명 형식으로 표시할 배열 생성
            String[] deviceDisplayNames = devices.stream()
                .map(device -> String.format("%s (%s)", 
                    device.getName(), 
                    device.getDescription() != null ? device.getDescription() : "설명 없음"))
                .toArray(String[]::new);
            // 디바이스 선택 창 표시
            String selectedDeviceDisplayName = (String) JOptionPane.showInputDialog(
                    null, 
                    "캡처할 네트워크를 장치 선택하세요:", 
                    "장치 선택", 
                    JOptionPane.QUESTION_MESSAGE, 
                    null, 
                    deviceDisplayNames, 
                    deviceDisplayNames[0]
            );
            // 사용자가 디바이스를 선택했는지 확인
            if (selectedDeviceDisplayName != null) {
                int selectedIndex = -1;
                for (int i = 0; i < deviceDisplayNames.length; i++) {
                    if (deviceDisplayNames[i].equals(selectedDeviceDisplayName)) {
                        selectedIndex = i;
                        break;
                    }
                }

                // 선택된 디바이스로 패킷 캡처 시작
                if (selectedIndex >= 0) {
                    packetCapture.startCapture(devices.get(selectedIndex));
                }
            }
        });

        // 패킷 캡처 중지 버튼 동작
        stopMenu.addActionListener(e -> {
            packetCapture.stopCapture();
            JOptionPane.showMessageDialog(null, "패킷 캡처가 중지되었습니다.", "정보", JOptionPane.INFORMATION_MESSAGE);
        });

        // DB 저장 버튼
        dbSaveMenu.addActionListener(e -> {
            int selectedRow = packetTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(null, "저장할 패킷을 선택하세요.", "오류", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String protocol = (String) packetTable.getValueAt(selectedRow, 1);
            String sourceIP = (String) packetTable.getValueAt(selectedRow, 2);
            String destinationIP = (String) packetTable.getValueAt(selectedRow, 3);
            int length = Integer.parseInt(packetTable.getValueAt(selectedRow, 4).toString());
            String timeStamp = (String) packetTable.getValueAt(selectedRow, 5);
            String rawData = rawDataMap.get(selectedRow);
            if (rawData == null) {
                rawData = "원시 데이터 없음";
            }
            PacketRepository repository = new PacketRepository();
            repository.savePacket(protocol, sourceIP, destinationIP, length, timeStamp, rawData);
            JOptionPane.showMessageDialog(null, "패킷이 데이터베이스에 저장되었습니다.", "성공", JOptionPane.INFORMATION_MESSAGE);
        });
        
        // DB 열람 버튼 
        dbListMenu.addActionListener(e -> {
            PacketRepository repository = new PacketRepository();
            List<String[]> packets = repository.getAllPackets();

            tableModel.setRowCount(0);
            rawDataMap.clear();

            // 데이터 추가
            for (int i = 0; i < packets.size(); i++) {
                String[] packet = packets.get(i);
                String protocol = packet[0];
                String sourceIP = packet[1];
                String destinationIP = packet[2];
                String length = packet[3];
                String timestamp = packet[4];
                String rawData = packet[5];

                tableModel.addRow(new Object[]{i + 1, protocol, sourceIP, destinationIP, length, timestamp});
                rawDataMap.put(i, rawData); // rawData 저장
            }

            JOptionPane.showMessageDialog(null, "데이터베이스에서 패킷 목록을 불러왔습니다.", "성공", JOptionPane.INFORMATION_MESSAGE);
        });
        
        // 테이블 초기화 버튼
        clearMenu.addActionListener(e -> {
            tableModel.setRowCount(0);
            rawDataMap.clear(); // rawData도 초기화
        });

        frame.setVisible(true);
    }
}