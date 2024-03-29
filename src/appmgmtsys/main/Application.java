package appmgmtsys.main;

import java.sql.Date;
import java.sql.Time;
import java.text.SimpleDateFormat;

import appmgmtsys.business.LAMBusinessLayer;
import appmgmtsys.util.AppointmentUtil;

public class Application {
	public static void main(String[] args) {
		LAMBusinessLayer businessLayer = new LAMBusinessLayer();
		
		String addAppointmentInput = "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\"?>"
				+ "<appointment>"
				+ "<date>2018-12-28</date>"
				+ "<time>09:00</time>"
				+ "<patientId>220</patientId>"
				+ "<physicianId>20</physicianId>"
				+ "<pscId>520</pscId>"
				+ "<phlebotomistId>110</phlebotomistId>"
				+ "<labTests>"
				+ "<test id=\"86900\" dxcode=\"292.9\"/>"
				+ "<test id=\"86609\" dxcode=\"307.3\"/>"
				+ "</labTests>"
				+ "</appointment>";
		
		try {
			System.out.println(addAppointmentInput);
//			System.out.println(businessLayer.getAllAppointments());
//			System.out.println(businessLayer.getAppointment("770"));
			System.out.println(businessLayer.addAppointment(addAppointmentInput));
//			System.out.println(AppointmentUtil.getDateObj("1991-10-2").toString());
//			System.out.println(AppointmentUtil.getTimeObj("11:30:01").toString());
//			Time time = AppointmentUtil.getTimeObj("17:00");
//			System.out.println(businessLayer.checkAppointmentConflict(AppointmentUtil.getDateObj("2017-02-02"),AppointmentUtil.getTimeObj("12:30:00"),"110","520"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
