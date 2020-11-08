package com.capg.employeepayrollrestapi;

import java.util.ArrayList;
import java.util.List;

public class EmployeePayrollRESTAPIService {
	List<EmployeePayrollData> payrollList;
    public EmployeePayrollRESTAPIService(List<EmployeePayrollData> empList) {
		payrollList=new ArrayList<>(empList);
	}
	public long countREST_IOEntries() {
		return payrollList.size();
	}
}
