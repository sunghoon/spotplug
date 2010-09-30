import java.util.Date;
import java.util.LinkedList;

import junit.framework.Assert;

import org.drools.command.assertion.AssertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.plugtree.spotplug.impl.EventLog;
import com.plugtree.spotplug.impl.FusionEngine;
import com.plugtree.spotplug.impl.GenericEvent;
import com.plugtree.spotplug.impl.LogActuator;

public class RulesTest{

	private static FusionEngine engine;
	private static LinkedList<EventLog> eventLogList;
	
	@BeforeClass
	public static void setUp() {
		
		ApplicationContext context = new ClassPathXmlApplicationContext(new String[] {"/spotplug.xml"});
		
		engine = (FusionEngine)context.getBean("Engine");
		eventLogList = ((LogActuator)context.getBean("Actuator")).getEventLogList();
		
		engine.configure();
	}
	
	@Before
	public void initialize() {
		
		eventLogList.clear();		
	}
	
	@Test
	public void sameTransactionSameTimeDifferentDay() {
	
		GenericEvent event1 = new GenericEvent("Hera", 6000, new Date(2010,9,23,10,30,00), 20000, 0, 1,105);
		GenericEvent event2 = new GenericEvent("Hera", 7000, new Date(2010,9,22,11,30,00), 20000, 1, 1,105);
		GenericEvent event3 = new GenericEvent("Hera", 7000, new Date(2010,9,22,10,30,00), 20000, 2, 3,105);
		
		engine.processEvent(event1);
		Assert.assertTrue(eventLogList.isEmpty());

		engine.processEvent(event2);
		Assert.assertTrue(eventLogList.isEmpty());
		
		engine.processEvent(event3);
		Assert.assertTrue(eventLogList.size() == 1);
		Assert.assertTrue(eventLogList.getLast().getUserId() == "Hera");
		Assert.assertTrue(eventLogList.getLast().getFraudPattern() == "Same transaction, same time, everyday");
	}
	
	@Test
	public void excesiveAmountPattern() {
		
		GenericEvent event1 = new GenericEvent("Zeus", 6000, new Date(), 20000, 0, 1,105);
		GenericEvent event2 = new GenericEvent("Zeus", 5000, new Date(), 20000, 1, 2,105);
		GenericEvent event3 = new GenericEvent("Zeus", 10001, new Date(), 20000, 3, 3,105);
		
		engine.processEvent(event1);
		Assert.assertTrue(eventLogList.isEmpty());
		
		engine.processEvent(event2);
		Assert.assertTrue(eventLogList.isEmpty());
		
		engine.processEvent(event3);
		Assert.assertTrue(eventLogList.size() == 1);
		Assert.assertTrue(eventLogList.getLast().getUserId() == "Zeus");
		Assert.assertTrue(eventLogList.getLast().getFraudPattern() == "Excesive amount");
	}
	
	@Test
	public void manyEventsShortPeriod() {
		
		GenericEvent event1 = new GenericEvent("Thor", 6000, new Date(), 20000, 0, 1,105);
		GenericEvent event2 = new GenericEvent("Thor", 5000, new Date(), 20000, 1, 2,105);
		GenericEvent event3 = new GenericEvent("Thor", 10001, new Date(), 20000, 2, 3,105);
		GenericEvent event4 = new GenericEvent("Thor", 10001, new Date(), 20000, 3, 4,105);
		
		engine.processEvent(event1);
		Assert.assertTrue(eventLogList.isEmpty());
		
		engine.processEvent(event2);
		Assert.assertTrue(eventLogList.isEmpty());
		
		engine.processEvent(event3);
		Assert.assertTrue(eventLogList.isEmpty());
		
		engine.processEvent(event4);
		Assert.assertTrue(eventLogList.size() == 1);
		Assert.assertTrue(eventLogList.getLast().getUserId() == "Thor");
		Assert.assertTrue(eventLogList.getLast().getFraudPattern() == "Too many events in a short period of time");
	}
	
	@Test
	public void identicalTransactions() {

		GenericEvent event1 = new GenericEvent("Hera", 6000, new Date(), 20000, 0, 1,105);
		GenericEvent event2 = new GenericEvent("Hulk", 6000, new Date(), 20000, 1, 2,105);
		GenericEvent event3 = new GenericEvent("Hera", 7000, new Date(), 20000, 2, 3,105);
		GenericEvent event4 = new GenericEvent("Hera", 6000, new Date(), 20000, 3, 4,105);
		
		engine.processEvent(event1);
		Assert.assertTrue(eventLogList.isEmpty());
		
		engine.processEvent(event2);
		Assert.assertTrue(eventLogList.isEmpty());
		
		engine.processEvent(event3);
		Assert.assertTrue(eventLogList.isEmpty());
		
		engine.processEvent(event4);
		Assert.assertTrue(eventLogList.size() == 1);
		Assert.assertTrue(eventLogList.getLast().getUserId() == "Hera");
		Assert.assertTrue(eventLogList.getLast().getFraudPattern() == "Identical transactions");
	}
	
	@Test
	public void unusualHoursTransactions(){
		GenericEvent event1 = new GenericEvent("Hera", 6000, new Date(2010,9,23,2,30,00), 20000, 0, 1,105);
		GenericEvent event2 = new GenericEvent("Hulk", 6000, new Date(), 20000, 1, 2,105);
		GenericEvent event3 = new GenericEvent("Hera", 7000, new Date(2010,9,23,4,30,00), 20000, 2, 3,105);
		GenericEvent event4 = new GenericEvent("Hera", 6000, new Date(), 20000, 3, 4,105);
		
		engine.processEvent(event1);
		//Se activa
		
		engine.processEvent(event2);
		//No se activa
		
		engine.processEvent(event3);
		//Se activa
		
		engine.processEvent(event4);
		//No Se activa
	}
	
	@Test
	public void wideDistanceBetweenMessages(){
		GenericEvent event1 = new GenericEvent("Hera", 6000, new Date(2010,9,23,10,30,00), 20000, 0, 1,105);
		GenericEvent event2 = new GenericEvent("Hulk", 6000, new Date(), 20000, 1, 2,105);
		GenericEvent event3 = new GenericEvent("Hera", 7000, new Date(2010,9,23,14,30,00), 20000, 2, 3,105);
		GenericEvent event4 = new GenericEvent("Hera", 6000, new Date(2010,9,23,14,31,00), 20000, 3, 4,105);
		
		engine.processEvent(event1);
		
		engine.processEvent(event2);
		
		engine.processEvent(event3);
		
		engine.processEvent(event4);
	}
}
