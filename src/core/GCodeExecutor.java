package core;

import lejos.geom.Point;
import lejos.nxt.Button;
import lejos.nxt.MotorPort;
import lejos.nxt.NXT;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.SensorPort;
import lejos.nxt.TouchSensor;

public class GCodeExecutor {

	
//Constants	
public static final NXTRegulatedMotor mx = new NXTRegulatedMotor(MotorPort.A);
public static final NXTRegulatedMotor my = new NXTRegulatedMotor(MotorPort.B);
public static final NXTRegulatedMotor mz = new NXTRegulatedMotor(MotorPort.C);
public static final TouchSensor tx = new TouchSensor(SensorPort.S1);
public static final TouchSensor ty = new TouchSensor(SensorPort.S2);
public static final TouchSensor tz = new TouchSensor(SensorPort.S3);



	
	private static void homeY(float velocity) {
		my.setSpeed(velocity);
		while(ty.isPressed() == false) {
			my.rotate(10);
		}
	}
	
	private static void homeZ(float velocity) {
		mz.setSpeed(velocity);
		while(tz.isPressed() == false) {
			mz.rotate(10);
		}
	}
	
	private static void homeX(float velocity) {
		mx.setSpeed(velocity);
		while(tx.isPressed() == false) {
			mz.rotate(-10);
		}
		
	}
	
	/**
	 * Move to origin
	 * @param start_point
	 * @param end_point
	 * @param velocity
	 */
	public static void G28 (Point3D start_point, Point3D end_point, float velocity) {
		if (end_point.x == 0) {
			homeX(velocity);
		} 
		
		if (end_point.y == 0) {
			homeY(velocity);
		}
		
		if (end_point.z == 0) {
			homeZ(velocity);
		}
		
		G0(start_point,end_point,velocity);
		
	}
	
	
	
	
	public static void G0 (Point start_point, Point end_point, float velocity) {
		int sidex = (int) (end_point.x - start_point.x);		
		int sidey = (int) (end_point.y - start_point.y);
		float velx,vely;
		if (sidex != 0) {
			velx = velocity; 
			vely = (velx * sidey) / sidex;
		}
		else {
			vely = velocity; velx=0;
		}
		Mover mover_x = new Mover(mx, sidex, velx);
		Mover mover_y = new Mover(my,sidey,vely);
		mover_x.start(); mover_y.start();
		try{
			mover_x=null; mover_y = null; System.gc();}
		catch (Exception e) {}
		finally {System.gc();};
	}
	
	public static void G0 (Point3D start_point,Point3D end_point,float velocity) {
		int sidex = (int) (end_point.x - start_point.x);		
		int sidey = (int) (end_point.y - start_point.y);
		int sidez = (int) (end_point.z - start_point.z);
		float velx=0,vely = 0,velz=0; 
		if (sidex != 0) {
			velx = velocity;
			vely = (velx * sidey) / sidex;
			velz = (velx * sidez) / sidex;
		} else if (sidey != 0 && sidex == 0) {
			velx=0;
			vely = velocity;
			velz = (vely * sidez) / sidey;}
	else if (sidez !=0 && sidex==0 && sidey==0) {
		velx = vely = 0; velz = velocity;
	}
		Mover mover_x  = new Mover(mx,sidex,velx);
		Mover mover_y  = new Mover(my,sidey,vely);
		Mover mover_z = new Mover(mz,sidez,velz);
		mover_x.start(); mover_y.start(); mover_z.start();
		//mover_x = null; mover_y = null; mover_z = null; System.gc();
	}
	
	
	public static void G2 (Point start_point,Point end_point, float velocity) {
		int sidex = (int) (end_point.x - start_point.x);		
		int sidey = (int) (end_point.y - start_point.y);
		if (Math.abs(sidey) >= Math.abs(sidex)) {
			int accely = (int) Math.abs((velocity * velocity) / sidex);
			float velx = velocity;
			Mover mover_x = new Mover(mx,sidex,velx);
			AcceleratingMover accel_mover_y = new AcceleratingMover(my, sidey, accely);
			mover_x.start(); accel_mover_y.start();
			mover_x=null;accel_mover_y=null; System.gc();
		} else {
			int accelx = (int) Math.abs(velocity*velocity/sidey);
			float vely = velocity;
			Mover mover_y = new Mover(my,sidey,vely);
			AcceleratingMover accel_mover_x = new AcceleratingMover(mx, sidex, accelx);
			mover_y.start(); accel_mover_x.start();
			//mover_y=null;accel_mover_x=null; System.gc();
		}
	}
	
	
	
	
	
	
	
	
	
	
	public static void G2 (Point3D start_point,Point3D end_point,float velocity) {
		int sidex = (int) (end_point.x - start_point.x);		
		int sidey = (int) (end_point.y - start_point.y);
		int sidez = (int) (end_point.z - start_point.z);
		int[] distance = new int[2];
		distance[0] = Math.abs(sidex); distance[1]=Math.abs(sidey);distance[2]=Math.abs(sidez);
		
		//find max distance, min distance, determine movements
		if (distance[0] >=  distance[1] && distance[0] >=distance[2]) {
			//x > y > z or x > z > y
			if (distance[1] > distance[2]) {
				int accelx = (int) Math.abs((velocity*velocity)/sidez);
				float vely = (float) Math.abs((sidey * accelx)/velocity);
				float velz = velocity;
				AcceleratingMover accel_mover_x = new AcceleratingMover(mx, sidez, accelx);
				Mover mover_y = new Mover(my,sidey,vely);
				Mover mover_z = new Mover(mz,sidez,velz);
				accel_mover_x.start();mover_y.start();mover_z.start();
			} else {
				int accelx = (int) Math.abs((velocity*velocity)/sidez);
				float velz = (float) Math.abs((sidez * accelx)/velocity);
				float vely = velocity;
				AcceleratingMover accel_mover_x = new AcceleratingMover(mx, sidez, accelx);
				Mover mover_y = new Mover(my,sidey,vely);
				Mover mover_z = new Mover(mz,sidez,velz);
				accel_mover_x.start();mover_y.start();mover_z.start();
			}
			
			
			
			
		} else if (distance[1] > distance[0] && distance[1] >= distance[2]) {
			//y > x > z or y > z >x
			if (distance[0] > distance[2]) {
				int accely = (int) Math.abs((velocity*velocity)/sidez);
				float velx = (float) Math.abs((sidex * accely)/velocity);
				float velz = velocity;
				Mover mover_x = new Mover(mx,sidex,velx);
				AcceleratingMover accel_mover_y  = new AcceleratingMover(my,sidey,accely);
				Mover mover_z = new Mover(mz,sidez,velz);
				mover_x.start();accel_mover_y.start();mover_z.start();
							
			} else {
				int accely = (int) Math.abs((velocity*velocity)/sidez);
				float velz = (float) Math.abs((sidez * accely)/velocity);
				float velx = velocity;
				Mover mover_x = new Mover(mx,sidex,velx);
				AcceleratingMover accel_mover_y  = new AcceleratingMover(my,sidey,accely);
				Mover mover_z = new Mover(mz,sidez,velz);
				mover_x.start();accel_mover_y.start();mover_z.start();
			}
			
			
			
			
			
		} else {//if (distance[2] > distance[0] && distance[2] > distance[1]) {
			//z > x > y or z > y > x
			//TODO add last functionality
			if(distance[0] > distance[1]) {
				int accelz = (int) Math.abs((velocity*velocity)/sidey);
				float velx = (float) Math.abs((sidex * accelz))/velocity;
				float vely = velocity;
				Mover mover_x = new Mover(mx,sidex,velx);
				Mover mover_y = new Mover(my,sidey,vely);
				AcceleratingMover accel_mover_z = new AcceleratingMover(mx,sidez,accelz);
				mover_x.start();mover_y.start();accel_mover_z.start();
							
			} else {
				int accelz = (int) Math.abs((velocity*velocity)/sidex);
				float vely = (float) Math.abs((sidey * accelz))/velocity;
				float velx = velocity;
				Mover mover_x = new Mover(mx,sidex,velx);
				Mover mover_y = new Mover(my,sidey,vely);
				AcceleratingMover accel_mover_z = new AcceleratingMover(mx,sidez,accelz);
				mover_x.start();mover_y.start();accel_mover_z.start();

			}
			
		}
		
		
		
	}
	
	
	
	public static void G1 (Point3D start_point, Point3D end_point, float velocity) {
		
		Drill drill = new Drill(SensorPort.S4);
		drill.start();
		G0(start_point, end_point, velocity);
		drill.stop();
	}
	
	
	public static void M2() throws InterruptedException {
		System.out.println("End of program");
		try {
			Thread.sleep(2000);
		} finally {
			NXT.exit(0);
		}
	}
	
	
	/**
	 * Freezes the execution of the program until the user presses the ENTER Button on the NXT 
	 */
	public static void M0() { 
		while(Button.ENTER.isDown() == false) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {};
		}
		
		
	}
	

	public static void M1(boolean stop_switch) {
		if (stop_switch) {
			while(Button.ENTER.isDown() == false) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {};
			}
		}
	}
}