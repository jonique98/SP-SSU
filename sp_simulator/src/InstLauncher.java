public class InstLauncher {
    private ResourceManager rManager;

    public InstLauncher() {
        rManager = ResourceManager.getInstance();
    }
    
    public void launch(Instruction inst) throws BadInstructionException {
    	String name = inst.getName();
    	if ("ADD".equals(name))
    		add(inst);
    	else if ("ADDR".equals(name))
    		addr(inst);
    	else if ("AND".equals(name))
    		and(inst);
    	else if ("CLEAR".equals(name))
    		clear(inst);
    	else if ("COMP".equals(name))
    		comp(inst);
    	else if ("COMPR".equals(name))
    		compr(inst);
    	else if ("DIV".equals(name))
    		div(inst);
    	else if ("DIVR".equals(name))
    		divr(inst);
    	else if ("J".equals(name))
    		j(inst);
    	else if ("JEQ".equals(name))
    		jeq(inst);
    	else if ("JGT".equals(name))
    		jgt(inst);
    	else if ("JLT".equals(name))
    		jlt(inst);
    	else if ("JSUB".equals(name))
    		jsub(inst);
    	else if ("LDA".equals(name))
    		ld(Register.A, inst);
    	else if ("LDB".equals(name))
    		ld(Register.B, inst);
    	else if ("LDCH".equals(name))
    		ldch(inst);
    	else if ("LDL".equals(name))
    		ld(Register.L, inst);
    	else if ("LDS".equals(name))
    		ld(Register.S, inst);
    	else if ("LDT".equals(name))
    		ld(Register.T, inst);
    	else if ("LDX".equals(name))
    		ld(Register.X, inst);
    	else if ("MUL".equals(name))
    		mul(inst);
    	else if ("MULR".equals(name))
    		mulr(inst);
    	else if ("OR".equals(name))
    		or(inst);
    	else if ("RD".equals(name))
    		rd(inst);
    	else if ("RMO".equals(name))
    		rmo(inst);
    	else if ("RSUB".equals(name))
    		rsub(inst);
    	else if ("SHIFTL".equals(name))
    		shiftl(inst);
    	else if ("SHIFTR".equals(name))
    		shiftr(inst);
    	else if ("STA".equals(name))
    		st(Register.A, inst);
    	else if ("STB".equals(name))
    		st(Register.B, inst);
    	else if ("STCH".equals(name))
    		stch(inst);
    	else if ("STL".equals(name))
    		st(Register.L, inst);
    	else if ("STS".equals(name))
    		st(Register.S, inst);
    	else if ("STSW".equals(name))
    		st(Register.SW, inst);
    	else if ("STT".equals(name))
    		st(Register.T, inst);
    	else if ("STX".equals(name))
    		st(Register.X, inst);
    	else if ("SUB".equals(name))
    		sub(inst);
    	else if ("SUBR".equals(name))
    		subr(inst);
    	else if ("TD".equals(name))
    		td(inst);
    	else if ("TIX".equals(name))
    		tix(inst);
    	else if ("TIXR".equals(name))
    		tixr(inst);
    	else if ("WD".equals(name))
    		wd(inst);
    	else
    		throw new BadInstructionException();
    }

    private void add(Instruction instruction) throws BadInstructionException {
    	Instruction3 inst = (Instruction3)instruction;
    	int location = inst.getLocation();
    	int data;
    	if (!inst.getNFlag())
    		data = location;
    	else if (!inst.getIFlag())
    		data = rManager.getWord(rManager.getWord(location));
    	else
    		data = rManager.getWord(location);
    	rManager.setRegister(Register.A, rManager.getRegister(Register.A) + data);
    }
    
    private void addr(Instruction instruction) throws BadInstructionException {
    	Instruction2 inst = (Instruction2)instruction;
    	int r1 = rManager.getRegister(inst.getOperand1());
    	int r2 = rManager.getRegister(inst.getOperand2());
    	rManager.setRegister(inst.getOperand2(), r1 + r2);
    }

    private void and(Instruction instruction) throws BadInstructionException {
    	Instruction3 inst = (Instruction3)instruction;
    	int location = inst.getLocation();
    	int data;
    	if (!inst.getNFlag())
    		data = location;
    	else if (!inst.getIFlag())
    		data = rManager.getWord(rManager.getWord(location));
    	else
    		data = rManager.getWord(location);
    	rManager.setRegister(Register.A, rManager.getRegister(Register.A) & data);
    }
    
    private void clear(Instruction instruction) throws BadInstructionException {
    	Instruction2 inst = (Instruction2)instruction;
    	rManager.setRegister(inst.getOperand1(), 0);
    }
    
    private void comp(Instruction instruction) throws BadInstructionException {
    	Instruction3 inst = (Instruction3)instruction;
    	int location = inst.getLocation();
    	int data;
    	if (!inst.getNFlag())
    		data = location;
    	else if (!inst.getIFlag())
    		data = rManager.getMemory(rManager.getWord(location), 1)[0];
    	else
    		data = rManager.getMemory(location, 1)[0];
    	int a = rManager.getRegister(Register.A);
    	if (a < data)
    		rManager.setConditionCode(0);
    	else if (a == data)
    		rManager.setConditionCode(1);
    	else
    		rManager.setConditionCode(2);
    }
    
    private void compr(Instruction instruction) throws BadInstructionException {
    	Instruction2 inst = (Instruction2)instruction;
    	int r1 = rManager.getRegister(inst.getOperand1());
    	int r2 = rManager.getRegister(inst.getOperand2());
    	if (r1 < r2)
    		rManager.setConditionCode(0);
    	else if (r1 == r2)
    		rManager.setConditionCode(1);
    	else
    		rManager.setConditionCode(2);
    }
    
    private void div(Instruction instruction) throws BadInstructionException {
    	Instruction3 inst = (Instruction3)instruction;
    	int location = inst.getLocation();
    	int data;
    	if (!inst.getNFlag())
    		data = location;
    	else if (!inst.getIFlag())
    		data = rManager.getWord(rManager.getWord(location));
    	else
    		data = rManager.getWord(location);
    	rManager.setRegister(Register.A, rManager.getRegister(Register.A) / data);
    }
    
    private void divr(Instruction instruction) throws BadInstructionException {
    	Instruction2 inst = (Instruction2)instruction;
    	int r1 = rManager.getRegister(inst.getOperand1());
    	int r2 = rManager.getRegister(inst.getOperand2());
    	rManager.setRegister(inst.getOperand2(), r2 / r1);
    }
    
    private void j(Instruction instruction) throws BadInstructionException {
    	Instruction3 inst = (Instruction3)instruction;
    	int location = inst.getLocation();
    	if (!inst.getIFlag())
    		location = rManager.getWord(location);
    	rManager.setRegister(Register.PC, location);
    }
    
    private void jeq(Instruction instruction) throws BadInstructionException {
    	if (rManager.getConditionCode() == 1)
    		j(instruction);
    }
    
    private void jgt(Instruction instruction) throws BadInstructionException{
    	if (rManager.getConditionCode() == 2)
    		j(instruction);
    }
    
    private void jlt(Instruction instruction) throws BadInstructionException {
    	if (rManager.getConditionCode() == 0)
    		j(instruction);
    }
    
    private void jsub(Instruction instruction) throws BadInstructionException {
    	rManager.setRegister(Register.L, rManager.getRegister(Register.PC));
    	
    	Instruction3 inst = (Instruction3)instruction;
    	int location = inst.getLocation();
    	if (!inst.getIFlag())
    		location = rManager.getWord(location);
    	rManager.setRegister(Register.PC, location);
    }
    
    private void ld(Register register, Instruction instruction) throws BadInstructionException {
    	Instruction3 inst = (Instruction3)instruction;
    	int location = inst.getLocation();
    	int data;
    	if (!inst.getNFlag())
    		data = location;
    	else if (!inst.getIFlag())
    		data = rManager.getWord(rManager.getWord(location));
    	else
    		data = rManager.getWord(location);
    	rManager.setRegister(register, data);
    }
    
    private void ldch(Instruction instruction) throws BadInstructionException {
    	Instruction3 inst = (Instruction3)instruction;
    	int location = inst.getLocation();
    	int data;
    	if (!inst.getNFlag())
    		data = location;
    	else if (!inst.getIFlag())
    		data = rManager.getMemory(rManager.getWord(location), 1)[0];
    	else
    		data = rManager.getMemory(location, 1)[0];
    	data = (data & 0xFF) + (rManager.getRegister(Register.A) & 0xFFFF00);
    	rManager.setRegister(Register.A, data);
    }
    
    private void mul(Instruction instruction) throws BadInstructionException {
    	Instruction3 inst = (Instruction3)instruction;
    	int location = inst.getLocation();
    	int data;
    	if (!inst.getNFlag())
    		data = location;
    	else if (!inst.getIFlag())
    		data = rManager.getWord(rManager.getWord(location));
    	else
    		data = rManager.getWord(location);
    	rManager.setRegister(Register.A, rManager.getRegister(Register.A) * data);
    }
    
    private void mulr(Instruction instruction) throws BadInstructionException {
    	Instruction2 inst = (Instruction2)instruction;
    	int r1 = rManager.getRegister(inst.getOperand1());
    	int r2 = rManager.getRegister(inst.getOperand2());
    	rManager.setRegister(inst.getOperand2(), r2 * r1);
    }

    private void or(Instruction instruction) throws BadInstructionException {
    	Instruction3 inst = (Instruction3)instruction;
    	int location = inst.getLocation();
    	int data;
    	if (!inst.getNFlag())
    		data = location;
    	else if (!inst.getIFlag())
    		data = rManager.getWord(rManager.getWord(location));
    	else
    		data = rManager.getWord(location);
    	rManager.setRegister(Register.A, rManager.getRegister(Register.A) | data);
    }
    
    private void rd(Instruction instruction) throws BadInstructionException {
    	Instruction3 inst = (Instruction3)instruction;
    	int location = inst.getLocation();
    	int data;
    	if (!inst.getNFlag())
    		data = location;
    	else if (!inst.getIFlag())
    		data = rManager.getMemory(rManager.getWord(location), 1)[0];
    	else
    		data = rManager.getMemory(location, 1)[0];
    	String deviceNumber = String.format("%02X", data & 0xFF);
    	char ch = rManager.getDevice(deviceNumber).read();
    	int a = rManager.getRegister(Register.A);
    	rManager.setRegister(Register.A, (a & 0xFFFF00) + ((int)ch & 0xFF));
    }
    
    private void rmo(Instruction instruction) throws BadInstructionException {
    	Instruction2 inst = (Instruction2)instruction;
    	int r1 = rManager.getRegister(inst.getOperand1());
    	rManager.setRegister(inst.getOperand2(), r1);
    }
    
    private void rsub(Instruction instruction) {
    	int l = rManager.getRegister(Register.L);
    	rManager.setRegister(Register.PC, l);
    }
    
    private void shiftl(Instruction instruction) throws BadInstructionException {
    	Instruction2 inst = (Instruction2)instruction;
    	int r1 = rManager.getRegister(inst.getOperand1());
    	int n = inst.getOperand2();
    	rManager.setRegister(inst.getOperand1(), r1 << n);
    }
    
    private void shiftr(Instruction instruction) throws BadInstructionException {
    	Instruction2 inst = (Instruction2)instruction;
    	int r1 = rManager.getRegister(inst.getOperand1());
    	int n = inst.getOperand2();
    	rManager.setRegister(inst.getOperand1(), r1 >> n);
    }
    
    private void st(Register register, Instruction instruction) throws BadInstructionException {
    	Instruction3 inst = (Instruction3)instruction;
    	int location = inst.getLocation();
    	if (!inst.getIFlag())
    		location = rManager.getWord(location);
    	rManager.setWord(location, rManager.getRegister(register));
    }
    
    private void stch(Instruction instruction) throws BadInstructionException {
    	Instruction3 inst = (Instruction3)instruction;
    	int location = inst.getLocation();
    	if (!inst.getIFlag())
    		location = rManager.getWord(location);
    	byte[] data = new byte[] {(byte)(rManager.getRegister(Register.A) & 0xFF)};
    	rManager.setMemory(location, data);
    }

    private void sub(Instruction instruction) throws BadInstructionException {
    	Instruction3 inst = (Instruction3)instruction;
    	int location = inst.getLocation();
    	int data;
    	if (!inst.getNFlag())
    		data = location;
    	else if (!inst.getIFlag())
    		data = rManager.getWord(rManager.getWord(location));
    	else
    		data = rManager.getWord(location);
    	rManager.setRegister(Register.A, rManager.getRegister(Register.A) - data);
    }
    
    private void subr(Instruction instruction) throws BadInstructionException {
    	Instruction2 inst = (Instruction2)instruction;
    	int r1 = rManager.getRegister(inst.getOperand1());
    	int r2 = rManager.getRegister(inst.getOperand2());
    	rManager.setRegister(inst.getOperand2(), r1 - r2);
    }
    
    private void td(Instruction instruction) throws BadInstructionException {
    	Instruction3 inst = (Instruction3)instruction;
    	int location = inst.getLocation();
    	int data;
    	if (!inst.getNFlag())
    		data = location;
    	else if (!inst.getIFlag())
    		data = rManager.getMemory(rManager.getWord(location), 1)[0];
    	else
    		data = rManager.getMemory(location, 1)[0];
    	String deviceNumber = String.format("%02X", data & 0xFF);
    	rManager.setConditionCode(rManager.isDeviceAvailable(deviceNumber) ? 0 : 1);
    }
    
    private void tix(Instruction instruction) throws BadInstructionException {
    	Instruction3 inst = (Instruction3)instruction;
    	int location = inst.getLocation();
    	int data;
    	if (!inst.getNFlag())
    		data = location;
    	else if (!inst.getIFlag())
    		data = rManager.getWord(rManager.getWord(location));
    	else
    		data = rManager.getWord(location);
    	int x = rManager.getRegister(Register.X);
    	rManager.setRegister(Register.X, ++x);
    	if (x < data)
    		rManager.setConditionCode(0);
    	else if (x == data)
    		rManager.setConditionCode(1);
    	else
    		rManager.setConditionCode(2);
    }
    
    private void tixr(Instruction instruction) throws BadInstructionException {
    	Instruction2 inst = (Instruction2)instruction;
    	int x = rManager.getRegister(Register.X);
    	rManager.setRegister(Register.X, ++x);
    	int r1 = rManager.getRegister(inst.getOperand1());
    	if (x < r1)
    		rManager.setConditionCode(0);
    	else if (x == r1)
    		rManager.setConditionCode(1);
    	else
    		rManager.setConditionCode(2);
    }
    
    private void wd(Instruction instruction) throws BadInstructionException {
    	Instruction3 inst = (Instruction3)instruction;
    	int location = inst.getLocation();
    	int data;
    	if (!inst.getNFlag())
    		data = location;
    	else if (!inst.getIFlag())
    		data = rManager.getMemory(rManager.getWord(location), 1)[0];
    	else
    		data = rManager.getMemory(location, 1)[0];
    	String deviceNumber = String.format("%02X", data & 0xFF);
    	int a = rManager.getRegister(Register.A);
    	rManager.getDevice(deviceNumber).write((char)(a & 0xFF));
    }
}
