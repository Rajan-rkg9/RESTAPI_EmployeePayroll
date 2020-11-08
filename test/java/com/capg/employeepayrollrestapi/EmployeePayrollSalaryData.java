package com.capg.employeepayrollrestapi;

import java.util.Objects;

public class EmployeePayrollSalaryData {
	public String name;
	public double salary;
	public EmployeePayrollSalaryData(String name, double salary) {
		this.name = name;
		this.salary = salary;
	}
	@Override
	public  int hashCode()
	{
		return Objects.hash(name,salary);
	}
}
