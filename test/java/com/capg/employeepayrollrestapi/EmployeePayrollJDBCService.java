package com.capg.employeepayrollrestapi;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmployeePayrollJDBCService {
	private int connectionCounter=0;
	private static EmployeePayrollJDBCService employeePayrollDBService;
	private PreparedStatement prepareStatement;
	private PreparedStatement employeePayrollDataStatement;

	public EmployeePayrollJDBCService() {
	}

	public static EmployeePayrollJDBCService getInstance() {
		if (employeePayrollDBService == null) {
			employeePayrollDBService = new EmployeePayrollJDBCService();
		}
		return employeePayrollDBService;
	}

	public synchronized Connection getConnection() throws ServiceException  {
		connectionCounter++;
		final String URL = "jdbc:mysql://localhost:3306/payroll_service?allowPublicKeyRetrieval-true&useSSL=false";
		final String USER = "root";
		final String PASSWORD = "RajanRKG@0909";
		Connection con;
		try {
			con = DriverManager.getConnection(URL, USER, PASSWORD);
		} catch (SQLException e) {
			throw new ServiceException ("Bad Connection");
		}
		return con;
	}

	public List<EmployeePayrollData> readData() throws ServiceException  {
		String query = "select * from employee_payroll; ";
		try (Connection con = this.getConnection()) {
			Statement statement = con.createStatement();
			ResultSet resultSet = statement.executeQuery(query);
			return this.getEmployeePayrollListFromResultset(resultSet);
		} catch (SQLException e) {
			throw new ServiceException ("No Data Found");
		}
	}

	private List<EmployeePayrollData> getEmployeePayrollListFromResultset(ResultSet resultSet) throws ServiceException  {
		List<EmployeePayrollData> employeePayrollList = new ArrayList<EmployeePayrollData>();
		try {
			while (resultSet.next()) {
				int id = resultSet.getInt(1);
				String name = resultSet.getString(2);
				String gender = resultSet.getString(3);
				double salary = resultSet.getDouble(4);
				LocalDate start = resultSet.getDate(5).toLocalDate();
				employeePayrollList.add(new EmployeePayrollData(id, name, salary, gender, start));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return employeePayrollList;
	}

	public int updateEmployeeDataUsingStatement(String name, double salary) throws ServiceException  {
		Connection connection = this.getConnection();
		int empId,result = 0;
		try {
			connection.setAutoCommit(false);
		} catch (SQLException e2) {
		}
		try (Statement statement = connection.createStatement();){
			String query = String.format("update employee_payroll set salary = %.2f where name = '%s';", salary, name);
			statement.executeUpdate(query);
		} catch (SQLException e) {
			try {
				connection.rollback();
			} catch (SQLException e1) {
			}
		}
		try(Statement statement = connection.createStatement()){
			String query = String.format("select id from employee_payroll where name = %s", name);
			ResultSet resultSet = statement.executeQuery(query);
			empId = resultSet.getInt(1);
		}
		catch(SQLException e)
		{
			try {
				connection.rollback();
			} catch (SQLException e1) {
			}
		}
		try(Statement statement = connection.createStatement()){
			double deductions = salary * 0.2;
			double taxablePay = salary - deductions;
			double tax = taxablePay * 0.1;
			double netPay = salary - tax;
			String query = String.format("update payroll_details set basic_pay = %s,"
					+ "deductions = %s, taxable_pay = %s, tax = %s, net_pay = %s where employee_id = %s",
					deductions, taxablePay, tax, netPay);
			result = statement.executeUpdate(query);
			return result;
		} catch(SQLException e) {
			try {
				connection.rollback();
			} catch (SQLException e1) {
			}
		}
		try {
			
			connection.commit();
		} catch (SQLException e) {
		}
		return result;
	}

	public synchronized int updateEmployeePayrollDataUsingPreparedStatement(String name, double salary)
			throws ServiceException  {
		if (this.prepareStatement == null) {
			this.prepareStatementForEmployeePayroll();
		}
		try {
			prepareStatement.setDouble(1, salary);
			prepareStatement.setString(2, name);
			int result = prepareStatement.executeUpdate();
			return result;
		} catch (SQLException e) {
			throw new ServiceException ("Updationerror");
		}
	}

	private void prepareStatementForEmployeePayroll() throws ServiceException  {
		try {
			Connection con = this.getConnection();
			String query = "update employee_payroll set salary = ? where name = ?";
			this.prepareStatement = con.prepareStatement(query);
		} catch (SQLException e) {
			throw new ServiceException ("error");
		}
	}

	public synchronized List<EmployeePayrollData> getEmployeePayrollDataFromDB(String name) throws ServiceException  {
		if (this.employeePayrollDataStatement == null) {
			this.prepareStatementForEmployeePayrollDataRetrieval();
		}
		try (Connection con = this.getConnection()) {
			this.employeePayrollDataStatement.setString(1, name);
			ResultSet resultSet = employeePayrollDataStatement.executeQuery();
			return this.getEmployeePayrollListFromResultset(resultSet);
		} catch (SQLException e) {
			throw new ServiceException ("error");
		}
	}

	private void prepareStatementForEmployeePayrollDataRetrieval() throws ServiceException  {
		try {
			Connection con = this.getConnection();
			String sql = "select * from employee_payroll where name=?";
			this.employeePayrollDataStatement = con.prepareStatement(sql);
		} catch (SQLException e) {
			throw new ServiceException ("error");
		}
	}

	public List<EmployeePayrollData> getEmployeePayrollDataByStartingDate(LocalDate startDate, LocalDate endDate)
			throws ServiceException  {
		String query = String.format("select * from employee_payroll where start between cast('%s' as date) and cast('%s' as date);",
				startDate.toString(), endDate.toString());
		try (Connection con = this.getConnection()) {
			Statement statement = con.createStatement();
			ResultSet resultSet = statement.executeQuery(query);
			return this.getEmployeePayrollListFromResultset(resultSet);
		} catch (SQLException e) {
			throw new ServiceException ("Connection Failed.");
		}
	}

	public Map<String, Double> empDataOperations(String column, String operation)
			throws ServiceException  {
		String query = String.format("select gender , %s(%s) from employee_payroll group by gender;", operation, column);
		Map<String, Double> empData = new HashMap<>();
		try (Connection connection = this.getConnection()) {
			PreparedStatement preparedStatement = connection.prepareStatement(query);
			ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				empData.put(resultSet.getString(1), resultSet.getDouble(2));
			}
		} catch (SQLException e) {
			throw new ServiceException ("error");
		}
		return empData;
	}

	public EmployeePayrollData addEmployeePayroll(String name, double salary, LocalDate startdate, String gender) throws ServiceException  {
		int empId = -1;
		EmployeePayrollData employeePayrollData = null;
		String query = String.format("insert into employee_payroll values('%s','%s','%s','%s')", name, gender,salary, Date.valueOf(startdate));
		try (Connection con = this.getConnection()) {
			Statement statement = con.createStatement();
			int result = statement.executeUpdate(query, statement.RETURN_GENERATED_KEYS);
			if (result == 1) {
				ResultSet resultSet = statement.getGeneratedKeys();
				if (resultSet.next())
					empId = resultSet.getInt(1);
			}
			employeePayrollData = new EmployeePayrollData(empId, name, salary, gender, startdate);
		} catch (SQLException e) {
		}
		return employeePayrollData;
	}

	public EmployeePayrollData addEmployeeToPayroll(String name, double salary, LocalDate startdate, String gender) throws ServiceException  {
		int result = -1;
		Connection con= null;
		EmployeePayrollData employeePayrollData = null;
		try {
			con = this.getConnection();
			con.setAutoCommit(false);
		} catch (Exception e) {
			throw new ServiceException ("Error");
		}
		try (Statement statement = con.createStatement()) {
			String query = String.format("insert into employee_payroll values ('%s','%s','%s','%s')", name,gender, salary, Date.valueOf(startdate));
			int rowsAffected = statement.executeUpdate(query, statement.RETURN_GENERATED_KEYS);
			if (rowsAffected == 1) {
				ResultSet resultSet = statement.getGeneratedKeys();
				if (resultSet.next())
					result = resultSet.getInt(1);
			}
			employeePayrollData = new EmployeePayrollData(result, name, salary, gender, startdate);
		} catch (SQLException e) {
			try {
				con.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			throw new ServiceException ("Could Not Add");
		}
		try (Statement statement = con.createStatement()) {
			double deductions = salary * 0.2;
			double taxablePay = salary - deductions;
			double tax = taxablePay * 0.1;
			double netPay = salary - tax;
			String query = String.format("insert into payroll_details VALUES ('%s','%s','%s','%s','%s','%s')",result, salary, deductions, taxablePay, tax, netPay);
			int result1 = statement.executeUpdate(query);
			if (result1 == 1)
				employeePayrollData = new EmployeePayrollData(result1, name, salary, gender, startdate);
		} catch (SQLException e) {
			try {
				con.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			throw new ServiceException ("Not Able to add");
		}
		try {
			con.commit();
		} catch (SQLException e1) {
			e1.printStackTrace();
		} finally {
			if (con != null)
				try {
					con.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
		return employeePayrollData;
	}

	public EmployeePayrollData addNewEmployee(int id, String name, String gender, String phone_no, String address,
			Date date, double salary, String comp_name, int comp_id, String department[], int dept_id[]) throws ServiceException  {
		int empId = 0;
		EmployeePayrollData employeePayrollData = null;
		Connection con= null;
		try {
			con = this.getConnection();
			con.setAutoCommit(false);
			Statement statement = con.createStatement();
			List<EmployeePayrollData> dataList = this.readData();
			boolean isInserted = true;
			for (EmployeePayrollData e : dataList) {
				if (e.getId() == comp_id) {
					isInserted = false;
					break;
				}
			}
			if (isInserted) {
				String query2 = String.format("insert into company values (%s, '%s')", comp_id, comp_name);
				statement.executeUpdate(query2);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			Statement statement = con.createStatement();
			List<EmployeePayrollData> empDataList = this.readData();
			Integer[] departmentArr;
			List<Integer> idList = new ArrayList<>();
			if (empDataList != null) {
				String query3 = "select * from department";
				ResultSet resultSet = statement.executeQuery(query3);
				while (resultSet.next()) {
					int d_id = resultSet.getInt(1);
					idList.add(d_id);
				}
				departmentArr = idList.toArray(new Integer[0]);
			}

			for (int i = 0; i < dept_id.length; i++) {
				boolean toInsert = true;
				for (Integer dep : idList) {
					if (dept_id[i] == dep) {
						toInsert = false;
						break;
					}
				}
				if (toInsert == true) {
					Statement statement_d = con.createStatement();
					String query4 = String.format("insert into department values (%s,'%s')", dept_id[i],
							department[i]);
					statement_d.executeUpdate(query4);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			Statement stat = con.createStatement();
			String sql = String.format("insert into employee values (%s,%s,'%s','%s'", id, name, gender, date);
			int rowAffected = stat.executeUpdate(sql, stat.RETURN_GENERATED_KEYS);
		} catch (Exception e) {
			try {
				con.rollback();
			} catch (SQLException e1) {
				throw new ServiceException ("error");
			}
			return employeePayrollData;
		}
		double deductions = salary * 0.2;
		double taxable_pay = salary - deductions;
		double tax = taxable_pay * 0.1;
		double net_pay = taxable_pay - tax;
		String sql_salary = String.format("insert into payroll values (%s,%s,%s,%s,%s,%s)", id, salary, deductions,
				taxable_pay, tax, net_pay);
		try {
			Statement statSal = con.createStatement();
			int result = statSal.executeUpdate(sql_salary, statSal.RETURN_GENERATED_KEYS);
			if (result == 1) {
				ResultSet resultSet = statSal.getGeneratedKeys();
				if (resultSet.next())
					empId = resultSet.getInt(1);
				employeePayrollData = new EmployeePayrollData(empId, name, salary);
			}
		} catch (Exception e) {
			try {
				con.rollback();
			} catch (SQLException e1) {
				throw new ServiceException ("error2");
			}
		}
		try {
			Statement statement = con.createStatement();
			for (int i = 0; i < dept_id.length; i++) {
				String sql_emp_department = String.format("insert into department values (%s,%s)", id, dept_id[i]);
				statement.executeUpdate(sql_emp_department);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				con.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
		try {
			con.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return employeePayrollData;
	}

	public void removeEmployee(String name) throws ServiceException {
		String query = String.format("delete from employee where name=%s", name);
		Connection connection = this.getConnection();
		try {
			Statement statement = connection.createStatement();
			statement.execute(query);
		} catch (Exception e) {
		}

	}

}
