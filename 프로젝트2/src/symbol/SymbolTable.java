/**
 * @author Enoch Jung (github.com/enochjung)
 * @file SymbolTable.java
 * @date 2024-05-05
 * @version 1.0.0
 *
 * @brief 조교가 구현한 SIC/XE 어셈블러 코드 구조 샘플
 */

package symbol;

import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.stream.Collectors;

import numeric.Numeric;

public class SymbolTable {
	/**
	 * 심볼 테이블 객체를 초기화한다.
	 */
	public SymbolTable() {
		_symbolMap = new LinkedHashMap<String, Symbol>();
		_repSymbol = Optional.empty();
	}

	/**
	 * 주소값이 정해지지 않은 심볼을 추가한다.
	 * 
	 * @param name 심볼 명칭
	 * @return 심볼 객체
	 * @throws RuntimeException 잘못된 심볼 생성 시도
	 */
	public Symbol put(String name) throws RuntimeException {
		// TODO: 예외 처리하기 (exception)

		Symbol symbol = Symbol.createAddressNotAssignedSymbol(name);
		_symbolMap.put(name, symbol);
		return symbol;
	}

	/**
	 * 주소값이 정해진 심볼을 추가한다.
	 * 
	 * @param name    심볼 명칭
	 * @param address 심볼 주소
	 * @return 심볼 객체
	 * @throws RuntimeException 잘못된 심볼 생성 시도
	 */
	public Symbol put(String name, int address) throws RuntimeException {
		// TODO: 심볼 추가하기. 만약 심볼이 이미 존재하고 해당 심볼이 주소가 지정되지 않은 심볼일 경우, 주소값 할당하기.

		Symbol symbol;
		Optional<Symbol> optSymbol = search(name);
		if (optSymbol.isPresent()) {
			// TODO: 해당 심볼이 주소가 지정되지 않은 심볼일 경우 주소값 할당하기.
		} else {
			// TODO: 심볼 추가하기.
		}

		return symbol;
	}

	/**
	 * EQU label에 해당하는 심볼을 추가한다.
	 * 
	 * @param name    심볼 명칭
	 * @param formula 수식 문자열
	 * @param locctr  location counter 값
	 * @return 심볼 객체
	 * @throws RuntimeException 잘못된 심볼 생성 시도 혹은 잘못된 수식 포맷
	 */
	public Symbol put(String name, String formula, int locctr) throws RuntimeException {
		// TODO: 심볼 추가하기. 만약 심볼이 이미 존재하고 해당 심볼이 주소가 지정되지 않은 심볼일 경우, 주소값 할당하기.

		Symbol symbol;
		Numeric addr = new Numeric(formula, this, locctr);
		Optional<Symbol> optSymbol = search(name);

		if (optSymbol.isPresent()) {
			// TODO: 해당 심볼이 주소가 지정되지 않은 심볼일 경우 주소값 할당하기.
		} else {
			// TODO: 심볼 추가하기.
		}

		return symbol;
	}

	/**
	 * control section 명칭에 해당하는 심볼을 추가한다.
	 * 
	 * @param name    심볼 명칭
	 * @param address 심볼 주소
	 * @return 심볼 객체
	 * @throws RuntimeException 잘못된 심볼 생성 시도
	 */
	public Symbol putRep(String name, int address) throws RuntimeException {
		Symbol symbol;

		// TODO: control section 명칭에 해당하는 심볼을 추가하기.
		// with Symbol.createRepSymbol(?, ?);

		return symbol;
	}

	/**
	 * EXTERN operand에 주어지는 외부 심볼을 추가한다.
	 * 
	 * @param name 심볼 명칭
	 * @return 심볼 객체
	 * @throws RuntimeException 잘못된 심볼 생성 시도
	 */
	public Symbol putRefer(String name) throws RuntimeException {
		Symbol symbol;

		// TODO: EXTERN operand에 주어지는 외부 심볼을 추가하기.

		return symbol;
	}

	/**
	 * 심볼 테이블에서 심볼을 찾는다.
	 * 
	 * @param name 찾을 심볼 명칭
	 * @return 심볼. 없을 경우 empty <code>Optional</code>
	 */
	public Optional<Symbol> search(String name) {
		return Optional.ofNullable(_symbolMap.get(name));
	}

	/**
	 * control section 명칭에 해당하는 심볼을 반환한다.
	 * 
	 * @return 심볼. 없을 경우 empty <code>Optional</code>
	 */
	public Optional<Symbol> getRepSymbol() {
		return _repSymbol;
	}

	/**
	 * 심볼 테이블 객체의 정보를 문자열로 반환한다. 심볼 테이블 출력 용도로 사용한다.
	 */
	@Override
	public String toString() {
		String symbols = _symbolMap.entrySet().stream()
				.map(x -> x.getValue().toString())
				.collect(Collectors.joining("\n"));

		return symbols;
	}

	private final LinkedHashMap<String, Symbol> _symbolMap;
	private Optional<Symbol> _repSymbol;
}
