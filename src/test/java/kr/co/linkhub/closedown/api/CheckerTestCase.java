package kr.co.linkhub.closedown.api;

import org.junit.Test;

public class CheckerTestCase {

	private final String testLinkID = "TESTER";
	private final String testSecretKey = "SwWxqU+0TErBXy/9TVjIPEnI0VTUMMSQZtJf3Ed8q3I=";

	private CloseDownChecker closeDownChecker;
	
	public CheckerTestCase() {
		CloseDownCheckerImp checker = new CloseDownCheckerImp();

		checker.setLinkID(testLinkID);
		checker.setSecretKey(testSecretKey);
		
		closeDownChecker = checker;
	}
		
	@Test
	public void getBalance_TEST() throws CloseDownException {

		double balance = closeDownChecker.getBalance();

		System.out.println(balance);
	}
	
	@Test
	public void getUnitCost_TEST() throws CloseDownException {

		float unitCost = closeDownChecker.getUnitCost();

		System.out.println(unitCost);
	}
	
	@Test
	public void check4108600477_TEST() throws CloseDownException {

		CorpState corpState = closeDownChecker.CheckCorpNum("4108600477");

		System.out.println(corpState.getState());
	}
	
	@Test
	public void check다량_TEST() throws CloseDownException {

		
		String[] CorpNums = new String []{"4108600477","1231212312"};
		
		
		CorpState[] corpStates = closeDownChecker.CheckCorpNum(CorpNums);

		System.out.println(corpStates[0].getState());
	}

}
