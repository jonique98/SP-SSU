import java.io.*;
import java.util.*;

public class SicXeSimulator {
    ResourceManager resourceManager;
	InstructionExecutor instExecutor;

    public SicXeSimulator(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
		instExecutor = new InstructionExecutor(resourceManager);
    }

	public void oneStep() {
		int pc = resourceManager.getRegister(8); // Assuming PC is register 8

		byte instruction = resourceManager.getMemory(pc, 1)[0]; // Get the instruction byte from memory
		int opcode = instruction & 0xFC; // Extract the first 6 bits without shifting

		if (instExecutor.validateOpcode(opcode)) {
			int formatTemp = instExecutor.getInstructionFormat(opcode);

			if (formatTemp == 2) {
				instExecutor.execute(opcode, formatTemp, resourceManager.getMemory(pc, formatTemp));
				return ;
			}

			int nixbpe = instExecutor.fetchNixbpe(resourceManager.getMemory(pc, 2));
			boolean format4 = (nixbpe & 0x01) != 0;
			// Execute the instruction
			int format = format4 ? 4 : 3;
			instExecutor.execute(opcode, format, resourceManager.getMemory(pc, format));

		} else {
			resourceManager.setRegister(8, pc + 1); // Increment the PC by 1
		}
		// Continue with your instruction execution logic
	}
	
}