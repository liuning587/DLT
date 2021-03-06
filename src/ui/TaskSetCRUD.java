package ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;


import base.BaseFrame;
import base.CommonObjectList;
import base.InfoClass;

import com.eastsoft.fio.ReadWriteExcel;
import com.eastsoft.util.DebugSwing;

import entity.TaskSet;
import entity.TransImplTaskSet;
import util.DB;
import util.Util698;
import util.Util698.NewObjAction;

/**
 * 数据类型管理
 * 
 * @author xuky
 * @version 2016.10.13
 * 
 */
public class TaskSetCRUD extends BaseFrame implements ActionListener {

	public JTable table;
	DefaultTableModel defaultModel;
	public JButton[] buttonArr;
	
	private JTextField txt_search;
	public JButton bt_search;
	
	CommonObjectList<TaskSet> tasksetList;

	// 用于列表中的显示字段、用于明细信息的中的显示字段
	String[] colNames_table, colNames_info;
	
	// 明细数据对象，1、列表下方显示明细数据用；2、弹窗界面中显示明细数据用
	InfoClass info_detail;
	
	// list对象用
	NewObjAction newobj_act;
	
	// table控件中显示的数据对应的字段名称列表
	String table_columns, export_columns;
	
	@Override
	protected void init() {
		
		// 开发注意：在这里进行当前类变量的赋值代码，不要放在之前的类定义代码中  
		// 禁止，例如：public JTable table = null;
		// 禁止，例如：String[] colNames_info1 = { "code", "父code", "顺序号", "数据名称", "数据类型;noButton", "默认值",		"数据定义;TextArea", "备注" };
		
		// 调整1
		String[] colNames_info1 = { "执行频率","任务类型;code:TaskType","采集方案编号","开始时间",
				"结束时间","延时","执行优先级;code:TaskPriority",
				"状态;code:TaskState","任务开始前脚本ID","任务完成后脚本ID","运行时段类型;code:IntervalType" };
		export_columns = "freq,taskType,schemeID,beginTime,endTime,delay,taskPriority,taskState,preScriptID,afterScriptID,intervalType";

		colNames_info = Util698.setArrayData(colNames_info1);

		// 调整2
		// 注意colNames_table1需要与columns相匹配 且 多个一个ID
		String[] colNames_table1 = { "ID", "执行频率","任务类型","采集方案编号","开始时间",
				"结束时间","执行优先级",	"状态" };
		table_columns = "ID,freq,taskType,schemeID,beginTime,endTime,taskPriority,taskState";
		
		colNames_table = Util698.setArrayData(colNames_table1);
		
		info_detail = new InfoClass(colNames_info,150,200);
		
		// 调整3
		info_detail.setTrans(new TransImplTaskSet());
		// 在界面中放置 终端详细信息显示控件
		panel.add(info_detail.panel);
		info_detail.panel.setBounds(10, 600 - 270-50, 780, 500);

		txt_search= new JTextField(100);
		//panel.add(txt_search);
		txt_search.setBounds(350, 20, 100, 30);

		bt_search= new JButton("查找定位");
		//panel.add(bt_search);
		bt_search.setBounds(460, 20, 100, 30);
		bt_search.addActionListener(this);
		
		
		//dataTypeUserList = new DataTypeUserList("FromDB");
		
		defaultModel = new DefaultTableModel(null, colNames_table);
		table = new JTable(defaultModel);
		
		// 刷新数据
		refresh_List();
		
		// 设置表格的大小
		// table.setPreferredScrollableViewportSize(new Dimension(300, 80));
		table.setRowHeight(20);
		// xuky? 修改table中的字体颜色，目前存在的问题，无法修改标题栏的字体颜色
		table.setForeground(new Color(0, 114, 198));
		// 表格放置在滚动面板中
		JScrollPane scroll = new JScrollPane(table);

		// xuky 2013.10.25 添加排序控制
		TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<DefaultTableModel>(
				defaultModel);
		table.setRowSorter(sorter);

		// 添加表格的选中事件监听器
		ListSelectionModel model;
		model = table.getSelectionModel();
		model.addListSelectionListener(new SelectRowListener());

		setTableColumnWidth();

		// 功能按钮设置
		JPanel panel_buttons = new JPanel();
		panel_buttons.setLayout(null);
		panel_buttons.setBackground(Color.white);
		int buttionNum = 6;
		buttonArr = new JButton[buttionNum];
		Font font = new Font("宋体", Font.BOLD, 13);
		for (int i = 0; i < buttionNum; i++) { // 通过一个循环,对按钮数组中的每一个按钮实例化.
			buttonArr[i] = new JButton();
			buttonArr[i].setForeground(Color.white);
			buttonArr[i].setBackground(new Color(0, 114, 198));
			buttonArr[i].setFont(font);
			panel_buttons.add(buttonArr[i]);
			buttonArr[i].addActionListener(this);
		}

		buttonArr[0].setText("增加");
		buttonArr[1].setText("修改");
		buttonArr[2].setText("删除");
		buttonArr[3].setText("选择");
		
		buttonArr[4].setText("导入");
		buttonArr[5].setText("导出");

		buttonArr[3].setVisible(false);

		buttonArr[0].setBounds(110-105, 15, 80, 30);
		buttonArr[1].setBounds(215-105-20, 15, 80, 30);
		
		buttonArr[2].setBounds(320-105-40, 15, 80, 30);
		buttonArr[3].setBounds(5, 15, 80, 30);
		
		//bt_search.setBounds(560, 20, 100, 30);
		
		
		buttonArr[4].setBounds(460+105, 20, 80, 30);
		buttonArr[5].setBounds(460+105+80+5, 20, 80, 30);
//		panel.add(buttonArr[4]);
//		panel.add(buttonArr[5]);
		
		// 在界面中添加按钮区域、添加列表区域
		panel.add(panel_buttons);
		panel.add(scroll);
		panel_buttons.setBounds(5, 5, 420, 50);
		scroll.setBounds(5, 70, 800 - 30, 600 - 400);
		
		// xuky 默认选中第一行
		if (table.getRowCount() > 0) {
			table.setRowSelectionInterval(0, 0);
		}

	}

	private void setTableColumnWidth() {
		//String[] colNames_table1 = { "ID", "配置序号","通信地址","波特率","规约类型","端口","用户类型","接线方式","采集器地址" };		// 调整列宽度
		
		TableColumn col = table.getColumnModel().getColumn(0);
		col.setMinWidth(0);
		col.setMaxWidth(0);
		
	}
	
	private void importFromExcel() {
		
		String fileName = DebugSwing.fileChoose();
		String[][] data = null;
		if (!fileName.equals("")) {
			data = ReadWriteExcel.excel2StringArray(fileName);
//			System.out.println("importFromExcel=>"+DateTimeFun.getDateTimeSSS() +" begin");
			tasksetList.deleteAll();
//			System.out.println("importFromExcel=>"+DateTimeFun.getDateTimeSSS() +" deleteAll end");
			tasksetList.converFormStringArray(data);
//			System.out.println("importFromExcel=>"+DateTimeFun.getDateTimeSSS() + " converFormStringArray end");
			tasksetList.addAll();
//			System.out.println("importFromExcel=>"+DateTimeFun.getDateTimeSSS() + " addAll end");
		}
		refresh_List();
		DebugSwing.showMsg("导入数据成功！");
	}

	
	private void export2Excel() {
		// 导出前，对数据进行一次重置，按照测量点排序展示
		//setTableData(TerminalID);
		
		String filePath = DebugSwing.directorChoose();
		if (!filePath.equals("")) {
			String fileName = filePath + "电表档案.xls";

			// 将电表对象转换为字符串数组
			String[][] data = tasksetList.getStringArray();

			// 将字符串数据转存为excel文件
			String ret = ReadWriteExcel.stringArray2Excel(data, fileName);

			// 之前作废的函数，将表格中的数据导出为excel文件
			// String ret = ReadWriteUtil.table2Excel(colNames,
			// defaultModel,fileName);

			// 根据返回结果，进行用户提醒
			if (ret.equals(""))
				DebugSwing.showMsg("电表档案导出到\"" + fileName + "\"成功！");
			else
				DebugSwing.showMsg("电表档案导出失败！" + ret);
		}

	}
	
	
	@Override
	public void actionPerformed(java.awt.event.ActionEvent e) {
		
		if (e.getActionCommand().equals("导入")) {
			if (JOptionPane.showConfirmDialog(null,
					"是否从外部导入电表档案？这样操作将会覆盖当前的电表档案数据!",
					"导入提示", JOptionPane.OK_OPTION,
					JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) 
				importFromExcel();
		}
		
		if (e.getActionCommand().equals("导出")) {
			export2Excel();
		}
		
		if (e.getActionCommand().equals("查找定位")) {
			
			String txt = txt_search.getText();
			
			if (txt.equals("")){
				refresh_List();
				return;
			}
			Boolean find = false;
			
			for(int i=table.getSelectedRow()+1;i<table.getRowCount();i++){
				String data = (String)table.getValueAt(i, 2);
				data = data.toUpperCase();
				txt = txt.toUpperCase();
				if ( data.indexOf(txt) >= 0){
					table.getSelectionModel().setSelectionInterval(i, i);
					Rectangle rect = table.getCellRect(i, 0, true);
					table.scrollRectToVisible(rect);
					find = true; 
					break;
				}
			}
			
			
		}
		
		
		if (e.getActionCommand().equals("修改")
				|| e.getActionCommand().equals("增加")) {

			// xuky 弹出一个窗口进行数据修改
			final String aType = e.getActionCommand();

			final JFrame subWin = new JFrame("增加数据");
			if (aType == "修改") {
				if (table.getRowCount() <= 0)
					return;
				subWin.setTitle("修改数据");
			}
			
			subWin.setLayout(null);
			subWin.setSize(750, 320);

			// 直接使用主界面中的明细对象窗口
			final InfoClass info_pop = new InfoClass(colNames_info,150,200);
			info_pop.setTrans(new TransImplTaskSet());
			subWin.add(info_pop.panel);
			info_pop.panel.setBounds(5, 5, 760, 500);
			
			if (aType == "修改") {
				int s = (int) table.getValueAt(table.getSelectedRow(), 0);
				info_pop.setData(tasksetList.getOne(s));
			}
			
			if (aType == "增加") {

				// 防止出现数据为空的情况
				TaskSet newTaskSet = new TaskSet(); 
				// 增加前，清空之前的数据  
				//info_pop.clearComponent();
				info_pop.setData(newTaskSet);
				
			}
			
			// 弹出窗口中 需要有确认和取消按钮
			JButton buttonOk = new JButton("确定");
			buttonOk.setBackground(new Color(0, 114, 198));
			buttonOk.setForeground(Color.white);
			buttonOk.setBounds(250+100, 240, 100, 30);
			subWin.add(buttonOk);
			
			JButton buttonCancle = new JButton("取消");
			buttonCancle.setBackground(new Color(0, 114, 198));
			buttonCancle.setForeground(Color.white);
			buttonCancle.setBounds(355+100, 240, 100, 30);
			subWin.add(buttonCancle);

			// xuky 2016.08.28 以下代码未生效
			// 弹窗中的控件获得焦点
			info_pop.setFocus();

			// 确认按钮添加事件
			buttonOk.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					// http://blog.csdn.net/ethanq/article/details/7200490
					int id = 0;
					TaskSet d;
					if (aType == "增加") {
						// 1、判断各项数据是否合格
						// 2、创建一个唯一的ID
						id = tasksetList.getUseableID();
						// 3、将界面中的数据添加到对象中
						//d = dataTypeUserInfo_pop.getDataNoID();
						d = (TaskSet) info_pop.trans.getData();
						d.setID(id);
						// 4、terminal对象添加到对象列表中 并保存到数据库中
						tasksetList.add(d);
					} else {
						// 修改原先的数据
						// 1、判断各项数据是否合格
						// 3、将界面中的数据添加到对象中
						// 需要将原先的ID恢复
						//d = dataTypeUserInfo_pop.getDataWithID();
						d = (TaskSet)info_pop.trans.getDataWithID();
						id = d.getID();
						// 4、terminal对象替换原先的对象
						tasksetList.update(d);
					}
					// 5、界面中的对象同步到界面的多行列表中
					refresh_List(id);
					subWin.dispose();
				}
			});

			// 取消按钮添加事件
			buttonCancle.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					subWin.dispose();
				}
			});

			DebugSwing.center(subWin); // xuky 窗口居中
			subWin.setVisible(true);

			// xuky 弹出的窗口锁定焦点，不允许定位到主窗口
			subWin.addWindowFocusListener(new WindowFocusListener() {
				public void windowGainedFocus(WindowEvent e) {
				}

				public void windowLostFocus(WindowEvent e) {
					e.getWindow().toFront();
				}
			});

		}

		if (e.getActionCommand().equals("删除")) {

			int rowcount = defaultModel.getRowCount();
			if (rowcount > 0) {
				// xuky 删除当前行
				int rowCount = table.getRowCount(); 
				int deleterow = table.getSelectedRow();
				if (JOptionPane.showConfirmDialog(null,
						"是否确认删除\"" + table.getValueAt(deleterow, 1) +" "+ table.getValueAt(deleterow, 2) + "\"",
						"删除提示", JOptionPane.OK_OPTION,
						JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
					tasksetList.delete((int) table.getValueAt(deleterow, 0));
					// 定位到删除行的前一条deleterow-1
					if (rowCount != deleterow  ){
						int id = 0;
						if (deleterow == 0)
							id = (int)table.getValueAt(0, 0);
						else
							id = (int)table.getValueAt(deleterow-1, 0);
						refresh_List(id);
					}
				}
			}
		}
	}

	private void refresh_List(int id) {
		refresh_List();

		int rowNum = table.getRowCount();
		for (int i = 0; i < rowNum; i++) {
			if ((int) table.getValueAt(i, 0) == id) {
				// xuky 2016。08.25 滚动到某行，但是因为table放在了scrollpanle中，有时会无效
				// table_attr.setRowSelectionInterval(i, i);

				// 参考http://blog.csdn.net/dancen/article/details/7379847
				table.getSelectionModel().setSelectionInterval(i, i);
				Rectangle rect = table.getCellRect(i, 0, true);
				table.scrollRectToVisible(rect);
				break;
			}
		}
	}
	
	
	public void selectRow(int code){
		int rowNum = table.getRowCount();
		for (int i = 0; i < rowNum; i++) {
			if ((int) table.getValueAt(i, 1) == code) {
				// xuky 2016。08.25 滚动到某行，但是因为table放在了scrollpanle中，有时会无效
				// table_attr.setRowSelectionInterval(i, i);

				// 参考http://blog.csdn.net/dancen/article/details/7379847
				table.getSelectionModel().setSelectionInterval(i, i);
				Rectangle rect = table.getCellRect(i, 0, true);
				table.scrollRectToVisible(rect);
				break;
			}
		}
		
	}
	
	private void refresh_List() {
		
		newobj_act =  new NewObjAction(){
			public Object getNewObject() {	return new TaskSet(); }
		};
		tasksetList = new CommonObjectList<TaskSet>(newobj_act,"taskset","taskType,schemeID","","order by taskType,beginTime");
		
		tasksetList.setExportColmunNames(colNames_info);
		tasksetList.setExportColmuns(export_columns);
		
		
		Object[][] data1 = DB.getInstance().getDataList("taskset","","order by taskType,beginTime",new TaskSet(),table_columns);
		refresh_List(data1);
	}
	

	private void refresh_List(Object[][] data1) {
		defaultModel.setDataVector(data1, colNames_table);
		if (table.getRowCount() > 0)
			table.setRowSelectionInterval(0, 0);
		setTableColumnWidth();
	}

	private class SelectRowListener implements ListSelectionListener {
		// 利用该内部类来监听所有事件源产生的事件 便于处理事件代码模块化
		@Override
		public void valueChanged(ListSelectionEvent e) {
			if (e.getValueIsAdjusting())
				return;
			//panel.add(info_detail.panel);
			//info_detail.setBounds(10, 600 - 270-50, 780, 500);
			setDetailData();
		}
	}
	
	private void setDetailData(){
		int row = table.getSelectedRow();
		if (row >= 0) {
			int id = (int) table.getValueAt(row, 0);
			TaskSet dtu = (TaskSet) tasksetList.getOne(id);
			info_detail.setData(dtu);
		}
	}
	
	// 用作数据类型选择窗口时，不显示增加等按钮
	public void DisableEdit() {
		buttonArr[0].setVisible(false);
		buttonArr[1].setVisible(false);
		buttonArr[2].setVisible(false);
		buttonArr[3].setVisible(true);		
	}

	public static void main(String[] args) {
		BaseFrame mainFrame = new TaskSetCRUD();
		mainFrame.getPanel().setBounds(0, 0, 800, 600);
		JFrame frame = new JFrame();
		// frame.setTitle(buttonName);
		frame.setLayout(null);
		frame.add(mainFrame.getPanel());
		frame.setSize(800, 600);
		frame.setVisible(true);
		DebugSwing.center(frame);
	}

	
	


}
