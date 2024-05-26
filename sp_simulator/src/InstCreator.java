import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InstCreator {
	private HashMap<Integer, InstType> insts;
	private ResourceManager rManager;

	public InstCreator(File instructionFile) throws BadInstructionFileException {
		insts = new HashMap<Integer, InstType>();
		rManager = ResourceManager.getInstance();
		parse(instructionFile);
	}
	
	public int getOpcodeFormat(int opcode) throws BadInstructionException{
		if (!insts.containsKey(opcode))
			throw new BadInstructionException();
		return insts.get(opcode).getFormat();
	}
	
	public String getOpcodeName(int opcode) throws BadInstructionException {
		if (insts.containsKey(opcode) == false)
			throw new BadInstructionException();
		return insts.get(opcode).getName();
	}

	public Instruction create(int pc) throws BadInstructionException {
		byte[] data = rManager.getMemory(pc, 1);
		int opcode = (int) data[0] & 0xFC;
		if (insts.containsKey(opcode) == false)
			throw new BadInstructionException();
		InstType inst = insts.get(opcode);
		int format = inst.getFormat();

		if (format == 1) {
			return new Instruction1(inst, data);
		} else if (format == 2) {
			return new Instruction2(inst, rManager.getMemory(pc, 2));
		} else {
			data = rManager.getMemory(pc, 2);
			int eFlag = data[1] & 0b00010000;
			if (eFlag == 0)
				return new Instruction3(inst, rManager.getMemory(pc, 3));
			else
				return new Instruction4(inst, rManager.getMemory(pc, 4));
		}
	}

	private void parse(File instructionFile) throws BadInstructionFileException {
		try {
			BufferedReader br = new BufferedReader(new FileReader(instructionFile));

			String line;
			while ((line = br.readLine()) != null) {
				InstType inst = new InstType(line);
				insts.put(inst.getOpcode(), inst);
			}
			br.close();
		} catch (IOException e) {
			throw new BadInstructionFileException();
		}
	}
}

abstract class Instruction {
	protected String name;
	protected int opcode;
	protected int nixbpe;
	protected int size;

	protected Instruction(InstType inst, byte[] data) {
		name = inst.getName();
		opcode = inst.getOpcode();
		if (inst.getFormat() == 3) {
			nixbpe = ((data[0] & 0b11) << 4) + ((data[1] & 0b11110000) >> 4);
		} else
			nixbpe = 0;
	}
	
	public String getName() {
		return name;
	}

	public int getOpcode() {
		return opcode;
	}

	public boolean getNFlag() {
		return (nixbpe & 0b100000) > 0;
	}
	
	public boolean getIFlag() {
		return (nixbpe & 0b010000) > 0;
	}
	
	public boolean getEFlag() {
		return (nixbpe & 0b000001) > 0;
	}
	
	public int getSize() {
		return size;
	}
}

class Instruction1 extends Instruction {
	public Instruction1(InstType inst, byte[] data) {
		super(inst, data);
		size = 1;
	}
}

class Instruction2 extends Instruction {
	private int o1;
	private int o2;
	public Instruction2(InstType inst, byte[] data) {
		super(inst, data);
		o1 = (data[1] & 0xF0) >> 4;
		o2 = data[1] & 0x0F;
		size = 2;
	}
	
	public int getOperand1() {
		return o1;
	}
	
	public int getOperand2() {
		return o2;
	}
}

class Instruction3 extends Instruction {
	protected int o;
	public Instruction3(InstType inst, byte[] data) {
		super(inst, data);
		o = ((data[1] & 0x0F) << 8) + (data[2] & 0xFF);
		if ((o & 0x800) > 0)
			o += 0xFFFFF000;
		size = 3;
	}

	public int getLocation() {
		int loc = o;
		if ((nixbpe & 0b001000) > 0)
			loc += ResourceManager.getInstance().getRegister(Register.X);
		if ((nixbpe & 0b000100) > 0)
			loc += ResourceManager.getInstance().getRegister(Register.B);
		if ((nixbpe & 0b000010) > 0)
			loc += ResourceManager.getInstance().getRegister(Register.PC);
		return loc;
	}
}

class Instruction4 extends Instruction3 {
	public Instruction4(InstType inst, byte[] data) {
		super(inst, data);
		o = ((data[1] & 0x0F) << 16) + ((data[2] & 0xFF) << 8) + (data[3] & 0xFF);
		if ((o & 0x80000) > 0)
			o += 0xFFF00000;
		size = 4;
	}
}

class BadInstructionFileException extends Exception {
	private static final long serialVersionUID = -7871727715793537416L;

	public BadInstructionFileException() {
	}
}

class BadInstructionException extends Exception {
	private static final long serialVersionUID = -6262766643541786606L;

	public BadInstructionException() {
	}
}

class InstType {
	enum OperandType {
		None, M, R, RR, RN, N
	}

	private int opcode;
	private String name;
	private OperandType operand;
	private int format;

	public InstType(String line) throws BadInstructionFileException {
		Pattern pattern = Pattern.compile("([A-Z]+)\t(-|M|R|RR|RN|N)\t([1-3])\t([0-9A-F]{2})");
		Matcher matcher = pattern.matcher(line);
		if (matcher.find()) {
			try {
				name = matcher.group(1);
				String opd = matcher.group(2);
				if ("-".equals(opd))
					operand = OperandType.None;
				else if ("M".equals(opd))
					operand = OperandType.M;
				else if ("R".equals(opd))
					operand = OperandType.R;
				else if ("RR".equals(opd))
					operand = OperandType.RR;
				else if ("RN".equals(opd))
					operand = OperandType.RN;
				else if ("N".equals(opd))
					operand = OperandType.N;
				else
					throw new BadInstructionFileException();
				format = Integer.parseInt(matcher.group(3));
				if (format < 1 || 3 < format)
					throw new BadInstructionFileException();
				opcode = Integer.parseInt(matcher.group(4), 16);
			} catch (NumberFormatException e) {
				throw new BadInstructionFileException();
			}
		} else {
			throw new BadInstructionFileException();
		}
	}

	public int getOpcode() {
		return opcode;
	}

	public int getFormat() {
		return format;
	}

	public String getName() {
		return name;
	}

	public OperandType getOperand() {
		return operand;
	}
}