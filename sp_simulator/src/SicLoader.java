import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * SicLoader는 프로그램을 해석해서 메모리에 올리는 역할을 수행한다. 이 과정에서 linker의 역할 또한 수행한다.
 * 
 * SicLoader가 수행하는 일을 예를 들면 다음과 같다. - program code를 메모리에 적재시키기 - 주어진 공간만큼 메모리에 빈
 * 공간 할당하기 - 과정에서 발생하는 symbol, 프로그램 시작주소, control section 등 실행을 위한 정보 생성 및 관리
 */
public class SicLoader {
	private ResourceManager rManager;

	public SicLoader() {
		rManager = ResourceManager.getInstance();
	}

	/**
	 * object code를 읽어서 load과정을 수행한다. load한 데이터는 resourceManager가 관리하는 메모리에 올라가도록
	 * 한다. load과정에서 만들어진 symbol table 등 자료구조 역시 resourceManager에 전달한다.
	 * 
	 * @param objectCode 읽어들인 파일
	 */
	public void load(File objectCode) throws FileNotFoundException, OutOfMemoryException {
		ArrayList<SymbolTable> tableList = new ArrayList<SymbolTable>();
		
		ArrayList<ArrayList<String>> linesList = parse(objectCode);

		for (ArrayList<String> lines : linesList) {
			SymbolTable table = null;
			for (String line : lines) {
				if (line.charAt(0) == 'H') {
					table = headerRecord(line);
					tableList.add(table);
				} else if (line.charAt(0) == 'D')
					defineRecord(line, table);
				else if (line.charAt(0) == 'R')
					continue;
				else if (line.charAt(0) == 'T')
					textRecord(line, table.getStartAddress());
				else if (line.charAt(0) == 'M')
					continue;
				else if (line.charAt(0) == 'E')
					endRecord(line);
				else
					throw new FileNotFoundException();
			}
		}

		for (int i = 0; i < linesList.size(); ++i) {
			ArrayList<String> lines = linesList.get(i);
			HashMap<String, Integer> external = new HashMap<String, Integer>();
			SymbolTable table = tableList.get(i);
			for (String line : lines) {
				if (line.charAt(0) == 'H')
					continue;
				else if (line.charAt(0) == 'D')
					continue;
				else if (line.charAt(0) == 'R')
					referRecord(line, external, tableList);
				else if (line.charAt(0) == 'T')
					continue;
				else if (line.charAt(0) == 'M')
					modifyRecord(line, external, table.getStartAddress());
				else if (line.charAt(0) == 'E')
					continue;
			}
		}
		
		rManager.setSymbolTableList(tableList);
	}

	private ArrayList<ArrayList<String>> parse(File objectCode) throws FileNotFoundException {
		try {
			BufferedReader br = new BufferedReader(new FileReader(objectCode));
			ArrayList<ArrayList<String>> linesList = new ArrayList<ArrayList<String>>();

			String line;
			ArrayList<String> lines = null;
			while ((line = br.readLine()) != null) {
				if (line.length() == 0)
					continue;
				if (line.charAt(0) == 'H')
					lines = new ArrayList<String>();
				lines.add(line);
				if (line.charAt(0) == 'E') {
					linesList.add(lines);
					lines = null;
				}
			}
			br.close();
			return linesList;
		} catch (IOException e) {
			throw new FileNotFoundException();
		}
	}

	private SymbolTable headerRecord(String line) throws FileNotFoundException, OutOfMemoryException {
		try {
			int startAddress = Integer.parseInt(line.substring(7, 13), 16); // no use
			int programLength = Integer.parseInt(line.substring(13, 19), 16);
			startAddress = rManager.allocate(programLength);
			SymbolTable table = new SymbolTable(line.substring(1, 7).trim(), startAddress);
			return table;
		} catch (NumberFormatException e) {
			throw new FileNotFoundException();
		} catch (StringIndexOutOfBoundsException e) {
			throw new FileNotFoundException();
		}
	}

	private void defineRecord(String line, SymbolTable table) throws FileNotFoundException {
		try {
			int startAddress = table.getStartAddress();
			line = line.substring(1);
			while (!line.isEmpty()) {
				String label = line.substring(0, 6).trim();
				int address = Integer.parseInt(line.substring(6, 12), 16) + startAddress;
				table.putSymbol(label, address);
				line = line.substring(12);
			}
		} catch (NullPointerException e) {
			throw new FileNotFoundException();
		} catch (StringIndexOutOfBoundsException e) {
			throw new FileNotFoundException();
		}
	}

	private void textRecord(String line, int startAddress) throws FileNotFoundException {
		try {
			startAddress += Integer.parseInt(line.substring(1, 7), 16);
			int length = Integer.parseInt(line.substring(7, 9), 16);
			byte[] data = stringToByte(line.substring(9, length * 2 + 9));
			rManager.setMemory(startAddress, data);
		} catch (NumberFormatException e) {
			throw new FileNotFoundException();
		} catch (StringIndexOutOfBoundsException e) {
			throw new FileNotFoundException();
		}
	}

	private void endRecord(String line) throws FileNotFoundException {
		try {
			if (line.length() == 1)
				return;
			int startAddress = Integer.parseInt(line.substring(1, 7), 16);
			rManager.setRegister(Register.PC, startAddress);
			rManager.setRegister(Register.L, 0xFFFFFF);
		} catch (NumberFormatException e) {
			throw new FileNotFoundException();
		} catch (StringIndexOutOfBoundsException e) {
			throw new FileNotFoundException();
		}
	}

	private void referRecord(String line, HashMap<String, Integer> external, ArrayList<SymbolTable> tableList)
			throws FileNotFoundException {
		try {
			line = line.substring(1);
			while (line.isEmpty() == false) {
				String label = line.substring(0, 6).trim();
				for (SymbolTable table : tableList)
					if (table.contain(label)) {
						if (external.containsKey(label))
							throw new FileNotFoundException();
						external.put(label, table.getAddress(label));
					}
				line = line.substring(6);
			}
		} catch (StringIndexOutOfBoundsException e) {
			throw new FileNotFoundException();
		}
	}

	private void modifyRecord(String line, HashMap<String, Integer> external, int startAddress) throws FileNotFoundException {
		try {
			int address = startAddress + Integer.parseInt(line.substring(1, 7), 16);
			int size = Integer.parseInt(line.substring(7, 9), 16);
			char sign = line.charAt(9);
			String label = line.substring(10, 16).trim(); 
			if (external.containsKey(label) == false)
				throw new FileNotFoundException();
			byte[] data = rManager.getMemory(address, (size + 1) / 2);
			int value = 0;
			for (int i = 0; i < data.length; ++i) {
				value <<= 8;
				value += data[i];
			}
			int newValue = value;
			newValue += external.get(label) * (sign == '+' ? 1 : -1);
			value -= value & ((1 << (size * 4)) - 1);
			value += newValue & ((1 << (size * 4)) - 1);
			for (int i = data.length - 1; i >= 0; --i) {
				data[i] = (byte)(value & 0xFF);
				value >>= 8;
			}
			rManager.setMemory(address, data);
		} catch (StringIndexOutOfBoundsException e) {
			throw new FileNotFoundException();
		} catch (NumberFormatException e) {
			throw new FileNotFoundException();
		} catch (BadInstructionException e) {
			e.printStackTrace();
		}
	}
	
	private byte[] stringToByte(String data) throws FileNotFoundException {
		try {
			byte[] ret = new byte[data.length() / 2];
			for (int i = 0; i < ret.length; ++i)
				ret[i] = (byte) Integer.parseInt(data.substring(i * 2, i * 2 + 2), 16);
			return ret;
		} catch (NumberFormatException e) {
			throw new FileNotFoundException();
		}
	}
}
