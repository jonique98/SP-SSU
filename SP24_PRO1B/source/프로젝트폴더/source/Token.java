import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Arrays;

public class Token {
	/**
	 * 소스 코드 한 줄에 해당하는 토큰을 초기화한다.
	 * 
	 * @param input 소스 코드 한 줄에 해당하는 문자열
	 * @throws RuntimeException 잘못된 형식의 소스 코드 파싱 시도.
	 */

	public Token(String input) throws RuntimeException {

		if (input == null || input.isEmpty()) {
			throw new IllegalArgumentException("Input string must not be null or empty.");
		}
		
        String[] parts = input.split("\t", -1);

		// 첫번쨰 요소가 . 이면 전부 주석
		if (parts[0].startsWith(".")) {
			_comment = Optional.of(parts[0]);
			return;
		}

        if (parts[0].isEmpty()) {
            parseOperatorAndOperands(parts, 1);
        } else {
            _label = Optional.of(parts[0]);
            parseOperatorAndOperands(parts, 1);
        }
	}

	private void parseOperatorAndOperands(String[] parts, int start) {
		if (parts.length > start) {
			String operator = parts[start];
	
			// Clear previous flags
			_eBit = false;
			_nBit = true; // Default to true for SIC/XE unless specifically set to false
			_iBit = true; // Default to true for SIC/XE unless specifically set to false
			_pBit = true; // PC-relative by default unless explicitly set to false
			
			if (operator.startsWith("+")) {
				_eBit = true;  // Extended instruction
				_pBit = false;  // Extended instruction is not PC-relative
			}

			_operator = Optional.of(operator);  // Clean operator for storage
		}
	
		if (parts.length > start + 1) {
			_operands = new ArrayList<>(Arrays.asList(parts[start + 1].split(",\\s*")));
			

			if (_operands.size() > 0 && _operands.get(_operands.size() - 1).endsWith("X")) {
				_xBit = true;
				//모든 operand 저장 해야함 
			}

			//operands가 #으로 시작할 때 
			if (_operands.size() > 0 && _operands.get(0).startsWith("#")) {
				_iBit = true;
				_nBit = false;
				_pBit = false;
			}

			//operands가 @으로 시작할 때
			if (_operands.size() > 0 && _operands.get(0).startsWith("@")) {
				_iBit = false;
				_nBit = true;
				_pBit = true;
			}
		}

		if (parts.length > start + 2) {
			_comment = Optional.of(parts[start + 2]);
		}
	}

	public void setSize(int size) {
		_size = size;
	}

	public int getSize() {
		return _size;
	}
	
	/**
	 * label 문자열을 반환한다.
	 * 
	 * @return label 문자열. 없으면 empty <code>Optional</code>.
	 */
	public Optional<String> getLabel() {
		return _label;
	}

	/**
	 * operator 문자열을 반환한다.
	 * 
	 * @return operator 문자열. 없으면 empty <code>Optional</code>.
	 */
	public Optional<String> getOperator() {
		return _operator;
	}

	/**
	 * operand 문자열 배열을 반환한다.
	 * 
	 * @return operand 문자열 배열
	 */
	public ArrayList<String> getOperands() {
		ArrayList<String> operandsSplit = new ArrayList<>();

		return _operands;
	}

	/**
	 * comment 문자열을 반환한다.
	 * 
	 * @return comment 문자열. 없으면 empty <code>Optional</code>.
	 */
	public Optional<String> getComment() {
		return _comment;
	}

	/**
	 * 토큰의 iNdirect bit가 1인지 여부를 반환한다.
	 * 
	 * @return N bit가 1인지 여부
	 */
	public boolean isN() {
		return _nBit;
	}

	/**
	 * 토큰의 Immediate bit가 1인지 여부를 반환한다.
	 * 
	 * @return I bit가 1인지 여부
	 */
	public boolean isI() {
		return _iBit;
	}

	/**
	 * 토큰의 indeX bit가 1인지 여부를 반환한다.
	 * 
	 * @return X bit가 1인지 여부
	 */
	public boolean isX() {
		return _xBit;
	}

	/**
	 * 토큰의 Pc relative bit가 1인지 여부를 반환한다.
	 * 
	 * @return P bit가 1인지 여부
	 */
	public boolean isP() {
		return _pBit;
	}

	/**
	 * 토큰의 Extra bit가 1인지 여부를 반환한다.
	 * 
	 * @return E bit가 1인지 여부
	 */
	public boolean isE() {
		return _eBit;
	}

	public void setAddress(int address) {
		_address = Optional.of(address);
	}

	public Optional<Integer> getAddress() {
		return _address;
	}

	/**
	 * StringToken 객체의 정보를 문자열로 반환한다. 디버그 용도로 사용한다.
	 */
	@Override
	public String toString() {
		String label = _label.map(x -> "<" + x + ">").orElse("(no label)");
		String operator = (isE() ? "+" : "") + _operator.map(x -> "<" + x + ">").orElse("(no operator)");
		String operand = (isN() && !isI() ? "@" : "") + (isI() && !isN() ? "#" : "")
				+ (_operands.isEmpty() ? "(no operand)"
						: "<" + _operands.stream().collect(Collectors.joining("/")) + ">")
				+ (isX() ? (_operands.isEmpty() ? "X" : "/X") : "");
		String comment = _comment.map(x -> "<" + x + ">").orElse("(no comment)");

		String formatted = String.format("%-12s\t%-12s\t%-18s\t%s", label, operator, operand, comment);
		return formatted;
	}

	private Optional<String> _label = Optional.empty();
	private Optional<String> _operator = Optional.empty();
	private ArrayList<String> _operands = new ArrayList<>();
	private Optional<String> _comment = Optional.empty();
	private Optional<Integer> _address = Optional.empty();

	private boolean _nBit = true;
	private boolean _iBit = true;
	private boolean _xBit = false;
	// private boolean _bBit; /** base relative는 구현하지 않음 */
	private boolean _pBit = true;
	private boolean _eBit = false;

	private int _size = 0;
}
