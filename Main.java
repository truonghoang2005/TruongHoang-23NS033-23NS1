package giuaky;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Main {
    private static final String STUDENT_FILE_PATH = "student.xml";
    private static final String RESULT_FILE_PATH = "kq.xml";

    public static void main(String[] args) {
        try {
            createStudentFileAndInputInfo();

            ExecutorService executor = Executors.newFixedThreadPool(3);

            executor.submit(() -> {
				try {
					readStudentXML();
				} catch (ParserConfigurationException | IOException | SAXException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});

            executor.submit(() -> calculateAgeFromDOB());

            executor.submit(() -> checkPrimeFromDOB());

            executor.shutdown();
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

            System.out.println("Kết quả:");
            readResultFromXML();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

   

	private static void createStudentFileAndInputInfo() throws IOException, TransformerException {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Nhập thông tin sinh viên (id.name.address.dateOfBirth): ");
        String input = scanner.nextLine();
        String[] studentInfo = input.split("\\.");

        FileWriter writer = new FileWriter(STUDENT_FILE_PATH);
        writer.write("<students>\n");
        writer.write("  <student>\n");
        writer.write("    <id>" + studentInfo[0] + "</id>\n");
        writer.write("    <name>" + studentInfo[1] + "</name>\n");
        writer.write("    <address>" + studentInfo[2] + "</address>\n");
        writer.write("    <dateOfBirth>" + studentInfo[3] + "</dateOfBirth>\n");
        writer.write("  </student>\n");
        writer.write("</students>\n");
        writer.close();
    }

    private static void readStudentXML() throws ParserConfigurationException, IOException, SAXException {
        File file = new File(STUDENT_FILE_PATH);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(file);
        doc.getDocumentElement().normalize();

        NodeList nodeList = doc.getElementsByTagName("student");

        for (int temp = 0; temp < nodeList.getLength(); temp++) {
            Node node = nodeList.item(temp);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                String dateOfBirth = element.getElementsByTagName("dateOfBirth").item(0).getTextContent();
                System.out.println("Date of Birth: " + dateOfBirth);
                synchronized (Main.class) {
                    try {
                        Main.class.wait(); 
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static void calculateAgeFromDOB() {
        try {
            File file = new File(STUDENT_FILE_PATH);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            doc.getDocumentElement().normalize();

            NodeList nodeList = doc.getElementsByTagName("student");

            for (int temp = 0; temp < nodeList.getLength(); temp++) {
                Node node = nodeList.item(temp);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String dateOfBirth = element.getElementsByTagName("dateOfBirth").item(0).getTextContent();
                    int age = calculateAge(dateOfBirth);
                    writeResultToXML("age", String.valueOf(age));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    

	private static void checkPrimeFromDOB() {
        try {
            File file = new File(STUDENT_FILE_PATH);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            doc.getDocumentElement().normalize();

            NodeList nodeList = doc.getElementsByTagName("student");

            for (int temp = 0; temp < nodeList.getLength(); temp++) {
                Node node = nodeList.item(temp);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String dateOfBirth = element.getElementsByTagName("dateOfBirth").item(0).getTextContent();
                    int sum = calculateSumOfDigits(dateOfBirth);
                    boolean isPrime = isPrime(sum);
                    writeResultToXML("isPrime", String.valueOf(isPrime));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int calculateAge(String dob) {
        int currentYear = java.time.Year.now().getValue();
        int birthYear = Integer.parseInt(dob.split("-")[0]);
        return currentYear - birthYear;
    }

    private static int calculateSumOfDigits(String str) {
        int sum = 0;
        for (int i = 0; i < str.length(); i++) {
            if (Character.isDigit(str.charAt(i))) {
                sum += Character.getNumericValue(str.charAt(i));
            }
        }
        return sum;
    }
    private static boolean isPrime(int n) {
        if (n <= 1) {
            return false;
        }
        for (int i = 2; i <= Math.sqrt(n); i++) {
            if (n % i == 0) {
                return false;
            }
        }
        return true;
    }
		


	private static void writeResultToXML(String tagName, String value) {
	    try {
	        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	        Document doc = dBuilder.newDocument();
	
	        Element rootElement = doc.createElement("results");
	        doc.appendChild(rootElement);
	
	        Element studentElement = doc.createElement("student");
	        rootElement.appendChild(studentElement);
	
	        Element idElement = doc.createElement("id");
	        idElement.appendChild(doc.createTextNode("001"));
	        studentElement.appendChild(idElement);
	
	        Element resultElement = doc.createElement(tagName);
	        resultElement.appendChild(doc.createTextNode(value));
	        studentElement.appendChild(resultElement);
	
	        TransformerFactory transformerFactory = TransformerFactory.newInstance();
	        Transformer transformer = transformerFactory.newTransformer();
	        DOMSource source = new DOMSource(doc);
	        StreamResult result = new StreamResult(new FileWriter(RESULT_FILE_PATH, true)); 
	        transformer.transform(source, result);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

	private static void readResultFromXML() throws ParserConfigurationException, IOException, SAXException {
		    File file = new File(RESULT_FILE_PATH);
		    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		    Document doc = dBuilder.parse(file);
		    doc.getDocumentElement().normalize();
		
		    NodeList nodeList = doc.getElementsByTagName("student");
		
		    for (int temp = 0; temp < nodeList.getLength(); temp++) {
		        Node node = nodeList.item(temp);
		        if (node.getNodeType() == Node.ELEMENT_NODE) {
		            Element element = (Element) node;
		            String id = element.getElementsByTagName("id").item(0).getTextContent();
		            String age = element.getElementsByTagName("age").item(0).getTextContent();
		            String isPrime = element.getElementsByTagName("isPrime").item(0).getTextContent();
		            System.out.println("ID: " + id + ", Age: " + age + ", Is Prime: " + isPrime);
		        }
		    }
		}
	}

