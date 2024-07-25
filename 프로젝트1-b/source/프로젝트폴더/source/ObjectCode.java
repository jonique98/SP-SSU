import java.util.ArrayList;
import java.util.Optional;

import org.w3c.dom.Text;

public class ObjectCode {
	public ObjectCode() {
		_sectionName = Optional.empty();
		_startAddress = Optional.empty();
		_programLength = Optional.empty();
		_initialPC = Optional.empty();

		_refers = new ArrayList<String>();
		_texts = new ArrayList<Text>();
		_mods = new ArrayList<Modification>();
		_defines = new ArrayList<Define>();
	}

	public void setSectionName(String sectionName) {
		_sectionName = Optional.of(sectionName);
	}

	public void setStartAddress(int address) {
		_startAddress = Optional.of(address);
	}

	public void setProgramLength(int length) {
		_programLength = Optional.of(length);
	}

	public void addDefineSymbol(String symbolName, int address) {
		_defines.add(new Define(symbolName, address));
	}

	public void addReferSymbol(String symbolName) {
		_refers.add(symbolName);
	}

	public void addText(int address, String value) {
		_texts.add(new Text(address, value));
	}

	public void addModification(String symbolNameWithSign, int address, int sizeHalfByte) {
		_mods.add(new Modification(address, sizeHalfByte, symbolNameWithSign));
	}

	public void setInitialPC(int address) {
		_initialPC = Optional.of(address);
	}

	public boolean isInReferList(String symbolName) {
		return _refers.contains(symbolName);
	}

	public boolean isInDefineList(String symbolName) {
		for (Define def : _defines) {
			if (def.symbolName.equals(symbolName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * ObjectCode 객체를 String으로 변환한다. Assembler.java에서 오브젝트 코드를 출력하는 데에 사용된다.
	 */
	@Override
	public String toString() {
		if (_sectionName.isEmpty() || _startAddress.isEmpty() || _programLength.isEmpty())
			throw new RuntimeException("illegal operation");
	
		String sectionName = _sectionName.get();
		int startAddress = _startAddress.get();
		int programLength = _programLength.get();
	
		String header = String.format("H%-6s%06X%06X\n", sectionName, startAddress, programLength);
	
		// Define Record
		StringBuilder define = new StringBuilder();
		if (!_defines.isEmpty()) {
			define.append("D");
			for (Define def : _defines) {
				define.append(String.format("%-6s%06X", def.symbolName, def.address));
			}
			define.append("\n");
		}
	
		// Reference Record
		StringBuilder refer = new StringBuilder();
		if (!_refers.isEmpty()) {
			refer.append("R");
			for (String ref : _refers) {
				refer.append(String.format("%-6s", ref));
			}
			refer.append("\n");
		}
	
		// Text Records
		StringBuilder text = new StringBuilder();
		StringBuilder currentRecord = new StringBuilder();
		int currentRecordStartAddress = 0;
		int currentRecordLength = 0;
	
		for (Text tx : _texts) {
			int txLength = tx.value.length() / 2;
	
			// If adding the current text would exceed the max length, finalize the current text record
			if (currentRecordLength + txLength > 0x1D) {
				text.append(String.format("T%06X%02X%s\n", currentRecordStartAddress, currentRecordLength, currentRecord.toString()));
				currentRecord.setLength(0);
				currentRecordLength = 0;
			}
	
			// If starting a new text record
			if (currentRecordLength == 0) {
				currentRecordStartAddress = tx.address;
			}
	
			currentRecord.append(tx.value);
			currentRecordLength += txLength;
	
			// Finalize the text record if it reaches the maximum length
			if (currentRecordLength == 0x1D) {
				text.append(String.format("T%06X%02X%s\n", currentRecordStartAddress, currentRecordLength, currentRecord.toString()));
				currentRecord.setLength(0);
				currentRecordLength = 0;
			}
		}
	
		// Finalize any remaining text record
		if (currentRecordLength > 0) {
			text.append(String.format("T%06X%02X%s\n", currentRecordStartAddress, currentRecordLength, currentRecord.toString()));
		}
	
		// Modification Records
		StringBuilder modification = new StringBuilder();
		for (Modification mod : _mods) {
			modification.append(String.format("M%06X%02X%s\n", mod.address, mod.sizeHalfByte, mod.symbolNameWithSign));
		}
	
		// End Record
		String end = "E" + _initialPC.map(x -> String.format("%06X", x)).orElse("") + "\n";
		return header + define.toString() + refer.toString() + text.toString() + modification.toString() + end;
	}
	

	class Define {
		Define(String symbolName, int address) {
			this.symbolName = symbolName;
			this.address = address;
		}

		String symbolName;
		int address;
	}

	class Text {
		Text(int address, String value) {
			this.address = address;
			this.value = value;
		}

		int address;
		String value;
	}

	class Modification {
		Modification(int address, int sizeHalfByte, String symbolNameWithSign) {
			this.address = address;
			this.sizeHalfByte = sizeHalfByte;
			this.symbolNameWithSign = symbolNameWithSign;
		}

		int address;
		int sizeHalfByte;
		String symbolNameWithSign;
	}

	// TODO: private field 선언.
	private Optional<String> _sectionName;
	private Optional<Integer> _startAddress;
	private Optional<Integer> _programLength;
	private Optional<Integer> _initialPC;
	private ArrayList<Define> _defines;

	private ArrayList<String> _refers;
	private ArrayList<Text> _texts;
	private ArrayList<Modification> _mods;
}
