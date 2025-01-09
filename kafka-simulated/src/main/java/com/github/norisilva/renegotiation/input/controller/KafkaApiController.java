package com.github.norisilva.renegotiation.input.controller;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Path("/send")
@RequiredArgsConstructor
public class KafkaApiController {

    @POST
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    public Response handler(byte[] data) {
        // Imprime o tamanho do objeto recebido
        System.out.println("Tamanho: " + data.length + " bytes");

        // Imprime a data e hora de recebimento
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        System.out.println("Recebido em: " + now.format(formatter));

        // Envia o objeto para outra API
        try {
            URL url = new URL("http://localhost:9595/consumer");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", MediaType.APPLICATION_OCTET_STREAM);

            try (OutputStream os = connection.getOutputStream()) {
                os.write(data);
                os.flush();
            }

            int responseCode = connection.getResponseCode();
            System.out.println("Response from consumer API: " + responseCode);

            if (responseCode != HttpURLConnection.HTTP_OK) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        return Response.status(Response.Status.ACCEPTED).build();
    }
}