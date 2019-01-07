package com.java.model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CreateModel {

	private static final String ENTER = "\r\n"; // 换行符
	// 是否含有BigDecimal 类型字段;用于判断是否导包
	private boolean hasBigDecimal = false;
	private String modelName; // model名
	private String packageName; // 包路径
	private String path; // 文件输出路径
	// Map集合:存储列名及该列对应的java数据类型
	private Map<String, List<ColumnModel>> columnMap = new LinkedHashMap<>();

	/**
	 * 创建Bean
	 * 
	 * @param path
	 *            输出文件地址
	 * @param string
	 * @param map
	 *            <表名, List<列名>>
	 */
	public Map<String, Object> createBean(String path, String packageName, Map<String, List<ColumnModel>> map) {
		char charAt = path.charAt(path.length() - 1);
		if (charAt == '\\' || charAt == '/') {
			path = path.substring(0, path.length() - 1);
		}
		this.path = path;
		this.packageName = packageName;
		this.columnMap = map;

		/* 输出Model文件 */
		return outputModel();
	}

	// 输出Model文件
	private Map<String, Object> outputModel() {
		Map<String, Object> result = new HashMap<>();
		result.put("isSuccess", false);
		if (columnMap != null) {
			Iterator<String> iterator = columnMap.keySet().iterator();
			while (iterator.hasNext()) {
				String tableName = iterator.next();
				modelName = changeUpper(tableName.substring(tableName.indexOf('_') + 1, tableName.length()), true);
				File dir = new File(path + File.separator + modelName.toLowerCase() + File.separator + "model");
				File file = new File(dir.getPath() + File.separator + modelName + "Model.java");
				dir.mkdirs();
				try (BufferedWriter bw = new BufferedWriter(new FileWriter(file));) {
					String outStr = getModelOutStr(tableName);
					String importStr = getModelImportStr();
					bw.write(new StringBuilder(importStr).append(outStr).toString());
					// 生成是否成功
					result.put("isSuccess", outputMapper() && outputService() && outputServiceImpl() && outputAction());
					// 生成后的地址
					result.put("path", path + File.separator + modelName.toLowerCase());
				} catch (IOException e) {
					result.put("isSuccess", false);
				}
			}
		}
		return result;
	}

	/**
	 * 获取引入包的语句
	 * 
	 * @return
	 */
	private String getModelImportStr() {
		// 待输出字符串
		StringBuilder importStr = new StringBuilder("");
		importStr.append("package " + packageName + "." + modelName.toLowerCase() + ".model;");
		importStr.append(ENTER);
		importStr.append(ENTER);
		// 判断是否需要引入 BigDecimal 包
		if (hasBigDecimal) {
			importStr.append("import java.math.BigDecimal;");
			importStr.append(ENTER);
			importStr.append(ENTER);
		}
		return importStr.toString();
	}

	/**
	 * 获取Model bean语句
	 * 
	 * @param tableName
	 * @return
	 */
	private String getModelOutStr(String tableName) {
		StringBuilder outStr = new StringBuilder("");
		outStr.append("import javax.persistence.Column;");
		outStr.append(ENTER);
		outStr.append("import javax.persistence.Id;");
		outStr.append(ENTER);
		outStr.append("import javax.persistence.Table;");
		outStr.append(ENTER);
		outStr.append(ENTER);
		outStr.append("import com.ronhe.core.annotation.Tenant;");
		outStr.append(ENTER);
		outStr.append("import com.ronhe.core.framework.model.BaseModel;");
		outStr.append(ENTER);
		outStr.append(ENTER);
		outStr.append("@Table(name = \"" + tableName.toUpperCase() + "\")");
		outStr.append(ENTER);
		outStr.append("public class " + modelName + "Model extends BaseModel {");
		outStr.append(ENTER);
		// 追加类型和属性名
		List<ColumnModel> columnModels = columnMap.get(tableName);
		for (ColumnModel columnModel : columnModels) {
			String key = columnModel.getColumnName().toLowerCase();
			if ("create_user".equals(key) || "create_date".equals(key) || "update_user".equals(key)
					|| "update_date".equals(key)) {
				continue;
			}
			if ("id".equalsIgnoreCase(key)) {
				outStr.append("\t@Id");
				outStr.append(ENTER);
			}
			outStr.append("\t@Column(name = \"" + key.toUpperCase() + "\")");
			outStr.append(ENTER);
			outStr.append("\tprivate " + changeType(columnModel.getColumnType()) + " " + changeUpper(key, false) + ";");
			outStr.append(ENTER);
		}
		outStr.append(ENTER);
		outStr.append(getModelGetterAndSetter(tableName));
		outStr.append("}");
		return outStr.toString();
	}

	/**
	 * 获取 Model 属性的getter and setter
	 * 
	 * @param tableName
	 * @return
	 */
	private String getModelGetterAndSetter(String tableName) {
		StringBuilder str = new StringBuilder("");
		List<ColumnModel> columnModels = columnMap.get(tableName);
		for (ColumnModel columnModel : columnModels) {
			String key = columnModel.getColumnName().toLowerCase();
			if ("create_user".equals(key) || "create_date".equals(key) || "update_user".equals(key)
					|| "update_date".equals(key)) {
				continue;
			}
			str.append(
					"\tpublic " + changeType(columnModel.getColumnType()) + " get" + changeUpper(key, true) + "() {");
			str.append(ENTER);
			str.append("\t\treturn " + changeUpper(key, false) + ";");
			str.append(ENTER);
			str.append("\t}");
			str.append(ENTER);
			str.append(ENTER);
			str.append("\tpublic void set" + changeUpper(key, true) + "(" + changeType(columnModel.getColumnType())
					+ " " + changeUpper(key, false) + ") {");
			str.append(ENTER);
			str.append("\t\tthis." + changeUpper(key, false) + " = " + changeUpper(key, false) + ";");
			str.append(ENTER);
			str.append("\t}");
			str.append(ENTER);
			str.append(ENTER);
		}

		return str.toString();
	}

	// 输出Mapper文件
	private boolean outputMapper() {
		File dir = new File(path + File.separator + modelName.toLowerCase() + File.separator + "dao");
		File file = new File(dir.getPath() + File.separator + modelName + "Mapper.java");
		if (!dir.exists()) {
			dir.mkdirs();
		}
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(file));) {
			StringBuilder outStr = new StringBuilder();
			outStr.append("package " + packageName + "." + modelName.toLowerCase() + ".dao;");
			outStr.append(ENTER);
			outStr.append(ENTER);
			outStr.append("import com.ronhe.core.framework.dao.BaseMapper;");
			outStr.append(ENTER);
			outStr.append("import " + packageName + "." + modelName.toLowerCase() + ".model." + modelName + "Model;");
			outStr.append(ENTER);
			outStr.append(ENTER);
			outStr.append("public interface " + modelName + "Mapper extends BaseMapper<" + modelName + "Model> {");
			outStr.append(ENTER);
			outStr.append(ENTER);
			outStr.append("}");

			bw.write(outStr.toString());
			return true;
		} catch (IOException e) {
			// Do nothing
		}
		return false;
	}

	// 输出Service文件
	private boolean outputService() {
		File dir = new File(path + File.separator + modelName.toLowerCase() + File.separator + "service");
		File file = new File(dir.getPath() + File.separator + modelName + "Service.java");
		if (!dir.exists()) {
			dir.mkdirs();
		}
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(file));) {
			StringBuilder outStr = new StringBuilder();
			outStr.append("package " + packageName + "." + modelName.toLowerCase() + ".service;");
			outStr.append(ENTER);
			outStr.append(ENTER);
			outStr.append("import com.ronhe.core.framework.service.BaseService;");
			outStr.append(ENTER);
			outStr.append("import " + packageName + "." + modelName.toLowerCase() + ".model." + modelName + "Model;");
			outStr.append(ENTER);
			outStr.append(ENTER);
			outStr.append("public interface " + modelName + "Service extends BaseService<" + modelName + "Model> {");
			outStr.append(ENTER);
			outStr.append(ENTER);
			outStr.append("}");

			bw.write(outStr.toString());
			return true;
		} catch (IOException e) {
			// Do nothing
		}
		return false;
	}

	// 输出ServiceImpl文件
	private boolean outputServiceImpl() {
		File dir = new File(
				path + File.separator + modelName.toLowerCase() + File.separator + "service" + File.separator + "impl");
		File file = new File(dir.getPath() + File.separator + modelName + "ServiceImpl.java");
		if (!dir.exists()) {
			dir.mkdirs();
		}
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(file));) {
			StringBuilder outStr = new StringBuilder();
			outStr.append("package " + packageName + "." + modelName.toLowerCase() + ".service.impl;");
			outStr.append(ENTER);
			outStr.append(ENTER);
			outStr.append("import org.springframework.beans.factory.annotation.Autowired;");
			outStr.append(ENTER);
			outStr.append("import org.springframework.stereotype.Service;");
			outStr.append(ENTER);
			outStr.append(ENTER);
			outStr.append("import com.ronhe.core.framework.service.impl.BaseServiceImpl;");
			outStr.append(ENTER);
			outStr.append("import " + packageName + "." + modelName.toLowerCase() + ".dao." + modelName + "Mapper;");
			outStr.append(ENTER);
			outStr.append("import " + packageName + "." + modelName.toLowerCase() + ".model." + modelName + "Model;");
			outStr.append(ENTER);
			outStr.append(
					"import " + packageName + "." + modelName.toLowerCase() + ".service." + modelName + "Service;");
			outStr.append(ENTER);
			outStr.append(ENTER);
			String name = modelName.substring(0, 1).toLowerCase() + modelName.substring(1, modelName.length());
			outStr.append("@Service(\"" + name + "Service\")");
			outStr.append(ENTER);
			outStr.append("public class " + modelName + "ServiceImpl extends BaseServiceImpl<" + modelName
					+ "Model> implements " + modelName + "Service {");
			outStr.append(ENTER);
			outStr.append(ENTER);
			outStr.append("\t@Autowired");
			outStr.append(ENTER);
			outStr.append("\tprivate " + modelName + "Mapper " + name + "Mapper;");
			outStr.append(ENTER);
			outStr.append(ENTER);
			outStr.append("}");
			bw.write(outStr.toString());
			return true;
		} catch (IOException e) {
			// Do nothing
		}
		return false;
	}

	// 输出Action文件
	private boolean outputAction() {
		File dir = new File(path + File.separator + modelName.toLowerCase() + File.separator + "action");
		File file = new File(dir.getPath() + File.separator + modelName + "Action.java");
		if (!dir.exists()) {
			dir.mkdirs();
		}
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(file));) {
			StringBuilder outStr = new StringBuilder();
			outStr.append("package " + packageName + "." + modelName.toLowerCase() + ".action;");
			outStr.append(ENTER);
			outStr.append(ENTER);
			outStr.append("import org.springframework.beans.factory.annotation.Autowired;");
			outStr.append(ENTER);
			outStr.append("import org.springframework.stereotype.Controller;");
			outStr.append(ENTER);
			outStr.append("import org.springframework.web.bind.annotation.RequestMapping;");
			outStr.append(ENTER);
			outStr.append(ENTER);
			outStr.append("import com.ronhe.core.framework.web.BaseAction;");
			outStr.append(ENTER);
			outStr.append("import " + packageName + "." + modelName.toLowerCase() + ".model." + modelName + "Model;");
			outStr.append(ENTER);
			outStr.append(
					"import " + packageName + "." + modelName.toLowerCase() + ".service." + modelName + "Service;");
			outStr.append(ENTER);
			outStr.append(ENTER);
			outStr.append("@Controller");
			outStr.append(ENTER);
			outStr.append("@RequestMapping(\"\")");
			outStr.append(ENTER);
			outStr.append("public class " + modelName + "Action extends BaseAction<" + modelName + "Model> {");
			outStr.append(ENTER);
			outStr.append(ENTER);
			outStr.append("\t@Autowired");
			outStr.append(ENTER);
			String name = modelName.substring(0, 1).toLowerCase() + modelName.substring(1, modelName.length());
			outStr.append("\tprivate " + modelName + "Service " + name + "Service;");
			outStr.append(ENTER);
			outStr.append(ENTER);
			outStr.append("}");

			bw.write(outStr.toString());
			return true;
		} catch (IOException e) {
			// Do nothing
		}
		return false;
	}

	/**
	 * 将字符串某些字符转为大写
	 * 
	 * @param string
	 *            待转换字符串
	 * @param flag
	 *            是否首字母大写
	 * @return 转换后的字符串
	 */
	private static String changeUpper(String string, boolean flag) {
		string = string.toLowerCase();
		StringBuilder str = new StringBuilder("");
		char[] array = string.toCharArray();
		// 字母是否大写
		boolean isToUpper = false;
		str.append(flag ? String.valueOf(array[0]).toUpperCase() : String.valueOf(array[0]));
		for (int i = 1; i < array.length; i++) {
			if ('_' == array[i]) {
				// 下划线后一个字母大写
				isToUpper = true;
				continue;
			}

			if (isToUpper) {
				str.append(String.valueOf(array[i]).toUpperCase());
				isToUpper = false;
			} else {
				str.append(String.valueOf(array[i]));
			}

		}
		return str.toString();
	}

	/**
	 * 数据库数据类型转java数据类型
	 * 
	 * @param str
	 * @return
	 */
	private String changeType(String str) {
		str = str.toLowerCase();
		if (str.contains("char") || str.contains("clob") || str.contains("blob") || str.contains("text")) {
			return "String";
		} else if (str.contains("integer") || str.contains("int")) {
			return "Integer";
		} else if (str.contains("numeric") || str.contains("number") || str.contains("decimal")) {
			hasBigDecimal = true;
			return "BigDecimal";
		} else {
			return null;
		}
	}
}
