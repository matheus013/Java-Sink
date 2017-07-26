package app;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;

import net.tinyos.message.Message;
import net.tinyos.message.MessageListener;
import net.tinyos.message.MoteIF;
import net.tinyos.packet.BuildSource;
import net.tinyos.packet.PhoenixSource;
import net.tinyos.util.PrintStreamMessenger;

class Sense implements MessageListener  {

	/***********Load native libraries *************/
	private static String nativeLibraryPath =
			System.getProperty("user.dir") + "/sense_lib/native/Linux/x86_64-unknown-linux-gnu/";

	static {
//		System.load(nativeLibraryPath + "libgetenv.so");
//		System.load(nativeLibraryPath + "libtoscomm.so");
	}

	private PhoenixSource phoenix;
	private MoteIF mif;
	private String[] environments = {"SINK","sensor_1", "sensor_2", "sensor_3",
			"sensor_4", "sensor_5", "sensor_6", "sensor_7", "sensor_8",
			"sensor_9", "sensor_10"};
	private SimpleDateFormat dt;
	private String date;
	private long msDate;

	public Sense(final String source){
		phoenix = BuildSource.makePhoenix(source, PrintStreamMessenger.err);
		mif = new MoteIF(phoenix);
		mif.registerListener(new SenseMsg(),this);
		//		Example "2016-06-24T21:58:19.000Z"
		dt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.000Z");

	}

	public void messageReceived(int dest_addr, Message msg) {

		//Get current date
		msDate = System.currentTimeMillis();
		date = dt.format(msDate);
		
		//Display sensor data
		showData(msg);

		//insert data into database
//		saveData(msg);
	}


	private double calculateTaosLight(int visibleLight, int infraredLight) {
			final int CHORD_VAL[] = {0,16,49,115,247,511,1039,2095};
			final int STEP_VAL[] = {1,2,4,8,16,32,64,128};
			int chordNumber, stepNumber, ch0Counts, ch1Counts;
			
			chordNumber = (visibleLight>>4) & 7;
			stepNumber = visibleLight & 15;
			ch0Counts = CHORD_VAL[chordNumber] + stepNumber * STEP_VAL[chordNumber];

			chordNumber = (infraredLight>>4)&7;
			stepNumber = infraredLight & 15;
			ch1Counts = CHORD_VAL[chordNumber] + stepNumber * STEP_VAL[chordNumber];
			
			double pConst = -3.13 * ch1Counts/ch0Counts;
			return ch0Counts * 0.46 * Math.exp(pConst);
	}

	private double[] calculateSensirion(int Temperature,int Humidity){
		double [] converted = new double[2];

		converted[0]=-39.4+(0.01*(double)Temperature);
		converted[1]=(-2.0468+0.0367*(double)Humidity-0.0000015955*Math.pow((double)Humidity,(double )2))+(converted[0]-25)*(0.01+0.00008*(double)Humidity);

		return converted;
	}

	private void showData(Message message) {
		if(message instanceof SenseMsg) {
			SenseMsg tempMessage = (SenseMsg) message;

			double light = calculateTaosLight(tempMessage.get_VisLight_data(),tempMessage.get_InfLight_data());

			double[] sensirionCalcData =
					calculateSensirion(tempMessage.get_Temp_data(),tempMessage.get_Hum_data());

			int voltage =
					(1223 * 1024)/tempMessage.get_Voltage_data();

			System.out.println("The measured results are:");
			System.out.println();
			System.out.println("Node:                   "+tempMessage.get_nodeid());
			System.out.printf("Sensirion temperature:  %.2f\n",sensirionCalcData[0]);
			System.out.printf("Sensirion humidity:     %.2f\n",sensirionCalcData[1]);
			System.out.println("Intersema temperature:  "+tempMessage.getElement_Intersema_data(0)/10);
			System.out.println("Intersema pressure:     "+tempMessage.getElement_Intersema_data(1)/10);
			System.out.println("Taos visible light:     "+light);
			System.out.println("Accelerometer X axis:   "+tempMessage.get_AccelX_data());
			System.out.println("Accelerometer Y axis:   "+tempMessage.get_AccelY_data());
			System.out.println("Voltage:                "+voltage);
			System.out.println("Environment id:\t\t" + environments[(tempMessage.get_nodeid()%10)]);
			System.out.println("Country:                "+"Brazil");
			System.out.println("State:                	"+"Alagoas");
			System.out.println("City:                	"+"Maceio");
			System.out.println("Latitude:               "+"-9.555032");
			System.out.println("Longitude:              "+"-35.774708");
			System.out.println("date:\t\t\t" + date);
			System.out.println("date:\t\t\t" + msDate);
			System.out.println();
		}
	}

	private void connectAndSaveToDatabase(String databaseName, String data) {
		try {

			//Set URL address
			//192.168.200.242
			URL url = new URL("http://192.168.202.63/api/v1/"+databaseName+'/');

			//Request connection
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();

			//Set request type
			conn.setRequestMethod("POST");
			//Set request properties
			conn.setRequestProperty("User-Agent", "Mozilla/5.0");
			conn.setRequestProperty("Accept-Language", "pt-br");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setRequestProperty("charset", "UTF-8");
			conn.setDoOutput(true);

			//Write data to server
			DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
			wr.writeBytes(data);

			//Close connection
			wr.close();

			System.out.println("\nSending 'POST' request to URL : " + url);
			System.out.println("Response Code : " + conn.getResponseCode());


		} catch (MalformedURLException e) {
			System.out.println("A malformed URL exception has occurred"+ e.getMessage());
		} catch (IOException e) {
			System.out.println("Error:"+ e.getMessage());
		}
	}

	private void saveData(Message message) {
        System.out.println("SAVE ALL");
        if(message instanceof SenseMsg) {
			//Get packet
			SenseMsg tempMessage = (SenseMsg) message;

			//Calibrate sensor values
			double taosLight =
					calculateTaosLight(tempMessage.get_VisLight_data(),tempMessage.get_InfLight_data());
			
			double[] sensirionCalcData =
					calculateSensirion(tempMessage.get_Temp_data(),tempMessage.get_Hum_data());

			int voltage =
					(1223 * 1024)/tempMessage.get_Voltage_data();

			if(voltage >= 2100) {

				//Save sensor data to a formatted string
				String data = String.format("nodeID=%s&sensirion_temp=%s&"
						+ "sensirion_hum=%s&intersema_temp=%s&"
						+ "intersema_press=%s&infrared_light=%s&"
						+ "light=%s&accel_x=%s&"
						+ "accel_y=%s&voltage=%s&"
						+ "country=%s&state=%s&"
						+ "city=%s&latitude=%s&"
						+ "longitude=%s&env_id=%s&"
						+ "date=%s", tempMessage.get_nodeid(), sensirionCalcData[0],
						sensirionCalcData[1], tempMessage.getElement_Intersema_data(0)/10,
						tempMessage.getElement_Intersema_data(1)/10, taosLight,
						taosLight, tempMessage.get_AccelX_data(),
						tempMessage.get_AccelY_data(), voltage,
						"Brazil", "Alagoas", "Maceió",
						"-9.555032", "-35.774708", environments[(tempMessage.get_nodeid() - 1)/ 5], date);

				//Save data to a remote server
				connectAndSaveToDatabase("sensors", data);
				connectAndSaveToDatabase("sensors0", data);
			}
			else {
				System.out.println("Voltage is too low, package rejected.\n");
			}
		}
		else {
			System.out.println("Invalid");
		}
	}
}
