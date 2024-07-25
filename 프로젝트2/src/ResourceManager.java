import java.io.*;
import java.util.HashMap;

public class ResourceManager {
    private static final int MEMORY_SIZE = 0x100000;  // 1MB
    byte[] memory = new byte[MEMORY_SIZE];
    int[] registers = new int[10];  // A, X, L, B, S, T, F, PC, SW (10개의 레지스터)
    HashMap<String, Object> deviceManager = new HashMap<>();

    String programName = "";
    int startAddr;
    int programLength;
    int currentMemory;

    String currentOperator;
    String targetAddress;
    String device;
    String log = "";

    String currentDevice = "";

    public void setDevice() {
        File file = new File("F1");
        try {
            deviceManager.put("F1", new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        File file2 = new File("05");
        try {
            deviceManager.put("05", new FileOutputStream(file2));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    HashMap<String, Integer> symtabList = new HashMap<>();

	public void setSymtabList(String name, int address) {
        if (symtabList.containsKey(name) == true) {
            return;
        }
		symtabList.put(name, address);

	}

	public int getSymtabList(String name) {
		if (symtabList.containsKey(name) == false) {
			return -1;
		}
		return symtabList.get(name);
	}

    int changedMemorySize = -1;
    int changedMemoryAddr = -1;

    public void initializeResource() {
        for (int i = 0; i < MEMORY_SIZE; i++) {
            memory[i] = 0;
        }
        for (int i = 0; i < registers.length; i++) {
            registers[i] = 0;
        }
        deviceManager.clear();
        programName = "";
        startAddr = 0;
        programLength = 0;
        currentMemory = 0;
        changedMemorySize = -1;
        changedMemoryAddr = -1;
    }

    public void setMemory(int address, byte[] data, int length) {
        if (address < 0 || address + length > MEMORY_SIZE) {
            throw new IllegalArgumentException("Invalid memory address");
        }
        System.arraycopy(data, 0, memory, address, length);
        changedMemoryAddr = address;
        changedMemorySize = length;
    }

    public byte[] getMemory(int address, int length) {
        if (address < 0 || address + length > MEMORY_SIZE) {
            throw new IllegalArgumentException("Invalid memory address");
        }
        byte[] data = new byte[length];
        System.arraycopy(memory, address, data, 0, length);
        return data;
    }

    public void setRegister(int index, int value) {
        if (index < 0 || index >= registers.length) {
            throw new IllegalArgumentException("Invalid register index");
        }
        registers[index] = value;
    }

    public int getRegister(int index) {
        if (index < 0 || index >= registers.length) {
            throw new IllegalArgumentException("Invalid register index");
        }
        return registers[index];
    }

	public void TD(String name) {
		currentDevice = name;
		if (deviceManager.containsKey(name)) {
			registers[9] = 1;
		} else {
			registers[9] = 0;
		}
	}

    public byte RD() {
        if (currentDevice.equals("")) {
            registers[9] = -1;
            return 0;
        }
        InputStream inputStream = (InputStream) deviceManager.get(currentDevice);
        if (inputStream == null) {
            registers[9] = -1;
            return 0;
        }

        byte data = 0;
        try {
            int readData = inputStream.read();
            if (readData == -1) {
                registers[9] = 1; // End of file
            } else {
                data = (byte) readData;
                registers[9] = 0; // Successful read
            }
        } catch (Exception e) {
            registers[9] = -1; // Error during read
        }

        return data;
    }

    public byte WD() {
        if (currentDevice.equals("")) {
            registers[9] = -1;
            return 0;
        }
        OutputStream outputStream = (OutputStream) deviceManager.get(currentDevice);
        if (outputStream == null || !(outputStream instanceof OutputStream)) {
            registers[9] = -1;
            return 0;
        }

        byte data = (byte) registers[0]; // Assuming register A holds the data to be written
        try {
            outputStream.write(data);
            outputStream.flush(); // Ensure the data is written out
            registers[9] = 0; // Successful write
        } catch (Exception e) {
            registers[9] = -1; // Error during write
        }

        return data;
    }

    public void setCurrentDevice(String deviceName) {
        if (deviceManager.containsKey(deviceName)) {
            currentDevice = deviceName;
        } else {
            throw new IllegalArgumentException("Device not found: " + deviceName);
        }
    }

    public byte[] intToByte(int data) {
        String dataString = String.format("%06X", data);
        byte[] ret = new byte[3];
        ret[0] = (byte) Integer.parseInt(dataString.substring(0, 2), 16);
        ret[1] = (byte) Integer.parseInt(dataString.substring(2, 4), 16);
        ret[2] = (byte) Integer.parseInt(dataString.substring(4, 6), 16);
        return ret;
    }

    public int byteToInt(byte[] data) {
        return Integer.parseInt(String.format("%02X%02X%02X", data[0], data[1], data[2]), 16);
    }
}
