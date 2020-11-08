package com.capg.employeepayrollrestapi;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.Gson;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class TestEmployeePayroll {
	EmployeePayrollService serviceObj;
	@BeforeClass
	public void setup()
	{
		RestAssured.baseURI="http://localhost";
		RestAssured.port=3000;
		serviceObj = new EmployeePayrollService();
	}
	public EmployeePayrollData[] getEmployeeList()
	{
		Response response=RestAssured.get("/employees");
		EmployeePayrollData empData[] = new Gson().fromJson(response.asString(),EmployeePayrollData[].class);
		return empData;
	}
    @Test
    public void givenEmployeeDatainJSONServer_WhenRetrieved_ShouldMatchCount()
    {
        EmployeePayrollData empData[] = getEmployeeList();
        EmployeePayrollRESTAPIService restApiObj;
        restApiObj=new EmployeePayrollRESTAPIService(Arrays.asList(empData));
        long count = restApiObj.countREST_IOEntries();
        assertEquals(2,count);
    }
    @Test
    public void addedNewEmployee_ShouldMatch_ResponseAndCount()
    {
    	EmployeePayrollData[] empData = getEmployeeList();
    	serviceObj=new EmployeePayrollService(Arrays.asList(empData));
    	EmployeePayrollData employeePayrollData=new EmployeePayrollData(3,"Sumit",93746.0);
    	Response response=addEmployeeToJsonServer(employeePayrollData);
    	int HTTPstatusCode=response.getStatusCode();
    	assertEquals(201,HTTPstatusCode);
    	employeePayrollData=new Gson().fromJson(response.asString(),EmployeePayrollData.class);
    	serviceObj.addEmployeeToPayrollUsingRestAPI(employeePayrollData);
    	long entries=serviceObj.entryCount();
    	assertEquals(4,entries);
    }
    public Response addEmployeeToJsonServer(EmployeePayrollData employeePayrollData) {
		String employeeJson=new Gson().toJson(employeePayrollData);
		RequestSpecification request=RestAssured.given();
		request.header("Content-Type","application/json");
		request.body(employeeJson);
		return request.post("/employees");
	}
    
    @Test
    public void addedMultipleEmployees_ShouldMatch_ResponseAndCount()
    {
    	EmployeePayrollData empData[] = getEmployeeList();
    	serviceObj=new EmployeePayrollService(Arrays.asList(empData));
    	EmployeePayrollData[] newEmpRecord= {
							    			new EmployeePayrollData(5,"Gaurav",7365454),
							    			new EmployeePayrollData(6,"Arun",7643988),
							    			new EmployeePayrollData(7,"Shambhu",8654433)};
    	for(EmployeePayrollData employeePayrollData:newEmpRecord)
    	{
    		Response response=addEmployeeToJsonServer(employeePayrollData);
    		int HTTPstatusCode=response.getStatusCode();
    		assertEquals(201,HTTPstatusCode);
    		employeePayrollData=new Gson().fromJson(response.asString(),EmployeePayrollData.class);
        	serviceObj.addEmployeeToPayrollUsingRestAPI(employeePayrollData);
    	}
    	long count = serviceObj.entryCount();
    	assertEquals(7,count);
    }
}
