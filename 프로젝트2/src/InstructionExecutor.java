import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

//코드 참조 : https://github.com/always0ne/SIC_XE_Simulator

public class InstructionExecutor {
	public class Instruction {
		String name;
		int opcode;
		int format; // 1, 2, 3/4
		int numOperands;
	
		public Instruction(String name, int opcode, int format, int numOperands) {
			this.name = name;
			this.opcode = opcode;
			this.format = format;
			this.numOperands = numOperands;
		}
	}

	private ResourceManager resourceManager;
	private HashMap<Integer, Instruction> opcodeTable = new HashMap<>();
	private Instruction currentInstruction;

	public InstructionExecutor(ResourceManager resourceManager) {
		this.resourceManager = resourceManager;
		loadOpcodeTable("inst_table.txt");
	}

	   private void loadOpcodeTable(String fileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\s+");
                String name = parts[0];
                int opcode = Integer.parseInt(parts[1], 16);
                int format = parts[2].contains("/") ? 3 : Integer.parseInt(parts[2]);
                int numOperands = Integer.parseInt(parts[3]);
                Instruction inst = new Instruction(name, opcode, format, numOperands);
                opcodeTable.put(opcode, inst);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	public boolean validateOpcode(int opcode) {
		//opcode를 16진수로 변환
		String opcodeString = Integer.toHexString(opcode);
		int opcodeHex = Integer.parseInt(opcodeString, 16);
		return opcodeTable.containsKey(opcodeHex);
	}

	public void setInstruction(int opcode) {
		Instruction inst = opcodeTable.get(opcode);
		currentInstruction = inst;
	}

	public int getInstructionFormat(int opcode) {
		Instruction inst = opcodeTable.get(opcode);
		return inst.format;
	}

	public void execute(int opcode, int format, byte[] data) {

		setInstruction(opcode);

		resourceManager.currentOperator = currentInstruction.name;
		resourceManager.targetAddress = Integer.toHexString(fetchOperandAddress(data, format));

		//data 16진수로 이어서 출력
		for(int i = 0; i < data.length; i++) {
			resourceManager.log += String.format("%02X", data[i]);
		}
		int nixbpe = fetchNixbpe(data);

		boolean isImmediate = (nixbpe & 0x30) >> 4 == 1;

		boolean isExtended = (nixbpe & 0x01) == 1;
		if (format == 2) {
			executeFormat2(data);
		} else {
			int targetAddress;
			if (isExtended) {
				resourceManager.setRegister(8, resourceManager.getRegister(8) + 4);
				targetAddress = fetchOperandAddress(data, 4);
			} else {
				resourceManager.setRegister(8, resourceManager.getRegister(8) + 3);
				targetAddress = fetchOperandAddress(data, 3);
			}
			executeFormat3or4(targetAddress, format, isImmediate);
		}
	}

	private void executeFormat2(byte[] data) {
		// Implement format 2 instruction execution logic here
		String name = currentInstruction.name;
		int r1 = (data[1] & 0xF0) >> 4;
		int r2 = data[1] & 0x0F;


		switch (name) {
			case "CLEAR":
			resourceManager.setRegister(8, resourceManager.getRegister(8) + 2);
				resourceManager.setRegister(r1, 0);
				break;
			case "TIXR":
			resourceManager.setRegister(8, resourceManager.getRegister(8) + 2);
				resourceManager.setRegister(1, resourceManager.getRegister(1) + 1);
				resourceManager.setRegister(9, resourceManager.getRegister(1) - resourceManager.getRegister(r1));
				break;
			case "COMPR":
			resourceManager.setRegister(8, resourceManager.getRegister(8) + 2);
				resourceManager.setRegister(9, resourceManager.getRegister(r1) - resourceManager.getRegister(r2));
				break;
			default:
				break;
		}
	}

	private void executeFormat3or4(int data, int format, boolean isImmediate) {

		String name = currentInstruction.name;

		int numOperands = currentInstruction.numOperands;


		switch (name) {
			case "LDA":
				LDA(data, isImmediate);
				break;
			case "LDT":
				LDT(data);
				break;
			case "LDCH":
				LDCH(data, (numOperands == 2));
				break;
			case "STCH":
				STCH(data, (numOperands == 2));
				break;
			case "STX":
				STX(data);
				break;
			case "STA":
				STA(data);
				break;
			case "STL":
				STL(data);
				break;
			case "COMP":
				COMP(data, isImmediate);
				break;
			case "JEQ":
				JEQ(data);
				break;
			case "JLT":
				JLT(data);
				break;
			case "JSUB":
				JSUB(data);
				break;
			case "J":
				J(data, format);
				break;
			case "RSUB":
				RSUB();
				break;
			case "TD":
				TD(data);
				break;
			case "RD":
				RD();
				break;
			case "WD":
				WD();
				break;
			default:
				break;
		}
	}

	private void LDA(int data, boolean isImmediate) {
        if (isImmediate)
            resourceManager.setRegister(0, data);
        else
            resourceManager.setRegister(0, resourceManager.byteToInt(resourceManager.getMemory(data, 3)));
    }

    private void LDT(int data) {
		resourceManager.setRegister(5, resourceManager.byteToInt(resourceManager.getMemory(data, 3)));
    }

    private void LDCH(int data, Boolean useIndex) {
        int index = 0;
        if (useIndex)
            index = resourceManager.getRegister(1);
        resourceManager.setRegister(0, resourceManager.getMemory(data + index, 1)[0]);
    }

    private void STCH(int data, Boolean useIndex) {
        int index = 0;
        if (useIndex)
            index = resourceManager.getRegister(1);
        byte[] store = new byte[1];
        store[0] = (byte) resourceManager.registers[0];
        resourceManager.setMemory(data + index, store, 1);
    }

    private void STX(int data) {
        byte[] bytedata = resourceManager.intToByte(resourceManager.registers[1]);
        resourceManager.setMemory(data, bytedata, 3);
    }

    private void STA(int data) {
        resourceManager.setMemory(data, resourceManager.intToByte(resourceManager.registers[0]), 3);
    }

    private void STL(int data) {
		resourceManager.setMemory(data, resourceManager.intToByte(resourceManager.registers[5]), 3);
    }

    private void COMP(int data, boolean isImmediate) {
		if (isImmediate)
			resourceManager.setRegister(9, resourceManager.getRegister(0) - data);
		else
			resourceManager.setRegister(9, resourceManager.getRegister(0) - resourceManager.byteToInt(resourceManager.getMemory(data, 3)));
    }

    private void JEQ(int data) {
        if (0 == resourceManager.registers[9])
            resourceManager.setRegister(8, data);
		else 
			resourceManager.setRegister(8, resourceManager.getRegister(8));
    }

    private void JLT(int data) {
        if (resourceManager.registers[9] < 0)
            resourceManager.setRegister(8, data);
		else 
			resourceManager.setRegister(8, resourceManager.getRegister(8));
    }

    private void JSUB(int data) {
        resourceManager.setRegister(2, resourceManager.getRegister(8));
        resourceManager.setRegister(8, data);
    }

    private void J(int data, int addressingMode) { //unstable
        if (addressingMode == 2)
            resourceManager.setRegister(8, resourceManager.getMemory(data, 1)[0]);
        else
            resourceManager.setRegister(8, data);
    }

    private void RSUB() {
        resourceManager.setRegister(8, resourceManager.getRegister(2));
    }

    private void TD(int data) {
        resourceManager.TD(String.format("%02X", resourceManager.memory[data]));
    }

    private void RD() {
        resourceManager.setRegister(0, resourceManager.RD());
    }

    private void WD() {
        resourceManager.WD();
    }

	private int fetchOperandAddress(byte[] data, int format) {
		int address = 0;
		boolean n = (data[0] & 0x02) != 0;
		boolean i = (data[0] & 0x01) != 0;
		boolean x = (data[1] & 0x80) != 0;
		boolean b = (data[1] & 0x40) != 0;
		boolean p = (data[1] & 0x20) != 0;
		boolean e = (data[1] & 0x10) != 0;
	
		if (format == 3) {
			int disp = ((data[1] & 0x0F) << 8) | (data[2] & 0xFF);
			if (p) { // PC relative addressing
				address = resourceManager.getRegister(8) + ((disp & 0x800) != 0 ? disp | 0xFFFFF000 : disp);
			} else if (b) { // Base relative addressing
				address = resourceManager.getRegister(3) + disp;
			} else { // Direct addressing
				address = disp;
			}
			if (x) { // Indexed addressing
				address += resourceManager.getRegister(1);
			}
			if (n && !i) { // Indirect addressing
				address = resourceManager.byteToInt(resourceManager.getMemory(address, 3));
			}
		} else if (format == 4) {
			address = ((data[1] & 0x0F) << 16) | ((data[2] & 0xFF) << 8) | (data[3] & 0xFF);
			if (x) { // Indexed addressing
				address += resourceManager.getRegister(1);
			}
			if (n && !i) { // Indirect addressing
				address = resourceManager.byteToInt(resourceManager.getMemory(address, 3));
			}
		}
		
		return address;
	}
	
	public byte fetchNixbpe(byte[] data) {
		if (data.length < 2) {
			throw new IllegalArgumentException("Data array is too short to contain nixbpe bits.");
		}
		
		byte firstByte = data[0];
		byte secondByte = data[1];
		
		// Extract the last 2 bits from the first byte and the first 4 bits from the second byte
		byte nixbpe = (byte) (((firstByte & 0x03) << 4) | ((secondByte & 0xF0) >> 4));
		
		return nixbpe;
	}

}
