package appmgmtsys.test;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import appmgmtsys.business.LAMBusinessLayer;

public class LAMTest {
	
	private LAMBusinessLayer businessLayer = new LAMBusinessLayer();
	
	@Test
	public void testInitialize() {
		assertNotNull(businessLayer.initialize());
	}
	
	@Test
	public void testGetAllAppointments() {
		try {
			assertNotNull(businessLayer.getAllAppointments());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testGetAppointment() {
		try {
			assertNotNull(businessLayer.getAppointment("710"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testAddAppointment() {
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
			assertNotNull(businessLayer.addAppointment(addAppointmentInput));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
