package appmgmtsys.config;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import appmgmtsys.service.LAMSAppointmentService;

@ApplicationPath("resources")
public class ApplicationConfig extends Application{
	public Set<Class<?>> getClasses(){
		return getRestResourceClasses();
	}
	
	private Set<Class<?>> getRestResourceClasses() {
		// TODO Auto-generated method stub
		Set<Class<?>> resources = new HashSet<>();
		resources.add(LAMSAppointmentService.class);
		return resources;
	}
}
