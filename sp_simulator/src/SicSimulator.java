import java.io.File;
import java.io.FileNotFoundException;

public class SicSimulator {
	private SicLoader loader;
	private InstCreator creator;
	private InstLauncher launcher;
	private ResourceManager rManager;

	public SicSimulator(File instructionFile) throws BadInstructionFileException {
		loader = new SicLoader();
		creator = new InstCreator(instructionFile);
		launcher = new InstLauncher();
		rManager = ResourceManager.getInstance();
	}

	/**
	 * load object code program
	 * @param program : object code program
	 * @throws FileNotFoundException
	 * @throws OutOfMemoryException 
	 */
	public void load(File program) throws FileNotFoundException, OutOfMemoryException {
		loader.load(program);
	}

	/**
	 * run one command
	 * @throws FinishException : last command executed
	 * @throws BadInstructionException
	 */
	public void oneStep() throws FinishException, BadInstructionException {
		int pc = rManager.getRegister(Register.PC);
		if (pc == 0xFFFFFF)
			throw new FinishException();
		Instruction inst = creator.create(pc);
		rManager.setRegister(Register.PC, pc + inst.getSize());
		launcher.launch(inst);
	}

	/**
	 * run every command
	 * @throws FinishException
	 * @throws BadInstructionException
	 */
	public void allStep() throws FinishException, BadInstructionException {
		for (int i = 0; i < 10000; ++i)
			oneStep();
	}

	/**
	 * returns the position of the command to be executed
	 * @return : new Pair(start position, command size)
	 */
	public Pair currentInstructionPosition() {
		int pc = rManager.getRegister(Register.PC);
		try {
			byte[] data = rManager.getMemory(pc, 1);
			int opcode = data[0] & 0xFC;
			try {
				int format = creator.getOpcodeFormat(opcode);
				if (format <= 2)
					return new Pair(pc, format);
				data = rManager.getMemory(pc + 1, 1);
				boolean eFlag = (data[0] & 0b00010000) > 0;
				return new Pair(pc, format + (eFlag ? 1 : 0));
			} catch (BadInstructionException e) {
				return new Pair(pc, 1);
			}
		} catch (BadInstructionException e) {
			return new Pair(0, 0);
		}
	}

	/**
	 * returns the name of the command to be executed
	 * @return : name of the command if the command is normal, otherwise "-"
	 */
	public String currentInstructionName() {
		try {
			int pc = rManager.getRegister(Register.PC);
			byte[] data = rManager.getMemory(pc, 1);
			int opcode = data[0] & 0xFC;
			return creator.getOpcodeName(opcode);
		} catch (BadInstructionException e) {
			return "-";
		}
	}
}

class FinishException extends Exception {
	private static final long serialVersionUID = -4838545835862758853L;

	public FinishException() {
	}
}