package com.capg.employeepayrollrestapi;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Objects;

public class EmployeePayrollData {

		private int id;
		private String name;
		private double salary;
		private LocalDate start;
		private String gender;
		private String company_name;
		private String department[];
		private int department_id[];
		public EmployeePayrollData(int id, String name, double salary, LocalDate start, String gender, String company_name,
				String[] department,int[] department_id) {
			super();
			this.id = id;
			this.name = name;
			this.salary = salary;
			this.start = start;
			this.gender = gender;
			this.company_name = company_name;
			this.department = department;
			this.department_id=department_id;
		}

		public EmployeePayrollData(int id, String name, double salary, String gender) {
			this.id = id;
			this.name = name;
			this.salary = salary;
			this.gender = gender;
		}
		
		public EmployeePayrollData(int id, String name, double salary, String gender, LocalDate start) {
			this(id,name,salary,gender);
			this.start=start;
		}
		public EmployeePayrollData(int id, String name, double salary) {
			this.id = id;
			this.name = name;
			this.salary = salary;
		}

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public double getSalary() {
			return salary;
		}

		public void setSalary(double salary) {
			this.salary = salary;
		}

		public String getGender() {
			return gender;
		}

		public void setGender(String gender) {
			this.gender = gender;
		}

		public LocalDate getStart() {
			return start;
		}

		public void setStart(LocalDate start) {
			this.start = start;
		}
		
		@Override
		public  int hashCode()
		{
			return Objects.hash(name,gender,salary,start);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			EmployeePayrollData other = (EmployeePayrollData) obj;
			if (id != other.id)
				return false;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			if (Double.doubleToLongBits(salary) != Double.doubleToLongBits(other.salary))
				return false;
			if (start == null) {
				if (other.start != null)
					return false;
			} else if (!start.equals(other.start))
				return false;
			return true;
		}
		
		@Override
		public String toString() {
			return "EmployeePayrollData [id=" + id + ", name=" + name + ", salary=" + salary + ", start=" + start
					+ ", gender=" + gender + ", company_name=" + company_name + ", department="
					+ Arrays.toString(department) + "]";
		}

		
	}
