import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.DefaultHighlighter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class VIsualSimulator extends JFrame {
    ResourceManager resourceManager = new ResourceManager();
    SicXeSimulator sicXeSimulator = new SicXeSimulator(resourceManager);
    ObjectCodeLoader objectCodeLoader = new ObjectCodeLoader(resourceManager);
    private JTextPane programName, programLength, currentOperatorPane, targetAddress, devicePane;
    private JTextPane regAHex, regADec, regXHex, regXDec, regLHex, regLDec, regBHex, regBDec, regSHex, regSDec, regTHex, regTDec, regF, regPCHex, regPCDec, regSW;
    private JTextArea memArea, opArea;
    private JButton stepBtn, runBtn;

    public VIsualSimulator() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 700, 730); // Increased window size
        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(mainPanel);
        mainPanel.setLayout(null);

        JTextField filePathField = new JTextField();
        JButton openBtn = new JButton("Open");
        openBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                Frame f = new Frame("Parent");
                FileDialog dialog = new FileDialog(f, "Open", FileDialog.LOAD);
                dialog.setDirectory(".");
                dialog.setVisible(true);
                if (dialog.getFile() == null) return;
                try {
                    String dfName = dialog.getDirectory() + dialog.getFile();
                    File program = new File(dfName);
                    loadFile(program);
                } catch (Exception e2) {
                    JOptionPane.showMessageDialog(f, "Error opening file: " + e2.getMessage());
                }
                filePathField.setText(dialog.getFile());
                setTitle(dialog.getFile());
            }
        });
        openBtn.setBounds(320, 17, 68, 23);
        mainPanel.add(openBtn);

        filePathField.setEditable(false);
        filePathField.setBounds(90, 17, 200, 23);
        mainPanel.add(filePathField);
        filePathField.setColumns(10);

        JLabel fileLabel = new JLabel("File:");
        fileLabel.setBounds(12, 21, 73, 15);
        mainPanel.add(fileLabel);

        JPanel regPanel = new JPanel();
        regPanel.setLayout(null);
        regPanel.setToolTipText("");
        regPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Registers", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        regPanel.setBounds(12, 165, 400, 246);
        mainPanel.add(regPanel);

        JLabel regALabel = new JLabel("A(#0)");
        regALabel.setBounds(12, 41, 37, 15);
        regPanel.add(regALabel);

        regAHex = new JTextPane();
        regAHex.setEditable(false);
        regAHex.setBorder(new LineBorder(UIManager.getColor("Button.shadow")));
        regAHex.setBackground(SystemColor.menu);
        regAHex.setBounds(134, 40, 65, 18);
        regPanel.add(regAHex);

        regADec = new JTextPane();
        regADec.setEditable(false);
        regADec.setBorder(new LineBorder(UIManager.getColor("Button.shadow")));
        regADec.setBackground(SystemColor.menu);
        regADec.setBounds(57, 40, 65, 18);
        regPanel.add(regADec);

        JLabel decLabel = new JLabel("Dec");
        decLabel.setBounds(57, 20, 37, 15);
        regPanel.add(decLabel);

        JLabel hexLabel = new JLabel("Hex");
        hexLabel.setBounds(134, 20, 37, 15);
        regPanel.add(hexLabel);

        JLabel regXLabel = new JLabel("X(#1)");
        regXLabel.setBounds(12, 62, 37, 15);
        regPanel.add(regXLabel);

        regXDec = new JTextPane();
        regXDec.setEditable(false);
        regXDec.setBorder(new LineBorder(UIManager.getColor("Button.shadow")));
        regXDec.setBackground(SystemColor.menu);
        regXDec.setBounds(57, 61, 65, 18);
        regPanel.add(regXDec);

        regXHex = new JTextPane();
        regXHex.setEditable(false);
        regXHex.setBorder(new LineBorder(UIManager.getColor("Button.shadow")));
        regXHex.setBackground(SystemColor.menu);
        regXHex.setBounds(134, 61, 65, 18);
        regPanel.add(regXHex);

        JLabel regLLabel = new JLabel("L(#2)");
        regLLabel.setBounds(12, 84, 37, 15);
        regPanel.add(regLLabel);

        regLDec = new JTextPane();
        regLDec.setEditable(false);
        regLDec.setBorder(new LineBorder(UIManager.getColor("Button.shadow")));
        regLDec.setBackground(SystemColor.menu);
        regLDec.setBounds(57, 83, 65, 18);
        regPanel.add(regLDec);

        regLHex = new JTextPane();
        regLHex.setEditable(false);
        regLHex.setBorder(new LineBorder(UIManager.getColor("Button.shadow")));
        regLHex.setBackground(SystemColor.menu);
        regLHex.setBounds(134, 83, 65, 18);
        regPanel.add(regLHex);

        JLabel regBLabel = new JLabel("B(#3)");
        regBLabel.setBounds(12, 106, 37, 15);
        regPanel.add(regBLabel);

        regBDec = new JTextPane();
        regBDec.setEditable(false);
        regBDec.setBorder(new LineBorder(UIManager.getColor("Button.shadow")));
        regBDec.setBackground(SystemColor.menu);
        regBDec.setBounds(57, 105, 65, 18);
        regPanel.add(regBDec);

        regBHex = new JTextPane();
        regBHex.setEditable(false);
        regBHex.setBorder(new LineBorder(UIManager.getColor("Button.shadow")));
        regBHex.setBackground(SystemColor.menu);
        regBHex.setBounds(134, 105, 65, 18);
        regPanel.add(regBHex);

        JLabel regSLabel = new JLabel("S(#4)");
        regSLabel.setBounds(12, 129, 37, 15);
        regPanel.add(regSLabel);

        regSDec = new JTextPane();
        regSDec.setEditable(false);
        regSDec.setBorder(new LineBorder(UIManager.getColor("Button.shadow")));
        regSDec.setBackground(SystemColor.menu);
        regSDec.setBounds(57, 128, 65, 18);
        regPanel.add(regSDec);

        regSHex = new JTextPane();
        regSHex.setEditable(false);
        regSHex.setBorder(new LineBorder(UIManager.getColor("Button.shadow")));
        regSHex.setBackground(SystemColor.menu);
        regSHex.setBounds(134, 128, 65, 18);
        regPanel.add(regSHex);

        JLabel regTLabel = new JLabel("T(#5)");
        regTLabel.setBounds(12, 150, 37, 15);
        regPanel.add(regTLabel);

        regTDec = new JTextPane();
        regTDec.setEditable(false);
        regTDec.setBorder(new LineBorder(UIManager.getColor("Button.shadow")));
        regTDec.setBackground(SystemColor.menu);
        regTDec.setBounds(57, 149, 65, 18);
        regPanel.add(regTDec);

        regTHex = new JTextPane();
        regTHex.setEditable(false);
        regTHex.setBorder(new LineBorder(UIManager.getColor("Button.shadow")));
        regTHex.setBackground(SystemColor.menu);
        regTHex.setBounds(134, 149, 65, 18);
        regPanel.add(regTHex);

        JLabel regFLabel = new JLabel("F(#6)");
        regFLabel.setBounds(12, 173, 37, 15);
        regPanel.add(regFLabel);

        regF = new JTextPane();
        regF.setEditable(false);
        regF.setBorder(new LineBorder(UIManager.getColor("Button.shadow")));
        regF.setBackground(SystemColor.menu);
        regF.setBounds(57, 172, 142, 18);
        regPanel.add(regF);

        JLabel regPCLabel = new JLabel("PC(#8)");
        regPCLabel.setBounds(12, 195, 40, 15);
        regPanel.add(regPCLabel);

        regPCDec = new JTextPane();
        regPCDec.setEditable(false);
        regPCDec.setBorder(new LineBorder(UIManager.getColor("Button.shadow")));
        regPCDec.setBackground(SystemColor.menu);
        regPCDec.setBounds(57, 194, 65, 18);
        regPanel.add(regPCDec);

        regPCHex = new JTextPane();
        regPCHex.setEditable(false);
        regPCHex.setBorder(new LineBorder(UIManager.getColor("Button.shadow")));
        regPCHex.setBackground(SystemColor.menu);
        regPCHex.setBounds(134, 194, 65, 18);
        regPanel.add(regPCHex);

        JLabel regSWLabel = new JLabel("SW(#9)");
        regSWLabel.setBounds(12, 217, 42, 15);
        regPanel.add(regSWLabel);

        regSW = new JTextPane();
        regSW.setEditable(false);
        regSW.setBorder(new LineBorder(UIManager.getColor("Button.shadow")));
        regSW.setBackground(SystemColor.menu);
        regSW.setBounds(57, 216, 142, 18);
        regPanel.add(regSW);

        JLabel programLabel = new JLabel("Program Name:");
        programLabel.setBounds(15, 60, 120, 15);
        mainPanel.add(programLabel);

        programName = new JTextPane();
        programName.setEditable(false);
        programName.setBorder(new LineBorder(UIManager.getColor("Button.shadow")));
        programName.setBackground(SystemColor.menu);
        programName.setBounds(175, 60, 100, 18);
        mainPanel.add(programName);

        JLabel programLengthLabel = new JLabel("Program Length:");
        programLengthLabel.setBounds(15, 90, 120, 15);
        mainPanel.add(programLengthLabel);

        programLength = new JTextPane();
        programLength.setEditable(false);
        programLength.setBorder(new LineBorder(UIManager.getColor("Button.shadow")));
        programLength.setBackground(SystemColor.menu);
        programLength.setBounds(175, 90, 100, 18);
        mainPanel.add(programLength);

        JLabel diveceLabel = new JLabel("Device:");
        diveceLabel.setBounds(430, 60, 120, 15);
        mainPanel.add(diveceLabel);

        devicePane = new JTextPane();
        devicePane.setEditable(false);
        devicePane.setBorder(new LineBorder(UIManager.getColor("Button.shadow")));
        devicePane.setBackground(SystemColor.menu);
        devicePane.setBounds(550, 60, 100, 18);
        mainPanel.add(devicePane);

        JLabel targetAddressLabel = new JLabel("Target Address:");
        targetAddressLabel.setBounds(430, 90, 120, 18);
        mainPanel.add(targetAddressLabel);

        targetAddress = new JTextPane();
        targetAddress.setEditable(false);
        targetAddress.setBorder(new LineBorder(UIManager.getColor("Button.shadow")));
        targetAddress.setBackground(SystemColor.menu);
        targetAddress.setBounds(550, 90, 100, 18);
        mainPanel.add(targetAddress);

        JLabel currentOperatorLabel = new JLabel("Current Operator:");
        currentOperatorLabel.setBounds(430, 120, 120, 15);
        mainPanel.add(currentOperatorLabel);

        currentOperatorPane = new JTextPane();
        currentOperatorPane.setEditable(false);
        currentOperatorPane.setBorder(new LineBorder(UIManager.getColor("Button.shadow")));
        currentOperatorPane.setBackground(SystemColor.menu);
        currentOperatorPane.setBounds(550, 120, 100, 18);
        mainPanel.add(currentOperatorPane);

        JLabel instLabel = new JLabel("Instructions");
        instLabel.setBounds(430, 150, 100, 15);
        mainPanel.add(instLabel);

        opArea = new JTextArea();
        opArea.setEditable(false);
        JScrollPane instScrollPane = new JScrollPane(opArea);
        instScrollPane.setBounds(430, 170, 150, 240);
        instScrollPane.setVisible(true);
        mainPanel.add(instScrollPane);


        JLabel memLabel = new JLabel("Memory");
        memLabel.setBounds(12, 420, 100, 15);
        mainPanel.add(memLabel);

        memArea = new JTextArea();
        memArea.setEditable(false);
        JScrollPane memScrollPane = new JScrollPane(memArea);
        memScrollPane.setBounds(12, 440, 680, 250);
        memScrollPane.setVisible(true);
        mainPanel.add(memScrollPane);

        stepBtn = new JButton("Step");
        stepBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                executeOneStep();
            }
        });
        stepBtn.setBounds(600, 200, 100, 23);
        mainPanel.add(stepBtn);

        runBtn = new JButton("Run All");
        runBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                executeAllSteps();
            }
        });
        runBtn.setBounds(600, 240, 100, 23);
        mainPanel.add(runBtn);

        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        closeBtn.setBounds(600, 280, 100, 23);
        mainPanel.add(closeBtn);
    }

  private void loadFile(File program) {
    resourceManager.initializeResource();
    objectCodeLoader.load(program);
    objectCodeLoader.modify();
    resourceManager.setDevice();
    updateMemory();
    updateRegisters();
    programName.setText(resourceManager.programName);
    programLength.setText(Integer.toString(resourceManager.programLength));
}

private void executeOneStep() {
    // One step logic
    sicXeSimulator.oneStep();
    updateRegisters();
    updateMemory();
    currentOperatorPane.setText(resourceManager.currentOperator);
    targetAddress.setText(resourceManager.targetAddress);
    opArea.append(resourceManager.currentOperator);
    opArea.append("\n");
    resourceManager.currentOperator = "";

    highlightMemory();
}

private void highlightMemory() {
    if (resourceManager.changedMemoryAddr != -1) {
        memArea.getHighlighter().removeAllHighlights();

        int startOffset = memoryAddressToOffset(resourceManager.changedMemoryAddr);
        int endOffset = startOffset + resourceManager.changedMemorySize * 3; // 각 바이트가 3개의 문자로 표시됨 (예: "FF ")

        try {
            memArea.getHighlighter().addHighlight(startOffset, endOffset, new DefaultHighlighter.DefaultHighlightPainter(Color.CYAN));
            
            // 포커스 이동
            Rectangle viewRect = memArea.modelToView(startOffset);
            if (viewRect != null) {
                memArea.scrollRectToVisible(viewRect);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        resourceManager.changedMemoryAddr = -1;
        resourceManager.changedMemorySize = -1;
    }
}

private int memoryAddressToOffset(int address) {
    int lines = address / 16;
    int column = address % 16;
    return lines * (4 + 1 + 16 * 3 + 1) + (4 + 1) + column * 3; // 주소 + 탭 + 열 오프셋
}

private void executeAllSteps() {
    this.executeOneStep();
    while (resourceManager.getRegister(8) != resourceManager.startAddr) {
        this.executeOneStep();
    }
    currentOperatorPane.setText("");
    targetAddress.setText("");
}

private void updateMemory() {
    // Update memory logic
    byte[] memory = resourceManager.getMemory(0, 0x1080);
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < memory.length; i++) {
        if (i % 16 == 0) {
            sb.append(String.format("%04X\t", i)); // 주소 부분에 \t 추가
        }
        sb.append(String.format("%02X ", memory[i]));
        if (i % 16 == 15) {
            sb.append("\n");
        }
    }
    memArea.setText(sb.toString());

    // 스크롤을 맨 위로 이동
    memArea.setCaretPosition(0);
}


private void updateRegisters() {
    // Update screen logic
    regADec.setText(Integer.toString(resourceManager.getRegister(0)));
    regAHex.setText(Integer.toHexString(resourceManager.getRegister(0)));

    regXDec.setText(Integer.toString(resourceManager.getRegister(1)));
    regXHex.setText(Integer.toHexString(resourceManager.getRegister(1)));

    regLDec.setText(Integer.toString(resourceManager.getRegister(2)));
    regLHex.setText(Integer.toHexString(resourceManager.getRegister(2)));

    regBDec.setText(Integer.toString(resourceManager.getRegister(3)));
    regBHex.setText(Integer.toHexString(resourceManager.getRegister(3)));

    regSDec.setText(Integer.toString(resourceManager.getRegister(4)));
    regSHex.setText(Integer.toHexString(resourceManager.getRegister(4)));

    regTDec.setText(Integer.toString(resourceManager.getRegister(5)));
    regTHex.setText(Integer.toHexString(resourceManager.getRegister(5)));

    regF.setText(Integer.toString(resourceManager.getRegister(6)));
    regF.setText(Integer.toHexString(resourceManager.getRegister(6)));

    regPCDec.setText(Integer.toString(resourceManager.getRegister(8)));
    regPCHex.setText(Integer.toHexString(resourceManager.getRegister(8)));

    regSW.setText(Integer.toString(resourceManager.getRegister(9)));
    regSW.setText(Integer.toHexString(resourceManager.getRegister(9)));
}

public static void main(String[] args) {
    EventQueue.invokeLater(() -> {
        try {
            VIsualSimulator frame = new VIsualSimulator();
            frame.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    });
}
}
