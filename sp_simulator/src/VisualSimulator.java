import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.tree.DefaultMutableTreeNode;

public class VisualSimulator extends JFrame {
	private static final long serialVersionUID = 8254130048142315862L;
	private static VisualSimulator _instance;
	private static Font font;

	private MyMenu myMenu;
	private InfoPanel infoPanel;
	private JTabbedPane tabPane;
	private MemoryPanel memoryPanel;
	private SymbolPanel symbolPanel;
	private DevicePanel devicePanel;

	private SicSimulator sicSimulator;
	private ResourceManager rManager;

	private VisualSimulator() {
		super("SIC/XE Visual Simulator");
		super.setLayout(new FlowLayout());

		try {
			sicSimulator = new SicSimulator(new File("instruction.txt"));
		} catch (BadInstructionFileException e) {
			JOptionPane.showMessageDialog(null, "Fail to load instruction file");
			System.exit(0);
		}
		rManager = ResourceManager.getInstance();

		setSize(970, 585);
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		myMenu = new MyMenu();
		setJMenuBar(myMenu);

		infoPanel = new InfoPanel();
		add(infoPanel);

		tabPane = new JTabbedPane();
		tabPane.setFont(font);
		add(tabPane);

		memoryPanel = new MemoryPanel();
		tabPane.addTab("Memory", memoryPanel);
		
		symbolPanel = new SymbolPanel();
		tabPane.addTab("Symbol", symbolPanel);

		devicePanel = new DevicePanel();
		tabPane.addTab("Device", devicePanel);

		setVisible(true);
	}

	/**
	 * load object code
	 * @param program : object code file
	 */
	public void load(File program) {
		try {
			rManager.initializeResource();
			sicSimulator.load(program);
			setEnabled(true);
			update();
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(null, "Fail to load. data error");
		} catch (OutOfMemoryException e) {
			JOptionPane.showMessageDialog(null, "Fail to load. out of memory");
		}
	}
	
	/**
	 * close currently open file
	 */
	public void close() {
		setEnabled(false);
		update();
	}

	/**
	 * run one command
	 */
	public void oneStep() {
		try {
			sicSimulator.oneStep();
			update();
		} catch (BadInstructionException e) {
			update();
			JOptionPane.showMessageDialog(null, "Invalid instruction");
		} catch (FinishException e) {
			update();
			JOptionPane.showMessageDialog(null, "Finish");
		}
	}

	/**
	 * run every command
	 */
	public void allStep() {
		try {
			sicSimulator.allStep();
			update();
		} catch (BadInstructionException e) {
			update();
			JOptionPane.showMessageDialog(null, "Invalid instruction");
		} catch (FinishException e) {
			update();
			JOptionPane.showMessageDialog(null, "Finish");
		}
		update();
	}

	@Override
	public void setEnabled(boolean enabled) {
		myMenu.setEnabled(enabled);
		infoPanel.setEnabled(enabled);
		memoryPanel.setEnabled(enabled);
		symbolPanel.setEnabled(enabled);
	}

	/**
	 * update screen
	 */
	public void update() {
		infoPanel.update();
		memoryPanel.update();
		symbolPanel.update();
		devicePanel.update();
	}

	public static VisualSimulator getInstance() {
		if (_instance == null)
			_instance = new VisualSimulator();
		return _instance;
	}

	public static void main(String[] args) {
		font = new Font("Consolas", Font.PLAIN, 14);
		VisualSimulator.getInstance();
	}

	class MyMenu extends JMenuBar {
		private static final long serialVersionUID = -2127042418569798495L;
		private JMenu fileMenu = new JMenu("File");
		private JMenu helpMenu = new JMenu("Help");
		private JMenuItem openMenuItem;
		private JMenuItem closeMenuItem;
		private JMenuItem aboutMenuItem;

		public MyMenu() {
			fileMenu = new JMenu("File");
			helpMenu = new JMenu("Help");

			openMenuItem = new JMenuItem("Open File...");
			openMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					FileDialog dlg = new FileDialog(VisualSimulator.getInstance(), "Open");
					dlg.setVisible(true);
					try {
						load(new File(dlg.getDirectory(), dlg.getFile()));
					} catch (NullPointerException ne) {
						// cancel
					}
				}
			});
			closeMenuItem = new JMenuItem("Close File");
			closeMenuItem.setEnabled(false);
			closeMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					close();
				}
			});
			fileMenu.add(openMenuItem);
			fileMenu.addSeparator();
			fileMenu.add(closeMenuItem);

			aboutMenuItem = new JMenuItem("About");
			aboutMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					JOptionPane.showMessageDialog(null, "SIC/XE visual simulator v1.0\nMade by Enoch Jung, 2021");
				}
			});
			helpMenu.add(aboutMenuItem);

			add(fileMenu);
			add(helpMenu);
		}

		@Override
		public void setEnabled(boolean enabled) {
			closeMenuItem.setEnabled(enabled);
		}
	}

	class InfoPanel extends JPanel {
		private static final long serialVersionUID = -9186192420779371914L;

		private ArrayList<JLabel> labels;
		private JLabel lName;
		private JLabel lAddress;
		private ArrayList<JLabel> lRegister;
		private JLabel lOperator;
		private JButton bOneStep;
		private JButton bAllStep;

		public InfoPanel() {
			labels = new ArrayList<JLabel>();
			lRegister = new ArrayList<JLabel>();

			// layout
			setLayout(null);
			setPreferredSize(new Dimension(250, 500));
			TitledBorder tb = new TitledBorder("Info");
			tb.setTitleFont(font);
			setBorder(tb);

			// Program Name :
			JLabel programName = new JLabel("Program Name : ");
			programName.setBounds(20, 25, 200, 20);
			programName.setFont(font);
			labels.add(programName);

			// name
			lName = new JLabel("......");
			lName.setBounds(140, 25, 100, 20);
			lName.setFont(font);
			add(lName);

			// Location :
			JLabel address = new JLabel("Location :");
			address.setBounds(20, 55, 100, 20);
			address.setFont(font);
			labels.add(address);

			// location
			lAddress = new JLabel("...... ~ ......");
			lAddress.setBounds(108, 55, 200, 20);
			lAddress.setFont(font);
			add(lAddress);

			// Registers
			String[] registerName = { "A", "X", "L", "B", "S", "T", "F", "PC", "SW" };
			for (int i = 0; i < registerName.length; ++i) {
				JLabel regName = new JLabel(registerName[i] + " : ");
				regName.setBounds(20, 100 + i * 20, 200, 20);
				regName.setFont(font);
				labels.add(regName);

				JLabel regValue = new JLabel("......" + (i == 6 ? "......" : ""));
				regValue.setBounds(60, 100 + i * 20, 200, 20);
				regValue.setFont(font);
				lRegister.add(regValue);
			}

			// Current Operator :
			JLabel operator = new JLabel("Current Operator :");
			operator.setBounds(20, 350, 200, 20);
			operator.setFont(font);
			labels.add(operator);

			// operator name
			lOperator = new JLabel("-");
			lOperator.setBounds(170, 350, 200, 20);
			lOperator.setFont(font);
			add(lOperator);

			// one step
			bOneStep = new JButton("One step");
			bOneStep.setBounds(20, 420, 210, 25);
			bOneStep.setFont(font);
			bOneStep.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					oneStep();
				}
			});
			add(bOneStep);

			// all step
			bAllStep = new JButton("All step");
			bAllStep.setBounds(20, 455, 210, 25);
			bAllStep.setFont(font);
			bAllStep.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					allStep();
				}
			});
			add(bAllStep);

			for (JLabel label : labels)
				add(label);
			for (JLabel label : lRegister)
				add(label);
			this.setEnabled(false);
		}

		@Override
		public void setEnabled(boolean enabled) {
			super.setEnabled(enabled);
			for (JLabel label : labels)
				label.setEnabled(enabled);
			lName.setVisible(enabled);
			lAddress.setVisible(enabled);
			if (enabled == false) {
				lName.setText("");
				lAddress.setText("");
			}
			for (JLabel label : lRegister) {
				label.setEnabled(enabled);
				if (enabled == false) {
					label.setText("");
				}
			}
			lOperator.setEnabled(enabled);
			if (enabled == false)
				lOperator.setText("-");
			bOneStep.setEnabled(enabled);
			bAllStep.setEnabled(enabled);
		}

		public void update() {
			lName.setText(rManager.getProgramName());
			String first = String.format("%06X", rManager.getFirstAddress());
			String last = String.format("%06X", rManager.getLastAddress());
			lAddress.setText(first + " ~ " + last);
			lRegister.get(0).setText(String.format("%06X", rManager.getRegister(Register.A)));
			lRegister.get(1).setText(String.format("%06X", rManager.getRegister(Register.X)));
			lRegister.get(2).setText(String.format("%06X", rManager.getRegister(Register.L)));
			lRegister.get(3).setText(String.format("%06X", rManager.getRegister(Register.B)));
			lRegister.get(4).setText(String.format("%06X", rManager.getRegister(Register.S)));
			lRegister.get(5).setText(String.format("%06X", rManager.getRegister(Register.T)));
			lRegister.get(6).setText(String.format("%012X", 0));
			lRegister.get(7).setText(String.format("%06X", rManager.getRegister(Register.PC)));
			lRegister.get(8).setText(String.format("%06X", rManager.getRegister(Register.SW)));
			lOperator.setText(sicSimulator.currentInstructionName());
		}
	}

	class MemoryPanel extends JPanel {
		private static final long serialVersionUID = -2732953370226644513L;

		private JTextPane tMemory;
		private Document dMemory;
		private JScrollPane scrollPane;
		private DefaultHighlighter.DefaultHighlightPainter painter;

		public MemoryPanel() {
			tMemory = new JTextPane();
			dMemory = tMemory.getDocument();

			// layout
			setLayout(null);
			setPreferredSize(new Dimension(660, 470));

			// memory
			tMemory.setFont(font);
			tMemory.setEditable(false);
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < ResourceManager.MEMORY_SIZE / 16; ++i) {
				sb.append(String.format(" %06X  ", i * 16));
				for (int j = 0; j < 16; ++j)
					sb.append("-- ");
				sb.append(" ");
				for (int j = 0; j < 16; ++j)
					sb.append(".");
				if (i + 1 < ResourceManager.MEMORY_SIZE / 16)
					sb.append("\n");
			}
			tMemory.setText(sb.toString());

			// scroll pane
			scrollPane = new JScrollPane(tMemory);
			scrollPane.setBounds(20, 20, 620, 430);
			add(scrollPane);

			painter = new DefaultHighlighter.DefaultHighlightPainter(Color.cyan);

			this.setEnabled(false);
		}

		@Override
		public void setEnabled(boolean enabled) {
			super.setEnabled(enabled);
			tMemory.setEnabled(enabled);
			scrollPane.setEnabled(enabled);
		}

		public void update() {
			try {
				// memory text update
				ArrayList<Pair> mods = rManager.getModification();
				rManager.clearModification();
				for (Pair mod : mods) {
					int start = mod.position;
					int end = mod.size + start;
					int offset = 0;
					byte[] data = rManager.getMemory(mod.position, mod.size);
					while (start < end) {
						int a = start;
						int b = Math.min((a / 16 + 1) * 16, end);
						updateMemory(a, b, data, offset);
						offset += (start / 16 + 1) * 16 - start;
						start = (start / 16 + 1) * 16;
					}
				}

				// instruction highlighting
				tMemory.getHighlighter().removeAllHighlights();
				Pair pair = sicSimulator.currentInstructionPosition();
				int start = pair.position;
				int size = pair.size;
				while (size > 0) {
					int row = start / 16;
					int offset = start % 16;
					int drawPosition = row * 75 + offset * 3 + 9;
					int drawSize = size == 1 ? 2 : 3;
					tMemory.getHighlighter().addHighlight(drawPosition, drawPosition + drawSize, painter);
					++start;
					--size;
				}
				tMemory.setCaretPosition((start / 16) * 75);
			} catch (BadLocationException e) {
				e.printStackTrace();
			} catch (BadInstructionException e) {
				e.printStackTrace();
			}
		}

		private boolean isPrintable(Character ch) {
			if (ch < 0 || 127 < ch)
				return false;
			if (Character.isDigit(ch))
				return true;
			if (Character.isAlphabetic(ch))
				return true;
			return "!@#$%&*()'+,-./:;<=>?[]^_`{|} ".indexOf(ch) != -1;
		}

		private void updateMemory(int start, int end, byte[] data, int index) throws BadLocationException {
			int row = start / 16;
			int offset = start % 16;
			int length = end - start;
			int bytePosition = row * 75 + offset * 3 + 9;
			int charPosition = row * 75 + 58 + offset;
			StringBuffer bsb = new StringBuffer();
			StringBuffer csb = new StringBuffer();
			for (int i = 0; i < length; ++i) {
				bsb.append(String.format("%02X ", data[index + i]));
				char ch = (char) data[index + i];
				csb.append(isPrintable(ch) ? Character.toString(ch) : ".");
			}
			dMemory.remove(bytePosition, length * 3);
			dMemory.insertString(bytePosition, bsb.toString(), null);
			dMemory.remove(charPosition, length);
			dMemory.insertString(charPosition, csb.toString(), null);
		}
	}

	class SymbolPanel extends JPanel {
		private static final long serialVersionUID = 1509741061592885554L;

		private DefaultMutableTreeNode root;
		private JTree tSymbol;
		private JScrollPane scrollPane;
		
		public SymbolPanel() {
			// layout
			setLayout(null);
			setPreferredSize(new Dimension(660, 470));

			// symbol
			root = new DefaultMutableTreeNode("Program");
			
			// symbol tree
			tSymbol = new JTree(root);
			tSymbol.setFont(font);
			tSymbol.setBorder(BorderFactory.createCompoundBorder(tSymbol.getBorder(),
					BorderFactory.createEmptyBorder(5, 5, 5, 5)));

			// scroll pane
			scrollPane = new JScrollPane(tSymbol);
			scrollPane.setBounds(20, 20, 620, 430);
			add(scrollPane);
		}

		@Override
		public void setEnabled(boolean enabled) {
			super.setEnabled(enabled);
			tSymbol.setEnabled(enabled);
			scrollPane.setEnabled(enabled);
		}

		public void update() {
			ArrayList<SymbolTable> symbolTables = rManager.getSymbolTableList();
			
			root.removeAllChildren();
			for (SymbolTable table : symbolTables) {
				ArrayList<String> symbols = table.getSymbolStrings();
				DefaultMutableTreeNode program = new DefaultMutableTreeNode(symbols.get(0));
				for (int i = 1; i < symbols.size(); ++i) {
					DefaultMutableTreeNode symbol = new DefaultMutableTreeNode(symbols.get(i));
					program.add(symbol);
				}
				root.add(program);
			}
		}
	}
	
	class DevicePanel extends JPanel {
		private static final long serialVersionUID = -9146673462361141885L;

		private JList<String> deviceList;
		private DefaultListModel<String> model;
		private JScrollPane listScrollPane;
		private JButton bInsert;
		private JButton bRemove;
		private JTextArea tData;
		private JScrollPane dataScrollPane;

		public DevicePanel() {
			// layout
			setLayout(null);
			setPreferredSize(new Dimension(660, 470));

			// model
			model = new DefaultListModel<String>();

			// device list
			deviceList = new JList<String>(model);
			deviceList.setFont(font);
			deviceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			deviceList.addListSelectionListener(new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent e) {
					//update();
					String selected = deviceList.getSelectedValue();
					select(selected);
				}
			});

			// list scroll
			listScrollPane = new JScrollPane(deviceList);
			listScrollPane.setBounds(20, 20, 140, 340);
			add(listScrollPane);

			// insert
			bInsert = new JButton("Insert");
			bInsert.setBounds(20, 380, 140, 25);
			bInsert.setFont(font);
			bInsert.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					new InsertFrame();
					update();
				}
			});
			add(bInsert);

			// delete
			bRemove = new JButton("Remove");
			bRemove.setBounds(20, 420, 140, 25);
			bRemove.setFont(font);
			bRemove.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					String selected = deviceList.getSelectedValue();
					rManager.removeDevice(selected);
					update();
				}
			});
			bRemove.setEnabled(false);
			add(bRemove);

			// memory
			tData = new JTextArea();
			tData.setFont(font);
			tData.setEditable(false);
			tData.setEnabled(false);

			// data scroll pane
			dataScrollPane = new JScrollPane(tData);
			dataScrollPane.setBounds(180, 20, 460, 430);
			add(dataScrollPane);

			update();
		}

		public void update() {
			ArrayList<Device> devices = rManager.getDevices();
			model.clear();
			for (Device device : devices) {
				model.addElement(device.getName());
			}
		}
		
		private void select(String name) {
			if (name == null) {
				bRemove.setEnabled(false);
				tData.setEnabled(false);
				tData.setText("");
			} else {
				bRemove.setEnabled(true);
				tData.setEnabled(true);
				tData.setText(rManager.getDevice(name).getData());
			}
		}

		private class InsertFrame extends JDialog {
			private static final long serialVersionUID = 1074858126318837442L;

			private JTextField tDeviceNumber;
			private JTextArea tData;
			private JButton bOK;
			private JButton bCancel;

			public InsertFrame() {
				super((Frame) null, "Insert new device", true);

				setLayout(null);
				setSize(500, 470);
				setResizable(false);
				setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

				// Device Number :
				JLabel lDeviceNumber = new JLabel("Device Number :");
				lDeviceNumber.setBounds(20, 20, 200, 20);
				lDeviceNumber.setFont(font);
				add(lDeviceNumber);

				// device number
				tDeviceNumber = new JTextField();
				tDeviceNumber.setBounds(20, 45, 100, 24);
				tDeviceNumber.setFont(font);
				add(tDeviceNumber);

				// Data :
				JLabel lData = new JLabel("Data :");
				lData.setBounds(20, 90, 200, 20);
				lData.setFont(font);
				add(lData);

				// memory
				tData = new JTextArea();
				tData.setFont(font);
				tData.setBorder(BorderFactory.createCompoundBorder(tData.getBorder(),
						BorderFactory.createEmptyBorder(5, 5, 5, 5)));

				// data scroll pane
				dataScrollPane = new JScrollPane(tData);
				dataScrollPane.setBounds(20, 115, 440, 250);
				add(dataScrollPane);

				// ok
				bOK = new JButton("OK");
				bOK.setBounds(20, 380, 210, 25);
				bOK.setFont(font);
				bOK.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							rManager.addDevice(tDeviceNumber.getText(), tData.getText());
							dispose();
						} catch (IllegalArgumentException iae) {
							JOptionPane.showMessageDialog(null,
									"Device Number must be a two-digit hexadecimal number.");
						}
					}
				});
				add(bOK);

				// cancel
				bCancel = new JButton("Cancel");
				bCancel.setBounds(250, 380, 210, 25);
				bCancel.setFont(font);
				bCancel.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						dispose();
					}
				});
				add(bCancel);

				setVisible(true);
			}
		}
	}
}

class Pair {
	public int position;
	public int size;

	public Pair(int position, int size) {
		this.position = position;
		this.size = size;
	}
}