package com.github.cunvoas.geoserviceisochrone.config.hardening;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ErrorReportValve;
import org.apache.coyote.ActionCode;
import org.apache.tomcat.util.ExceptionUtils;
import org.springframework.http.MediaType;

import lombok.extern.slf4j.Slf4j;

/**
 * Valve Tomcat personnalisée pour la gestion des rapports d’erreur HTTP.
 * Permet de contrôler la réponse envoyée au client en cas d’erreur serveur.
 * Utile pour renforcer la sécurité et masquer les détails techniques.
 *
 * @see https://www.springcloud.io/post/2022-07/spring-boot-hardening/#gsc.tab=0
 */
@Slf4j
public class CustomErrorReportValve extends ErrorReportValve {

    /**
     * @see ErrorReportValve.report.
     */
    @Override
    protected void report(final Request request, final Response response, final Throwable throwable) {
        // ref: ErrorReportValve implementation

        final int statusCode = response.getStatus();

        // Do nothing on a 1xx, 2xx and 3xx status
        // Do nothing if anything has been written already
        // Do nothing if the response hasn't been explicitly marked as in error
        //    and that error has not been reported.
        if (statusCode < 400 || response.getContentWritten() > 0 || !response.setErrorReported()) {
            return;
        }

        log.error("CustomErrorReportValve.statusCode {}", statusCode);
        log.error("CustomErrorReportValve.report {}", throwable);
        
        // If an error has occurred that prevents further I/O, don't waste time
        // producing an error report that will never be read
        final AtomicBoolean result = new AtomicBoolean(false);
        response.getCoyoteResponse().action(ActionCode.IS_IO_ALLOWED, result);
        if (!result.get()) {
            return;
        }

        try {
            try {
                response.setContentType(MediaType.TEXT_HTML_VALUE);
                response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            } catch (final Throwable t) {
                ExceptionUtils.handleThrowable(t);
                if (container.getLogger().isDebugEnabled()) {
                    container.getLogger().debug("status.setContentType", t);
                }
            }
            final Writer writer = response.getReporter();
            if (writer != null) {
                // If writer is null, it's an indication that the response has
                // been hard committed already, which should never happen
                writer.write("<!doctype html><html lang=\"fr\"><title>error</title><body>Oups! " + statusCode + "</body></html>");
                response.finishResponse();
            }
        } catch (IOException | IllegalStateException e) {
            // Ignore
        }
    }
}