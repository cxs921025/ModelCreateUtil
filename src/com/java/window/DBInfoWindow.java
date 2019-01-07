package com.java.window;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import com.java.connection.ConnectionToDB;
import com.java.model.ColumnModel;
import com.java.model.CreateModel;
import com.java.model.ResultModel;
import com.sun.awt.AWTUtilities;

/**
 * 
 * @author ChenXS
 *
 */
public class DBInfoWindow extends JFrame {
	private static final long serialVersionUID = 1L;

	private static String dbType = "MySQL"; // 数据库类型
	private static String ip; // ip地址
	private static String dbName; // 数据库名称
	private static String userName; // 登录用户名
	private static String pwd; // 登录密码
	private static String path = "D:"; // 文件输出地址
	private static ResultModel results = null;
	private static ConnectionToDB connectionToDB = null;

	/**
	 * 私有化构造方法,不允许外部创建
	 */
	private DBInfoWindow() {
		createWindowShow(this);
	}

	/**
	 * 创建窗口显示内容
	 * 
	 * @param frame
	 */
	private static void createWindowShow(final JFrame frame) {
		frame.setTitle("工具");
		frame.setLocation(500, 150); // 窗口位置(X, Y)
		frame.setSize(600, 340); // 窗口大小(宽, 高)
		frame.setLayout(new FlowLayout());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 关闭窗口退出程序
		frame.setLocationRelativeTo(null); // 窗体居中显示
		Container pane = frame.getContentPane(); // 获取窗口容器
		pane.setBackground(new Color(255, 250, 250)); // 设置颜色(R, G, B)
		pane.setLayout(new BorderLayout());

		/* 设置下拉选择框 */
		final JComboBox<String> comboBox = new JComboBox<>();
		comboBox.addItem("MySQL");
		comboBox.addItem("Oracle");
		comboBox.addItem("DB2");
		/* 添加选择时监听事件 */
		comboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dbType = comboBox.getSelectedItem().toString();
			}
		});

		/* 下拉框面板 */
		JPanel comboxPanel = new JPanel();
		comboxPanel.setLayout(new FlowLayout());
		comboxPanel.add(comboBox);

		/* 地址输入框面板 */
		JPanel addrPanel = new JPanel();
		addrPanel.setLayout(new FlowLayout());
		addrPanel.add(new JLabel("IP&Port:"));
		final JTextField iPField = new JTextField("localhost:3306", 15);
		addrPanel.add(iPField);
		JLabel jLabel = new JLabel("127.0.0.1:3306");
		jLabel.setForeground(Color.RED);
		addrPanel.add(jLabel);

		/* 数据库类型选择面板 */
		JPanel dbTypePanel = new JPanel();
		dbTypePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		dbTypePanel.add(new JLabel("数据库类型:"));
		dbTypePanel.add(comboxPanel);

		/* 数据库用户名、密码面板 */
		JPanel uNameAndPwsPanel = new JPanel();
		uNameAndPwsPanel.setLayout(new BorderLayout());
		JPanel insertPanel = new JPanel();
		insertPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		insertPanel.add(new JLabel("数 据 库:"));
		final JTextField dbNameField = new JTextField(15);
		insertPanel.add(dbNameField);
		uNameAndPwsPanel.add(insertPanel, BorderLayout.NORTH);
		insertPanel = new JPanel();
		insertPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		insertPanel.add(new JLabel("用 户 名:"));
		final JTextField userNameField = new JTextField(15);
		insertPanel.add(userNameField);
		uNameAndPwsPanel.add(insertPanel, BorderLayout.CENTER);
		insertPanel = new JPanel();
		insertPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		insertPanel.add(new JLabel("密       码:"));
		final JPasswordField pwdField = new JPasswordField(15);
		insertPanel.add(pwdField);
		uNameAndPwsPanel.add(insertPanel, BorderLayout.SOUTH);

		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BorderLayout());
		centerPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

		JPanel centerNP = new JPanel();
		centerNP.setLayout(new FlowLayout(FlowLayout.LEFT));
		centerNP.add(new JLabel("包   路 径:"));
		final JTextField packageField = new JTextField(15);
		centerNP.add(packageField);

		JPanel centerCP = new JPanel();
		centerCP.setLayout(new FlowLayout(FlowLayout.LEFT));
		centerCP.add(new JLabel("输出目录:"));
		final JTextField pathField = new JTextField(15);
		centerCP.add(pathField);
		jLabel = new JLabel("默认地址: D:\\");
		jLabel.setForeground(Color.RED);
		centerCP.add(jLabel);

		centerPanel.add(centerNP, BorderLayout.NORTH);
		centerPanel.add(centerCP, BorderLayout.CENTER);
		JButton selectBtn = new JButton("查询");
		selectBtn.setPreferredSize(new Dimension(0, 20));
		centerPanel.add(selectBtn, BorderLayout.SOUTH);

		JPanel northPanel = new JPanel();
		northPanel.setLayout(new BorderLayout());
		northPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		northPanel.add(dbTypePanel, BorderLayout.NORTH); // 添加数据库类型 面板
		northPanel.add(addrPanel, BorderLayout.CENTER); // 添加地址输入面板
		northPanel.add(uNameAndPwsPanel, BorderLayout.SOUTH); // 添加用户名、密码面板

		/* 左半部分面板 */
		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new BorderLayout());
		leftPanel.add(northPanel, BorderLayout.NORTH);
		leftPanel.add(centerPanel, BorderLayout.CENTER);
		pane.add(BorderLayout.WEST, leftPanel); // 向窗体的左部分添加面板控件

		/* 右半部分面板 */
		final JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new BorderLayout());
		pane.add(BorderLayout.CENTER, rightPanel); // 向窗体的左部分添加面板控件

		/* 底部面板 */
		final JPanel bottomPanel = new JPanel();
		JButton createBtn = new JButton("生成Bean");
		bottomPanel.add(createBtn);

		pane.add(BorderLayout.SOUTH, bottomPanel); // 向窗体的底部添加面板控件

		frame.setResizable(false);
		frame.setVisible(true); // 显示控件

		/* 查询按钮监听 */
		selectBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setDBConProPerty(iPField, dbNameField, userNameField, pwdField, rightPanel);
			}
		});

		/* 生成Bean按钮监听 */
		createBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				createBean(pathField, packageField, bottomPanel);
			}
		});
	}

	/**
	 * 设置数据库连接属性
	 * 
	 * @param iPField
	 *            数据库链接地址文本框
	 * @param dbNameField
	 *            数据库名称文本框
	 * @param userNameField
	 *            用户名文本框
	 * @param pwdField
	 *            密码文本框
	 * @param rightPanel
	 *            右侧显示数据库表面板
	 */
	private static void setDBConProPerty(JTextField iPField, JTextField dbNameField, JTextField userNameField,
			JTextField pwdField, JPanel rightPanel) {
		ip = iPField.getText();
		dbName = dbNameField.getText();
		userName = userNameField.getText();
		pwd = pwdField.getText();
		searchListener(rightPanel, null);
	}

	/**
	 * 生成bean
	 * 
	 * @param pathField
	 *            文件生成目录文本框
	 * @param packageField
	 *            包路径文本框
	 * @param bottomPanel
	 *            底部提示框
	 */
	private static void createBean(JTextField pathField, JTextField packageField, JPanel bottomPanel) {
		path = "".equals(pathField.getText().trim()) ? path : pathField.getText();
		LinkedHashMap<String, List<ColumnModel>> map = new LinkedHashMap<>();
		Object[][] data = results.getData();
		try {
			for (int i = 0; i < data.length; i++) {
				if ((boolean) data[i][0]) {
					String tableName = data[i][1].toString();
					List<ColumnModel> columnNames = connectionToDB.selectColumns(tableName, dbName);
					map.put(tableName, columnNames);
				}
			}
		} catch (Exception e1) {
			// Do nothing
		}
		CreateModel createModel = new CreateModel();
		Map<String, Object> result = createModel.createBean(path, packageField.getText(), map);
		String content;
		List<Object> options = new ArrayList<>();
		options.add("关闭");
		if ((boolean) result.get("isSuccess")) {
			content = "<html><div style='width:200px; height: 30px; line-height: 30px; padding-left: 72px; padding-top: 8px;'>成功 *\\\\ ^o^ //*</div></html>";
			options.add("打开文件目录");
		} else {
			content = "<html><div style='width:200px; height: 30px; line-height: 30px; padding-left: 72px; padding-top: 8px;'>失败了 q *_* p</div></html>";
		}
		int response = JOptionPane.showOptionDialog(null, content, "结果", JOptionPane.YES_OPTION,
				JOptionPane.QUESTION_MESSAGE, new Icon() {
					// 不使用图标
					@Override
					public void paintIcon(Component c, Graphics g, int x, int y) {
						// Do nothing
					}

					@Override
					public int getIconWidth() {
						return 0;
					}

					@Override
					public int getIconHeight() {
						return 0;
					}
				}, options.toArray(new Object[0]), options.get(0));
		if (response == 1) {
			try {
				// 打开本地文件目录，并选中生成的文件夹
				Runtime.getRuntime()
						.exec("rundll32 SHELL32.DLL,ShellExec_RunDLL " + "Explorer.exe /select," + result.get("path"));
			} catch (IOException e) {
				// Do nothing
			}
		}

		bottomPanel.revalidate();
	}

	public static JFrame alert(String msg) {

		JFrame frame = new JFrame("");// 新建窗体
		Window win = new Window(frame);// 设置圆角
		AWTUtilities.setWindowShape(win,
				new RoundRectangle2D.Double(0.0D, 0.0D, win.getWidth(), win.getHeight(), 26.0D, 26.0D));
		Color color = new Color(238, 238, 238, 100);// 透明的取值范围0~255
		frame.setAlwaysOnTop(true);// 设置窗口置顶
		frame.setLayout(new GridBagLayout());// 设置网格包布局
		frame.setUndecorated(true);// 设置无边框
		frame.setBackground(color);// 设置背景色

		JLabel label = new JLabel(msg);
		label.setForeground(Color.BLACK);
		int fontSize = 20;
		label.setFont(new Font("黑体", 0, fontSize));
		frame.setSize(msg.length() * fontSize, fontSize + 50);
		// 长度为字符大小*字符数量，宽度为字体大小+50像素
		frame.add(label);// 添加到窗体

		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		return frame;
	}

	/**
	 * 搜索、查询按钮 统一监听方法
	 * 
	 * @param panel
	 */
	private static void searchListener(JPanel panel, String tableName) {
		try {
			if (connectionToDB == null) {
				connectionToDB = new ConnectionToDB();
			}
			connectionToDB.setInfo(dbType, ip, dbName, userName, pwd);
			showTables(panel, connectionToDB.selectTables(tableName));
		} catch (Exception e) {
			// Do nothing
		}
	}

	/**
	 * 显示表名
	 * 
	 * @param panel
	 * @param list
	 *            表名集合
	 */
	private static void showTables(final JPanel panel, List<Object> list) {
		/* 清空所有控件 */
		panel.removeAll();

		/* 按表名查询 */
		JPanel selectP = new JPanel();
		selectP.setLayout(new FlowLayout(FlowLayout.LEFT));
		selectP.add(new JLabel("表名:"));
		final JTextField tableNameField = new JTextField(10);
		selectP.add(tableNameField);
		JButton searchB = new JButton("搜索");
		JButton searchC = new JButton("重置");
		searchB.setFont(new java.awt.Font("微软雅黑", 2, 10));
		searchC.setFont(new java.awt.Font("微软雅黑", 2, 10));
		selectP.add(searchB);
		selectP.add(searchC);
		results = new ResultModel(list);
		JTable table = new JTable(results);
		table.getColumnModel().getColumn(0).setMaxWidth(10);
		JScrollPane scrollPane = new JScrollPane(table); // 带滚动条的面板

		panel.add(selectP, BorderLayout.NORTH);
		panel.add(scrollPane, BorderLayout.CENTER);
		panel.revalidate(); // 刷新控件内容

		/* 搜索按钮监听 */
		searchB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				searchListener(panel, tableNameField.getText());
			}
		});
		/* 重置按钮监听 */
		searchC.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				searchListener(panel, null);
			}
		});
	}

	public static void main(String[] args) {
		new DBInfoWindow();
	}
}
