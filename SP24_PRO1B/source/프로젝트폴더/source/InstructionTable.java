import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class InstructionTable {
	/**
	 * 기계어 목록 파일을 읽어, 기계어 목록 테이블을 초기화한다.
	 * 
	 * @param instFileName 기계어 목록이 적힌 파일
	 * @throws FileNotFoundException 기계어 목록 파일 미존재
	 * @throws IOException           파일 읽기 실패
	 */


	public InstructionTable(String instFileName) throws FileNotFoundException, IOException, RuntimeException {
		HashMap<String, InstructionInfo> instMap = new HashMap<String, InstructionInfo>();

		ArrayList<String> data = readFile(instFileName);
		data.forEach(x -> {
			String name = x.substring(0, x.indexOf('\t'));
			instMap.put(name, new InstructionInfo(x));
		});

		_instructionMap = instMap;
	}

	/**
	 * 기계어 목록 테이블에서 특정 기계어를 검색한다.
	 * 
	 * @param instructionName 검색할 기계어 명칭
	 * @return 기계어 정보. 없을 경우 empty
	 */
	public Optional<InstructionInfo> search(String instructionName) {
		// TODO: instructionMap에서 instructionName에 해당하는 명령어의 정보 반환하기.

		if(instructionName.charAt(0) == '+')
			instructionName = instructionName.substring(1);

		if (_instructionMap.containsKey(instructionName))
			return Optional.of(_instructionMap.get(instructionName));
		else
			return Optional.empty();
	}

	private ArrayList<String> readFile(String fileName) throws FileNotFoundException, IOException {
		ArrayList<String> data = new ArrayList<String>();

		File file = new File(fileName);
		BufferedReader bufReader = new BufferedReader(new FileReader(file));

		String line = "";
		while ((line = bufReader.readLine()) != null)
			data.add(line);

		bufReader.close();

		return data;
	}

	/** 기계어 목록 테이블. key: 기계어 명칭, value: 기계어 정보 */
	private HashMap<String, InstructionInfo> _instructionMap;
}