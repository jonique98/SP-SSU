import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * symbol과 관련된 데이터와 연산을 소유한다. section 별로 하나씩 인스턴스를 할당한다.
 */
public class SymbolTable {
	private HashMap<String, Integer> list;
	private String programName;
	private int startAddress;
	
	/**
	 * initialize symbol table
	 * 
	 * @param programNameSymbol : program name
	 * @param programStartAddress : start address of program
	 */
	public SymbolTable(String programNameSymbol, int programStartAddress) {
		list = new HashMap<String, Integer>();
		list.put(programNameSymbol, programStartAddress);
		programName = programNameSymbol;
		startAddress = programStartAddress;
	}
	
	/**
	 * @return start address
	 */
	public int getStartAddress() {
		return startAddress;
	}
	
	/**
	 * @return program name
	 */
	public String getProgramName() {
		return programName;
	}

	/**
	 * put new symbol.
	 * @param symbol : new symbol label
	 * @param address : symbol address
	 * @throws FileNotFoundException : duplicate symbol declaration
	 */
	public void putSymbol(String symbol, int address) throws FileNotFoundException {
		if (list.containsKey(symbol))
			throw new FileNotFoundException();
		list.put(symbol, address);
	}

	/**
	 * 기존에 존재하는 symbol 값에 대해서 가리키는 주소값을 변경한다.
	 * 
	 * @param symbol     : 변경을 원하는 symbol의 label
	 * @param newaddress : 새로 바꾸고자 하는 주소값
	 */
	public void modifySymbol(String symbol, int newaddress) {

	}
	
	public int getAddress(String symbol) throws FileNotFoundException {
		if (list.containsKey(symbol) == false)
			throw new FileNotFoundException();
		return list.get(symbol);
	}

	/**
	 * returns whether the symbol exists or not
	 * @param symbol : symbol to search
	 * @return true if symbol exists
	 */
	public boolean contain(String symbol) {
		return list.containsKey(symbol);
	}

	public ArrayList<String> getSymbolStrings() {
		ArrayList<String> nameList = new ArrayList<String>(list.keySet());
		ArrayList<Integer> addressList = new ArrayList<Integer>(list.values());
		
		ArrayList<String> result = new ArrayList<String>();
		for (int i = 0; i < nameList.size(); ++i) {
			String name = nameList.get(i);
			int address = addressList.get(i);
			result.add(String.format("%06X%-6s - %06X", address, name, address));
		}
		Collections.sort(result);
		for (int i = 0; i < nameList.size(); ++i) {
			String to = result.get(i).substring(6);
			result.set(i, to);
		}
		return result;
	}
}
