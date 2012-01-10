package cl.ciudadanointeligente.sil.parser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

import cl.ciudadanointeligente.sil.model.SilSession;
import cl.ciudadanointeligente.sil.model.SilVote;
import cl.votainteligente.legislativo.model.SessionChamber;
import cl.votainteligente.legislativo.model.Vote;

public class DiputeeChamberSessionParser {
	private final String CHAMBER_BASE_URL = "http://www.camara.cl";
	private final String SIL_BASE_URL = "http://www.camara.cl/trabajamos/";
	private final String SIL_SESSION_SUMMARY_URL = SIL_BASE_URL
			+ "sala_sesiones.aspx";

	private String PROPERTIES[][] = { { "__EVENTTARGET", "" },
			{ "__EVENTARGUMENT", "" }, { "__LASTFOCUS", "" },
			{ "__VIEWSTATE", "" }, { "__EVENTVALIDATION", "" },
			{ "ctl00$mainPlaceHolder$ddlLegislaturas", "" },
			{"ctl00$mainPlaceHolder$ScriptManager1",""}};

	private String HEADER_PROPERTIES[][] = {
			{ "User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows 2000)" },
			{ "Content-Type","application/x-www-form-urlencoded; charset=UTF-8" },
			{ "Content-Language", "en-US" } };
	private DateFormat df;
	private DateFormat bdf;
	private DateFormat dfLey;
	private DateFormat dfLeyAlt;
	private NumberFormat nfLey;
	private HtmlCleaner cleaner;

	public DiputeeChamberSessionParser() {
		df = new SimpleDateFormat("dd/MM/yyyy");
		bdf = new SimpleDateFormat("EEEE d 'de' MMMM, yyyy", new Locale("es",
				"CL"));
		dfLey = new SimpleDateFormat("yyyy-MM-dd");
		dfLeyAlt = new SimpleDateFormat("dd/MM/yy");
		nfLey = new DecimalFormat("###,###,###", new DecimalFormatSymbols(
				new Locale("es", "CL")));
		cleaner = new HtmlCleaner();


	}

	private String getProperty(TagNode document, String id) throws Throwable {
		TagNode input = document.findElementByAttValue("name", id, true, false);
		if (input == null) 
			return "";
		return input.getAttributeByName("value");
	}

	private void setUpAllProperties(TagNode document) throws Throwable {
		for (String[] property : PROPERTIES) {
			String found = getProperty(document, property[0]);
			if (found != null)
				property[1] = found;
			else
				property[1] = "";
		}
		fixScriptProperty();
	}

	public int[][] findLegislatures() throws Throwable {
		CookieManager cm = new CookieManager();
		cm.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cm);
		URL url = new URL(SIL_SESSION_SUMMARY_URL);
		HttpURLConnection  connection = (HttpURLConnection) url.openConnection();
		connection.setConnectTimeout(40000);
		connection.setReadTimeout(40000);
		connection.setRequestMethod("GET");
		//for(String[] head:HEADER_PROPERTIES)
		//	connection.setRequestProperty(head[0], head[1]);
        connection.setInstanceFollowRedirects(false);
		
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setUseCaches(true);
        connection.setAllowUserInteraction(false);
		//connection.setDoOutput(true);

		TagNode document = cleaner.clean(new InputStreamReader(connection
				.getInputStream(), "utf-8"));

		// find properties

		setUpAllProperties(document);
		// Find Legislatures

		TagNode select = document.findElementByAttValue("name",
				"ctl00$mainPlaceHolder$ddlLegislaturas", true, false);
		TagNode[] legislaturesNodes = select.getAllElements(false);

		int[][] legislaturesIds = new int[legislaturesNodes.length][2];
		for (int i = 0; i < legislaturesNodes.length; i++) {
			legislaturesIds[i][0] = Integer.parseInt(legislaturesNodes[i]
					.getAttributeByName("value"));
			legislaturesIds[i][1] = Integer.parseInt(legislaturesNodes[i]
					.getText().toString());
		}
		return legislaturesIds;
	}

	public void getSessionSummaryFromLegislature(int id) throws Throwable {

		getSessionSummaryFromLegislature(id, null);
	}

	private String getParamString() throws UnsupportedEncodingException {
		return getParamString(PROPERTIES);
	}
	private String getParamString(String[][] paramsArray) throws UnsupportedEncodingException {
		String params = "";
		for (String[] property : paramsArray) {
			params += "&" + URLEncoder.encode(property[0],"UTF-8") + "=" + URLEncoder.encode(property[1],"UTF-8");
		}
		params = params.substring(1);
		return params;
	}
	private void fixScriptProperty() {
		PROPERTIES[6][1] = "ctl00$mainPlaceHolder$UpdatePanel1|"
				+ PROPERTIES[0][1];
	}
	@SuppressWarnings("unchecked")
	public List<SilSession> getSessionSummaryFromLegislature(int id, String page)
			throws Throwable {
		URL url = new URL(SIL_SESSION_SUMMARY_URL);
		if(page == null)
			PROPERTIES[0][1] = "ctl00$mainPlaceHolder$ddlLegislaturas";
		//URLConnection connection = url.openConnection();
		HttpURLConnection  connection = (HttpURLConnection) url.openConnection();
		connection.setConnectTimeout(40000);
		connection.setReadTimeout(40000);
		connection.setRequestMethod("POST");
		for(String[] property: PROPERTIES)
			if(property[0].equals("ctl00$mainPlaceHolder$ddlLegislaturas"))
				property[1] = ""+id;
		if (page != null)// select page
			PROPERTIES[0][1] =  page;
		
		fixScriptProperty();
		String params = getParamString();
		connection.addRequestProperty("Content-Length",""+params.length());
		
		

		connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows 2000)");
		connection.setInstanceFollowRedirects(false);

        connection.setDoOutput(true);
        connection.setUseCaches(true);
        connection.setAllowUserInteraction(false);

        OutputStreamWriter ps = new OutputStreamWriter(connection.getOutputStream());
        ps.write(params);
        ps.flush();
        System.out.println("RESPUESTA: "+connection.getResponseCode());
		//for (String cookie : cookies) {
		//    connection.addRequestProperty("Cookie", cookie.split(";", 2)[0]);
		//}

		
		TagNode document = cleaner.clean(new InputStreamReader(connection
				.getInputStream(), "utf-8"));
		TagNode centralTable = (TagNode) document.findElementByAttValue("id",
				"ctl00_mainPlaceHolder_UpdatePanel1", true, false);
		TagNode tbody = centralTable.findElementByName("tbody", true);
		List<SilSession> silSessions = new ArrayList<SilSession>();
		List<TagNode> sessions = tbody.getChildren();
		for (TagNode child : sessions) {
			SilSession tmp = new SilSession();
			SessionChamber tmpSession = new SessionChamber();
			tmp.setSession(tmpSession);
			tmpSession.setLegislature(new Long(id));
			tmp.setChamber("C.Diputados");
			
			List<TagNode> row = child.getChildren();
			List<String> values = new ArrayList<String>();
			for (TagNode td : row) {
				System.out.print(td.getText().toString());
				values.add(td.getText().toString());
			}
			tmp.setDate(values.get(0));
			int firstBlankSpace = values.get(1).indexOf(" ")+1;
			int secondBlankSpace = values.get(1).indexOf(" ",firstBlankSpace);
			tmpSession.setNumber(Long.parseLong(values.get(1).substring(firstBlankSpace,secondBlankSpace-1)));
			TagNode link = child.findElementByName("a", true);
			String idsession = link.getAttributeByName("href");
			findSessionDetails(tmp,idsession.substring(idsession.indexOf("=")+1));
			System.out.println("");
		}
		List<TagNode> linkContinue = centralTable.getElementListByAttValue(
				"class", "next", true, false);

		if (linkContinue.size() != 0) {
			TagNode liContinue = linkContinue.get(0);
			List<TagNode> aList = liContinue.getElementListHavingAttribute(
					"href", true);
			TagNode link = aList.get(0);
			String pageLink = link.getAttributeByName("href");
			int first = pageLink.indexOf('\'') + 1;
			int second = pageLink.indexOf('\'', first);
			pageLink = pageLink.substring(first, second);
			System.out.println(pageLink);

			// find properties
			//setUpAllProperties(document);
			//silSessions.addAll(getSessionSummaryFromLegislature(id, pageLink));
		}
		return silSessions;
	}
	private void findSessionDetails(SilSession silSession,String sessionid)throws Throwable{
		System.out.println("Buscando detalle: "+sessionid);
		findSessionAccount(silSession,sessionid);
		findSessionTable(silSession,sessionid);
		findSessionAssistants(silSession,sessionid);
		findSessionVotes(silSession,sessionid);
		
	}
	@SuppressWarnings("unchecked")
	private void findSessionVotes(SilSession silSession,String sessionId)throws Throwable{
		String[][] params = new String[1][2];
		params[0][0] = "prmid";
		params[0][1] = sessionId;
		String urlString = SIL_BASE_URL + "sesion_votaciones.aspx";

		HttpURLConnection connection = setUpGetConnection(urlString, params);
		TagNode document = cleaner.clean(new InputStreamReader(connection
				.getInputStream(), "utf-8"));

		TagNode[] votesSummaries = document.getElementsByAttValue("class",
				"stress", true, false);
		if (votesSummaries == null) {
			System.out.println("Session " + sessionId+ " doesn't have Assistance data.");
			return;
		}
		List<SilVote> votes = new ArrayList<SilVote>();
		for(TagNode voteNode : votesSummaries){
			TagNode[] ps = voteNode.getElementsByName("p", true);
			SilVote vote = new SilVote();
			for(TagNode p : ps){
				TagNode link = p.findElementByName("a", true);
				if(link != null){
					try{
						String href = link.getAttributeByName("href");
						findVoteDetails(vote,href);
						votes.add(vote);
					}catch(Exception e){
						e.printStackTrace();
					}
				}
					
			}
		}

	}
	private void findVoteDetails(SilVote vote, String link){
		
	}
	
	@SuppressWarnings("unchecked")
	private void findSessionAssistants(SilSession silSession,String sessionId)throws Throwable{
		String[][] params = new String[1][2];
		params[0][0] = "prmid";
		params[0][1] = sessionId;
		String urlString = SIL_BASE_URL+"sesion_asistencia.aspx";
		
		HttpURLConnection connection = setUpGetConnection(urlString,params);
		TagNode document = cleaner.clean(new InputStreamReader(connection
				.getInputStream(), "utf-8"));

		TagNode detailTable = document.findElementByAttValue("class",
				"col detalle", true, false);
		if (detailTable == null) {
			System.out.println("Session " + sessionId
					+ " doesn't have Assistance data.");
			return;
		}
		List<TagNode> tr = (List<TagNode>) detailTable.getElementListByName(
				"tr", true);
		int tablew = 4;
		int tableh = tr.size();

		String[][] assistants = new String[tableh][tablew];

		for (int i = 0; i < tableh; i++) {
			TagNode currentTr = tr.get(i);
			List<TagNode> td = (List<TagNode>) currentTr.getElementListByName(
					"td", true);
			for (int j = 0; j < tablew && j < td.size(); j++) {
				assistants[i][j] = td.get(j).getText().toString();
			}
		}
		silSession.setAssistants(assistants);

	}

	private void findSessionAccount(SilSession silSession, String sessionId)
			throws Throwable {
		String[][] params = new String[1][2];
		params[0][0] = "prmid";
		params[0][1] = sessionId;
		String urlString = SIL_BASE_URL + "sesion_cuenta.aspx";

		HttpURLConnection connection = setUpGetConnection(urlString, params);
		TagNode document = cleaner.clean(new InputStreamReader(connection
				.getInputStream(), "utf-8"));
		try {
			TagNode ultable = document.findElementByAttValue("id",
					"ctl00_mainPlaceHolder_docpdf", true, false);
			TagNode link = ultable.findElementByName("a", true);
			String href = link.getAttributeByName("href");
			String absolutePath = CHAMBER_BASE_URL + href;
			System.out.println(absolutePath);
			silSession.getSession().setSessionAccountURL(absolutePath);
		} catch (NullPointerException e) {
			System.out.println("Session " + sessionId
					+ " doesn't have an Account document.");
		}
	}

	private void findSessionTable(SilSession silSession, String sessionId)
			throws Throwable {
		String[][] params = new String[1][2];
		params[0][0] = "prmid";
		params[0][1] = sessionId;
		String urlString = SIL_BASE_URL + "sesion_tabla.aspx";

		HttpURLConnection connection = setUpGetConnection(urlString, params);
		TagNode document = cleaner.clean(new InputStreamReader(connection
				.getInputStream(), "utf-8"));
		try {
			TagNode ultable = document.findElementByAttValue("id",
					"ctl00_mainPlaceHolder_docpdf", true, false);
			TagNode link = ultable.findElementByName("a", true);
			String href = link.getAttributeByName("href");
			String absolutePath = CHAMBER_BASE_URL + href;
			System.out.println(absolutePath);
			silSession.getSession().setSessionTableURL(absolutePath);
		} catch (NullPointerException e) {
			System.out.println("Session " + sessionId
					+ " doesn't have a Table document.");
		}
	}

	private HttpURLConnection setUpGetConnection(String urlString,
			String[][] params) throws IOException {
		String paramsEncoded = getParamString(params);
		URL url = new URL(urlString + "?" + paramsEncoded);

		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setConnectTimeout(40000);
		connection.setReadTimeout(40000);
		connection.setRequestMethod("GET");
		connection.setRequestProperty("User-Agent",
				"Mozilla/4.0 (compatible; MSIE 6.0; Windows 2000)");
		connection.setInstanceFollowRedirects(false);

		connection.setDoOutput(true);
		connection.setUseCaches(true);
		connection.setAllowUserInteraction(false);

		return connection;
	}

}
