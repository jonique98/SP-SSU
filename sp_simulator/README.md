# SIC-XE-Simulator
![main](https://user-images.githubusercontent.com/21188135/154781295-2b56ecd5-6876-4b88-8dc9-cb7b939da166.png)   
숭실대학교 최재영 교수님의 시스템프로그래밍 과목 중 개발한 SIC/XE 시뮬레이터   
Eclipse에서 swing을 사용해 개발하였다.   
# 사용 방법
1. 상단 메뉴에서 File -> Open 으로 SIC/XE object code를 열기
2. Memory 탭 좌측의 One Step / All Step 버튼을 통해 시뮬레이터 실행
# UI 설명
## Info 탭
File -> Open 으로 연 프로그램에 대한 정보를 보여 준다.
* 프로그램 이름
* 프로그램이 상주한 메모리 위치
* 여러 레지스터들의 값
* PC가 가리키는 명령어
* 명령어 하나 실행
* 모든 명령어 실행 (PC 값이 0xFFFFFF가 될 때까지 실행)
## Memory 탭
![memory tab](https://user-images.githubusercontent.com/21188135/154781687-1e26f56e-801c-4929-865d-1a2d25ef8e10.png)   
SIC/XE 가상 머신의 전체 메모리를 16진법으로 보여 준다. 우측에서 문자형으로도 확인할 수 있다. PC가 가리키는 명령어는 파란색으로 강조한다.
## Symbol 탭
![symbol tab](https://user-images.githubusercontent.com/21188135/154781798-d66c0dda-41b1-4320-91b8-28e7d9a36404.png)   
object code에서 사용한 symbol을 보여 준다.
## Device 탭
![device tab](https://user-images.githubusercontent.com/21188135/154781854-20ed1a03-0401-4dcf-b162-06a26d58377c.png)   
가상 장치를 추가 및 제거하거나 장치의 버퍼에 들어간 문자열 값을 확인할 수 있다.

참고**만**하세요!!
