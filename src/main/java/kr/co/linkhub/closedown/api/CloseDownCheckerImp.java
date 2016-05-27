package kr.co.linkhub.closedown.api;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import kr.co.linkhub.auth.LinkhubException;
import kr.co.linkhub.auth.Token;
import kr.co.linkhub.auth.TokenBuilder;

import com.google.gson.Gson;

public class CloseDownCheckerImp implements CloseDownChecker {

	private final String ServiceID = "CLOSEDOWN";
	private final String ServiceURL = "https://closedown.linkhub.co.kr";
	private final String APIVersion = "1.0";

	private TokenBuilder tokenBuilder;

	private String linkID;
	private String secretKey;
	private Gson _gsonParser = new Gson();
	
	private static Token token = null;
	
	protected String getLinkID() {
		return linkID;
	}

	/**
	 * 링크아이디 설정. (링크허브에서 발급)
	 * 
	 * @param linkID
	 */
	public void setLinkID(String linkID) {
		this.linkID = linkID;
	}

	protected String getSecretKey() {
		return secretKey;
	}

	/**
	 * 비밀키 설정 (Issued by Linkhub)
	 * 
	 * @param secretKey
	 *            SecretKey.
	 */
	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	/* (non-Javadoc)
	 * @see com.jusolink.api.SearchService#getUnitCost()
	 */
	@Override
	public float getUnitCost() throws CloseDownException {
		UnitCostResponse response = httpget("/UnitCost", UnitCostResponse.class);

		return response.unitCost;
	}
	
	/* (non-Javadoc)
	 * @see com.jusolink.api.SearchService#getBalance()
	 */
	@Override
	public double getBalance() throws CloseDownException {
		try {
			return getTokenbuilder().getPartnerBalance(
					this.getSessionToken(null));
		} catch (LinkhubException le) {
			throw new CloseDownException(le);
		}
	}

	
	private TokenBuilder getTokenbuilder() {
		if (this.tokenBuilder == null) {
			tokenBuilder = TokenBuilder
					.getInstance(getLinkID(), getSecretKey())
					.ServiceID(ServiceID)
					.addScope("170");
		}

		return tokenBuilder;
	}

	private String getSessionToken( String ForwardIP)
			throws CloseDownException {
	

		boolean expired = true;

		if (token != null) {

			SimpleDateFormat format = new SimpleDateFormat(
					"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			format.setTimeZone(TimeZone.getTimeZone("UTC"));
			
			SimpleDateFormat subFormat = new SimpleDateFormat(
					"yyyy-MM-dd'T'HH:mm:ss'Z'");
			subFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
			
			try {
				Date expiration = format.parse(token.getExpiration());
				Date UTCTime = subFormat.parse(getTokenbuilder().getTime());
				expired = expiration.before(UTCTime);
				
			} catch (LinkhubException le){
				throw new CloseDownException(le);
			} catch (ParseException e){
			}
		}

		if (expired) {
			try {
				token = getTokenbuilder().build(null, ForwardIP);
			} catch (LinkhubException le) {
				throw new CloseDownException(le);
			}
		}

		return token.getSession_token();
	}

	/**
	 * Convert Object to Json String.
	 * 
	 * @param Graph
	 * @return jsonString
	 */
	protected String toJsonString(Object Graph) {
		return _gsonParser.toJson(Graph);
	}

	/**
	 * Convert JsonString to Object of Clazz
	 * 
	 * @param json
	 * @param clazz
	 * @return Object of Clazz
	 */
	protected <T> T fromJsonString(String json, Class<T> clazz) {
		return _gsonParser.fromJson(json, clazz);
	}

	/**
	 * 
	 * @param url
	 * @param CorpNum
	 * @param PostData
	 * @param UserID
	 * @param Action
	 * @param clazz
	 * @return returned object
	 * @throws JusoLinkException
	 */
	protected <T> T httppost(String url, String PostData, String Action, Class<T> clazz)
			throws CloseDownException {
		HttpURLConnection httpURLConnection;
		try {
			URL uri = new URL(ServiceURL + url);
			httpURLConnection = (HttpURLConnection) uri.openConnection();
		} catch (Exception e) {
			throw new CloseDownException(-99999999, "휴폐업조회 API 서버 접속 실패", e);
		}

		httpURLConnection.setRequestProperty("Authorization", "Bearer " + getSessionToken(null));
		

		httpURLConnection.setRequestProperty("x-api-version".toLowerCase(),
				APIVersion);

		if (Action != null && Action.isEmpty() == false) {
			httpURLConnection.setRequestProperty("X-HTTP-Method-Override",
					Action);
		}

		httpURLConnection.setRequestProperty("Content-Type",
				"application/json; charset=utf8");

		try {
			httpURLConnection.setRequestMethod("POST");
		} catch (ProtocolException e1) {
		}

		httpURLConnection.setUseCaches(false);
		httpURLConnection.setDoOutput(true);

		if ((PostData == null || PostData.isEmpty()) == false) {

			byte[] btPostData = PostData.getBytes(Charset.forName("UTF-8"));

			httpURLConnection.setRequestProperty("Content-Length",
					String.valueOf(btPostData.length));

			try {

				DataOutputStream output = new DataOutputStream(
						httpURLConnection.getOutputStream());
				output.write(btPostData);
				output.flush();
				output.close();
			} catch (Exception e) {
				throw new CloseDownException(-99999999,
						"Fail to POST data to Server.", e);
			}

		}
		String Result = "";

		try {
			InputStream input = httpURLConnection.getInputStream();
			Result = fromStream(input);
			input.close();
		} catch (IOException e) {

			ErrorResponse error = null;

			try {
				InputStream input = httpURLConnection.getErrorStream();
				Result = fromStream(input);
				input.close();
				error = fromJsonString(Result, ErrorResponse.class);
			} catch (Exception E) {
			}

			if (error == null)
				throw new CloseDownException(-99999999,
						"Fail to receive data from Server.", e);
			else
				throw new CloseDownException(error.getCode(), error.getMessage());
		}

		return fromJsonString(Result, clazz);
	}

	/**
	 * 
	 * @param url
	 * @param CorpNum
	 * @param UserID
	 * @param clazz
	 * @return returned object
	 * @throws JusoLinkException
	 */
	protected <T> T httpget(String url, Class<T> clazz) throws CloseDownException {
		HttpURLConnection httpURLConnection;
		try {
			URL uri = new URL(ServiceURL + url);
			httpURLConnection = (HttpURLConnection) uri.openConnection();
		} catch (Exception e) {
			throw new CloseDownException(-99999999, "휴폐업조회 API 서버 접속 실패", e);
		}

		httpURLConnection.setRequestProperty("Authorization", "Bearer " + getSessionToken(null));
		

		httpURLConnection.setRequestProperty("x-api-version".toLowerCase(),
				APIVersion);

		String Result = "";

		try {
			InputStream input = httpURLConnection.getInputStream();
			Result = fromStream(input);
			input.close();
		} catch (IOException e) {

			ErrorResponse error = null;

			try {
				InputStream input = httpURLConnection.getErrorStream();
				Result = fromStream(input);
				input.close();
				error = fromJsonString(Result, ErrorResponse.class);
			} catch (Exception E) {
			}

			if (error == null)
				throw new CloseDownException(-99999999,
						"Fail to receive data from Server.", e);
			else
				throw new CloseDownException(error.getCode(), error.getMessage());
		}

		return fromJsonString(Result, clazz);
	}
	
	private class ErrorResponse {

		private long code;
		private String message;

		public long getCode() {
			return code;
		}

		public String getMessage() {
			return message;
		}

	}

	protected class UnitCostResponse {

		public float unitCost;

	}
	
	private static String fromStream(InputStream input) throws IOException {

		InputStreamReader is = new InputStreamReader(input,
				Charset.forName("UTF-8"));
		StringBuilder sb = new StringBuilder();
		BufferedReader br = new BufferedReader(is);

		String read = br.readLine();

		while (read != null) {
			sb.append(read);
			read = br.readLine();
		}

		return sb.toString();
	}

	@Override
	public CorpState CheckCorpNum(String CorpNum) throws CloseDownException {
		if(CorpNum == null || CorpNum.trim().isEmpty()) {
			throw new CloseDownException(-17000000, "사업자번호가 입력되지 않았습니다.");
		}
		
		return  httpget("/Check?CN=" + CorpNum, CorpState.class);
	}

	@Override
	public CorpState[] CheckCorpNum(String[] CorpNums) throws CloseDownException {
		if(CorpNums == null || CorpNums.length == 0) {
			throw new CloseDownException(-17000001, "사업자번호가 입력되지 않았습니다.");
		}
		
		String postData = new Gson().toJson(CorpNums);
		
		return  httppost("/Check",postData,null, CorpState[].class);
	}

	
}
