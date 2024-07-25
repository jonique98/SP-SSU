import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

//코드 참조 : https://github.com/always0ne/SIC_XE_Simulator

public class ObjectCodeLoader {
    private ResourceManager rmng;
    private ArrayList<Modify> mRec = new ArrayList<>();

    public ObjectCodeLoader(ResourceManager resourceManager) {
        this.rmng = resourceManager;
    }

    public void load(File objectCode) {
        String line;
        BufferedReader bufReader;
        int codeCur = 0, secLen = 0;
        byte[] data = new byte[1];

        try {
            bufReader = new BufferedReader(new FileReader(objectCode));
            while ((line = bufReader.readLine()) != null) {
                if (line.length() == 0) continue;
                char recordCode = line.charAt(0);
                switch (recordCode) {
                    case 'H':
                        String symbol = line.substring(1, 7).trim();
                        if (rmng.programName.isEmpty()) {
                            rmng.programName = symbol;
                            rmng.startAddr = Integer.parseInt(line.substring(7, 13), 16);
                        }
                        rmng.setSymtabList(symbol, rmng.programLength);
                        secLen = Integer.parseInt(line.substring(13), 16);
                        rmng.programLength += secLen;
                        codeCur = 0;
                        break;
                    case 'D':
                        String buf = "";
                        for (int i = 1; i < line.length() - 5; i++) {
                            if (Character.isDigit(line.charAt(i))) {
                                rmng.setSymtabList(buf, Integer.parseInt(line.substring(i, i + 6), 16));
                                i += 5;
                                buf = "";
                            } else {
                                buf += line.charAt(i);
                            }
                        }
                        break;
                    case 'T':
                        while (codeCur != Integer.parseInt(line.substring(1, 7), 16)) {
                            data[0] = 0;
                            rmng.setMemory(rmng.currentMemory++, data, 1);
                            codeCur++;
                        }
                        for (int i = 9; i < line.length(); i += 2) {
                            data[0] = (byte) Integer.parseInt(line.substring(i, i + 2), 16);
                            rmng.setMemory(rmng.currentMemory++, data, 1);
                            codeCur++;
                        }
                        break;
                    case 'M':
                        while (codeCur != secLen) {
                            data[0] = 0;
                            rmng.setMemory(rmng.currentMemory++, data, 1);
                            codeCur++;
                        }
                        mRec.add(new Modify(Integer.parseInt(line.substring(1, 7), 16),
                                Integer.parseInt(line.substring(7, 9)),
                                rmng.programLength - secLen,
                                line.substring(9, line.length())));
                        break;
                    case 'E':
                        if (line.length() > 1) {
                            int startAddr = Integer.parseInt(line.substring(1), 16);
                            rmng.startAddr = startAddr;
                        }
                        break;
                    default:
                        // Handle unknown record type
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Modify class to store modification information
    public class Modify {
        int addr;
        int size;
        int sectionAddr;
        String symbol;

        public Modify(int addr, int size, int sectionAddr, String symbol) {
            this.addr = addr;
            this.size = size;
            this.sectionAddr = sectionAddr;
            this.symbol = symbol;
        }
    }

    public void modify() {
        for (int i = 0; i < mRec.size(); i++) {
            Modify modify = mRec.get(i);
            int addr = modify.addr + modify.sectionAddr;
            byte[] originalMem = rmng.getMemory(addr, 3), changedData = new byte[3];
            String originalData = String.format("%02X%02X%02X", originalMem[0], originalMem[1], originalMem[2]);
            String newData = String.format("%06X", rmng.getSymtabList(modify.symbol.substring(1)));
            String calculatedData = "";
            if (modify.symbol.substring(0, 1).equals("+"))
                calculatedData = String.format("%06X",
                        Integer.parseInt(originalData, 16) + Integer.parseInt(newData, 16));
            else if (modify.symbol.substring(0, 1).equals("-"))
                calculatedData = String.format("%06X",
                        Integer.parseInt(originalData, 16) - Integer.parseInt(newData, 16));
            for (int j = 0; j < 3; j++)
                changedData[j] = (byte) Integer.parseInt(calculatedData.substring(2 * j, 2 * j + 2), 16);
            rmng.setMemory(addr, changedData, 3);
        }
    }
}
