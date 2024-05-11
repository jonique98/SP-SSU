import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ControlSection {
	/**
	 * pass1 작업을 수행한다. 기계어 목록 테이블을 통해 소스 코드를 토큰화하고, 심볼 테이블 및 리터럴 테이블을 초기화환다.
	 * 
	 * @param instTable 기계어 목록 테이블
	 * @param input     하나의 control section에 속하는 소스 코드. 마지막 줄은 END directive를 강제로
	 *                  추가하였음.
	 * @throws RuntimeException 소스 코드 컴파일 오류
	 */
	public ControlSection(InstructionTable instTable, ArrayList<String> input) throws RuntimeException {
		_instTable = instTable; // 기계어 목록 테이블을 필드에 할당
		_symbolTable = new SymbolTable(); // 심볼 테이블 초기화
		_literalTable = new LiteralTable(); // 리터럴 테이블 초기화

		_tokens = input.stream()
				.map(Token::new) // String을 Token으로 변환
				.collect(Collectors.toCollection(ArrayList::new)); // ArrayList<Token>으로 수집

		int locctr = 0;

		for (Token token : _tokens) {
			if (token.getOperator().isEmpty()) {
				boolean isLabelEmpty = token.getLabel().isEmpty();
				boolean isOperandEmpty = token.getOperands().isEmpty();
				if (!isLabelEmpty || !isOperandEmpty)
					throw new RuntimeException("missing operator\n\n" + token.toString());
				continue;
			}
			String operator = token.getOperator().get();

			Optional<InstructionInfo> optInst = instTable.search(operator);
			boolean isOperatorInstruction = optInst.isPresent();
			if (isOperatorInstruction) {
				int size = handlePass1InstructionStep(token, locctr, optInst.get());
			} else {
				// Token token = handlePass1DirectiveStep(stringToken, locctr, symTab, litTab);
				// locctr += token.getSize();
				// tokens.add(token);
				// System.out.println(token.toString()); /** 디버깅 용도 */
			}
		}

	

		
	}

	int handlePass1InstructionStep(Token token, int locctr, InstructionInfo instInfo) {
		Optional<String> label = token.getLabel();

		if(label.isPresent()) {
			_symbolTable.put(label.get(), locctr);
		}

		ArrayList<String> operands = token.getOperands();


		int size = instInfo.getSize(token);
		locctr += size;
	}

	/**
	 * pass2 작업을 수행한다. pass1에서 초기화한 토큰 테이블, 심볼 테이블 및 리터럴 테이블을 통해 오브젝트 코드를 생성한다.
	 * 
	 * @return 해당 control section에 해당하는 오브젝트 코드 객체
	 * @throws RuntimeException 소스 코드 컴파일 오류
	 */
	public ObjectCode buildObjectCode() throws RuntimeException {
		ObjectCode objCode = new ObjectCode();

		// TODO: pass2 수행하기.

		return objCode;
	}

	/**
	 * 심볼 테이블을 String으로 변환하여 반환한다. Assembler.java에서 심볼 테이블을 출력하는 데에 사용된다.
	 * 
	 * @return 문자열로 변경된 심볼 테이블
	 */
	public String getSymbolString() {
		return _symbolTable.toString();
	}

	/**
	 * 리터럴 테이블을 String으로 변환하여 반환한다. Assembler.java에서 리터럴 테이블을 출력하는 데에 사용된다.
	 * 
	 * @return 문자열로 변경된 리터럴 테이블
	 */
	public String getLiteralString() {
		return _literalTable.toString();
	}

	/** 기계어 목록 테이블 */
	private InstructionTable _instTable;

	/** 토큰 테이블 */
	private ArrayList<Token> _tokens;

	/** 심볼 테이블 */
	private SymbolTable _symbolTable;

	/** 리터럴 테이블 */
	private LiteralTable _literalTable;
}