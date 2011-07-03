package org.sapia.corus.tomcat;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.interop.api.InteropLink;

public class CatalinaBootstrapWrapperTest {

	// Fixtures
	private MockCorusInteropLink _corusInterop;
	
	@Before
	public void setUp() throws Exception {
		_corusInterop = new MockCorusInteropLink();
		InteropLink.setImpl(_corusInterop);
	}
	
	@Test
	public void testLifeCycle() throws Exception {
		Thread t = new Thread(new CatalinaBootstapTask(new String[] {"start"}));
		t.start();
		
		Thread.sleep(5000);
		
		_corusInterop.shutdown();
		
		t.join(5000);
	}
	
	public class CatalinaBootstapTask implements Runnable {
		private String[] _args;
		public CatalinaBootstapTask(String[] someArgs) {
			_args = someArgs;
		}
		
		public void run() {
			CatalinaBootstrapWrapper.main(_args);
		}
	}
	
	public static void main(String[] args) {
		try {
			CatalinaBootstrapWrapperTest test = new CatalinaBootstrapWrapperTest();
			test.setUp();
			test.testLifeCycle();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
