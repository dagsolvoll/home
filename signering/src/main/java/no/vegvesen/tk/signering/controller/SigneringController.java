package no.vegvesen.tk.signering.controller;

import no.bbs.trust.esignclientapi.exception.ESignClientException;
import no.vegvesen.tk.signering.service.NetsService;
import no.vegvesen.tk.signering.model.Certificate;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Base64;

@RestController
@RequestMapping(path = "/api/*")
public class SigneringController {

    private static final Logger LOGGER = Logger.getLogger(SigneringController.class);

    @Autowired
    private NetsService netsService;

    @GetMapping(path = "ssn")
    public Certificate getSsn(@RequestParam(name="name", required=false, defaultValue="World") String name, Model model) {
        model.addAttribute("name", name);
        LOGGER.info(String.format("Request received for /signing/ssn"));
        Certificate cert = null;
        try {
            cert =  netsService.getSsn();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ESignClientException e) {
            e.printStackTrace();
        }

        return cert;
    }

    //@RequestMapping(value = "/api/validate", headers = ("content-type=multipart/*"), method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PostMapping(path = "validate")
    public Certificate handleFileUpload(@RequestParam("file") MultipartFile file ) {

        Certificate cert = null;
        try {
            cert =  netsService.getSsn(convertToBasic64(file.getInputStream(), Charset.defaultCharset()));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ESignClientException e) {
            e.printStackTrace();
        }

        return cert;
    }

    public String convertToBasic64(InputStream stream, Charset charset) throws IOException {
        String content = convert(stream, charset);
        return Base64.getEncoder().encodeToString(content.getBytes());
    }

    public String convert(InputStream inputStream, Charset charset) throws IOException {

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
