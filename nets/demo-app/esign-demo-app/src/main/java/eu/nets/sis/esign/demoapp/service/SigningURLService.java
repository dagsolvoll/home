package eu.nets.sis.esign.demoapp.service;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

import eu.nets.sis.esign.demoapp.util.SignAndSendXMLRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

/**
 * REST service that returns signing URL to the application
 * 
 *
 */
@RestController
@RequestMapping(path = "/signing/*")
public class SigningURLService {
	
	private static final Logger LOGGER = Logger.getLogger(SigningURLService.class);

	@Autowired
	private SignAndSendXMLRequest signAndSendXMLRequest;

	/**
	 * Handles the call to redirect to signing
	 * @param docType Type of the document -for eg: PDF or TEXT
	 * @param pki Name of the PKI
	 * @return url of the view
	 */
	@GetMapping(path = "/url/{pki}/{docType}")
	public RedirectView getUrl(@PathVariable String docType, @PathVariable String pki) {
		
		LOGGER.info(String.format("Request received for /signing/url/%s/%s", pki, docType));
		String param = pki + "_" + docType;
		String url = signAndSendXMLRequest.getSigningURL(param, "", "");
		url = url.replaceAll("sref", "ref");

		if ("BankIdSe".equals(pki)) {
			url = url + "&autostart=true";
		}

		LOGGER.info("Redirecting to E-Signing service");
		return new RedirectView(url);
	}

	@PostMapping(path = "/url/{pki}/{docType}")
	public RedirectView getUrl(@PathVariable String docType, @PathVariable String pki, @RequestParam("file_xml") MultipartFile file_xml,
							   @RequestParam("file_xsl") MultipartFile file_xsl ) throws IOException {

		String param = pki + "_" + docType;
		LOGGER.info("POST " + pki + ", " + docType + ", " + file_xml.getOriginalFilename());
		String xmlStr = convertToBasic64(file_xml.getInputStream(), Charset.defaultCharset());
		String xslStr = convertToBasic64(file_xsl.getInputStream(), Charset.defaultCharset());
		LOGGER.info("XML " + xmlStr);
		LOGGER.info("XML " + xslStr);
		String url = signAndSendXMLRequest.getSigningURL(param, xmlStr, xslStr);

		url = url.replaceAll("sref", "ref");

		if ("BankIdSe".equals(pki)) {
			url = url + "&autostart=true";
		}

		LOGGER.info("Redirecting to E-Signing service " + url);
		return new RedirectView(url);
	}


	private String convertToBasic64(InputStream stream, Charset charset) throws IOException {
		String content = convert(stream, charset);
		return Base64.getEncoder().encodeToString(content.getBytes());
	}

	private String convert(InputStream inputStream, Charset charset) throws IOException {

		StringBuilder stringBuilder = new StringBuilder();
		String line = null;

		try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, charset))) {
			while ((line = bufferedReader.readLine()) != null) {
				stringBuilder.append(line);
			}
		}

		return stringBuilder.toString();
	}
}
