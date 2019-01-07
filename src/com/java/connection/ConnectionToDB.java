package com.java.connection;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import com.java.model.ColumnModel;

public class ConnectionToDB {
	private static final String MYSQL = "MySQL";
	private static final String ORACLE = "Oracle";
	private static final String DB2 = "DB2";
	private Properties prop = null; // 配置文件
	private String dbType; // 数据库类型:MySQL,Oracle,DB2
	private String ip; // ip地址
	private String dbName; // 数据库名
	private String userName; // 登录用户名
	private String pwd; // 密码

	public ConnectionToDB() {
		if (this.prop == null) {
			// 读取配置文件
			InputStream in;
			try {
				in = new BufferedInputStream(this.getClass().getResourceAsStream("/config.properties"));
				this.prop = new Properties();
				this.prop.load(in);
			} catch (Exception e) {
				// Do nothing
			}
		}
	}

	/**
	 * 获取数据库连接
	 * 
	 * @return
	 */
	private Connection getConnection() {
		Connection con;
		// URL指向要访问的数据库名
		String url = "";
		String prefix = ".prefix";
		switch (dbType) {
		case MYSQL:
			url = prop.getProperty(dbType + prefix) + ip + "/" + dbName
					+ "?useUnicode=true&characterEncoding=UTF-8&useSSL=false";
			break;
		case ORACLE:
			url = prop.getProperty(dbType + prefix) + ip + ":" + dbName;
			break;
		case DB2:
			url = prop.getProperty(dbType + prefix) + ip + "/" + dbName;
			break;
		default:
			break;
		}
		// 加载驱动程序
		try {
			Class.forName(prop.getProperty(dbType + ".jdbc.driver"));
			// getConnection()方法，连接MySQL数据库
			con = DriverManager.getConnection(url, userName, pwd);
			return con;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 查询表名
	 * 
	 * @param tableName
	 *            条件查询
	 * @return
	 */
	public List<Object> selectTables(String tableName) {
		// ResultSet类，用来存放获取的结果集
		ResultSet rs = null;
		Statement statement = null;
		// 要执行的SQL语句
		String sql = getSql(tableName);
		try (Connection con = getConnection();) {
			if (null != con) {
				// 创建statement类对象，并执行SQL语句
				statement = con.createStatement();
				rs = statement.executeQuery(sql);
				List<Object> tableNames = new ArrayList<>();
				while (rs.next()) {
					tableNames.add(rs.getString("NAME"));
				}
				return tableNames;
			} else {
				return Collections.emptyList();
			}

		} catch (Exception e) {
			return Collections.emptyList();
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
			} catch (SQLException e) {
				// Do nothing
			}
			try {
				if (statement != null) {
					statement.close();
				}
			} catch (SQLException e) {
				// Do nothing
			}
		}
	}

	/**
	 * 查询列名及结构
	 * 
	 * @param tableName
	 * @return
	 */
	public List<ColumnModel> selectColumns(String tableName, String dbName) {
		Statement statement = null;
		// ResultSet类，用来存放获取的结果集
		ResultSet rs = null;
		try (Connection con = getConnection();) {
			if (con != null) {
				// 创建statement类对象，用来执行SQL语句
				statement = con.createStatement();
				// 要执行的SQL语句
				StringBuilder sql = new StringBuilder(prop.getProperty(dbType + ".selectColumns"));
				switch (dbType) {
				case MYSQL:
					sql.append(" WHERE TABLE_NAME = '" + tableName + "' AND TABLE_SCHEMA = '" + dbName + "'");
					break;
				case ORACLE:
					sql.append(" WHERE TABLE_NAME = '" + tableName + "'");
					break;
				case DB2:
					sql.append(" WHERE TABNAME='" + tableName + "'");
					break;
				default:
					break;
				}
				rs = statement.executeQuery(sql.toString());
				List<ColumnModel> columnNames = new ArrayList<>();
				while (rs.next()) {
					ColumnModel columnModel = new ColumnModel();
					columnModel.setColumnName(rs.getString("NAME"));
					columnModel.setColumnType(rs.getString("TYPE"));
					columnNames.add(columnModel);
				}

				return columnNames;
			} else {

				return Collections.emptyList();
			}
		} catch (Exception e) {
			return Collections.emptyList();
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
			} catch (SQLException e) {
				// Do nothing
			}
			try {
				if (rs != null) {
					rs.close();
				}
			} catch (SQLException e) {
				// Do nothing
			}
		}
	}

	/**
	 * 创建SQL语句
	 * 
	 * @param tableName
	 * @return
	 */
	private String getSql(String tableName) {
		StringBuilder sql = new StringBuilder(prop.getProperty(dbType + ".selectTables"));
		switch (dbType) {
		case MYSQL:
			sql.append(" AND TABLE_SCHEMA='" + dbName + "'");
			if (StringUtils.isNotBlank(tableName)) {
				sql.append(" AND TABLE_NAME LIKE '%" + tableName.trim() + "%'");
			}
			break;
		case ORACLE:
			if (StringUtils.isNotBlank(tableName)) {
				sql.append(" AND TABLE_NAME LIKE '%" + tableName.trim() + "%'");
			}
			break;
		case DB2:
			if (StringUtils.isNotBlank(tableName)) {
				sql.append(" AND NAME LIKE '%" + tableName.trim() + "%'");
			}
			break;
		default:
			break;
		}
		return sql.toString();
	}

	/**
	 * 设置数据库连接信息
	 * 
	 * @param dbType
	 * @param ip
	 * @param dbName
	 * @param userName
	 * @param pwd
	 */
	public void setInfo(String dbType, String ip, String dbName, String userName, String pwd) {
		this.dbType = dbType;
		this.dbName = dbName;
		this.ip = ip;
		this.userName = userName;
		this.pwd = pwd;
	}
}
