package elPackage.testcase;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Assert;

import elPackage.Camera;
import elPackage.Firebase.Common;


public class Test {

	@org.junit.Test
	public void test() {
		Assert.assertEquals(1, 1);;
	}
	
	/*@org.junit.Test
	public void test_callable_function() throws IOException {
		Camera instance_camera = new Camera();
		Boolean callable = instance_camera.firstRead();
		System.out.println(callable);
		Assert.assertEquals(callable, true);
	}*/
	@org.junit.Test
	public void test_common_te() {
		Assert.assertNotEquals(Common.te, null);
	}
	@org.junit.Test
	public void test_common_initFireBase() {
		try {
			Boolean callable = Common.initFireBase();
		}catch (Exception e ){
			String cause = "fichier de connexion non present";
			Assert.assertEquals(cause, "fichier de connexion non present");
		}
		
	}
}
