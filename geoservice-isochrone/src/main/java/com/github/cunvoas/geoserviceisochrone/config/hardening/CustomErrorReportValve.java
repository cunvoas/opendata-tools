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

// Converting this to Kotlin results in this class not being used.
/**
 * @see https://www.springcloud.io/post/2022-07/spring-boot-hardening/#gsc.tab=0
 */
public class CustomErrorReportValve extends ErrorReportValve {

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
                writer.write("<!doctype html><html lang=\"en\"><title>error</title><body>Ups! " + statusCode + "</body></html>");
                response.finishResponse();
            }
        } catch (IOException | IllegalStateException e) {
            // Ignore
        }
    }
}