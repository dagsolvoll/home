package eu.nets.sis.esign.demoapp.controller;

import eu.nets.sis.esign.demoapp.model.Sdo;
import eu.nets.sis.esign.demoapp.util.SignAndSendXMLRequest;
import no.bbs.trust.esignclientapi.exception.ESignClientException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;

/**
 * 
 * Controller that handles various statuses and home page request of the application
 * by mapping the requests to the appropriate views
 *
 */
@Controller
public class StatusHandlerController {

	private static final Logger LOGGER = Logger.getLogger(StatusHandlerController.class);
	
	@Value("${base.url}")
	private String baseUrl;
	
	@Value("${esign.valid.status}")
	private String validStatus;

	@Autowired
	private SignAndSendXMLRequest signAndSendXMLRequest;

	/***
	 * Handles the calls to verify,signing completed ,rejected/cancelled calls
	 * @param status
	 * @return -the view name 
	 */
	@RequestMapping(value = "/verify", method = RequestMethod.GET)
	public String statusHandler(@RequestParam("status") String status, Model model) {

		LOGGER.info("Request received for /verify");
		model.addAttribute("homeUrl", baseUrl + "/esign");

		if (validStatus.contains(status)) {
			LOGGER.info("Signing " + status);
			model.addAttribute("status", status);
			try {
				Sdo sdo = signAndSendXMLRequest.getSdo();
				model.addAttribute("sdo", sdo);
			} catch (ESignClientException | IOException e ) {
				e.printStackTrace();
			}
			return "status";
		}

		LOGGER.info("returns Error");
		return "error";
	}

	/***
	 * Handles the calls to verify,signing completed ,rejected/cancelled calls
	 * @param status
	 * @return -the view name
	 */

	@RequestMapping(value = "/verify1", method = RequestMethod.GET)
	public RedirectView statusHandler2(@RequestParam("status") String status, Model model) {

		LOGGER.info("Request received for /verify1");
		model.addAttribute("homeUrl", baseUrl + "/esign");

		if (validStatus.contains(status)) {
			String url = signAndSendXMLRequest.getSignUrlFromSigningProcess2();
			url = url.replaceAll("sref", "ref");

			return new RedirectView(url);
		}

		LOGGER.info("returns Error");
		return new RedirectView("error");
	}

	@RequestMapping(value = "/esign", method = RequestMethod.GET)
    public String home() {
		
		LOGGER.info("Request received for /esign");
        return "index";
    }
}
