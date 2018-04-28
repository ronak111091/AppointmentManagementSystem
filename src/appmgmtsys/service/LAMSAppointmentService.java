package appmgmtsys.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import appmgmtsys.business.LAMBusinessLayer;

@Path("/Services")
public class LAMSAppointmentService {
	
	private LAMBusinessLayer businessLayer = new LAMBusinessLayer();
	
	@Context
	private UriInfo context;
	
	@GET
	@Produces("application/xml")
	public String initialize() {
		businessLayer.initialize();
		return businessLayer.createXMLInitializeString(this.context.getBaseUri().toString());
	}
	
	@Path("/Appointments")
	@GET
	@Produces("application/xml")
	public String getAllAppointments() {
		try {
			return businessLayer.getAllAppointments();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return businessLayer.createXMLErrorString("An error occurred while processing the request");
		}
	}
	
	@Path("/Appointments/{appointment}")
	@GET
	@Produces("application/xml")
	public String getAppointment(@PathParam("appointment")String appointmentId) {
		try {
			return businessLayer.getAppointment(appointmentId);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return businessLayer.createXMLErrorString("An error occurred while processing the request");
		}
	}
	
	@Path("/Appointments")
	@PUT
	@Consumes({"text/xml","application/xml"})
	@Produces("application/xml")
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
