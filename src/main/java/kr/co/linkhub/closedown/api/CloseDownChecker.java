package kr.co.linkhub.closedown.api;


/**
 * CloseDown Check Service Interface.
 * 
 * @author KimSeongjun
 * @version 1.0.0
 */
public interface CloseDownChecker {

	/**
	 * 잔여포인트 확인
	 * 
	 * @return RemainPoint
	 * @throws JusoLinkException
	 */
	public abstract double getBalance()
			throws CloseDownException;
	
	/**
	 * 조회 단가 확인
	 * 
	 * @return 단가 (ex. 1.0)
	 * @throws PopbillException
	 */
	public float getUnitCost() throws CloseDownException;
	
	/**
	 * 휴폐업상태 조회
	 * @param CorpNum
	 * @return
	 */
	CorpState CheckCorpNum(String CorpNum) throws CloseDownException;	
	
	/**
	 * 휴폐업상태 다량 조회
	 * @param CorpNum List
	 * @return
	 */
	CorpState[] CheckCorpNum(String[] CorpNum) throws CloseDownException;
}
