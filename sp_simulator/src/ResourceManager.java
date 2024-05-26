import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

enum Register {
	A, X, L, B, S, T, F, PC, SW
}

public class ResourceManager {
	public final static int MEMORY_SIZE = 1 << 16;
	private final static int REGISTER_NUM = 10;

	private static ResourceManager _instance;
	private byte[] memory;
	private boolean[] isOccupied;
	private int[] register;
	private ArrayList<SymbolTable> symbolTableList;
	private ArrayList<Pair> modification;
	private HashMap<String, Device> devices = new HashMap<String, Device>();

	/**
	 * allocate program
	 * 
	 * @param length : program length
	 * @return start address of program
	 */
	public int allocate(int length) throws OutOfMemoryException {
		for (int i = 0; i <= MEMORY_SIZE - length; ++i) {
			boolean available = true;
			for (int j = i; j < i + length; ++j)
				if (isOccupied[j] == true) {
					available = false;
					break;
				}
			if (available == true) {
				for (int j = i; j < i + length; ++j)
					isOccupied[j] = true;
				return i;
			}
		}
		throw new OutOfMemoryException();
	}

	/**
	 * deallocate all programs
	 */
	public void deallocateAll() {
		isOccupied = new boolean[MEMORY_SIZE];
	}
	
	public int getWord(int address) throws BadInstructionException {
		byte[] data = getMemory(address, 3);
		return ((data[0] & 0xFF) << 16) + ((data[1] & 0xFF) << 8) + (data[2] & 0xFF);
	}
	
	public void setWord(int address, int word) {
		byte[] data = new byte[3];
		data[0] = (byte)(word >> 16);
		data[1] = (byte)((word >> 8) & 0xFF);
		data[2] = (byte)(word & 0xFF);
		setMemory(address, data);
	}

	/**
	 * retrieves the byte of data from a specific location in memory.
	 * @param address : first address in memory from which to get data
	 * @param size : size of data to get
	 * @return data
	 */
	public byte[] getMemory(int address, int size) throws BadInstructionException {
		byte[] ret = new byte[size];
		for (int i = 0; i < size; ++i) {
			if (address + i < 0 || MEMORY_SIZE <= address + i)
				throw new BadInstructionException();
			ret[i] = memory[address + i];
		}
		return ret;
	}

	/**
	 * replace data from a specific address in the memory
	 * @param address : address to start replacing data
	 * @param data : data to be replaced
	 */
	public void setMemory(int address, byte[] data) {
		for (int i = 0; i < data.length; ++i)
			memory[address + i] = data[i];
		modification.add(new Pair(address, data.length));
	}
	
	public ArrayList<Pair> getModification() {
		return new ArrayList<Pair>(modification);
	}
	
	public void clearModification() {
		modification.clear();
	}

	/**
	 * get the value of the register
	 * @param register
	 * @return : value of register
	 */
	public int getRegister(Register register) {
		try {
			return getRegister(registerToNumber(register));
		} catch (BadInstructionException e) {
			return 0;
		}
	}
	
	/**
	 * get the value of the register
	 * @param registerNumber : register number
	 * @return : value of register
	 * @throws FileNotFoundException : bad register number
	 */
	public int getRegister(int registerNumber) throws BadInstructionException {
		if (registerNumber < 0 || (5 < registerNumber && registerNumber < 8) || 9 < registerNumber)
			throw new BadInstructionException();
		return register[registerNumber];
	}
	
	public void setRegister(Register register, int value) {
		try {
			setRegister(registerToNumber(register), value);
		} catch (BadInstructionException e) {
			// nop
		}
	}
	
	// <:0, =:1, >:2
	public int getConditionCode() {
		return (getRegister(Register.SW) >> 17) & 0b11;
	}
	
	public void setConditionCode(int code) {
		setRegister(Register.SW, (code & 0b11) << 17);
	}

	public void setRegister(int registerNumber, int value) throws BadInstructionException {
		if (registerNumber < 0 || (5 < registerNumber && registerNumber < 8) || 9 < registerNumber)
			throw new BadInstructionException();
		register[registerNumber] = value & 0xFFFFFF;
	}
	
	public String getProgramName() {
		return symbolTableList.get(0).getProgramName();
	}
	
	public int getFirstAddress() {
		for (int i = 0; i < MEMORY_SIZE; ++i)
			if (isOccupied[i] == true)
				return i;
		return -1;
	}
	
	public int getLastAddress() {
		for (int i = getFirstAddress(); i < MEMORY_SIZE; ++i)
			if (isOccupied[i] == false)
				return i - 1;
		return -1;
	}
	
	public void addDevice(String name, String data) throws IllegalArgumentException {
		name = name.toUpperCase();
		if (devices.containsKey(name))
			throw new IllegalArgumentException();
		devices.put(name, new Device(name, data));
	}
	
	public ArrayList<Device> getDevices() {
		return new ArrayList<Device>(devices.values());
	}
	
	public Device getDevice(String name) {
		return devices.get(name);
	}
	
	public void removeDevice(String name) {
		devices.remove(name);
	}
	
	public boolean isDeviceAvailable(String name) {
		return devices.containsKey(name);
	}
	
	public void initializeResource() {
		isOccupied = new boolean[MEMORY_SIZE];
		symbolTableList = null;
		modification = new ArrayList<Pair>();
		modification.add(new Pair(0, MEMORY_SIZE));
	}
	
	public ArrayList<SymbolTable> getSymbolTableList() {
		return symbolTableList;
	}
	
	public void setSymbolTableList(ArrayList<SymbolTable> symbolTableList) {
		this.symbolTableList = symbolTableList;
	}
	
	private int registerToNumber(Register name) {
		if (name == Register.A)
			return 0;
		if (name == Register.X)
			return 1;
		if (name == Register.L)
			return 2;
		if (name == Register.B)
			return 3;
		if (name == Register.S)
			return 4;
		if (name == Register.T)
			return 5;
		if (name == Register.F)
			return 6;
		if (name == Register.PC)
			return 8;
		return 9;
	}

	private ResourceManager() {
		memory = new byte[MEMORY_SIZE];
		register = new int[REGISTER_NUM];
		devices = new HashMap<String, Device>();
	}

	public static ResourceManager getInstance() {
		if (_instance == null)
			_instance = new ResourceManager();
		return _instance;
	}
	
}

class Device {
	private String name;
	private StringBuffer data;
	private int cursor;
	
	public Device(String name, String data) throws IllegalArgumentException {
		try {
			if (name.length() != 2)
				throw new IllegalArgumentException();
			Integer.parseInt(name, 16);
			this.name = name;
			this.data = new StringBuffer(data);
			this.cursor = 0;
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException();
		}
	}
	
	public String getName() {
		return name;
	}
	
	public String getData() {
		return data.toString();
	}
	
	public char read() {
		if (data.length() == cursor)
			return 0;
		return data.charAt(cursor++);
	}
	
	public void write(char ch) {
		if (data.length() == cursor)
			data.append(ch);
		else
			data.replace(cursor, cursor + 1, Character.toString(ch));
		++cursor;
	}
}

class OutOfMemoryException extends Exception {
	private static final long serialVersionUID = -8994222651175337463L;

	public OutOfMemoryException() {
	}
}