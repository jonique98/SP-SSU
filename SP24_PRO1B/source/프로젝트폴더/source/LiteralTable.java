import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.stream.Collectors;

public class LiteralTable {
	/**
	 * 리터럴 테이블을 초기화한다.
	 */
	public LiteralTable() {
		literalMap = new LinkedHashMap<String, Literal>();
	}

	/**
	 * 리터럴을 리터럴 테이블에 추가한다.
	 * 
	 * @param literal 추가할 리터럴
	 * @throws RuntimeException 비정상적인 리터럴 서식
	 */
	public void putLiteral(String literal) throws RuntimeException {
		//중복되는 리터럴이 없다면 추가함
		if (!literalMap.containsKey(literal)) {
			literalMap.put(literal, new Literal(literal));
		}


	}

	public void setLiteralAddress(String literal, int address) {
		literalMap.get(literal).setAddress(address);
	}
	public int setLiteralAddressAll(int address) {
		//모든 리터럴의 주소를 설정함
		//여기서 address는 현재 locctr값임
		//주소값을 할당하고 리터럴 크기만큼 locctr값을 증가시킨 후 모든 리터럴에 대해 반복함
		// 만약 이미 주소값이 할당되어 있다면 그냥 넘어감

		for (Literal lit : literalMap.values()) {
			if (!lit.getAddress().isPresent()) {
				lit.setAddress(address);
				address += lit.getSize();
			}
		}

		return address;
	}

	public Optional<Literal> searchLiteral(String literal) {
		return Optional.ofNullable(literalMap.get(literal));
	}

	// TODO: 추가로 필요한 method 구현하기.

	/**
	 * 리터럴 테이블을 String으로 변환한다.
	 */
	@Override
	public String toString() {
		return literalMap.values().stream()
				.map(Literal::toString)
				.collect(Collectors.joining("\n"));
	}

	public ArrayList<Literal> getLiteralMap() {
		return new ArrayList<>(literalMap.values());
	}

	/** 리터럴 맵. key: 리터럴 String, value: 리터럴 객체 */
	private HashMap<String, Literal> literalMap;
}

class Literal {
	/**
	 * 리터럴 객체를 초기화한다.
	 * 
	 * @param literal 리터럴 String
	 */
	public Literal(String literal) {
		// TODO: 리터럴 객체 초기화.
		_literal = literal;
		_address = Optional.empty();
	}

	/**
	 * 리터럴 String을 반환한다.
	 * 
	 * @return 리터럴 String
	 */
	public String getLiteral() {
		return _literal;
	}

	public int getSize() {
		//=x'00'과 같은 리터럴의 경우 1바이트를 차지함
		//=c'EOF'와 같은 리터럴의 경우 3바이트를 차지함

		if (_literal.startsWith("=X'")) {
			return 1;
		} else if (_literal.startsWith("=C'")) {
			return _literal.length() - 4;
		} else {
			return 0;
		}
	}

	/**
	 * 리터럴의 주소를 반환한다. 주소가 지정되지 않은 경우, Optional.empty()를 반환한다.
	 * 
	 * @return 리터럴의 주소
	 */
	public Optional<Integer> getAddress() {
		return _address;
	}

	// TODO: 추가로 선언한 field에 대한 getter 작성하기.

	/**
	 * 리터럴을 String으로 변환한다. 리터럴의 address에 관한 정보도 리턴값에 포함되어야 한다.
	 */
	@Override
	public String toString() {
		return _address.map(addr -> String.format("%s 0x%04X", _literal, addr))
					   .orElse(String.format("%s not assigned", _literal));
	}

	public void setAddress(int address) {
		_address = Optional.of(address);
	}

	/** 리터럴 String */
	private String _literal;

	/** 리터럴 주소. 주소가 지정되지 않은 경우 empty */
	private Optional<Integer> _address;

	// TODO: 추가로 필요한 field 선언하기.
}