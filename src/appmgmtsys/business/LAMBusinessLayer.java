package appmgmtsys.business;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Date;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import appmgmtsys.data.DBSingleton;
import appmgmtsys.util.AppointmentUtil;
import components.data.Appointment;
import components.data.AppointmentLabTest;
import components.data.Diagnosis;
import components.data.LabTest;
import components.data.PSC;
import components.data.Patient;
import components.data.Phlebotomist;
import components.data.Physician;

public class LAMBusinessLayer {
	
	private DBSingleton dbSingleton;
	
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
	
	private static final String APPOINTMENT_ERROR = "ERROR:Appointment is not available"; 
	
	private java.util.Date APPOINTMENT_START_TIME;
	private java.util.Date APPOINTMENT_END_TIME;
	
	public LAMBusinessLayer() {
		super();
		initialize();
		initializeAppointmentStartAndEndTime();
	}

	public String initialize() {
		dbSingleton = DBSingleton.getInstance();
		dbSingleton.db.initialLoad("LAMS");
		return "database initialized!";
	}
	
	private void initializeAppointmentStartAndEndTime() {
		String appointmentStartTimeStr = "07:59:59";
		String appointmentEndTimeStr = "17:00:00";
		
		try {
			APPOINTMENT_START_TIME = timeFormat.parse(appointmentStartTimeStr);
			APPOINTMENT_END_TIME = timeFormat.parse(appointmentEndTimeStr);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getAllAppointments() throws Exception {
        List<Object> appointments = dbSingleton.db.getData("Appointment", "");
        if(!appointments.isEmpty()) {
        	return convertAppointmentToXMLString(appointments);
        }else {
        	initialize();
        	return getAllAppointments();
        }
	}
	
	public String getAppointment(String appointmentNo) throws Exception {
		List<Object> appointments = dbSingleton.db.getData("Appointment", "id='"+appointmentNo+"'");
		if(!appointments.isEmpty()) {
			return convertAppointmentToXMLString(appointments);
		}else {
			return createXMLErrorString("ERROR: No appointment found for "+appointmentNo);
		}
	}
	
	public String addAppointment(String xml) throws Exception {
	    DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
	    InputSource is = new InputSource();
	    is.setCharacterStream(new StringReader(xml));
	    Document doc = docBuilder.parse(is);
	    doc.getDocumentElement().normalize();
	    NodeList dateNodeList = doc.getElementsByTagName("date");
	    NodeList timeNodeList = doc.getElementsByTagName("time");
	    NodeList patientIdNodeList = doc.getElementsByTagName("patientId");
	    NodeList physicianIdNodeList = doc.getElementsByTagName("physicianId");
	    NodeList pscIdNodeList = doc.getElementsByTagName("pscId");
	    NodeList phlebotomistIdNodeList = doc.getElementsByTagName("phlebotomistId");
	    NodeList labTestsNodeList = doc.getElementsByTagName("labTests");
	    
	    //checking if the input has all the required tags
		if (dateNodeList.item(0) == null || timeNodeList.item(0) == null || patientIdNodeList.item(0) == null
				|| physicianIdNodeList.item(0) == null || pscIdNodeList.item(0) == null
				|| phlebotomistIdNodeList.item(0) == null || labTestsNodeList.item(0) == null) {
			return createXMLErrorString(null);
		}
	    
	    String dateStr = dateNodeList.item(0).getTextContent();
	    String timeStr = timeNodeList.item(0).getTextContent();
	    String patientId = patientIdNodeList.item(0).getTextContent();
	    String physicianId = physicianIdNodeList.item(0).getTextContent();
	    String pscId = pscIdNodeList.item(0).getTextContent();
	    String phlebotomistId = phlebotomistIdNodeList.item(0).getTextContent();
	    List<String> testIds = new ArrayList<>();
	    List<String> dxCodes = new ArrayList<>();
	    
	    Node labTestNode = labTestsNodeList.item(0);
	    if(labTestNode.getNodeType()==Node.ELEMENT_NODE) {
	    	Element labTestEle = (Element) labTestNode;
	    	NodeList tests = labTestEle.getElementsByTagName("test");
	    	if(tests.getLength()>0) {
	    		for(int i=0;i<tests.getLength();i++) {
		    		Node temp = tests.item(i);
		    		if(temp.getNodeType()==Node.ELEMENT_NODE) {
		    			Element ele = (Element)temp;
		    			testIds.add(ele.getAttribute("id"));
		    			dxCodes.add(ele.getAttribute("dxcode"));
		    		}
		    	}
	    	}else {
	    		return createXMLErrorString(null);
	    	}
	    }
    	
	    int id = fetchLastAppointmentId()+1;
    	Date date = null;
    	Time time = null;
    	Patient patient = null;
    	Physician physician = null;
    	PSC psc = null;
    	Phlebotomist phlebotomist = null;
    	List<AppointmentLabTest> appointmentLabTests = null;
    	
    	//validating input ids from database
    	if ((date = AppointmentUtil.getDateObj(dateStr)) == null || (time = AppointmentUtil.getTimeObj(timeStr)) == null || !(isAppointmentTimeBetween8AmTo5Pm(time))
				|| (patient = getPatient(patientId,physicianId)) == null || (physician = getPhysician(physicianId)) == null
				|| (psc = getPSC(pscId)) == null || (phlebotomist = getPhlebotomist(phlebotomistId)) == null
				|| (appointmentLabTests = getAppointmentLabTests(id+"", testIds, dxCodes)) == null) {
    		return createXMLErrorString(null);
		}
    	
    	//checking duplicate appointments
    	if (checkDuplicateAppointment(patientId, phlebotomistId, pscId, date, time)) {
    		return createXMLErrorString("Duplicate appointment");
		}
    	
    	//checking conflicting appointments
    	if(checkAppointmentConflict(date,time,phlebotomist.getId(), psc.getId())) {
    		return createXMLErrorString("Conflicting appointment");
    	}
    	
		Appointment newAppointment = new Appointment(id+"",date,time);
		newAppointment.setAppointmentLabTestCollection(appointmentLabTests);
		newAppointment.setPatientid(patient);
		newAppointment.setPhlebid(phlebotomist);
		newAppointment.setPscid(psc);
		boolean success = dbSingleton.db.addData(newAppointment);
		if(success) {
			try {
				return getAppointment(id+"");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				return createXMLErrorString("ERROR: error while fetching data for new appointment");
			}
		}else {
			return createXMLErrorString("ERROR: error while saving data");
		}
	}

	private PSC getPSC(String id) {
		List<Object> pscs = dbSingleton.db.getData("PSC", "id='"+id+"'");
		if(pscs.size()>0) {
			return (PSC) pscs.get(0);
		}else {
			return null;
		}
	}

	private Phlebotomist getPhlebotomist(String id) {
		List<Object> phlebotomists = dbSingleton.db.getData("Phlebotomist", "id='"+id+"'");
		if(phlebotomists.size()>0) {
			return (Phlebotomist) phlebotomists.get(0);
		}else {
			return null;
		}
	}
	
	private Physician getPhysician(String id) {
		List<Object> physicians = dbSingleton.db.getData("Physician", "id='"+id+"'");
		if(physicians.size()>0) {
			return (Physician) physicians.get(0);
		}else {
			return null;
		}
	}

	private Patient getPatient(String id, String physicianId) {
		List<Object> patients = dbSingleton.db.getData("Patient", "id='"+id+"'");
		if(patients.size()>0) {
			Patient patient = (Patient) patients.get(0);
			if(physicianId.equals(patient.getPhysician().getId())) {
				return patient;
			}
		}
		return null;
	}
	
	private List<AppointmentLabTest> getAppointmentLabTests(String appointmentId, List<String> testIds, List<String> dxCodes){
		if(testIds.size()!=dxCodes.size())
			return null;
		List<AppointmentLabTest> tests = new ArrayList<>();
		String testId = null;
		String dxCode = null;
		boolean invalid = false;
		for(int i=0;i<testIds.size();i++) {
			testId = testIds.get(i);
			dxCode = dxCodes.get(i);
			List<Object> labtests = dbSingleton.db.getData("LabTest", "id='"+testId+"'");
			if(labtests.size()>0) {
				List<Object> diagnosisList = dbSingleton.db.getData("Diagnosis", "code='"+dxCode+"'");
				if(diagnosisList.size()>0) {
					AppointmentLabTest test = new AppointmentLabTest(appointmentId, testId, dxCode);
					test.setLabTest((LabTest) labtests.get(0));
					test.setDiagnosis((Diagnosis) diagnosisList.get(0));
					tests.add(test);
				}else {
					invalid = true;
					break;
				}
			}else {
				invalid = true;
				break;
			}
		}
		if(!invalid) {
			return tests;
		}else {
			return null;
		}
	}
	
	public String createXMLErrorString(String errorMsg) {
		errorMsg=errorMsg!=null?errorMsg:APPOINTMENT_ERROR;
		StringBuilder output = new StringBuilder();
		output.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><AppointmentList><error>");
		output.append(errorMsg);
		output.append("</error></AppointmentList>");
		return output.toString();
	}
	
	public String createXMLInitializeString(String uri) {
		StringBuilder output = new StringBuilder();
		output.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><AppointmentList><intro>")
				.append("Welcome to the LAMS Appointment Service").append("</intro><wadl>").append(uri)
				.append("application.wadl")
				.append("</wadl></AppointmentList>");
		return output.toString();
	}
	
	private boolean isAppointmentTimeBetween8AmTo5Pm(Time appointmentTime) {
		if(appointmentTime.after(APPOINTMENT_START_TIME) && appointmentTime.before(APPOINTMENT_END_TIME)) {
			return true;
		}
		return false;
	}
	
	private boolean checkAppointmentConflict(Date date, Time time, String phlebid, String pscId) {
		Appointment a = null;
		String timeStr = time.toLocalTime().toString();
		String fifteenMinBefore = time.toLocalTime().minusMinutes(15).toString(); 
		String fifteenMinAfter = time.toLocalTime().plusMinutes(15).toString();
		List<Object> appointments = dbSingleton.db.getData("Appointment", "appttime>'"+fifteenMinBefore+"' and appttime < '"+fifteenMinAfter+"' and apptdate='"+dateFormat.format(date)+"' "
				+ "and phlebid='"+phlebid+"'");

		if(appointments.size()>0) {
			return true;
		}
		
		String fortyFiveMinutesBefore = time.toLocalTime().minusMinutes(45).toString();
		String fortyFiveMinutesAfter = time.toLocalTime().plusMinutes(45).toString();
		appointments = dbSingleton.db.getData("Appointment", "appttime>'"+fortyFiveMinutesBefore+"' and appttime <= '"+timeStr+"' and apptdate='"+dateFormat.format(date)+"' "
				+ "and phlebid='"+phlebid+"' order by appttime desc");
		if(appointments.size()>0) {
			a = (Appointment) appointments.get(0);
			if(!pscId.equals(a.getPscid().getId())) {
				return true;
			}
		}
		
		appointments = dbSingleton.db.getData("Appointment", "appttime >= '"+timeStr+"'and appttime<'"+fortyFiveMinutesAfter+"' and apptdate='"+dateFormat.format(date)+"' "
				+ "and phlebid='"+phlebid+"' order by appttime asc");
		if(appointments.size()>0) {
			a = (Appointment) appointments.get(0);
			if(!pscId.equals(a.getPscid().getId())) {
				return true;
			}
		}
		return false;
	}
	
	private String convertAppointmentToXMLString(List<Object> appointments) throws ParserConfigurationException, TransformerException, IOException {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
	    
	    Document doc = docBuilder.newDocument();
	    Element rootElement = doc.createElement("AppointmentList");
	    doc.appendChild(rootElement);
	    Appointment appointment = null;
	    for (Object app : appointments) {
			appointment = (Appointment) app;
			Element appointmentEle = doc.createElement("Appointment");
			appointmentEle.setAttribute("date", dateFormat.format(appointment.getApptdate())); //convert to util date and format https://stackoverflow.com/questions/15668329/convert-string-date-to-java-sql-date
			appointmentEle.setAttribute("id", appointment.getId());
			appointmentEle.setAttribute("time", appointment.getAppttime().toString());
			rootElement.appendChild(appointmentEle);
			
			Element patientEle = doc.createElement("patient");
			appointmentEle.appendChild(patientEle);
			patientEle.setAttribute("id", appointment.getPatientid().getId());
			
			Element patientNameEle = doc.createElement("name");
			patientNameEle.appendChild(doc.createTextNode(appointment.getPatientid().getName())); // patient name
			patientEle.appendChild(patientNameEle);
			
			Element patientAddressEle = doc.createElement("address");
			patientAddressEle.appendChild(doc.createTextNode(appointment.getPatientid().getAddress())); // patient address
			patientEle.appendChild(patientAddressEle);
			
			Element patientInsuranceEle = doc.createElement("insurance");
			patientInsuranceEle.appendChild(doc.createTextNode(appointment.getPatientid().getInsurance()+"")); //patient insurance
			patientEle.appendChild(patientInsuranceEle);
			
			Element patientDOBEle = doc.createElement("dob");
			patientDOBEle.appendChild(doc.createTextNode(dateFormat.format(appointment.getPatientid().getDateofbirth()))); //patient dob
			patientEle.appendChild(patientDOBEle);
			
			Element phlebotomistEle = doc.createElement("phlembotomist");
			appointmentEle.appendChild(phlebotomistEle);
			phlebotomistEle.setAttribute("id", appointment.getPhlebid().getId());
			
			Element phlebotomistNameEle = doc.createElement("name");
			phlebotomistNameEle.appendChild(doc.createTextNode(appointment.getPhlebid().getName())); //phelbotomist name
			phlebotomistEle.appendChild(phlebotomistNameEle);
			
			Element pscEle = doc.createElement("psc");
			appointmentEle.appendChild(pscEle);
			pscEle.setAttribute("id", appointment.getPscid().getId());
			
			Element pscNameEle = doc.createElement("name");
			pscNameEle.appendChild(doc.createTextNode(appointment.getPscid().getName()));//psc name
			pscEle.appendChild(pscNameEle);
			
			Element allLabTestsEle = doc.createElement("allLabTests");
			appointmentEle.appendChild(allLabTestsEle);
			
			List<AppointmentLabTest> appointmentLabTests = appointment.getAppointmentLabTestCollection();
			for (AppointmentLabTest appointmentLabTest : appointmentLabTests) {
				Element appointmentLabTestEle = doc.createElement("appointmentLabTest");
				allLabTestsEle.appendChild(appointmentLabTestEle);
				appointmentLabTestEle.setAttribute("appointmentId",appointmentLabTest.getAppointmentLabTestPK().getApptid());
				appointmentLabTestEle.setAttribute("dxcode", appointmentLabTest.getDiagnosis().getCode());
				appointmentLabTestEle.setAttribute("labTestId", appointmentLabTest.getLabTest().getId());
			}
		}
	    
	    TransformerFactory transformerFactory = TransformerFactory.newInstance();
	    Transformer transformer = transformerFactory.newTransformer();
	    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
//		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.STANDALONE, "no");
//		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
	    DOMSource source = new DOMSource(doc);
	    StringWriter writer = new StringWriter();
		StreamResult result = new StreamResult(writer);
		transformer.transform(source, result);
		String output = writer.toString();
		return output;
	}
	
	private int fetchLastAppointmentId() {
		List<Object> appointments = dbSingleton.db.getData("Appointment", "id=(SELECT max(id) from Appointment)");
		if(appointments.size()>0) {
			Appointment app = (Appointment) appointments.get(0);
			return Integer.parseInt(app.getId());
		}
		return 0;
	}
	
	private boolean checkDuplicateAppointment(String patientId, String phlebId, String pscId, Date date, Time time) {
		List<Object> appointments = dbSingleton.db.getData("Appointment", "patientid='"+patientId+"' and apptdate='"
				+dateFormat.format(date)+"' and appttime='"+time.toLocalTime().toString()+"' and phlebid='"+phlebId+"' and"
					+" pscid='"+pscId+"'");
		if(appointments.size()>0) {
			return true;
		}
		return false;
	}
	
}
