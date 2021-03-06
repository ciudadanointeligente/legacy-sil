package cl.ciudadanointeligente.sil.parser;

import cl.votainteligente.legislativo.model.Bill;
import cl.votainteligente.legislativo.model.Person;

import cl.ciudadanointeligente.sil.model.SilBill;

import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.text.*;
import java.util.*;

public class BillParser {
	private final String SIL_BASE_URL = "http://sil.senado.cl/cgi-bin/";
	private final String SIL_BILL_SUMMARY_URL = SIL_BASE_URL + "sil_ultproy.pl";
	private final String SIL_BILL_URL = SIL_BASE_URL + "sil_proyectos.pl";

	private final DateFormat df;
	private final DateFormat bdf;
	private final DateFormat dfLey;
	private final DateFormat dfLeyAlt;
	private final NumberFormat nfLey;
	private final HtmlCleaner cleaner;

	public BillParser() {
		df = new SimpleDateFormat("dd/MM/yyyy");
		bdf = new SimpleDateFormat("EEEE d 'de' MMMM, yyyy", new Locale("es", "CL"));
		dfLey = new SimpleDateFormat("yyyy-MM-dd");
		dfLeyAlt = new SimpleDateFormat("dd/MM/yy");
		nfLey = new DecimalFormat("###,###,###", new DecimalFormatSymbols(new Locale("es", "CL")));
		cleaner = new HtmlCleaner();
	}

	@SuppressWarnings("unchecked")
	public List<SilBill> getBills(Date startDate, Date endDate) throws Throwable {
		if (startDate == null || endDate == null) {
			throw new Exception("Fecha inválida");
		}

		List<SilBill> bills = new ArrayList<SilBill>();

		String formattedStartDate = df.format(startDate);
		String formattedEndDate = df.format(endDate);
		URL url = new URL(SIL_BILL_SUMMARY_URL);
		URLConnection connection = url.openConnection();
		connection.setDoOutput(true);

		OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
		out.write("desde=" + formattedStartDate + "&hasta=" + formattedEndDate + "&buscar=%3E%3E+Buscar");
		out.close();

		TagNode document = cleaner.clean(new InputStreamReader(connection.getInputStream(), "ISO-8859-1"));
		List<TagNode> linkCells = document.getElementListByAttValue("class", "TEXTpais", true, true);

		for (TagNode linkCell : linkCells) {
			TagNode linkNode = linkCell.findElementByName("a", true);
			URL billUrl = new URL(SIL_BASE_URL + linkNode.getAttributeByName("href").toString().replaceAll("^.*cgi-bin/", ""));
			URLConnection billConnection = billUrl.openConnection();
			TagNode billDocument = cleaner.clean(new InputStreamReader(billConnection.getInputStream(), "ISO-8859-1"));
			SilBill bill = parseBillDocument(billDocument);
			bills.add(bill);
		}

		return bills;
	}

	public SilBill getBill(String searchBulletinNumber) throws Throwable {
		if (searchBulletinNumber == null || searchBulletinNumber.length() == 0) {
			throw new Exception("Boletín inválido");
		}

		URL url = new URL(SIL_BILL_URL);
		URLConnection billConnection = url.openConnection();
		billConnection.setDoOutput(true);

		OutputStreamWriter out = new OutputStreamWriter(billConnection.getOutputStream());
		out.write("nboletin=" + searchBulletinNumber + "&buscar=%3E%3E+Buscar");
		out.close();

		TagNode billDocument = cleaner.clean(new InputStreamReader(billConnection.getInputStream(), "ISO-8859-1"));
		return parseBillDocument(billDocument);
	}

	@SuppressWarnings("unchecked")
	public SilBill parseBillDocument(TagNode billDocument) throws Throwable {
		TagNode spanNumBoletin = billDocument.findElementByAttValue("class", "azu", true, true);
		TagNode spanTitulo = billDocument.findElementByAttValue("class", "TEXTpais", true, true);
		TagNode[] spanDetalle = billDocument.getElementsByAttValue("class", "TEXTarticulo", true, true);

		if (spanDetalle.length < 7 || spanDetalle.length > 8) {
			throw new Exception("Estructura de proyecto de ley desconocida");
		}

		String bulletinNumber = spanNumBoletin.getText().toString().trim();
		String title = spanTitulo.getText().toString().trim();
		Date entryDate = bdf.parse(spanDetalle[0].getText().toString().trim());
		String initiative = spanDetalle[spanDetalle.length == 7 ? 1 : 2].getText().toString().trim();
		String type = spanDetalle[spanDetalle.length == 7 ? 2 : 3].getText().toString().trim();
		String originChamber = spanDetalle[spanDetalle.length == 7 ? 3 : 4].getText().toString().trim();
		String urgency = spanDetalle[spanDetalle.length == 7 ? 4 : 5].getText().toString().trim();
		String stage = spanDetalle[spanDetalle.length == 7 ? 5 : 6].getText().toString().trim();
		String substage = spanDetalle[spanDetalle.length == 7 ? 6 : 7].getText().toString().trim();

		Bill bill = new Bill();
		bill.setBulletinNumber(bulletinNumber);
		bill.setTitle(title);
		bill.setEntryDate(entryDate);
		bill.setInitiative(initiative);
		bill.setType(type);
		bill.setUrgency(urgency);
		bill.setSummary(title);
		SilBill silBill = new SilBill();
		silBill.setOriginChamberName(originChamber);
		silBill.setStageName(stage);
		silBill.setSubstageName(substage);
		silBill.setBill(bill);

		TagNode linkLey = spanDetalle[spanDetalle.length == 7 ? 6 : 7].findElementByName("a", true);

		if (linkLey != null) {
			String bcnUrl = linkLey.getAttributeByName("onClick").replaceAll(".*'\\s*(http://.*?)',.*", "$1");
			Long bcnId = (Long) nfLey.parse(linkLey.getText().toString().trim().replaceAll("Ley\\s+N.\\s*", "").replaceAll("D[\\.]{0,1}\\s*S[\\.]{0,1}\\s*(N.){0,1}\\s*", ""));
			Date bcnDate = bcnUrl.matches(".*idVersion=\\d\\d\\d\\d-\\d\\d-\\d\\d") ? dfLey.parse(bcnUrl.replaceAll(".*idVersion=(\\d\\d\\d\\d-\\d\\d-\\d\\d)", "$1")) : null;

			if (bcnDate == null) {
				try {
					bcnDate = dfLeyAlt.parse(linkLey.getParent().getText().toString().trim().replaceAll(".*\\(D\\.Oficial: (\\d\\d/\\d\\d/\\d\\d)\\).*", "$1"));
				} catch (ParseException pe) {
					System.err.println("WARN: No se pudo determinar fecha de publicación para esta ley o decreto");
				}
			}

			if (bcnUrl.matches("^http://.*?/Navegar\\?idLey=.*")) {
				bill.setBcnLawId(bcnId);
				bill.setBcnLawURL(bcnUrl);
				bill.setPublicationDate(bcnDate);
			} else if (bcnUrl.matches("^http://.*?/Navegar\\?idNorma=.*")) {
				bill.setDecree(bcnId);
				bill.setDecreeUrl(bcnUrl);
				bill.setPublicationDate(bcnDate);
			}
		} else {
			silBill.setSubstageName(substage);
		}

		if (spanDetalle.length == 8) {
			silBill.setMergedBulletinNumbers(spanDetalle[1].getText().toString().trim().split(" +"));
		}

		Long internalNumber = Long.parseLong(billDocument.findElementByAttValue("target", "cont_if1", true, true).getAttributeByName("href").replaceAll(".*\\.pl\\?(\\d+).*", "$1"));
		bill.setSilIndicationsId(internalNumber);
		bill.setSilOficiosId(internalNumber);
		bill.setSilProcessingsId(internalNumber);
		bill.setSilUrgenciesId(internalNumber);

		bill.setUpdatedAt(new Date());
		bill.setCreatedAt(bill.getUpdatedAt());

		Set<Person> authors = new HashSet<Person>();
		TagNode linkAuthor = billDocument.findElementByAttValue("onClick", "ima_ck('aut')", true, true);

		if (linkAuthor != null) {
			URL authorUrl = new URL(SIL_BASE_URL + linkAuthor.getAttributeByName("href"));
			URLConnection authorConnection = authorUrl.openConnection();
			TagNode authorDocument = cleaner.clean(new InputStreamReader(authorConnection.getInputStream(), "ISO-8859-1"));
			List<TagNode> authorCells = authorDocument.getElementListByAttValue("class", "TEXTarticulo", true, true);

			for (TagNode authorSpan : authorCells) {
				String[] authorNames = authorSpan.getText().toString().replaceAll("&nbsp;", "").split(", ");
				Person author = new Person();
				author.setLastName(authorNames[0]);
				author.setFirstName(authorNames[1]);
				author.setUpdatedAt(new Date());
				author.setCreatedAt(author.getUpdatedAt());
				authors.add(author);
			}
		}

		bill.setAuthors(authors);
		return silBill;
	}
}
