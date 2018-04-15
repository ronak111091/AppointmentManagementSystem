package appmgmtsys.main;

import appmgmtsys.business.BusinessLayer;

public class Application {
	public static void main(String[] args) {
		BusinessLayer businessLayer = new BusinessLayer();
//		System.out.println(businessLayer.getAllAppointments());
//		System.out.println(businessLayer.getAppointment("210"));
		System.out.println(businessLayer.addAppointment(""));
	}
}
