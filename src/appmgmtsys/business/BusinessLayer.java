package appmgmtsys.business;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import appmgmtsys.data.DBSingleton;
import components.data.Appointment;
import components.data.AppointmentLabTest;
import components.data.Diagnosis;
import components.data.LabTest;
import components.data.PSC;
import components.data.Patient;
import components.data.Phlebotomist;

public class BusinessLayer {
	
	private DBSingleton dbSingleton;
	
	public BusinessLayer() {
		super();
		initialize();
	}

	public String initialize() {
		dbSingleton = DBSingleton.getInstance();
		dbSingleton.db.initialLoad("LAMS");
		return "database initialized!";
	}
	
	public String getAllAppointments() {
        System.out.println("All appointments");
        StringBuilder sb = new StringBuilder();
        List<Object> objs = dbSingleton.db.getData("Appointment", "");
        if(!objs.isEmpty()) {
        	sb.append("[");
            for (Object obj : objs){
            	sb.append("{");
                Appointment a = (Appointment) obj;
                sb.append(a.toString());
                sb.append("}\n");
            }
            sb.append("]");
            return sb.toString();
        }else {
        	initialize();
        	return getAllAppointments();
        }
	}
	
	public String getAppointment(String appointmentNo) {
		List<Object> objs = dbSingleton.db.getData("Appointment", "patientid='"+appointmentNo+"'");
		StringBuilder sb = new StringBuilder();	
		Patient patient = null;
		Phlebotomist phleb = null;
		PSC psc = null;
		if(!objs.isEmpty()) {
			sb.append("[");
			for (Object obj : objs) {
				sb.append("{");
				Appointment a = (Appointment) obj;
				//they are used for adding an appointment late?
//				patient = a.getPatientid();
//				phleb = a.getPhlebid();
//				psc = a.getPscid();
				sb.append(a.toString());
				sb.append("}\n");
			}
			sb.append("]");
			return sb.toString();
		}else {
			return "ERROR: No appointment found for "+appointmentNo;
		}
	}
	
	public String addAppointment(String xmlStyle) {
		  //these ids would be from the xml...
		Patient patient = getPatient("210");      
		Phlebotomist phleb = getPhleb("100");      
		PSC psc = getPSC("500");
        Appointment newAppt = new Appointment("800",java.sql.Date.valueOf("2009-09-01"),java.sql.Time.valueOf("10:15:00"));
        //extra steps here due to persistence api and join, need to create objects in list
        List<AppointmentLabTest> tests = new ArrayList<AppointmentLabTest>();
        AppointmentLabTest test = new AppointmentLabTest("800","86900","292.9");
        test.setDiagnosis((Diagnosis)dbSingleton.db.getData("Diagnosis", "code='292.9'").get(0));
        test.setLabTest((LabTest)dbSingleton.db.getData("LabTest","id='86900'").get(0));
        tests.add(test);
        newAppt.setAppointmentLabTestCollection(tests);
        newAppt.setPatientid(patient);
        newAppt.setPhlebid(phleb);
        newAppt.setPscid(psc);
        boolean good = dbSingleton.db.addData(newAppt);
        if(good) {
        	 List<Object> objs = dbSingleton.db.getData("Appointment", "id='800'");
             String result = "";
             Appointment a = null;
             for (Object obj : objs){
             	a = (Appointment) obj;
                result+=a.toString()+"\n";
             }
     		return result;
        }else {
        	return "ERROR creating appointment!";
        }
       
	}

	private PSC getPSC(String string) {
		return new PSC("500", "North Hampton");
	}

	private Phlebotomist getPhleb(String string) {
		return new Phlebotomist("130","Mark Green");
	}

	private Patient getPatient(String string) {
		try {
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
			return new Patient("210", "Tom Thumb", "31 Westbrook Drive", 'Y', simpleDateFormat.parse("9/22/1959"));
		} catch (ParseException e) {
			return null;
		}
	}
}
