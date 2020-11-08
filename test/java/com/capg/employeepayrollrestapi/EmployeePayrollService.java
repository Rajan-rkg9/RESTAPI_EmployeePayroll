package com.capg.employeepayrollrestapi;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmployeePayrollService {
	public EmployeePayrollJDBCService serviceObj;
		public EmployeePayrollService(List<EmployeePayrollData> employeeList) {
				employeePayrollList=new ArrayList<>(employeeList);
			}
		public List<EmployeePayrollData> readEmployeePayrollData() throws ServiceException {
			this.employeePayrollList = this.serviceObj.readData();
			return this.employeePayrollList;
		}
		public void updateEmployeeSalary(String name,double salary) throws ServiceException 
		{
			int result = new EmployeePayrollJDBCService().updateEmployeeDataUsingStatement(name,salary);
			if(result==0)
				return;
			EmployeePayrollData employeePayrollData=this.getEmployeePayrollData(name);
			if(employeePayrollData !=null)
				employeePayrollData.setSalary(salary);
		}
		private List<EmployeePayrollData> employeePayrollList;
		public EmployeePayrollService() {
			this.serviceObj = EmployeePayrollJDBCService.getInstance();
		}
		public long entryCount() {
			return employeePayrollList.size();
		}
		public void addEmployeeToPayrollUsingRestAPI(EmployeePayrollData employeePayrollData) {
			employeePayrollList.add(employeePayrollData);
		}
		public EmployeePayrollData getEmployeePayrollData(String name) {
			return this.employeePayrollList.stream()
										   .filter(emp->emp
										   .getName()
										   .equals(name))
										   .findFirst()
										   .orElse(null);
		}
		public List<EmployeePayrollData> getEmployeePayrollDataByStartDate(LocalDate startDate, LocalDate endDate)throws ServiceException  {
			return this.serviceObj.getEmployeePayrollDataByStartingDate(startDate, endDate);
		}

		public Map<String, Double> payrollOperationByGender(String column,String operation) throws ServiceException  {
			return this.serviceObj.empDataOperations(column,operation);
		}

		public void addEmployeeToPayroll(String name, double salary, LocalDate startdate, String gender) throws ServiceException  {
			employeePayrollList.add(serviceObj.addEmployeeToPayroll(name,salary,startdate,gender));
		}
		public void addEmployeesToPayroll(List<EmployeePayrollData> employeePayrollDataList) throws ServiceException {
			employeePayrollDataList.forEach(employeePayrollData->{
				try {
					this.addEmployeeToPayroll(employeePayrollData.getName(),employeePayrollData.getSalary(),employeePayrollData.getStart(),employeePayrollData.getGender());
				} catch (ServiceException  e) {
					e.printStackTrace();
				}
			});
		}
		public EmployeePayrollData addNewEmployee(int id, String name, String gender, String phone_no, String address,Date date, double salary, String comp_name,
												   int comp_id, String[] department, int[] dept_id) throws ServiceException{
			return EmployeePayrollJDBCService.getInstance().addNewEmployee
									(id, name, gender, phone_no, address, date, salary, comp_name, comp_id, department, dept_id);
		}
		public boolean isEmployeeDBSyncWithMemory(String name) throws ServiceException  {
			List<EmployeePayrollData> employeePayrollDataList=new EmployeePayrollJDBCService().getEmployeePayrollDataFromDB(name);
			return employeePayrollDataList.get(0).equals(getEmployeePayrollData(name));
		}
		
		public void removeEmployee(String name) throws ServiceException  {
			if (!this.isEmployeeDBSyncWithMemory(name))
				throw new ServiceException ("errror");
			EmployeePayrollJDBCService.getInstance().removeEmployee(name);
		}
		public void addEmployeesToPayrollWithThreads(List<EmployeePayrollData> employeePayrollDataList) {
			Map<Integer,Boolean> addStatus = new HashMap<Integer,Boolean>();
			employeePayrollDataList.forEach(empObject->
			{
				Runnable task=()->{
					addStatus.put(empObject.hashCode(),false);
					try {
						this.addEmployeeToPayroll(empObject.getName(),empObject.getSalary(),empObject.getStart(),empObject.getGender());
					} catch (Exception e) {
						e.printStackTrace();
					}
					addStatus.put(empObject.hashCode(),true);
				};
				Thread thread=new Thread(task,empObject.getName());
				thread.start();
			});
			while(addStatus.containsValue(false))
			{
				try {
					Thread.sleep(10);
				}
				catch(InterruptedException e) {}
			}
		}
		public int countEntries() {
			return employeePayrollList.size();
		}
		public void updateSalary(List<EmployeePayrollSalaryData> employeeNameAndSalaryList) {
			Map<Integer, Boolean> addStatus = new HashMap<>();
			employeeNameAndSalaryList.forEach(empObject ->
			{
				Runnable task = () -> {
					addStatus.put(empObject.hashCode(), false);
					try {
						this.updateEmployeeSalary(empObject.name, empObject.salary);
					} catch (Exception e) {
					}
					addStatus.put(empObject.hashCode(), true);
				};
				Thread thread = new Thread(task, empObject.name);
				thread.start();
			}
					);
			while(addStatus.containsValue(false)) {
				try {
					Thread.sleep(100);
				} catch(InterruptedException e) {}
			}
		}
		public void updateSalaryUsingRestAPI(String name, double salary) {
			EmployeePayrollData empDataObj = this.getEmployeePayrollData(name);
			if(empDataObj!=null)
				empDataObj.setSalary(salary);
		}
}
