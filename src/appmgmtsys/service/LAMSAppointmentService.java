package appmgmtsys.service;

import javax.jws.WebMethod;
import javax.jws.WebService;

import appmgmtsys.business.LAMBusinessLayer;

@WebService
public class LAMSAppointmentService {
	
	private LAMBusinessLayer businessLayer = new LAMBusinessLayer();
	
	@WebMethod
	public String initialize() {
		return businessLayer.initialize();
	}
	
	@WebMethod
	public String getAllAppointments() {
		try {
			return businessLayer.getAllAppointments();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return businessLayer.createXMLErrorString("An error occurred while processing the request");
		}
	}
	
	@WebMethod
	public String getAppointment(String appointmentId) {
		try {
			return businessLayer.getAppointment(appointmentId);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return businessLayer.createXMLErrorString("An error occurred while processing the request");
		}
	}
	
	@WebMethod
	public String addAppointment(String input) {
		try {
			return businessLayer.addAppointment(input);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return businessLayer.createXMLErrorString("An error occurred while processing the request");
		} 
	}
}
