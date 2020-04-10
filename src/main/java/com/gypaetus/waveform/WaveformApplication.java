package com.gypaetus.waveform;

import com.gypaetus.waveform.waveform.Waveform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

import java.io.BufferedWriter;
import java.io.FileWriter;

@SpringBootApplication
public class WaveformApplication implements CommandLineRunner {
    private static Logger LOG = LoggerFactory.getLogger(WaveformApplication.class);

    @Autowired
    ApplicationContext applicationContext;

    public static void main(String[] args) {
        LOG.info("Running");
        SpringApplication.run(WaveformApplication.class, args);
        LOG.info("Done");
    }

    @Override
    public void run(final String... args) {
        try {
            Resource[] resources = applicationContext.getResources("classpath:input/*.wav");

            for (Resource resource : resources) {
                short[][] waveform = Waveform.getWaveformFromFile(resource.getFile());

                BufferedWriter br = new BufferedWriter(new FileWriter(resource.getFilename() + ".csv"));
                StringBuilder sb = new StringBuilder();

                for (int i = 0; i < waveform[0].length; i++) {
                    sb.append(i);
                    System.out.print(i);
                    for (int j = 0; j < waveform.length; j++) {
                        sb.append("\t" + waveform[j][i]);
                    }
                    sb.append("\n");
                }
                br.write(sb.toString());
                br.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
