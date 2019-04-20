package eu.nets.sis.esign.demoapp.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.ArrayList;
import java.util.Date;

import javax.annotation.PostConstruct;

import eu.nets.sis.esign.demoapp.model.Certificate;
import eu.nets.sis.esign.demoapp.model.Sdo;
import eu.nets.sis.esign.demoapp.model.Signatur;
import no.bbs.tt.trustsign.tsm.xml.messages.*;
import no.bbs.tt.trustsign.tsm.xml.messages.containers.*;
import org.apache.log4j.Logger;
import org.bouncycastle.cms.SignerId;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import no.bbs.trust.esignclientapi.exception.ESignClientException;
import no.bbs.trust.esignclientapi.impl.ESigningFacade;
import no.bbs.trust.esignclientapi.impl.ESigningFactory;
import no.bbs.trust.esignclientapi.impl.MerchantContext;
import no.bbs.trust.esignclientapi.statics.Environment;
import no.bbs.trust.esignclientapi.statics.KeyStoreType;

/**
 * 
 * Utility class that is used to insert signing order and get 
 * signing url
 *
 */
@Component
@PropertySource("classpath:application.properties")
public class SignAndSendXMLRequest {
	
	private static final Logger LOGGER = Logger.getLogger(SignAndSendXMLRequest.class);
	
	private static final String DOC1 = "doc1";
	private static final String USER1 = "user1";
	private static final String USER2 = "user2";
	private static final String GUI1 = "gui1";
	private static final String GUI2 = "gui2";

	@Value("classpath:${clienttruststore}")
	private Resource clientTrustStorePath;
	
	@Value("${clienttruststorepwd}")
	private String clientTrustStorePwd;
	
	@Value("${merchantid}")
	private String merchantId;
	
	@Value("${document.description}")
	private String description;
	
	@Value("${additionalinfo}")
	private String additionalInfo;

	@Value("${endusersigner}")
	private String enduserSigner;

	@Value("${endusersigner2}")
	private String enduserSigner2;

	@Value("${title}")
	private String title;
	
	@Value("${merchantname}")
	private String merchantName;
	
	@Value("${signurlbase}")
	private String signUrlBase;
	
	@Value("${pdf.doc.bytes}")
	private String pdfBase64String;

	@Value("${text.doc.bytes}")
	private String txtBase64String;

	@Value("${xml.doc.bytes}")
	private String xmlBase64String;

	@Value("${xsl.doc.bytes}")
	private String xslBase64String;

	@Value("${styleurl}")
	private String styleUrl;
	
	@Value("${base.url}")
	private String baseUrl;
	
	@Autowired
	private ApplicationArguments applicationArguments;

	private String orderId;
	private long merchantLongId;
	public String pades;
	public String messId;
	/**
	 * Register merchant context after bean creation
	 */
	@PostConstruct
	public void registerMerchantContext() {
		String[] args = applicationArguments.getSourceArgs();
		
		Security.addProvider(new BouncyCastleProvider());
		try {
			MerchantContext merchantContext = new MerchantContext();
			merchantContext.setSslKeystorePath(args[0]);
			merchantContext.setSslKeystorePwd(args[1]);
			merchantContext.setSigningKeystorePath(args[0]);
			merchantContext.setSigningKeystorePwd(args[1]);
			merchantContext.setTruststorePath(clientTrustStorePath.getFile().getPath());
			merchantContext.setTruststorePwd(this.clientTrustStorePwd);
			merchantContext.setTruststoreType(KeyStoreType.JKS);
			merchantContext.setCommTimeout(10000);
			merchantContext.setEnv(Environment.PRE_PRODUCTION);
			merchantContext.setMerchantId(merchantId);
			merchantContext.setMerchantName(merchantName);
			
			ESigningFactory.INSTANCE.registerMerchantContext(merchantContext);
		} catch (ESignClientException | IOException e) {
			LOGGER.error("E-Sign client api setup has failed", e);
			System.out.println("\n\n<<<<<<<<<<ERROR>>>>>>>>>>\n\nE-Sign client api setup has failed.\n\nErrorMessage=" + e.getMessage() + ".\n\n");
			System.exit(0);
		}
	}

	public String getPades() throws ESignClientException {

		ESigningFacade facade = ESigningFactory.INSTANCE.getESigningFacade(merchantName);
		PDF pdf = new PDF();
		Doc doc = new Doc();
		pdf.setB64DocumentBytesAsString(pdfBase64String);
		doc.setDocType(pdf);

		GetPAdESRequest req = new GetPAdESRequest();
		req.setOrderID(orderId);
		req.setMerchantID(merchantLongId);
		req.setLocalDocumentRef(DOC1);
		req.setpAdESDocumentReference(pdfBase64String);
		req.setIncludeSSN(true);
		req.setTime(new Date());
		req.setMessageID(messId);

		GetPAdESResponse resp = facade.getPAdES(req);

		return resp.getPAdESSignedDocumentBytes();
	}

	public Sdo getSdo() throws ESignClientException, IOException {

		ESigningFacade facade = ESigningFactory.INSTANCE.getESigningFacade(merchantName);

		GetSDORequest req = new GetSDORequest();
		req.setOrderID(orderId);
		req.setMerchantID(merchantLongId);
		req.setTime(new Date());
		req.setMessageID(messId);

		GetSDOResponse resp = facade.getSDO(req);

		Certificate cert = getSdoDetails(resp.getB64SDOBytes());

		byte [] ar = Base64.decode(resp.getB64SDOBytes());

		return new Sdo(cert, new String(ar, StandardCharsets.UTF_8));
	}

	/***
	 * Gets Sign url to sign using the requested pki
	 * @param pki '_' separated pki name and document type
	 * @return url url of the signing page
	 */
	public String getSigningURL(String pki, String xmlSTr, String xslString) {
		String url = null;
		try {
			String[] idpArray = pki.split("_");
			InsertOrderResponse insertOrderResponse = insertOrder(idpArray[0], idpArray[1], xmlSTr, xslString);
			url = getSignUrlFromSigningProcess(insertOrderResponse);
			if(url != null) {
				url = url.replaceAll("sref", "ref");
			}
		} catch (Exception e) {
			LOGGER.error("Got Exception during get Signurl from Nets ", e);
		}
		return url;
	}

	/***
	 * Prepares an insert order xml to make a request to get the sign url
	 * @param insertOrderResponse  outcome of the insert order
	 * @return url url of the signing page
	 */
	private String getSignUrlFromSigningProcess(InsertOrderResponse insertOrderResponse) {
		String signUrl = null;
		try {
			
			LOGGER.info("Retrieving SignURL");
			orderId = insertOrderResponse.getOrderID();
			merchantLongId = insertOrderResponse.getMerchantID();
			messId = insertOrderResponse.getMessageID();
			LOGGER.info("Order(" + orderId + ", " + merchantLongId + ", " + messId );

			ESigningFacade facade = ESigningFactory.INSTANCE.getESigningFacade(merchantName);
			GetSigningProcessesRequest req = new GetSigningProcessesRequest();
			req.setOrderID(insertOrderResponse.getOrderID());
			req.setMerchantID(insertOrderResponse.getMerchantID());
			req.setMessageID(insertOrderResponse.getMessageID());
			req.setTime(new Date());
			req.setLocalSignerReference(USER1);
			GetSigningProcessesResponse response = facade.getSigningProcesses(req);
			ArrayList<SigningProcessResult> signinprocesses = response.getSigningProcessResults();
			if (null != signinprocesses) {
				for (SigningProcessResult sp : signinprocesses) {

					signUrl = sp.getSignURL();
				}
			}
			return signUrl;
		} catch (ESignClientException t) {
			LOGGER.error("Got ESignClientException during getSignurl ", t);

		} catch (Throwable t) {
			LOGGER.error("Got exception during getSignurl ", t);

		}
		return signUrl;
	}


	public String getSignUrlFromSigningProcess2() {
		String signUrl = null;
		try {
			ESigningFacade facade = ESigningFactory.INSTANCE.getESigningFacade(merchantName);
			GetSigningProcessesRequest req = new GetSigningProcessesRequest();
			req.setOrderID(orderId);
			req.setMerchantID(merchantLongId);
			req.setMessageID(messId);
			req.setTime(new Date());
			req.setLocalSignerReference(USER2);
			GetSigningProcessesResponse response = facade.getSigningProcesses(req);
			ArrayList<SigningProcessResult> signinprocesses = response.getSigningProcessResults();
			if (null != signinprocesses) {
				for (SigningProcessResult sp : signinprocesses) {

					signUrl = sp.getSignURL();
				}
			}
		} catch (ESignClientException e) {
			e.printStackTrace();
		}
		return signUrl;

	}


	/***
	 * Loads the document based on the document type requested by the user
	 * @param docType type of the document -PDF or TEXT
	 * @return list containing the doc to be signed
	 * @throws UnsupportedEncodingException
	 */
	private ArrayList<Doc> getDocument(String docType, String xmlStr, String xslStr) throws UnsupportedEncodingException {
		Text txt = null;
		PDF pdf = null;
		Doc doc = new Doc();
		if ("PDF".equals(docType)) {
			pdf = new PDF();
			pdf.setB64DocumentBytesAsString(pdfBase64String);
			doc.setDocType(pdf);
		} else if ("TXT".equals(docType)) {
			txt = new Text();
			txt.setB64DocumentBytesAsString(txtBase64String);
			doc.setDocType(txt);
		} else if ("XML".equals(docType)) {
			XML xml = new XML();
			if(xmlStr.length() > 0) {
				xml.setB64XMLBytesAsString(xmlStr);
				xml.setB64XSLBytesAsString(xslStr);
			} else {
				xml.setB64XMLBytesAsString(xmlBase64String);
				xml.setB64XSLBytesAsString(xslBase64String);
			}
			doc.setDocType(xml);
		}

		doc.setDesc(description);
		doc.setLocalDocRef(DOC1);
		doc.setTitle(title);
		doc.setRequiresAuthentication(false);
		ArrayList<Doc> documents = new ArrayList<>();
		documents.add(doc);
		return documents;
	}
	
	/***
	 * Creates an order to insert an order to get the sign url
	 * @param pki Name of the PKI
	 * @param docType type of the document -PDF or TEXT
	 * @return insertorderresponse response of the insert order
	 * @throws ESignClientException Esign Client API exception
	 * @throws UnsupportedEncodingException
	 */
	private InsertOrderResponse insertOrder(String pki, String docType, String xmlStr, String xslStr) throws ESignClientException, UnsupportedEncodingException {
		ESigningFacade facade = ESigningFactory.INSTANCE.getESigningFacade(merchantName);
		InsertOrderRequest request = new InsertOrderRequest();
		request.setMerchantID(Long.valueOf(merchantId));
		request.setMessageID("esignclientapi-test-1");
		request.setTime(new Date());
		request.setAdditionalInfo(additionalInfo);
		request.setOrderID(merchantId + "-esc-" + System.currentTimeMillis());
		request.setDocuments(getDocument(docType, xmlStr, xslStr));

		LOGGER.info(String.format("OrderID=%s, MerchantID=%s", request.getOrderID(), request.getMerchantID()));

		// Add Signers with preset accepted PKIs 
		ArrayList<IPKI> acceptedPKIs = new ArrayList<>();
		ArrayList<IPKI> acceptedPKIs2 = new ArrayList<>();
		if ("BankId".equals(pki)) {
			BankID bankid = new BankID();
			bankid.addCertificatePolicy("PersonalQualified");
			bankid.setSignerID(new SignerID("SSN", "09096522557"));
			acceptedPKIs.add(bankid);
			BankID bankid2 = new BankID();
			bankid2.addCertificatePolicy("PersonalQualified");
			bankid2.setSignerID(new SignerID("SSN", "02105892090"));
			acceptedPKIs2.add(bankid2);
		} else if ("NemId".equals(pki)) {
			NemID nemid = new NemID();
			acceptedPKIs.add(nemid);
		} else if ("PkiEid".equals(pki)) {
			Tupas tupas = new Tupas();
			acceptedPKIs.add(tupas);
		} else if ("BankIdSe".equals(pki)) {
			BankID_SE bankID_se = new BankID_SE();
			acceptedPKIs.add(bankID_se);
		}

		Signer signer1 = new Signer();
		signer1.setLocalSignerRef(USER1);
		signer1.setName(enduserSigner);
		signer1.setAcceptedPKIs(acceptedPKIs);

		Signer signer2 = new Signer();
		signer2.setLocalSignerRef(USER2);
		signer2.setName(enduserSigner2);
		signer2.setAcceptedPKIs(acceptedPKIs2);

		ArrayList<Signer> signers = new ArrayList<>();
		signers.add(signer1);
		signers.add(signer2);

		request.setSigners(signers);

		// ExecutionDetails
		ExecutionDetails executionDetails = new ExecutionDetails();
		executionDetails.setOrderDeadline(new Date(System.currentTimeMillis() + 86400000));
		executionDetails.setDisplayProcessInfo("NameStatusTime");
		Step step1 = new Step();
		step1.setStepNumber(1);

		SigningProcess signingProcess = new SigningProcess();
		signingProcess.setLocalDocumentReferance(DOC1);
		signingProcess.setLocalSignerReferance(USER1);
		signingProcess.setLocalWebContextRef(GUI1);
		SigningProcess signingProcess2 = new SigningProcess();
		signingProcess2.setLocalDocumentReferance(DOC1);
		signingProcess2.setLocalSignerReferance(USER2);
		signingProcess2.setLocalWebContextRef(GUI2);
		step1.addSigningProcess(signingProcess);
		step1.addSigningProcess(signingProcess2);

		executionDetails.addStep(step1);

		WebContext webContext = new WebContext();
		webContext.setExitURL(baseUrl + "/verify1?status=completed");
		webContext.setAbortURL(baseUrl + "/verify1?status=canceled");
		webContext.setLocalWebContextRef(GUI1);
		webContext.setSignURLBase(signUrlBase);
		webContext.setErrorURLBase(baseUrl + "/error.html");
		webContext.setStyleURL(this.styleUrl);

		WebContext webContext2 = new WebContext();
		webContext2.setExitURL(baseUrl + "/verify?status=completed");
		webContext2.setAbortURL(baseUrl + "/verify?status=canceled");
		webContext2.setLocalWebContextRef(GUI2);
		webContext2.setSignURLBase(signUrlBase);
		webContext2.setErrorURLBase(baseUrl + "/error.html");
		webContext2.setStyleURL(this.styleUrl);

		request.setExecutionDetails(executionDetails);
		ArrayList<WebContext> webContexts = new ArrayList<>();
		webContexts.add(webContext);
		webContexts.add(webContext2);
		request.setWebContexts(webContexts);

		LOGGER.info("Sending an InsertOrder request");
		return facade.insertOrder(request);
	}

	private Certificate getSdoDetails(String fileContent) throws IOException, ESignClientException  {
		int i=0, j=0;
		LOGGER.debug(fileContent);
		ArrayList<Certificate> list = new ArrayList<Certificate>();
		Certificate cert = new Certificate();

		ESigningFacade facade = ESigningFactory.INSTANCE.getESigningFacade(merchantName);
		GetSDODetailsRequest sdoRequest = new GetSDODetailsRequest();
		sdoRequest.setMerchantID(Long.valueOf(merchantId));
		sdoRequest.setMessageID("esignclientapi-test-3");
		sdoRequest.setTime(new Date());

		sdoRequest.setB64SDO(fileContent);
		sdoRequest.setVerifySDO(true);
		sdoRequest.setReturnSSN(true);
		sdoRequest.setReturnOrganizationNumber(true);

		GetSDODetailsResponse sdoResponse = facade.getSDODetails(sdoRequest);

		for (SDO sdos : sdoResponse.getSdos()) {
			i++;
			cert.setData(sdos.getB64SignedData());
			cert.setSeal(new Signatur(sdos.getSealSignature()));
			//list.add(new Certificate(true, sdos.getSealSignature()));
			for (SDOSignature signatures : sdos.getSdoSignatures()) {
				//list.add(new Certificate(false, signatures));
				cert.addSignatur(signatures);
				j++;
				LOGGER.info("-----------------Cert info in " + "filename" + " (" + i + ", " + j + ")");
				LOGGER.info("SDO cert1.SSN        = " + signatures.getSignerCertificateInfo().getSsn());
				LOGGER.info("SDO cert1.Cn         = " + signatures.getSignerCertificateInfo().getCn());
				LOGGER.info("SDO cert1.PKI Vendor = " + signatures.getSignerCertificateInfo().getPkiVendor());
				LOGGER.info("SDO cert1.OrganiNbmr = " + signatures.getSignerCertificateInfo().getOrganizationNumber());
				LOGGER.info("SDO cert1.O          = " + signatures.getSignerCertificateInfo().getO());
				LOGGER.info("SDO cert1.Policy     = " + signatures.getSignerCertificateInfo().getCertificatePolicy());
				LOGGER.info("SDO cert1.IssuerCn   = " + signatures.getSignerCertificateInfo().getIssuerCN());
				LOGGER.info("SDO cert1.UniqueId   = " + signatures.getSignerCertificateInfo().getUniqueId());
				LOGGER.info("SDO cert1.From       = " + signatures.getSignerCertificateInfo().getValidFrom());
				LOGGER.info("SDO cert1.To         = " + signatures.getSignerCertificateInfo().getValidTo());
				LOGGER.info("-----------------------------------------------------");
			}
		}
		return cert;
	}
}
