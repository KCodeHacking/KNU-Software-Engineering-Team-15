package System_UI;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import credit_management.completed_credit;
import credit_management.completed_credit_list;


public class credit_UI extends JPanel{
	private UserInterface MAIN;
	
	private int count = 0;
	private CreditSemester SemesterList = null;
	private JPanel contents_bg = null; 
	private JTable credit_JTable;
	private JList<String> credit_JList;
	private Credit_Contents Display = null;
	private Credit_Contents temp = null;
	
	private int selected_index = -1;
	private JButton register_button = new JButton("신청");
	
	public credit_UI(int st_id, UserInterface MAIN, boolean isapped) throws ClassNotFoundException {
		this.MAIN = MAIN;
		setLayout(new FlowLayout());
		setSize(500, 400);
		
		setting(st_id, isapped);
	}
	
	public void setting(int st_id, boolean isapped) throws ClassNotFoundException {
		
		if (SemesterList != null) remove(SemesterList);
		if (contents_bg != null) remove(contents_bg);
		
		SemesterList = new CreditSemester(st_id, isapped);
		add(SemesterList.getCredit_JList());
		contents_bg = new JPanel();
		contents_bg.setPreferredSize(new Dimension(380, 400));
		add(contents_bg);

		ActionListener RBAL = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				selected_index = temp.getSelected_index();
				if(selected_index == -1) {}
				else{
					if(temp.getList().get(selected_index).isApplication_state()) {
						JOptionPane.showMessageDialog(null, "이미 신청한 학점입니다", "알림", JOptionPane.PLAIN_MESSAGE);
					}
					else{
						String[] buttons = {"신청", "취소"};
						int result = 0;
						result = JOptionPane.showOptionDialog(null, "신청하시겠습니까?", "학점인정 신청", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, buttons, "취소");
						if(result == 0) {
							try {
								temp.getList().get(selected_index).credit_application();
								JOptionPane.showMessageDialog(null, "등록되었습니다", "알림", JOptionPane.PLAIN_MESSAGE);
								contents_print(st_id, isapped, this);
								MAIN.getCreditViewIsapped().removeAll();
								MAIN.getCreditViewIsapped().setting(st_id, true);
							} catch (ClassNotFoundException e1) {};
						}
					}
				}
			}
		};
		
		SemesterList.getCredit_JList().addListSelectionListener(new ListSelectionListener() {
			
			public void valueChanged(ListSelectionEvent e) {
				if(e.getValueIsAdjusting()) {
					credit_JList = SemesterList.getCredit_JList();
					int index = credit_JList.getSelectedIndex();
					
					if(index >= 0 && !(credit_JList.getSelectedValue() == completed_credit_list.NO_LIST)) {
						contents_print(st_id, isapped, RBAL);
					}
				}
			}
		});
	}
	
	public void contents_print(int st_id, boolean isapped, ActionListener RBAL) {
		if(Display != null) {
			register_button.removeActionListener(RBAL);
			contents_bg.removeAll();
			selected_index = -1;
		}
		try {
			temp = new Credit_Contents(st_id, isapped, Integer.parseInt(credit_JList.getSelectedValue().substring(0, 4)), Integer.parseInt(credit_JList.getSelectedValue().substring(6, 7)));
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		contents_bg.add(temp.getContents_table());
		if(!isapped) contents_bg.add(register_button); // 등록학점 조회 탭에서는 미출력
		contents_bg.revalidate();
		contents_bg.repaint();
		Display = temp;
		
		
		register_button.addActionListener(RBAL);
	}
	
	
	class CreditSemester extends JPanel{
		private JList<String> credit_JList;
		private completed_credit_list c_list;

		public CreditSemester(int st_id, boolean isapped) throws ClassNotFoundException {
			setLayout(null);
			c_list = completed_credit_list.get_completed_credit_list();
			String[] credit_list_string;
			if(isapped) {
				credit_list_string = c_list.semester_list_isapped(st_id);				
			}
			else {
				credit_list_string = c_list.semester_list(st_id);
			}
			
			if(credit_list_string != null) {
				credit_JList = new JList<String>(credit_list_string);
			}
			else {
				String[] temp = {"내역이 없습니다"};
				credit_JList = new JList<String>(temp);
			}
			credit_JList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			credit_JList.setVisibleRowCount(20);
			credit_JList.setFixedCellHeight(20);
			credit_JList.setFixedCellWidth(120);
			JScrollPane ScrollList = new JScrollPane(credit_JList);
			
			add(ScrollList);
		}

		public JList<String> getCredit_JList() {
			return credit_JList;
		}

		public void setCredit_JList(JList<String> credit_JList) {
			this.credit_JList = credit_JList;
		}
		
		public completed_credit_list getC_list() {
			return c_list;
		}

		public void setC_list(completed_credit_list c_list) {
			this.c_list = c_list;
		}
		
	}
	
	
	class Credit_Contents extends JPanel implements MouseListener{
		private JTable contents_table;
		private JScrollPane ScrollList;
		
		private String contents_column[] = {"국가", "대학", "학과", "과목", "인정학점", "성적", "비고"};
		
		private Object DATA[][];
		
		private ArrayList<completed_credit> list = new ArrayList<>();
		private int selected_index = -1;
		
		private int year = 0;
		private int semester = 0;
		
		public Credit_Contents(int st_id, boolean isapped, int input_year, int input_semester) throws ClassNotFoundException {
			selected_index = -1;
			
			year = input_year;
			semester = input_semester;
			
			completed_credit_list c_list = completed_credit_list.get_completed_credit_list();
			if(isapped) {
				list = c_list.applicated_credit_list_print(st_id, year, semester);
			}
			else {
				list = c_list.completed_credit_list_print(st_id, year, semester);
			}
			DATA = new Object[list.size()][7];
			
			for(int i = 0; i < list.size(); i++) {
				DATA[i][0] = list.get(i).getNation();
				DATA[i][1] = list.get(i).getUniv();
				DATA[i][2] = list.get(i).getDept();
				DATA[i][3] = list.get(i).getCourse();
				DATA[i][4] = list.get(i).getAccept_credit();
				DATA[i][5] = list.get(i).getGrade();
				if(list.get(i).isApplication_state()) {
					DATA[i][6] = "등록완료";
				}
				else {
					DATA[i][6] = "미신청";
				}
			}
			
			contents_table = new JTable(DATA, contents_column);
			contents_table.setBackground(Color.WHITE);
			contents_table.setGridColor(Color.BLACK);
			contents_table.setForeground(Color.BLACK);
			
			resize_rowcol(contents_table);
			
			contents_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			contents_table.addMouseListener(this);
			
			
			
			ScrollList = new JScrollPane(contents_table);
			add(ScrollList);
		}
		
		public void resize_rowcol(JTable table) {
			final TableColumnModel model = table.getColumnModel();
			for(int col = 0; col < table.getColumnCount(); col++) {
				int width = 10;
				for(int row = 0; row < table.getRowCount(); row++) {
					TableCellRenderer renderer = table.getCellRenderer(row, col);
					Component comp = table.prepareRenderer(renderer, row, col);
					width = Math.max(comp.getPreferredSize().width + 1, width);
				}
				model.getColumn(col).setPreferredWidth(width);
			}
		}

		public JTable getContents_table() {
			return contents_table;
		}
		
		public int getSelected_index() {
			return selected_index;
		}

		public ArrayList<completed_credit> getList() {
			return list;
		}

		@Override
		public void mouseClicked(MouseEvent MC) {
			// TODO Auto-generated method stub
			selected_index = contents_table.getSelectedRow();
		}

		@Override
		public void mouseEntered(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseExited(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mousePressed(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
	}
}
	
