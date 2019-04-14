package no.vegvesen.tk.signering;


import com.github.ulisesbocchio.jar.resources.JarResourceLoader;
import no.bbs.trust.esignclientapi.exception.ESignClientException;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.PropertySource;

import java.io.IOException;

@SpringBootApplication
@PropertySource("classpath:application.properties")
public class Main {



    public static void main(String[] args) throws IOException, ESignClientException {
        System.setProperty("jsse.enableSNIExtension", "false");
        new SpringApplicationBuilder()
                .sources(Main.class)
                .resourceLoader(new JarResourceLoader())
                .run(args);
    }

}
