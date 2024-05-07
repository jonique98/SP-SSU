import java.util.HashMap;
import java.util.Optional;

public class SymbolTable {
	/**
	 * 심볼 테이블 객체를 초기화한다.
	 */
	public SymbolTable() {
		symbolMap = new HashMap<String, Symbol>();
	}

	/**
	 * EQU를 제외한 명령어/지시어에 label이 포함되어 있는 경우, 해당 label을 심볼 테이블에 추가한다.
	 * 
	 * @param label   라벨
	 * @param address 심볼의 주소
	 * @throws RuntimeException (TODO: exception 발생 조건을 작성하기)
	 */
	public void putLabel(String label, int address) throws RuntimeException {
		// TODO: EQU를 제외한 명령어/지시어의 label로 생성되는 심볼을 추가하기.
	}

	/**
	 * EQU에 label이 포함되어 있는 경우, 해당 label을 심볼 테이블에 추가한다.
	 * 
	 * @param label    라벨
	 * @param locctr   locctr 값
	 * @param equation equation 문자열
	 * @throws RuntimeException equation 파싱 오류
	 */
	public void putLabel(String label, int locctr, String equation) throws RuntimeException {
		// TODO: EQU의 label로 생성되는 심볼을 추가하기.
	}

	/**
	 * EXTREF에 operand가 포함되어 있는 경우, 해당 operand를 심볼 테이블에 추가한다.
	 * 
	 * @param refer operand에 적힌 하나의 심볼
	 * @throws RuntimeException (TODO: exception 발생 조건을 작성하기)
	 */
	public void putRefer(String refer) throws RuntimeException {
		// TODO: EXTREF의 operand로 생성되는 심볼을 추가하기.
	}

	/**
	 * 심볼 테이블에서 심볼을 찾는다.
	 * 
	 * @param name 찾을 심볼 명칭
	 * @return 심볼. 없을 경우 empty
	 */
	public Optional<Symbol> searchSymbol(String name) {
		// TODO: symbolMap에서 name에 해당하는 심볼을 찾아 반환하기.
		return Optional.empty();
	}

	/**
	 * 심볼 테이블에서 심볼을 찾아, 해당 심볼의 주소를 반환한다.
	 * 
	 * @param symbolName 찾을 심볼 명칭
	 * @return 심볼의 주소. 없을 경우 empty
	 */
	public Optional<Integer> getAddress(String symbolName) {
		Optional<Symbol> optSymbol = searchSymbol(symbolName);
		return optSymbol.map(s -> s.getAddress());
	}

	/**
	 * 심볼 테이블을 String으로 변환한다. Assembler.java에서 심볼 테이블을 출력하기 위해 사용한다.
	 */
	@Override
	public String toString() {
		// TODO: 심볼 테이블을 String으로 표현하기. Symbol 객체의 toString을 활용하자.
		return "<SymbolTable.toString()>";
	}

	/** 심볼 테이블. key: 심볼 명칭, value: 심볼 객체 */
	private HashMap<String, Symbol> symbolMap;
}

class Symbol {
	/**
	 * 심볼 객체를 초기화한다.
	 * 
	 * @param name    심볼 명칭
	 * @param address 심볼의 절대 주소
	 */
	public Symbol(String name, int address /* , 추가로 선언한 field들... */) {
		// TODO: 심볼 객체 초기화.
	}

	/**
	 * 심볼 명칭을 반환한다.
	 * 
	 * @return 심볼 명칭
	 */
	public String getName() {
		return _name;
	}

	/**
	 * 심볼의 주소를 반환한다.
	 * 
	 * @return 심볼 주소
	 */
	public int getAddress() {
		return _address;
	}

	// TODO: 추가로 선언한 field에 대한 getter 작성하기.

	/**
	 * 심볼을 String으로 변환한다.
	 */
	@Override
	public String toString() {
		// TODO: 심볼을 String으로 표현하기.
		return "<Symbol.toString()>";
	}

	/** 심볼의 명칭 */
	private String _name;

	/** 심볼의 주소 */
	private int _address;

	// TODO: 추가로 필요한 field 선언
}