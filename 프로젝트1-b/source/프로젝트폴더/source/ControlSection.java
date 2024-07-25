
import java.sql.Ref;
import java.util.ArrayList;
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
				locctr = handlePass1InstructionStep(token, locctr, _symbolTable, _literalTable, optInst.get());
				// System.out.println(token.toString()); /** 디버깅 용도 */
			} else {
				locctr = handlePass1DirectiveStep(token, locctr, _symbolTable, _literalTable, operator);
				// System.out.println(token.toString()); /** 디버깅 용도 */
			}
		}
		_endAddress = Optional.of(locctr);
		_programLength = Optional.of(locctr - _startAddress.get());
	}

	int handlePass1InstructionStep(Token token, int locctr, SymbolTable _symbolTable, LiteralTable _literalTable, InstructionInfo instInfo) {
		Optional<String> label = token.getLabel();

		if(label.isPresent()) {
			_symbolTable.putLabel(label.get(), locctr);
		}

		ArrayList<String> operands = token.getOperands();

		if (operands.get(0).startsWith("=")) {
			_literalTable.putLiteral(operands.get(0));
		}

		int size = instInfo.getSize(token);
		token.setSize(size);
		token.setAddress(locctr);
		return (locctr + size);
	}

	int handlePass1DirectiveStep(Token token, int locctr, SymbolTable _symbolTable, LiteralTable _literalTable, String operator) {

		// System.out.println("operator: " + operator);
		Optional<String> label = token.getLabel();
		switch (operator) {
			case "START":
				_startAddress = Optional.of(Integer.parseInt(token.getOperands().get(0), 16));
				locctr = _startAddress.get();
				_sectionLabel = token.getLabel();
				_symbolTable.setSectionName(_sectionLabel);
				_symbolTable.putLabel(_sectionLabel.get(), locctr);
				break;
			case "CSECT":
				_sectionLabel = token.getLabel();
				_symbolTable.setSectionName(_sectionLabel);
				locctr = 0;
				_startAddress = Optional.of(locctr);
				_symbolTable.putLabel(_sectionLabel.get(), locctr);
				break;
			case "END":
				//남은 리터럴 주소 할당 해줘야함
				locctr = _literalTable.setLiteralAddressAll(locctr);
				
				break;
			case "BYTE":
				if(label.isPresent()) {
					_symbolTable.putLabel(label.get(), locctr);
				}
				token.setSize(1);
				token.setAddress(locctr);
				locctr += 1;
				break;
			case "WORD":
				if(label.isPresent()) {
					_symbolTable.putLabel(label.get(), locctr);
				}
				token.setSize(3);
				token.setAddress(locctr);
				locctr += 3;
				break;
			case "RESB":
				if(token.getOperands().get(0).startsWith("=")) {
					_literalTable.putLiteral(token.getOperands().get(0));
				}
				if(token.getLabel().isPresent()) {
					_symbolTable.putLabel(token.getLabel().get(), locctr);
				}
				locctr += Integer.parseInt(token.getOperands().get(0));
				break;
			case "RESW":
				if(token.getOperands().get(0).startsWith("=")) {
					_literalTable.putLiteral(token.getOperands().get(0));
				}
				if(token.getLabel().isPresent()) {
					_symbolTable.putLabel(token.getLabel().get(), locctr);
				}
				locctr += 3 * Integer.parseInt(token.getOperands().get(0));
				break;
			case "EQU":
				_symbolTable.putLabel(token.getLabel().get(), locctr, token.getOperands().get(0));
				break;
			case "EXTDEF":
				break;
			case "EXTREF":
				for (String refer : token.getOperands()) {
					_symbolTable.putRefer(refer);
				}
				break;
			case "LTORG":
				locctr = _literalTable.setLiteralAddressAll(locctr);
				break;
			default:
				break;
		}

		return locctr;
	}

	/**
	 * pass2 작업을 수행한다. pass1에서 초기화한 토큰 테이블, 심볼 테이블 및 리터럴 테이블을 통해 오브젝트 코드를 생성한다.
	 * 
	 * @return 해당 control section에 해당하는 오브젝트 코드 객체
	 * @throws RuntimeException 소스 코드 컴파일 오류
	 */
	public ObjectCode buildObjectCode() throws RuntimeException {
		ObjectCode objCode = new ObjectCode();
		objCode.setProgramLength(_programLength.get()); // Set the length of the program from a predefined start
		int locctr = _startAddress.get(); // Initialize LOCCTR to the start address of the program
	
		for (Token token : _tokens) {
			if (token.getOperator().isEmpty()) {
				continue;
			}
			
			int pc = locctr + token.getSize();  // Set PC to the address after the current instruction
			String operator = token.getOperator().get();
			Optional<InstructionInfo> optInst = _instTable.search(operator);

			//e 비트 프린트
			if (optInst.isPresent()) {
				handlePass2InstructionStep(pc, token, locctr, _symbolTable, _literalTable, optInst.get(), objCode);  // Increment LOCCTR by the size of the current instruction
			} else {
				handlePass2DirectiveStep(pc, token, locctr, operator, objCode);
			}
			locctr = pc;
		}

		return objCode;
	}

	private void handlePass2InstructionStep(int pc, Token token, int locctr, SymbolTable _symbolTable, LiteralTable _literalTable, InstructionInfo instInfo, ObjectCode objCode) {

		int opcode = instInfo.getOpcode();
		InstructionInfo.Format format = instInfo.getFormat();
		boolean isExtended = token.isE();
		boolean isPCRelative = token.isP();

		if(token.getOperator().get().equals("RSUB")) {
			objCode.addText(locctr, "4F0000");
			return;
		}

		String operand = token.getOperands().isEmpty() ? "" : token.getOperands().get(0);
		int targetAddress = 0;
		int n = token.isN() ? 1 : 0;
		int i = token.isI() ? 1 : 0;
		int x = token.isX() ? 1 : 0;
		int p = token.isP() ? 1 : 0;
		int b = 0;
		int e = isExtended ? 1 : 0;
	
		switch (format) {
			case TWO:
				handleFormat2Instruction(token, opcode, objCode, locctr, instInfo);
				//op와 operand와 objectcode 출력
				break;
			case THREE_OR_FOUR:
				targetAddress = calculateTargetAddress(pc, operand, locctr, _symbolTable, isExtended, isPCRelative, objCode);
				String objectCode = encodeInstruction(opcode, targetAddress, n, i, x, b, p, e);

				//op와 operand와 objectcode 출력
				// System.out.println("operator: " + token.getOperator() + " operand: " + token.getOperands().get(0) + " objectCode: " + objectCode);
				objCode.addText(locctr, objectCode);
				if (e == 1) {
					String temp = "+" + operand;
					objCode.addModification(temp, locctr + 1, 5);
				}
				break;
		}
		// pc counter op와 opcode targetAddress n i x b p e
		// System.out.println("pc = " + pc + " operator" + token.getOperator() + " opcode = " + opcode + " targetAddress = " + targetAddress + " n = " + n + " i = " + i + " x = " + x + " b = " + b + " p = " + p + " e = " + e);
	}

	private int calculateTargetAddress(int pc, String operand, int locctr, SymbolTable _symbolTable, boolean isExtended, boolean isPCRelative, ObjectCode objCode) {
		int targetAddress = 0;
		Optional<Integer> SymbolIsPresent = _symbolTable.getAddress(operand);
		Boolean RefIsPresent = objCode.isInReferList(operand);
		if (isExtended) {
			if(!SymbolIsPresent.isPresent() && !RefIsPresent) {
				throw new RuntimeException("undefined symbol in expression: " + operand);
			}
			return targetAddress; // 포맷 4는 주소 계산을 따로 하지 않습니다.
		}

		// 피연산자로부터 타겟 주소 또는 변위를 계산
		if (operand.startsWith("=")) {
			targetAddress = _literalTable.searchLiteral(operand).get().getAddress().get();
		} else if (operand.startsWith("#")) {
			targetAddress = Integer.parseInt(operand.substring(1));
		} else if (operand.startsWith("@")) {
			String symbol = operand.substring(1);
			targetAddress = _symbolTable.getAddress(symbol).get();
		} else {
			if (SymbolIsPresent.isPresent()) {
				targetAddress = SymbolIsPresent.get();
			} else if (RefIsPresent) {
				targetAddress = 0;
			} else {
				throw new RuntimeException("undefined symbol in expression: " + operand);
			}
		}

		if (isPCRelative) {
			int disp = targetAddress - pc;
			if (disp >= -2048 && disp <= 2047) {
				return disp & 0xFFF;
			} else {
				throw new RuntimeException("PC-relative addressing out of range for symbol: " + operand);
			}
		} else {
			return targetAddress;
		}
	}
	
	private String encodeInstruction(int opcode, int targetAddress, int n, int i, int x, int b, int p, int e) {

		if (e == 1) {
			String ret = "";
			String objectCode = String.format("%02X", opcode);
			ret += objectCode.charAt(0);

			// objectCOde 뒤에 남은 숫자와 n, i 를 조합
			int temp = Integer.parseInt(objectCode.substring(1), 16);
			temp += n * 2;
			temp += i;
			objectCode = String.format("%01X", temp);
			ret += objectCode;

			// x, b, p, e 를 조합
			temp = x * 8;
			temp += b * 4;
			temp += p * 2;
			temp += e;
			objectCode = String.format("%01X", temp);

			ret += objectCode;

			// targetAddress를 16진수로 변환
			objectCode = String.format("%05X", targetAddress);
			ret += objectCode;

			return ret;

		}

		//opcode 16진수로 변환
		String ret = "";
		String objectCode = String.format("%02X", opcode);
		ret += objectCode.charAt(0);

		// objectCOde 뒤에 남은 숫자와 n, i 를 조합
		int temp = Integer.parseInt(objectCode.substring(1), 16);
		temp += n * 2;
		temp += i;
		objectCode = String.format("%01X", temp);
		ret += objectCode;

		// x, b, p, e 를 조합
		temp = x * 8;
		temp += b * 4;
		temp += p * 2;
		temp += e;
		objectCode = String.format("%01X", temp);
		ret += objectCode;

		// targetAddress를 16진수로 변환
		objectCode = String.format("%03X", targetAddress);
		ret += objectCode;

		return ret;
	}

	private void handleFormat2Instruction(Token token, int opcode, ObjectCode objCode, int locctr, InstructionInfo instInfo) {
		int r1 = InstructionInfo.stringToRegister(token.getOperands().get(0));
		int r2 = token.getOperands().size() > 1 ? InstructionInfo.stringToRegister(token.getOperands().get(1)) : 0;
		String objectCode = String.format("%02X%1X%1X", opcode, r1, r2);
		objCode.addText(locctr, objectCode);
	}

	private void handlePass2DirectiveStep(int pc, Token token, int locctr, String operator, ObjectCode objCode) {
		switch (operator) {
			case "START":
				objCode.setStartAddress(locctr);
				objCode.setSectionName(_sectionLabel.get());
				break;
			case "CSECT":
				objCode.setSectionName(_sectionLabel.get());
				objCode.setStartAddress(0);
				objCode.setProgramLength(_endAddress.get());
				break;
			case "END":
				break;
			case "BYTE":
				String operand = token.getOperands().get(0);
				if (operand.startsWith("X")) {
					String value = operand.substring(2, operand.length() - 1); // X'...'에서 ... 부분만 추출
					objCode.addText(locctr, value);
					locctr += value.length() / 2; // 16진수이므로 2로 나누어야 함
				} else if (operand.startsWith("C")) {
					String value = operand.substring(2, operand.length() - 1); // C'...'에서 ... 부분만 추출
					StringBuilder hexValue = new StringBuilder();
					for (int i = 0; i < value.length(); i++) {
						hexValue.append(String.format("%02X", (int) value.charAt(i))); // 각 문자를 ASCII 코드로 변환
					}
					objCode.addText(locctr, hexValue.toString());
					locctr += value.length(); // 문자 수만큼 LOCCTR 증가
				}
				break;
			case "WORD":
				ArrayList<String> operands = token.getOperands();
				if(operands.size() == 1) {
					 int result = evalueateExpression(operands.get(0), objCode, token.getAddress().get(), _symbolTable);
					 objCode.addText(locctr, String.format("%06X", result));
				} else {
					Optional<Integer> op = _symbolTable.getAddress(operands.get(0));
					if (!op.isPresent()) {
						throw new RuntimeException("undefined symbol in expression: " + operands.get(0));
					}
					objCode.addText(locctr, String.format("%06X", op.get()));
				}
				locctr += 3; // 3바이트
				break;
			case "RESB":
				break;
			case "RESW":
				break;
			case "EQU":
				break;
			case "EXTDEF":
				ArrayList<String> defList = token.getOperands();
				for (String def : defList) {
					int address = _symbolTable.getAddress(def).get();
					objCode.addDefineSymbol(def, _symbolTable.getAddress(def).get());
				}
				break;
			case "EXTREF":
				ArrayList<String> refList = token.getOperands();
				for (String ref : refList) {
					objCode.addReferSymbol(ref);
				}
				break;
			case "LTORG":
				ArrayList<Literal> literals = _literalTable.getLiteralMap();
				for (Literal lit : literals) {
					String literal = lit.getLiteral();
					String value = literal.substring(3, literal.length() - 1); // 리터럴에서 =X'...' 또는 =C'...' 부분만 추출
					if (literal.charAt(1) == 'X') {
						if (value.length() % 2 != 0) {
							value = "0" + value; // 짝수 길이가 아닐 경우 앞에 0을 추가
						}
						objCode.addText(locctr, value);
						locctr += value.length() / 2; // 길이를 2로 나눈 값이 바이트 수임
					} else if (literal.charAt(1) == 'C') {
						// 문자 리터럴 처리: 각 문자를 ASCII 코드의 16진수로 변환
						StringBuilder hexValue = new StringBuilder();
						for (int i = 0; i < value.length(); i++) {
							hexValue.append(String.format("%02X", (int) value.charAt(i)));
						}
						objCode.addText(locctr, hexValue.toString());
						locctr += value.length();
					}
				}
				break;
			default:
				break;
		}
	}

	public static int evalueateExpression(String expression, ObjectCode objCode, int locctr, SymbolTable _symbolTable) {
		int result = 0;

		if (expression.contains("+")) {
			String[] operands = expression.split("\\+");
			boolean op1 = objCode.isInReferList(operands[0]);
			boolean op2 = objCode.isInReferList(operands[1]);

			if (op1 && op2) {
				String temp = "+" + operands[0];
				String temp2 = "+" + operands[1];
				objCode.addModification(temp, locctr, 6);
				objCode.addModification(temp2, locctr, 6);
			}
		} else if (expression.contains("-")) {
			String[] operands = expression.split("\\-");
			boolean op1 = objCode.isInReferList(operands[0]);
			boolean op2 = objCode.isInReferList(operands[1]);

			if (op1 && op2) {
				String temp = "+" + operands[0];
				String temp2 = "-" + operands[1];
				objCode.addModification(temp, locctr, 6);
				objCode.addModification(temp2, locctr, 6);
			}
		} else {
			Optional<Integer> operand = _symbolTable.getAddress(expression);
			if (!operand.isPresent()) {
				throw new RuntimeException("undefined symbol in expression: " + expression);
			}
			result = operand.get();
		}
		return result;
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

	private Optional<String> _sectionLabel;
	private Optional<Integer> _startAddress;
	private Optional<Integer> _programLength;
	private Optional<Integer> _endAddress;
	
}