package tst;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Test;
import org.junit.Before;
import org.junit.Ignore;

import scn.Game.DifficultySetting;
import cls.Aircraft;
import cls.Waypoint;

public class SerializableTest {	
	
	Aircraft testAircraft;
	
	@Before
	public void setUp() {
		Waypoint[] waypointList = new Waypoint[]{
				new Waypoint(0, 0, true, 0),
				new Waypoint(100, 100, true, 0),
				new Waypoint(25, 75, false, 0),
				new Waypoint(75, 25, false, 0),
				new Waypoint(50, 50, false, 0)};
		
		testAircraft = new Aircraft("testAircraft", "Berlin", "Dublin",
				new Waypoint(100, 100, true, 0), new Waypoint(0, 0, true, 0),
				null, 10.0, waypointList, DifficultySetting.MEDIUM, null);
	}
	
	@Test
	@Ignore
	public void serializeAircraft() throws IOException {
		File file = new File("TestAircraft.ser");
        FileOutputStream fos = new FileOutputStream(file);
        ObjectOutputStream oos = new ObjectOutputStream(fos);

        oos.writeObject(testAircraft);
        oos.close();
	}
	
	@Test
	public void deserializeAircraft() throws IOException, ClassNotFoundException {
		File file = new File("TestAircraft.ser");
		FileInputStream fis = new FileInputStream(file);
        ObjectInputStream ois = new ObjectInputStream(fis);

        Aircraft recoveredAircraft = (Aircraft) ois.readObject();
        System.out.println("data : " + recoveredAircraft.getName());
        ois.close();
	}
}
