import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SymbolTable {
	/**
	 * 심볼 테이블 객체를 초기화한다.
	 */
	public SymbolTable() {
		_sectionName = Optional.empty();
		_symbolMap = new LinkedHashMap<String, Symbol>();
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

		Symbol symbol = new Symbol(label, address);
		_symbolMap.put(label, symbol);
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
        if (equation.equals("*")) {
            putLabel(label, locctr);
            return;
        }

        try {
            int result = evaluateExpression(equation);
            _symbolMap.put(label, new Symbol(label, result));
        } catch (Exception e) {
            throw new RuntimeException("Failed to evaluate expression: " + equation, e);
        }
    }

    public Integer evaluateExpression(String equation) throws RuntimeException {
        // 정규표현식을 이용하여 피연산자와 연산자를 분리합니다.
        Matcher m = Pattern.compile("([a-zA-Z]+)|([0-9]+)|([+\\-*/])").matcher(equation);
        int result = 0;
        String lastOperator = "+"; // 초기 연산자는 '+'로 설정하여 첫 번째 값을 그대로 사용합니다.

        while (m.find()) {
            String token = m.group();
            if (token.matches("[+\\-*/]")) { // 연산자인 경우
                lastOperator = token;
            } else {
                int value;
                if (Character.isDigit(token.charAt(0))) {
                    value = Integer.parseInt(token); // 숫자인 경우
                } else {
                    Optional<Integer> optValue = getAddress(token);
                    if (optValue.isEmpty()) {
                        throw new RuntimeException("Failed to find symbol: " + token);
                    }
                    value = optValue.get(); // 라벨의 주소값을 가져옵니다.
                }

                switch (lastOperator) {
                    case "+": result += value; break;
                    case "-": result -= value; break;
                    case "*": result *= value; break;
                    case "/": result /= value; break;
                }
            }
        }
        return result;
    }

	/**
	 * EXTREF에 operand가 포함되어 있는 경우, 해당 operand를 심볼 테이블에 추가한다.
	 * 
	 * @param refer operand에 적힌 하나의 심볼
	 * @throws RuntimeException (TODO: exception 발생 조건을 작성하기)
	 */
	public void putRefer(String refer) throws RuntimeException {
		// TODO: EXTREF의 operand로 생성되는 심볼을 추가하기.
		// EXTREF의 operand는 주소를 가지지 않으므로, 주소값을 출력할 때 REF라고 저장해야함
		// 순서는 맨 마지막에 출력돼야함
		Symbol symbol = new Symbol(refer, -1);
		_symbolMap.put(refer, symbol);
	}

	/**
	 * 심볼 테이블에서 심볼을 찾는다.
	 * 
	 * @param name 찾을 심볼 명칭
	 * @return 심볼. 없을 경우 empty
	 */
	public Optional<Symbol> searchSymbol(String name) {


		return Optional.empty();
	}

	/**
	 * 심볼 테이블에서 심볼을 찾아, 해당 심볼의 주소를 반환한다.
	 * 
	 * @param symbolName 찾을 심볼 명칭
	 * @return 심볼의 주소. 없을 경우 empty
	 */
	public Optional<Integer> getAddress(String symbolName) {

		if (_symbolMap.containsKey(symbolName)) {
			return Optional.of(_symbolMap.get(symbolName).getAddress());
		} else {
			return Optional.empty();
		}
	}

	public void setSectionName(Optional<String> sectionName) {
		_sectionName = sectionName;
	}

	public String getSectionName() {
		return _sectionName.orElse("");
	}

	/**
	 * 심볼 테이블을 String으로 변환한다. Assembler.java에서 심볼 테이블을 출력하기 위해 사용한다.
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		List<String> refList = new ArrayList<>();
		String lastSectionLabel = null; // 마지막으로 사용된 섹션 라벨을 추적합니다.
		boolean isFirst = true; // 첫 번째 요소를 추적하는 플래그
	
		for (Map.Entry<String, Symbol> entry : _symbolMap.entrySet()) {
			Symbol symbol = entry.getValue();
			if (symbol.getAddress() == -1) {
				// REF로 표시된 항목들은 리스트에 추가하여 나중에 처리합니다.
				refList.add(String.format("%-8s REF\n", entry.getKey()));
			} else {
				// 첫 번째 심볼 출력 시 섹션 라벨을 출력하지 않습니다.
				if (isFirst) {
					sb.append(String.format("%-8s 0x%04X\n", entry.getKey(), symbol.getAddress()));
					isFirst = false; // 첫 번째 요소를 처리한 후 플래그를 false로 설정합니다.
				} else {
					// 현재 심볼의 섹션 라벨이 이전과 다르면 업데이트합니다.
					if (!this.getSectionName().equals(lastSectionLabel)) {
						lastSectionLabel = this.getSectionName(); // 섹션 라벨을 업데이트합니다.
					}
					// 섹션 라벨이 있는 경우에만 섹션 라벨을 추가합니다.
					if (lastSectionLabel.isEmpty()) {
						sb.append(String.format("%-8s 0x%04X\n", entry.getKey(), symbol.getAddress()));
					} else {
						sb.append(String.format("%-8s 0x%04X    %s\n", entry.getKey(), symbol.getAddress(), lastSectionLabel));
					}
				}
			}
		}
	
		// REF로 표시된 항목들을 마지막에 추가합니다.
		for (String ref : refList) {
			sb.append(ref);
		}
	
		return sb.toString();
	}



	/** 심볼 테이블. key: 심볼 명칭, value: 심볼 객체 */
	private HashMap<String, Symbol> _symbolMap;
	private Optional<String> _sectionName;
}

class Symbol {
	/**
	 * 심볼 객체를 초기화한다.
	 * 
	 * @param name    심볼 명칭
	 * @param address 심볼의 절대 주소
	 */
	public Symbol(String name, int address /* , 추가로 선언한 field들... */) {

		_name = name;
		_address = address;
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
		//address는 16진수
		return String.format("%s\t%s", _name, Integer.toHexString(_address));
	}

	/** 심볼의 명칭 */
	private String _name;

	/** 심볼의 주소 */
	private int _address;

	// TODO: 추가로 필요한 field 선언
}